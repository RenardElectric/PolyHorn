package polycube.polyhorns.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import polycube.polyhorns.PolyHorns;
import polycube.polyhorns.utils.Helpers;

import java.util.Objects;

public final class PolyHornsCommands {
    private static PolyHornsCommand @Nullable [] commands;

    private PolyHornsCommands() {
    }

    public static void registerCommands(PolyHornsCommand... commands) {
        PolyHornsCommands.commands = commands;
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
            var baseCommand = Commands.literal(PolyHorns.MOD_ID);
            baseCommand.executes(context -> printModInfo(context.getSource()));
            for (PolyHornsCommand command : commands) {
                baseCommand.then(command.getCommand());
            }
            dispatcher.register(baseCommand);
            Helpers.debug("Registered {} PolyHorns subcommand(s)", commands.length);
        });
    }

    public static int printModInfo(CommandSourceStack cst) {
        var optionalModData = FabricLoader.getInstance()
                .getModContainer(PolyHorns.MOD_ID)
                .map(ModContainer::getMetadata);

        if (optionalModData.isEmpty()) {
            cst.sendFailure(Component.literal("Could not fetch mod information."));
            return 0;
        }
        var modData = optionalModData.get();
        var authors = modData.getAuthors().stream()
                .map(Person::getName)
                .reduce((a, b) -> a + " and " + b)
                .orElse("Unknown authors");
        var modInfo = Component.literal("\n" + modData.getName() + " v" + modData.getVersion().getFriendlyString())
                .append("\nMade by " + authors)
                .append("\n" + modData.getDescription());
        cst.sendSuccess(() -> modInfo, false);
        return 1;
    }

    public static PolyHornsCommand[] getCommands() {
        return Objects.requireNonNull(commands, "PolyHorns commands are unavailable before registration");
    }
}
