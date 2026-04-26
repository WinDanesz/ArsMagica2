package am2.common.blocks.tileentity;

import am2.common.armor.ArmorHelper;
import am2.common.power.PowerTypes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class TileEntityArmorImbuer extends TileEntityAMPower implements IInventory {

    private ItemStack[] inventory;
    private boolean creativeModeAllowed;

    public TileEntityArmorImbuer() {
        super(5);
        inventory = new ItemStack[getSizeInventory()];
        java.util.Arrays.fill(inventory, ItemStack.EMPTY);
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public int getChargeRate() {
        return 10;
    }

	/*
	@Override
	public ItemStack[] getRunesInKey(){
		return new ItemStack[]{
				inventory[1],
				inventory[2],
				inventory[3]
		};
	}
	*/

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), compound);
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

	/*
	@Override
	public boolean keystoneMustBeHeld(){
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar(){
		return false;
	}
	*/

    @Override
    public int getSizeInventory() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= inventory.length)
            return ItemStack.EMPTY;
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (!inventory[i].isEmpty()) {
            if (inventory[i].getCount() <= j) {
                ItemStack itemstack = inventory[i];
                inventory[i] = ItemStack.EMPTY;
                return itemstack;
            }
            ItemStack itemstack1 = inventory[i].splitStack(j);
            if (inventory[i].isEmpty()) {
                inventory[i] = ItemStack.EMPTY;
            }
            return itemstack1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        if (!inventory[i].isEmpty()) {
            ItemStack itemstack = inventory[i];
            inventory[i] = ItemStack.EMPTY;
            return itemstack;
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
        return "ArmorInfuserInventory";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }

        return entityplayer.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        NBTTagList nbttaglist = nbttagcompound.getTagList("ArmorInfuserInventory", Constants.NBT.TAG_COMPOUND);
        inventory = new ItemStack[getSizeInventory()];
        java.util.Arrays.fill(inventory, ItemStack.EMPTY);
        for (int i = 0; i < nbttaglist.tagCount(); i++) {
            String tag = String.format("ArrayIndex", i);
            NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.getCompoundTagAt(i);
            byte byte0 = nbttagcompound1.getByte(tag);
            if (byte0 >= 0 && byte0 < inventory.length) {
                inventory[byte0] = new ItemStack((nbttagcompound1));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
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

        nbttagcompound.setTag("ArmorInfuserInventory", nbttaglist);
        return nbttagcompound;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    public void imbueCurrentArmor(ResourceLocation string) {
        ItemStack armorStack = inventory[0];
        ArmorHelper.imbueArmor(armorStack, string, this.creativeModeAllowed);
    }

    public boolean isCreativeAllowed() {
        return this.creativeModeAllowed;
    }

    public void setCreativeModeAllowed(boolean creative) {
        this.creativeModeAllowed = creative;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }

}
