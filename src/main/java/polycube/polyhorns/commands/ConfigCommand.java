package polycube.polyhorns.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorns.Config;
import polycube.polyhorns.PolyHorns;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class ConfigCommand extends PolyHornsCommand {
    public ConfigCommand() {
        super(
                "config",
                "Manage the PolyHorns mod configuration.",
                "<subcommand> [args]",
                PermissionLevel.GAMEMASTERS
        );
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> getCommand() {
        return super.getCommand()
                .then(Commands.literal("horn_cooldown")
                            .executes(ConfigCommand::getHornCooldown)
                            .then(Commands.argument("value", IntegerArgumentType.integer(Config.HRN_COOLDOWN_MIN, Config.HRN_COOLDOWN_MAX))
                                    .executes(ConfigCommand::setHornCooldown)
                            )
                );
    }

    private static int getHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = PolyHorns.config().getHornCooldown();
        ctx.getSource().sendSuccess(() -> Component.literal("Horn cooldown is " + value + " ticks"), false);
        return 1;
    }

    private static int setHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = getInteger(ctx, "value");
        PolyHorns.config().setHornCooldown(value);
        ctx.getSource().sendSuccess(() -> Component.literal("Set horn cooldown to " + value + " ticks"), false);
        return 1;
    }
}
