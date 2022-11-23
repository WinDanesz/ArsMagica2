package am2.common.items;

import java.util.List;

import am2.common.defs.ItemDefs;
import am2.common.extensions.EntityExtension;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemManaPotionBundle extends ItemArsMagica{
	public ItemManaPotionBundle(){
		super();
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
	}

	private Item getPotion(int damage){
		int id = damage >> 8;
		switch (id){
		case 0:
			return ItemDefs.lesserManaPotion;
		case 1:
			return ItemDefs.standardManaPotion;
		case 2:
			return ItemDefs.greaterManaPotion;
		case 3:
			return ItemDefs.epicManaPotion;
		case 4:
			return ItemDefs.legendaryManaPotion;
		}
		return ItemDefs.lesserManaPotion;
	}

	private int getUses(int damage){
		return (damage & 0x0F);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
		ItemStack stack = player.getHeldItem(hand);
		EntityExtension props = EntityExtension.For(player);
		if (props.getCurrentMana() < props.getMaxMana()){
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}


	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World par2World, EntityLivingBase ent){
		if (!(ent instanceof EntityPlayer)) return super.onItemUseFinish(stack, par2World, ent);
		EntityPlayer par3EntityPlayer = (EntityPlayer)ent;
		Item potion = getPotion(stack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			ItemDefs.lesserManaPotion.onItemUseFinish(stack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.standardManaPotion){
			ItemDefs.standardManaPotion.onItemUseFinish(stack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.greaterManaPotion){
			ItemDefs.greaterManaPotion.onItemUseFinish(stack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.epicManaPotion){
			ItemDefs.epicManaPotion.onItemUseFinish(stack, par2World, par3EntityPlayer);
		}else if (potion == ItemDefs.legendaryManaPotion){
			ItemDefs.legendaryManaPotion.onItemUseFinish(stack, par2World, par3EntityPlayer);
		}

		stack.setItemDamage(((stack.getItemDamage() >> 8) << 8) + getUses(stack.getItemDamage()) - 1);

		if (getUses(stack.getItemDamage()) == 0){
			giveOrDropItem(par3EntityPlayer, new ItemStack(Items.STRING));
			if (stack.getCount() == 0)
				stack = ItemStack.EMPTY;
		}

		giveOrDropItem(par3EntityPlayer, new ItemStack(Items.GLASS_BOTTLE));

		return stack;
	}

	private void giveOrDropItem(EntityPlayer player, ItemStack stack){
		if (!player.inventory.addItemStackToInventory(stack))
			player.dropItem(stack, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		Item potion = getPotion(stack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			tooltip.add("Lesser Mana Restoration");
		}else if (potion == ItemDefs.standardManaPotion){
			tooltip.add("Standard Mana Restoration");
		}else if (potion == ItemDefs.greaterManaPotion){
			tooltip.add("Greater Mana Restoration");
		}else if (potion == ItemDefs.epicManaPotion){
			tooltip.add("Epic Mana Restoration");
		}else if (potion == ItemDefs.legendaryManaPotion){
			tooltip.add("Legendary Mana Restoration");
		}
		tooltip.add("" + getUses(stack.getItemDamage()) + " " + I18n.format("am2.tooltip.uses") + ".");
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(new ItemStack(ItemDefs.manaPotionBundle, 1, 3));
		items.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (1 << 8) + 3));
		items.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (2 << 8) + 3));
		items.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (3 << 8) + 3));
		items.add(new ItemStack(ItemDefs.manaPotionBundle, 1, (4 << 8) + 3));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack){
		Item potion = getPotion(stack.getItemDamage());
		if (potion == ItemDefs.lesserManaPotion){
			return String.format("%s %s", I18n.format("item.arsmagica2:lesser_mana_potion.name"), I18n.format("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.standardManaPotion){
			return String.format("%s %s", I18n.format("item.arsmagica2:standard_mana_potion.name"), I18n.format("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.greaterManaPotion){
			return String.format("%s %s", I18n.format("item.arsmagica2:greater_mana_potion.name"), I18n.format("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.epicManaPotion){
			return String.format("%s %s", I18n.format("item.arsmagica2:epic_mana_potion.name"), I18n.format("item.arsmagica2:potion_bundle.name"));
		}else if (potion == ItemDefs.legendaryManaPotion){
			return String.format("%s %s", I18n.format("item.arsmagica2:legendary_mana_potion.name"), I18n.format("item.arsmagica2:potion_bundle.name"));
		}
		return "? " + I18n.format("am2.items.bundle");
	}

	@Override
	public boolean getHasSubtypes(){
		return true;
	}

}
