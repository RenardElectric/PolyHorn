package polycube.polyhorns;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import polycube.polyhorns.commands.ConfigCommand;
import polycube.polyhorns.commands.GiveCommand;
import polycube.polyhorns.commands.HelpCommand;
import polycube.polyhorns.commands.PolyHornsCommands;
import polycube.polyhorns.utils.Helpers;

import java.util.Objects;

public class PolyHorns implements ModInitializer {
	public static final String MOD_ID = "polyhorns";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static @Nullable Config config;

	public static Config config() {
		return Objects.requireNonNull(config, "PolyHorns config is unavailable before the server has started");
	}

	@Override
	public void onInitialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			config = Config.load(server);
			Helpers.debug("Initialized PolyHorns state for server {}", server.getServerModName());
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(_ -> {
			config = null;
			Helpers.debug("Cleared PolyHorns server state");
		});

		PolyHornsCommands.registerCommands(
				new HelpCommand(),
				new GiveCommand(),
				new ConfigCommand()
		);

		HornEvents.register();
	}
}
