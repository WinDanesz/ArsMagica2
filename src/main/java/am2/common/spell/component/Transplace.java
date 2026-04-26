package am2.common.spell.component;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleArcToPoint;
import am2.common.blocks.tileentity.TileEntityOtherworldAura;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Transplace extends SpellComponent implements IRitualInteraction {

    @Override
    public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster) {
        Block block = world.getBlockState(blockPos).getBlock();
        if (!world.isRemote && caster instanceof EntityPlayer && block == AMBlocks.inert_spawner) {
            if (RitualShapeHelper.instance.matchesRitual(this, world, blockPos)) {
                RitualShapeHelper.instance.consumeReagents(this, world, blockPos);
                RitualShapeHelper.instance.consumeShape(this, world, blockPos);
                world.setBlockState(blockPos, AMBlocks.otherworld_aura.getDefaultState());
                TileEntity te = world.getTileEntity(blockPos);
                if (te != null && te instanceof TileEntityOtherworldAura) {
                    ((TileEntityOtherworldAura) te).setPlacedByUsername(((EntityPlayer) caster).getName());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target) {
        if (!world.isRemote && target != null && !target.isDead) {
            double tPosX = target.posX;
            double tPosY = target.posY;
            double tPosZ = target.posZ;

            double cPosX = caster.posX;
            double cPosY = caster.posY;
            double cPosZ = caster.posZ;

            caster.setPositionAndUpdate(tPosX, tPosY, tPosZ);
            if (target instanceof EntityLiving)
                ((EntityLiving) target).setPositionAndUpdate(cPosX, cPosY, cPosZ);
            else
                target.setPosition(cPosX, cPosY, cPosZ);

        }
        if (target instanceof EntityLiving)
            ((EntityLiving) target).faceEntity(caster, 180f, 180f);
        return true;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.noneOf(SpellModifiers.class);
    }

    @Override
    public float manaCost() {
        return 100;
    }


    @Override
    public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier) {
        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                particle.AddParticleController(new ParticleArcToPoint(particle, 1, target.posX, target.posY + target.getEyeHeight(), target.posZ, false).SetSpeed(0.05f).generateControlPoints());
                particle.setMaxAge(40);
                particle.setParticleScale(0.2f);
                particle.setRGBColorF(1, 0, 0);
                if (colorModifier > -1) {
                    particle.setRGBColorI(colorModifier);
                }
            }
        }

        for (int i = 0; i < 15; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", target.posX, target.posY + target.getEyeHeight(), target.posZ);
            if (particle != null) {
                particle.addRandomOffset(1, 1, 1);
                particle.AddParticleController(new ParticleArcToPoint(particle, 1, caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ, false).SetSpeed(0.05f).generateControlPoints());
                particle.setMaxAge(40);
                particle.setParticleScale(0.2f);
                particle.setRGBColorF(0, 0, 1);
                if (colorModifier > -1) {
                    particle.setRGBColorI(~colorModifier);
                }
            }
        }
    }

    @Override
    public Set<Affinity> getAffinity() {
        return Sets.newHashSet(Affinities.ender);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.rune, 1, EnumDyeColor.RED.getDyeDamage()),
                Items.COMPASS,
                new ItemStack(AMItems.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
                Items.ENDER_PEARL
        };
    }

    @Override
    public float getAffinityShift(Affinity affinity) {
        return 0.02f;
    }

    @Override
    public IMultiblock getRitualShape() {
        return RitualShapeHelper.instance.ringedCross;
    }

    @Override
    public ItemStack[] getRitualReagents() {
        return new ItemStack[]{
                new ItemStack(AMItems.purified_vinteum_dust),
                new ItemStack(AMItems.mage_robe),
                new ItemStack(AMItems.mage_boots),
                new ItemStack(AMItems.mage_hood),
                new ItemStack(AMItems.mage_leggings),
                new ItemStack(AMItems.player_focus)
        };
    }
    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getResult() {
        return new ItemStack(AMBlocks.otherworld_aura);
    }
}
