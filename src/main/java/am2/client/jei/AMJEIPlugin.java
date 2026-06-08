package am2.client.jei;

import am2.ArsMagica;
import am2.api.recipes.RecipeArsMagica;
import am2.api.recipes.RecipesEssenceRefiner;
import am2.client.gui.GuiEssenceRefiner;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * JEI plugin for Ars Magica 2.
 * Registers the Essence Refiner recipe category and recipes.
 */
@JEIPlugin
public class AMJEIPlugin implements IModPlugin {

	@Override
	public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(
				new EssenceRefinerRecipeCategory(registry.getJeiHelpers().getGuiHelper())
		);
	}

	@Override
	public void register(@Nonnull IModRegistry registry) {
		// Collect all essence refiner recipes into wrappers
		List<EssenceRefinerRecipeWrapper> wrappers = new ArrayList<>();
		for (RecipeArsMagica recipe : RecipesEssenceRefiner.essenceRefinement().getAllRecipes()) {
			wrappers.add(new EssenceRefinerRecipeWrapper(recipe));
		}

		registry.addRecipes(wrappers, EssenceRefinerRecipeCategory.UID);

		// Register the Essence Refiner block as a recipe catalyst
		registry.addRecipeCatalyst(new ItemStack(AMBlocks.essence_refiner), EssenceRefinerRecipeCategory.UID);

		// Add click-area on the Essence Refiner GUI to open JEI recipes
		// The click area covers the arrow / rune circle area between inputs and output
		registry.addRecipeClickArea(GuiEssenceRefiner.class, 120, 70, 30, 30, EssenceRefinerRecipeCategory.UID);

		// Register item info / description tooltips
		// Lang keys are derived as: jei.arsmagica2.{registryName}.desc
		Item[] infoTooltipItems = {
				AMItems.mana_martini,
				AMItems.mana_cake,
				AMItems.liquid_essence_bottle,
				AMItems.crystal_phylactery,
				AMItems.attuning_staff,
				AMItems.crystal_wrench,
				AMItems.mage_hood,
				AMItems.mage_robe,
				AMItems.mage_leggings,
				AMItems.mage_boots,
				AMItems.lesser_focus,
				AMItems.standard_focus,
				AMItems.greater_focus,
				AMItems.mana_focus,
				AMItems.charge_focus,
				AMItems.mob_focus,
				AMItems.creature_focus,
				AMItems.player_focus,
				AMItems.item_focus,
				AMItems.flicker_focus,
				AMItems.life_ward,
				AMItems.lightning_charm,
		};
		for (Item item : infoTooltipItems) {
			String path = item.getRegistryName().getPath();
			String descKey = "jei." + ArsMagica.MODID + "." + path + ".desc";
			registry.addIngredientInfo(new ItemStack(item), VanillaTypes.ITEM, descKey);
		}

		// Register infinity orb variants (meta 0=blue, 1=green, 2=red)
		String[] orbTiers = {"blue", "green", "red"};
		for (int meta = 0; meta < orbTiers.length; meta++) {
			String tierName = orbTiers[meta];
			String descKey = "jei." + ArsMagica.MODID + ".infinity_orb_" + tierName + ".desc";
			registry.addIngredientInfo(new ItemStack(AMItems.infinity_orb, 1, meta), VanillaTypes.ITEM, descKey);
		}

		// Hide internal items that should not appear in JEI
		registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(AMItems.spell_part));

	}
}
