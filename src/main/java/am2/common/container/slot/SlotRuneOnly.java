package am2.common.container.slot;

import am2.common.items.ItemRune;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRuneOnly extends Slot {

    public SlotRuneOnly(IInventory par1iInventory, int index, int xPosition, int yPosition) {
        super(par1iInventory, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return par1ItemStack.getItem() instanceof ItemRune;
    }
}
