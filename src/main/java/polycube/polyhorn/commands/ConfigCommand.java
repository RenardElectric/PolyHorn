package polycube.polyhorn.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polycore.commands.PolyCommand;
import polycube.polycore.text.TextComponents;
import polycube.polyhorn.Config;
import polycube.polyhorn.PolyHorn;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;

public class ConfigCommand extends PolyCommand {
    public ConfigCommand() {
        super(
                "config",
                "Manage the PolyHorn mod configuration.",
                PermissionLevel.GAMEMASTERS
        );
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String name) {
        return super.getCommand(name)
                .then(Commands.literal("horn_cooldown")
                        .executes(this::getHornCooldown)
                        .then(Commands.argument("value", IntegerArgumentType.integer(Config.HRN_COOLDOWN_MIN, Config.HRN_COOLDOWN_MAX))
                                .executes(this::setHornCooldown)
                        )
                );
    }

    private int getHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = PolyHorn.config().getHornCooldown();
        var message = textComponents.header("Horn Cooldown")
                        .append(TextComponents.field("Current value", TextComponents.value(value + " ticks")))
                        .append(TextComponents.field("Valid range", TextComponents.value(Config.HRN_COOLDOWN_MIN + " - " + Config.HRN_COOLDOWN_MAX + " ticks")));
        ctx.getSource().sendSuccess(() -> message, false);
        return 1;
    }

    private int setHornCooldown(CommandContext<CommandSourceStack> ctx) {
        int value = getInteger(ctx, "value");
        int oldValue = PolyHorn.config().getHornCooldown();
        return PolyHorn.config().setHornCooldown(value).ifError(err ->
                ctx.getSource().sendFailure(textComponents.error("Failed to set horn cooldown: " + err.message()))
        ).ifSuccess(
                _ -> {
                    var message = textComponents.success("Horn cooldown updated")
                            .append(TextComponents.field("Before", TextComponents.value(oldValue + " ticks")))
                            .append(TextComponents.field("Now", TextComponents.value(value + " ticks")));
                    ctx.getSource().sendSuccess(() -> message, false);
                }
        ).mapOrElse(_ -> 1, _ -> 0);
    }
}
