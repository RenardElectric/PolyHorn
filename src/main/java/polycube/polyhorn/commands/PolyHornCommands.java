package polycube.polyhorn.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jspecify.annotations.Nullable;
import polycube.polyhorn.PolyHorn;

import java.util.Objects;

public final class PolyHornCommands {
    private static PolyHornCommand @Nullable [] commands;

    private PolyHornCommands() {}

    public static void registerCommands(PolyHornCommand... commands) {
        PolyHornCommands.commands = commands;
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            var baseCommand = Commands.literal(PolyHorn.MOD_ID);
            baseCommand.executes(context -> printModInfo(context.getSource()));
            for (PolyHornCommand command : commands) {
                for (var commandAlias : command.getCommands()) {
                    baseCommand.then(commandAlias);
                    if (command.hasQuickAlias()) dispatcher.register(commandAlias);
                }
            }
            dispatcher.register(baseCommand);
            PolyHorn.LOGGER.debug("Registered {} PolyHorn subcommand(s)", commands.length);
        });
    }

    public static int printModInfo(CommandSourceStack cst) {
        var optionalModData = FabricLoader.getInstance()
                .getModContainer(PolyHorn.MOD_ID)
                .map(ModContainer::getMetadata);

        if (optionalModData.isEmpty()) {
            PolyHorn.LOGGER.warn("Could not find PolyHorn metadata while handling the base command");
            cst.sendFailure(CommandText.error("Could not fetch mod information."));
            return 0;
        }
        var modData = optionalModData.get();
        var authors = modData.getAuthors().stream()
                .map(Person::getName)
                .reduce((a, b) -> a + " and " + b)
                .orElse("Unknown authors");
        var modInfo = CommandText.header(modData.getName())
                .append(CommandText.muted(" v" + modData.getVersion().getFriendlyString()))
                .append(CommandText.field("Made by", CommandText.value(authors)))
                .append("\n" + modData.getDescription())
                .append("\n").append(CommandText.action("[View commands]", "/" + PolyHorn.MOD_ID + " help"));
        cst.sendSuccess(() -> modInfo, false);
        return 1;
    }

    public static PolyHornCommand[] getCommands() {
        return Objects.requireNonNull(commands, "PolyHorn commands are unavailable before registration");
    }
}
