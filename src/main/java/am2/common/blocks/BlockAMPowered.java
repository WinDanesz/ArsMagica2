package am2.common.blocks;

import am2.api.power.IPowerNode;
import am2.common.blocks.tileentity.TileEntityAMPower;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockAMPowered extends BlockAMContainer {

    protected boolean defaultRender = false;

    public BlockAMPowered(Material material) {
        super(material);
    }

    protected boolean HandleSpecialItems(World world, EntityPlayer player, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityAMPower)) {
            return false;
        }

        if (player.getHeldItemMainhand() != ItemStack.EMPTY &&
                (player.getHeldItemMainhand().getItem() == AMItems.attuning_staff
                        || player.getHeldItemMainhand().getItem() == AMItems.crystal_wrench)) {
            return true;
        }

        return false;
    }

    protected String getColorNameFromPowerType(PowerTypes type) {
        return I18n.format("am2.gui.powerType" + type.name());
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);

        if (HandleSpecialItems(worldIn, playerIn, pos))
            return false;

        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof IPowerNode<?>)
            PowerNodeRegistry.For(worldIn).removePowerNode((IPowerNode<?>) te);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof IPowerNode<?>)
            PowerNodeRegistry.For(worldIn).registerPowerNode((IPowerNode<?>) te);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        if (defaultRender) return EnumBlockRenderType.MODEL;
        return super.getRenderType(state);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        //if (defaultRender) return BlockRenderLayer.CUTOUT;
        return BlockRenderLayer.CUTOUT;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        if (defaultRender) return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullBlock(IBlockState state) {
        if (defaultRender) return super.isFullBlock(state);
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isFullCube(IBlockState state) {
        if (defaultRender) return super.isFullCube(state);
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isNormalCube(IBlockState state) {
        if (defaultRender) return super.isNormalCube(state);
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (defaultRender) return super.getLightOpacity(state, world, pos);
        return 0;
    }
}
