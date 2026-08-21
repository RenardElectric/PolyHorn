package polycube.polyhorn;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import polycube.polyhorn.commands.ConfigCommand;
import polycube.polyhorn.commands.GiveCommand;
import polycube.polyhorn.commands.HelpCommand;
import polycube.polyhorn.commands.PolyHornCommands;

import java.util.Objects;

public class PolyHorn implements ModInitializer {
    public static final String MOD_ID = "polyhorn";
    public static final Logger LOGGER = LogManager.getLogger("PolyCard");
    private static @Nullable Config config;

    public static Config config() {
        return Objects.requireNonNull(config, "PolyHorn config is unavailable before the server has started");
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            config = Config.load(server);
            LOGGER.info("Initialized PolyHorn state for server {}", server.getServerModName());
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(_ -> {
            config = null;
            LOGGER.info("Cleared PolyHorn server state");
        });

        PolyHornCommands.registerCommands(
                new HelpCommand(),
                new GiveCommand(),
                new ConfigCommand()
        );

        HornEvents.register();
    }
}
