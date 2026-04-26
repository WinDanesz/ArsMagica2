package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.extensions.IEntityExtension;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleApproachEntity;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class LifeTap extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {

        if (world.getBlockState(pos).getBlock().equals(Blocks.MOB_SPAWNER)) {
            boolean hasMatch = RitualShapeHelper.instance.matchesRitual(this, world, pos);
            if (hasMatch) {
                if (!world.isRemote) {
                    world.setBlockToAir(pos);
                    RitualShapeHelper.instance.consumeReagents(this, world, pos);
                    RitualShapeHelper.instance.consumeShape(this, world, pos);
                    EntityItem item = new EntityItem(world);
                    item.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    item.setItem(new ItemStack(AMBlocks.inert_spawner));
                    world.spawnEntity(item);
                } else {

                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!(target instanceof EntityLivingBase)) return false;
        if (!world.isRemote) {
            double damage = spell.getModifiedValue(ArsMagica.config.getLifeTapDamageMultiplier(), SpellModifiers.DAMAGE, Operation.MULTIPLY, world, caster, target);
            IEntityExtension casterProperties = EntityExtension.For(caster);
            float manaRefunded = (float) (((damage * 0.01)) * casterProperties.getMaxMana());

            if ((caster).attackEntityFrom(DamageSource.OUT_OF_WORLD, (int) Math.floor(damage))) {
                casterProperties.setCurrentMana(casterProperties.getCurrentMana() + manaRefunded);
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE);
    }


    @Override
    public float manaCost() {
        return 0;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 25; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(2, 2, 2);
                particle.setMaxAge(15);
                particle.setParticleScale(0.1f);
                particle.AddParticleController(new ParticleApproachEntity(particle, target, 0.1, 0.1, 1, false));
                if (rand.nextBoolean())
                    particle.setRGBColorF(0.4f, 0.1f, 0.5f);
                else
                    particle.setRGBColorF(0.1f, 0.5f, 0.1f);
                if (colorModifier > -1) {
                    particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.life, Affinities.ender);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage()),
                AMBlocks.aum
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.01f;
    }

    @Override
    public IMultiblock getRitualShape() {
        return RitualShapeHelper.instance.corruption;
    }

    @Override
    public ItemStack[] getRitualReagents() {
        return new ItemStack[]{
                new ItemStack(AMItems.mob_focus),
                AffinityShiftUtils.getEssenceForAffinity(Affinities.ender)
        };
    }
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getResult() {
        return new ItemStack(AMBlocks.inert_spawner);
    }
}
