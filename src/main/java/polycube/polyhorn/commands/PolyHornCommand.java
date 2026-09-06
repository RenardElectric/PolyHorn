package polycube.polyhorn.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorn.PolyHorn;

import java.util.ArrayList;
import java.util.List;

public abstract class PolyHornCommand {
    private final String name;
    private final String description;
    private final String usage;
    private final PermissionLevel permissionLevel;
    private final boolean hasQuickAlias;
    private final List<String> aliases;

    public PolyHornCommand(String name, String description, String usage, PermissionLevel permissionLevel) {
        this(name, description, usage, permissionLevel, false);
    }

    public PolyHornCommand(String name, String description, String usage, PermissionLevel permissionLevel, boolean hasQuickAlias) {
        this(name, description, usage, permissionLevel, hasQuickAlias, List.of());
    }

    public PolyHornCommand(String name, String description, String usage, PermissionLevel permissionLevel, boolean hasQuickAlias, List<String> aliases) {
        this.name = name;
        this.description = description;
        this.usage = usage;
        this.permissionLevel = permissionLevel;
        this.hasQuickAlias = hasQuickAlias;
        this.aliases = aliases;
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
        if (hasQuickAlias) {
            var shortcuts = new ArrayList<String>();
            shortcuts.add("/" + name);
            for (String alias : aliases) shortcuts.add("/" + alias);
            message.append(CommandText.field("Shortcuts", CommandText.value(String.join(", ", shortcuts))));
        }
        return message.append("\n<...> required • [...] optional.");
    }

    protected PermissionLevel getPermissionLevel() {
        return this.permissionLevel;
    }

    protected boolean hasQuickAlias() {
        return this.hasQuickAlias;
    }

    protected List<String> getAliases() {
        return this.aliases;
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

    public List<LiteralArgumentBuilder<CommandSourceStack>> getCommands() {
        var commands = new ArrayList<LiteralArgumentBuilder<CommandSourceStack>>();
        var aliases = new ArrayList<>(getAliases());
        aliases.add(name);
        for (String alias : aliases) {
            commands.add(getCommand(alias));
        }
        return commands;
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
