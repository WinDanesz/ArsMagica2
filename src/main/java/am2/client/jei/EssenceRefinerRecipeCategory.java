package am2.client.jei;

import am2.ArsMagica;
import am2.common.registry.AMBlocks;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * JEI recipe category for the Essence Refiner.
 * Displays the 5-slot diamond crafting layout and the output.
 */
public class EssenceRefinerRecipeCategory implements IRecipeCategory<EssenceRefinerRecipeWrapper> {

	public static final String UID = ArsMagica.MODID + ".essence_refiner";
	private static final ResourceLocation TEXTURE = new ResourceLocation(ArsMagica.MODID, "textures/gui/jei/essence_refiner.png");

	private final IDrawable background;
	private final IDrawable icon;
	private final String localizedName;

	public EssenceRefinerRecipeCategory(IGuiHelper guiHelper) {
		// Background: 116x90 area from the texture
		this.background = guiHelper.createDrawable(TEXTURE, 0, 0, 116, 90);
		this.icon = guiHelper.createDrawableIngredient(new ItemStack(AMBlocks.essence_refiner));
		this.localizedName = I18n.format("gui.arsmagica2.jei.essence_refiner");
	}

	@Nonnull
	@Override
	public String getUid() {
		return UID;
	}

	@Nonnull
	@Override
	public String getTitle() {
		return localizedName;
	}

	@Nonnull
	@Override
	public String getModName() {
		return ArsMagica.NAME;
	}

	@Nonnull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(@Nonnull IRecipeLayout recipeLayout, @Nonnull EssenceRefinerRecipeWrapper recipeWrapper, @Nonnull IIngredients ingredients) {
		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

		// Diamond layout slots - positions relative to the background drawable
		// Slot 0: Top center
		stacks.init(0, true, 39, 1);
		// Slot 1: Left
		stacks.init(1, true, 7, 33);
		// Slot 2: Center (fuel/catalyst)
		stacks.init(2, true, 39, 33);
		// Slot 3: Right
		stacks.init(3, true, 71, 33);
		// Slot 4: Bottom center
		stacks.init(4, true, 39, 65);

		// Slot 5: Output
		stacks.init(5, false, 95, 65);

		stacks.set(ingredients);
	}
}
