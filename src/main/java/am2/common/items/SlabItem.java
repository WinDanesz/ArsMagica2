package am2.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SlabItem extends ItemBlock {

    Block doubleSlab;
    CreativeTabs tab;

    public SlabItem(Block block, Block doubleSlab) {
        super(block);
        this.doubleSlab = doubleSlab;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (stack.getCount() == 0) {
            return EnumActionResult.FAIL;
        } else if (!playerIn.canPlayerEdit(pos.offset(side), side, stack)) {
            return EnumActionResult.FAIL;
        } else {
            IBlockState iblockstate = worldIn.getBlockState(pos);

            if (iblockstate.getBlock() == getBlock()) {
                BlockSlab.EnumBlockHalf enumblockhalf = iblockstate.getValue(BlockSlab.HALF);

                if ((side == EnumFacing.UP && enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM
                        || side == EnumFacing.DOWN && enumblockhalf == BlockSlab.EnumBlockHalf.TOP)) {
                    IBlockState iblockstate1 = this.doubleSlab.getDefaultState();

                    if (worldIn.checkNoEntityCollision(this.doubleSlab.getBoundingBox(iblockstate1, worldIn, pos))
                            && worldIn.setBlockState(pos, iblockstate1, 3)) {
                        worldIn.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                                this.doubleSlab.getSoundType().getPlaceSound(), SoundCategory.BLOCKS,
                                (this.doubleSlab.getSoundType().getVolume() + 1.0F) / 2.0F,
                                this.doubleSlab.getSoundType().getPitch() * 0.8F, true);
                        stack.shrink(1);
                    }

                    return EnumActionResult.SUCCESS;
                }
            }

            return (this.tryPlace(stack, worldIn, pos.offset(side)) || (super.onItemUse(playerIn, worldIn, pos,
                    hand, side, hitX, hitY, hitZ) == EnumActionResult.SUCCESS ? true : false))
                    ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing facing,
                                       EntityPlayer player, ItemStack stack) {
        BlockPos blockpos1 = pos;
        IBlockState iblockstate = world.getBlockState(pos);

        if (iblockstate.getBlock() == getBlock()) {
            boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;

            if ((facing == EnumFacing.UP && !flag || facing == EnumFacing.DOWN && flag)) {
                return true;
            }
        }

        pos = pos.offset(facing);
        IBlockState iblockstate1 = world.getBlockState(pos);
        return iblockstate1.getBlock() == getBlock()
                || super.canPlaceBlockOnSide(world, blockpos1, facing, player, stack);
    }

    private boolean tryPlace(ItemStack stack, World world, BlockPos pos) {
        IBlockState iblockstate = world.getBlockState(pos);

        if (iblockstate.getBlock() == getBlock()) {
            IBlockState iblockstate1 = this.doubleSlab.getDefaultState();

            if (world.checkNoEntityCollision(this.doubleSlab.getBoundingBox(iblockstate1, world, pos))
                    && world.setBlockState(pos, iblockstate1, 3)) {
                world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        this.doubleSlab.getSoundType().getPlaceSound(), SoundCategory.BLOCKS,
                        (this.doubleSlab.getSoundType().getVolume() + 1.0F) / 2.0F,
                        this.doubleSlab.getSoundType().getPitch() * 0.8F, true);
                stack.shrink(1);
            }

            return true;
        }

        return false;
    }

}

