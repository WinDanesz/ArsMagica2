package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.power.IPowerNode;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.common.entity.EntityDarkling;
import am2.common.entity.EntityFireElemental;
import am2.common.power.PowerNodeRegistry;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class FireDamage extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos pos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        Block block = world.getBlockState(pos).getBlock();

        if (block == AMBlocks.obelisk) {
            if (RitualShapeHelper.instance.matchesRitual(this, world, pos)) {
                if (!world.isRemote) {
                    RitualShapeHelper.instance.consumeReagents(this, world, pos);
                    RitualShapeHelper.instance.consumeShape(this, world, pos);
                    world.setBlockState(pos, AMBlocks.black_aurem.getDefaultState());
                    PowerNodeRegistry.For(world).registerPowerNode((IPowerNode<?>) world.getTileEntity(pos));
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
        double damage = spell.getModifiedValue(ArsMagica.config.getFireDamageBase(), SpellModifiers.DAMAGE, Operation.ADD, world, caster, target);
        if (isNetherMob(target))
            return true;
        boolean result = SpellUtils.attackTargetSpecial(spell, target, DamageSources.causeFireDamage(caster), SpellUtils.modifyDamage(caster, (float) damage));
        return result;
    }

    private boolean isNetherMob(Entity target) {
        return target instanceof EntityPigZombie ||
                target instanceof EntityDarkling ||
                target instanceof EntityFireElemental ||
                target instanceof EntityGhast;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.DAMAGE);
    }


    @Override
    public float manaCost() {
        return 120;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 5; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "explosion_2", x, y, z);
            if (particle != null) {
                particle.addRandomOffset(1, 0.5, 1);
                particle.addVelocity(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2, rand.nextDouble() * 0.2 - 0.1);
                particle.setAffectedByGravity();
                particle.setDontRequireControllers();
                particle.setMaxAge(5);
                particle.setParticleScale(0.1f);
                if (colorModifier > -1) {
                    particle.setRGBColorI(colorModifier);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.fire);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.RED.getDyeDamage()),
                Items.FLINT_AND_STEEL,
                new ItemStack(AMItems.vinteum_dust),
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
                new ItemStack(AMItems.sunstone)
        };
    }
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getResult() {
        return new ItemStack(AMBlocks.black_aurem);
    }
}
