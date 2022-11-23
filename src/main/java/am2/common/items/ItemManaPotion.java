package am2.common.items;

import am2.common.buffs.BuffEffectManaRegen;
import am2.common.defs.ItemDefs;
import am2.common.extensions.EntityExtension;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemManaPotion extends ItemArsMagica{

	public ItemManaPotion(){
		super();
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		EntityExtension props = EntityExtension.For(player);
		if (props.getCurrentMana() < props.getMaxMana()){
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
	}


	private float getManaRestored(){
		float manaRestored = 0;
		if (this == ItemDefs.lesserManaPotion){
			manaRestored = 100;
		}else if (this == ItemDefs.standardManaPotion){
			manaRestored = 250;
		}else if (this == ItemDefs.greaterManaPotion){
			manaRestored = 2000;
		}else if (this == ItemDefs.epicManaPotion){
			manaRestored = 5000;
		}else if (this == ItemDefs.legendaryManaPotion){
			manaRestored = 10000;
		}

		return manaRestored;
	}

	private int getManaRegenLevel(){
		if (this == ItemDefs.lesserManaPotion){
			return 0;
		}else if (this == ItemDefs.standardManaPotion){
			return 0;
		}else if (this == ItemDefs.greaterManaPotion){
			return 1;
		}else if (this == ItemDefs.epicManaPotion){
			return 1;
		}else if (this == ItemDefs.legendaryManaPotion){
			return 2;
		}
		return 0;
	}

	private int getManaRegenDuration(){
		if (this == ItemDefs.lesserManaPotion){
			return 600;
		}else if (this == ItemDefs.standardManaPotion){
			return 1200;
		}else if (this == ItemDefs.greaterManaPotion){
			return 1800;
		}else if (this == ItemDefs.epicManaPotion){
			return 2400;
		}else if (this == ItemDefs.legendaryManaPotion){
			return 3000;
		}
		return 600;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		return EnumAction.DRINK;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World par2World, EntityLivingBase player){
		stack = new ItemStack(Items.GLASS_BOTTLE);
		EntityExtension.For(player).setCurrentMana(EntityExtension.For(player).getCurrentMana() + getManaRestored());

		if (!par2World.isRemote){
			player.addPotionEffect(new BuffEffectManaRegen(getManaRegenDuration(), getManaRegenLevel()));
		}

		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 32;
	}

	@Override
	public boolean getHasSubtypes(){
		return false;
	}
}
