package am2.common.bosses;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.sources.DamageSourceFrost;
import am2.client.particles.*;
import am2.common.bosses.ai.*;
import am2.common.packet.AMNetHandler;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.World;

import java.util.List;

public class EntityFireGuardian extends AM2Boss {

    private boolean isUnderground = false;
    private int hitCount = 0;

    public EntityFireGuardian(World par1World) {
        super(par1World);
        this.setSize(1.0f, 4.0f);
        this.isImmuneToFire = true;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getFireGuardianMaxHealth());
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getFireGuardianArmor();
    }

    @Override
    protected void initSpecificAI() {
        this.tasks.addTask(1, new EntityAIFireRain(this));
        this.tasks.addTask(1, new EntityAIDispel(this));
        this.tasks.addTask(2, new EntityAIDive(this));
        this.tasks.addTask(2, new EntityAICastSpell<EntityFireGuardian>(this, NPCSpells.getInstance().meltArmor, 12, 23, 40, BossActions.CASTING));
        this.tasks.addTask(3, new EntityAIFlamethrower(this));
        this.tasks.addTask(4, new EntityAICastSpell<EntityFireGuardian>(this, NPCSpells.getInstance().fireBolt, 12, 23, 5, BossActions.CASTING));
    }

    public boolean getIsUnderground() {
        return this.isUnderground;
    }


    @Override
    public void setCurrentAction(BossActions action) {
        super.setCurrentAction(action);

        if (action == BossActions.SPINNING) {
            this.addVelocity(0, 1.5, 0);
        } else {
            hitCount = 0;
            isUnderground = false;
        }

        if (!world.isRemote) {
            AMNetHandler.INSTANCE.sendActionUpdateToAllAround(this);
        }
    }

    private void nova() {
        if (this.world.isRemote) {
            for (int i = 0; i < 36; ++i) {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "explosion_2", posX, posY - 3, posZ);
                if (particle != null) {
                    particle.AddParticleController(new ParticleMoveOnHeading(particle, i * 10, rand.nextInt(20) - 10, 0.2f, 1, false));
                    particle.AddParticleController(new ParticleLeaveParticleTrail(particle, "explosion_2", false, 10, 1, false)
                            .setTicksBetweenSpawns(1)
                            .setParticleRGB_F(1, 1, 1)
                            .addControllerToParticleList(new ParticleHoldPosition(particle, 10, 1, false)));
                    particle.setMaxAge(20);
                    particle.setParticleScale(0.5f);
                    particle.setIgnoreMaxAge(false);
                }
            }
        } else {
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5, 2.5, 2.5).expand(0, -3, 0));
            for (EntityLivingBase ent : entities) {
                if (ent == this)
                    continue;
                ent.attackEntityFrom(DamageSources.causeFireDamage(this), 5);
            }
        }
    }

    private void flamethrower() {
        Vec3d look = this.getLook(1.0f);
        if (world.isRemote) {
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "explosion_2", posX + (Math.cos(Math.toRadians(this.renderYawOffset + 90)) * 2), posY + 3, posZ + (Math.sin(Math.toRadians(this.renderYawOffset + 90)) * 2));
            if (particle != null) {
                particle.AddParticleController(new ParticleMoveOnHeading(particle, this.rotationYaw + 90 + rand.nextInt(20) - 10, rand.nextInt(20) - 10, 0.2f, 1, false));
                particle.setMaxAge(40);
                particle.setParticleScale(0.5f);
                particle.setIgnoreMaxAge(false);
            }
        } else {
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().grow(2.5, 2.5, 2.5).expand(look.x * 3, 0, look.z * 3));
            for (EntityLivingBase ent : entities) {
                if (ent == this)
                    continue;
                ent.attackEntityFrom(DamageSources.causeFireDamage(this), 5);
            }
        }
    }

    private void doFlameShield() {
        if (world.isRemote) {
            return;
        } else {
            for (Object p : world.playerEntities) {
                EntityPlayer player = (EntityPlayer) p;
                if (this.getDistanceSq(player) < 9) {
                    player.attackEntityFrom(DamageSources.causeFireDamage(this), 2);
                }
            }
        }
    }

    @Override
    public boolean isBurning() {
        return !isUnderground;
    }

    @Override
    public void onUpdate() {

        if (ticksInCurrentAction == 30 && currentAction == BossActions.SPINNING) {
            nova();
        }

        if (ticksInCurrentAction > 13 && currentAction == BossActions.LONG_CASTING) {
            if (this.getAttackTarget() != null)
                this.faceEntity(this.getAttackTarget(), 10, 10);
            flamethrower();
        }

        doFlameShield();

        super.onUpdate();
    }

    @Override
    public void fall(float par1, float par2) {
        if (this.getCurrentAction() == BossActions.SPINNING) {
            this.isUnderground = true;
            return;
        }
        super.fall(par1, par2);
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {

        if (par1DamageSource.isFireDamage()) {
            this.heal(par2);
            if (this.world.isRemote) {
                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle", posX, posY - 1, posZ);
                if (particle != null) {
                    particle.addRandomOffset(1, 1, 1);
                    particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.1f, 1, false));
                    particle.AddParticleController(new ParticleOrbitEntity(particle, this, 0.5f, 2, false).setIgnoreYCoordinate(true).SetTargetDistance(0.3f + rand.nextDouble() * 0.3));
                    particle.setMaxAge(20);
                    particle.setParticleScale(0.2f);
                    particle.setRGBColorF(0.1f, 1f, 0.1f);
                }
            }
            return false;
        }

        if (this.isUnderground && this.getCurrentAction() != BossActions.SPINNING) {
            return false;
        } else {
            if (this.getCurrentAction() == BossActions.SPINNING)
                ++hitCount;
        }

        return super.attackEntityFrom(par1DamageSource, par2);
    }

    @Override
    protected float modifyDamageAmount(DamageSource source, float damageAmt) {
        if (source == DamageSource.DROWN)
            damageAmt *= 2;
        else if (source instanceof DamageSourceFrost)
            damageAmt /= 3;
        return damageAmt;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.FIRE_GUARDIAN_HIT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.FIRE_GUARDIAN_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.FIRE_GUARDIAN_IDLE;
    }

    @Override
    public SoundEvent getAttackSound() {
        return AMSounds.FIRE_GUARDIAN_ATTACK;
    }

    public int getNumHits() {
        return this.hitCount;
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.FIRE_GUARDIAN_LOOT;
    }

    @Override
    protected Color getBarColor() {
        return Color.RED;
    }
}
