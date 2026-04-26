package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.AMBeam;
import am2.client.particles.AMParticle;
import am2.client.particles.AMParticleDefs;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import am2.common.utils.MathUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.EnumSet;
import java.util.HashMap;

public class Beam extends SpellShape {

    private final HashMap<Integer, AMBeam> beams;

    public Beam() {
        beams = new HashMap<>();
    }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        boolean shouldApplyEffectBlock = useCount % ArsMagica.config.getBeamBlockTickRate() == 0;
        boolean shouldApplyEffectEntity = useCount % ArsMagica.config.getBeamEntityTickRate() == 0;

        double range = spell.getModifiedValue(SpellModifiers.RANGE, Operation.ADD, world, caster, target);
        boolean targetWater = spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS);
        RayTraceResult mop = spell.raytrace(caster, world, range, true, targetWater);

        SpellCastResult result = null;
        Vec3d beamHitVec = null;
        Vec3d spellVec = null;

        if (mop == null) {
            beamHitVec = MathUtilities.extrapolateEntityLook(world, caster, range);
            spellVec = beamHitVec;
        } else if (mop.typeOfHit == RayTraceResult.Type.ENTITY) {
            if (shouldApplyEffectEntity) {
                Entity e = mop.entityHit;
                result = spell.applyComponentsToEntity(world, caster, e);
                if (result != SpellCastResult.SUCCESS) {
                    return result;
                }
            }
            float rng = (float) mop.hitVec.distanceTo(new Vec3d(caster.posX, caster.posY, caster.posZ));
            beamHitVec = MathUtilities.extrapolateEntityLook(world, caster, rng);
            spellVec = beamHitVec;
        } else {
            if (shouldApplyEffectBlock) {
                result = spell.applyComponentsToGround(world, caster, mop.getBlockPos(), mop.sideHit, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                if (result != SpellCastResult.SUCCESS) {
                    return result;
                }
            }
            beamHitVec = mop.hitVec;
            spellVec = new Vec3d(mop.getBlockPos());
        }

        if (world.isRemote && beamHitVec != null) {
            spawnBeamParticles(spell, caster, target, world, beamHitVec);
        }

        if (result != null && spellVec != null) {
            boolean shouldExecute = mop.typeOfHit == RayTraceResult.Type.ENTITY ? shouldApplyEffectEntity : shouldApplyEffectBlock;
            if (shouldExecute) {
                return spell.execute(world, caster, target, spellVec.x, spellVec.y, spellVec.z, mop.sideHit);
            }
        }
        return SpellCastResult.SUCCESS_REDUCE_MANA;
    }

    @SideOnly(Side.CLIENT)
    private void spawnBeamParticles(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, Vec3d beamHitVec) {
        AMBeam beam = beams.get(caster.getEntityId());
        Affinity affinity = spell.getMainShift();
        int color = spell.getColor(world, caster, target);

        if (beam != null) {
            if (!beam.isAlive() || caster.getDistanceSq(beam.getPosX(), beam.getPosY(), beam.getPosZ()) > 4) {
                beams.remove(caster.getEntityId());
            } else {
                double startX = caster.posX;
                double startY = caster.posY + caster.getEyeHeight() - 0.2f;
                double startZ = caster.posZ;
                beam.setBeamLocationAndTarget(startX, startY, startZ, beamHitVec.x, beamHitVec.y, beamHitVec.z);
            }
        } else {
            if (affinity.equals(Affinities.lightning)) {
                ArsMagica.proxy.particleManager.BoltFromEntityToPoint(world, caster, beamHitVec.x, beamHitVec.y, beamHitVec.z, 1, color == -1 ? affinity.getColor() : color);
            } else {
                beam = (AMBeam) ArsMagica.proxy.particleManager.BeamFromEntityToPoint(world, caster, beamHitVec.x, beamHitVec.y, beamHitVec.z, color == -1 ? affinity.getColor() : color);
                if (beam != null) {
                    if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
                        beam.setFirstPersonPlayerCast();
                    beams.put(caster.getEntityId(), beam);
                }
            }
        }

        for (int i = 0; i < ArsMagica.config.getGFXLevel() + 1; ++i) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(affinity), beamHitVec.x, beamHitVec.y, beamHitVec.z);
            if (particle != null) {
                particle.setMaxAge(2);
                particle.setParticleScale(0.1f);
                particle.setIgnoreMaxAge(false);
                if (color != -1)
                    particle.setRGBColorI(color);
                particle.AddParticleController(new ParticleMoveOnHeading(particle, world.rand.nextDouble() * 360, world.rand.nextDouble() * 360, world.rand.nextDouble() * 0.2 + 0.02f, 1, false));
            }
        }
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RANGE, SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }

    @Override
    public boolean isChanneled() {
        return true;
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                AMItems.standard_focus,
                new ItemStack(AMItems.blue_topaz),
                new ItemStack(AMItems.blue_topaz),
                new ItemStack(AMItems.purified_vinteum_dust),
                AMBlocks.aum,
                "E:" + PowerTypes.NEUTRAL.ID(), 500
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 0.2f;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return false;
    }

    @Override
    public SoundEvent getSoundForAffinity(Affinity affinity, SpellData stack, World world) {
        return AMSounds.LOOP_MAP.get(affinity);
    }
}
