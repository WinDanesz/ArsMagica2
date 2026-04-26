package am2.common.entity;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleFloatUpward;
import am2.common.entity.ai.EntityAIWaterElementalAttack;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityWaterElemental extends EntityMob {

    private float hostileSpeed;

    public EntityWaterElemental(World par1World) {
        super(par1World);
        this.hostileSpeed = 0.46F;
        initAI();
        EntityExtension.For(this).setCurrentLevel(5);
        EntityExtension.For(this).setCurrentMana(300);
    }

    @Override
    protected void initEntityAI() {
        // Moved AI initialization to proper method
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getWaterElementalMaxHealth());
    }

    private void initAI() {

        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(3, new EntityAIWaterElementalAttack(this, EntityPlayer.class, this.hostileSpeed, false));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.DROWN) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onUpdate() {
        if (this.world != null) {
            if (this.world.isRemote) {
                spawnLivingParticles();
            }
        }
        super.onUpdate();
    }

    @Override
    protected net.minecraft.util.ResourceLocation getLootTable() {
        return AMLoot.WATER_ELEMENTAL_LOOT;
    }

    private void spawnLivingParticles() {
        if (rand.nextBoolean()) {
            double yPos = this.posY + 1.1;
            AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "water_ball",
                    this.posX + ((rand.nextFloat() * 0.2) - 0.1f),
                    yPos,
                    this.posZ + ((rand.nextFloat() * 0.4) - 0.2f));
            if (effect != null) {
                effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, -0.06f, 1, false));
                effect.AddParticleController(new ParticleFadeOut(effect, 2, false).setFadeSpeed(0.04f));
                effect.setMaxAge(25);
                effect.setIgnoreMaxAge(false);
                effect.setParticleScale(0.1f);
            }
        }
    }

    /* Checks if this entity is inside water (if inWater field is true as a result of handleWaterMovement() returning
     * true)
     */
    @Override
    public boolean isInWater() {
        return this.world.handleMaterialAcceleration(this.getEntityBoundingBox().expand(0.0D, -0.6000000238418579D, 0.0D), Material.WATER, this);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
            return false;
        return super.getCanSpawnHere();
    }
}
