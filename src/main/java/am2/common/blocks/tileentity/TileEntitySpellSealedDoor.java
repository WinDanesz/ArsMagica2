package am2.common.blocks.tileentity;

import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellPart;
import am2.common.blocks.BlockSpellSealedDoor;
import am2.common.registry.AMBlocks;
import am2.common.spell.SpellCaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntitySpellSealedDoor extends TileEntity implements ITickable, IInventory, IKeystoneLockable<TileEntitySpellSealedDoor> {

    private NonNullList<ItemStack> inventory;

    private int lastAppliedTime = -1;
    private int closeTime = -1;
    private int curTime = 0;
    private int opentime = 40;

    private ArrayList<SpellComponent> appliedParts;
    private ArrayList<SpellComponent> key;

    public TileEntitySpellSealedDoor() {
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        appliedParts = new ArrayList<SpellComponent>();
        key = new ArrayList<SpellComponent>();
    }

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory.get(0);
        runes[1] = inventory.get(1);
        runes[2] = inventory.get(2);
        return runes;
    }

    @Override
    public boolean keystoneMustBeHeld() {
        return false;
    }

    @Override
    public boolean keystoneMustBeInActionBar() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, slot, amount);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {

        ItemStack stack = getStackInSlot(slot);

        if (!stack.isEmpty()) {
            setInventorySlotContents(slot, ItemStack.EMPTY);
        }

        return stack;
    }

    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        if (stack.isEmpty()) {
            stack = ItemStack.EMPTY;
        }
        this.inventory.set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return false;
    }

    @Override
    public String getName() {
        return "Spell Sealed Door";
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        analyzeSpellForKey();
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        ItemStackHelper.loadAllItems(nbttagcompound, this.inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        ItemStackHelper.saveAllItems(nbttagcompound, this.inventory);
        return nbttagcompound;
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            curTime++;

            if (closeTime == -1 && lastAppliedTime != -1) {
                if (curTime > lastAppliedTime + 10) {
                    clearAppliedParts();
                    return;
                }
                if (checkKey()) {
                    clearAppliedParts();
                    setOpenState(true);
                    this.closeTime = curTime + opentime;
                }
            }

            if (closeTime != -1 && curTime > closeTime) {
                clearAppliedParts();
                setOpenState(false);
                closeTime = -1;
            }
        }
    }

    private void setOpenState(boolean open) {
        ((BlockSpellSealedDoor) AMBlocks.spell_sealed_door).toggleDoor(world, pos, open);
    }

    public void addPartToCurrentKey(SpellComponent component) {
        this.appliedParts.add(component);
        this.lastAppliedTime = curTime;
    }

    private boolean checkKey() {
        if (key.size() != appliedParts.size()) return false;
        if (key.equals(appliedParts)) return true;
        return false;
    }

    private void clearAppliedParts() {
        appliedParts.clear();
        lastAppliedTime = -1;
    }

    public void analyzeSpellForKey() {
        ItemStack spell = this.inventory.get(3);

        if (spell.isEmpty()) return;

        //if we're here, we have a spell to analyze!
        key.clear();
        ISpellCaster caster = SpellCaster.of(spell);
        if (caster != null) {
            for (List<SpellPart> parts : caster.getSpellCommon()) {
                for (SpellPart part : parts) {
                    if (part instanceof SpellComponent) {
                        key.add((SpellComponent) part);
                    }
                }
            }
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
