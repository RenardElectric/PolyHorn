package polycube.polyhorn.client;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import polycube.polyhorn.HornType;

import java.util.concurrent.CompletableFuture;

/// Fabric data-generation entrypoint for horn item models.
public class DataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(MyModelProvider::new);
        pack.addProvider(MyRecipeProvider::new);
    }

    public static class MyModelProvider extends FabricModelProvider {
        public MyModelProvider(FabricPackOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {}

        @Override
        public void generateItemModels(ItemModelGenerators itemModelGenerators) {
            for (var hornType : HornType.values()) {
                itemModelGenerators.generateFlatItem(PolyHornClient.getDatagenItem(hornType), ModelTemplates.FLAT_ITEM);
            }
        }
    }

    public static class MyRecipeProvider extends FabricRecipeProvider {
        public MyRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            return new RecipeProvider(recipes, advancements) {
                @Override
                public void buildRecipes() {
                    HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                    new ShapedRecipeBuilder(itemLookup, RecipeCategory.TOOLS, HornType.ORIGIN.getItemTemplate())
                            .pattern(" P ")
                            .pattern("IHI")
                            .pattern(" I ")
                            .define('I', Items.IRON_INGOT)
                            .define('P', Items.ENDER_PEARL)
                            .define('H', Items.GOAT_HORN)
                            .unlockedBy("unlock_right_away", PlayerTrigger.TriggerInstance.tick())
                            .save(output, HornType.ORIGIN.getIdentifier().getPath());

                    new ShapedRecipeBuilder(itemLookup, RecipeCategory.TOOLS, HornType.RETURN.getItemTemplate())
                            .pattern("CEC")
                            .pattern("PHP")
                            .pattern("CSC")
                            .define('C', Items.CHORUS_FRUIT)
                            .define('E', Items.ECHO_SHARD)
                            .define('P', Items.ENDER_PEARL)
                            .define('H', Items.GOAT_HORN)
                            .define('S', Items.NETHER_STAR)
                            .unlockedBy("unlock_right_away", PlayerTrigger.TriggerInstance.tick())
                            .save(output, HornType.RETURN.getIdentifier().getPath());
                }
            };
        }

        @Override
        public String getName() {
            return "MyRecipeProvider";
        }
    }
}
