package am2.common.entity;

import am2.ArsMagica;
import am2.api.sources.DamageSourceLightning;
import am2.common.entity.ai.EntityAIFlyingWander;
import am2.common.entity.ai.EntityAIRangedAttackSpell;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityLightningElemental extends EntityMob {

    public EntityLightningElemental(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        this.moveHelper = new EntityFlyHelper(this);
        EntityExtension.For(this).setMagicLevelWithMana(10 + this.rand.nextInt(10));
        initAI();
    }

    @Override
    protected PathNavigate createNavigator(World world) {
        // EntityFlyHelper drives motion directly; a standard navigator avoids
        // conflicts with the fly helper while still supporting combat pathfinding.
        return super.createNavigator(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    private void initAI() {
        this.tasks.addTask(2, new EntityAIRangedAttackSpell(this, 1.0f, 60, NPCSpells.getInstance().lightningElemental_attack));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0f, false));
        this.tasks.addTask(7, new EntityAIFlyingWander(this, 0.6, 10.0F));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, 0, true, false, null));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getLightningElementalMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getLightningElementalAttackDamage());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3);
        this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.5);
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getLightningElementalArmor();
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        if (!this.onGround && this.motionY < 0.0D) {
            this.motionY *= 0.6D;
        }
        super.onLivingUpdate();
    }

    @Override
    public int getBrightnessForRender() {
        return 0xF000F0; // always fully lit, like enderman eyes / blazes
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_LIGHTNING_THUNDER;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_LIGHTNING_THUNDER;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_LIGHTNING_THUNDER;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.LIGHTNING_BOLT) return false;
        if (source == DamageSource.FALL) return false;
        if (source instanceof DamageSourceLightning) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if (id == 2) { // hurt
            for (int i = 0; i < 20; i++) {
                double dx = rand.nextGaussian() * 0.35;
                double dy = rand.nextDouble() * this.height;
                double dz = rand.nextGaussian() * 0.35;
                world.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
                        posX + dx, posY + dy, posZ + dz,
                        dx * 2.5, 0.1, dz * 2.5);
            }
        }
        super.handleStatusUpdate(id);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
            return false;
        // Must spawn where it can see the sky (open air)
        return world.canSeeSky(getPosition()) && super.getCanSpawnHere();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.LIGHTNING_ELEMENTAL_LOOT;
    }
}
