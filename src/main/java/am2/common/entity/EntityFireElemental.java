package am2.common.entity;

import am2.ArsMagica;
import am2.api.sources.DamageSourceFire;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleApproachPoint;
import am2.common.entity.ai.EntityAIFireballAttack;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class EntityFireElemental extends EntityMob {

    private static final int cookRadius = 10;
    private int blockFireTimer = 0;

    private static final DataParameter<Integer> COOK_TARGET_ID = EntityDataManager.createKey(EntityFireElemental.class, DataSerializers.VARINT);

    public EntityFireElemental(World world) {
        super(world);
        setSize(0.6F, 1.8F);
        isImmuneToFire = true;
        EntityExtension.For(this).setMagicLevelWithMana(30);
        initAI();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source instanceof DamageSourceFire) return false;
        return super.attackEntityFrom(source, amount);
    }

    private void initAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIBreakDoor(this));
        this.tasks.addTask(2, new EntityAIFireballAttack(this, 1.0f, 1, 60));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0f, false));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0f));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getFireElementalMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getFireElementalAttackDamage());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(COOK_TARGET_ID, 0);
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getFireElementalArmor();
    }

    @Override
    public boolean isBurning() {
        return this.getAttackTarget() != null;
    }

    @Override
    protected boolean isValidLightLevel() {
        return true;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BLAZE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_BLAZE_HURT;
    }

    public int getEntityBrightnessForRender(float f) {
        return 0xf000f0;
    }

    public float getEntityBrightness(float f) {
        return 1.0F;
    }

    @Override
    public void onLivingUpdate() {
        if (isWet()) {
            this.attackEntityFrom(DamageSource.DROWN, 1);
        }
        if (!world.isRemote) {
            if (++blockFireTimer >= 60) {
                blockFireTimer = 0;
                if (rand.nextFloat() < 0.30f) {
                    BlockPos below = getPosition().down();
                    BlockPos firePos = getPosition();
                    if (!world.isAirBlock(below) && world.isAirBlock(firePos)) {
                        world.setBlockState(firePos, Blocks.FIRE.getDefaultState());
                    }
                }
            }
            if (ticksExisted % 20 == 0) {
                BlockPos pos = getPosition();
                boolean onFireOrLava = world.getBlockState(pos).getBlock() == Blocks.FIRE
                        || world.getBlockState(pos).getBlock() == Blocks.LAVA
                        || world.getBlockState(pos).getBlock() == Blocks.FLOWING_LAVA
                        || world.getBlockState(pos.down()).getBlock() == Blocks.FIRE
                        || world.getBlockState(pos.down()).getBlock() == Blocks.LAVA
                        || world.getBlockState(pos.down()).getBlock() == Blocks.FLOWING_LAVA;
                if (onFireOrLava) {
                    heal(0.5f);
                }
            }
        }
        super.onLivingUpdate();
    }

    @Override
    public void onUpdate() {
        int cookTargetID = dataManager.get(COOK_TARGET_ID);
        if (cookTargetID != 0) {
            List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().grow(cookRadius, cookRadius, cookRadius));
            EntityItem inanimate = null;
            for (EntityItem item : items) {
                if (item.getEntityId() == cookTargetID) {
                    inanimate = item;
                }
            }

            if (inanimate != null && world.isRemote) {
                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "fire", posX, posY + getEyeHeight(), posZ);
                if (effect != null) {
                    effect.setIgnoreMaxAge(true);
                    effect.AddParticleController(new ParticleApproachPoint(effect, inanimate.posX + (rand.nextFloat() - 0.5), inanimate.posY + (rand.nextFloat() - 0.5), inanimate.posZ + (rand.nextFloat() - 0.5), 0.1f, 0.1f, 1, false).setKillParticleOnFinish(true));
                }
            }
        }

        if (world.isRemote && rand.nextInt(100) > 75 && !isBurning())
            for (int i = 0; i < ArsMagica.config.getGFXLevel(); i++)
                world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, posX + (rand.nextDouble() - 0.5D) * width, posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5D) * width, 0.0D, 0.0D, 0.0D);
        super.onUpdate();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.FIRE_ELEMENTAL_LOOT;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        boolean hit = super.attackEntityAsMob(entity);
        if (hit) {
            entity.setFire(5);
        }
        return hit;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
            return false;
        if (world.provider.getDimension() != -1) {
            // In the overworld, only spawn inside or directly adjacent to lava
            BlockPos pos = getPosition();
            boolean nearLava = world.getBlockState(pos).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos).getBlock() == Blocks.FLOWING_LAVA
                    || world.getBlockState(pos.down()).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos.down()).getBlock() == Blocks.FLOWING_LAVA
                    || world.getBlockState(pos.north()).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos.south()).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos.east()).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos.west()).getBlock() == Blocks.LAVA
                    || world.getBlockState(pos.north()).getBlock() == Blocks.FLOWING_LAVA
                    || world.getBlockState(pos.south()).getBlock() == Blocks.FLOWING_LAVA
                    || world.getBlockState(pos.east()).getBlock() == Blocks.FLOWING_LAVA
                    || world.getBlockState(pos.west()).getBlock() == Blocks.FLOWING_LAVA;
            if (!nearLava) return false;
        }
        return super.getCanSpawnHere();
    }
}
