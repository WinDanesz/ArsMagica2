package am2.common.items;

import am2.client.gui.AMGuiHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemArcaneCompendium extends Item {

    public ItemArcaneCompendium() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (worldIn.isRemote) {
            AMGuiHelper.OpenCompendiumGui(stack);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }
}
