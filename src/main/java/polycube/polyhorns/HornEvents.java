package polycube.polyhorns;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import polycube.polyhorns.utils.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HornEvents {

    public static void registerHornEvents() {
        Helpers.debug("Registering Horn Events for " + PolyHorns.MOD_ID);

        UseEntityCallback.EVENT.register((player, world, _, entity, _) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            if (player.isShiftKeyDown() && !player.isSpectator()) {
                return InteractionResult.PASS;
            }

            if (!(entity instanceof ItemFrame frame)) {
                return InteractionResult.PASS;
            }

            return interactBehavior(player, world, frame.getItem());
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            return interactBehavior(player, world, stack);
        });
    }

    public static InteractionResult interactBehavior(Player player, Level world, ItemStack stack) {
        if (!stack.getItem().equals(Items.POISONOUS_POTATO)) {
            return InteractionResult.PASS;
        }

        var item_model = stack.get(DataComponents.ITEM_MODEL);
        if (item_model == null) {
            return InteractionResult.PASS;
        }

        return switch (item_model.toString()) {
            case "polyhorns:horn_of_return" -> hornOfReturnBehavior(player, world, stack);
            case "polyhorns:horn_of_origin" -> hornOfOriginBehavior(player, world, stack);
            default -> InteractionResult.PASS;
        };
    }

    public static InteractionResult hornOfOriginBehavior(Player player, Level world, ItemStack stack) {
        if (!player.getCooldowns().isOnCooldown(stack)) {
            player.sendOverlayMessage(Component.literal("Teleporting..."));
            var pos = ((ServerPlayer) player).findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            hornTeleport(player, world, stack, pos);
        } else {
            player.sendOverlayMessage(Component.literal("Unable to teleport, Horn of Origin is on cooldown!"));
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult hornOfReturnBehavior(Player player, Level world, ItemStack stack) {
        if (player.isShiftKeyDown() && !player.isSpectator()) {
            addLore(player, world, stack);
            player.sendOverlayMessage(Component.literal("Location set for Horn of Return"));
        } else {
            if (!player.getCooldowns().isOnCooldown(stack)) {
                var info = getHornOfReturnInfo(stack, world);
                if (info.isEmpty()) {
                    player.sendOverlayMessage(Component.literal("Unable to teleport, Horn of Return has no saved location!"));
                } else {
                    LocationInfo infoValue = info.get();
                    player.sendOverlayMessage(Component.literal("Teleporting..."));
                    var pos = new TeleportTransition(infoValue.dimension, infoValue.pos, Vec3.ZERO, infoValue.g, infoValue.h, TeleportTransition.DO_NOTHING);
                    hornTeleport(player, world, stack, pos);
                }
            } else if (player.getCooldowns().getCooldownPercent(stack, 0) < 0.99) {
                player.sendOverlayMessage(Component.literal("Unable to teleport, Horn of Return is on cooldown!"));
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static void addLore(Player player, Level world, ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        var dimension = world.dimension().identifier().toString();
        var DIM_LINE = Component.literal(dimension).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        lines.addFirst(DIM_LINE);
        double x = player.position().x;
        double y = player.position().y;
        double z = player.position().z;
        var LORE_LINE = Component.literal((int)x + ", " + (int)y + ", " + (int)z).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        lines.addFirst(LORE_LINE);
        stack.set(DataComponents.LORE, new ItemLore(lines));

        var customData = new CompoundTag();
        customData.putString("dimension", dimension);
        customData.putDouble("x", x);
        customData.putDouble("y", y);
        customData.putDouble("z", z);
        customData.putFloat("g", player.getYRot());
        customData.putFloat("h", player.getXRot());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData));
    }

    public static Optional<LocationInfo> getHornOfReturnInfo(ItemStack stack, Level world) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            var tag = customData.copyTag();
            var dimension = getDimension(tag, "dimension", world);
            var x = tag.getDouble("x");
            var y = tag.getDouble("y");
            var z = tag.getDouble("z");
            var g = tag.getFloat("g");
            var h = tag.getFloat("h");
            if (dimension.isPresent() && x.isPresent() && y.isPresent() && z.isPresent() && g.isPresent() && h.isPresent()) {
                return Optional.of(new LocationInfo(dimension.get(), new Vec3(x.get(), y.get(), z.get()), g.get(), h.get()));
            }
        }
        return Optional.empty();
    }

    public static Optional<ServerLevel> getDimension(CompoundTag tag, String key, Level world) {
        var dimension = tag.getString(key);
        if (dimension.isEmpty()) {
            return Optional.empty();
        }
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimension.get()));
        var server = world.getServer();
        if (server == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(world.getServer().getLevel(dim));
    }

    public static void hornTeleport(Player player, Level world, ItemStack stack, TeleportTransition transition) {
        player.teleport(transition);
        player.resetFallDistance();
        player.setSpeed(0);
        player.getCooldowns().addCooldown(stack, PolyHorns.config().getHornCooldown());
        world.playSound(null, transition.position().x, transition.position().y, transition.position().z, SoundEvents.PLAYER_TELEPORT, player.getSoundSource());
    }

    public record LocationInfo(ServerLevel dimension, Vec3 pos, float g, float h) {}
}
