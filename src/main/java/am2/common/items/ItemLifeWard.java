package am2.common.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemLifeWard extends ItemArsMagica{

	public ItemLifeWard(){
		super();
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5){
		if (par4 < 9 && par3Entity.ticksExisted % 80 == 0 && par3Entity instanceof EntityLivingBase){
			float abs = ((EntityLivingBase)par3Entity).getAbsorptionAmount();
			if (abs < 20){
				abs++;
				((EntityLivingBase)par3Entity).setAbsorptionAmount(abs);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("am2.tooltip.life_ward"));
		tooltip.add(I18n.format("am2.tooltip.life_ward2"));
		super.addInformation(stack, world, tooltip, flag);
	}

}
