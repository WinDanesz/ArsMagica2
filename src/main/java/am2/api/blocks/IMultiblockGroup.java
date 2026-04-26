package am2.api.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public interface IMultiblockGroup {
    boolean matches(World world, BlockPos pos);

    void addBlock(BlockPos pos);

    int getMinX();

    int getMinY();

    int getMinZ();

    int getMaxX();

    int getMaxY();

    int getMaxZ();

    ImmutableList<BlockPos> getPositions();

    ImmutableList<IBlockState> getStates();

    void addState(IBlockState state);

    ArrayList<IBlockState> getState(BlockPos pos);

}
