package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.ISpellFocus;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleHoldPosition;
import am2.client.particles.ParticleOrbitPoint;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.utils.DimensionUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityAstralBarrier extends TileEntityAMPower implements IInventory, IKeystoneLockable<TileEntityAstralBarrier> {

    private NonNullList<ItemStack> inventory;
    private boolean displayAura;
    private int particleTickCounter;

    public static int keystoneSlot = 0;

    public TileEntityAstralBarrier() {
        super(ArsMagica.config.getCapacityAstralBarrier());
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        displayAura = false;
        particleTickCounter = 0;
    }

    public void ToggleAuraDisplay() {
        this.displayAura = !this.displayAura;
    }

    public int getRadius() {
        if (!this.inventory.get(0).isEmpty() && this.inventory.get(0).getItem() instanceof ISpellFocus) {
            ISpellFocus focus = (ISpellFocus) this.inventory.get(0).getItem();
            return (focus.getFocusLevel() + 1) * 5;
        }
        return 0;
    }

    public boolean IsActive() {
        return PowerNodeRegistry.For(this.world).checkPower(this, ArsMagica.config.getAstralBarrierCostPerRadius() * getRadius()) && world.getStrongPower(pos) > 0 && getRadius() > 0;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        SPacketUpdateTileEntity packet = new SPacketUpdateTileEntity(pos, 0, compound);
        return packet;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public void setWorld(World worldIn) {
        super.setWorld(worldIn);
        DimensionUtilities.registerAstralBarrier(this);
    }

    @Override
    public void invalidate() {
        DimensionUtilities.invalidateAstralBarrier(this);
        super.invalidate();
    }

    @Override
    public void update() {
        super.update();

        int radius = getRadius();

        if (IsActive()) {
            PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(world).getHighestPowerType(this), ArsMagica.config.getAstralBarrierCostPerRadius() * radius);
        }

        if (world.isRemote) {
            if (IsActive()) {
                if (displayAura) {
                    AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "symbols", pos.getX(), pos.getY() + 0.5, pos.getZ());
                    if (effect != null) {
                        effect.setIgnoreMaxAge(false);
                        effect.setMaxAge(100);
                        effect.setParticleScale(0.5f);
                        effect.AddParticleController(new ParticleOrbitPoint(effect, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, false).SetOrbitSpeed(0.03).SetTargetDistance(radius));
                    }
                }

                particleTickCounter++;

                if (particleTickCounter >= 15) {

                    particleTickCounter = 0;

                    AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", pos.getX() + 0.5, pos.getY() + 0.1 + world.rand.nextDouble() * 0.5, pos.getZ() + 0.5);
                    if (effect != null) {
                        effect.setIgnoreMaxAge(false);
                        effect.setMaxAge(100);
                        effect.setParticleScale(0.5f);
                        effect.AddParticleController(new ParticleOrbitPoint(effect, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, false).SetOrbitSpeed(0.005).SetTargetDistance(world.rand.nextDouble() * 0.6 - 0.3));
                        effect.AddParticleController(new ParticleHoldPosition(effect, 80, 2, true));
                        effect.AddParticleController(new ParticleFadeOut(effect, 3, false).setFadeSpeed(0.05f));
                    }
                }
            }
        }
    }

    public void onEntityBlocked(EntityLivingBase entity) {
        if (this.world.isRemote) {
            if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, ArsMagica.config.getAstralBarrierDarkCost())) {
                entity.attackEntityFrom(DamageSource.MAGIC, 5);
                PowerNodeRegistry.For(world).consumePower(this, PowerTypes.DARK, ArsMagica.config.getAstralBarrierDarkCost());
            }
        }
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
    public String getName() {
        return "Astral Barrier";
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
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

//	private void writeInventory(AMDataWriter writer){
//		for (ItemStack stack : inventory){
//			if (stack == null){
//				writer.add(false);
//				continue;
//			}else{
//				writer.add(true);
//				writer.add(stack);
//			}
//		}
//	}

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
        return 50;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory.get(1);
        runes[1] = inventory.get(2);
        runes[2] = inventory.get(3);
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
