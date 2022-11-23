package am2.common.items;

import am2.api.IBoundItem;
import am2.common.defs.ItemDefs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SuppressWarnings("deprecation")
public class ItemBoundShield extends ItemShield implements IBoundItem {

	public ItemBoundShield() {
		super();
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setCreativeTab(null);
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}

//	@Override
//	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
//		item.setItem(ItemDefs.spell);
//		return false;
//	}

	@Override
	public float maintainCost(EntityPlayer player, ItemStack stack) {
		return normalMaintain;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		return I18n.format("item." + getRegistryName().toString() + ".name");
	}

	public ItemBoundShield registerAndName(String name) {
		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
	//	// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}
	
}
