package am2.common.blocks.tileentity;


import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.common.items.ItemFocusCharge;
import am2.common.items.ItemFocusMana;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;

public class TileEntityCalefactor extends TileEntityAMPower implements IInventory, ISidedInventory, IKeystoneLockable<TileEntityCalefactor>, ITileEntityAMBase {
    private NonNullList<ItemStack> inventory;

    private float rotationX, rotationY, rotationZ;
    private float rotationStepX;
    private final short baseCookTime = 220; //default to the same as a standard furnace
    private short timeSpentCooking = 0;
    private final float basePowerConsumedPerTickCooking = 0.85f;
    private int particleCount = 0;
    private boolean isCooking;
    private boolean dirty = false;

    private static final byte PKT_PRG_UPDATE = 1;
    private boolean isFirstTick = true;

    public TileEntityCalefactor() {
        super(ArsMagica.config.getCapacityCalefactor());

        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);

        isCooking = false;
    }

    /**
     * Returns whether the calefactor is currently cooking an item.
     * On the server, checks the actual cooking progress.
     * On the client, uses the synced isCooking flag.
     */
    public boolean isCooking() {
        if (world != null && !world.isRemote) {
            // Server side: check actual cooking state
            return timeSpentCooking > 0;
        }
        // Client side: use synced value
        return isCooking;
    }

    @Override
    public float particleOffset(int axis) {
        // Calefactor no longer has facing, return center offset
        return 0.5f;
    }

    public void incrementRotations() {
        rotationX += rotationStepX;
        rotationY += rotationStepX;
        rotationZ += rotationStepX;

        if (rotationX > 359) rotationX -= 360;
        if (rotationY > 359) rotationY -= 360;
        if (rotationZ > 359) rotationZ -= 360;

        if (rotationX < 0) rotationX += 360;
        if (rotationY < 0) rotationY += 360;
        if (rotationZ < 0) rotationZ += 360;
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationX;
    }

    public float getRotationZ() {
        return this.rotationX;
    }

    public ItemStack getItemBeingCooked() {
        return inventory.get(0);
    }

    private boolean canSmelt() {
        if (this.inventory.get(0).isEmpty()) {
            return false;
        } else {
            ItemStack var1 = FurnaceRecipes.instance().getSmeltingResult(this.inventory.get(0));
            if (var1.isEmpty()) return false;
            if (this.inventory.get(1).isEmpty()) return true;
            if (!this.inventory.get(1).isItemEqual(var1)) return false;
            int result = inventory.get(1).getCount() + var1.getCount();
            return (result <= getInventoryStackLimit() && result <= var1.getMaxStackSize());
        }
    }

    public void smeltItem() {
        if (this.canSmelt()) {
            ItemStack var1 = FurnaceRecipes.instance().getSmeltingResult(this.inventory.get(0));

            ItemStack smeltStack = var1.copy();

            if (this.inventory.get(0).getItem() instanceof ItemFood || this.inventory.get(0).getItem() instanceof ItemBlock || this.inventory.get(0).getItem() == AMItems.vinteum_dust) {
                if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, getCookTickPowerCost()))
                    if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.NEUTRAL, getCookTickPowerCost()))
                        if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.LIGHT, getCookTickPowerCost()))
                            smeltStack.grow(1);
            }

            if (this.inventory.get(0).getItem() instanceof ItemFood) {
                if (smeltStack.getCount() == var1.getCount() && world.rand.nextDouble() < 0.15f) {
                    smeltStack.grow(1);
                }
            }

            boolean doSmelt = true;

            if (doSmelt) {

                if (this.inventory.get(1).isEmpty()) {
                    this.setInventorySlotContents(1, smeltStack.copy());
                } else if (this.inventory.get(1).isItemEqual(smeltStack)) {
                    inventory.get(1).grow(smeltStack.getCount());
                    if (inventory.get(1).getCount() > inventory.get(1).getMaxStackSize()) {
                        inventory.get(1).setCount(inventory.get(1).getMaxStackSize());
                    }
                }

                if (Math.random() <= 0.25) {
                    if (inventory.get(5).isEmpty()) {
                        this.setInventorySlotContents(5, new ItemStack(AMItems.vinteum_dust));
                    } else {
                        inventory.get(5).grow(1);
                        if (inventory.get(5).getCount() > inventory.get(5).getMaxStackSize()) {
                            inventory.get(5).setCount(inventory.get(5).getMaxStackSize());
                        }
                    }
                }
            }

            this.inventory.get(0).shrink(1);

            if (this.inventory.get(0).getCount() <= 0) {
                this.setInventorySlotContents(0, ItemStack.EMPTY);
            }
        }
    }

    public void handlePacket(byte[] data) {
        if (world.isRemote) {
            AMDataReader rdr = new AMDataReader(data);
            switch (rdr.ID) {
                case PKT_PRG_UPDATE:
                    isCooking = rdr.getByte() == 1;
                    if (rdr.getByte() == 1) {
                        this.setInventorySlotContents(0, rdr.getItemStack());
                    } else {
                        this.setInventorySlotContents(0, ItemStack.EMPTY);
                    }
                    break;
                default:
            }
        }
    }

    protected void sendCookStatusUpdate(boolean isCooking) {
        if (this.world.isRemote)
            return;

        AMDataWriter writer = new AMDataWriter();
        writer.add(PKT_PRG_UPDATE);
        writer.add(isCooking ? (byte) 1 : (byte) 0);
        writer.add(!this.inventory.get(0).isEmpty() ? (byte) 1 : (byte) 0);
        if (!this.inventory.get(0).isEmpty())
            writer.add(this.inventory.get(0));

        AMNetHandler.INSTANCE.sendCalefactorCookUpdate(this, writer.generate());
    }

    private short getModifiedCookTime() {
        int foci = this.numFociOfType(ItemFocusCharge.class);
        short base = baseCookTime;
        short modified = (short) (base * Math.pow(0.5, foci));
        return modified;
    }

    private float getCookTickPowerCost() {
        int fociMana = this.numFociOfType(ItemFocusMana.class);
        int fociCharge = this.numFociOfType(ItemFocusCharge.class);
        float base = basePowerConsumedPerTickCooking;
        return (float) (base * Math.pow(2.25, fociCharge) * Math.pow(0.5, fociMana));
    }

    private boolean isSmelting() {
        return this.timeSpentCooking != 0;
    }

    @Override
    public void update() {
        super.update();
        if (isFirstTick) {
            rotationStepX = world.rand.nextFloat() * 0.03f - 0.015f;
            isFirstTick = false;
        }
        if (this.world.isRemote) {
            incrementRotations();
            if (this.isCooking) {
                particleCount--;
                if (particleCount <= 0) {
                    particleCount = (int) (Math.random() * 20);
                    double rStartX = Math.random() > 0.5 ? this.pos.getX() + 0.19 : this.pos.getX() + 0.81;
                    double rStartY = this.pos.getY() + 1.25;
                    double rStartZ = Math.random() > 0.5 ? this.pos.getZ() + 0.19 : this.pos.getZ() + 0.81;

                    double endX = this.pos.getX() + 0.5f;
                    double endY = this.pos.getY() + 0.7f + (world.rand.nextDouble() * 0.5f);
                    double endZ = this.pos.getZ() + 0.5f;

                    ArsMagica.proxy.particleManager.BeamFromPointToPoint(world, rStartX, rStartY, rStartZ, endX, endY, endZ, 0xFF8811);
                    if (world.rand.nextBoolean()) {
                        AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "smoke", endX, endY, endZ);
                        if (effect != null) {
                            effect.setIgnoreMaxAge(false);
                            effect.setMaxAge(60);
                            effect.AddParticleController(new ParticleFloatUpward(effect, 0.02f, 0.01f, 1, false));
                        }
                    } else {
                        AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "explosion_2", endX, endY, endZ);
                        if (effect != null) {
                            effect.setIgnoreMaxAge(false);
                            effect.setMaxAge(10);
                            effect.setParticleScale(0.04f);
                            effect.addVelocity(world.rand.nextDouble() * 0.2f - 0.1f, 0.2f, world.rand.nextDouble() * 0.2f - 0.1f);
                            effect.setAffectedByGravity();
                            effect.setDontRequireControllers();
                        }
                    }
                }
            } else {
                particleCount = 0;
            }
        }

        boolean powerCheck = PowerNodeRegistry.For(this.world).checkPower(this, getCookTickPowerCost());
        if (this.canSmelt() && this.isSmelting() && powerCheck) {
            ++this.timeSpentCooking;

            if (this.timeSpentCooking >= getModifiedCookTime()) {
                if (!this.world.isRemote) {
                    this.smeltItem();
                    this.markDirty(); // Mark dirty when smelting completes
                } else {
                    world.playSound(pos.getX(), pos.getY(), pos.getZ(), AMSounds.CALEFACTOR_BURN, SoundCategory.BLOCKS, 0.2f, 1.0f, true);
                }
                this.timeSpentCooking = 0;
                if (!world.isRemote) {
                    sendCookStatusUpdate(false);
                }
            }
            if (!world.isRemote) {
                if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, getCookTickPowerCost()) &&
                        PowerNodeRegistry.For(world).checkPower(this, PowerTypes.NEUTRAL, getCookTickPowerCost()) &&
                        PowerNodeRegistry.For(world).checkPower(this, PowerTypes.LIGHT, getCookTickPowerCost())) {

                    PowerNodeRegistry.For(this.world).consumePower(this, PowerTypes.DARK, getCookTickPowerCost());
                    PowerNodeRegistry.For(this.world).consumePower(this, PowerTypes.NEUTRAL, getCookTickPowerCost());
                    PowerNodeRegistry.For(this.world).consumePower(this, PowerTypes.LIGHT, getCookTickPowerCost());

                } else {
                    PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), getCookTickPowerCost());
                }
            }
        } else if (!this.isSmelting() && this.canSmelt() && powerCheck) {
            this.timeSpentCooking = 1;
            if (!world.isRemote) {
                sendCookStatusUpdate(true);
                this.markDirty(); // Mark dirty when starting to smelt
            }
        } else if (!this.canSmelt() && this.timeSpentCooking > 0) {
            this.timeSpentCooking = 0;
            if (!world.isRemote) {
                this.markDirty(); // Mark dirty when stopping cooking
            }
        }
        // Fix performance: Only mark dirty when state actually changes, not every tick
    }

    public int getCookProgressScaled(int par1) {
        return this.timeSpentCooking * par1 / getModifiedCookTime();
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

    private int numFociOfType(Class<?> type) {
        int count = 0;
        for (int i = 2; i < getSizeInventory(); ++i) {
            if (!inventory.get(i).isEmpty() && type.isInstance(inventory.get(i).getItem())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public int getSizeInventory() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
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

    public void setInventorySlotContents(int index, ItemStack stack) {
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
        return "Calefactor";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
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
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return i == 0;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing var1) {
        return new int[]{0, 1, 5};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j) {
        return i == 0;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j) {
        return i == 1 || i == 5;
    }

    @Override
    public int getChargeRate() {
        int numFoci = numFociOfType(ItemFocusCharge.class);
        int base = 20;
        if (numFoci > 0) {
            base += 27 * numFoci;
        }

        return base;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory.get(6);
        runes[1] = inventory.get(7);
        runes[2] = inventory.get(8);
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
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public ITextComponent getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0:
                return this.timeSpentCooking;
            default:
                return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0:
                this.timeSpentCooking = (short) value;
                break;
        }
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void markDirty() {
        markForUpdate();
        super.markDirty();
    }

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
