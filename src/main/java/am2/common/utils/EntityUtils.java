package am2.common.utils;

import am2.ArsMagica;
import am2.api.math.AMVector3;
import am2.common.blocks.tileentity.TileEntitySummoner;
import am2.common.entity.ai.EntityAIGuardSpawnLocation;
import am2.common.entity.ai.EntityAISummonFollowOwner;
import am2.common.entity.ai.selectors.SummonEntitySelector;
import am2.common.extensions.EntityExtension;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EntityUtils {

    private static final HashMap<Integer, ArrayList<EntityAITasks.EntityAITaskEntry>> storedTasks = new HashMap<>();
    private static final HashMap<Integer, ArrayList<EntityAITasks.EntityAITaskEntry>> storedAITasks = new HashMap<>();
    private static final String isSummonKey = "AM2_Entity_Is_Made_Summon";

    private static final String summonDurationKey = "AM2_Summon_Duration";
    private static final String summonOwnerKey = "AM2_Summon_Owner";
    private static Method ptrSetSize = null;
    private static final String summonTileXKey = "AM2_Summon_Tile_X";
    private static final String summonTileYKey = "AM2_Summon_Tile_Y";
    private static final String summonTileZKey = "AM2_Summon_Tile_Z";


    /**
     * Checks whether a player has completed the given advancement (server-side only).
     * Returns false when called on the client or when the advancement does not exist.
     */
    public static boolean playerHasAdvancement(EntityPlayer player, ResourceLocation advancementId) {
        if (player.world.isRemote || !(player instanceof EntityPlayerMP)) return false;
        EntityPlayerMP playerMP = (EntityPlayerMP) player;
        Advancement advancement = playerMP.getServer().getAdvancementManager().getAdvancement(advancementId);
        return advancement != null && playerMP.getAdvancements().getProgress(advancement).isDone();
    }

    /**
     * Converts a cumulative XP value into the corresponding magic level.
     * Iterates through each level's XP cap, subtracting until the remainder is negative.
     *
     * @param totalXP the total accumulated XP points
     * @return the level that corresponds to the given XP amount
     */
    public static int getLevelFromXP(float totalXP) {
        int level = 0;
        int xp = (int) Math.floor(totalXP);
        int cap;
        while ((cap = xpBarCap(level)) <= xp) {
            xp -= cap;
            level++;
        }
        return level;
    }

    /**
     * Performs a ray trace from the caster's eye position to find the nearest entity
     * along the look vector, within the given range.
     *
     * @param world         the current world
     * @param entityplayer  the entity doing the looking (typically the caster)
     * @param range         maximum reach distance in blocks
     * @param collideRadius additional expansion radius around the ray for entity hit-testing
     * @param nonCollide    if {@code true}, entities that cannot normally be collided with are still considered
     * @param targetWater   if {@code true}, the ray can pass through water; otherwise it stops at water
     * @return the closest entity struck by the ray, or {@code null} if none was found
     */
    public static Entity getPointedEntity(World world, EntityLivingBase entityplayer, double range, double collideRadius, boolean nonCollide, boolean targetWater) {
        Entity pointedEntity = null;
        Vec3d vec3d = new Vec3d(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ);
        Vec3d vec3d1 = entityplayer.getLookVec();
        Vec3d vec3d2 = vec3d.add(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range);
        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entityplayer, entityplayer.getEntityBoundingBox().grow(vec3d1.x * range, vec3d1.y * range, vec3d1.z * range).expand(collideRadius, collideRadius, collideRadius));

        double d2 = 0.0D;
        for (Entity entity : list) {
            RayTraceResult mop = world.rayTraceBlocks(
                    new Vec3d(entityplayer.posX, entityplayer.posY + entityplayer.getEyeHeight(), entityplayer.posZ),
                    new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
                    targetWater, !targetWater, false);
            if (((entity.canBeCollidedWith()) || (nonCollide)) && mop == null) {
                float f2 = Math.max(0.8F, entity.getCollisionBorderSize());
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(f2, f2, f2);
                RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(vec3d, vec3d2);
                if (axisalignedbb.contains(vec3d)) {
                    if ((0.0D < d2) || (d2 == 0.0D)) {
                        pointedEntity = entity;
                        d2 = 0.0D;
                    }

                } else if (movingobjectposition != null) {
                    double d3 = vec3d.distanceTo(movingobjectposition.hitVec);
                    if ((d3 < d2) || (d2 == 0.0D)) {
                        pointedEntity = entity;
                        d2 = d3;
                    }
                }
            }
        }
        return pointedEntity;
    }

    /**
     * Returns the total cumulative XP required to reach the given level from zero.
     *
     * @param level the target level
     * @return total XP needed to reach that level
     */
    public static int getXPFromLevel(int level) {
        int totalXP = 0;
        for (int i = 0; i < level; i++) {
            totalXP += xpBarCap(i);
        }
        return totalXP;
    }

    /**
     * Returns the amount of XP needed to advance from {@code experienceLevel} to the next level.
     * Mirrors Minecraft's vanilla XP curve (tiered at levels 15 and 30).
     *
     * @param experienceLevel the current level
     * @return XP required to complete that level's bar
     */
    public static int xpBarCap(int experienceLevel) {
        return experienceLevel >= 30 ? 112 + (experienceLevel - 30) * 9 : (experienceLevel >= 15 ? 37 + (experienceLevel - 15) * 5 : 7 + experienceLevel * 2);
    }

    /**
     * Returns {@code true} when the entity's AI is active (not disabled via NBT or game rules).
     *
     * @param ent the creature to check
     * @return {@code true} if AI is enabled
     */
    public static boolean isAIEnabled(EntityCreature ent) {
        return !ent.isAIDisabled();
    }

    /**
     * Converts an existing entity into a player-allied summon.
     * Replaces the entity's target AI so it attacks hostile mobs instead of the player,
     * optionally saves the original AI tasks for later restoration via {@link #revertAI},
     * and registers the entity with the owning player's summon list.
     *
     * @param entityliving   the entity to convert
     * @param player         the player who gains ownership of the summon
     * @param storeForRevert if {@code true}, the original AI tasks are saved so they can be
     *                       restored by calling {@link #revertAI}
     */
    public static void makeSummon_PlayerFaction(EntityCreature entityliving, EntityPlayer player, boolean storeForRevert) {
        if (isAIEnabled(entityliving) && EntityExtension.For(player).getCanHaveMoreSummons()) {
            if (storeForRevert)
                storedTasks.put(entityliving.getEntityId(), new ArrayList<EntityAITasks.EntityAITaskEntry>(entityliving.targetTasks.taskEntries));

            boolean addMeleeAttack = false;
            ArrayList<EntityAITaskEntry> toRemove = new ArrayList<EntityAITaskEntry>();
            for (Object task : entityliving.tasks.taskEntries) {
                EntityAITaskEntry base = (EntityAITaskEntry) task;
                if (base.action instanceof EntityAIAttackMelee) {
                    toRemove.add(base);
                    addMeleeAttack = true;
                }
            }

            entityliving.tasks.taskEntries.removeAll(toRemove);

            if (storeForRevert)
                storedAITasks.put(entityliving.getEntityId(), toRemove);

            if (addMeleeAttack) {
                float speed = entityliving.getAIMoveSpeed();
                if (speed <= 0) speed = 1.0f;
                entityliving.tasks.addTask(3, new EntityAIAttackMelee(entityliving, speed, true));
            }

            entityliving.targetTasks.taskEntries.clear();

            entityliving.targetTasks.addTask(1, new EntityAIHurtByTarget(entityliving, true));
            entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityMob>(entityliving, EntityMob.class, 0, true, false, SummonEntitySelector.instance));
            entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntitySlime>(entityliving, EntitySlime.class, true));
            entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityGhast>(entityliving, EntityGhast.class, true));
            entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityShulker>(entityliving, EntityShulker.class, true));

            if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null && entityliving.getAttackTarget() instanceof EntityPlayer)
                ArsMagica.proxy.addDeferredTargetSet(entityliving, null);

            if (entityliving instanceof EntityTameable) {
                ((EntityTameable) entityliving).setTamed(true);
                ((EntityTameable) entityliving).setOwnerId(player.getPersistentID());
            }

            entityliving.getEntityData().setBoolean(isSummonKey, true);
            EntityExtension.For(player).addSummon(entityliving);
        }
    }

    /**
     * Returns {@code true} if the entity was marked as a summon (either player- or monster-faction).
     *
     * @param entityliving the entity to test
     * @return {@code true} if the entity is currently acting as a summon
     */
    public static boolean isSummon(EntityLivingBase entityliving) {
        return entityliving.getEntityData().getBoolean(isSummonKey);
    }

    /**
     * Converts an existing entity into a monster-faction summon that attacks players.
     * Replaces the entity's target AI to prioritize {@link EntityPlayer} targets and optionally
     * saves the original tasks for later restoration via {@link #revertAI}.
     *
     * @param entityliving   the entity to convert
     * @param storeForRevert if {@code true}, the original target AI tasks are saved so they can be
     *                       restored by calling {@link #revertAI}
     */
    public static void makeSummon_MonsterFaction(EntityCreature entityliving, boolean storeForRevert) {
        if (isAIEnabled(entityliving)) {
            if (storeForRevert)
                storedTasks.put(entityliving.getEntityId(), new ArrayList<EntityAITasks.EntityAITaskEntry>(entityliving.targetTasks.taskEntries));
            entityliving.targetTasks.taskEntries.clear();
            entityliving.targetTasks.addTask(1, new EntityAIHurtByTarget(entityliving, true));
            entityliving.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityPlayer>(entityliving, EntityPlayer.class, true));
            if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null && entityliving.getAttackTarget() instanceof EntityMob)
                ArsMagica.proxy.addDeferredTargetSet(entityliving, null);

            entityliving.getEntityData().setBoolean(isSummonKey, true);
        }
    }

    /**
     * Sets the owner of a summon entity and, if the entity is a creature, adds a follow-owner
     * AI task so it stays near its owner. Passing {@code null} clears ownership.
     *
     * @param entityliving the summon entity to update
     * @param owner        the new owner, or {@code null} to remove the current owner
     */
    public static void setOwner(EntityLivingBase entityliving, EntityLivingBase owner) {
        if (owner == null) {
            entityliving.getEntityData().removeTag(summonOwnerKey);
            return;
        }

        entityliving.getEntityData().setInteger(summonOwnerKey, owner.getEntityId());
        if (entityliving instanceof EntityCreature) {
            float speed = entityliving.getAIMoveSpeed();
            if (speed <= 0) speed = 1.0f;
            ((EntityCreature) entityliving).tasks.addTask(1, new EntityAISummonFollowOwner((EntityCreature) entityliving, speed, 10, 20));
        }
    }

    /**
     * Sets how long (in ticks) the summon will persist before expiring.
     * A value of {@code 0} or less means the summon lasts indefinitely.
     *
     * @param entity   the summon entity
     * @param duration lifetime in ticks, or {@code <= 0} for permanent
     */
    public static void setSummonDuration(EntityLivingBase entity, int duration) {
        entity.getEntityData().setInteger(summonDurationKey, duration);
    }

    /**
     * Checks whether a summon has exceeded its configured lifetime.
     * Returns {@code false} for non-summons, entities with no duration set, or when the
     * entity tick count has not yet surpassed the stored duration.
     *
     * @param entity the entity to check
     * @return {@code true} if the summon's duration has elapsed
     */
    public static boolean isSummonExpired(EntityLivingBase entity) {
        if (entity.getEntityData() == null || !isSummon(entity))
            return false;
        int duration = entity.getEntityData().getInteger(summonDurationKey);
        if (duration <= 0)
            return false;
        return entity.ticksExisted > duration;
    }

    /**
     * Returns the entity ID of the summon's owner, or {@code -1} if the entity is not a summon
     * or has no owner stored.
     *
     * @param entityliving the summon entity to query
     * @return owner entity ID, or {@code -1}
     */
    public static int getOwner(EntityLivingBase entityliving) {
        if (!isSummon(entityliving)) return -1;
        return entityliving.getEntityData().getInteger(summonOwnerKey);
    }

    /**
     * Reverts a summon back to its original AI state, restoring the tasks that were saved when
     * {@link #makeSummon_PlayerFaction} or {@link #makeSummon_MonsterFaction} was called with
     * {@code storeForRevert = true}. Also notifies the former owner's capability to decrement
     * summon count and removes the is-summon tag.
     *
     * @param entityliving the creature whose AI should be restored
     * @return {@code true} if stored tasks were found and successfully restored;
     *         {@code false} if no tasks were stored for this entity
     */
    public static boolean revertAI(EntityCreature entityliving) {

        int ownerID = getOwner(entityliving);
        Entity owner = entityliving.world.getEntityByID(ownerID);
        if (owner != null && owner instanceof EntityLivingBase) {
            EntityExtension.For((EntityLivingBase) owner).removeSummon();
            if (EntityExtension.For((EntityLivingBase) owner).isManaLinkedTo(entityliving)) {
                EntityExtension.For((EntityLivingBase) owner).updateManaLink(entityliving);
            }
        }

        entityliving.getEntityData().setBoolean(isSummonKey, false);
        setOwner(entityliving, null);

        if (storedTasks.containsKey(entityliving.getEntityId())) {
            entityliving.targetTasks.taskEntries.clear();
            entityliving.targetTasks.taskEntries.addAll(storedTasks.get(entityliving.getEntityId()));
            storedTasks.remove(entityliving.getEntityId());

            if (storedAITasks.get(entityliving.getEntityId()) != null) {
                ArrayList<EntityAITaskEntry> toRemove = new ArrayList<EntityAITaskEntry>();
                for (Object task : entityliving.tasks.taskEntries) {
                    EntityAITaskEntry base = (EntityAITaskEntry) task;
                    if (base.action instanceof EntityAIAttackMelee || base.action instanceof EntityAISummonFollowOwner) {
                        toRemove.add(base);
                    }
                }

                entityliving.tasks.taskEntries.removeAll(toRemove);

                entityliving.tasks.taskEntries.addAll(storedAITasks.get(entityliving.getEntityId()));
                storedAITasks.remove(entityliving.getEntityId());
            }
            if (!entityliving.world.isRemote && entityliving.getAttackTarget() != null)
                ArsMagica.proxy.addDeferredTargetSet(entityliving, null);
            if (entityliving instanceof EntityTameable) {
                ((EntityTameable) entityliving).setTamed(false);
            }
            return true;
        }

        return false;
    }

    /**
     * Determines whether {@code living} is facing toward the damage source in a way that
     * would allow a shield or blocking action to mitigate it.
     * Returns {@code false} for unblockable damage or when no attack position is available.
     *
     * @param living          the entity that might block
     * @param damageSourceIn  the incoming damage source
     * @return {@code true} if the entity's facing could block the attack
     */
    public static boolean canBlockDamageSource(EntityLivingBase living, DamageSource damageSourceIn) {
        if (!damageSourceIn.isUnblockable()) {
            Vec3d vec3d = damageSourceIn.getDamageLocation();

            if (vec3d != null) {
                Vec3d vec3d1 = living.getLook(1.0F);
                Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(living.posX, living.posY, living.posZ)).normalize();
                vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

                if (vec3d2.dotProduct(vec3d1) < 0.0D) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Resizes an entity's bounding box via reflection, calling the obfuscated {@code setSize}
     * method on {@link net.minecraft.entity.Entity}. Does nothing if the dimensions are
     * unchanged or if the reflected method cannot be found.
     *
     * @param entityliving the entity to resize
     * @param width        new collision box width
     * @param height       new collision box height
     */
    public static void setSize(EntityLivingBase entityliving, float width, float height) {
        if (entityliving.width == width && entityliving.height == height)
            return;
        if (ptrSetSize == null) {
            try {
                ptrSetSize = net.minecraftforge.fml.relauncher.ReflectionHelper.findMethod(Entity.class, "setSize", "func_70105_a", float.class, float.class);
            } catch (Throwable t) {
                am2.ArsMagica.LOGGER.error("Failed to find setSize method on Entity: ", t);
                return;
            }
        }
        if (ptrSetSize != null) {
            try {
                ptrSetSize.setAccessible(true);
                ptrSetSize.invoke(entityliving, width, height);
                //entityliving.yOffset = entityliving.height * 0.8f;
            } catch (Throwable t) {
                am2.ArsMagica.LOGGER.error("Failed to invoke setSize method on Entity: ", t);
                return;
            }
        }
    }

    /**
     * Flips the X and Y components of a look vector when the entity is under an inversion effect
     * (e.g., flying upside-down). Returns the vector unchanged for non-living or non-inverted entities.
     *
     * @param vecIn    the original look vector
     * @param entityIn the entity whose inversion state is checked
     * @return the corrected look vector
     */
    public static Vec3d correctLook(Vec3d vecIn, Entity entityIn) {
        if (entityIn instanceof EntityLivingBase && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
            return new Vec3d(-vecIn.x, -vecIn.y, vecIn.z);
        }
        return vecIn;
    }

    /**
     * Adjusts the eye-height offset for inverted or shrunken players.
     * For an inverted player the eye position is mirrored from the feet up;
     * for a shrunken entity the height is scaled by the shrink factor stored in
     * {@link EntityExtension}.
     *
     * @param floatIn  the nominal eye height offset
     * @param entityIn the entity to correct for
     * @return the corrected eye height offset
     */
    public static float correctEyePos(float floatIn, Entity entityIn) {
        float curHeight = floatIn;
        if (entityIn instanceof EntityPlayer && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
            EntityPlayer player = (EntityPlayer) entityIn;
            curHeight = player.height - floatIn - 0.1f;
        }
        if (entityIn instanceof EntityLivingBase && EntityExtension.For((EntityLivingBase) entityIn).shrinkAmount != 0)
            curHeight *= EntityExtension.For((EntityLivingBase) entityIn).shrinkAmount;
        return curHeight;
    }

    /**
     * Applies movement impulses for an inverted player so that strafing and forward/backward
     * controls map correctly to world-space directions when the player is upside-down.
     * Returns {@code false} (and does nothing) if the entity is not an inverted player.
     *
     * @param strafe   lateral input [-1, 1]
     * @param forward  forward input [-1, 1]
     * @param friction movement friction / speed multiplier provided by the caller
     * @param entityIn the entity being moved
     * @return {@code true} if the corrected movement was applied; {@code false} otherwise
     */
    public static boolean correctMovement(float strafe, float forward, float friction, Entity entityIn) {
        if (entityIn instanceof EntityPlayer && EntityExtension.For((EntityLivingBase) entityIn).isInverted()) {
            float f = strafe * strafe + forward * forward;
            if (f >= 1.0E-4F) {
                f = MathHelper.sqrt(f);

                if (f < 1.0F) {
                    f = 1.0F;
                }

                f = friction / f;
                strafe = strafe * f;
                forward = forward * f;
                float f1 = MathHelper.sin(-entityIn.rotationYaw * 0.017453292F);
                float f2 = MathHelper.cos(-entityIn.rotationYaw * 0.017453292F);
                entityIn.motionX += (double) (strafe * f2 - forward * f1);
                entityIn.motionZ += (double) (forward * f2 + strafe * f1);
            }
            return true;
        }
        return false;
    }

    /**
     * Records the block coordinates of the {@link TileEntitySummoner} that spawned this entity
     * in the entity's persistent NBT data, so the entity can navigate back to or guard that position.
     *
     * @param entityliving the entity that was spawned
     * @param summoner     the summoner tile that created it
     */
    public static void setTileSpawned(EntityLivingBase entityliving, TileEntitySummoner summoner) {
        entityliving.getEntityData().setInteger(summonTileXKey, summoner.getPos().getX());
        entityliving.getEntityData().setInteger(summonTileYKey, summoner.getPos().getY());
        entityliving.getEntityData().setInteger(summonTileZKey, summoner.getPos().getZ());
    }

    /**
     * Adds a guard-post AI task to the entity that causes it to patrol/return to the given
     * world coordinates. Uses the entity's movement speed (minimum 1.0) and a wander radius
     * of 3 blocks within a 16-block detection range.
     *
     * @param entity the creature that will guard the location
     * @param x      X coordinate of the guard post
     * @param y      Y coordinate of the guard post
     * @param z      Z coordinate of the guard post
     */
    public static void setGuardSpawnLocation(EntityCreature entity, double x, double y, double z) {
        float speed = entity.getAIMoveSpeed();
        if (speed <= 0) speed = 1.0f;
        entity.tasks.addTask(1, new EntityAIGuardSpawnLocation(entity, speed, 3, 16, new AMVector3(x, y, z)));
    }

    /**
     * Deducts up to {@code amount} XP from the player's total, capped at their current XP pool,
     * and recalculates their level and progress bar accordingly.
     *
     * @param amount the amount of XP to remove
     * @param player the player to deduct from
     * @return the actual amount of XP deducted (may be less than {@code amount} if the player
     *         had insufficient XP)
     */
    public static int deductXP(int amount, EntityPlayer player) {
        int i = player.experienceTotal;

        if (amount > i) {
            amount = i;
        }
        player.experienceTotal -= amount;
        player.experience = 0;
        player.experienceLevel = 0;
        int addedXP = 0;
        while (addedXP < player.experienceTotal) {
            int toAdd = player.experienceTotal - addedXP;
            int cap = xpBarCap(player.experienceLevel);
            toAdd = Math.min(toAdd, cap);
            player.experience = toAdd / (float) cap;
            if (player.experience == 1f) {
                player.experienceLevel++;
                player.experience = 0;
            }
            addedXP += toAdd;
        }
        return amount;
    }

    private EntityUtils() {}

}
