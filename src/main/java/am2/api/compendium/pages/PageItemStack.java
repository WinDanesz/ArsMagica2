package am2.api.compendium.pages;

import am2.api.recipes.RecipeArsMagica;
import am2.api.recipes.RecipesEssenceRefiner;
import am2.client.gui.AMGuiHelper;
import am2.common.items.ItemEssence;
import am2.common.registry.AMItems;
import am2.common.utils.RecipeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class PageItemStack extends CompendiumPage<ItemStack> {


    private int recipeWidth;
    private int recipeHeight;
    private List craftingComponents;
    private ItemStack stackTip;

    public PageItemStack(ItemStack element) {
        super(element);
        getAndAnalyzeRecipe();
    }

    @Override
    protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
        int cx = posX + 60;
        int cy = posY + 92;
        stackTip = null;

        // Draw background textures FIRST (before items) so they are always visible.
        // Previously these were drawn after RenderRecipe(), which leaves depth testing enabled
        // (via enableDepth() for the output item but no matching disableDepth()), causing
        // background draws at z=1 to fail the depth test against items at z=0.
        mc.renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/gui/arcane_compendium_gui_extras.png"));
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        this.drawTexturedModalRect_Classic(posX + 125, posY + 15, 112, 145, 60, 40, 40, 40);
        this.drawTexturedModalRect_Classic(posX, posY + 200, 112, 175, 60, 40, 40, 40);
        drawExtra(cx, cy);
        GlStateManager.disableBlend();

        // Then draw items on top of the background
        RenderHelper.disableStandardItemLighting();
        if (craftingComponents == null) {
            AMGuiHelper.DrawItemAtXY(element, cx, cy, 0);
            if (mouseX > cx && mouseX < cx + 16) {
                if (mouseY > cy && mouseY < cy + 16) {
                    ArrayList<String> tooltip = new ArrayList<>();
                    tooltip.addAll(element.getTooltip(Minecraft.getMinecraft().player, ITooltipFlag.TooltipFlags.NORMAL));
                    drawHoveringText(tooltip, mouseX, mouseY, mc.fontRenderer);
                }
            }
        }
        RenderRecipe(cx, cy, mouseX, mouseY);

        // Clean up GL state after item rendering (RenderRecipe may leave depth enabled)
        GlStateManager.disableDepth();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableAlpha();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (stackTip != null)
            renderItemToolTip(stackTip, mouseX, mouseY);
        RenderHelper.enableStandardItemLighting();
    }

    private void RenderRecipe(int cx, int cy, int mousex, int mousey) {
        int step = 32;
        int sx = cx - step;
        int sy = cy - step;
        if (craftingComponents == null) return;
        boolean isEssenceRefiner = false;
        for (Entry<Integer, RecipeArsMagica> entry : RecipesEssenceRefiner.essenceRefinement().GetRecipeList().entrySet()) {
            if (entry.getValue().getOutput().isItemEqual(element))
                isEssenceRefiner = true;
            if (isEssenceRefiner) break;
        }
        if (isEssenceRefiner) {
            renderCraftingComponent(0, cx, cy - 36, mousex, mousey);
            renderCraftingComponent(1, cx - 30, cy - 2, mousex, mousey);
            renderCraftingComponent(2, cx, cy - 2, mousex, mousey);
            renderCraftingComponent(3, cx + 28, cy - 2, mousex, mousey);
            renderCraftingComponent(4, cx, cy + 30, mousex, mousey);
        } else {
            if (recipeHeight > 0) {
                for (int i = 0; i < recipeHeight; ++i) {
                    for (int j = 0; j < recipeWidth; ++j) {
                        int index = i * recipeWidth + j;

                        renderCraftingComponent(index, sx, sy, mousex, mousey);
                        sx += step;
                    }

                    sx = cx - step;
                    sy += step;
                }
            } else {
                int col = 0;
                int row = 0;
                int widthSq = (int) Math.ceil(Math.sqrt(recipeWidth));

                String label = "\247nShapeless";
                mc.fontRenderer.drawString(label, cx - (int) (mc.fontRenderer.getStringWidth(label) / 2.5), cy - step * 3, 0x6600FF);

                for (int i = 0; i < recipeWidth; ++i) {
                    sx = cx - step + (step * col);
                    sy = cy - step + (step * row);
                    int index = (row * widthSq) + (col++);
                    if (col >= widthSq) {
                        row++;
                        col = 0;
                    }

                    renderCraftingComponent(index, sx, sy, mousex, mousey);
                }
            }
        }

        sx = cx;
        sy = cy - (int) (2.5 * step);
        if (element != null) {
            // Ensure proper GL state for rendering the output item (especially important for TESR items)
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            AMGuiHelper.DrawItemAtXY(element, sx, sy, this.zLevel);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();

            if (element.getCount() > 1)
                mc.fontRenderer.drawString("x" + element.getCount(), sx + 16, sy + 8, 0, false);

            if (mousex > sx && mousex < sx + 16) {
                if (mousey > sy && mousey < sy + 16) {
                    stackTip = this.element;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void renderCraftingComponent(int index, int sx, int sy, int mousex, int mousey) {
        Object craftingComponent = craftingComponents.get(index);

        if (craftingComponent == null) return;

        ItemStack stack = ItemStack.EMPTY;

        if (craftingComponent instanceof ItemStack) {
            stack = (ItemStack) craftingComponent;
        } else if (craftingComponent instanceof List) {
            if (((List<ItemStack>) craftingComponent).isEmpty())
                return;
            int idx = new Random(AMGuiHelper.instance.getSlowTicker()).nextInt(((List<ItemStack>) craftingComponent).size());
            stack = ((ItemStack) ((List<ItemStack>) craftingComponent).get(idx)).copy();
        }

        // Early return if stack is still null after all checks
        if (stack.isEmpty()) return;

//		List<ItemStack> oredict = OreDictionary.getOres(stack.getItem().getUnlocalizedName());
        List<ItemStack> alternates = new ArrayList<ItemStack>();
        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
//			TODO:
//			stack.getItem().getSubItems(stack.getItem(), stack.getItem().getCreativeTab(), alternates);
        }
//		alternates.addAll(oredict);

        if (!alternates.isEmpty()) {
            stack = alternates.get(new Random(AMGuiHelper.instance.getSlowTicker()).nextInt(alternates.size()));
        }

        try {
            AMGuiHelper.DrawItemAtXY(stack, sx, sy, this.zLevel);
            RenderHelper.disableStandardItemLighting();
        } catch (Throwable t) {
        }

        if (mousex > sx && mousex < sx + 16) {
            if (mousey > sy && mousey < sy + 16) {
                stackTip = stack;
            }
        }
    }

    private void drawExtra(int cx, int cy) {
        GlStateManager.disableBlend();
        if (craftingComponents == null) {
            GlStateManager.pushMatrix();

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect_Classic(cx - 77, cy - 68, 0, 101, 150, 150, 100, 147);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        } else {

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            if (element.getItem() instanceof ItemEssence || element.getItem() == AMItems.deficit_crystal || element.getItem() == AMItems.core) {
                this.drawTexturedModalRect_Classic(cx - 43, cy - 45, 367, 0, 105, 105, 70, 105);
            } else if (element.getItem() == AMItems.spell_component) {
                //intentionally do nothing
            } else {
                this.drawTexturedModalRect_Classic(cx - 43, cy - 43, 0, 0, 100, 100, 67, 95);
            }
            GlStateManager.disableBlend();
        }

    }

    private void getAndAnalyzeRecipe() {
        boolean isEssenceRefiner = false;
        for (Entry<Integer, RecipeArsMagica> entry : RecipesEssenceRefiner.essenceRefinement().GetRecipeList().entrySet()) {
            if (entry.getValue().getOutput().isItemEqual(element))
                isEssenceRefiner = true;
            if (isEssenceRefiner) break;
        }
        if (isEssenceRefiner) {
            RecipeArsMagica essenceRecipe = RecipesEssenceRefiner.essenceRefinement().recipeFor(element);
            if (essenceRecipe != null) {
                craftingComponents = Arrays.asList(essenceRecipe.getRecipeItems());
                recipeHeight = 2;
            } else {
                craftingComponents = null;
            }
        } else {
            IRecipe recipe = RecipeUtils.getRecipeFor(element);

            if (recipe != null) {
                element = recipe.getRecipeOutput();

                if (recipe instanceof ShapedRecipes) {
                    recipeWidth = ((ShapedRecipes) recipe).recipeWidth;
                    recipeHeight = ((ShapedRecipes) recipe).recipeHeight;
                    // Convert Ingredients to List<ItemStack> for rendering
                    craftingComponents = new ArrayList<>();
                    for (net.minecraft.item.crafting.Ingredient ingredient : ((ShapedRecipes) recipe).getIngredients()) {
                        if (ingredient.getMatchingStacks().length > 0) {
                            craftingComponents.add(Arrays.asList(ingredient.getMatchingStacks()));
                        } else {
                            craftingComponents.add(null);
                        }
                    }
                } else if (recipe instanceof ShapedOreRecipe) {
                    recipeWidth = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, ((ShapedOreRecipe) recipe), "width");
                    recipeHeight = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, ((ShapedOreRecipe) recipe), "height");

                    // Convert Ingredients to List<ItemStack> for rendering
                    craftingComponents = new ArrayList<>();
                    for (net.minecraft.item.crafting.Ingredient ingredient : ((ShapedOreRecipe) recipe).getIngredients()) {
                        if (ingredient.getMatchingStacks().length > 0) {
                            craftingComponents.add(Arrays.asList(ingredient.getMatchingStacks()));
                        } else {
                            craftingComponents.add(null);
                        }
                    }
                } else if (recipe instanceof ShapelessRecipes) {
                    recipeWidth = ((ShapelessRecipes) recipe).getIngredients().size();
                    recipeHeight = -1;

                    // Convert Ingredients to List<ItemStack> for rendering
                    craftingComponents = new ArrayList<>();
                    for (net.minecraft.item.crafting.Ingredient ingredient : ((ShapelessRecipes) recipe).getIngredients()) {
                        if (ingredient.getMatchingStacks().length > 0) {
                            craftingComponents.add(Arrays.asList(ingredient.getMatchingStacks()));
                        } else {
                            craftingComponents.add(null);
                        }
                    }
                } else if (recipe instanceof ShapelessOreRecipe) {
                    recipeWidth = ((ShapelessOreRecipe) recipe).getIngredients().size();
                    recipeHeight = -1;

                    // Convert Ingredients to List<ItemStack> for rendering
                    craftingComponents = new ArrayList<>();
                    for (net.minecraft.item.crafting.Ingredient ingredient : ((ShapelessOreRecipe) recipe).getIngredients()) {
                        if (ingredient.getMatchingStacks().length > 0) {
                            craftingComponents.add(Arrays.asList(ingredient.getMatchingStacks()));
                        } else {
                            craftingComponents.add(null);
                        }
                    }
                } else {
                    craftingComponents = null;
                }
            } else {
                craftingComponents = null;
            }
        }
    }

}
