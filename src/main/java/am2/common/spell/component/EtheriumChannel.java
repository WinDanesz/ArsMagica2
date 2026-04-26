package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.power.IPowerNode;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

/**
 * Channels neutral etherium into a targeted power node. The transfer is lossy
 * (~55% efficiency). Each BUFF_POWER modifier scales both mana cost and etherium
 * output by ×1.25, keeping efficiency constant.
 */
public class EtheriumChannel extends SpellComponent {

    // Efficiency ≈ BASE_POWER / BASE_MANA ≈ 55 %. Both scale at ×1.25 per BUFF_POWER stack.
    private static final float BASE_POWER = 100f;
    private static final float BASE_MANA  = 180f;

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
                AMItems.arcane_compound,
                "E:" + PowerTypes.NEUTRAL.ID(), 500
        };
    }

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos,
                                    EnumFacing blockFace,
                                    double impactX, double impactY, double impactZ,
                                    EntityLivingBase caster) {
        if (world.isRemote) return false;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IPowerNode)) return false;

        IPowerNode<?> node = (IPowerNode<?>) te;
        if (!node.getValidPowerTypes().contains(PowerTypes.NEUTRAL)) return false;

        int stacks = spell.getModifierCount(SpellModifiers.BUFF_POWER);
        float amount = BASE_POWER * (float) Math.pow(1.25, stacks);

        return PowerNodeRegistry.For(world).insertPower(node, PowerTypes.NEUTRAL, amount) > 0;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return false;
    }

    @Override
    public float manaCost() {
        return BASE_MANA;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.BUFF_POWER);
    }

    @Override
    public void spawnParticles(World world, double x, double y, double z,
                               EntityLivingBase caster, Entity target,
                               Random rand, int colorModifier) {
        for (int i = 0; i < 10; i++) {
            double dx = (rand.nextDouble() - 0.5) * 0.6;
            double dy = rand.nextDouble() * 0.4;
            double dz = (rand.nextDouble() - 0.5) * 0.6;
            world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE,
                    x + dx, y + dy, z + dz, dx * 0.1, dy * 0.1, dz * 0.1);
        }
        for (int i = 0; i < 4; i++) {
            double dx = (rand.nextDouble() - 0.5) * 0.3;
            double dy = rand.nextDouble() * 0.3;
            double dz = (rand.nextDouble() - 0.5) * 0.3;
            world.spawnParticle(EnumParticleTypes.PORTAL, x + dx, y + dy, z + dz, 0, 0.05, 0);
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        if (affinity == Affinities.arcane) return 0.05f;
        return 0f;
    }
}
