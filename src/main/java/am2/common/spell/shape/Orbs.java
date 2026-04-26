package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.AMParticleDefs;
import am2.common.entity.EntitySpellOrb;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class Orbs extends SpellShape {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                Items.ENDER_PEARL,
                Items.SNOWBALL,
                Items.SLIME_BALL,
                AMItems.moonstone
        };
    }



    @Override
    public float manaCostMultiplier() {
        return 1.5F;
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
            // Calculate number of orbs based on POWER modifier (default 1, +1 per power modifier)
            int numOrbs = 1 + spell.getModifierCount(SpellModifiers.BUFF_POWER);

            // Collect existing orbs already orbiting this caster
            List<EntitySpellOrb> existingOrbList = world.getEntitiesWithinAABB(EntitySpellOrb.class,
                    caster.getEntityBoundingBox().grow(4))
                    .stream()
                    .filter(e -> e.getShooter() == caster)
                    .collect(Collectors.toList());
            int existingCount = existingOrbList.size();

            // Apply the config cap
            int maxOrbs = ArsMagica.config.getMaxOrbsPerPlayer();
            int orbsToSpawn = Math.min(numOrbs, maxOrbs - existingCount);
            if (orbsToSpawn <= 0) return SpellCastResult.FREE_CAST;

            int newTotal = existingCount + orbsToSpawn;

            // Update existing orbs so they redistribute evenly with the new total
            for (EntitySpellOrb existing : existingOrbList) {
                existing.setTotalOrbs(newTotal);
            }

            // Spawn the orbs at their target orbit positions for even spacing
            // Use world time as the base angle so spawn position matches onUpdate's first tick
            float baseAngle = (float) (world.getTotalWorldTime() * EntitySpellOrb.ORBIT_SPEED);
            for (int i = 0; i < orbsToSpawn; i++) {
                int orbIndex = existingCount + i;
                float spawnAngle = baseAngle + (float) (2.0F * Math.PI * orbIndex / newTotal);

                double spawnX = caster.posX + Math.cos(spawnAngle) * EntitySpellOrb.ORBIT_RADIUS;
                double spawnZ = caster.posZ + Math.sin(spawnAngle) * EntitySpellOrb.ORBIT_RADIUS;
                double spawnY = caster.posY + caster.getEyeHeight() * 0.5 + Math.sin(spawnAngle * 2) * 0.5;

                EntitySpellOrb orb = new EntitySpellOrb(world);
                orb.setPosition(spawnX, spawnY, spawnZ);
                orb.setShooter(caster);
                orb.setSpell(spell);
                orb.setIcon(AMParticleDefs.getParticleForAffinity(spell.getMainShift()));
                orb.setOrbIndex(orbIndex);
                orb.setTotalOrbs(newTotal);
                world.spawnEntity(orb);
            }
        }
        return SpellCastResult.SUCCESS;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.COLOR, SpellModifiers.BUFF_POWER);
    }
}
