package polycube.polyhorn;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.UseCooldown;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum HornType implements StringRepresentable {
    RETURN("horn_of_return", "Horn of Return") {{
        addLoreLine(Component.literal("A magical horn that teleports you back to its return point.").withStyle(s -> s.withItalic(false)));
        addLoreLine(Component.literal("Right-click to return to your set location.").withStyle(s -> s.withItalic(false)).withStyle(ChatFormatting.DARK_GRAY));
        addLoreLine(Component.literal("Sneak and right-click to set the return point.").withStyle(s -> s.withItalic(false)).withStyle(ChatFormatting.DARK_GRAY));
    }},
    ORIGIN("horn_of_origin", "Horn of Origin") {{
        addLoreLine(Component.literal("A magical horn that teleports you back to your respawn point.").withStyle(s -> s.withItalic(false)));
    }};

    public static final Item HORN_ITEM = Items.POISONOUS_POTATO;
    public static final Identifier HORN_COOLDOWN_GROUP = Identifier.fromNamespaceAndPath(PolyHorn.MOD_ID, "horn_cooldown");

    private final String id;
    private final String displayName;
    private final Identifier identifier;
    private @Nullable ItemStackTemplate itemTemplate;
    private final List<Component> loreLines = new ArrayList<>();

    HornType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
        this.identifier = Identifier.fromNamespaceAndPath(PolyHorn.MOD_ID, id);
    }

    protected void addLoreLine(Component line) {
        loreLines.add(line);
    }

    public String commandName() {
        return id;
    }

    public ItemStack createItem() {
        return getItemTemplate().create();
    }

    public ItemStackTemplate getItemTemplate() {
        if (itemTemplate != null) {
            return itemTemplate;
        }

        var components = DataComponentPatch.builder()
                .set(DataComponents.ITEM_NAME, Component.literal(displayName))
                .set(DataComponents.ITEM_MODEL, identifier)
                .set(DataComponents.RARITY, Rarity.EPIC)
                .set(DataComponents.LORE, new ItemLore(loreLines))
                .set(DataComponents.MAX_STACK_SIZE, 1)
                .set(DataComponents.USE_COOLDOWN, new UseCooldown(1, Optional.of(HORN_COOLDOWN_GROUP)))
                .remove(DataComponents.CONSUMABLE)
                .remove(DataComponents.FOOD);

        itemTemplate = new ItemStackTemplate(HORN_ITEM, components.build());
        return itemTemplate;
    }

    public static Optional<HornType> from(ItemStack stack) {
        if (stack.getItem() != HORN_ITEM) {
            return Optional.empty();
        }

        var identifier = stack.get(DataComponents.ITEM_MODEL);
        if (identifier == null) {
            return Optional.empty();
        }

        for (HornType hornType : values()) {
            if (hornType.identifier.equals(identifier)) {
                return Optional.of(hornType);
            }
        }
        return Optional.empty();
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
