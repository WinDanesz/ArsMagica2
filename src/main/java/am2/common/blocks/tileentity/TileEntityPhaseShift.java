package am2.common.blocks.tileentity;

import am2.common.blocks.BlockPhaseShift;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class TileEntityPhaseShift extends TileEntity implements ITickable {

    private IBlockState originalState;
    private int ticksRemaining;
    private boolean restored = false;

    public TileEntityPhaseShift() {
    }

    public void setOriginalState(IBlockState state, int duration) {
        this.originalState = state;
        this.ticksRemaining = duration;
        markDirty();
    }

    public IBlockState getOriginalState() {
        return originalState;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    @Override
    public void update() {
        if (world.isRemote) return;
        if (restored) return;

        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining % 20 == 0) {
                markDirty();
            }
        }

        if (ticksRemaining <= 0) {
            restoreOriginalBlock();
        }
    }

    public void restoreOriginalBlock() {
        if (restored) return;
        restored = true;

        if (world != null && !world.isRemote) {
            IBlockState toRestore = originalState != null ? originalState : Blocks.STONE.getDefaultState();
            world.setBlockState(pos, toRestore, 3);
        }
    }

    /**
     * Checks if the adjacent block at the given position is also a phase shift block.
     */
    public boolean isAdjacentPhaseShift(net.minecraft.util.EnumFacing facing) {
        if (world == null) return false;
        return world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockPhaseShift;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("ticksRemaining", ticksRemaining);
        compound.setBoolean("restored", restored);
        if (originalState != null) {
            Block block = originalState.getBlock();
            ResourceLocation regName = block.getRegistryName();
            if (regName != null) {
                compound.setString("origBlock", regName.toString());
                compound.setInteger("origMeta", block.getMetaFromState(originalState));
            }
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.ticksRemaining = compound.getInteger("ticksRemaining");
        this.restored = compound.getBoolean("restored");
        if (compound.hasKey("origBlock")) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString("origBlock")));
            if (block != null && block != Blocks.AIR) {
                int meta = compound.getInteger("origMeta");
                this.originalState = block.getStateFromMeta(meta);
            }
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public net.minecraft.network.play.server.SPacketUpdateTileEntity getUpdatePacket() {
        return new net.minecraft.network.play.server.SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
