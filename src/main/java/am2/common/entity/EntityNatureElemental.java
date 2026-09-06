package am2.common.entity;

import am2.ArsMagica;
import am2.common.entity.ai.EntityAINatureElementalCharge;
import am2.common.entity.ai.EntityAINatureElementalSniff;
import am2.common.entity.ai.EntityAINatureElementalWander;
import am2.common.entity.ai.NatureElementalCharge;
import am2.common.registry.AMLoot;
import am2.common.registry.Affinities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityNatureElemental extends EntityElemental {
    private static final DataParameter<Byte> CHARGE_POSE = EntityDataManager.createKey(EntityNatureElemental.class, DataSerializers.BYTE);
    private static final DataParameter<Boolean> SNIFFING = EntityDataManager.createKey(EntityNatureElemental.class, DataSerializers.BOOLEAN);
    private static final byte HEADBUTT_STATUS = 4;
    private static final UUID CHARGE_DAMAGE_ID = UUID.fromString("3737fc9b-86bc-4829-93ab-19d747df4e9f");
    private NatureElementalCharge charge;
    private EntityLivingBase chargeTarget;
    private long nextChargeTick;
    private int headbuttTicks;
    private float previousSniffAmount;
    private float sniffAmount;

    public EntityNatureElemental(World world) {
        super(world, Affinities.nature);
        setSize(1.1F, 1.5F);
        experienceValue = 8;
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAINatureElementalCharge(this));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.1D, true) {
            @Override
            protected double getAttackReachSqr(EntityLivingBase target) {
                double reach = 1.5D + target.width * 0.5D;
                return reach * reach;
            }

            @Override
            protected void checkAndPerformAttack(EntityLivingBase target, double distance) {
                if (getEntitySenses().canSee(target)) super.checkAndPerformAttack(target, distance);
            }
        });
        tasks.addTask(6, new EntityAINatureElementalSniff(this));
        tasks.addTask(7, new EntityAINatureElementalWander(this));
        tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(8, new EntityAILookIdle(this));
        initAffinityTargeting(16.0D);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CHARGE_POSE, (byte) 0);
        dataManager.register(SNIFFING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getNatureElementalMaxHealth());
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getNatureElementalAttackDamage());
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.28D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.4D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24.0D);
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getNatureElementalArmor();
    }

    public NatureElementalCharge getCharge() { return charge; }
    public boolean isPreparingCharge() { return dataManager.get(CHARGE_POSE) == 1; }
    public boolean isCharging() { return dataManager.get(CHARGE_POSE) == 2; }

    public boolean canPerformIdleActions() {
        return isEntityAlive() && !isAIDisabled() && getAttackTarget() == null && charge == null
                && hurtTime == 0 && !isBurning() && !isRiding() && !isBeingRidden();
    }

    public boolean isSniffing() { return dataManager.get(SNIFFING); }
    public void setSniffing(boolean sniffing) { dataManager.set(SNIFFING, sniffing); }

    public float getSniffAmount(float partialTicks) {
        return previousSniffAmount + (sniffAmount - previousSniffAmount) * partialTicks;
    }

    public int getChargeCooldown() {
        return (int) Math.max(0L, Math.min(NatureElementalCharge.COOLDOWN_TICKS, nextChargeTick - world.getTotalWorldTime()));
    }

    public void beginCharge(EntityLivingBase target) {
        setSniffing(false);
        charge = new NatureElementalCharge(target.posX - posX, target.posZ - posZ, world.getTotalWorldTime());
        chargeTarget = target;
        getNavigator().clearPath();
        dataManager.set(CHARGE_POSE, (byte) 1);
    }

    public void endCharge() {
        if (charge != null) {
            nextChargeTick = world.getTotalWorldTime() + NatureElementalCharge.COOLDOWN_TICKS;
            motionX = motionZ = 0.0D;
        }
        charge = null;
        chargeTarget = null;
        dataManager.set(CHARGE_POSE, (byte) 0);
        getNavigator().clearPath();
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (world.isRemote || charge == null) {
            super.travel(strafe, vertical, forward);
            return;
        }
        long now = world.getTotalWorldTime();
        if (chargeTarget != null) charge.updateAim(chargeTarget.posX - posX, chargeTarget.posZ - posZ, now);
        boolean rushing = charge.isCharging(now);
        dataManager.set(CHARGE_POSE, (byte) (charge.isPreparing(now) ? 1 : rushing ? 2 : 0));
        rotationYaw = (float) Math.toDegrees(Math.atan2(-charge.getDirectionX(), charge.getDirectionZ()));
        rotationYawHead = renderYawOffset = rotationYaw;
        motionX = rushing ? charge.getDirectionX() * NatureElementalCharge.SPEED : 0.0D;
        motionZ = rushing ? charge.getDirectionZ() * NatureElementalCharge.SPEED : 0.0D;
        Vec3d from = new Vec3d(posX, posY + height * 0.5D, posZ);
        // Use vanilla movement/collision and gravity, but suppress all navigation steering.
        float previousStepHeight = stepHeight;
        try {
            // Let a ram climb full blocks before treating horizontal collisions as a stop.
            if (rushing) stepHeight = Math.max(previousStepHeight, 1.0F);
            super.travel(0.0F, 0.0F, 0.0F);
        } finally {
            stepHeight = previousStepHeight;
        }
        if (!rushing) return;
        Vec3d to = new Vec3d(posX, posY + height * 0.5D, posZ);
        if (chargeTarget != null && chargeTarget.isEntityAlive() && getEntitySenses().canSee(chargeTarget)) {
            // Sweep the actual, block-clipped movement so fast charges cannot skip a target.
            AxisAlignedBB hitBox = chargeTarget.getEntityBoundingBox().grow(width * 0.5D, height * 0.5D, width * 0.5D);
            if (hitBox.contains(from) || hitBox.contains(to) || hitBox.calculateIntercept(from, to) != null) {
                if (attackWithChargeDamage(chargeTarget)) {
                    chargeTarget.knockBack(this, 1.2F, -charge.getDirectionX(), -charge.getDirectionZ());
                    chargeTarget.velocityChanged = true;
                }
                charge.stop(now);
            }
        }
        if (collidedHorizontally || !onGround) charge.stop(now);
        if (!charge.isCharging(now)) {
            motionX = motionZ = 0.0D;
            dataManager.set(CHARGE_POSE, (byte) 0);
        }
    }

    @Override
    protected float updateDistance(float yaw, float distance) {
        if (isPreparingCharge() || isCharging()) {
            renderYawOffset = rotationYawHead = rotationYaw;
            return distance;
        }
        return super.updateDistance(yaw, distance);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (target instanceof EntityNatureElemental) return false;
        headbuttTicks = 8;
        world.setEntityState(this, HEADBUTT_STATUS);
        return super.attackEntityAsMob(target);
    }

    private boolean attackWithChargeDamage(Entity target) {
        IAttributeInstance damage = getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        AttributeModifier bonus = new AttributeModifier(CHARGE_DAMAGE_ID, "Nature elemental charge damage",
                ArsMagica.config.getNatureElementalChargeDamageMultiplier() - 1.0D, 2).setSaved(false);
        damage.applyModifier(bonus);
        try {
            // Keep vanilla damage, armor, shields, enchantments and the headbutt animation.
            return attackEntityAsMob(target);
        } finally {
            // Charge damage must never leak into subsequent regular attacks or saved attributes.
            damage.removeModifier(bonus);
        }
    }

    @Override
    public void onLivingUpdate() {
        if (headbuttTicks > 0) headbuttTicks--;
        if (!world.isRemote && !canPerformIdleActions()) setSniffing(false);
        previousSniffAmount = sniffAmount;
        sniffAmount = MathHelper.clamp(sniffAmount + (isSniffing() ? 0.1F : -0.15F), 0.0F, 1.0F);
        super.onLivingUpdate();
    }

    @Override
    public void handleStatusUpdate(byte id) {
        if (id == HEADBUTT_STATUS) headbuttTicks = 8;
        else super.handleStatusUpdate(id);
    }

    public float getHeadbuttProgress(float partialTicks) {
        return headbuttTicks > 0 ? 1.0F - Math.max(0.0F, headbuttTicks - partialTicks) / 8.0F : 0.0F;
    }

    @Override
    public void setAttackTarget(EntityLivingBase target) {
        super.setAttackTarget(target instanceof EntityNatureElemental ? null : target);
        if (getAttackTarget() != null) setSniffing(false);
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> targetClass) {
        return !EntityNatureElemental.class.isAssignableFrom(targetClass) && super.canAttackClass(targetClass);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return !(source.getTrueSource() instanceof EntityNatureElemental) && super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean isValidLightLevel() {
        // Use the day cycle: a daytime thunderstorm must not enable spawning.
        long time = Math.floorMod(world.getWorldTime(), 24000L);
        return time >= 13000L && time < 23000L;
    }

    @Override
    public float getBlockPathWeight(BlockPos pos) {
        // EntityCreature also uses this for spawning; do not inherit the mob's light penalty.
        return 0.0F;
    }

    @Override
    public boolean getCanSpawnHere() {
        return SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this) && super.getCanSpawnHere();
    }

    @Override
    public int getMaxSpawnedInChunk() { return 2; }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_PIG_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ENTITY_PIG_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_PIG_DEATH; }
    @Override
    protected float getSoundPitch() { return super.getSoundPitch() * 0.7F; }
    @Override
    protected ResourceLocation getLootTable() { return AMLoot.NATURE_ELEMENTAL_LOOT; }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("NatureChargeCooldown", charge == null ? getChargeCooldown() : NatureElementalCharge.COOLDOWN_TICKS);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        endCharge();
        setSniffing(false);
        previousSniffAmount = sniffAmount = 0.0F;
        nextChargeTick = world.getTotalWorldTime() + MathHelper.clamp(compound.getInteger("NatureChargeCooldown"), 0, NatureElementalCharge.COOLDOWN_TICKS);
    }
}
