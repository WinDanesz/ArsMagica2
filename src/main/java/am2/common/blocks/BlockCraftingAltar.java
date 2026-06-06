package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.common.registry.AMTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCraftingAltar extends BlockAMPowered {

    public static final PropertyBool MIMIC = PropertyBool.create("mimic");

    public BlockCraftingAltar() {
        super(Material.ROCK);
        setCreativeTab(AMTabs.AMBLOCKS);
        this.setHardness(1.5f);
        this.setResistance(10.0f);
        this.setHarvestLevel("pickaxe", 0);
        setDefaultState(blockState.getBaseState().withProperty(MIMIC, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MIMIC);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        // bye bye TESR
        TileEntityCraftingAltar te = getTileEntity(worldIn, pos);
        return te != null &&  te.isStructureValid() && te.getMimicState() != null ? te.getMimicState() : state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCraftingAltar();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntityCraftingAltar te = getTileEntity(worldIn, pos);
            if (te != null) {
                playerIn.sendStatusMessage(new TextComponentTranslation(te.isStructureValid() ? "am2.tooltip.altarStructureValid" : "am2.tooltip.altarStructureInvalid"), true);
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    public BlockCraftingAltar registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        // TODO: registry GameRegistry.register(new ItemBlock(this), rl);
        return this;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return true;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.SOLID;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return true;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return true;
    }

    public static TileEntityCraftingAltar getTileEntity(IBlockAccess world, BlockPos pos) {
        if (world == null)
            return null;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityCraftingAltar))
            return null;

        return (TileEntityCraftingAltar) te;
    }


}
