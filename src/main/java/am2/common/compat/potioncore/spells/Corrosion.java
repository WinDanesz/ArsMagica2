package am2.common.compat.potioncore.spells;

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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Spell component that applies the {@code potioncore:corrosion} potion effect to a target.
 * Corrosion damages the target's armor and held items over time (gold is immune).
 *
 * <p>Only registered when PotionCore is present.
 * The potion is looked up lazily from the Forge registry at cast time,
 * so there is no compile-time dependency on PotionCore.
 */
public class Corrosion extends SpellComponent {

    /** Default duration: 10 seconds = 200 ticks. */
    private static final int DEFAULT_DURATION = 200;

    @Nullable
    private static Potion corrosionPotion;

    @Nullable
    private static Potion getPotion() {
        if (corrosionPotion == null) {
            corrosionPotion = ForgeRegistries.POTIONS.getValue(new ResourceLocation("potioncore", "corrosion"));
        }
        return corrosionPotion;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        Potion potion = getPotion();
        if (potion == null) return false;
        if (!(target instanceof EntityLivingBase)) return false;

        if (!world.isRemote) {
            int duration = (int) spell.getModifiedValue(DEFAULT_DURATION, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
            int amplifier = spell.getModifierCount(SpellModifiers.BUFF_POWER);
            ((EntityLivingBase) target).addPotionEffect(new PotionEffect(potion, duration, amplifier));
        }
        return true;
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        return false;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DURATION, SpellModifiers.BUFF_POWER);
    }

    @Override
    public float manaCost() {
        return 55;
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{ new ItemStack(Items.IRON_NUGGET) };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 12; i++) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y + 1, z);
            if (particle != null) {
                particle.setIgnoreMaxAge(false);
                particle.addVelocity(
                        (rand.nextDouble() - 0.5) * 0.1,
                        rand.nextDouble() * 0.03,
                        (rand.nextDouble() - 0.5) * 0.1);
                particle.setParticleAge(6 + rand.nextInt(6));
                // Rust-orange / brown hue
                particle.setRGBColorF(0.55f + rand.nextFloat() * 0.2f, 0.3f + rand.nextFloat() * 0.15f, 0.0f);
                particle.AddParticleController(new ParticleFloatUpward(particle, 0.04f, 0.01f, 1, false));
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.earth);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }
}
