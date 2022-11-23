package am2.common.items;

import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.skill.SkillPoint;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemInfinityOrb extends ItemArsMagica {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
		if (point == null) {
			player.sendMessage(new TextComponentString("Broken Item : Please use a trash bin."));
		}
		giveSkillPoints(player, stack, point);
		return EnumActionResult.SUCCESS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
		if (point == null) { return "Unavailable Item"; }
		return I18n.format("item.arsmagica2:inf_orb_" + point.toString().toLowerCase() + ".name");
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
		if (point == null) { tooltip.add("Place this into your inventory to convert it into the usable version"); }
	}

	private ItemStack giveSkillPoints(EntityPlayer player, ItemStack stack, SkillPoint type) {
		if (EntityExtension.For(player).getCurrentLevel() > 0) {
			if (!player.world.isRemote) {
				SkillData.For(player).setSkillPoint(type, SkillData.For(player).getSkillPoint(type) + 1);
			}
			if (player.world.isRemote) {
				player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.infOrb" + type.toString())));
			}
			if (!player.capabilities.isCreativeMode) { stack.shrink(1); }
			if (stack.getCount() < 1) {
				player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
			}
		} else {
			if (player.world.isRemote) {
				int message = player.world.rand.nextInt(10);
				player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.infOrbFail" + message)));
			}
		}
		return stack;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(new ItemStack(this, 1, 0));
		items.add(new ItemStack(this, 1, 1));
		items.add(new ItemStack(this, 1, 2));
		if (ArsMagicaAPI.hasTier4()) { items.add(new ItemStack(this, 1, 3)); }
		if (ArsMagicaAPI.hasTier5()) { items.add(new ItemStack(this, 1, 4)); }
		if (ArsMagicaAPI.hasTier6()) { items.add(new ItemStack(this, 1, 5)); }
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
		SkillPoint point = SkillPointRegistry.getPointForTier(stack.getItemDamage());
		if (point == null && stack.getItemDamage() > 0) { stack.setItemDamage(stack.getItemDamage() - 1); }
	}

}
