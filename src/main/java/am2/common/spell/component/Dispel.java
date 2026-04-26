package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import am2.common.utils.EntityUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.*;

public class Dispel extends SpellComponent {

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {

        if (!(target instanceof EntityLivingBase)) return false;

        if (EntityUtils.isSummon((EntityLivingBase) target)) {
            if (EntityUtils.getOwner((EntityLivingBase) target) == caster.getEntityId()) {
                target.attackEntityFrom(DamageSource.MAGIC, 50000);
                return true;
            }
        }
        List<Potion> effectsToRemove = new ArrayList<>();
        int magnitudeLeft = 6;
        Iterator<PotionEffect> iter = ((EntityLivingBase) target).getActivePotionEffects().iterator();
        while (iter.hasNext()) {
            Potion potion = ((PotionEffect) iter.next()).getPotion();

            PotionEffect pe = ((EntityLivingBase) target).getActivePotionEffect(potion);
            int magnitudeCost = pe.getAmplifier();

            if (magnitudeLeft >= magnitudeCost) {
                magnitudeLeft -= magnitudeCost;
                // Potion cleanup now handled by removeAttributesModifiersFromEntity when effect is removed
                effectsToRemove.add(potion);
            }

        }

        if (effectsToRemove.isEmpty() && EntityExtension.For((EntityLivingBase) target).getCurrentSummons() == 0)
            return false;

        if (!world.isRemote)
            removePotionEffects((EntityLivingBase) target, effectsToRemove);

        if (EntityExtension.For((EntityLivingBase) target).getCurrentSummons() > 0) {
            if (!world.isRemote) {
                Iterator<Entity> it = world.loadedEntityList.iterator();
                int i = EntityExtension.For((EntityLivingBase) target).getCurrentSummons();
                while (it.hasNext()) {
                    Entity ent = (Entity) it.next();
                    if (ent instanceof EntityLivingBase && EntityUtils.isSummon((EntityLivingBase) ent) && EntityUtils.getOwner((EntityLivingBase) ent) == target.getEntityId()) {
                        ent.setDead();
                        if (--i <= 0)
                            break;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }


    private void removePotionEffects(EntityLivingBase target, List<Potion> effectsToRemove) {
        for (Potion i : effectsToRemove) {
            if (i == AMPotions.flight || i == AMPotions.levitation) {
                if (target instanceof EntityPlayer && target.isPotionActive(AMPotions.flight)) {
                    ((EntityPlayer) target).capabilities.isFlying = false;
                    ((EntityPlayer) target).capabilities.allowFlying = false;
                }
            }
            target.removePotionEffect(i);
        }
    }

    @Override
    public float manaCost() {
        return 200;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 2, 1);
                particle.AddParticleController(new ParticleOrbitEntity(particle, target, 0.1f + rand.nextFloat() * 0.1f, 1, false));
                if (rand.nextBoolean())
                    particle.setRGBColorF(0.7f, 0.1f, 0.7f);
                particle.setMaxAge(20);
                particle.setParticleScale(0.1f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.none);
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
                new ItemStack(AMItems.arcane_ash),
                new ItemStack(AMItems.blue_topaz),
                Items.MILK_BUCKET
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0;
    }

}
