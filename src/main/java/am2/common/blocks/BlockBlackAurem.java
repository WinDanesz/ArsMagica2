package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityBlackAurem;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockBlackAurem extends BlockEssenceGenerator {

    public BlockBlackAurem() {
        super(Material.CLOTH);
        setLightLevel(0.73f);
        setBlockBounds(0.0f, 0.5f, 0.0f, 1.0f, 2f, 1.0f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBlackAurem();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
