package am2.common.bosses;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.math.AMVector3;
import am2.common.bosses.ai.*;
import am2.common.registry.AMLoot;
import am2.common.registry.AMPotions;
import am2.common.registry.AMSounds;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;
import am2.common.animation.IAnimatedEntity;

public class EntityEnderGuardian extends AM2Boss implements IAnimatedEntity {

    private int wingFlapTime = 0;
    private int ticksSinceLastAttack = 0;
    private String lastDamageType = "";
    private int hitCount = 0;
    private AMVector3 spawn;

    private static final DataParameter<Integer> ATTACK_TARGET = EntityDataManager.createKey(EntityEnderGuardian.class, DataSerializers.VARINT);

    public EntityEnderGuardian(World par1World) {
        super(par1World);
        setSize(1, 3);
    }

    @Override
    protected void initSpecificAI() {
        //tasks.addTask(2, new EntityAIHandsOfTheDead(this));
        tasks.addTask(2, new EntityAIShadowstep(this));
        tasks.addTask(2, new EntityAIEnderwave(this));
        tasks.addTask(2, new EntityAIOtherworldlyRoar(this));
        tasks.addTask(2, new EntityAIProtect(this));
        tasks.addTask(2, new EntityAIEnderRush(this));
        tasks.addTask(2, new EntityAIEndertorrent(this));
        tasks.addTask(2, new EntityAIEnderbolt(this));
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getEnderGuardianArmor();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ATTACK_TARGET, -1);
    }

    @Override
    public float getEyeHeight() {
        return 2.5f;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getEnderGuardianMaxHealth());
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        if (source.isMagicDamage()) {
            damageAmt *= 2f;
        }
        return damageAmt;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (spawn == null)
            spawn = new AMVector3(this);

        wingFlapTime++;
        ticksSinceLastAttack++;

        if (this.motionY < 0)
            this.motionY *= 0.7999999f;

        switch (getCurrentAction()) {
            case LONG_CASTING: //roar
                if (this.getTicksInCurrentAction() == 32)
                    world.playSound(posX, posY, posZ, AMSounds.ENDER_GUARDIAN_ROAR, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
                break;
            case CHARGE:
                if (this.getTicksInCurrentAction() == 0)
                    this.addVelocity(0, 1.5f, 0);
                break;
            default:
        }

        if (shouldFlapWings() && wingFlapTime % (50 * this.getWingFlapSpeed()) == 0) {
            world.playSound(posX, posY, posZ, AMSounds.ENDER_GUARDIAN_FLAP, SoundCategory.HOSTILE, 1.0f, 1.0f, false);
        }
    }

    public int getTicksSinceLastAttack() {
        return ticksSinceLastAttack;
    }

    @Override
    public void setAttackTarget(EntityLivingBase par1EntityLivingBase) {
        super.setAttackTarget(par1EntityLivingBase);
        if (!world.isRemote) {
            if (par1EntityLivingBase != null)
                this.dataManager.set(ATTACK_TARGET, par1EntityLivingBase.getEntityId());
            else
                this.dataManager.set(ATTACK_TARGET, -1);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {

//		int you = 0;
//		int should = 0;
//		int not = 0;
//		int be = 0;
//		int looking = 0;
//		int here = 0;
//		int cheater = 0;

        //Thanks but I'm not reading all the code, just fixing.

        if (par1DamageSource.getTrueSource() instanceof EntityEnderman) {
            ((EntityEnderman) par1DamageSource.getTrueSource()).attackEntityFrom(DamageSources.wtfBoom, 5000);
            this.heal(10);
            return false;
        }

        if (par1DamageSource.damageType.equals("OUT_OF_WORLD")) {
            if (spawn != null) {
                this.setPosition(spawn.x, spawn.y, spawn.z);
                this.setCurrentAction(BossActions.IDLE);
                if (!this.world.isRemote)
                    ArsMagica.proxy.addDeferredTargetSet(this, null);
            } else {
                this.setDead();
            }
            return false;
        }

        ticksSinceLastAttack = 0;

        if (!world.isRemote && par1DamageSource.getTrueSource() != null && par1DamageSource.getTrueSource() instanceof EntityPlayer) {
            if (par1DamageSource.damageType == this.lastDamageType) {
                hitCount++;
                if (hitCount > 5)
                    this.heal(par2 / 4);
                return false;
            } else {
                this.lastDamageType = par1DamageSource.damageType;
                hitCount = 1;
            }
        }

        return super.attackEntityFrom(par1DamageSource, par2);
    }

    @Override
    public EntityLivingBase getAttackTarget() {
        if (!world.isRemote)
            return super.getAttackTarget();
        else
            return (EntityLivingBase) world.getEntityByID(this.dataManager.get(ATTACK_TARGET));
    }

    @Override
    public void setCurrentAction(BossActions action) {
        this.currentAction = action;
        if (action == BossActions.LONG_CASTING)
            wingFlapTime = 0;
    }

    public int getWingFlapTime() {
        return wingFlapTime;
    }

    public float getWingFlapSpeed() {
        switch (this.currentAction) {
            case CASTING:
                return 0.5f;
            case STRIKE:
                return 0.4f;
            case CHARGE:
                if (ticksInCurrentAction < 15)
                    return 0.25f;
                return 0.75f;
            default:
                return 0.25f;
        }
    }

    public boolean shouldFlapWings() {
        return currentAction != BossActions.LONG_CASTING && currentAction != BossActions.SHIELD_BASH;
    }

    @Override
    public boolean isPotionActive(Potion par1Potion) {
        if (par1Potion == AMPotions.spell_reflect && (currentAction == BossActions.SHIELD_BASH || currentAction == BossActions.LONG_CASTING))
            return true;
        if (par1Potion == AMPotions.magic_shield && (currentAction == BossActions.SHIELD_BASH || currentAction == BossActions.LONG_CASTING))
            return true;
        return super.isPotionActive(par1Potion);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.ENDER_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.ENDER_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.ENDER_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.ENDER_GUARDIAN_ATTACK;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.ENDER_GUARDIAN_LOOT;
    }

    @Override
    public void setAnimID(int id) {
        setCurrentAction(BossActions.values()[id]);
        ticksInCurrentAction = 0;
    }

    @Override
    public void setAnimTick(int tick) {
        this.ticksInCurrentAction = tick;
    }

    @Override
    public int getAnimID() {
        return currentAction.ordinal();
    }

    @Override
    public int getAnimTick() {
        return ticksInCurrentAction;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    protected Color getBarColor() {
        return Color.PURPLE;
    }

    @Override
    public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        return false;
    }
}
