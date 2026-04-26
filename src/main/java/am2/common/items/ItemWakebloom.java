package am2.common.items;

import am2.common.registry.AMBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class ItemWakebloom extends ItemBlock {

    public ItemWakebloom(Block block) {
        super(block);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        RayTraceResult mop = this.rayTrace(world, player, true);

        if (mop == null) {
            return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
        } else {
            if (mop.typeOfHit == Type.BLOCK) {

                if (!world.canMineBlockBody(player, mop.getBlockPos())) {
                    return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
                }

                if (!player.canPlayerEdit(mop.getBlockPos(), mop.sideHit, stack)) {
                    return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
                }

                if (world.getBlockState(mop.getBlockPos()) == Blocks.FLOWING_WATER.getDefaultState() || world.getBlockState(mop.getBlockPos()) == Blocks.WATER.getDefaultState()) {
                    world.setBlockState(mop.getBlockPos().up(), AMBlocks.wakebloom.getDefaultState());

                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                }
            }

            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
        }
    }

}
