package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.blocks.*;
import am2.common.ObeliskFuelHelper;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMPotions;
import am2.common.utils.InventoryUtilities;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.List;

// ...existing imports...
public class TileEntityObelisk extends TileEntityAMPower implements IMultiblockController, IInventory, ITileEntityAMBase {
    protected static int pillarBlockID = 98; //stone brick
    protected static int pillarBlockMeta = 3; //arcane texture
    protected int surroundingCheckTicks;
    private NonNullList<ItemStack> inventory;
    protected float powerMultiplier = 1f;
    protected float powerBase = 5.0f;

    public float offsetY = 0;
    public float lastOffsetY = 0;

    public int burnTimeRemaining = 0;
    public int maxBurnTime = 1;
    public boolean fullyCharged = false;

    private static final byte PK_BURNTIME_CHANGE = 1;

    protected IMultiblock structure;
    protected MultiblockGroup wizardChalkCircle;
    protected MultiblockGroup pillars;
    protected HashMap<IBlockState, Float> caps;
    protected TypedMultiblockGroup capsGroup;

    // obelisk rituals
    protected IMultiblock[] rituals;
    protected MultiblockGroup ritual1Chalk;
    protected MultiblockGroup ritual1Candles;
    protected MultiblockGroup ritual2Chalk;
    protected MultiblockGroup ritual2Candles;

    protected HashMap<Integer, IBlockState> createMap(IBlockState state) {
        HashMap<Integer, IBlockState> states = new HashMap<>();
        states.put(0, state);
        return states;
    }

    public TileEntityObelisk() {
        this(ArsMagica.config.getCapacityObelisk());
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    }

    protected void checkNearbyBlockState() {
        List<IMultiblockGroup> groups = structure.getMatchingGroups(world, pos);

        float capsLevel = 1;
        boolean pillarsFound = false;
        boolean wizChalkFound = false;
        boolean capsFound = false;

        for (IMultiblockGroup group : groups) {
            if (group == pillars)
                pillarsFound = true;
            else if (group == wizardChalkCircle)
                wizChalkFound = true;
            else if (group == capsGroup)
                capsFound = true;
        }

        if (pillarsFound && capsFound) {
            IBlockState capState = world.getBlockState(pos.add(2, 2, 2));

            for (IBlockState cap : caps.keySet()) {
                if (capState == cap) {
                    capsLevel = caps.get(cap);
                    break;
                }
            }
        }

        powerMultiplier = 1;

        if (wizChalkFound)
            powerMultiplier = 1.25f;

        if (pillarsFound)
            powerMultiplier *= capsLevel;
    }

    @SuppressWarnings("unchecked")
    public TileEntityObelisk(int capacity) {
        super(capacity);
        setNoPowerRequests();
        surroundingCheckTicks = 0;

        structure = new Multiblock("obelisk_structure");
        pillars = new MultiblockGroup("pillars", Lists.newArrayList(Blocks.STONEBRICK.getDefaultState()), false);
        caps = new HashMap<>();
        capsGroup = new TypedMultiblockGroup("caps", Lists.newArrayList(createMap(Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED))), false);
        caps.put(Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED), 1.35f);

        MultiblockGroup obelisk = new MultiblockGroup("obelisk", Lists.newArrayList(AMBlocks.obelisk.getDefaultState()), true);

        obelisk.addBlock(BlockPos.ORIGIN);

        pillars.addBlock(new BlockPos(-2, 0, -2));
        pillars.addBlock(new BlockPos(-2, 1, -2));
        capsGroup.addBlock(new BlockPos(-2, 2, -2), 0);

        pillars.addBlock(new BlockPos(2, 0, -2));
        pillars.addBlock(new BlockPos(2, 1, -2));
        capsGroup.addBlock(new BlockPos(2, 2, -2), 0);

        pillars.addBlock(new BlockPos(-2, 0, 2));
        pillars.addBlock(new BlockPos(-2, 1, 2));
        capsGroup.addBlock(new BlockPos(-2, 2, 2), 0);

        pillars.addBlock(new BlockPos(2, 0, 2));
        pillars.addBlock(new BlockPos(2, 1, 2));
        capsGroup.addBlock(new BlockPos(2, 2, 2), 0);

        wizardChalkCircle = addWizChalkGroupToStructure(structure);
        structure.addGroup(pillars);
        structure.addGroup(capsGroup);
        structure.addGroup(wizardChalkCircle);
        structure.addGroup(obelisk);

        // Obelisk ritual - Light
        rituals = new Multiblock[2];
        rituals[0] = new Multiblock("obelisk_light");

        ritual1Candles = new MultiblockGroup("candles", Lists.newArrayList(AMBlocks.warding_candle.getDefaultState()), false);

        ritual1Candles.addBlock(new BlockPos(-2, 0, -2));
        ritual1Candles.addBlock(new BlockPos(2, 0, 2));
        ritual1Candles.addBlock(new BlockPos(2, 0, -2));
        ritual1Candles.addBlock(new BlockPos(-2, 0, 2));
        //rituals[0].addGroup(obelisk);
        rituals[0].addGroup(ritual1Candles);
        // Obelisk ritual - Dark
        rituals[1] = new Multiblock("obelisk_dark");
    }

    public boolean isActive() {
        return burnTimeRemaining > 0 || !getFuelStack().isEmpty() && ObeliskFuelHelper.instance.getFuelBurnTime(getFuelStack()) > 0;
    }

    public ItemStack getFuelStack() {
        return inventory.get(0);
    }

    public boolean isHighPowerActive() {
        return burnTimeRemaining > 200 && !getFuelStack().isEmpty();
    }

    public int getCookProgressScaled(int par1) {
        return burnTimeRemaining * par1 / maxBurnTime;
    }

    protected MultiblockGroup addWizChalkGroupToStructure(IMultiblock def) {
        MultiblockGroup group = new MultiblockGroup("wizardChalkCircle", Lists.newArrayList(AMBlocks.wizard_chalk.getDefaultState()), true);

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                group.addBlock(new BlockPos(i, 0, j));
            }
        }

        return group;
    }

    protected void callSuperUpdate() {
        super.update();
    }

    private void setMaxBurnTime(int burnTime) {
        if (burnTime == 0)
            burnTime = 1;
        maxBurnTime = burnTime;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    private void sendCookUpdateToClients() {
        if (!world.isRemote) {
            AMNetHandler.INSTANCE.sendObeliskUpdate(this, new AMDataWriter().add(PK_BURNTIME_CHANGE).add(this.burnTimeRemaining).add(this.fullyCharged).generate());
        }
    }

    public void handlePacket(byte[] data) {
        AMDataReader rdr = new AMDataReader(data);
        if (rdr.ID == TileEntityObelisk.PK_BURNTIME_CHANGE) {
            boolean wasActive = this.burnTimeRemaining > 0 || this.fullyCharged;
            this.burnTimeRemaining = rdr.getInt();
            this.fullyCharged = rdr.getBoolean();
            boolean isActive = this.burnTimeRemaining > 0 || this.fullyCharged;
            if (wasActive != isActive && world != null) {
                world.markBlockRangeForRenderUpdate(pos, pos);
                world.checkLight(pos);
            }
        }
    }

    @Override
    public void update() {
        surroundingCheckTicks++;

        if (isActive()) {
            if (!world.isRemote && surroundingCheckTicks % 100 == 0) {
                checkNearbyBlockState();
                surroundingCheckTicks = 1;
                if (PowerNodeRegistry.For(this.world).checkPower(this, this.capacity * 0.1f)) {
                    List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.add(-2, 0, -2), pos.add(2, 3, 2)));
                    for (EntityPlayer p : nearbyPlayers) {
                        if (p.isPotionActive(AMPotions.mana_regeneration)) continue;
                        p.addPotionEffect(new PotionEffect(AMPotions.mana_regeneration, 600, 1));
                    }
                }
            }

            float powerAmt = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL);
            float powerAdded = !getFuelStack().isEmpty() ? ObeliskFuelHelper.instance.getFuelBurnTime(getFuelStack()) * (powerBase * powerMultiplier) : 0;

            float chargeThreshold = Math.max(this.getCapacity() - powerAdded, this.getCapacity() * 0.75f);

            if (!world.isRemote && burnTimeRemaining <= 0 && powerAmt < chargeThreshold) {
                burnTimeRemaining = ObeliskFuelHelper.instance.getFuelBurnTime(getFuelStack());
                if (burnTimeRemaining > 0) {
                    setMaxBurnTime(burnTimeRemaining);
                    if (getFuelStack().getItem() instanceof UniversalBucket) {
                        setInventorySlotContents(0, ((UniversalBucket) getFuelStack().getItem()).getEmpty().copy());
                    } else if (getFuelStack().getItem().hasContainerItem(getFuelStack())) {
                        ItemStack containerItem = getFuelStack().getItem().getContainerItem(getFuelStack());
                        setInventorySlotContents(0, containerItem);
                    } else {
                        InventoryUtilities.decrementStackQuantity(this, 0, 1);
                    }
                    sendCookUpdateToClients();
                    markDirty(); // Mark dirty when fuel is consumed
                }
            }

            if (!world.isRemote && burnTimeRemaining > 0) {
                burnTimeRemaining--;
                PowerNodeRegistry.For(world).insertPower(this, PowerTypes.NEUTRAL, powerBase * powerMultiplier);

                if (burnTimeRemaining % 20 == 0)
                    sendCookUpdateToClients();
            }

        }

        if (!world.isRemote) {
            boolean wasFullyCharged = fullyCharged;
            fullyCharged = PowerNodeRegistry.For(world).getPower(this, PowerTypes.NEUTRAL) >= this.getCapacity();
            if (wasFullyCharged != fullyCharged) {
                sendCookUpdateToClients();
            }
        }

        if (world.isRemote) {
            lastOffsetY = offsetY;
            offsetY = (float) Math.max(Math.sin(world.getTotalWorldTime() / 20f) / 5, 0.25f);
            if (burnTimeRemaining > 0)
                burnTimeRemaining--;
        }
        // Fix performance: markDirty is called when state changes, not every tick
        super.update();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 0.3, pos.getZ() + 2);
    }

    @Override
    public IMultiblock getMultiblockStructure() {
        return structure;
    }

    public IMultiblock getRitual(int index) {
        return rituals[index];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("burnTimeRemaining", burnTimeRemaining);
        nbttagcompound.setInteger("maxBurnTime", maxBurnTime);
        nbttagcompound.setBoolean("fullyCharged", fullyCharged);
        if (inventory != null) {
            NBTTagCompound inventoryTag = new NBTTagCompound();
            ItemStackHelper.saveAllItems(inventoryTag, this.inventory);
            nbttagcompound.setTag("BurnInventory", inventoryTag);
        }
        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        burnTimeRemaining = nbttagcompound.getInteger("burnTimeRemaining");
        setMaxBurnTime(nbttagcompound.getInteger("maxBurnTime"));
        fullyCharged = nbttagcompound.getBoolean("fullyCharged");

        if (nbttagcompound.hasKey("BurnInventory")) {
            NBTTagCompound inventoryTag = nbttagcompound.getCompoundTag("BurnInventory");
            ItemStackHelper.loadAllItems(inventoryTag, this.inventory);
        }
    }

    @Override
    public int getChargeRate() {
        return 0;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return type == PowerTypes.NEUTRAL;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return Lists.newArrayList(PowerTypes.NEUTRAL);
    }

    @Override
    public boolean canRequestPower() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return 1;
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

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {

        ItemStack previous = inventory.set(slot, stack);

        // Only the central slot affects the in-world rendering, so only sync if that changes
        // This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
        if (slot == 0 && previous.isEmpty() != stack.isEmpty()) this.markDirty();

        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public String getName() {
        return "obelisk";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isStructureValid() {
        return false;
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
        return ObeliskFuelHelper.instance.getFuelBurnTime(itemstack) > 0;
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

    @Override
    public void markDirty() {
        markForUpdate();
        super.markDirty();
    }

    private boolean dirty = false;

    @Override
    public void markForUpdate() {
        this.dirty = true;
    }

    @Override
    public boolean needsUpdate() {
        return this.dirty;
    }

    @Override
    public void clean() {
        this.dirty = false;
    }
}
