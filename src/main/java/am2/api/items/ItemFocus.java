package am2.api.items;

import am2.common.defs.CreativeTabsDefs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public abstract class ItemFocus extends Item{
	
	public ItemFocus() {
		setCreativeTab(CreativeTabsDefs.tabAM2Items);
		setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	public abstract Object[] getRecipeItems();

	public abstract String getInGameName();
	
	public ItemFocus registerAndName(String name) {
		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
		//// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}
}
