package polycube.polyhorn;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.slf4j.Logger;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;
import polycube.polycore.commands.PolyCommands;
import polycube.polyhorn.commands.ConfigCommand;
import polycube.polyhorn.commands.GiveCommand;

import java.util.Objects;

public class PolyHorn implements ModInitializer {
    public static final String MOD_ID = "polyhorn";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
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

        PolyCommands.registerCommands(
                MOD_ID,
                "PolyHorn",
                LOGGER,
                new GiveCommand(),
                new ConfigCommand()
        );

        HornEvents.register();
    }
}
