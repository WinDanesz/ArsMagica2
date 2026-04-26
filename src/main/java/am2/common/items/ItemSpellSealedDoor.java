package am2.common.items;

import am2.common.registry.AMBlocks;
import am2.common.registry.AMTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Item that places the Spell-Sealed Door block (two-block-tall door).
 * Uses ItemDoor.placeDoor() to correctly place both halves.
 */
public class ItemSpellSealedDoor extends Item {

    public ItemSpellSealedDoor() {
        super();
        this.maxStackSize = 1;
        this.setCreativeTab(AMTabs.AMBLOCKS);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (facing != EnumFacing.UP) {
            return EnumActionResult.PASS;
        }

        pos = pos.up();

        if (player.canPlayerEdit(pos, facing, stack) && player.canPlayerEdit(pos.up(), facing, stack)) {
            if (!AMBlocks.spell_sealed_door.canPlaceBlockAt(worldIn, pos)) {
                return EnumActionResult.FAIL;
            }

            EnumFacing enumfacing = EnumFacing.fromAngle(player.rotationYaw);
            int i = enumfacing.getXOffset();
            int j = enumfacing.getZOffset();
            boolean flag = i < 0 && hitZ < 0.5F || i > 0 && hitZ > 0.5F
                    || j < 0 && hitX > 0.5F || j > 0 && hitX < 0.5F;

            ItemDoor.placeDoor(worldIn, pos, enumfacing, AMBlocks.spell_sealed_door, flag);
            stack.shrink(1);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.FAIL;
    }
}
