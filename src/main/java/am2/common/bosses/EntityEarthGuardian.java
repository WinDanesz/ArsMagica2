package am2.common.bosses;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.sources.DamageSourceFrost;
import am2.api.sources.DamageSourceLightning;
import am2.common.bosses.ai.EntityAIDispel;
import am2.common.bosses.ai.EntityAISmash;
import am2.common.bosses.ai.EntityAIStrikeAttack;
import am2.common.bosses.ai.EntityAIThrowRock;
import am2.common.packet.AMNetHandler;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

public class EntityEarthGuardian extends AM2Boss {

    private float rodRotation = 0;
    public boolean leftArm = false;

    public EntityEarthGuardian(World par1World) {
        super(par1World);
        this.setSize(1.5f, 3.5f);
        this.stepHeight = 1.02f;
    }

    @Override
    protected void initSpecificAI() {
        this.tasks.addTask(1, new EntityAIDispel(this));
        this.tasks.addTask(1, new EntityAIThrowRock(this, 0.5f));
        this.tasks.addTask(2, new EntityAISmash(this, 0.5f, DamageSources.DamageSourceTypes.PHYSICAL));
        this.tasks.addTask(2, new EntityAIStrikeAttack(this, 0.5f, 4.0f, DamageSources.DamageSourceTypes.PHYSICAL));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getEarthGuardianMaxHealth());
    }

    public boolean shouldRenderRock() {
        return this.currentAction == BossActions.THROWING_ROCK && ticksInCurrentAction > 5 && ticksInCurrentAction < 27;
    }

    @Override
    public void setCurrentAction(BossActions action) {
        super.setCurrentAction(action);

        if (currentAction != action && action == BossActions.STRIKE && world.isRemote)
            this.leftArm = !this.leftArm;

        if (!world.isRemote) {
            AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
        }
    }

    public float getRodRotations() {
        return this.rodRotation;
    }

    @Override
    public void onUpdate() {
        if (ticksInCurrentAction > 40 && !world.isRemote) {
            setCurrentAction(BossActions.IDLE);
        }

        if (world.isRemote) {
            updateRotations();
        }

        super.onUpdate();
    }

    private void updateRotations() {
        this.rodRotation += 0.02f;
        this.rodRotation %= 360;
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getEarthGuardianArmor();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.EARTH_GUARDIAN_LOOT;
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        if (source instanceof DamageSourceFrost) {
            return damageAmt * 2;
        } else if (source instanceof DamageSourceLightning) {
            return damageAmt / 4;
        }
        return damageAmt;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.EARTH_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.EARTH_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.EARTH_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.EARTH_GUARDIAN_ATTACK;
    }

    @Override
    protected Color getBarColor() {
        return Color.YELLOW;
    }
}
