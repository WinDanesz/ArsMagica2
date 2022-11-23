package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.AMParticleDefs;
import am2.common.defs.ItemDefs;
import am2.common.defs.SoundDefs;
import am2.common.entity.EntitySpellProjectile;
import am2.common.items.ItemOre;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Projectile extends SpellShape {

	@Override
	public Object[] getRecipe() {
		return new Object[]{
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_VINTEUM),
				Items.ARROW,
				Items.SNOWBALL
		};
	}

	@Override
	public boolean isChanneled() {
		return false;
	}

	@Override
	public float manaCostMultiplier() {
		return 1.25F;
	}

	@Override
	public boolean isTerminusShape() {
		return false;
	}

	@Override
	public boolean isPrincipumShape() {
		return false;
	}

	@Override
	public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
		if (!world.isRemote) {
			double projectileSpeed = spell.getModifiedValue(SpellModifiers.SPEED, Operation.ADD, world, caster, target); // SpellUtils.getModifiedDouble_Add(stack, caster, target, world, SpellModifiers.SPEED);
			float projectileGravity = (float) spell.getModifiedValue(SpellModifiers.GRAVITY, Operation.MULTIPLY, world, caster, target); // SpellUtils.getModifiedDouble_Mul(stack, caster, target, world, SpellModifiers.GRAVITY);
			int projectileBounce = (int) spell.getModifiedValue(SpellModifiers.BOUNCE, Operation.ADD, world, caster, target); // SpellUtils.getModifiedInt_Add(stack, caster, target, world, SpellModifiers.BOUNCE);
			EntitySpellProjectile projectile = new EntitySpellProjectile(world);
			projectile.setPosition(caster.posX, caster.getEyeHeight() + caster.posY, caster.posZ);
			projectile.motionX = caster.getLookVec().x * projectileSpeed;
			projectile.motionY = caster.getLookVec().y * projectileSpeed;
			projectile.motionZ = caster.getLookVec().z * projectileSpeed;
			if (spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS))
				projectile.setTargetWater();
			projectile.setGravity(projectileGravity);
			projectile.setBounces(projectileBounce);
			projectile.setNumPierces(spell.getModifierCount(SpellModifiers.PIERCING) * 4);
			projectile.setShooter(caster);
			projectile.setHoming(spell.isModifierPresent(SpellModifiers.HOMING));
			projectile.setSpell(spell);
			projectile.setIcon(AMParticleDefs.getParticleForAffinity(spell.getMainShift()));
			world.spawnEntity(projectile);
		}
		return SpellCastResult.SUCCESS;
	}

	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.HOMING, SpellModifiers.TARGET_NONSOLID_BLOCKS, SpellModifiers.SPEED, SpellModifiers.BOUNCE, SpellModifiers.PIERCING);
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {

	}

	@Override
	public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
		return SoundDefs.CAST_MAP.get(affinity);
	}
}
