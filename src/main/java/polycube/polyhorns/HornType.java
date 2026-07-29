package polycube.polyhorns;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.UseCooldown;

import java.util.Optional;

public enum HornType {
    RETURN("horn_of_return", "Horn of Return"),
    ORIGIN("horn_of_origin", "Horn of Origin");

    public static final Identifier HORN_COOLDOWN_GROUP = Identifier.fromNamespaceAndPath(PolyHorns.MOD_ID, "horn_cooldown");

    private final String commandName;
    private final String displayName;
    private final Identifier itemModel;

    HornType(String commandName, String displayName) {
        this.commandName = commandName;
        this.displayName = displayName;
        this.itemModel = Identifier.fromNamespaceAndPath(PolyHorns.MOD_ID, commandName);
    }

    public String commandName() {
        return commandName;
    }

    public ItemStack createItem() {
        var horn = new ItemStack(Items.POISONOUS_POTATO);
        horn.set(DataComponents.ITEM_NAME, Component.literal(displayName));
        horn.set(DataComponents.ITEM_MODEL, itemModel);
        horn.set(DataComponents.RARITY, Rarity.EPIC);
        horn.set(DataComponents.MAX_STACK_SIZE, 1);
        horn.set(DataComponents.USE_COOLDOWN, new UseCooldown(1, Optional.of(HORN_COOLDOWN_GROUP)));
        horn.remove(DataComponents.CONSUMABLE);
        horn.remove(DataComponents.FOOD);
        return horn;
    }

    public static Optional<HornType> from(ItemStack stack) {
        if (stack.getItem() != Items.POISONOUS_POTATO) {
            return Optional.empty();
        }

        var itemModel = stack.get(DataComponents.ITEM_MODEL);
        if (itemModel == null) {
            return Optional.empty();
        }

        for (HornType hornType : values()) {
            if (hornType.itemModel.equals(itemModel)) {
                return Optional.of(hornType);
            }
        }
        return Optional.empty();
    }
}
