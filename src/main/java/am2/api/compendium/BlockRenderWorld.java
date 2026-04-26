package am2.api.compendium;

import am2.common.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;

public class BlockRenderWorld implements IBlockAccess {

    HashMap<BlockPos, IBlockState> states = new HashMap<>();

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
        return 15;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
        IBlockState state = states.get(pos);
        return state == null ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return getBlockState(pos) == Blocks.AIR.getDefaultState();
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return Biomes.OCEAN;
    }

    public void setBlockState(BlockPos pos, IBlockState state) {
        if (state == null)
            state = Blocks.AIR.getDefaultState();
        if (pos == null)
            return;
        states.put(pos, state);
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
        return 0;
    }

    @Override
    public WorldType getWorldType() {
        return WorldType.DEFAULT;
    }

    public void clear() {
        this.states.clear();
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
//		return false;
        try {
            return this.getBlockState(pos).isSideSolid(this, pos, side);
        } catch (Throwable t) {
            LogHelper.debug("Error check with isSideSolid.", t);
            return false;
        }
    }

}
