package polycube.polyhorn.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import polycube.polyhorn.HornType;
import polycube.polyhorn.PolyHorn;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/// Registers placeholder horn items used only while generating item-model JSON.
public class PolyHornClient implements ClientModInitializer {
    private static final String DATAGEN_PROPERTY = "fabric-api.datagen";
    private static final Map<HornType, Item> DATAGEN_HORN_ITEMS = new HashMap<>();

    @Override
    public void onInitializeClient() {
        if (System.getProperty(DATAGEN_PROPERTY) == null) {
            PolyHorn.LOGGER.debug("Skipping datagen-only horn item registration");
            return;
        }

        for (var hornType : HornType.values()) {
            DATAGEN_HORN_ITEMS.put(hornType, registerDatagenItem(hornType));
        }
        PolyHorn.LOGGER.debug("Registered {} placeholder horn items for data generation", DATAGEN_HORN_ITEMS.size());
    }

    static Item getDatagenItem(HornType hornType) {
        return Objects.requireNonNull(DATAGEN_HORN_ITEMS.get(hornType), "Missing datagen item horn : " + hornType);
    }

    private static Item registerDatagenItem(HornType hornType) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, hornType.getIdentifier());
        Item item = new Item(new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
