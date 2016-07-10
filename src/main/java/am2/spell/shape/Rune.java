package am2.spell.shape;

import am2.defs.ItemDefs;
import am2.items.ItemSpellBase;
import am2.spell.IShape;
import am2.spell.SpellCastResult;
import am2.spell.SpellModifiers;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Rune implements IShape{

	@Override
	public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean consumeMBR, int useCount){
//		int procs = SpellUtils.getModifiedInt_Add(1, stack, caster, target, world, SpellModifiers.PROCS);
		boolean targetWater = SpellUtils.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack);
		RayTraceResult mop = item.getMovingObjectPosition(caster, world, 8.0f, true, targetWater);
		if (mop == null || mop.typeOfHit == RayTraceResult.Type.ENTITY) return SpellCastResult.EFFECT_FAILED;

//		if (!BlockDefs.spellRune.placeAt(world, mop.blockX, mop.blockY + 1, mop.blockZ, SpellUtils.instance.mainAffinityFor(stack).ordinal())){
//			return SpellCastResult.EFFECT_FAILED;
//		}
//		if (!world.isRemote){
//			world.setTileEntity(mop.blockX, mop.blockY + 1, mop.blockZ, BlockDefs.spellRune.createTileEntity(world, 0));
//			BlockDefs.spellRune.setSpellStack(world, mop.blockX, mop.blockY + 1, mop.blockZ, SpellUtils.instance.popStackStage(stack));
//			BlockDefs.spellRune.setPlacedBy(world, mop.blockX, mop.blockY + 1, mop.blockZ, caster);
//			int meta = world.getBlockMetadata(mop.blockX, mop.blockY + 1, mop.blockZ);
//			BlockDefs.spellRune.setNumTriggers(world, mop.blockX, mop.blockY + 1, mop.blockZ, meta, procs);
//		}

		return SpellCastResult.SUCCESS;
	}

	@Override
	public boolean isChanneled(){
		return false;
	}

	@Override
	public Object[] getRecipe(){
		//Blue Rune, Red Rune, White Rune, Black Rune, Orange Rune, Purple Rune, Yellow Rune
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.RED.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.YELLOW.getDyeDamage())
		};
	}

	@Override
	public float manaCostMultiplier(ItemStack spellStack){
		return 1.8f;
	}

	@Override
	public boolean isTerminusShape(){
		return false;
	}

	@Override
	public boolean isPrincipumShape(){
		return true;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {}

//	@Override
//	public String getSoundForAffinity(Affinity affinity, ItemStack stack, World world){
//		return "arsmagica2:spell.rune.cast";
//	}
}
