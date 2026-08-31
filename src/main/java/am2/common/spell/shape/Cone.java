package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.AMParticle;
import am2.client.particles.AMParticleDefs;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import am2.common.utils.MathUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class Cone extends SpellShape {

    // Cone half-angle in degrees (total cone width = coneAngle * 2)
    private int getConeAngle() { return ArsMagica.config.getConeAngle(); }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        boolean shouldApplyEffectBlock = useCount % ArsMagica.config.getBeamBlockTickRate() == 0;
        boolean shouldApplyEffectEntity = useCount % ArsMagica.config.getBeamEntityTickRate() == 0;

        double range = spell.getModifiedValue(SpellModifiers.RANGE, Operation.ADD, world, caster, target);
        boolean targetWater = spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS);

        SpellCastResult result = SpellCastResult.SUCCESS;
        Set<Entity> hitEntities = new HashSet<>();
        Set<BlockPos> hitBlocks = new HashSet<>();

        // Get entities in cone
        Entity[] entitiesInCone = MathUtilities.GetEntitiesInAngleNearEntity(world, caster, getConeAngle(), (int) range, Entity.class, false);

        // Apply effects to entities
        if (shouldApplyEffectEntity && !world.isRemote) {
            for (Entity entity : entitiesInCone) {
                if (!hitEntities.contains(entity)) {
                    SpellCastResult entityResult = spell.applyComponentsToEntity(world, caster, entity);
                    if (entityResult == SpellCastResult.SUCCESS) {
                        hitEntities.add(entity);
                    }
                }
            }
        }

        // Cast rays in a cone pattern to find blocks
        if (shouldApplyEffectBlock && !world.isRemote) {
            // Sample rays across the cone
            int numHorizontalRays = 5;
            int numVerticalRays = 5;

            for (int h = 0; h < numHorizontalRays; h++) {
                for (int v = 0; v < numVerticalRays; v++) {
                    // Calculate angle offsets
                    float horizontalOffset = numHorizontalRays > 1 ? ((float) h / (numHorizontalRays - 1) - 0.5f) * getConeAngle() * 2 : 0;
                    float verticalOffset = numVerticalRays > 1 ? ((float) v / (numVerticalRays - 1) - 0.5f) * getConeAngle() * 2 : 0;

                    // Perform raycast with offset
                    RayTraceResult mop = performOffsetRaytrace(spell, caster, world, range, targetWater, horizontalOffset, verticalOffset);

                    if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                        BlockPos blockPos = mop.getBlockPos();
                        if (!hitBlocks.contains(blockPos)) {
                            SpellCastResult blockResult = spell.copy().applyComponentsToGround(world, caster, blockPos, mop.sideHit, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
                            if (blockResult == SpellCastResult.SUCCESS) {
                                hitBlocks.add(blockPos);
                            }
                        }
                    }
                }
            }
        }

        // Spawn particles on client side
        if (world.isRemote) {
            spawnConeParticles(spell, caster, world, range);
        }

        return result;
    }

    private RayTraceResult performOffsetRaytrace(SpellData spell, EntityLivingBase caster, World world, double range, boolean targetWater, float horizontalOffset, float verticalOffset) {
        // Get the caster's look vector
        Vec3d startVec = new Vec3d(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);

        // Apply offsets to the look direction
        float yaw = caster.rotationYaw + horizontalOffset;
        float pitch = caster.rotationPitch + verticalOffset;

        // Convert to radians and calculate direction vector using same formula as MathUtilities.getLook
        float yawRad = -yaw * 0.017453292F - (float) Math.PI;
        float pitchRad = -pitch * 0.017453292F;

        float xzLen = -MathHelper.cos(pitchRad);
        float vx = MathHelper.sin(yawRad) * xzLen;
        float vy = MathHelper.sin(pitchRad);
        float vz = MathHelper.cos(yawRad) * xzLen;

        Vec3d lookVec = new Vec3d(vx, vy, vz);
        Vec3d endVec = startVec.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);

        return world.rayTraceBlocks(startVec, endVec, targetWater, !targetWater, false);
    }

    private void spawnConeParticles(SpellData spell, EntityLivingBase caster, World world, double range) {
        Vec3d lookVec = MathUtilities.getLook(caster, 1.0f);
        double startX = caster.posX + lookVec.x * 0.5;
        double startY = caster.posY + caster.getEyeHeight() - 0.2 + lookVec.y * 0.5;
        double startZ = caster.posZ + lookVec.z * 0.5;
        Affinity affinity = spell.getMainShift();
        int color = spell.getColor(world, caster, null);
        String particleType = AMParticleDefs.getParticleForAffinity(affinity);

        // Spawn particles near the caster and give them outward motion
        int particleCount = ArsMagica.config.getGFXLevel() * 3 + 3;
        float speed = (float) (range * 0.12);

        for (int i = 0; i < particleCount; i++) {
            // Random angular offset within cone
            float horizontalAngle = (world.rand.nextFloat() - 0.5f) * getConeAngle() * 2;
            float verticalAngle = (world.rand.nextFloat() - 0.5f) * getConeAngle() * 2;

            // Small random offset from start position for spread
            double px = startX + (world.rand.nextFloat() - 0.5f) * 0.3;
            double py = startY + (world.rand.nextFloat() - 0.5f) * 0.3;
            double pz = startZ + (world.rand.nextFloat() - 0.5f) * 0.3;

            float particleYaw = MathHelper.wrapDegrees(caster.rotationYaw + 90 + horizontalAngle);
            float particlePitch = MathHelper.wrapDegrees(caster.rotationPitch + verticalAngle);
            float particleSpeed = speed + world.rand.nextFloat() * speed * 0.3f;

            if (affinity == Affinities.ice && EBWizardryCompatBootstrap.isActive) {
                double yawRad = Math.toRadians(particleYaw);
                double pitchRad = Math.toRadians(particlePitch);
                double vx = Math.cos(yawRad) * particleSpeed;
                double vz = Math.sin(yawRad) * particleSpeed;
                double vy = -Math.sin(pitchRad) * particleSpeed;
                EBWizardryCompatBootstrap.spawnDirectedFrostParticle(world, px, py, pz, vx, vy, vz);
            } else {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, particleType, px, py, pz);
                if (particle != null) {
                    particle.setMaxAge(8 + world.rand.nextInt(5));
                    particle.setParticleScale(0.1f + world.rand.nextFloat() * 0.1f);
                    particle.setIgnoreMaxAge(false);
                    if (color != -1) {
                        particle.setRGBColorI(color);
                    }
                    // Move outward in the cone direction (yaw + 90 matches codebase convention for ParticleMoveOnHeading)
                    particle.AddParticleController(new ParticleMoveOnHeading(particle, particleYaw, particlePitch, particleSpeed, 1, false));
                    particle.AddParticleController(new ParticleFadeOut(particle, 1, false).setFadeSpeed(0.1f).setKillParticleOnFinish(true));
                }
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
                new ItemStack(AMItems.arcane_compound),
                new ItemStack(AMItems.arcane_compound),
                new ItemStack(AMItems.purified_vinteum_dust),
                AMBlocks.aum,
                "E:" + PowerTypes.NEUTRAL.ID(), 750
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 0.4f; // Higher than beam (0.2f) since it hits multiple targets
    }

    @Override
    public float damageMultiplier() {
        return 0.6f; // Reduced damage per target since it hits multiple entities
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
