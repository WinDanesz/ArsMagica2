package am2.common.entity;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleOrbitEntity;
import am2.common.bosses.BossSpawnHelper;
import am2.common.registry.AMLoot;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityDryad extends EntityCreature {

    private static final DataParameter<Integer> SKIN_VARIANT = EntityDataManager.createKey(EntityDryad.class, DataSerializers.VARINT);
    public static final int NUM_VARIANTS = 3;

    private static final class DryadGroupData implements IEntityLivingData {
    }

    public int getSkinVariant() {
        return this.dataManager.get(SKIN_VARIANT);
    }

    private void setSkinVariant(int variant) {
        this.dataManager.set(SKIN_VARIANT, variant);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SKIN_VARIANT, 0);
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        // livingdata == null means this dryad is the leader of a freshly-formed spawn group
        // (the same signal vanilla wolves/zombies use for pack behaviour) - the right moment to
        // decide whether the whole group gets an escorting druid.
        boolean isGroupLeader = livingdata == null;
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        // Vanilla passes this result to the next pack member. Record the group even if the
        // guard roll or placement fails, so later dryads cannot retry the same group's chance.
        if (livingdata == null) {
            livingdata = new DryadGroupData();
        }
        this.setSkinVariant(this.rand.nextInt(NUM_VARIANTS));
        if (isGroupLeader && !this.world.isRemote) {
            trySpawnDruidGuard();
        }
        return livingdata;
    }

    private void trySpawnDruidGuard() {
        if (this.rand.nextInt(100) >= ArsMagica.config.getDruidGuardChance()) return;

        double searchRadius = ArsMagica.config.getDruidGuardSearchRadius();
        AxisAlignedBB searchArea = this.getEntityBoundingBox().grow(searchRadius);
        if (!this.world.getEntitiesWithinAABB(EntityDruid.class, searchArea).isEmpty()) return;

        for (int attempt = 0; attempt < 8; attempt++) {
            int dx = this.rand.nextInt(9) - 4;
            int dz = this.rand.nextInt(9) - 4;
            if (dx == 0 && dz == 0) continue;
            BlockPos ground = this.world.getHeight(this.getPosition().add(dx, 0, dz));

            EntityDruid druid = new EntityDruid(this.world);
            druid.setLocationAndAngles(ground.getX() + 0.5, ground.getY(), ground.getZ() + 0.5, this.rand.nextFloat() * 360.0F, 0.0F);
            if (druid.getCanSpawnHere()) {
                druid.onInitialSpawn(this.world.getDifficultyForLocation(ground), null);
                this.world.spawnEntity(druid);
                return;
            }
        }
    }

    public EntityDryad(World par1World) {
        super(par1World);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 0.68F));
        tasks.addTask(1, new EntityAITempt(this, 0.6D, Item.getItemFromBlock(Blocks.SAPLING), false));
        tasks.addTask(5, new EntityAIWander(this, 0.5F));
        tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6F));
        tasks.addTask(7, new EntityAILookIdle(this));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getDryadMaxHealth());
    }

    @Override
    public boolean canTriggerWalking() {
        return false;
    }


    @Override
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("SkinVariant", getSkinVariant());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readEntityFromNBT(par1NBTTagCompound);
        setSkinVariant(par1NBTTagCompound.getInteger("SkinVariant"));
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.DRYAD_LOOT;
    }

    @Override
    public void onUpdate() {
        World world = this.world;
        super.onUpdate();
        if (world == null) return;

        if (world.isRemote) {
            if (world.rand.nextInt(100) == 3) {
                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "hr_sparkles_1", this.posX, this.posY + 2, this.posZ);
                if (effect != null) {
                    effect.AddParticleController(new ParticleOrbitEntity(effect, this, world.rand.nextDouble() * 0.2 + 0.2, 1, false));
                    effect.setIgnoreMaxAge(false);
                    effect.setRGBColorF(0.1f, 0.8f, 0.1f);
                }
            }
        } else {
            int growthRate = ArsMagica.config.GetDryadPlantGrowthRate();
            if (this.ticksExisted % growthRate == 0) {
                tryGrowNearbyPlant(world);
            }
        }
    }

    private void tryGrowNearbyPlant(World world) {
        int radius = 8;
        BlockPos origin = this.getPosition();
        if (ArsMagica.config.GetDryadCropGrowthEnabled()) {
            for (int attempt = 0; attempt < 10; attempt++) {
                int dx = world.rand.nextInt(radius * 2 + 1) - radius;
                int dy = world.rand.nextInt(radius * 2 + 1) - radius;
                int dz = world.rand.nextInt(radius * 2 + 1) - radius;
                BlockPos pos = origin.add(dx, dy, dz);
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof IGrowable) {
                    IGrowable growable = (IGrowable) block;
                    if (growable.canGrow(world, pos, state, false) && growable.canUseBonemeal(world, world.rand, pos, state)) {
                        growable.grow(world, world.rand, pos, state);
                        return;
                    }
                }
            }
        }
        // No crops found - 33% chance to apply bonemeal on 1-3 nearby blocks
        if (ArsMagica.config.GetDryadBonemealEnabled() && world.rand.nextInt(3) == 0) {
            int count = 1 + world.rand.nextInt(3);
            for (int i = 0; i < count; i++) {
                int dx = world.rand.nextInt(radius * 2 + 1) - radius;
                int dy = world.rand.nextInt(radius * 2 + 1) - radius;
                int dz = world.rand.nextInt(radius * 2 + 1) - radius;
                BlockPos pos = origin.add(dx, dy, dz);
                ItemDye.applyBonemeal(ItemStack.EMPTY, world, pos);
            }
        }
    }

    @Override
    protected boolean canDespawn() {
        return ArsMagica.config.canDraydsDespawn();
    }

    @Override
    public void onDeath(DamageSource par1DamageSource) {
        if (par1DamageSource.getTrueSource() instanceof EntityPlayer) {
            BossSpawnHelper.instance.onDryadKilled(this);
        }
        super.onDeath(par1DamageSource);
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
            return false;
        return super.getCanSpawnHere();
    }

}
