package polycube.polyhorn.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorn.Config;
import polycube.polyhorn.PolyHorn;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class ConfigCommand extends PolyHornCommand {
    public ConfigCommand() {
        super(
                "config",
                "Manage the PolyHorn mod configuration.",
                "<subcommand> [args]",
                PermissionLevel.GAMEMASTERS
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String name) {
        return super.getCommand(name)
                .then(Commands.literal("horn_cooldown")
                        .executes(ConfigCommand::getHornCooldown)
                        .then(Commands.argument("value", IntegerArgumentType.integer(Config.HRN_COOLDOWN_MIN, Config.HRN_COOLDOWN_MAX))
                                .executes(ConfigCommand::setHornCooldown)
                        )
                );
    }

    private static int getHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = PolyHorn.config().getHornCooldown();
        var message = CommandText.header("Horn Cooldown")
                        .append(CommandText.field("Current value", CommandText.value(value + " ticks")))
                        .append(CommandText.field("Valid range", CommandText.value(Config.HRN_COOLDOWN_MIN + " - " + Config.HRN_COOLDOWN_MAX + " ticks")));
        ctx.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private static int setHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = getInteger(ctx, "value");
        int oldValue = PolyHorn.config().getHornCooldown();
        return PolyHorn.config().setHornCooldown(value).ifError(err ->
                ctx.getSource().sendFailure(Component.literal("Failed to set horn cooldown: " + err.message()))
        ).ifSuccess(
                _ -> {
                    var message = CommandText.header("Horn Cooldown")
                            .append(CommandText.field("Old value", CommandText.value(oldValue + " ticks")))
                            .append(CommandText.field("New value", CommandText.value(value + " ticks")));
                    ctx.getSource().sendSuccess(() -> message, false);
                }
        ).mapOrElse(_ -> 1, _ -> 0);
    }
}
