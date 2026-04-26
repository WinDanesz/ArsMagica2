package am2.common.bosses;

import am2.ArsMagica;
import am2.api.sources.DamageSourceFrost;
import am2.api.sources.DamageSourceLightning;
import am2.common.bosses.ai.EntityAICastSpell;
import am2.common.bosses.ai.EntityAIChaosWaterBolt;
import am2.common.bosses.ai.EntityAICloneSelf;
import am2.common.bosses.ai.EntityAISpinAttack;
import am2.common.extensions.EntityExtension;
import am2.common.packet.AMNetHandler;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityWaterGuardian extends AM2Boss {

    private EntityWaterGuardian master;
    private final EntityWaterGuardian[] clones;
    private float orbitRotation;
    private boolean uberSpinAvailable = false;

    private static final DataParameter<Boolean> IS_CLONE = EntityDataManager.createKey(EntityWaterGuardian.class, DataSerializers.BOOLEAN);

    public float spinRotation = 0;

    public EntityWaterGuardian(World par1World) {
        super(par1World);
        this.currentAction = BossActions.IDLE;
        this.master = null;
        this.clones = new EntityWaterGuardian[2];
        this.setSize(1.0f, 2.0f);
        EntityExtension.For(this).setMagicLevelWithMana(10);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getWaterGuardianMaxHealth());
    }

    public void setClones(EntityWaterGuardian clone1, EntityWaterGuardian clone2) {
        if (this.clones[0] != null)
            this.clones[0].setDead();
        if (this.clones[1] != null)
            this.clones[1].setDead();
        this.clones[0] = clone1;
        this.clones[1] = clone2;
    }

    private boolean hasClones() {
        return this.clones[0] != null || this.clones[1] != null;
    }

    public void clearClones() {
        if (this.clones[0] != null) {
            this.clones[0].setDead();
        }
        if (this.clones[1] != null) {
            this.clones[1].setDead();
        }
        this.clones[0] = null;
        this.clones[1] = null;
    }

    private void enableUberAttack() {
        this.uberSpinAvailable = true;
    }

    public void setMaster(EntityWaterGuardian master) {
        this.dataManager.set(IS_CLONE, true);
        this.master = master;
    }

    public boolean isClone() {
        return this.dataManager.get(IS_CLONE);
    }

    public void clearMaster() {
        this.master = null;
    }

    @Override
    protected void initSpecificAI() {
        this.tasks.addTask(2, new EntityAIChaosWaterBolt(this));
        this.tasks.addTask(3, new EntityAICloneSelf(this));
        this.tasks.addTask(4, new EntityAICastSpell<>(this, NPCSpells.getInstance().waterBolt, 12, 23, 5, BossActions.CASTING));
        this.tasks.addTask(3, new EntityAISpinAttack(this, 0.5f, 4));
    }

    @Override
    public void onUpdate() {

        if (this.currentAction == BossActions.CASTING) {
            this.uberSpinAvailable = false;
        }

        if (!this.world.isRemote && this.uberSpinAvailable && this.currentAction != BossActions.IDLE) {
            this.setCurrentAction(BossActions.IDLE);
        }

        if (!this.world.isRemote && this.isClone() && (this.master == null || this.ticksExisted > 400)) {
            this.setDead();
        }

        if (this.world.isRemote) {
            this.updateRotations();
        }
        super.onUpdate();
    }

    @Override
    protected void entityInit() {
        super.entityInit();

        this.dataManager.register(IS_CLONE, false);
    }

    private void updateRotations() {
        if (!this.isClone())
            this.orbitRotation += 2f;
        else
            this.orbitRotation -= 2f;
        this.orbitRotation %= 360;

        if (this.getCurrentAction() == BossActions.SPINNING || this.getCurrentAction() == BossActions.CASTING) {
            this.spinRotation = (this.spinRotation - 30) % 360;
        }
    }

    public float getOrbitRotation() {
        return this.orbitRotation;
    }

    @Override
    public void setCurrentAction(BossActions action) {
        super.setCurrentAction(action);
        this.spinRotation = 0;

        if (!this.world.isRemote) {
            AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (par1DamageSource.getTrueSource() instanceof EntityWaterGuardian)
            return false;
        if (par1DamageSource.damageType.equals(DamageSource.OUT_OF_WORLD.damageType))
            return super.attackEntityFrom(par1DamageSource, par2);
        if (this.isClone() && this.master != null) {
            this.master.enableUberAttack();
            this.master.clearClones();
        } else if (this.hasClones()) {
            this.clearClones();
        }

        if (!this.isClone() && this.rand.nextInt(10) < 6) {
            this.world.playSound(this.posX, this.posY, this.posZ, this.getAmbientSound(), SoundCategory.HOSTILE, 1.0f, 0.4f + this.rand.nextFloat() * 0.6f, false);
            return false;
        }

        return super.attackEntityFrom(par1DamageSource, par2);
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        if (source instanceof DamageSourceLightning)
            damageAmt *= 2.0f;
        if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityWaterGuardian)
            damageAmt = 0;
        if (source instanceof DamageSourceFrost)
            damageAmt = 0;

        return damageAmt;
    }

    @Override
    public boolean isActionValid(BossActions action) {
        if (this.uberSpinAvailable && action != BossActions.CASTING) return false;
        if (action == BossActions.CASTING) {
            return this.uberSpinAvailable;
        }
        if (action == BossActions.CLONE) {
            return !this.isClone();
        }
        return true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
        super.writeEntityToNBT(par1nbtTagCompound);

        par1nbtTagCompound.setBoolean("isClone", this.isClone());
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getWaterGuardianArmor();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);

        this.dataManager.set(IS_CLONE, par1nbtTagCompound.getBoolean("isClone"));
    }


    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.WATER_GUARDIAN_LOOT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.WATER_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.WATER_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.WATER_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.WATER_GUARDIAN_ATTACK;
    }

    @Override
    protected Color getBarColor() {
        return Color.BLUE;
    }
}
