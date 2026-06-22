package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;

import java.util.List;

public class TileEntityManaBattery extends TileEntityAMPower implements ITileEntityAMBase {

    private boolean active;
    public static int storageCapacity = ArsMagica.config.getCapacityManaBattery();
    private PowerTypes outputPowerType = PowerTypes.NONE;
    //private int tickCounter = 0;
    boolean hasUpdated = false;
    int prevEnergy;

    public TileEntityManaBattery() {
        super(storageCapacity);
        active = false;
    }
    
    public int getClientEnergy() {
        return prevEnergy;
    }

    public PowerTypes getPowerType() {
        return outputPowerType;
    }

    public void setPowerType(PowerTypes type, boolean forceSubNodes) {
        this.outputPowerType = type;
        if (world != null && world.isRemote) {
            markDirty();
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return true;
    }

    @Override
    public void update() {

        if (world.getStrongPower(pos) > 0) {
            this.setPowerRequests();
        } else {
            this.setNoPowerRequests();
        }

        if (!this.world.isRemote) {
            PowerTypes highest = PowerNodeRegistry.For(world).getHighestPowerType(this);
            float amt = PowerNodeRegistry.For(world).getPower(this, highest);
            if (amt > 0) {
                boolean needsSync = false;
                if (this.outputPowerType != highest) {
                    this.outputPowerType = highest;
                    needsSync = true;
                }
                if (Math.abs(amt - prevEnergy) > storageCapacity * 0.05f) {
                    prevEnergy = (int)amt;
                    needsSync = true;
                }
                if (needsSync) {
                    this.getWorld().checkLight(this.getPos());
                    this.getWorld().notifyBlockUpdate(this.getPos(), this.getWorld().getBlockState(getPos()), this.getWorld().getBlockState(getPos()), 3);
                }
            } else {
                boolean needsSync = false;
                if (this.outputPowerType != PowerTypes.NONE) {
                    this.outputPowerType = PowerTypes.NONE;
                    needsSync = true;
                }
                if (prevEnergy > 0) {
                    prevEnergy = 0;
                    needsSync = true;
                }
                if (needsSync) {
                    this.getWorld().checkLight(this.getPos());
                    this.getWorld().notifyBlockUpdate(this.getPos(), this.getWorld().getBlockState(getPos()), this.getWorld().getBlockState(getPos()), 3);
                }
            }
        } else {
            if (this.getClientEnergy() > 0 && this.outputPowerType != PowerTypes.NONE) {
                // Occasional particle based on how full it is
                float fullness = (float)this.getClientEnergy() / this.getCapacity();
                if (fullness >= 0.3f) {
                    int chance = (int)(Math.max(10, (int)(100 - (fullness * 80))) * 1.5f); // between 15 (full) and 150 (empty)
                    if (this.world.rand.nextInt(chance) == 0) {
                        ArsMagica.proxy.spawnManaBatterySparkle(this);
                    }
                }
            }
        }
        this.markDirty();
//		this.getWorld().setBlockState(getPos(), this.getWorld().getBlockState(getPos()), 3);
//		this.getWorld().notifyBlockOfStateChange(getPos(), getBlockType());
//		if(this.tickCounter == 10) {
//			this.tickCounter++;
//			getWorld().notifyBlockUpdate(getPos(), getWorld().getBlockState(getPos()), getWorld().getBlockState(getPos()), 3);
//		} else{
//			if(this.tickCounter < 10)
//				this.tickCounter++;
//		}
        super.update();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setBoolean("isActive", active);
        nbttagcompound.setInteger("outputType", outputPowerType.ID());
        nbttagcompound.setInteger("clientEnergy", prevEnergy);
        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        active = nbttagcompound.getBoolean("isActive");
        if (nbttagcompound.hasKey("outputType"))
            outputPowerType = PowerTypes.getByID(nbttagcompound.getInteger("outputType"));
        if (nbttagcompound.hasKey("clientEnergy"))
            prevEnergy = nbttagcompound.getInteger("clientEnergy");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        int oldLight = this.world.getBlockState(this.getPos()).getLightValue(this.world, this.getPos());
        this.readFromNBT(pkt.getNbtCompound());
        int newLight = this.world.getBlockState(this.getPos()).getLightValue(this.world, this.getPos());
        if (oldLight != newLight) {
            this.world.checkLight(this.getPos());
        }
    }

    @Override
    public int getChargeRate() {
        return 1000;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        if (this.outputPowerType == PowerTypes.NONE)
            return PowerTypes.all();
        return Lists.newArrayList(outputPowerType);
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    public boolean dirty = false;

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

    @Override
    public void markDirty() {
        this.markForUpdate();
        super.markDirty();
    }
}
