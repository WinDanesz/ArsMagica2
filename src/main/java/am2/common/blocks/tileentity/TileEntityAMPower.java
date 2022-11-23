package am2.common.blocks.tileentity;

import am2.api.power.IPowerNode;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;

import java.util.List;

public abstract class TileEntityAMPower extends TileEntity implements IPowerNode<TileEntityAMPower>, ITickable {
	protected int capacity;
	protected boolean canRequestPower = true;
	private int tickCounter;

	private static final int REQUEST_INTERVAL = 20;

	public TileEntityAMPower(int capacity) {
		this.capacity = capacity;
	}

	protected void setNoPowerRequests() {
		this.canRequestPower = false;
	}

	protected void setPowerRequests() {
		this.canRequestPower = true;
	}

	/***
	 * Whether or not the tile entity is *capable* of providing power.
	 */
	@Override
	public boolean canProvidePower(PowerTypes type) {
		return false;
	}

	@Override
	public void invalidate() {
		PowerNodeRegistry.For(this.world).removePowerNode(this);
		super.invalidate();
	}

	@Override
	public void update() {
		if (!this.world.isRemote && this.canRequestPower() && this.tickCounter++ >= this.getRequestInterval()) {
			this.tickCounter = 0;
			List<PowerTypes> powerTypes = this.getValidPowerTypes();
			for (PowerTypes type : powerTypes) {
				float amtObtained = PowerNodeRegistry.For(this.world).requestPower(this, type, this.getChargeRate());
				if (amtObtained > 0)
					PowerNodeRegistry.For(this.world).insertPower(this, type, amtObtained);
			}
		}
		//world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	public int getRequestInterval() {
		return REQUEST_INTERVAL;
	}

	@Override
	public float particleOffset(int axis) {
		return 0.5f;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
	}

	@Override
	public void setWorld(World worldIn) {
		super.setWorld(worldIn);
		PowerNodeRegistry.For(this.world).registerPowerNode(this);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		return super.writeToNBT(nbttagcompound);
	}

	@Override
	public float getCapacity() {
		return this.capacity;
	}

	public void setPower(PowerTypes type, float amount) {
		PowerNodeRegistry.For(this.world).setPower(this, type, amount);
	}

	@Override
	public List<PowerTypes> getValidPowerTypes() {
		return PowerTypes.all();
	}

	@Override
	public boolean canRequestPower() {
		return this.canRequestPower;
	}

	@Override
	public boolean isSource() {
		return false;
	}
}
