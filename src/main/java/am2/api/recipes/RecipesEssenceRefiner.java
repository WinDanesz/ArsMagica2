package am2.api.recipes;

import am2.common.items.ItemCore;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class RecipesEssenceRefiner extends RecipesArsMagica {
    private static final RecipesEssenceRefiner essenceExtractorRecipesBase = new RecipesEssenceRefiner();

    public static final RecipesEssenceRefiner essenceRefinement() {
        return essenceExtractorRecipesBase;
    }

    private RecipesEssenceRefiner() {
        RecipeList = new HashMap<Integer, RecipeArsMagica>();
        InitRecipes();
    }

    private void InitRecipes() {
        //arcane essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.arcane_ash)
                },
                new ItemStack(AMItems.essence_arcane));
        //earth essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Blocks.DIRT),
                        new ItemStack(Blocks.STONE),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Blocks.STONE),
                        new ItemStack(Blocks.OBSIDIAN)
                },
                new ItemStack(AMItems.essence_earth));
        //air essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.FEATHER),
                        new ItemStack(AMBlocks.tarma_root),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMBlocks.tarma_root),
                        new ItemStack(Items.FEATHER)
                },
                new ItemStack(AMItems.essence_air));
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMBlocks.tarma_root),
                        new ItemStack(Items.FEATHER),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.FEATHER),
                        new ItemStack(AMBlocks.tarma_root)
                },
                new ItemStack(AMItems.essence_air));
        //fire essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.COAL),
                        new ItemStack(Items.BLAZE_POWDER),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.BLAZE_POWDER),
                        new ItemStack(Items.COAL)
                },
                new ItemStack(AMItems.essence_fire));
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.BLAZE_POWDER),
                        new ItemStack(Items.COAL),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.COAL),
                        new ItemStack(Items.BLAZE_POWDER)
                },
                new ItemStack(AMItems.essence_fire));
        //water essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(AMBlocks.wakebloom)
                },
                new ItemStack(AMItems.essence_water));

        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.WATER_BUCKET),
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(Items.WATER_BUCKET)
                },
                new ItemStack(AMItems.essence_water));

        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.POTIONITEM, 1, 0),
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(Items.POTIONITEM, 1, 0)
                },
                new ItemStack(AMItems.essence_water));

        AddRecipe(new ItemStack[]{
                        new ItemStack(AMBlocks.wakebloom),
                        new ItemStack(Items.POTIONITEM, 1, 0),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.POTIONITEM, 1, 0),
                        new ItemStack(AMBlocks.wakebloom)
                },
                new ItemStack(AMItems.essence_water));
        //ice essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Blocks.SNOW),
                        new ItemStack(Blocks.ICE),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Blocks.ICE),
                        new ItemStack(Blocks.SNOW)
                },
                new ItemStack(AMItems.essence_ice));
        AddRecipe(new ItemStack[]{
                        new ItemStack(Blocks.ICE),
                        new ItemStack(Blocks.SNOW),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Blocks.SNOW),
                        new ItemStack(Blocks.ICE)
                },
                new ItemStack(AMItems.essence_ice));
        //lightning essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(Items.GLOWSTONE_DUST),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.GLOWSTONE_DUST),
                        new ItemStack(Items.REDSTONE),
                },
                new ItemStack(AMItems.essence_lightning));
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.GLOWSTONE_DUST),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.REDSTONE),
                        new ItemStack(Items.GLOWSTONE_DUST),
                },
                new ItemStack(AMItems.essence_lightning));
        //plant essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Blocks.LEAVES, 1, -1),
                        new ItemStack(Blocks.WATERLILY),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Blocks.CACTUS),
                        new ItemStack(Blocks.VINE)
                },
                new ItemStack(AMItems.essence_nature));
        //life essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.EGG),
                        new ItemStack(Items.GOLDEN_APPLE, 1, 0),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.GOLDEN_APPLE, 1, 0),
                        new ItemStack(Items.EGG)
                },
                new ItemStack(AMItems.essence_life));
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.GOLDEN_APPLE),
                        new ItemStack(Items.EGG, 1, 0),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.EGG, 1, 0),
                        new ItemStack(Items.GOLDEN_APPLE)
                },
                new ItemStack(AMItems.essence_life));
        //ender essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.ENDER_PEARL),
                        new ItemStack(Items.ENDER_EYE),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.ENDER_EYE),
                        new ItemStack(Items.ENDER_PEARL)
                },
                new ItemStack(AMItems.essence_ender));
        AddRecipe(new ItemStack[]{
                        new ItemStack(Items.ENDER_EYE),
                        new ItemStack(Items.ENDER_PEARL),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(Items.ENDER_PEARL),
                        new ItemStack(Items.ENDER_EYE)
                },
                new ItemStack(AMItems.essence_ender));

        //base essence core
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMItems.essence_air),
                        new ItemStack(AMItems.essence_water),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.essence_fire),
                        new ItemStack(AMItems.essence_earth)
                },
                new ItemStack(AMItems.core, 1, ItemCore.META_BASE_CORE));
        //high essence core
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMItems.essence_lightning),
                        new ItemStack(AMItems.essence_ice),
                        new ItemStack(AMItems.arcane_ash),
                        new ItemStack(AMItems.essence_nature),
                        new ItemStack(AMItems.essence_arcane)
                },
                new ItemStack(AMItems.core, 1, ItemCore.META_HIGH_CORE));
        //pure essence
        AddRecipe(new ItemStack[]{
                        new ItemStack(AMItems.core, 1, ItemCore.META_HIGH_CORE),
                        new ItemStack(AMItems.essence_life),
                        new ItemStack(Items.DIAMOND),
                        new ItemStack(AMItems.essence_ender),
                        new ItemStack(AMItems.core, 1, ItemCore.META_BASE_CORE)
                },
                new ItemStack(AMItems.core, 1, ItemCore.META_PURE));

        AddRecipe(new ItemStack[]{
                        new ItemStack(AMItems.core, 1, ItemCore.META_HIGH_CORE),
                        new ItemStack(AMItems.essence_ender),
                        new ItemStack(Items.DIAMOND),
                        new ItemStack(AMItems.essence_life),
                        new ItemStack(AMItems.core, 1, ItemCore.META_BASE_CORE)
                },
                new ItemStack(AMItems.core, 1, ItemCore.META_PURE));

        //deficit crystal
        AddRecipe(new ItemStack[]{
                new ItemStack(AMItems.essence_ender),
                new ItemStack(Items.MAGMA_CREAM),
                new ItemStack(Items.EMERALD),
                new ItemStack(Blocks.OBSIDIAN),
                new ItemStack(AMItems.essence_ender),
        }, new ItemStack(AMItems.deficit_crystal));

        AddRecipe(new ItemStack[]{
                new ItemStack(AMItems.essence_ender),
                new ItemStack(Blocks.OBSIDIAN),
                new ItemStack(Items.EMERALD),
                new ItemStack(Items.MAGMA_CREAM),
                new ItemStack(AMItems.essence_ender),
        }, new ItemStack(AMItems.deficit_crystal));
    }
}
