package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityOtherworldAura;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockOtherworldAura extends BlockAMPowered {

    public BlockOtherworldAura() {
        super(Material.CIRCUITS);
        setHardness(2.0f);
        setResistance(2.0f);
        setBlockBounds(0.25f, 0.25f, 0.25f, 0.75f, 0.75f, 0.75f);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int var2) {
        return new TileEntityOtherworldAura();
    }

    @Override
    public int getLightValue(IBlockState state) {
        return 15;
    }

    @Override
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(@Nullable IBlockState blockState, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
                                ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (placer instanceof EntityPlayer) {
            TileEntityOtherworldAura te = (TileEntityOtherworldAura) worldIn.getTileEntity(pos);
            assert te != null;
            te.setPlacedByUsername(placer.getName());
        }
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
