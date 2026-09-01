package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Spell component that applies the effects of a splash potion held as a reagent.
 * On entity targets: applies effects directly to the target.
 * On block targets: acts as a splash, applying effects with distance falloff to all nearby entities.
 */
public class ApplyPotion extends SpellComponent {

    private static final float DEFAULT_RADIUS = 4.0f;

    @Override
    public ItemStack[] reagents(EntityLivingBase caster) {
        // Require any splash potion; all splash potions have meta 0, so this matches any type.
        return new ItemStack[]{ new ItemStack(Items.SPLASH_POTION) };
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (world.isRemote) return false;
        if (!(target instanceof EntityLivingBase)) return false;

        ItemStack potion = findSplashPotion(caster);
        if (potion.isEmpty()) return false;

        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
        if (effects.isEmpty()) return false;

        int durationMult = Math.max(1, (int) spell.getModifiedValue(1, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target));
        int bonusAmplifier = spell.getModifierCount(SpellModifiers.BUFF_POWER);

        for (PotionEffect effect : effects) {
            ((EntityLivingBase) target).addPotionEffect(
                    new PotionEffect(effect.getPotion(),
                            effect.getDuration() * durationMult,
                            effect.getAmplifier() + bonusAmplifier));
        }
        return true;
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        if (world.isRemote) return false;

        ItemStack potion = findSplashPotion(caster);
        if (potion.isEmpty()) return false;

        List<PotionEffect> effects = PotionUtils.getEffectsFromStack(potion);
        if (effects.isEmpty()) return false;

        double radius = spell.getModifiedValue(DEFAULT_RADIUS, SpellModifiers.RADIUS, Operation.MULTIPLY, world, caster, null);
        AxisAlignedBB area = new AxisAlignedBB(
                impactX - radius, impactY - radius, impactZ - radius,
                impactX + radius, impactY + radius, impactZ + radius);

        List<EntityLivingBase> nearbyEntities = world.getEntitiesWithinAABB(EntityLivingBase.class, area);

        int durationMult = Math.max(1, (int) spell.getModifiedValue(1, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, null));
        int bonusAmplifier = spell.getModifierCount(SpellModifiers.BUFF_POWER);

        for (EntityLivingBase entity : nearbyEntities) {
            double dist = Math.sqrt(entity.getDistanceSq(impactX, impactY, impactZ)) / radius;
            double intensity = Math.max(0.0, 1.0 - dist);
            if (intensity <= 0.0) continue;

            for (PotionEffect effect : effects) {
                if (effect.getPotion().isInstant()) {
                    effect.getPotion().affectEntity(null, null, entity, effect.getAmplifier() + bonusAmplifier, intensity);
                } else {
                    int duration = (int) (effect.getDuration() * intensity * 0.75 * durationMult);
                    if (duration > 20) {
                        entity.addPotionEffect(new PotionEffect(
                                effect.getPotion(), duration, effect.getAmplifier() + bonusAmplifier));
                    }
                }
            }
        }

        return !nearbyEntities.isEmpty();
    }

    private ItemStack findSplashPotion(EntityLivingBase caster) {
        if (!(caster instanceof EntityPlayer)) return ItemStack.EMPTY;
        EntityPlayer player = (EntityPlayer) caster;
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == Items.SPLASH_POTION) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
    }

    @Override
    public float manaCost() {
        return 40;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                PotionUtils.addPotionToItemStack(new ItemStack(Items.SPLASH_POTION), PotionTypes.WEAKNESS),
                new ItemStack(Items.BREWING_STAND)
        };
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 8; i++) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle",
                    x + rand.nextGaussian() * 0.3,
                    y + 0.5 + rand.nextGaussian() * 0.3,
                    z + rand.nextGaussian() * 0.3);
            if (particle != null) {
                particle.setMaxAge(15 + rand.nextInt(10));
                particle.setRGBColorF(0.7f, 0.2f, 0.9f); // violet - typical potion color
                particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.04f, 1, false));
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.water);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }
}
