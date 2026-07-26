package polycube.polyhorns;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/// World-persistent storage for the PolyHorns mod configuration.
public class Config extends SavedData {
    public static final int HRN_COOLDOWN_DEFAULT = 100;
    public static final int HRN_COOLDOWN_MAX = 10000;
    public static final int HRN_COOLDOWN_MIN = 0;

    public static final Codec<Config> CODEC = Codec.intRange(HRN_COOLDOWN_MIN, HRN_COOLDOWN_MAX)
            .xmap(Config::new, Config::getHornCooldown);

    @SuppressWarnings("DataFlowIssue")
    private static final SavedDataType<Config> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(PolyHorns.MOD_ID, "config"),
            () -> new Config(HRN_COOLDOWN_DEFAULT),
            CODEC,
            null
    );


    private int hornCooldown;

    public Config(int hornCooldown) {
        this.hornCooldown = hornCooldown;
    }

    /// Returns the horn cooldown in ticks.
    public int getHornCooldown() {
        return hornCooldown;
    }

    /// Sets the horn cooldown, returning true if the value was valid and set, or false if it was out of range.
    public boolean setHornCooldown(int cooldown) {
        if (cooldown < HRN_COOLDOWN_MIN || cooldown > HRN_COOLDOWN_MAX) {
            return false;
        }
        this.hornCooldown = cooldown;
        setDirty();
        return true;
    }

    /// Loads or creates the world-level PolyHorns config.
    public static Config load(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}
