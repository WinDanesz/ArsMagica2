package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TileEntityInertSpawner extends TileEntityAMPower implements ISidedInventory {

    private ItemStack phylactery = ItemStack.EMPTY;
    private float powerConsumed = 0.0f;

    private static final ArrayList<PowerTypes> valid = Lists.newArrayList(PowerTypes.DARK);

    private static final float SUMMON_REQ = 6000;

    public TileEntityInertSpawner() {
        super(ArsMagica.config.getCapacityInertSpawner());
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public int getChargeRate() {
        return 100;
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return phylactery.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if (i == 0) {
            return phylactery;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (i == 0 && j > 0 && !phylactery.isEmpty()) {
            ItemStack jar = phylactery.splitStack(j);
            if (phylactery.isEmpty()) phylactery = ItemStack.EMPTY;
            markDirty();
            return jar;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        if (i == 0 && !phylactery.isEmpty()) {
            ItemStack jar = phylactery;
            phylactery = ItemStack.EMPTY;
            markDirty();
            return jar;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        if (i != 0) return;
        phylactery = itemstack;
        if (!itemstack.isEmpty() && itemstack.getCount() > getInventoryStackLimit()) {
            itemstack.setCount(getInventoryStackLimit());
        }
        markDirty();

    }

    @Override
    public String getName() {
        return "Inert Spawner";
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
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return i == 0 && !stack.isEmpty() && stack.getItem() == AMItems.crystal_phylactery;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing p_94128_1_) {
        return new int[]{0};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack stack, EnumFacing face) {
        return
                i == 0 &&
                        this.getStackInSlot(0).isEmpty() &&
                        !stack.isEmpty() &&
                        stack.getItem() == AMItems.crystal_phylactery &&
                        stack.getCount() == 1 &&
                        ((ItemCrystalPhylactery) stack.getItem()).isFull(stack);
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, EnumFacing p_102008_3_) {
        return true;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new SPacketUpdateTileEntity(pos, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);

        if (!phylactery.isEmpty()) {
            NBTTagCompound phy = new NBTTagCompound();
            phylactery.writeToNBT(phy);
            nbttagcompound.setTag("phylactery", phy);
        } else {
            nbttagcompound.removeTag("phylactery");
        }

        nbttagcompound.setFloat("powerConsumed", powerConsumed);
        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);


        phylactery = ItemStack.EMPTY;
        if (nbttagcompound.hasKey("phylactery")) {
            NBTTagCompound phy = nbttagcompound.getCompoundTag("phylactery");
            phylactery = new ItemStack((phy));
        }

        this.powerConsumed = nbttagcompound.getFloat("powerConsumed");
    }

    @Override
    public void update() {
        super.update();

        if (!world.isRemote && !phylactery.isEmpty() && phylactery.getItem() instanceof ItemCrystalPhylactery && ((ItemCrystalPhylactery) phylactery.getItem()).isFull(phylactery) && world.getStrongPower(pos) == 0) {
            if (this.powerConsumed < TileEntityInertSpawner.SUMMON_REQ) {
                this.powerConsumed += PowerNodeRegistry.For(world).consumePower(
                        this,
                        PowerTypes.DARK,
                        Math.min(this.getCapacity(), TileEntityInertSpawner.SUMMON_REQ - this.powerConsumed)
                );
            } else {
                this.powerConsumed = 0;
                ItemCrystalPhylactery item = (ItemCrystalPhylactery) this.phylactery.getItem();
                if (item.isFull(phylactery)) {
                    String clazzName = null; // todo item.getSpawnClass(phylactery);
                    if (clazzName != null) {
                        Class<?> clazz = (Class<?>) EntityList.getClassFromName(clazzName);
                        if (clazz != null) {
                            EntityLiving entity = null;
                            try {
                                entity = (EntityLiving) clazz.getConstructor(World.class).newInstance(world);
                            } catch (Throwable t) {
                                ArsMagica.LOGGER.error("Exception caught: ", t);
                                return;
                            }
                            if (entity == null)
                                return;
                            setEntityPosition(entity);
                            world.spawnEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private void setEntityPosition(EntityLiving e) {
        for (EnumFacing dir : EnumFacing.values()) {
            if (world.isAirBlock(pos.offset(dir))) {
                e.setPosition(pos.offset(dir).getX(), pos.offset(dir).getY(), pos.offset(dir).getZ());
                return;
            }
        }
        e.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return valid;
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
        phylactery = ItemStack.EMPTY;
        markDirty();
    }

    @Override
    public ITextComponent getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }
}
