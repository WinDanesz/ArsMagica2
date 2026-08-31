package am2.common.container;

import am2.common.items.ItemEssenceBag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;

public class InventoryEssenceBag implements IInventory {
    public static int inventorySize = 12;
    private final NonNullList<ItemStack> inventoryContents;
    private ItemStack bagStack;

    public InventoryEssenceBag() {
        inventoryContents = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    }
    
    public void setBagStack(ItemStack stack) {
        this.bagStack = stack;
    }

    @Override
    public int getSizeInventory() {
        return inventorySize;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        int loops = Math.min(inventorySize, inventoryContents.length);
        for (int i = 0; i < loops; ++i) {
            this.inventoryContents.set(i, inventoryContents[i]);
        }
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventoryContents) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return index >= 0 && index < this.inventoryContents.size() ? (ItemStack) this.inventoryContents.get(index) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventoryContents, index, count);

        if (!itemstack.isEmpty()) {
            this.markDirty();
        }

        return itemstack;
    }

    public void setInventorySlotContents(int index, ItemStack stack) {
        this.inventoryContents.set(index, stack);

        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack itemstack = this.inventoryContents.get(index);

        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.inventoryContents.set(index, ItemStack.EMPTY);
            this.markDirty();
            return itemstack;
        }
    }

    @Override
    public String getName() {
        return "Essence Bag";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public void markDirty() {
        if (this.bagStack != null && !this.bagStack.isEmpty() && this.bagStack.getItem() instanceof ItemEssenceBag) {
            ItemStack[] items = new ItemStack[inventorySize];
            for (int i = 0; i < inventorySize; i++) items[i] = inventoryContents.get(i);
            ((ItemEssenceBag)this.bagStack.getItem()).updateStackTagCompound(this.bagStack, items);
        }
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
        // TODO Auto-generated method stub

    }

}
