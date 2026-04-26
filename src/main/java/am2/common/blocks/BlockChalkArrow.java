package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityChalkArrow;
import am2.common.registry.AMItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockChalkArrow extends Block {

    public static final PropertyDirection FACING    = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyBool      ON_CEILING = PropertyBool.create("on_ceiling");
    public static final PropertyBool      ON_WALL    = PropertyBool.create("on_wall");

    private static final AxisAlignedBB AABB_FLOOR       = new AxisAlignedBB(0,    0,    0,    1,    0.02, 1   );
    private static final AxisAlignedBB AABB_CEILING     = new AxisAlignedBB(0,    0.98, 0,    1,    1,    1   );
    private static final AxisAlignedBB AABB_NORTH_WALL  = new AxisAlignedBB(0,    0,    0,    1,    1,    0.02);
    private static final AxisAlignedBB AABB_SOUTH_WALL  = new AxisAlignedBB(0,    0,    0.98, 1,    1,    1   );
    private static final AxisAlignedBB AABB_EAST_WALL   = new AxisAlignedBB(0.98, 0,    0,    1,    1,    1   );
    private static final AxisAlignedBB AABB_WEST_WALL   = new AxisAlignedBB(0,    0,    0,    0.02, 1,    1   );

    public BlockChalkArrow() {
        super(Material.CIRCUITS);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(ON_CEILING, false)
                .withProperty(ON_WALL, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ON_CEILING, ON_WALL);
    }

    // Encoding:
    //   0-3  : floor,   arrow direction = FACING
    //   4-7  : ceiling, arrow direction = FACING
    //   8-11 : wall,    outward face    = FACING
    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta >= 8)
            return getDefaultState().withProperty(ON_WALL, true)
                    .withProperty(FACING, EnumFacing.byHorizontalIndex((meta - 8) & 3));
        if (meta >= 4)
            return getDefaultState().withProperty(ON_CEILING, true)
                    .withProperty(FACING, EnumFacing.byHorizontalIndex((meta - 4) & 3));
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int fi = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(ON_WALL))    return 8 + fi;
        if (state.getValue(ON_CEILING)) return 4 + fi;
        return fi;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityChalkArrow();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (state.getValue(ON_WALL)) {
            switch (state.getValue(FACING)) {
                case NORTH: return AABB_NORTH_WALL;
                case SOUTH: return AABB_SOUTH_WALL;
                case EAST:  return AABB_EAST_WALL;
                default:    return AABB_WEST_WALL;
            }
        }
        return state.getValue(ON_CEILING) ? AABB_CEILING : AABB_FLOOR;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 1.0f;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!isSupportValid(state, world, pos)) {
            world.setBlockToAir(pos);
        }
    }

    private boolean isSupportValid(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state.getValue(ON_WALL)) {
            EnumFacing outward = state.getValue(FACING);
            return world.isSideSolid(pos.offset(outward.getOpposite()), outward, false);
        }
        if (state.getValue(ON_CEILING)) {
            return world.isSideSolid(pos.up(), EnumFacing.DOWN, false);
        }
        return world.isSideSolid(pos.down(), EnumFacing.UP, false);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.isAirBlock(pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        int colorIndex = 0;
        if (te instanceof TileEntityChalkArrow) {
            colorIndex = ((TileEntityChalkArrow) te).getColorIndex();
        }
        return new ItemStack(AMItems.colored_chalk, 1, colorIndex);
    }
}
