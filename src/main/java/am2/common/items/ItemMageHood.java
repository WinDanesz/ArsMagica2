package am2.common.items;

import am2.client.utils.ModelLibrary;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.registry.AMItems;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemMageHood extends AMArmor {

    private static final String NBT_KEY_HOOD_DOWN = "hoodDown";

    public ItemMageHood(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial material, int renderIndex, EntityEquipmentSlot slot) {
        super(inheritFrom, material, renderIndex, slot);
    }

    public static boolean isHoodDown(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getBoolean(NBT_KEY_HOOD_DOWN);
    }

    public static void setHoodDown(ItemStack stack, boolean down) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(NBT_KEY_HOOD_DOWN, down);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking() && stack.getItem() == AMItems.mage_hood) {
            if (!world.isRemote) {
                boolean newState = !isHoodDown(stack);
                setHoodDown(stack, newState);
                player.sendStatusMessage(new TextComponentTranslation(newState ? "am2.tooltip.hood_down" : "am2.tooltip.hood_up"), true);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return super.onItemRightClick(world, player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (stack.getItem() == AMItems.mage_hood) {
            tooltip.add(I18n.format("am2.tooltip.hood_toggle"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (isHoodDown(itemStack) && itemStack.getItem() == AMItems.mage_hood) {
            return ModelLibrary.instance.mageHoodLowered;
        }
        return null;
    }

}
