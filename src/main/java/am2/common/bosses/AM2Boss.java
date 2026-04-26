package am2.common.bosses;

import am2.ArsMagica;
import am2.common.entity.EntityLightMage;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;

public abstract class AM2Boss extends EntityMob implements IEntityMultiPart, IArsMagicaBoss {

    protected BossActions currentAction = BossActions.IDLE;
    protected int ticksInCurrentAction;
    protected EntityDragon[] parts;

    public boolean playerCanSee = false;
    private BossInfoServer bossInfo = null;

    /**
     * Counter for how many ticks the boss has been in a boxed/enclosed state
     */
    private int boxedTicks = 0;
    /**
     * Cooldown between anti-box actions to prevent constant block breaking
     */
    private int antiBoxCooldown = 0;

    public AM2Boss(World par1World) {
        super(par1World);
        if (par1World != null)
            this.bossInfo = new BossInfoServer(this.getDisplayName(), this.getBarColor(), BossInfo.Overlay.PROGRESS);
        this.stepHeight = 1.02f;
        EntityExtension.For(this).setMagicLevelWithMana(50);
        this.initAI();
    }

    //Bosses should be able to follow players through doors and hallways, so setSize is overridden to instead add a
    //damageable entity based bounding box of the specified size, unless a boss already uses parts.
    @Override
    public void setSize(float width, float height) {
//		if (this.parts == null) {
//			this.parts = new EntityDragon[]{new EntityDragon(this, "defaultBody", width, height) {
//
//				@Override
//				public void onUpdate() {
//					super.onUpdate();
//					this.isDead = ((Entity) this.entityDragonObj).isDead;
//				}
//
//				@Override
//				public boolean shouldRenderInPass(int pass) {
//					return false;
//				}
//			}};
//		} else {
        super.setSize(width, height);
//		} todo
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(48);
    }

    /**
     * This contains the default AI tasks.  To add new ones, override {@link #initSpecificAI()}
     */
    protected void initAI() {
        //TODO this.getNavigator().setBreakDoors(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityLightMage.class, true));

        this.initSpecificAI();
    }

    /**
     * Initializer for class-specific AI
     */
    protected abstract void initSpecificAI();

    protected abstract BossInfo.Color getBarColor();

    @Override
    public BossActions getCurrentAction() {
        return this.currentAction;
    }

    @Override
    public void setCurrentAction(BossActions action) {
        // Only reset tick counter if action actually changes
        if (this.currentAction != action) {
            this.currentAction = action;
            this.ticksInCurrentAction = 0;
        }
    }

    @Override
    public int getTicksInCurrentAction() {
        return this.ticksInCurrentAction;
    }

    @Override
    public void setTicksInCurrentAction(int ticks) {
        this.ticksInCurrentAction = ticks;
    }

    @Override
    public boolean isActionValid(BossActions action) {
        return true;
    }

    @Override
    public abstract SoundEvent getAttackSound();

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public Entity[] getParts() {
        return this.parts;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true; // Must be true for melee attacks to work (multi-part system is disabled)
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {

        if (par1DamageSource == DamageSource.IN_WALL) {
            if (!this.world.isRemote) {// dead code? (calling canSnowAt() without using the result) could it be a buggy upgrade to 1.7.10?
                for (int i = -1; i <= 1; ++i) {
                    for (int j = 0; j < 3; ++j) {
                        for (int k = -1; k <= 1; ++k) {
                            this.world.destroyBlock(this.getPosition().add(i, j, k), true);
                        }
                    }
                }
            }
            return false;
        }

        if (par1DamageSource.getTrueSource() != null) {

            if (par1DamageSource.getTrueSource() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) par1DamageSource.getTrueSource();
                if (player.capabilities.isCreativeMode && player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == AMItems.wooden_leg) {
                    if (!this.world.isRemote)
                        this.setDead();
                    return false;
                }
            } else if (par1DamageSource.getTrueSource() instanceof EntityArrow) {
                Entity shooter = ((EntityArrow) par1DamageSource.getTrueSource()).shootingEntity;
                if (shooter != null && this.getDistanceSq(shooter) > 900) {
                    this.setPositionAndUpdate(shooter.posX, shooter.posY, shooter.posZ);
                }
                return false;
            } else if (this.getDistanceSq(par1DamageSource.getTrueSource()) > 900) {
                Entity shooter = (par1DamageSource.getTrueSource());
                if (shooter != null) {
                    this.setPositionAndUpdate(shooter.posX, shooter.posY, shooter.posZ);
                }
            }
        }

        float maxDmg = ArsMagica.config.getBossMaxDamagePerHit();
        if (maxDmg > 0 && par2 > maxDmg && !par1DamageSource.damageType.equals(DamageSource.OUT_OF_WORLD.damageType)) par2 = maxDmg;

        par2 = this.modifyDamageAmount(par1DamageSource, par2);

        if (par2 <= 0) {
            this.heal(-par2);
            return false;
        }

        if (super.attackEntityFrom(par1DamageSource, par2)) {
            this.hurtResistantTime = ArsMagica.config.getBossHurtResistantTime();
            return true;
        }
        return false;
    }

    protected abstract float modifyDamageAmount(DamageSource source, float damageAmt);


    @Override
    public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float damage) {
        return false;
    }

    @Override
    public void onUpdate() {
// todo
//		if (this.parts != null && this.parts[0] != null && this.parts[0].partName == "defaultBody") {
//			this.parts[0].setPosition(this.posX, this.posY, this.posZ);
//			if (this.world.isRemote) {
//				this.parts[0].setVelocity(this.motionX, this.motionY, this.motionZ);
//			}
//			if (!this.parts[0].addedToChunk) {
//				this.world.spawnEntity(this.parts[0]);
//			}
//		}

        this.ticksInCurrentAction++;
        // Reset to IDLE after 200 ticks of any action (matches original 1.7.10 behavior)
        // This prevents action state from getting stuck but doesn't interfere with AI animations
        if (this.ticksInCurrentAction > 200) {
            this.setCurrentAction(BossActions.IDLE);
        }

        if (this.world.isRemote) {
            this.playerCanSee = ArsMagica.proxy.getLocalPlayer().canEntityBeSeen(this);
            this.ignoreFrustumCheck = ArsMagica.proxy.getLocalPlayer().getDistance(this) < 32;
        } else {
            // Anti-boxing mechanic - prevent players from trivializing fights by trapping bosses
            this.checkAndHandleBoxing();
        }

        if (this.bossInfo != null)
            this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());

        super.onUpdate();
    }

    /**
     * Checks if the boss is boxed in (surrounded by solid blocks) and takes action to escape.
     * This prevents players from trivializing boss fights by trapping them in small enclosures.
     */
    protected void checkAndHandleBoxing() {
        if (this.antiBoxCooldown > 0) {
            this.antiBoxCooldown--;
            return;
        }

        // Only check every 10 ticks for performance
        if (this.ticksExisted % 10 != 0) {
            return;
        }

        boolean enclosed = isEnclosed();

        // Also consider boxed if we have a target but can't path to them for a while
        boolean cantReachTarget = false;
        if (this.getAttackTarget() != null && this.getNavigator().noPath()) {
            // If we have a target, no path, and target is within 10 blocks, we're probably boxed
            double distSq = this.getDistanceSq(this.getAttackTarget());
            if (distSq < 100 && distSq > 4) { // Between 2 and 10 blocks
                cantReachTarget = true;
            }
        }

        if (enclosed || cantReachTarget) {
            this.boxedTicks += 10;

            // After being boxed for 1.5 seconds (30 ticks), start breaking out
            if (this.boxedTicks >= 30) {
                this.breakSurroundingBlocks();
                this.antiBoxCooldown = 15; // 0.75 second cooldown between break attempts

                // If still enclosed after breaking and has a target, teleport to target
                if (this.boxedTicks >= 60 && this.getAttackTarget() != null) {
                    this.teleportToTarget(this.getAttackTarget());
                    this.boxedTicks = 0;
                    this.antiBoxCooldown = 40; // 2 second cooldown after teleport
                }
            }
        } else {
            // Gradually reduce boxed ticks when not enclosed
            if (this.boxedTicks > 0) {
                this.boxedTicks = Math.max(0, this.boxedTicks - 5);
            }
        }
    }

    /**
     * Checks if the boss is enclosed by counting solid blocks on the walls/ceiling/floor
     * of a small area around the boss. Returns true if enclosed on most sides.
     */
    private boolean isEnclosed() {
        BlockPos bossPos = this.getPosition();
        int wallCount = 0;
        int openings = 0;

        // Check the 6 cardinal directions for walls (multiple blocks high for walls)
        // Check North wall (z-2)
        if (isWallSolid(bossPos, 0, -2)) wallCount++;
        else openings++;
        // Check South wall (z+2)
        if (isWallSolid(bossPos, 0, 2)) wallCount++;
        else openings++;
        // Check West wall (x-2)
        if (isWallSolid(bossPos, -2, 0)) wallCount++;
        else openings++;
        // Check East wall (x+2)
        if (isWallSolid(bossPos, 2, 0)) wallCount++;
        else openings++;

        // Check ceiling (need solid blocks above)
        boolean hasCeiling = false;
        for (int y = 2; y <= 4; y++) {
            if (isSolidAt(bossPos.add(0, y, 0))) {
                hasCeiling = true;
                break;
            }
        }
        if (hasCeiling) wallCount++;

        // Enclosed if 4+ sides are walled (4 walls + ceiling, or all 4 walls)
        return wallCount >= 4;
    }

    /**
     * Checks if there's a wall in the given direction (checks 3 blocks high)
     */
    private boolean isWallSolid(BlockPos bossPos, int xOffset, int zOffset) {
        int solidCount = 0;
        for (int y = 0; y <= 2; y++) {
            if (isSolidAt(bossPos.add(xOffset, y, zOffset))) {
                solidCount++;
            }
        }
        // Wall is solid if at least 2 of 3 blocks are solid
        return solidCount >= 2;
    }

    /**
     * Checks if a block position contains a solid, full cube block
     */
    private boolean isSolidAt(BlockPos pos) {
        IBlockState state = this.world.getBlockState(pos);
        return state.getMaterial().isSolid() && state.isFullCube();
    }

    /**
     * Breaks solid blocks around the boss to escape from boxing
     */
    protected void breakSurroundingBlocks() {
        BlockPos bossPos = this.getPosition();

        // Break blocks in a 3x3x3 area around the boss
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos breakPos = bossPos.add(x, y, z);
                    IBlockState state = this.world.getBlockState(breakPos);

                    // Only break solid full blocks, skip unbreakable blocks like bedrock
                    if (state.getMaterial().isSolid() && state.isFullCube()
                            && state.getBlockHardness(this.world, breakPos) >= 0) {
                        this.world.destroyBlock(breakPos, true);
                    }
                }
            }
        }
    }

    /**
     * Teleports the boss near its target as an escape mechanism
     */
    protected void teleportToTarget(EntityLivingBase target) {
        // Find a position near the target (within 4 blocks)
        for (int attempts = 0; attempts < 10; attempts++) {
            double offsetX = (this.rand.nextDouble() - 0.5) * 8;
            double offsetZ = (this.rand.nextDouble() - 0.5) * 8;
            double newX = target.posX + offsetX;
            double newY = target.posY;
            double newZ = target.posZ + offsetZ;

            BlockPos newPos = new BlockPos(newX, newY, newZ);

            // Check if the position is valid (not inside solid blocks)
            if (this.world.isAirBlock(newPos) && this.world.isAirBlock(newPos.up()) && this.world.isAirBlock(newPos.up(2))) {
                this.setPositionAndUpdate(newX, newY, newZ);
                // Play teleport sound effect
                this.world.playEvent(2003, newPos, 0); // Ender teleport particles
                return;
            }
        }

        // If no valid position found, just teleport directly to target
        this.setPositionAndUpdate(target.posX, target.posY, target.posZ);
    }

    @Override
    public boolean canBeLeashedTo(EntityPlayer player) {
        return false;
    }

    @Override
    public void addPotionEffect(PotionEffect effect) {
        if (effect.getPotion() == AMPotions.silence)
            return;
        super.addPotionEffect(effect);
    }

    @Override
    public World getWorld() {
        return this.getEntityWorld();
    }

    /**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
     * order to view its associated boss bar.
     */
    @Override
    public void addTrackingPlayer(EntityPlayerMP player) {
        super.addTrackingPlayer(player);
        if (this.bossInfo != null)
            this.bossInfo.addPlayer(player);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
     * more information on tracking.
     */
    @Override
    public void removeTrackingPlayer(EntityPlayerMP player) {
        super.removeTrackingPlayer(player);
        if (this.bossInfo != null)
            this.bossInfo.removePlayer(player);
    }
}
