package polycube.polyhorn.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.permissions.PermissionLevel;
import polycube.polyhorn.PolyHorn;

public class HelpCommand extends PolyHornCommand {
    public HelpCommand() {
        super(
                "help",
                "Displays a list of available commands and their descriptions",
                "",
                PermissionLevel.ALL
        );
    }

    @Override
    protected int execute(CommandSourceStack source) {
        var helpMessage = CommandText.header("Commands")
                .append("\nClick a command to prepare it; use [Usage] for its syntax.");
        for (PolyHornCommand command : PolyHornCommands.getCommands()) {
            if (hasPermission(source, command.getPermissionLevel())) {
                String root = "/" + PolyHorn.MOD_ID + " " + command.getName();
                helpMessage.append("\n\n  ").append(CommandText.action(root, root + " "));
                helpMessage.append(" ").append(CommandText.action("[Usage]", root + " help"));
                if (command.getPermissionLevel() != PermissionLevel.ALL) helpMessage.append(CommandText.muted(" (Admin only)"));
                helpMessage.append("\n  " + command.getDescription());
            }
        }
        source.sendSuccess(() -> helpMessage, false);
        return 1;
    }
}
