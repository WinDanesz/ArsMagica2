package am2.client.jei;

import am2.api.recipes.RecipeArsMagica;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JEI recipe wrapper for a single Essence Refiner recipe.
 * Maps the 5-slot input array + output to JEI's ingredient system.
 */
public class EssenceRefinerRecipeWrapper implements IRecipeWrapper {

	private final List<List<ItemStack>> inputs;
	private final ItemStack output;

	public EssenceRefinerRecipeWrapper(RecipeArsMagica recipe) {
		ItemStack[] recipeItems = recipe.getRecipeItems();
		this.inputs = new ArrayList<>();
		for (ItemStack stack : recipeItems) {
			if (stack != null && !stack.isEmpty()) {
				this.inputs.add(Collections.singletonList(stack.copy()));
			} else {
				this.inputs.add(Collections.emptyList());
			}
		}
		this.output = recipe.getOutput().copy();
	}

	@Override
	public void getIngredients(@Nonnull IIngredients ingredients) {
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutput(VanillaTypes.ITEM, output);
	}
}
