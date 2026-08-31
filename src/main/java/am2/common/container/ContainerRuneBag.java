package am2.common.container;

import am2.common.container.slot.SlotRuneOnly;
import am2.common.items.ItemRuneBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerRuneBag extends Container {
    private ItemStack bagStack;
    private InventoryRuneBag runeBagInventory;
    public int specialSlotIndex;

    private static final int mainInventoryStart = 16;
    private static final int actionBarStart = 43;
    private static final int actionBarEnd = 51;

    public ContainerRuneBag(InventoryPlayer inventoryplayer, ItemStack bagStack, InventoryRuneBag inventoryBag) {
        this.runeBagInventory = inventoryBag;
        this.bagStack = bagStack;
        int slotIndex = 0;

        //rune slots

        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 2; ++y) {
                addSlotToContainer(new SlotRuneOnly(runeBagInventory, slotIndex++, 8 + (x * 18), 8 + (y * 18)));
            }
        }

        //display player inventory
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 8 + k * 18, 58 + i * 18));
            }
        }

        //display player action bar
        for (int j1 = 0; j1 < 9; j1++) {
            if (inventoryplayer.getStackInSlot(j1) == bagStack) {
                specialSlotIndex = j1 + 32;
                continue;
            }
            addSlotToContainer(new Slot(inventoryplayer, j1, 8 + j1 * 18, 116));
        }

    }

    public ItemStack[] GetFullInventory() {
        ItemStack[] stack = new ItemStack[InventoryRuneBag.inventorySize];
        for (int i = 0; i < InventoryRuneBag.inventorySize; ++i) {
            stack[i] = ((Slot) inventorySlots.get(i)).getStack();
        }
        return stack;
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer) {
        World world = entityplayer.world;

        if (!world.isRemote) {
            ItemStack runeBagItemStack = bagStack;
            if (!runeBagItemStack.isEmpty() && runeBagItemStack.getItem() instanceof ItemRuneBag) {
                ItemRuneBag bag = (ItemRuneBag) runeBagItemStack.getItem();
                ItemStack[] items = GetFullInventory();
                bag.UpdateStackTagCompound(runeBagItemStack, items);
                if (entityplayer.getHeldItemMainhand() == runeBagItemStack) {
                    entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem, runeBagItemStack);
                }
            }
        }

        super.onContainerClosed(entityplayer);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        if (bagStack.isEmpty()) return false;
        return (entityplayer.getHeldItemMainhand() == bagStack || entityplayer.getHeldItemOffhand() == bagStack) && runeBagInventory.isUsableByPlayer(entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = (Slot) inventorySlots.get(i);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (i < mainInventoryStart) {
                if (!mergeItemStack(itemstack1, mainInventoryStart, actionBarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= mainInventoryStart && i < actionBarStart) //player inventory
            {
                if (!mergeItemStack(itemstack1, 0, mainInventoryStart, false)) {
                    if (!mergeItemStack(itemstack1, actionBarStart, actionBarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (i >= actionBarStart && i < actionBarEnd) //player action bar
            {
                if (!mergeItemStack(itemstack1, 0, mainInventoryStart, false)) {
                    if (!mergeItemStack(itemstack1, mainInventoryStart, actionBarStart, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!mergeItemStack(itemstack1, mainInventoryStart, actionBarEnd, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.getCount() != itemstack.getCount()) {
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                return ItemStack.EMPTY;
            }
        }
        return itemstack;
    }
}
