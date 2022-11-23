package am2.common.items;

import am2.api.IBoundItem;
import am2.api.extensions.ISpellCaster;
import am2.common.defs.ItemDefs;
import am2.common.spell.SpellCaster;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemBoundSword extends ItemSword implements IBoundItem {

	public ItemBoundSword() {
		super(ItemDefs.BOUND);
		this.maxStackSize = 1;
		this.setMaxDamage(0);
		this.setCreativeTab(null);
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
		if (!stack.hasTagCompound())
			return true;
		int hurtResist = target.hurtResistantTime;
		target.hurtResistantTime = 0;
		ItemStack copiedStack = stack.copy();
		ISpellCaster caster = stack.copy().getCapability(SpellCaster.INSTANCE, null);
		if (caster != null)
			caster.createSpellData(copiedStack).execute(attacker.world, attacker, target, target.posX, target.posY, target.posZ, null);
		target.hurtResistantTime = hurtResist;
		return true;
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return false;
	}

//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean onDroppedByPlayer(ItemStack item, EntityPlayer player) {
//		item.setItem(ItemDefs.spell);
//		return false;
//	}

	@Override
	public float maintainCost(EntityPlayer player, ItemStack stack) {
		return normalMaintain;
	}

	public ItemSword registerAndName(String name) {
		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
	//	// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}

}
