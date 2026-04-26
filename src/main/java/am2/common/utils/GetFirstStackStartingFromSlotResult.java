package am2.common.utils;

import net.minecraft.item.ItemStack;

public class GetFirstStackStartingFromSlotResult {
    public final int slot;
    public final ItemStack stack;

    public GetFirstStackStartingFromSlotResult(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }
}