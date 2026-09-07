package am2.common.blocks;

import am2.common.container.ContainerEssenceRefiner;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class CraftingEssenceExtractor implements IInventory {

    private final NonNullList<ItemStack> stackList;
    private Container eventHandler;

    public CraftingEssenceExtractor(ContainerEssenceRefiner container) {
        stackList = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        eventHandler = container;
    }

    @Override
    public int getSizeInventory() {
        return 5;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stackList) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Extracting";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    public ItemStack getStackInSlot(int i) {
        if (i < 0 || i >= getSizeInventory()) {
            return ItemStack.EMPTY;
        } else {
            return stackList.get(i);
        }
    }

    public ItemStack getStackInRowAndColumn(int i, int j) {
        return ItemStack.EMPTY;
    }

    public ItemStack decrStackSize(int i, int j) {
        if (j > 0 && !getStackInSlot(i).isEmpty()) {
            if (stackList.get(i).getCount() <= j) {
                ItemStack itemstack = stackList.get(i);
                stackList.set(i, ItemStack.EMPTY);
                eventHandler.onCraftMatrixChanged(this);
                return itemstack;
            }
            ItemStack itemstack1 = stackList.get(i).splitStack(j);
            if (stackList.get(i).isEmpty()) {
                stackList.set(i, ItemStack.EMPTY);
            }
            eventHandler.onCraftMatrixChanged(this);
            return itemstack1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public void setInventorySlotContents(int i, ItemStack itemstack) {
        stackList.set(i, itemstack);
        eventHandler.onCraftMatrixChanged(this);
    }

    public int getInventoryStackLimit() {
        return 64;
    }

    public void onInventoryChanged() {
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        if (!getStackInSlot(i).isEmpty()) {
            ItemStack itemstack = stackList.get(i);
            stackList.set(i, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void closeInventory(EntityPlayer entityplayer) {
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer entityplayer) {
    }

    @Override
    public ITextComponent getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getField(int id) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFieldCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clear() {
        stackList.clear();
        eventHandler.onCraftMatrixChanged(this);
    }
}
