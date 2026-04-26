package am2.common.spell.shape;

import am2.ArsMagica;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.entity.EntitySpellPuddle;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCastResult;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.EnumSet;

/**
 * Puddle spell shape – creates a persistent, flat area-of-effect entity on the ground.
 * <p>
 * The caster aims at the ground (up to RANGE blocks away); the puddle appears where the
 * spell hits a solid surface and applies its components to all entities that walk through it.
 * If no block surface is found the puddle is placed at the caster's feet.
 * <p>
 * Modifiers: RADIUS, DURATION, COLOR
 */
public class Puddle extends SpellShape {

    /** @deprecated Use {@link am2.common.config.AMConfig#getPuddleDefaultRadius()} */
    private static int getDefaultRadius() { return ArsMagica.config.getPuddleDefaultRadius(); }
    /** @deprecated Use {@link am2.common.config.AMConfig#getPuddleDefaultDuration()} */
    private static int getDefaultDuration() { return ArsMagica.config.getPuddleDefaultDuration(); }
    /** @deprecated Use {@link am2.common.config.AMConfig#getPuddleDefaultRange()} */
    private static float getDefaultRange() { return ArsMagica.config.getPuddleDefaultRange(); }

    @Override
    public SpellCastResult beginStackStage(SpellData spell, EntityLivingBase caster,
                                           EntityLivingBase target, World world,
                                           double x, double y, double z,
                                           EnumFacing side, boolean giveXP, int useCount) {
        if (world.isRemote) return SpellCastResult.SUCCESS;

        int    radius   = (int) spell.getModifiedValue(getDefaultRadius(),   SpellModifiers.RADIUS,   Operation.ADD, world, caster, target);
        int    duration = (int) spell.getModifiedValue(getDefaultDuration(),  SpellModifiers.DURATION, Operation.ADD, world, caster, target);
        boolean targetWater = spell.isModifierPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS);

        // Decide spawn position.
        double puddleX = caster.posX;
        double puddleY = caster.posY;
        double puddleZ = caster.posZ;

        if (side != null) {
            // Called from a projectile (or other shape) that already resolved the hit block.
            // x, y, z are the integer block coordinates of the struck block; side is the hit face.
            // Use them directly so the puddle appears exactly where the projectile landed.
            if (side == EnumFacing.UP) {
                // Projectile hit the top surface of a floor block – ideal placement.
                puddleX = x + 0.5;
                puddleY = y + 1.0;
                puddleZ = z + 0.5;
            } else {
                // Hit a wall or ceiling.  Step into the adjacent air cell, then scan down to
                // the nearest floor so the puddle lands on the ground next to the wall.
                double adjX = x + 0.5 + side.getXOffset() * 0.5;
                double adjZ = z + 0.5 + side.getZOffset() * 0.5;
                puddleX = adjX;
                puddleZ = adjZ;
                puddleY = y;  // fallback if no floor found
                for (int i = 0; i <= 12; i++) {
                    BlockPos floor = new BlockPos(adjX, y - i, adjZ);
                    if (!world.isAirBlock(floor) && world.getBlockState(floor).isFullBlock()) {
                        puddleY = floor.getY() + 1.0;
                        break;
                    }
                }
            }
        } else {
            // Direct cast (no prior shape provided coordinates) – raytrace from the caster.
            RayTraceResult mop = spell.raytrace(caster, world, getDefaultRange(), false, targetWater);
            boolean placedByRaytrace = false;
            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK && mop.sideHit == EnumFacing.UP) {
                // Ray hit the top face of a floor block – ideal placement.
                BlockPos pos = mop.getBlockPos();
                puddleX = pos.getX() + 0.5;
                puddleY = pos.getY() + 1.0;
                puddleZ = pos.getZ() + 0.5;
                placedByRaytrace = true;
            }

            if (!placedByRaytrace) {
                // Raycast missed or hit a non-floor surface.
                // Project horizontally in the caster's look direction (ignore pitch so the
                // forward distance is always consistent regardless of vertical aim).
                net.minecraft.util.math.Vec3d look = caster.getLookVec();
                double horizLen = Math.sqrt(look.x * look.x + look.z * look.z);
                double forwardDist = 5.0;
                double projX, projZ;
                if (horizLen > 1e-4) {
                    projX = caster.posX + (look.x / horizLen) * forwardDist;
                    projZ = caster.posZ + (look.z / horizLen) * forwardDist;
                } else {
                    projX = caster.posX;
                    projZ = caster.posZ;
                }

                double scanStartY = caster.posY + caster.getEyeHeight() + 1.0;
                boolean foundGround = false;
                for (int i = 0; i <= 12; i++) {
                    BlockPos floor = new BlockPos(projX, scanStartY - i, projZ);
                    if (!world.isAirBlock(floor) && world.getBlockState(floor).isFullBlock()) {
                        puddleX = projX;
                        puddleY = floor.getY() + 1.0;
                        puddleZ = projZ;
                        foundGround = true;
                        break;
                    }
                }

                if (!foundGround) {
                    for (int i = 0; i <= 4; i++) {
                        BlockPos floor = new BlockPos(caster.posX, caster.posY - i, caster.posZ);
                        if (!world.isAirBlock(floor) && world.getBlockState(floor).isFullBlock()) {
                            puddleY = floor.getY() + 1.0;
                            break;
                        }
                    }
                }
            }
        }

        EntitySpellPuddle puddle = new EntitySpellPuddle(world);
        puddle.setPosition(puddleX, puddleY, puddleZ);
        puddle.setRadius(radius);
        puddle.setTicksToExist(duration);
        puddle.setCasterAndStack(caster, spell);
        world.spawnEntity(puddle);

        return SpellCastResult.SUCCESS;
    }



    @Override
    public EnumSet<SpellModifiers> getModifiers() {
        return EnumSet.of(SpellModifiers.RADIUS, SpellModifiers.DURATION,
                          SpellModifiers.COLOR, SpellModifiers.TARGET_NONSOLID_BLOCKS);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                new ItemStack(AMItems.vinteum_dust),
                new ItemStack(AMItems.blue_topaz),
                new ItemStack(AMItems.moonstone),
                AMBlocks.tarma_root,
                "E:" + PowerTypes.NEUTRAL.ID(), 250
        };
    }

    @Override
    public float manaCostMultiplier() {
        return 3.0f;
    }

    @Override
    public boolean isTerminusShape() {
        return false;
    }

    @Override
    public boolean isPrincipumShape() {
        return true;
    }
}
