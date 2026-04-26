package am2.common.recipe;

import am2.common.items.AMArmor;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom shapeless recipe for dyeing AM2 mage armor.
 * Preserves full NBT (infusions, damage, enchantments, etc.) from the input armor.
 */
public class RecipeArmorDye extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    /** Ore-dict dye name → RGB color value. */
    private static final Map<String, Integer> DYE_TO_COLOR = new LinkedHashMap<>();

    static {
        DYE_TO_COLOR.put("dyeWhite",     EnumDyeColor.WHITE.getColorValue());
        DYE_TO_COLOR.put("dyeOrange",    EnumDyeColor.ORANGE.getColorValue());
        DYE_TO_COLOR.put("dyeMagenta",   EnumDyeColor.MAGENTA.getColorValue());
        DYE_TO_COLOR.put("dyeLightBlue", EnumDyeColor.LIGHT_BLUE.getColorValue());
        DYE_TO_COLOR.put("dyeYellow",    EnumDyeColor.YELLOW.getColorValue());
        DYE_TO_COLOR.put("dyeLime",      EnumDyeColor.LIME.getColorValue());
        DYE_TO_COLOR.put("dyePink",      EnumDyeColor.PINK.getColorValue());
        DYE_TO_COLOR.put("dyeGray",      EnumDyeColor.GRAY.getColorValue());
        DYE_TO_COLOR.put("dyeLightGray", EnumDyeColor.SILVER.getColorValue());
        DYE_TO_COLOR.put("dyeCyan",      EnumDyeColor.CYAN.getColorValue());
        DYE_TO_COLOR.put("dyePurple",    EnumDyeColor.PURPLE.getColorValue());
        DYE_TO_COLOR.put("dyeBlue",      EnumDyeColor.BLUE.getColorValue());
        DYE_TO_COLOR.put("dyeBrown",     EnumDyeColor.BROWN.getColorValue());
        DYE_TO_COLOR.put("dyeGreen",     EnumDyeColor.GREEN.getColorValue());
        DYE_TO_COLOR.put("dyeRed",       EnumDyeColor.RED.getColorValue());
        DYE_TO_COLOR.put("dyeBlack",     0x3B3B44); // lighter than EnumDyeColor.BLACK so texture detail remains visible
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        boolean foundArmor = false;
        boolean foundDye = false;
        int itemCount = 0;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            itemCount++;

            if (stack.getItem() instanceof AMArmor) {
                if (foundArmor) return false;
                foundArmor = true;
            } else if (getDyeColor(stack) >= 0) {
                if (foundDye) return false;
                foundDye = true;
            } else {
                return false;
            }
        }
        return foundArmor && foundDye && itemCount == 2;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack armorStack = ItemStack.EMPTY;
        int dyeColor = -1;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof AMArmor) {
                armorStack = stack;
            } else {
                dyeColor = getDyeColor(stack);
            }
        }

        if (armorStack.isEmpty() || dyeColor < 0) return ItemStack.EMPTY;

        ItemStack result = armorStack.copy();
        ((AMArmor) result.getItem()).setColor(result, dyeColor);
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     * Returns the RGB color value for the given dye stack,
     * or -1 if the stack is not a recognized dye.
     */
    private static int getDyeColor(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int oreID : oreIDs) {
            String oreName = OreDictionary.getOreName(oreID);
            Integer color = DYE_TO_COLOR.get(oreName);
            if (color != null) return color;
        }
        return -1;
    }
}
