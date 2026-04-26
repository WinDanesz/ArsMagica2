package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.common.registry.AMTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCraftingAltar extends BlockAMPowered {

    public BlockCraftingAltar() {
        super(Material.ROCK);
        setCreativeTab(AMTabs.AMBLOCKS);
        this.setHardness(1.5f);
        this.setResistance(10.0f);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCraftingAltar();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityCraftingAltar) {
                boolean valid = ((TileEntityCraftingAltar) te).isStructureValid();
                if (valid) {
                    playerIn.sendStatusMessage(new TextComponentTranslation("am2.tooltip.altarStructureValid"), true);
                } else {
                    playerIn.sendStatusMessage(new TextComponentTranslation("am2.tooltip.altarStructureInvalid"), true);
                }
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
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
                                        EnumFacing side) {
        return true;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.SOLID;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }


    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }
}
