package am2.common.blocks.tileentity;

import am2.common.blocks.BlockEssenceConduit;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityEssenceConduit extends TileEntityAMPower {

    private float rotationX;
    private float rotationY;
    private float rotationZ;

    private boolean redstonePowered;

    private float rotationIncrementX;
    private float rotationIncrementY;
    private float rotationIncrementZ;

    private boolean isFirstTick = true;

    private boolean channeling;

    public TileEntityEssenceConduit() {
        super(1);

        redstonePowered = false;
    }

    @Override
    public void update() {
        if (isFirstTick) {
            rotationX = world.rand.nextInt(360);
            rotationY = world.rand.nextInt(360);
            rotationZ = world.rand.nextInt(360);
            rotationIncrementX = world.rand.nextFloat() * 0.002f + 0.005f;
            rotationIncrementY = world.rand.nextFloat() * 0.002f + 0.005f;
            rotationIncrementZ = world.rand.nextFloat() * 0.002f + 0.005f;
            isFirstTick = false;
        }
        if (world != null && world.getStrongPower(pos) > 0) {
            redstonePowered = true;
        } else {
            redstonePowered = false;
        }

        if (!world.isRemote) {
            boolean wasChanneling = channeling;
            channeling = PowerNodeRegistry.For(world).getHighestPower(this) > 0;
            if (wasChanneling != channeling) {
                world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            }
        }

        incrementRotations();
        super.update();
    }

    @Override
    public float particleOffset(int axis) {
        EnumFacing meta = world.getBlockState(pos).getValue(BlockEssenceConduit.FACING);

        if (axis == 0) {
            switch (meta) {
                case WEST:
                    return 0.8f;
                case EAST:
                    return 0.2f;
                default:
                    return 0.5f;
            }
        } else if (axis == 1) {
            switch (meta) {
                case UP:
                    return 0.2f;
                case DOWN:
                    return 0.8f;
                default:
                    return 0.5f;
            }
        } else if (axis == 2) {
            switch (meta) {
                case NORTH:
                    return 0.8f;
                case SOUTH:
                    return 0.2f;
                default:
                    return 0.5f;
            }
        }

        return 0.5f;
    }

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }

    public void incrementRotations() {
        rotationX += rotationIncrementX;
        rotationY += rotationIncrementY;
        rotationZ += rotationIncrementZ;

        if (rotationX >= 360) {
            rotationX = 0;
        }

        if (rotationY >= 360) {
            rotationY = 0;
        }

        if (rotationZ >= 360) {
            rotationZ = 0;
        }

        if (rotationX < 0) {
            rotationX = 359;
        }

        if (rotationY < 0) {
            rotationY = 359;
        }

        if (rotationZ < 0) {
            rotationZ = 359;
        }
    }

    @Override
    public int getChargeRate() {
        return 1;
    }

    @Override
    public boolean canRequestPower() {
        return !this.redstonePowered;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return true;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setBoolean("channeling", channeling);
        return new SPacketUpdateTileEntity(pos, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        channeling = pkt.getNbtCompound().getBoolean("channeling");
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        nbt.setBoolean("channeling", channeling);
        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        channeling = tag.getBoolean("channeling");
    }
}
