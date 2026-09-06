package am2.common.entity;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleGrow;
import am2.common.entity.ai.EntityAIAirBlast;
import am2.common.entity.ai.EntityAIFlyingWander;
import am2.common.entity.ai.EntityAIRangedAttackSpell;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import am2.common.registry.Affinities;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityAirElemental extends EntityElemental {

    private static final DataParameter<Integer> AIR_BLAST_TARGET = EntityDataManager.createKey(EntityAirElemental.class, DataSerializers.VARINT);
    private static final DataParameter<Float> AIR_BLAST_INFLATION = EntityDataManager.createKey(EntityAirElemental.class, DataSerializers.FLOAT);
    private EntityAIAirBlast airBlastAI;
    private long nextAirBlastTick;
    private float previousAirBlastInflation;
    private float airBlastInflation;

    /** Client-side ambient wind-trail particle timer. */
    private int trailTimer = 0;

    public EntityAirElemental(World world) {
        super(world, Affinities.air);
        setSize(0.6F, 1.8F);
        this.moveHelper = new EntityFlyHelper(this);
        EntityExtension.For(this).setMagicLevelWithMana(10 + this.rand.nextInt(10));
        initAI();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(AIR_BLAST_TARGET, -1);
        dataManager.register(AIR_BLAST_INFLATION, 0.0F);
    }

    private void initAI() {
        airBlastAI = new EntityAIAirBlast(this);
        this.tasks.addTask(1, airBlastAI);
        this.tasks.addTask(2, new EntityAIRangedAttackSpell(this, 1.0f, 45, NPCSpells.getInstance().airElemental_attack));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0f, false));
        this.tasks.addTask(7, new EntityAIFlyingWander(this, 0.7, 14.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        initAffinityTargeting(16.0D);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getAirElementalMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getAirElementalAttackDamage());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.65);
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getAirElementalArmor();
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> targetClass) {
        return !EntityAirElemental.class.isAssignableFrom(targetClass) && super.canAttackClass(targetClass);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        super.setAttackTarget(target instanceof EntityAirElemental ? null : target);
    }

    @Override
    public void onLivingUpdate() {
        if (world.isRemote) {
            previousAirBlastInflation = airBlastInflation;
            airBlastInflation = dataManager.get(AIR_BLAST_INFLATION);
        }
        // Drifts rather than drops - barely affected by gravity while airborne
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.4D;
        }
        super.onLivingUpdate();

        if (world.isRemote && dataManager.get(AIR_BLAST_TARGET) >= 0) {
            spawnAirTorrent();
        }

        if (world.isRemote && (this.motionX * this.motionX + this.motionZ * this.motionZ + this.motionY * this.motionY) > 0.0025D) {
            trailTimer++;
            if (trailTimer >= 3) {
                trailTimer = 0;
                double dx = -this.motionX * 4.0 + (rand.nextDouble() - 0.5) * 0.3;
                double dy = -this.motionY * 4.0 + (rand.nextDouble() - 0.5) * 0.3;
                double dz = -this.motionZ * 4.0 + (rand.nextDouble() - 0.5) * 0.3;
                // The rendered head is centered 1.5 blocks above the feet. Keep
                // the cloud just behind its face, even when moving sideways/backward.
                float headYaw = this.rotationYawHead * 0.017453292F;
                double backX = MathHelper.sin(headYaw) * 0.45D;
                double backZ = -MathHelper.cos(headYaw) * 0.45D;
                world.spawnParticle(EnumParticleTypes.CLOUD,
                        posX + backX, posY + 1.5D, posZ + backZ,
                        dx * 0.02, dy * 0.02, dz * 0.02);
            }
        }
    }

    public EntityAIAirBlast getAirBlastAI() {
        return airBlastAI;
    }

    public int getAirBlastCooldown() {
        return (int) Math.max(0L, Math.min(160L, nextAirBlastTick - world.getTotalWorldTime()));
    }

    public void setAirBlastCooldown(int ticks) {
        nextAirBlastTick = world.getTotalWorldTime() + Math.max(0, ticks);
    }

    public void setAirBlastTarget(EntityLivingBase target) {
        dataManager.set(AIR_BLAST_TARGET, target == null ? -1 : target.getEntityId());
    }

    public void setAirBlastInflation(float inflation) {
        dataManager.set(AIR_BLAST_INFLATION, MathHelper.clamp(inflation, 0.0F, 1.0F));
    }

    @SideOnly(Side.CLIENT)
    public float getAirBlastScale(float partialTicks) {
        return 1.0F + 0.5F * (previousAirBlastInflation + (airBlastInflation - previousAirBlastInflation) * partialTicks);
    }

    public Vec3d getAirBlastOrigin() {
        return new Vec3d(posX, posY + 1.5D, posZ);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("AirBlastCooldown", getAirBlastCooldown());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setAirBlastCooldown(MathHelper.clamp(compound.getInteger("AirBlastCooldown"), 0, 160));
        setAirBlastTarget(null);
        setAirBlastInflation(0.0F);
    }

    @SideOnly(Side.CLIENT)
    private void spawnAirTorrent() {
        Entity target = world.getEntityByID(dataManager.get(AIR_BLAST_TARGET));
        if (!(target instanceof EntityLivingBase) || !target.isEntityAlive()) return;
        Vec3d origin = getAirBlastOrigin();
        Vec3d end = new Vec3d(target.posX, target.posY + target.height * 0.6D, target.posZ);
        Vec3d direction = end.subtract(origin).normalize();
        if (direction.lengthSquared() < 0.001D) return;
        end = origin.add(direction.scale(10.0D));
        RayTraceResult wall = world.rayTraceBlocks(origin, end, false, true, false);
        if (wall != null) end = wall.hitVec;
        double length = origin.distanceTo(end);
        if (length <= 0.5D) return;
        for (int i = 0; i < 6; i++) {
            // Emit at the face and carry each wisp outward. Longer-lived,
            // expanding wisps form a breath instead of popping along a beam.
            double start = 0.25D * getAirBlastScale(1.0F) + 0.1D + rand.nextDouble() * 0.15D;
            double speed = 0.55D + rand.nextDouble() * 0.10D;
            // AMParticle moves before expiring; reserve its final two updates
            // and the spawn offset so wisps stop at walls and at maximum reach.
            int lifetime = Math.min(16, (int) ((length - start - 0.15D) / speed) - 2);
            if (lifetime < 0) continue;
            Vec3d point = origin.add(direction.scale(start));
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "wind", point.x, point.y, point.z);
            if (particle == null) continue;
            particle.addRandomOffset(0.20D, 0.20D, 0.20D);
            particle.setRandomScale(0.12F, 0.20F);
            particle.setRGBColorF(0.84F, 0.94F, 1.0F);
            particle.SetParticleAlpha(0.65F);
            particle.setMaxAge(lifetime);
            particle.addVelocity(direction.x * speed, direction.y * speed, direction.z * speed);
            particle.AddParticleController(new ParticleGrow(particle, 0.012F, 1, false));
            particle.AddParticleController(new ParticleFadeOut(particle, 2, false).setFadeSpeed(0.65F / (lifetime + 2)));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ITEM_ELYTRA_FLYING;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_TAKEOFF;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        // Made of wind - falling doesn't hurt it
        if (source == DamageSource.FALL) return false;
        // Ignore allied melee and spell damage before it can provoke retaliation.
        if (source.getTrueSource() instanceof EntityAirElemental) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 2) { // hurt
            for (int i = 0; i < 15; i++) {
                double dx = rand.nextGaussian() * 0.4;
                double dy = rand.nextDouble() * this.height;
                double dz = rand.nextGaussian() * 0.4;
                world.spawnParticle(EnumParticleTypes.CLOUD,
                        posX + dx, posY + dy, posZ + dz,
                        dx * 1.5, 0.05, dz * 1.5);
            }
        }
        super.handleStatusUpdate(id);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
            return false;
        // Air elementals only form where the wind is open and unobstructed
        return world.canSeeSky(getPosition()) && super.getCanSpawnHere();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.AIR_ELEMENTAL_LOOT;
    }
}
