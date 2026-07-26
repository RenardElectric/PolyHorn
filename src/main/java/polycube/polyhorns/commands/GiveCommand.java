package polycube.polyhorns.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumable;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class GiveCommand extends PolyHornsCommand {
    private enum HornType {
        HORN_OF_RETURN,
        HORN_OF_ORIGIN;

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public GiveCommand() {
        super(
                "give",
                "Give a horn of a specific type to one or more players.",
                "<players> <hornType>",
                PermissionLevel.GAMEMASTERS
        );
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> getCommand() {
        return super.getCommand()
                .then(
                        Commands.argument("player", EntityArgument.players())
                                .then(
                                        Commands.literal(HornType.HORN_OF_RETURN.getName())
                                                .executes(ctx -> giveHorn(ctx, HornType.HORN_OF_RETURN))
                                )
                                .then(
                                        Commands.literal(HornType.HORN_OF_ORIGIN.getName())
                                                .executes(ctx -> giveHorn(ctx, HornType.HORN_OF_ORIGIN))
                                )
                );
    }

    private static int giveHorn(CommandContext<CommandSourceStack> ctx, HornType hornType) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "player");
        giveHorn(players, hornType);
        return 1;
    }

    private static void giveHorn(Collection<ServerPlayer> players, HornType hornType) {
        var horn = new ItemStack(Items.POISONOUS_POTATO);
        horn.set(DataComponents.RARITY, Rarity.EPIC);
        horn.set(DataComponents.MAX_STACK_SIZE, 1);
        horn.remove(DataComponents.CONSUMABLE);
        horn.remove(DataComponents.FOOD);
        switch (hornType) {
            case HORN_OF_RETURN -> horn.set(DataComponents.ITEM_NAME, Component.literal("Horn of Return"));
            case HORN_OF_ORIGIN -> horn.set(DataComponents.ITEM_NAME, Component.literal("Horn of Origin"));
        }
        horn.set(DataComponents.ITEM_MODEL, Identifier.parse("polyhorns:" + hornType.getName()));
        for (ServerPlayer player : players) {
            player.getInventory().placeItemBackInInventory(horn);
        }
    }
}
