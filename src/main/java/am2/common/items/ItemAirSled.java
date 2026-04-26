package am2.common.items;

import am2.common.entity.EntityAirSled;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemAirSled extends Item {

    public ItemAirSled() {
        super();
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("am2.tooltip.air_sled"));
        super.addInformation(itemStack, world, tooltip, flag);
    }

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (!world.isRemote) {
            EntityAirSled sled = new EntityAirSled(world);
            sled.setPosition(pos.getX() + hitX, pos.getY() + hitY + 0.5, pos.getZ() + hitZ);
            world.spawnEntity(sled);
            player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            return EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }
}
