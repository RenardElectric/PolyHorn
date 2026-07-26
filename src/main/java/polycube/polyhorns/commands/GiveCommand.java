package polycube.polyhorns.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorns.HornType;

import java.util.Collection;

public class GiveCommand extends PolyHornsCommand {
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
        var playerArgument = Commands.argument("player", EntityArgument.players());
        for (HornType hornType : HornType.values()) {
            playerArgument.then(
                    Commands.literal(hornType.commandName())
                            .executes(ctx -> giveHorn(ctx, hornType))
            );
        }
        return super.getCommand().then(playerArgument);
    }

    private static int giveHorn(CommandContext<CommandSourceStack> ctx, HornType hornType) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(ctx, "player");
        giveHorn(players, hornType);
        return 1;
    }

    private static void giveHorn(Collection<ServerPlayer> players, HornType hornType) {
        for (ServerPlayer player : players) {
            player.getInventory().placeItemBackInInventory(hornType.createItem());
        }
    }
}
