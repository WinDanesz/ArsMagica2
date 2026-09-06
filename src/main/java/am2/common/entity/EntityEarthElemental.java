package am2.common.entity;

import am2.ArsMagica;
import am2.api.sources.DamageSourceWind;
import am2.common.registry.AMLoot;
import am2.common.registry.Affinities;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;

public class EntityEarthElemental extends EntityElemental {

    private static final DataParameter<Boolean> STRANGLING = EntityDataManager.createKey(EntityEarthElemental.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityEarthElemental.class, DataSerializers.VARINT);
    private static final int NUM_VARIANTS = 8;

    private int strangleTimer = 0;

    public int getVariant() {
        return this.dataManager.get(VARIANT);
    }

    private void setVariant(int variant) {
        this.dataManager.set(VARIANT, variant);
    }

    public boolean isStrangling() {
        return this.dataManager.get(STRANGLING);
    }

    private void setStrangling(boolean value) {
        this.dataManager.set(STRANGLING, value);
    }

    public EntityEarthElemental(World world) {
        super(world, Affinities.earth);
        setSize(0.6F, 1.8F);
        initAI();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(STRANGLING, false);
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
        int v = getVariant();
        if (v == 6 || v == 7) {
            this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getEarthElementalVariantMaxHealth());
            this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getEarthElementalVariantAttackDamage());
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getEarthElementalMaxHealth());
        this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.75);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(ArsMagica.config.getEarthElementalAttackDamage());
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getEarthElementalArmor();
    }

    protected float MovementSpeed() {
        return 0.4f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_IRONGOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ENTITY_IRONGOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRONGOLEM_DEATH;
    }

//	@Override
//	public boolean attackEntityFrom(DamageSource source, float amount) {
//		if (amount < 1.5f && entity.getEntityBoundingBox().maxY >= getEntityBoundingBox().minY && entity.getEntityBoundingBox().minY <= getEntityBoundingBox().maxY){
//			if (onGround){
//				entity.attackEntityFrom(DamageSource.causeMobDamage(this), 1);
//			}
//		}		
//	}

    @Override
    public void onUpdate() {
        super.onUpdate();
        EntityPlayer hugged = world.getClosestPlayerToEntity(this, 1.5);
        if (hugged != null && !hugged.isCreative() && hugged == this.getAttackTarget()) {
            // Clamp to the player's side on both server and client for smooth movement
            double dx = hugged.posX - this.posX;
            double dz = hugged.posZ - this.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.01) {
                double targetX = hugged.posX - (dx / dist) * 0.6;
                double targetZ = hugged.posZ - (dz / dist) * 0.6;
                this.setPosition(targetX, hugged.posY, targetZ);
                float yaw = (float)(Math.atan2(-dx, dz) * (180.0 / Math.PI));
                this.rotationYaw = yaw;
                this.renderYawOffset = yaw;
            }
            this.motionX = 0;
            this.motionY = 0;
            this.motionZ = 0;
            if (!world.isRemote) {
                setStrangling(true);
                hugged.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 5, 3, false, false));
                strangleTimer++;
                if (strangleTimer >= 20) {
                    strangleTimer = 0;
                    hugged.attackEntityFrom(DamageSource.causeMobDamage(this), 2.0f);
                }
            }
        } else {
            if (!world.isRemote) {
                setStrangling(false);
                strangleTimer = 0;
            }
        }
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.DROWN) return false;
        if (source instanceof DamageSourceWind) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(getPosition(), world, this))
            return false;
        Biome biome = world.getBiome(getPosition());
        boolean isHillsMountain = BiomeDictionary.hasType(biome, BiomeDictionary.Type.HILLS)
                || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN);
        if (!isHillsMountain && ArsMagica.config.GetEarthElementalSpawnUnderground()) {
            if (world.canSeeSky(getPosition())) return false;
        }
        return super.getCanSpawnHere();
    }

    @Override
    protected ResourceLocation getLootTable() {
        switch (getVariant()) {
            case 0: case 1: return AMLoot.EARTH_ELEMENTAL_LOOT_STONE;
            case 2: case 3: return AMLoot.EARTH_ELEMENTAL_LOOT_MOSSY;
            case 4:         return AMLoot.EARTH_ELEMENTAL_LOOT_FLOWER;
            case 5:         return AMLoot.EARTH_ELEMENTAL_LOOT_GRANITE;
            case 6:         return AMLoot.EARTH_ELEMENTAL_LOOT_DIAMOND;
            case 7:         return AMLoot.EARTH_ELEMENTAL_LOOT_EMERALD;
            default:        return AMLoot.EARTH_ELEMENTAL_LOOT;
        }
    }
}
