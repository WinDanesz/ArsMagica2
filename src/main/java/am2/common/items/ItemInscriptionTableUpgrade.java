package am2.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemInscriptionTableUpgrade extends ItemArsMagica{

	public ItemInscriptionTableUpgrade(){
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		this.setHasSubtypes(true);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		for (int i = 0; i < 3; ++i){
			items.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack){
		int meta = stack.getItemDamage();
		switch (meta){
		case 2:
			return I18n.format("item.arsmagica2:inscup_3.name");
		case 1:
			return I18n.format("item.arsmagica2:inscup_2.name");
		case 0:
		default:
			return I18n.format("item.arsmagica2:inscup_1.name");
		}
	}
}
