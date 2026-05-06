package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.DamageSources;
import am2.api.blocks.IKeystoneLockable;
import am2.api.spell.SpellData;
import am2.common.items.ItemFocusCharge;
import am2.common.items.ItemFocusMana;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.spell.component.Summon;
import am2.common.utils.DummyEntityPlayer;
import am2.common.utils.EntityUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntitySummoner extends TileEntityAMPower implements IInventory, IKeystoneLockable<TileEntitySummoner> {

    private static final float summonCost = 2000;
    private static final float maintainCost = 7.5f;
    private NonNullList<ItemStack> inventory;
    private int summonEntityID = -1;
    private DummyEntityPlayer dummyCaster;
    private int summonCooldown = 0;
    private int prevSummonCooldown = 0;
    private static final int maxSummonCooldown = 200;
    private static final int powerPadding = 500; //extra power to charge before summoning so that it can be maintained for a while

    private static final int SUMMON_SLOT = 3;

    public TileEntitySummoner() {
        super(ArsMagica.config.getCapacitySummoner());
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    }

    private boolean isRedstonePowered() {
        return this.world.getStrongPower(pos) > 0;
    }

    @Override
    public void update() {
        super.update();

        prevSummonCooldown = summonCooldown;
        summonCooldown--;
        if (summonCooldown < 0) summonCooldown = 0;

        if (!world.isRemote && summonCooldown == 0 && prevSummonCooldown > 0) {
            world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
        }

        if (!world.isRemote) {
            EntityLiving ent = getSummonedCreature();
            if (ent == null) {
                summonEntityID = -1;
            }
            if (isRedstonePowered() && !inventory.get(SUMMON_SLOT).isEmpty()) {
                if (PowerNodeRegistry.For(this.world).checkPower(this, maintainCost)) {
                    if (ent == null && canSummon()) {
                        summonCreature();
                    } else {
                        if (ent != null) {
                            PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), maintainCost);
                        }
                    }
                } else {
                    unsummonCreature();
                }
            } else {
                if (ent != null) {
                    unsummonCreature();
                    PowerNodeRegistry.For(this.world).insertPower(this, PowerTypes.NEUTRAL, summonCost / 2);
                }
            }
        }
    }

    public float getSummonCost() {
        int numManaFoci = numFociOfType(ItemFocusMana.class);
        return summonCost * (1.0f - 0.2f * numManaFoci);
    }

    public float getMaintainCost() {
        int numManaFoci = numFociOfType(ItemFocusMana.class);
        return maintainCost * (1.0f - 0.2f * numManaFoci);
    }

    public boolean canSummon() {
        if (this.world == null)
            return false;
        return summonCooldown == 0 && PowerNodeRegistry.For(this.world).checkPower(this, getSummonCost() + powerPadding);
    }

    public boolean hasSummon() {
        return this.summonEntityID != -1;
    }

    private void summonCreature() {
        if (world.isRemote || this.summonEntityID != -1) return;
        if (dummyCaster == null) {
            dummyCaster = new DummyEntityPlayer(world);
        }
        //FIXME
        SpellData data = new SpellData(new ItemStack(AMItems.spell), Lists.newArrayList(), UUID.randomUUID(), new NBTTagCompound());
        data.getStoredData().setString("SummonType", inventory.get(SUMMON_SLOT).getTagCompound().getString("SummonType"));
        EntityLiving summon = ((Summon) ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2:summon"))).summonCreature(data, dummyCaster, dummyCaster, world, pos.getX(), pos.getY() + 1, pos.getZ());
        if (summon != null) {
            if (summon instanceof EntityCreature)
                EntityUtils.setGuardSpawnLocation((EntityCreature) summon, pos.getX(), pos.getY(), pos.getZ());
            this.summonEntityID = summon.getEntityId();
            PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), summonCost);
            this.summonCooldown = TileEntitySummoner.maxSummonCooldown;
            EntityUtils.setTileSpawned(summon, this);
            world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
        }
    }

    private void unsummonCreature() {
        if (world.isRemote) return;
        EntityLiving ent = getSummonedCreature();
        if (ent == null) return;
        ent.attackEntityFrom(DamageSources.unsummon, 1000000);
        this.summonEntityID = -1;
        world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
    }

    private EntityLiving getSummonedCreature() {
        if (this.summonEntityID == -1) return null;
        return (EntityLiving) world.getEntityByID(this.summonEntityID);
    }

    @Override
    public int getSizeInventory() {
        return 7;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory.get(4);
        runes[1] = inventory.get(5);
        runes[2] = inventory.get(6);
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
    public String getName() {
        return "Summoner";
    }

    @Override
    public boolean hasCustomName() {
        return false;
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

    private int numFociOfType(Class<?> type) {
        int count = 0;
        for (int i = 0; i < 3; ++i) {
            if (!inventory.get(i).isEmpty() && type.isInstance(inventory.get(i).getItem())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, compound);
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public int getChargeRate() {
        int numChargeFoci = numFociOfType(ItemFocusCharge.class);
        return 100 + (50 * numChargeFoci);
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
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
