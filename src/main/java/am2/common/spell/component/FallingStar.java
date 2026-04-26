package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.entity.EntityThrownRock;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class FallingStar extends SpellComponent {

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AffinityShiftUtils.getEssenceForAffinity(Affinities.arcane),
                new ItemStack(AMItems.arcane_ash),
                AffinityShiftUtils.getEssenceForAffinity(Affinities.arcane),
                AMBlocks.mana_battery,
                Items.LAVA_BUCKET
        };
    }

    private boolean spawnStar(SpellData spell, EntityLivingBase caster, Entity target, World world, double x, double y, double z) {

        List<EntityThrownRock> rocks = world.getEntitiesWithinAABB(EntityThrownRock.class, new AxisAlignedBB(x - 10, y - 10, z - 10, x + 10, y + 10, z + 10));

        float damageMultitplier = (float) spell.getModifiedValue(ArsMagica.config.getFallingStarDamageMultiplier(), SpellModifiers.DAMAGE, Operation.MULTIPLY, world, caster, target);
        for (EntityThrownRock rock : rocks) {
            if (rock.getIsShootingStar())
                return false;
        }

        if (!world.isRemote) {
            EntityThrownRock star = new EntityThrownRock(world);
            star.setPosition(x, world.getActualHeight(), z);
            star.setShootingStar(2 * damageMultitplier);
            star.setThrowingEntity(caster);
            star.setSpell(spell.copy());
            world.spawnEntity(star);
        }
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE, SpellModifiers.COLOR);
    }


    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        return spawnStar(spell, caster, caster, world, impactX, impactY + 50, impactZ);
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        return spawnStar(spell, caster, target, world, target.posX, target.posY + 50, target.posZ);
    }

    @Override
    public float manaCost() {
        return 400;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.arcane);
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.05f;
    }

}
