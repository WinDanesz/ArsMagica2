package am2.common.blocks.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class TileEntityChalkArrow extends TileEntity {

    private int colorIndex = 0; // 0 = white (byMetadata ordering)
    // Direction the arrow texture points (used by TESR for wall arrows)
    private net.minecraft.util.EnumFacing arrowFacing = net.minecraft.util.EnumFacing.NORTH;

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex & 0xF;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public net.minecraft.util.EnumFacing getArrowFacing() {
        return arrowFacing;
    }

    public void setArrowFacing(net.minecraft.util.EnumFacing facing) {
        this.arrowFacing = facing;
        markDirty();
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Color", colorIndex);
        compound.setInteger("ArrowFacing", arrowFacing.getHorizontalIndex());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        colorIndex = compound.getInteger("Color") & 0xF;
        int fi = compound.getInteger("ArrowFacing");
        arrowFacing = net.minecraft.util.EnumFacing.byHorizontalIndex(fi & 3);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Nullable
    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }
}
