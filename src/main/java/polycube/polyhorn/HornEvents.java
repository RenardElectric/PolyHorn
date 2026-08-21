package polycube.polyhorn;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.portal.TeleportTransition;

import java.util.ArrayList;
import java.util.List;

public final class HornEvents {
    private HornEvents() {}

    public static void register() {
        PolyHorn.LOGGER.info("Registering horn events for {}", PolyHorn.MOD_ID);

        UseEntityCallback.EVENT.register((player, _, _, entity, _) -> {
            if ((player.isShiftKeyDown() && !player.isSpectator()) || !(entity instanceof ItemFrame frame)) {
                return InteractionResult.PASS;
            }
            return useHorn(player, frame.getItem());
        });

        UseItemCallback.EVENT.register((player, _, hand) -> {
                    if (player.isSpectator()) return InteractionResult.PASS;
                    return useHorn(player, player.getItemInHand(hand));
                }
        );
    }

    private static InteractionResult useHorn(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        var hornType = HornType.from(stack);
        if (hornType.isEmpty()) {
            return InteractionResult.PASS;
        }

        switch (hornType.get()) {
            case RETURN -> useReturnHorn(serverPlayer, stack);
            case ORIGIN -> useOriginHorn(serverPlayer, stack);
        }

        return InteractionResult.SUCCESS;
    }

    private static void useOriginHorn(ServerPlayer player, ItemStack stack) {
        if (player.getCooldowns().isOnCooldown(stack)) {
            notify(player, "Unable to teleport, Horn of Origin is on cooldown!");
            return;
        }

        notify(player, "Teleporting...");
        var transition = player.findRespawnPositionAndUseSpawnBlock(
                false,
                TeleportTransition.DO_NOTHING
        );
        teleport(player, transition);
        PolyHorn.LOGGER.debug("Player {} used Horn of Origin to teleport to their spawn point at {} in dimension {}", player.getName().getString(), transition.position(), transition.newLevel().dimension().identifier());
    }

    private static void useReturnHorn(ServerPlayer player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            var location = HornReturnLocation.capture(player);
            location.save(stack);
            updateLore(stack, location);
            notify(player, "Location set for Horn of Return");
            PolyHorn.LOGGER.debug("Player {} set Horn of Return location to {} in dimension {}", player.getName().getString(), location.position(), location.dimension().identifier());
            return;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            notify(player, "Unable to teleport, Horn of Return is on cooldown!");
            return;
        }

        var transition = HornReturnLocation.load(stack).flatMap(location -> location.createTransition(player.level().getServer()));
        if (transition.isEmpty()) {
            notify(player, "Unable to teleport, Horn of Return has no saved location!");
            return;
        }

        notify(player, "Teleporting...");
        teleport(player, transition.get());
        PolyHorn.LOGGER.debug("Player {} used Horn of Return to teleport to {} in dimension {}", player.getName().getString(), transition.get().position(), transition.get().newLevel().dimension().identifier());
    }

    private static void updateLore(ItemStack stack, HornReturnLocation location) {
        BlockPos position = BlockPos.containing(location.position());
        var coordinates = position.getX() + ", " + position.getY() + ", " + position.getZ();
        var dimension = location.dimension().identifier().toString();
        var lore = stack.get(DataComponents.LORE);
        if (lore == null) lore = new ItemLore(List.of());
        List<Component> lines = new ArrayList<>(lore.lines());
        var newLine = Component.literal("Return point set to: ")
                .append(Component.literal(coordinates)
                        .append(", ")
                        .append(dimension)
                        .withStyle(ChatFormatting.ITALIC))
                .withStyle(s -> s.withItalic(false))
                .withStyle(ChatFormatting.GRAY);
        if (lines.size() > 3)
            lines.set(1, newLine);
        else
            lines.add(1, newLine);
        stack.set(DataComponents.LORE, new ItemLore(lines));
    }

    private static void teleport(
            ServerPlayer player,
            TeleportTransition transition
    ) {
        player.teleport(transition);
        player.resetFallDistance();
        player.getCooldowns().addCooldown(HornType.HORN_COOLDOWN_GROUP, PolyHorn.config().getHornCooldown());
        transition.newLevel().playSound(
                null,
                transition.position().x,
                transition.position().y,
                transition.position().z,
                SoundEvents.PLAYER_TELEPORT,
                player.getSoundSource()
        );
    }

    private static void notify(Player player, String message) {
        player.sendOverlayMessage(Component.literal(message));
    }
}
