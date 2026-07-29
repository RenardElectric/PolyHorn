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
import polycube.polyhorn.utils.Helpers;

import java.util.List;

public final class HornEvents {
    private HornEvents() {}

    public static void register() {
        Helpers.debug("Registering horn events for {}", PolyHorn.MOD_ID);

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
    }

    private static void useReturnHorn(ServerPlayer player, ItemStack stack) {
        if (player.isShiftKeyDown()) {
            var location = HornReturnLocation.capture(player);
            location.save(stack);
            updateLore(stack, location);
            notify(player, "Location set for Horn of Return");
            return;
        }

        if (player.getCooldowns().isOnCooldown(stack)) {
            notify(player, "Unable to teleport, Horn of Return is on cooldown!");
            return;
        }

        var transition = HornReturnLocation.load(stack)
                .flatMap(location -> location.createTransition(player.level().getServer()));
        if (transition.isEmpty()) {
            notify(player, "Unable to teleport, Horn of Return has no saved location!");
            return;
        }

        notify(player, "Teleporting...");
        teleport(player, transition.get());
    }

    private static void updateLore(ItemStack stack, HornReturnLocation location) {
        BlockPos position = BlockPos.containing(location.position());
        var coordinates = position.getX() + ", " + position.getY() + ", " + position.getZ();
        var dimension = location.dimension().identifier().toString();
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                loreLine(coordinates),
                loreLine(dimension)
        )));
    }

    private static Component loreLine(String text) {
        return Component.literal(text).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
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
