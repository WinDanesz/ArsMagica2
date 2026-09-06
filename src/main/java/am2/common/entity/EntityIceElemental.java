package am2.common.entity;

import am2.ArsMagica;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.registry.AMLoot;
import am2.common.registry.Affinities;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;
import java.util.List;

public class EntityIceElemental extends EntityElemental {

    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityIceElemental.class, DataSerializers.VARINT);
    private static final int NUM_VARIANTS = 4;

    /** Ticks since last environment effect (fire extinguish / water freeze / aura). */
    private int envTimer = 0;
    /** Radius for environmental effects (blocks). */
    private static final int ENV_RADIUS = 4;
    /** Aura slowness radius (blocks). */
    private static final double AURA_RADIUS = 3.0;

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    private void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public EntityIceElemental(World world) {
        super(world, Affinities.ice);
        setSize(0.6F, 1.8F);
        initAI();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
    }

    private void initAI() {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIBreakDoor(this));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 0.375f, false));
        this.tasks.addTask(7, new EntityAIWander(this, 0.375f));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        initAffinityTargeting(16.0D);
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        this.setVariant(this.rand.nextInt(NUM_VARIANTS));
        applyVariantAttributes();
        return livingdata;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("SkinVariant", getVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setVariant(compound.getInteger("SkinVariant"));
        applyVariantAttributes();
    }

    private void applyVariantAttributes() {
        // All variants share the same base stats
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getIceElementalMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getIceElementalAttackDamage());
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getIceElementalArmor();
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        boolean attacked = super.attackEntityAsMob(target);
        if (attacked && target instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) target;
            // Freeze on hit: use EBWiz frost effect if available, otherwise fall back to vanilla debuffs
            if (!EBWizardryCompatBootstrap.applyFrostEffect(living, 80, 1)) {
                living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,       80, 2, false, false));
                living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 80, 1, false, false));
            }
        }
        return attacked;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (world.isRemote) return;

        envTimer++;
        if (envTimer < 20) return;
        envTimer = 0;

        BlockPos center = getPosition();

        // Cold aura: frost/slowness to nearby players (no attack needed)
        List<EntityPlayer> nearby = world.getEntitiesWithinAABB(EntityPlayer.class,
                getEntityBoundingBox().grow(AURA_RADIUS));
        for (EntityPlayer player : nearby) {
            if (!player.isCreative()) {
                if (!EBWizardryCompatBootstrap.applyFrostEffect(player, 40, 0)) {
                    player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 40, 0, false, false));
                }
            }
        }

        // Environmental effects in a radius
        for (int dx = -ENV_RADIUS; dx <= ENV_RADIUS; dx++) {
            for (int dy = -1; dy <= 2; dy++) {
                for (int dz = -ENV_RADIUS; dz <= ENV_RADIUS; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    Block block = world.getBlockState(pos).getBlock();

                    // Extinguish fire blocks
                    if (block == Blocks.FIRE) {
                        world.setBlockToAir(pos);
                    }

                    // Freeze still water into frosted ice; leave flowing water alone
                    if (block == Blocks.WATER) {
                        world.setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState(), 3);
                    }
                }
            }
        }
    }

    @Override
    public void onDeath(DamageSource cause) {
        super.onDeath(cause);

        if (world.isRemote) return;

        // Shatter on death: burst of snowballs in a ring
        int shards = 12;
        for (int i = 0; i < shards; i++) {
            double angle = (2 * Math.PI / shards) * i;
            EntitySnowball shard = new EntitySnowball(world, this);
            shard.shoot(Math.cos(angle), 0.3 + rand.nextDouble() * 0.3, Math.sin(angle), 1.2f, 0f);
            world.spawnEntity(shard);
        }
        world.playSound(null, posX, posY, posZ, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.HOSTILE, 1.5f, 0.8f + rand.nextFloat() * 0.4f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLOCK_SNOW_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.DROWN) return false;
        // Ice elementals take double damage from fire
        if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE || source == DamageSource.LAVA) {
            amount *= 2.0f;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
            return false;
        Biome biome = world.getBiome(getPosition());
        boolean isCold = BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY);
        return isCold && super.getCanSpawnHere();
    }

    @Override
    protected ResourceLocation getLootTable() {
        switch (getVariant()) {
            case 0:
            case 1: return AMLoot.ICE_ELEMENTAL_LOOT;
            case 2: return AMLoot.ICE_ELEMENTAL_LOOT_PACKED;
            case 3: return AMLoot.ICE_ELEMENTAL_LOOT_BLUE;
            default: return AMLoot.ICE_ELEMENTAL_LOOT;
        }
    }
}
