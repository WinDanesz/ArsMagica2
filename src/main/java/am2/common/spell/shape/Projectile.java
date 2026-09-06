package am2.common.spell.shape;

import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.AMParticleDefs;
import am2.common.entity.EntitySpellProjectile;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Projectile extends SpellShape {

    /** Cosmetic override for the Air Elemental's damaging knockback gust. */
    public static final String AIR_PROJECTILE_KEY = "AirProjectile";

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                Items.ARROW,
                Items.SNOWBALL
        };
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
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData spell, World world) {
        return super.getSoundForAffinity(spell.getStoredData().getBoolean(AIR_PROJECTILE_KEY) ? Affinities.air : affinity, spell, world);
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        if (!world.isRemote) {
            double projectileSpeed = spell.getModifiedValue(SpellModifiers.SPEED, Operation.ADD, world, caster, target); // SpellUtils.getModifiedDouble_Add(stack, caster, target, world, SpellModifiers.SPEED);
            float projectileGravity = (float) spell.getModifiedValue(SpellModifiers.GRAVITY, Operation.ADD, world, caster, target);
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
            // The synced icon also selects the projectile's secondary trail.
            Affinity appearance = spell.getStoredData().getBoolean(AIR_PROJECTILE_KEY) ? Affinities.air : spell.getMainShift();
            projectile.setIcon(AMParticleDefs.getParticleForAffinity(appearance));
            world.spawnEntity(projectile);
        }
        return SpellCastResult.SUCCESS;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.GRAVITY, SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.HOMING, SpellModifiers.TARGET_NONSOLID_BLOCKS, SpellModifiers.SPEED, SpellModifiers.BOUNCE, SpellModifiers.PIERCING, SpellModifiers.GRAVITATE);
    }
}
