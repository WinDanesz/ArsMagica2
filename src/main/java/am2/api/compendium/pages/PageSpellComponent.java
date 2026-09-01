package am2.api.compendium.pages;

import am2.api.ArsMagicaAPI;
import am2.api.event.SpellRecipeItemsEvent;
import am2.api.skill.Skill;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellPart;
import am2.client.gui.AMGuiHelper;
import am2.client.texture.SpellIconManager;
import am2.common.registry.AMItems;
import am2.common.utils.RecipeUtils;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

import static net.minecraft.client.renderer.texture.TextureMap.LOCATION_BLOCKS_TEXTURE;

public class PageSpellComponent extends CompendiumPage<SpellPart> {

    private static final float ROTATION_DEGREES_PER_TICK = 18f / 20f;
    private Object[] craftingComponents;
    private ItemStack stackTip = ItemStack.EMPTY;
    private int tipX;
    private int tipY;
    private static HashMap<Item, Integer> forcedMetas = new HashMap<>();

    public PageSpellComponent(SpellPart element) {
        super(element);
        getAndAnalyzeRecipe();
    }

    @Override
    protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
        RenderHelper.disableStandardItemLighting();
        int cx = posX + 64;
        int cy = posY + 92;
        long elapsedTicks = (long) AMGuiHelper.instance.getSlowTicker() * 40L + AMGuiHelper.instance.getFastTicker();
        float framecount = elapsedTicks * ROTATION_DEGREES_PER_TICK;
        stackTip = ItemStack.EMPTY;
        RenderRecipe(cx, cy, mouseX, mouseY, framecount);
        TextureAtlasSprite icon = SpellIconManager.INSTANCE.getSprite(element.getRegistryName().toString());
        mc.renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
        GlStateManager.color(1, 1, 1, 1);
        if (icon != null)
            AMGuiHelper.DrawIconAtXY(icon, cx, cy, zLevel, 16, 16, false);

        if (mouseX > cx && mouseX < cx + 16) {
            if (mouseY > cy && mouseY < cy + 16) {
                Skill skill = element.getSkill();
                if (skill != null) {
                    stackTip = new ItemStack(AMItems.spell_part, 1, skill.networkID());
                }
                tipX = mouseX;
                tipY = mouseY;
            }
        }
        {
            mc.renderEngine.bindTexture(new ResourceLocation("arsmagica2", "textures/gui/arcane_compendium_gui_extras.png"));
            zLevel++;
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect_Classic(posX + 125, posY + 15, 112, 145, 60, 40, 40, 40);
            this.drawTexturedModalRect_Classic(posX + 0, posY + 200, 112, 175, 60, 40, 40, 40);
            GlStateManager.disableBlend();
            zLevel--;
            mc.renderEngine.bindTexture(LOCATION_BLOCKS_TEXTURE);
        }
        renderModifiers(posX, posY, mouseX, mouseY);
        if (!stackTip.isEmpty()) {
            renderItemToolTip(stackTip, tipX, tipY);
        }
        RenderHelper.enableStandardItemLighting();
    }

    private void renderModifiers(int posX, int posY, int mouseX, int mouseY) {
        ArrayList<SpellModifier> modifiers = new ArrayList<>();
        EnumSet<SpellModifiers> mods = element.getModifiers();
        for (SpellPart modifier : ArsMagicaAPI.getSpellRegistry()) {
            if (element == modifier)
                continue;
            if (modifier instanceof SpellModifier) {
                for (SpellModifiers mod : ((SpellModifier) modifier).getAspectsModified()) {
                    if (mods.contains(mod)) {
                        modifiers.add((SpellModifier) modifier);
                        break;
                    }
                }
            }
        }
        int startX = 72 - (8 * modifiers.size());
        int yOffset = 10;
        if (!modifiers.isEmpty()) {
            String shapeName = I18n.format(element instanceof SpellModifier ? "am2.gui.modifies" : "am2.gui.modifiedBy");
            mc.fontRenderer.drawString(shapeName, posX + 72 - (mc.fontRenderer.getStringWidth(shapeName) / 2), posY, 0);
            GlStateManager.color(1.0f, 1.0f, 1.0f);
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (SpellModifier mod : modifiers) {
            TextureAtlasSprite modIcon = SpellIconManager.INSTANCE.getSprite(mod.getRegistryName().toString());
            if (modIcon != null)
                AMGuiHelper.DrawIconAtXY(modIcon, posX + startX, posY + yOffset, zLevel, 16, 16, false);
            if (mouseX > posX + startX && mouseX < posX + startX + 16) {
                if (mouseY > posY + yOffset && mouseY < posY + yOffset + 16) {
                    Skill skill = mod.getSkill();
                    if (skill != null) {
                        stackTip = new ItemStack(AMItems.spell_part, 1, skill.networkID());
                    }
                    tipX = mouseX;
                    tipY = mouseY;
                }
            }
            startX += 16;
        }
        RenderHelper.disableStandardItemLighting();
    }

    private void RenderRecipe(int cx, int cy, int mousex, int mousey, float framecount) {
        if (craftingComponents == null) return;
        float angleStep = (360.0f / craftingComponents.length);
        for (int i = 0; i < craftingComponents.length; ++i) {
            float angle = (float) (Math.toRadians((angleStep * i) + framecount % 360));
            float nextangle = (float) (Math.toRadians((angleStep * ((i + 1) % craftingComponents.length)) + framecount % 360));
            float dist = 45;
            int x = (int) Math.round(cx - Math.cos(angle) * dist);
            int y = (int) Math.round(cy - Math.sin(angle) * dist);
            int nextx = (int) Math.round(cx - Math.cos(nextangle) * dist);
            int nexty = (int) Math.round(cy - Math.sin(nextangle) * dist);
            AMGuiHelper.line2d(x + 8, y + 8, cx + 8, cy + 8, zLevel, 0x0000DD);
            AMGuiHelper.gradientline2d(x + 8, y + 8, nextx + 8, nexty + 8, zLevel, 0x0000DD, 0xDD00DD);
            renderCraftingComponent(i, x, y, mousex, mousey);
        }
        return;
    }

    @SuppressWarnings("unchecked")
    private void renderCraftingComponent(int index, int sx, int sy, int mousex, int mousey) {
        Object craftingComponent = craftingComponents[index];

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

        List<ItemStack> alternates = new ArrayList<ItemStack>();

        if (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
            NonNullList<ItemStack> subItems = NonNullList.create();
            stack.getItem().getSubItems(CreativeTabs.SEARCH, subItems);
            alternates.addAll(subItems);
        } else {
            alternates.add(stack);
        }

        if (!alternates.isEmpty()) {
            stack = alternates.get(new Random(new Random(AMGuiHelper.instance.getSlowTicker()).nextLong()).nextInt(alternates.size()));
        }
        if (forcedMetas.containsKey(stack.getItem()))
            stack = new ItemStack(stack.getItem(), stack.getCount(), forcedMetas.get(stack.getItem()));

        try {
            AMGuiHelper.DrawItemAtXY(stack, sx, sy, this.zLevel);
            RenderHelper.disableStandardItemLighting();
        } catch (Throwable t) {
            forcedMetas.put(stack.getItem(), 0);
        }

        if (mousex > sx && mousex < sx + 16) {
            if (mousey > sy && mousey < sy + 16) {
                stackTip = stack;
                tipX = mousex;
                tipY = mousey;
            }
        }
    }

    private void getAndAnalyzeRecipe() {
        ArrayList<Object> recipe = new ArrayList<Object>();
        Object[] recipeItems = ((SpellPart) element).getEffectiveRecipe();
        SpellRecipeItemsEvent event = new SpellRecipeItemsEvent(element.getRegistryName().toString(), recipeItems);
        MinecraftForge.EVENT_BUS.post(event);
        recipeItems = event.recipeItems;

        if (recipeItems != null) {
            for (int i = 0; i < recipeItems.length; ++i) {
                Object o = recipeItems[i];
                if (o instanceof ItemStack) {
                    recipe.add(o);
                } else if (o instanceof Item) {
                    recipe.add(new ItemStack((Item) o));
                } else if (o instanceof Block) {
                    recipe.add(new ItemStack((Block) o));
                } else if (o instanceof String) {
                    if (((String) o).startsWith("E:")) {
                        String s = ((String) o);
                        try {
                            int[] types = RecipeUtils.ParseEssenceIDs(s);
                            int type = 0;
                            for (int t : types)
                                type |= t;
                            int amount = (Integer) recipeItems[++i];
                            recipe.add(new ItemStack(AMItems.etherium, amount, type));
                        } catch (Throwable t) {
                            continue;
                        }
                    } else {
                        recipe.add(OreDictionary.getOres((String) o));
                    }
                }
            }
        }
        craftingComponents = recipe.toArray();
    }
}
