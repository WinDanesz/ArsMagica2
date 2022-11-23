package am2.common.items;

import java.util.List;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.common.entity.EntityFlicker;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFlickerJar extends ItemArsMagica{

	public ItemFlickerJar(){
		super();
		this.setMaxDamage(0);
		setHasSubtypes(true);
	}
	// TODO registry
//	@Override
//	@SideOnly(Side.CLIENT)
//	public String getItemStackDisplayName(ItemStack stack){
//		int meta = stack.getItemDamage();
//		String baseName = I18n.format("am2.item.flickerJar");
//		if (meta == ArsMagicaAPI.getAffinityRegistry().getId(Affinity.NONE))
//			return I18n.format("item.arsmagica2:flickerJar.name", I18n.format("am2.tooltip.empty"));
//
//		Affinity aff = ArsMagicaAPI.getAffinityRegistry().getObjectById(meta);
//		baseName = I18n.format("item.arsmagica2:flickerJar.name", aff.getLocalizedName());
//
//		return baseName;
//	}
//
	public void setFlickerJarTypeFromFlicker(ItemStack stack, EntityFlicker flick){
//		stack.setItemDamage(ArsMagicaAPI.getAffinityRegistry().getId(flick.getFlickerAffinity()));
	}
//
//	@Override
//	@SideOnly(Side.CLIENT)
//	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List<ItemStack> par3List){
//		for (Affinity aff : ArsMagicaAPI.getAffinityRegistry()){
//			par3List.add(new ItemStack(this, 1, ArsMagicaAPI.getAffinityRegistry().getId(aff)));
//		}
//	}
}
