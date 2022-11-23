package am2.common.items;

import am2.common.defs.CreativeTabsDefs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemArsMagica extends Item{
	
	public ItemArsMagica() {
		setCreativeTab(CreativeTabsDefs.tabAM2Items);
		setMaxDamage(0);
		setHasSubtypes(true);
	}


	public ItemArsMagica registerAndName(String name) {
	// todo registry
//		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
//		// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}
}
