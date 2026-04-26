package am2.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class BlockWitchwoodDoubleSlab extends BlockSlab {

    private Block slab;

    public BlockWitchwoodDoubleSlab(Block slab) {
        super(Material.WOOD);
        this.slab = slab;
        this.setHardness(2.0f);
        this.setResistance(2.0f);
        this.setHarvestLevel("axe", 2);
    }

    @Override
    public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(new ItemStack(Item.getItemFromBlock(this.slab), 1));
        drops.add(new ItemStack(Item.getItemFromBlock(this.slab), 1));
        return drops;
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    protected static boolean isHalfSlab(IBlockState state) {
        return true;
    }

    @Override
    public String getTranslationKey(int meta) {
        return null;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
                                  EntityPlayer player) {
        return new ItemStack(Item.getItemFromBlock(this.slab));
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState();
        if (!this.isDouble())
            iblockstate = iblockstate.withProperty(HALF,
                    (meta) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);

        return iblockstate;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) == EnumBlockHalf.BOTTOM ? 0 : 1;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, HALF);
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return HALF;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return 0;
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
