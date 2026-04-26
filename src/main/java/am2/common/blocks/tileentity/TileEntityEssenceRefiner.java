package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.recipes.RecipesEssenceRefiner;
import am2.common.blocks.BlockEssenceRefiner;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.List;

public class TileEntityEssenceRefiner extends TileEntityAMPower implements IInventory, IKeystoneLockable<TileEntityEssenceRefiner>, ISidedInventory {

    public static final float REFINE_TIME = 400;
    private static final int OUTPUT_INDEX = 5;
    private static final int FUEL_INDEX = 2;
    public static final float TICK_REFINE_COST = 12.5f;

    private ItemStack[] inventory;
    public float remainingRefineTime;

    public TileEntityEssenceRefiner() {
        super(ArsMagica.config.getCapacityEssenceRefiner());
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
        remainingRefineTime = 0;
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if (inventory[i] == null) return ItemStack.EMPTY;
        return inventory[i];
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (inventory[i] != null && !inventory[i].isEmpty()) {
            if (inventory[i].getCount() <= j) {
                ItemStack itemstack = inventory[i];
                inventory[i] = ItemStack.EMPTY;
                return itemstack;
            }
            ItemStack itemstack1 = inventory[i].splitStack(j);
            if (inventory[i].getCount() == 0) {
                inventory[i] = ItemStack.EMPTY;
            }
            return itemstack1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        inventory[i] = itemstack;
        if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
            itemstack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public String getName() {
        return "Essence Refiner";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("EssenceRefinerInventory", Constants.NBT.TAG_COMPOUND);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < inventory.length; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            String tag = String.format("ArrayIndex", i);
            NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.getCompoundTagAt(i);
            byte byte0 = nbttagcompound1.getByte(tag);
            if (byte0 >= 0 && byte0 < inventory.length) {
                inventory[byte0] = new ItemStack((nbttagcompound1));
            }
        }

        remainingRefineTime = nbttagcompound.getFloat("RefineTime");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setFloat("RefineTime", remainingRefineTime);
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && !inventory[i].isEmpty()) {
                String tag = String.format("ArrayIndex", i);
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte(tag, (byte) i);
                inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbttagcompound.setTag("EssenceRefinerInventory", nbttaglist);
        return nbttagcompound;
    }

    public int getRefinementProgressScaled(int i) {
        return (int) ((remainingRefineTime * i) / getRefineTime());
    }

    public float getRefinementPercentage() {
        if (!isRefining())
            return 0;
        return 1.0f - (remainingRefineTime / getRefineTime());
    }

    public boolean isRefining() {
        return remainingRefineTime > 0;
    }

    private float getRefineTime() {
        return REFINE_TIME;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return new SPacketUpdateTileEntity(pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote) {
            if (canRefine()) {
                if (remainingRefineTime <= 0) {
                    //start refining
                    remainingRefineTime = getRefineTime();
                    world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
                }
            } else {
                if (remainingRefineTime != 0) {
                    remainingRefineTime = 0;
                    world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
                }
            }

            if (isRefining()) {
                setActiveTexture();
                if (PowerNodeRegistry.For(this.world).checkPower(this, TICK_REFINE_COST)) {
                    remainingRefineTime--;
                    if (remainingRefineTime % 10 == 0)
                        world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
                    if (remainingRefineTime <= 0) {
                        remainingRefineTime = 0;
                        if (!world.isRemote) {
                            refineItem();
                        }
                    }

                    PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), TICK_REFINE_COST);
                }
            } else {
                setActiveTexture();
            }
        }
    }

    private void setActiveTexture() {
        if (this.getWorld().getBlockState(pos).getBlock() != AMBlocks.essence_refiner) {
            this.invalidate();
            return;
        }
        if (world.getBlockState(pos).getValue(BlockEssenceRefiner.ACTIVE) == isRefining() || world.isRemote) return;
        if (isRefining()) {
            if (!world.isRemote) {
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockEssenceRefiner.ACTIVE, true), 3);
            }
        } else {
            if (!world.isRemote) {
                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockEssenceRefiner.ACTIVE, false), 3);
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    private boolean canRefine() {
        if (inventory[FUEL_INDEX].isEmpty()) {
            return false;
        }
        ItemStack itemstack = RecipesEssenceRefiner.essenceRefinement().GetResult(getCraftingGridContents(), null);
        if (itemstack.isEmpty()) {
            return false;
        }
        if (inventory[OUTPUT_INDEX].isEmpty()) {
            return true;
        }
        if (!inventory[OUTPUT_INDEX].isItemEqual(itemstack)) {
            return false;
        }
        if (inventory[OUTPUT_INDEX].getCount() < getInventoryStackLimit() && inventory[OUTPUT_INDEX].getCount() < inventory[OUTPUT_INDEX].getMaxStackSize()) {
            return true;
        }
        return inventory[OUTPUT_INDEX].getCount() < itemstack.getMaxStackSize();
    }

    public void refineItem() {
        if (!canRefine()) {
            return;
        }
        ItemStack itemstack = RecipesEssenceRefiner.essenceRefinement().GetResult(getCraftingGridContents(), null);
        if (inventory[OUTPUT_INDEX] == null || inventory[OUTPUT_INDEX].isEmpty()) {
            inventory[OUTPUT_INDEX] = itemstack.copy();
        } else if (inventory[OUTPUT_INDEX].getItem() == itemstack.getItem()) {
            inventory[OUTPUT_INDEX].grow(itemstack.getCount());
        }
        decrementCraftingGridContents();
    }

    private void decrementCraftingGridContents() {
        for (int i = 0; i < 5; ++i) {
            decrementCraftingGridSlot(i);
        }
    }

    @SuppressWarnings("deprecation")
    private void decrementCraftingGridSlot(int slot) {
        if (inventory[slot].getItem().hasContainerItem()) {
            inventory[slot] = new ItemStack(inventory[slot].getItem().getContainerItem());
        } else {
            inventory[slot].shrink(1);
        }

        if (inventory[slot].getCount() <= 0) {
            inventory[slot] = ItemStack.EMPTY;
        }
    }

    private ItemStack[] getCraftingGridContents() {
        ItemStack[] contents = new ItemStack[5];
        for (int i = 0; i < 5; ++i) {
            contents[i] = inventory[i];
        }
        return contents;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer entityplayer) {
    }

    @Override
    public void closeInventory(EntityPlayer entityplayer) {
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        if (inventory[i] != null && !inventory[i].isEmpty()) {
            ItemStack itemstack = inventory[i];
            inventory[i] = ItemStack.EMPTY;
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
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
    public int getChargeRate() {
        return 500;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory[6];
        runes[1] = inventory[7];
        runes[2] = inventory[8];
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
    public List<PowerTypes> getValidPowerTypes() {
        return Lists.newArrayList(
                PowerTypes.NEUTRAL,
                PowerTypes.DARK
        );
    }


    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[]{5};
    }


    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, EnumFacing p_102007_3_) {
        return false;
    }


    @Override
    public boolean canExtractItem(int slot, ItemStack item, EnumFacing side) {
        return slot == 5;
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
