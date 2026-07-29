package polycube.polyhorn.utils;

import static polycube.polyhorn.PolyHorn.LOGGER;
import static polycube.polyhorn.PolyHorn.MOD_ID;

public final class Helpers {
    private Helpers() {}

    /// Logs a debug message with the mod id prefix.
    public static void debug(final String format, final Object... args) {
        //noinspection StringConcatenationArgumentToLogCall
        LOGGER.debug("[" + MOD_ID + "] " + format, args);
    }
}
