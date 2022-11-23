package am2.common.items;

import java.util.List;

import am2.common.defs.PotionEffectsDefs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemManaMartini extends ItemFood{
	public ItemManaMartini(){
		super(0, 0, false);
		this.setPotionEffect(new PotionEffect(PotionEffectsDefs.BURNOUT_REDUCTION, 300, 0), 1.0f);
	}

	public Item registerAndName(String name) {
		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
		//// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_){
		return EnumAction.DRINK;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add(I18n.format("am2.tooltip.shaken"));
		super.addInformation(stack, world, tooltip, flagIn);
	}
}
