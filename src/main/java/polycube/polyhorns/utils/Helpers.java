package polycube.polyhorns.utils;

import static polycube.polyhorns.PolyHorns.LOGGER;
import static polycube.polyhorns.PolyHorns.MOD_ID;

public final class Helpers {
    private Helpers() {}

    /// Logs a debug message with the mod id prefix.
    public static void debug(final String format, final Object... args) {
        //noinspection StringConcatenationArgumentToLogCall
        LOGGER.debug("[" + MOD_ID + "] " + format, args);
    }
}
