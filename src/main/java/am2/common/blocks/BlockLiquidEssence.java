package am2.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * Block for liquid essence fluid that provides magical properties.
 * Uses Material.WATER so vanilla drowning and fall-slowing apply automatically.
 */
public class BlockLiquidEssence extends BlockFluidClassic {

    public BlockLiquidEssence(Fluid fluid) {
        super(fluid, Material.WATER);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        return 9;
    }
}
