package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.client.particles.*;
import am2.common.entity.EntitySpellProjectile;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.List;

public class AoE extends SpellShape {

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, EnumFacing side, boolean giveXP, int useCount) {
        double radius = spell.getModifiedValue(1, SpellModifiers.RADIUS, Operation.ADD, world, caster, target); // SpellUtils.getModifiedDouble_Add(1, stack, caster, target, world, SpellModifiers.RADIUS);
        // Extend bounding box upward when hitting top of blocks to catch entities standing on them
        double yMin = y - radius;
        double yMax = y + radius;
        if (side == EnumFacing.UP) {
            yMax = y + radius + 2; // Add extra height to catch entities standing on top
        }
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(x - radius, yMin, z - radius, x + radius, yMax, z + radius));

        boolean appliedToAtLeastOneEntity = false;

        for (Entity e : entities) {
            // Skip projectiles always
            if (e instanceof EntitySpellProjectile) continue;
            // Skip caster UNLESS the target of this spell stage is the caster (e.g., from Self shape)
            if (e == caster && target != caster) continue;
            if (e instanceof EntityDragon && ((EntityDragon) e) instanceof EntityLivingBase)
                e = (EntityLivingBase) ((EntityDragon) e);
            if (spell.copy().applyComponentsToEntity(world, caster, e) == SpellCastResult.SUCCESS)
                appliedToAtLeastOneEntity = true;
        }

        BlockPos pos = new BlockPos(x, y, z);

        if (side != null) {
            switch (side) {
                case UP:
                case DOWN:
                    spawnAoEParticles(spell, caster, world, x + 0.5f, y + ((side.equals(EnumFacing.UP)) ? 0.5f : (target != null ? target.getEyeHeight() : -2.0f)), z + 0.5f, (int) radius);
                    int gravityMagnitude = spell.getModifierCount(SpellModifiers.GRAVITY);
                    return applyStageBlocks(spell, caster, world, pos, side, (int) Math.floor(radius), gravityMagnitude, COORD_HORIZONTAL);
                case NORTH:
                case SOUTH:
                    spawnAoEParticles(spell, caster, world, x + 0.5f, y - 1, z + 0.5f, (int) radius);
                    return applyStageBlocks(spell, caster, world, pos, side, (int) Math.floor(radius), 0, COORD_VERTICAL_Z);
                case EAST:
                case WEST:
                    spawnAoEParticles(spell, caster, world, x + 0.5f, y - 1, z + 0.5f, (int) radius);
                    return applyStageBlocks(spell, caster, world, pos, side, (int) Math.floor(radius), 0, COORD_VERTICAL_X);
            }
        } else {
            spawnAoEParticles(spell, caster, world, x, y - 1, z, (int) radius);
            int gravityMagnitude = spell.getModifierCount(SpellModifiers.GRAVITY);
            return applyStageBlocks(spell, caster, world, pos, EnumFacing.UP, (int) Math.floor(radius), gravityMagnitude, COORD_HORIZONTAL);
        }

        if (appliedToAtLeastOneEntity) {
            spawnAoEParticles(spell, caster, world, x, y + 1, z, (int) radius);
            return SpellCastResult.SUCCESS;
        }

        return SpellCastResult.EFFECT_FAILED;
    }

    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.GRAVITY);
    }


    private void spawnAoEParticles(SpellData stack, EntityLivingBase caster, World world, double x, double y, double z, int radius) {
        if (!world.isRemote) return; // Only spawn particles on client side

        String pfxName = AMParticleDefs.getParticleForAffinity(stack.getMainShift());
        float speed = 0.08f * radius;

        int color = stack.getColor(world, caster, null) & 0xFFFFFF;

        int angleStep = ArsMagica.config.FullGFX() ? 20 : ArsMagica.config.LowGFX() ? 40 : 60;
        // Create spherical particle explosion with both horizontal and vertical components
        for (int i = 0; i < 360; i += angleStep) {
            // Add particles at multiple vertical angles to create a sphere
            for (int pitch = -60; pitch <= 60; pitch += angleStep) {
                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, pfxName, x, y + 1.5f, z);
                if (effect != null) {
                    effect.setIgnoreMaxAge(true);
                    effect.AddParticleController(new ParticleMoveOnHeading(effect, i, pitch, speed, 1, false));
                    effect.setRGBColorI(color);
                    effect.AddParticleController(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.05f).setKillParticleOnFinish(true));
                    effect.AddParticleController(
                            new ParticleLeaveParticleTrail(effect, pfxName, false, 5, 1, false)
                                    .addControllerToParticleList(new ParticleFadeOut(effect, 1, false).setFadeSpeed(0.1f).setKillParticleOnFinish(true))
                                    .setParticleRGB_I(color)
                                    .addRandomOffset(0.2f, 0.2f, 0.2f)
                    );
                }
            }
        }
    }

    private static final int[] COORD_HORIZONTAL = {0, 2, 1};  // i, k, j
    private static final int[] COORD_VERTICAL_X = {2, 1, 0};  // k, j, i
    private static final int[] COORD_VERTICAL_Z = {0, 1, 2};  // i, j, k

    private SpellCastResult applyStageBlocks(SpellData stack, EntityLivingBase caster, World world, BlockPos pos, EnumFacing face, int radius, int gravityMagnitude, int[] coordOrder) {
        for (int i = -radius; i <= radius; ++i) {
            for (int j = -radius; j <= radius; ++j) {
                for (int k = -radius; k <= radius; ++k) {
                    int[] ijk = {i, j, k};
                    BlockPos lookPos = pos.add(ijk[coordOrder[0]], ijk[coordOrder[1]], ijk[coordOrder[2]]);
                    if (gravityMagnitude > 0) {
                        int searchDist = 0;
                        while (world.isAirBlock(lookPos) && searchDist < gravityMagnitude) {
                            lookPos = lookPos.down();
                            searchDist++;
                        }
                    }
                    if (world.isAirBlock(lookPos)) continue;
                    SpellCastResult result = stack.copy().applyComponentsToGround(world, caster, lookPos, face, lookPos.getX(), lookPos.getY(), lookPos.getZ());
                    if (result != SpellCastResult.SUCCESS)
                        return result;
                }
            }
        }
        return SpellCastResult.SUCCESS;
    }



    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.moonstone),
                AffinityShiftUtils.getEssenceForAffinity(Affinities.air),
                String.format("E:%d|%d|%d", PowerTypes.LIGHT.ID(), PowerTypes.NEUTRAL.ID(), PowerTypes.DARK.ID()), 1000,
                Blocks.TNT
        };
    }

    @Override
    public float manaCostMultiplier() {
        //FIXME
//		int multiplier = 2;
//		int radiusMods = 0;
//		int stages = SpellUtils.numStages(spellStack);
//		for (int i = SpellUtils.currentStage(spellStack); i < stages; ++i){
//			if (!SpellUtils.getShapeForStage(spellStack, i).equals(this)) continue;
//
//			ArrayList<SpellModifier> mods = SpellUtils.getModifiersForStage(spellStack, i);
//			for (SpellModifier modifier : mods){
//				if (modifier.getAspectsModified().contains(SpellModifiers.RADIUS)){
//					radiusMods++;
//				}
//			}
//		}
//		return multiplier * (radiusMods + 1);
        return 2F;
    }

    @Override
    public boolean isTerminusShape() {
        return true;
    }

    @Override
    public boolean isPrincipumShape() {
        return false;
    }
}
