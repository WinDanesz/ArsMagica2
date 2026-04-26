package am2.common.recipe;

import am2.common.registry.AMItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Custom shapeless recipe for dyeing an AM2 spell book.
 * Unlike the vanilla-style JSON recipes, this preserves the full NBT
 * (spells, active slot, enchantments, etc.) from the input book.
 */
public class RecipeSpellBookDye extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    /** Ore-dict dye name → spellbook metadata (color index). */
    private static final Map<String, Integer> DYE_TO_META = new LinkedHashMap<>();

    static {
        DYE_TO_META.put("dyeBrown",     0);
        DYE_TO_META.put("dyeCyan",      1);
        DYE_TO_META.put("dyeGray",      2);
        DYE_TO_META.put("dyeLightBlue", 3);
        DYE_TO_META.put("dyeWhite",     4);
        DYE_TO_META.put("dyeBlack",     5);
        DYE_TO_META.put("dyeOrange",    6);
        DYE_TO_META.put("dyePurple",    7);
        DYE_TO_META.put("dyeBlue",      8);
        DYE_TO_META.put("dyeGreen",     9);
        DYE_TO_META.put("dyeYellow",   10);
        DYE_TO_META.put("dyeRed",      11);
        DYE_TO_META.put("dyeLime",     12);
        DYE_TO_META.put("dyePink",     13);
        DYE_TO_META.put("dyeMagenta",  14);
        DYE_TO_META.put("dyeLightGray",15);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        boolean foundBook = false;
        boolean foundDye = false;
        int itemCount = 0;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            itemCount++;

            if (stack.getItem() == AMItems.spellbook) {
                if (foundBook) return false; // only one book allowed
                foundBook = true;
            } else if (getDyeMeta(stack) >= 0) {
                if (foundDye) return false; // only one dye allowed
                foundDye = true;
            } else {
                return false; // unknown ingredient
            }
        }
        return foundBook && foundDye && itemCount == 2;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack bookStack = ItemStack.EMPTY;
        int dyeMeta = -1;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == AMItems.spellbook) {
                bookStack = stack;
            } else {
                dyeMeta = getDyeMeta(stack);
            }
        }

        if (bookStack.isEmpty() || dyeMeta < 0) return ItemStack.EMPTY;

        // Create result with the new color metadata, preserving full NBT
        ItemStack result = new ItemStack(AMItems.spellbook, 1, dyeMeta);
        if (bookStack.hasTagCompound()) {
            result.setTagCompound(bookStack.getTagCompound().copy());
        }
        return result;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return new ItemStack(AMItems.spellbook);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        // Hides the recipe from the recipe book (no fixed output to show)
        return true;
    }

    /**
     * Returns the spellbook color metadata for the given dye stack,
     * or -1 if the stack is not a recognized dye.
     */
    private static int getDyeMeta(ItemStack stack) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int oreID : oreIDs) {
            String oreName = OreDictionary.getOreName(oreID);
            Integer meta = DYE_TO_META.get(oreName);
            if (meta != null) return meta;
        }
        return -1;
    }
}
