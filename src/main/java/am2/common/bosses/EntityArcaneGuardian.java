package am2.common.bosses;

import am2.ArsMagica;
import am2.common.bosses.ai.EntityAICastSpell;
import am2.common.bosses.ai.EntityAIDispel;
import am2.common.bosses.ai.ISpellCastCallback;
import am2.common.packet.AMNetHandler;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityArcaneGuardian extends AM2Boss {

    private float runeRotationZ = 0;
    private float runeRotationY = 0;

    private static final DataParameter<Integer> DW_TARGET_ID = EntityDataManager.createKey(EntityArcaneGuardian.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> DW_CAST_TICKS = EntityDataManager.createKey(EntityArcaneGuardian.class, DataSerializers.VARINT);

    public EntityArcaneGuardian(World par1World) {
        super(par1World);
        this.setSize(1.0f, 3.0f);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DW_TARGET_ID, -1);
        this.dataManager.register(DW_CAST_TICKS, 0);
    }

    /**
     * Get the current casting animation ticks (synced to client via DataManager)
     */
    public int getCastTicks() {
        return this.dataManager.get(DW_CAST_TICKS);
    }

    /**
     * Set the casting animation ticks (server side only)
     */
    public void setCastTicks(int ticks) {
        this.dataManager.set(DW_CAST_TICKS, ticks);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getArcaneGuardianMaxHealth());
    }

    @Override
    public void onUpdate() {

        if (this.motionY < 0) {
            this.motionY *= 0.7999999f;
        }

        if (world.isRemote) {
            updateRotations();
        }

        if (!world.isRemote) {
            int eid = this.dataManager.get(DW_TARGET_ID);
            int tid = -1;
            if (this.getAttackTarget() != null) {
                tid = this.getAttackTarget().getEntityId();
            }
            if (eid != tid) {
                this.dataManager.set(DW_TARGET_ID, tid);
            }

            // Sync cast ticks via DataManager for smooth client animation
            if (this.getCurrentAction() == BossActions.CASTING) {
                this.setCastTicks(this.getTicksInCurrentAction());
            } else {
                // Negative value signals "returning to rest" animation
                int current = this.getCastTicks();
                if (current > 0) {
                    this.setCastTicks(-current); // Start return animation
                } else if (current < 0) {
                    this.setCastTicks(current + 2); // Animate back toward 0
                }
            }
        }

        super.onUpdate();
    }

    private void updateRotations() {
        runeRotationZ = 0;
        float runeRotationSpeed = 0.085f;
        Entity target = this.getTarget();

        // Only recalculate target rotation if we have a valid target
        // This prevents jitter when the target entity isn't loaded on the client yet
        if (target != null) {
            double deltaX = target.posX - this.posX;
            double deltaZ = target.posZ - this.posZ;

            double angle = Math.atan2(deltaZ, deltaX);

            angle -= Math.toRadians(MathHelper.wrapDegrees(this.rotationYaw + 90) + 180);

            float targetRuneRotationY = (float) angle;

            if (targetRuneRotationY > runeRotationY)
                runeRotationY += runeRotationSpeed;
            else if (targetRuneRotationY < runeRotationY)
                runeRotationY -= runeRotationSpeed;

            if (isWithin(runeRotationY, targetRuneRotationY, runeRotationSpeed)) {
                runeRotationY = targetRuneRotationY;
            }
        }
        // When no target, keep the current runeRotationY - don't reset to 0
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.getTrueSource() == null) {
            return super.attackEntityFrom(par1DamageSource, par2);
        }

        if (checkRuneRetaliation(par1DamageSource)) {
            return super.attackEntityFrom(par1DamageSource, par2);
        }
        return super.attackEntityFrom(par1DamageSource, par2 * 0.8F);
    }

    private boolean checkRuneRetaliation(DamageSource damagesource) {
        Entity source = damagesource.getTrueSource();
        if (source instanceof EntityArcaneGuardian) {
            return true;
        }

        double deltaX = source.posX - this.posX;
        double deltaZ = source.posZ - this.posZ;

        double angle = Math.atan2(deltaZ, deltaX);

        angle -= Math.toRadians(MathHelper.wrapDegrees(this.rotationYaw + 90) + 180);

        float targetRuneRotationY = (float) angle;

        if (isWithin(runeRotationY, targetRuneRotationY, 0.5f)) {
            if (this.getDistanceSq(source) < 9) {
                double speed = 2.5;
                double vertSpeed = 0.325;

                deltaZ = source.posZ - this.posZ;
                deltaX = source.posX - this.posX;
                angle = Math.atan2(deltaZ, deltaX);

                double radians = angle;

                if (source instanceof EntityPlayer) {
                    AMNetHandler.INSTANCE.sendVelocityAddPacket(source.world, (EntityLivingBase) source, speed * Math.cos(radians), vertSpeed, speed * Math.sin(radians));
                }
                source.motionX = (speed * Math.cos(radians));
                source.motionZ = (speed * Math.sin(radians));
                source.motionY = vertSpeed;

                source.attackEntityFrom(DamageSource.causeMobDamage(this), 2);
                return false;
            }
        }
        return true;
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        return damageAmt;
    }

    private boolean isWithin(float source, float target, float tolerance) {
        return source + tolerance > target && source - tolerance < target;
    }

    public Entity getTarget() {
        int eid = this.dataManager.get(DW_TARGET_ID);
        if (eid == -1) return null;
        return this.world.getEntityByID(eid);
    }

    public float getRuneRotationZ() {
        return runeRotationZ;
    }

    public float getRuneRotationY() {
        return runeRotationY;
    }

    @Override
    protected void initSpecificAI() {
        this.tasks.addTask(1, new EntityAIDispel(this));
        this.tasks.addTask(1, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.getInstance().healSelf, 16, 23, 60, BossActions.CASTING, new ISpellCastCallback<EntityArcaneGuardian>() {
            @Override
            public boolean shouldCast(EntityArcaneGuardian host, ItemStack spell) {
                return host.getHealth() < host.getMaxHealth();
            }
        }));
        this.tasks.addTask(2, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.getInstance().blink, 16, 23, 20, BossActions.CASTING));
        this.tasks.addTask(3, new EntityAICastSpell<EntityArcaneGuardian>(this, NPCSpells.getInstance().arcaneBolt, 12, 23, 5, BossActions.CASTING));
    }

    @Override
    public void setCurrentAction(BossActions action) {
        // Only send packet if action actually changes to prevent animation reset jitter
        boolean changed = this.getCurrentAction() != action;
        super.setCurrentAction(action);
        if (!world.isRemote && changed) {
            AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
        }
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getArcaneGuardianArmor();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.ARCANE_GUARDIAN_LOOT;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.ARCANE_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.ARCANE_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.ARCANE_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.ARCANE_GUARDIAN_SPELL;
    }

    @Override
    protected Color getBarColor() {
        return Color.PURPLE;
    }

}
