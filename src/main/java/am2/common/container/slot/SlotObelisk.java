package am2.common.container.slot;

import am2.common.ObeliskFuelHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotObelisk extends Slot {

    public SlotObelisk(IInventory par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        return ObeliskFuelHelper.instance.getFuelBurnTime(par1ItemStack) > 0;
    }

}
