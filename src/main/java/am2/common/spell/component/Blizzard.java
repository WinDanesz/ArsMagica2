package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.entity.EntitySpellEffect;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Blizzard extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.ice),
                new ItemStack(AMItems.blue_topaz),
                Blocks.ICE,
                AffinityShiftUtils.getEssenceForAffinity(Affinities.ice)
        };
    }

    private boolean spawnBlizzard(SpellData spell, World world, EntityLivingBase caster, Entity target, double x, double y, double z) {

        List<EntitySpellEffect> zones = world.getEntitiesWithinAABB(EntitySpellEffect.class, new AxisAlignedBB(x - 10, y - 10, z - 10, x + 10, y + 10, z + 10));

        for (EntitySpellEffect zone : zones) {
            if (zone.isBlizzard())
                return false;
        }

        if (!world.isRemote) {
            int radius = (int) spell.getModifiedValue(2, SpellModifiers.RADIUS, Operation.ADD, world, caster, target);
            double damage = spell.getModifiedValue(ArsMagica.config.getBlizzardDamageMultiplier(), SpellModifiers.DAMAGE, Operation.MULTIPLY, world, caster, target);
            int duration = (int) spell.getModifiedValue(100, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);

            EntitySpellEffect blizzard = new EntitySpellEffect(world);
            blizzard.setPosition(x, y, z);
            blizzard.setBlizzard();
            blizzard.setRadius(radius);
            blizzard.setTicksToExist(duration);
            blizzard.setDamageBonus((float) damage);
            blizzard.SetCasterAndStack(caster, spell);
            world.spawnEntity(blizzard);
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.DAMAGE, SpellModifiers.DURATION, SpellModifiers.COLOR);
    }


    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        return spawnBlizzard(spell, world, caster, caster, impactX, impactY, impactZ);
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return spawnBlizzard(spell, world, caster, target, target.posX, target.posY, target.posZ);
    }

    @Override
    public float manaCost() {
        return 1200;
    }



    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {

    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ice);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.1f;
    }
}
