package polycube.polyhorn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/// World-persistent storage for the PolyHorn mod configuration.
public class Config extends SavedData {
    public static final int HRN_COOLDOWN_DEFAULT = 100;
    public static final int HRN_COOLDOWN_MAX = 10000;
    public static final int HRN_COOLDOWN_MIN = 0;

    public static final Codec<Config> CODEC = Codec.intRange(HRN_COOLDOWN_MIN, HRN_COOLDOWN_MAX)
            .xmap(Config::new, Config::getHornCooldown);

    @SuppressWarnings("DataFlowIssue")
    private static final SavedDataType<Config> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(PolyHorn.MOD_ID, "config"),
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
    public DataResult<Config> setHornCooldown(int cooldown) {
        if (cooldown < HRN_COOLDOWN_MIN) {
            return DataResult.error(() -> "Horn cooldown cannot be negative");
        }
        if (cooldown > HRN_COOLDOWN_MAX) {
            return DataResult.error(() -> "Horn cooldown cannot exceed " + HRN_COOLDOWN_MAX);
        }
        this.hornCooldown = cooldown;
        setDirty();
        return DataResult.success(this);
    }

    /// Loads or creates the world-level PolyHorn config.
    public static Config load(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }
}
