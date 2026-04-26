package am2.common.compat.ancientspellcraft.spells;

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
 * Spell component that applies the {@code ancientspellcraft:growth} potion effect to a target.
 *
 * <p>Only registered when both AncientSpellcraft and Artemislib are present.
 * The potion is looked up lazily from the Forge potion registry at cast time,
 * so no direct compile-time dependency on AncientSpellcraft is required.
 */
public class Growth extends SpellComponent {

    /** Default duration: 10 seconds = 200 ticks. */
    private static final int DEFAULT_DURATION = 200;

    /** Lazily cached potion. {@code null} if not yet fetched or if it was not found. */
    @Nullable
    private static Potion growthPotion;

    @Nullable
    private static Potion getPotion() {
        if (growthPotion == null) {
            growthPotion = ForgeRegistries.POTIONS.getValue(new ResourceLocation("ancientspellcraft", "growth"));
        }
        return growthPotion;
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
        return 60;
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{ new ItemStack(Items.GOLDEN_CARROT) };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster,
                               Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 10; i++) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", x, y + 1, z);
            if (particle != null) {
                particle.setIgnoreMaxAge(false);
                particle.addVelocity(
                        (rand.nextDouble() - 0.5) * 0.08,
                        rand.nextDouble() * 0.06,
                        (rand.nextDouble() - 0.5) * 0.08);
                particle.setParticleAge(10 + rand.nextInt(8));
                // Green hue: natural growth
                particle.setRGBColorF(0.0f, 0.7f + rand.nextFloat() * 0.3f, 0.1f + rand.nextFloat() * 0.2f);
                particle.AddParticleController(new ParticleFloatUpward(particle, 0.05f, 0.02f, 1, false));
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.nature);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }
}
