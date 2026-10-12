package am2.common.blocks;

import am2.common.registry.AMTabs;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockWitchwoodStairs extends BlockStairs {

    public BlockWitchwoodStairs(IBlockState state) {
        super(state);
        this.setHardness(2.0f);
        this.setResistance(2.0f);
        this.setHarvestLevel("axe", 2);
        this.setCreativeTab(AMTabs.AMBLOCKS);
        // vanilla only sets this for its own stairs in Block.registerBlocks(), so modded stairs render dark without it
        this.useNeighborBrightness = true;
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }
}
