package am2.common.entity;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.common.blocks.BlockSpellSealedDoor;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.NBTUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class EntitySpellProjectile extends Entity {


    private static final DataParameter<Integer> DW_BOUNCE_COUNTER = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
    private static final DataParameter<Float> DW_GRAVITY = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.FLOAT);
    private static final DataParameter<Optional<SpellData>> DW_EFFECT = EntityDataManager.createKey(EntitySpellProjectile.class, SpellData.OPTIONAL_SPELL_DATA);
    private static final DataParameter<String> DW_ICON_NAME = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.STRING);
    private static final DataParameter<Integer> DW_PIERCE_COUNT = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_COLOR = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_SHOOTER = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> DW_TARGETGRASS = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DW_HOMING = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> DW_HOMING_TARGET = EntityDataManager.createKey(EntitySpellProjectile.class, DataSerializers.VARINT);

    private int currentPierces;

    public EntitySpellProjectile(World worldIn) {
        super(worldIn);
        setSize(0.5F, 0.5F);
        //noClip = true;
    }

    public void setTargetWater() {
        if (!this.world.isRemote)
            this.getDataManager().set(DW_TARGETGRASS, true);
    }

    public boolean targetWater() {
        return this.getDataManager().get(DW_TARGETGRASS);
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(DW_BOUNCE_COUNTER, 0);
        this.getDataManager().register(DW_GRAVITY, 0.f);
        this.getDataManager().register(DW_EFFECT, Optional.<SpellData>absent());
        this.getDataManager().register(DW_ICON_NAME, "arcane");
        this.getDataManager().register(DW_PIERCE_COUNT, 0);
        this.getDataManager().register(DW_COLOR, 0xFFFFFF);
        this.getDataManager().register(DW_SHOOTER, 0);
        this.getDataManager().register(DW_TARGETGRASS, false);
        this.getDataManager().register(DW_HOMING, false);
        this.getDataManager().register(DW_HOMING_TARGET, -1);
    }

    public void setShooter(EntityLivingBase living) {
        this.getDataManager().set(DW_SHOOTER, living.getEntityId());
    }

    public void decreaseBounces() {
        setBounces(getBounces() - 1);
    }

    public int getBounces() {
        return this.getDataManager().get(DW_BOUNCE_COUNTER);
    }

    public int getPierces() {
        return this.getDataManager().get(DW_PIERCE_COUNT) - currentPierces;
    }

    public SpellData getSpell() {
        return this.getDataManager().get(DW_EFFECT).orNull();
    }

    public void bounce(EnumFacing facing) {
        if (facing == null) {
            motionX = -motionX;
            motionY = -motionY;
            motionZ = -motionZ;
        } else {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                motionY = -motionY;
            } else if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                motionZ = -motionZ;
            } else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
                motionX = -motionX;
            }
        }
        decreaseBounces();
    }

    /**
     * Pulls nearby living entities toward {@code cx, cy, cz} with an impulse strong enough to
     * drag them 1-2 blocks.  Only runs server-side.
     */
    private void applyGravitateImpact(double cx, double cy, double cz) {
        SpellData spell = getSpell();
        if (world.isRemote || spell == null || !spell.isModifierPresent(SpellModifiers.GRAVITATE)) return;
        double gravitateStrength = spell.getModifiedValue(SpellModifiers.GRAVITATE, Operation.ADD, world, getShooter(), null);
        double pullRadius = 4.0 + gravitateStrength * 2.0;
        // moderate impulse – enough to nudge an entity but not yank players across the room
        double impactPullStrength = 0.2 + gravitateStrength * 0.15;
        List<Entity> nearby = world.getEntitiesWithinAABBExcludingEntity(this,
                new AxisAlignedBB(cx - pullRadius, cy - pullRadius, cz - pullRadius,
                                  cx + pullRadius, cy + pullRadius, cz + pullRadius));
        EntityLivingBase shooter = getShooter();
        for (Entity entity : nearby) {
            if (!(entity instanceof EntityLivingBase) || entity.equals(shooter)) continue;
            double dx = cx - entity.posX;
            double dy = cy - entity.posY;
            double dz = cz - entity.posZ;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < 0.5 || dist > pullRadius) continue;
            double force = impactPullStrength / dist;
            entity.motionX += dx * force;
            entity.motionY += dy * force;
            entity.motionZ += dz * force;
            entity.velocityChanged = true;
        }
    }

    @Override
    public void onUpdate() {
        try {
            if (ticksExisted > 200)
                this.setDead();

            // ── In-flight gravitational pull (every 5 ticks, server-side only) ─────────────
            if (!world.isRemote && ticksExisted % 5 == 0) {
                SpellData inflight = getSpell();
                if (inflight != null && inflight.isModifierPresent(SpellModifiers.GRAVITATE)) {
                    double gravitateStrength = inflight.getModifiedValue(SpellModifiers.GRAVITATE, Operation.ADD, world, getShooter(), null);
                    double pullRadius = 4.0 + gravitateStrength * 2.0;
                    List<Entity> pullTargets = world.getEntitiesWithinAABBExcludingEntity(this,
                            getEntityBoundingBox().grow(pullRadius, pullRadius, pullRadius));
                    EntityLivingBase shooter = getShooter();
                    for (Entity entity : pullTargets) {
                        if (!(entity instanceof EntityLivingBase) || entity.equals(shooter)) continue;
                        double dx = posX - entity.posX;
                        double dy = posY - entity.posY;
                        double dz = posZ - entity.posZ;
                        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (dist < 0.5 || dist > pullRadius) continue;
                        // gentle per-5-tick nudge scaled down with distance
                        double force = gravitateStrength * 0.02 / dist;
                        entity.motionX += dx * force;
                        entity.motionY += dy * force;
                        entity.motionZ += dz * force;
                        entity.velocityChanged = true;
                    }
                }
            }
            // ─────────────────────────────────────────────────────────────────────────────

            Vec3d startVec = new Vec3d(posX, posY, posZ);
            Vec3d endVec = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);

            // ── Block rayTrace ────────────────────────────────────────────────────────────
            RayTraceResult mop = world.rayTraceBlocks(startVec, endVec);

            // ── Entity ray-intercept (independent of block check, like 1.7.10) ───────────
            // Expand search AABB symmetrically so negative motion directions are covered.
            Entity hitEntity = null;
            double closestEntityDist = Double.MAX_VALUE;
            List<Entity> candidates = world.getEntitiesWithinAABBExcludingEntity(this,
                    getEntityBoundingBox().grow(Math.abs(motionX) + 1.0, Math.abs(motionY) + 1.0, Math.abs(motionZ) + 1.0));
            for (Entity candidate : candidates) {
                if (!candidate.canBeCollidedWith()) continue;
                if (candidate.equals(getShooter())) continue;
                RayTraceResult intercept = candidate.getEntityBoundingBox().grow(0.3D, 0.3D, 0.3D).calculateIntercept(startVec, endVec);
                if (intercept != null) {
                    double dist = startVec.distanceTo(intercept.hitVec);
                    if (dist < closestEntityDist) {
                        closestEntityDist = dist;
                        hitEntity = candidate;
                    }
                }
            }

            // ── Determine which hit to process (entity vs. block – pick the closer one) ──
            boolean solidBlockHit = false;
            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                Block hitBlock = world.getBlockState(mop.getBlockPos()).getBlock();
                boolean isSpellSealedDoor = hitBlock instanceof BlockSpellSealedDoor;
                solidBlockHit = isSpellSealedDoor
                        || hitBlock.isSideSolid(world.getBlockState(mop.getBlockPos()), world, mop.getBlockPos(), mop.sideHit)
                        || targetWater();
            }
            if (hitEntity != null && solidBlockHit) {
                // Both hit – keep whichever is closer; entity wins on a tie
                if (closestEntityDist > startVec.distanceTo(mop.hitVec)) {
                    hitEntity = null;   // block is closer
                } else {
                    solidBlockHit = false; // entity is closer
                }
            }

            // ── Process solid block hit ───────────────────────────────────────────────────
            if (solidBlockHit) {
                world.getBlockState(mop.getBlockPos()).getBlock().onEntityCollision(world, mop.getBlockPos(), world.getBlockState(mop.getBlockPos()), this);
                if (getBounces() > 0) {
                    bounce(mop.sideHit);
                } else {
                    applyGravitateImpact(mop.getBlockPos().getX() + 0.5, mop.getBlockPos().getY() + 0.5, mop.getBlockPos().getZ() + 0.5);
                    if (getSpell() != null) {
                        SpellData spellCopy = getSpell().copy();
                        spellCopy.applyComponentsToGround(world, getShooter(), mop.getBlockPos(), mop.sideHit, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ());
                        spellCopy.execute(world, getShooter(), null, mop.getBlockPos().getX(), mop.getBlockPos().getY(), mop.getBlockPos().getZ(), mop.sideHit);
                    }
                    if (this.getPierces() == 1 || !getSpell().isModifierPresent(SpellModifiers.PIERCING))
                        this.setDead();
                    else
                        this.currentPierces++;
                }
            }

            // ── Process entity hit ────────────────────────────────────────────────────────
            if (hitEntity instanceof EntityLivingBase) {
                applyGravitateImpact(hitEntity.posX, hitEntity.posY, hitEntity.posZ);
                getSpell().applyComponentsToEntity(world, getShooter(), hitEntity);
                if (getSpell() != null && getSpell().hasMoreStages()) {
                    getSpell().execute(world, getShooter(), (EntityLivingBase) hitEntity, hitEntity.posX, hitEntity.posY, hitEntity.posZ, null);
                }
                if (this.getPierces() == 1 || !getSpell().isModifierPresent(SpellModifiers.PIERCING))
                    this.setDead();
                else
                    this.currentPierces++;
            }
            motionY += this.getDataManager().get(DW_GRAVITY);
            setPosition(posX + motionX, posY + motionY, posZ + motionZ);

            // ── Trail particles (client-side only) ───────────────────────────────────────
            if (world.isRemote && ticksExisted % 2 == 0) {
                String trailParticle = getTrailParticle();
                if (trailParticle != null) {
                    AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, trailParticle, posX, posY, posZ);
                    if (particle != null) {
                        particle.setMaxAge(12 + world.rand.nextInt(8));
                        particle.setParticleScale(0.07f + world.rand.nextFloat() * 0.05f);
                        particle.setRGBColorI(getColor());
                        particle.addVelocity(
                                (world.rand.nextDouble() - 0.5) * 0.04,
                                (world.rand.nextDouble() - 0.5) * 0.02,
                                (world.rand.nextDouble() - 0.5) * 0.04);
                        particle.setAffectedByGravity();
                        particle.setDontRequireControllers();
                    }
                }
            }
            // ─────────────────────────────────────────────────────────────────────────────
        } catch (NullPointerException e) {
            e.printStackTrace();
            this.setDead();
        }
    }

    public EntityLivingBase getShooter() {
        try {
            return (EntityLivingBase) world.getEntityByID(this.getDataManager().get(DW_SHOOTER));
        } catch (RuntimeException e) {
            this.setDead();
            return null;
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tagCompund) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompund);
        dataManager.set(DW_BOUNCE_COUNTER, am2Tag.getInteger("BounceCount"));
        dataManager.set(DW_GRAVITY, am2Tag.getFloat("Gravity"));
        dataManager.set(DW_EFFECT, Optional.of(SpellData.readFromNBT(am2Tag.getCompoundTag("Effect"))));
        dataManager.set(DW_ICON_NAME, am2Tag.getString("IconName"));
        dataManager.set(DW_PIERCE_COUNT, am2Tag.getInteger("PierceCount"));
        dataManager.set(DW_COLOR, am2Tag.getInteger("Color"));
        dataManager.set(DW_SHOOTER, am2Tag.getInteger("Shooter"));
        dataManager.set(DW_TARGETGRASS, am2Tag.getBoolean("TargetGrass"));
        dataManager.set(DW_HOMING, am2Tag.getBoolean("Homing"));
        dataManager.set(DW_HOMING_TARGET, am2Tag.getInteger("HomingTarget"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tagCompound) {
        NBTTagCompound am2Tag = NBTUtils.getAM2Tag(tagCompound);
        am2Tag.setInteger("BounceCount", dataManager.get(DW_BOUNCE_COUNTER));
        am2Tag.setFloat("Gravity", dataManager.get(DW_GRAVITY));
        NBTTagCompound tmp = new NBTTagCompound();
        dataManager.get(DW_EFFECT).or(new SpellData(new ItemStack(AMItems.spell), Lists.newArrayList(), UUID.randomUUID(), new NBTTagCompound())).writeToNBT(tmp);
        am2Tag.setTag("Effect", tmp);
        am2Tag.setString("IconName", dataManager.get(DW_ICON_NAME));
        am2Tag.setInteger("PierceCount", dataManager.get(DW_PIERCE_COUNT));

        am2Tag.setInteger("Color", dataManager.get(DW_COLOR));
        am2Tag.setInteger("Shooter", dataManager.get(DW_SHOOTER));
        am2Tag.setBoolean("TargetGrass", dataManager.get(DW_TARGETGRASS));
        am2Tag.setBoolean("Homing", dataManager.get(DW_HOMING));
        am2Tag.setInteger("HomingTarget", dataManager.get(DW_HOMING_TARGET));
    }

    public void selectHomingTarget() {
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, this.getCollisionBoundingBox().expand(10.0F, 10.0F, 10.0F));
        Vec3d pos = new Vec3d(posX, posY, posZ);
        EntityLivingBase target = null;
        double dist = 900;
        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase && !entity.equals(getShooter())) {
                Vec3d ePos = new Vec3d(entity.posX, entity.posY, entity.posZ);
                double eDist = pos.distanceTo(ePos);
                if (eDist < dist) {
                    dist = eDist;
                    target = (EntityLivingBase) entity;
                }
            }
        }

        if (target != null) {
            this.getDataManager().set(DW_HOMING_TARGET, target.getEntityId());
        }
    }

    public EntityLivingBase getHomingTarget() {
        return (EntityLivingBase) world.getEntityByID(this.getDataManager().get(DW_HOMING_TARGET));
    }

    public void setGravity(float projectileGravity) {
        this.getDataManager().set(DW_GRAVITY, projectileGravity);
    }

    public void setSpell(SpellData spell) {
        this.getDataManager().set(DW_EFFECT, Optional.fromNullable(spell));
        Affinity mainAff = spell.getMainShift();
        if (mainAff.equals(Affinities.ender)) this.getDataManager().set(DW_COLOR, 0x550055);
        else if (mainAff.equals(Affinities.ice)) this.getDataManager().set(DW_COLOR, 0xEEF6FF);
        else if (mainAff.equals(Affinities.life)) this.getDataManager().set(DW_COLOR, 0x22FF44);
    }

    public void setBounces(int projectileBounce) {
        this.getDataManager().set(DW_BOUNCE_COUNTER, projectileBounce);
    }

    public void setNumPierces(int pierces) {
        this.getDataManager().set(DW_PIERCE_COUNT, pierces);
        this.currentPierces = 0;
    }

    public void setHoming(boolean homing) {
        this.getDataManager().set(DW_HOMING, homing);
    }

    public void setIcon(String icon) {
        this.getDataManager().set(DW_ICON_NAME, icon);
    }

    public String getIcon() {
        return this.getDataManager().get(DW_ICON_NAME);
    }

    public int getColor() {
        return this.getDataManager().get(DW_COLOR);
    }

    /** Returns the trail particle name for the secondary ambient trail, keyed to the projectile icon (affinity). */
    private String getTrailParticle() {
        switch (getIcon()) {
            case "ice_hand":      return "snowflakes";
            case "explosion_2":   return "smoke";
            case "sparkle":       return "sparkle2";
            case "water_ball":    return "water_hand";
            case "pulse":         return "ghost";
            case "wind":          return "air_hand";
            case "rock":          return "earth_hand";
            case "plant":         return "leaf";
            case "arcane":        return "arcane";
            default:              return null;
        }
    }

}
