package am2.common.items;

import java.util.List;

import am2.common.defs.ItemDefs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArcaneGuardianSpellbook extends ItemSpellBook {

	public ItemArcaneGuardianSpellbook(){
		super();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("am2.tooltip.arcanespellbook"));
		super.addInformation(stack, world, tooltip, flag);
	}

	@Override
	public int getItemEnchantability(){
		return 0;
	}

	@Override
	public boolean isBookEnchantable(ItemStack bookStack, ItemStack enchantBook){
		return false;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(ItemDefs.arcaneSpellBookEnchanted.copy());
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack){
		return EnumRarity.EPIC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		ItemStack activeSpell = GetActiveItemStack(par1ItemStack);
		if (activeSpell != null){
			return String.format("\2477%s \2477(" + activeSpell.getDisplayName() + "\2477)", I18n.format("item.arsmagica2:arcaneSpellBook.name"));
		}
		return I18n.format("item.arsmagica2:arcane_spellbook.name");
	}
}
