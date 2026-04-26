package am2.common.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemInscriptionTable extends ItemBlock {

    public ItemInscriptionTable(Block block) {
        super(block);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!block.isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }
        BlockPos placePos = pos.offset(player.getHorizontalFacing().rotateY());
        if (world.isAirBlock(placePos) || world.getBlockState(placePos).getBlock().isReplaceable(world, placePos)) {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }
        return EnumActionResult.FAIL;
    }

}
