package polycube.polyhorn.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorn.PolyHorn;

public abstract class PolyHornCommand {
    private final String name;
    private final String description;
    private final String usage;
    private final PermissionLevel permissionLevel;

    public PolyHornCommand(String name, String description, String usage, PermissionLevel permissionLevel) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permissionLevel = permissionLevel;
    }

    protected String getName() {
        return name;
    }

    protected String getDescription() {
        return description.endsWith(".") ? description : description + ".";
    }

    protected Component getFullDescription() {
        var message = CommandText.header("/" + PolyHorn.MOD_ID + " " + name)
                .append("\n" + getDescription());
        if (permissionLevel != PermissionLevel.ALL) message.append(CommandText.muted(" (Admin only)"));
        for (String variant : usage.split(" \\| ")) {
            message.append("\n  ").append(CommandText.value("/" + PolyHorn.MOD_ID + " " + name
                    + (variant.isBlank() ? "" : " " + variant)));
        }
        return message.append("\n<...> required • [...] optional.");
    }

    protected PermissionLevel getPermissionLevel() {
        return this.permissionLevel;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getCommand(String name) {
        return Commands.literal(name)
                .requires(source -> hasPermission(source, permissionLevel))
                .executes(e -> execute(e.getSource()))
                .then(Commands.literal("help").executes(e -> {
                    e.getSource().sendSuccess(this::getFullDescription, false);
                    return 1;
                }));

    }
    protected boolean hasPermission(CommandSourceStack source, PermissionLevel permissionLevel) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(permissionLevel));
    }

    protected int execute(CommandSourceStack source) {
        source.sendFailure(CommandText.error("Incomplete command. Choose one of the forms below.")
                .append("\n").append(getFullDescription()));
        return 0;
    }
}
