package polycube.polyhorn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

/// Codec-backed return target stored in vanilla custom data, so clients do not need the mod.
public record HornReturnLocation(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch) {
    private static final String DATA_KEY = PolyHorn.MOD_ID + ":return_location";

    private static final Codec<HornReturnLocation> LOCATION_CODEC =
            RecordCodecBuilder.create(
                    instance -> instance.group(
                            ResourceKey.codec(Registries.DIMENSION)
                                    .fieldOf("dimension")
                                    .forGetter(HornReturnLocation::dimension),
                            Vec3.CODEC
                                    .fieldOf("position")
                                    .forGetter(HornReturnLocation::position),
                            Codec.FLOAT
                                    .fieldOf("yaw")
                                    .forGetter(HornReturnLocation::yaw),
                            Codec.FLOAT
                                    .fieldOf("pitch")
                                    .forGetter(HornReturnLocation::pitch)
                    ).apply(instance, HornReturnLocation::new)
            );

    public static final Codec<HornReturnLocation> CODEC =
            LOCATION_CODEC.validate(HornReturnLocation::validate);

    public static HornReturnLocation capture(ServerPlayer player) {
        return new HornReturnLocation(
                player.level().dimension(),
                player.position(),
                player.getYRot(),
                player.getXRot()
        );
    }

    public static Optional<HornReturnLocation> load(ItemStack stack) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return Optional.empty();
        }
        return customData.copyTag().read(DATA_KEY, CODEC);
    }

    public void save(ItemStack stack) {
        CustomData.update(
                DataComponents.CUSTOM_DATA,
                stack,
                tag -> tag.store(DATA_KEY, CODEC, this)
        );
    }

    public Optional<TeleportTransition> createTransition(MinecraftServer server) {
        return Optional.ofNullable(server.getLevel(dimension))
                .map(targetLevel -> new TeleportTransition(
                        targetLevel,
                        position,
                        Vec3.ZERO,
                        yaw,
                        pitch,
                        TeleportTransition.DO_NOTHING
                ));
    }

    private static DataResult<HornReturnLocation> validate(HornReturnLocation location) {
        if (!location.position().isFinite()
                || !Float.isFinite(location.yaw())
                || !Float.isFinite(location.pitch())) {
            return DataResult.error(() -> "Horn return location contains a non-finite value");
        }
        return DataResult.success(location);
    }
}
