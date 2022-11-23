package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.defs.BlockDefs;
import am2.common.defs.ItemDefs;
import am2.common.defs.SoundDefs;
import am2.common.entity.EntitySpellEffect;
import am2.common.items.ItemOre;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Wave extends SpellShape {

	@Override
	public Object[] getRecipe() {
		return new Object[]{
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_VINTEUM),
				BlockDefs.magicWall,
				"E:*", 25000
		};
	}

	@Override
	public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
		if (world.isRemote) return SpellCastResult.SUCCESS;
		double radius = spell.getModifiedValue(1, SpellModifiers.RADIUS, Operation.ADD, world, caster, target);
		double speed = spell.getModifiedValue(1, SpellModifiers.SPEED, Operation.ADD, world, caster, target) * 0.5;
		int duration = (int) spell.getModifiedValue(20, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
		int gravityModifiers = spell.getModifierCount(SpellModifiers.GRAVITY);
		boolean hasPiercing = spell.isModifierPresent(SpellModifiers.PIERCING);

		EntitySpellEffect wave = new EntitySpellEffect(world);
		wave.setRadius((float) radius);
		wave.setTicksToExist(duration);
		wave.SetCasterAndStack(caster, spell);
		wave.setPosition(x, y + 1, z);
		wave.setWave(caster.rotationYaw, (float) speed);
		wave.noClip = hasPiercing;
		wave.setGravity(gravityModifiers * 0.5f);
		world.spawnEntity(wave);
		return SpellCastResult.SUCCESS;
	}

	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.SPEED, SpellModifiers.PIERCING, SpellModifiers.TARGET_NONSOLID_BLOCKS);
	}

	@Override
	public boolean isChanneled() {
		return false;
	}

	@Override
	public float manaCostMultiplier() {
		return 3f;
	}

	@Override
	public boolean isTerminusShape() {
		return false;
	}

	@Override
	public boolean isPrincipumShape() {
		return true;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
	}

	@Override
	public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
		return SoundDefs.CAST_MAP.get(affinity);
	}

}
