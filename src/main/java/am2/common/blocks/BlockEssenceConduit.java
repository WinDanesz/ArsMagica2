package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityEssenceConduit;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockEssenceConduit extends BlockAMPowered {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");
    private static final AxisAlignedBB[] facingAABB = {
            new AxisAlignedBB(0.25, 0.4375, 0.25, 0.75, 1.0, 0.75),
            new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 0.5625, 0.75),
            new AxisAlignedBB(0.25, 0.25, 0.4375, 0.75, 0.75, 1.0),
            new AxisAlignedBB(0.25, 0.25, 0.0, 0.75, 0.75, 0.5625),
            new AxisAlignedBB(0.4375, 0.25, 0.25, 1.0, 0.75, 0.75),
            new AxisAlignedBB(0.0, 0.25, 0.25, 0.5625, 0.75, 0.75)
    };

    public BlockEssenceConduit() {
        super(Material.CLOTH);
        setHardness(3.0f);
        boundingBox = facingAABB[1];
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        // Enable JSON model rendering for the base (TESR will render the crystal on top)
        this.defaultRender = true;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getStateFromMeta(meta).withProperty(FACING, facing);
    }

    @Override
    public TileEntity createNewTileEntity(World par1World, int i) {
        return new TileEntityEssenceConduit();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return facingAABB[state.getValue(FACING).getIndex()];
    }
}
