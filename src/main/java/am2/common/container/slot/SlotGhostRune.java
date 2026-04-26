package am2.common.container.slot;

import am2.common.items.ItemRune;
import am2.common.registry.AMItems;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SlotGhostRune extends SlotGhostItem {

    public SlotGhostRune(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return !par1ItemStack.isEmpty() && par1ItemStack.getItem() == AMItems.rune && ((ItemRune) AMItems.rune).getKeyIndex(par1ItemStack) > 0;
    }

}
