package am2.common.entity;

import am2.ArsMagica;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMSounds;
import am2.common.trackers.CompendiumDiscoveryHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * A flying Book entity used in the Arcane Compendium discovery sequence.
 * <p>
 * Flies slowly to the pool center, hovers while enchant particles stream up
 * from the pool for 4 seconds, then transforms into the Arcane Compendium and drops.
 */
public class EntityFlyingBook extends Entity {

	/** Flight phases */
	public static final byte PHASE_FLYING = 0;
	public static final byte PHASE_ENCHANTING = 1;
	public static final byte PHASE_TRANSFORMING = 2;
	public static final byte PHASE_COMPLETE = 3;

	private static final DataParameter<Byte> PHASE = EntityDataManager.createKey(EntityFlyingBook.class, DataSerializers.BYTE);
	private static final DataParameter<Float> TARGET_X = EntityDataManager.createKey(EntityFlyingBook.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> TARGET_Y = EntityDataManager.createKey(EntityFlyingBook.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> TARGET_Z = EntityDataManager.createKey(EntityFlyingBook.class, DataSerializers.FLOAT);
	private static final DataParameter<Float> POOL_RADIUS = EntityDataManager.createKey(EntityFlyingBook.class, DataSerializers.FLOAT);

	private UUID ownerUUID;
	private int phaseTimer;
	/** Whether this entity has already safely dropped/given its item */
	private boolean itemResolved;

	/** Speed in blocks/tick — very slow, mystical drift */
	private static final double FLIGHT_SPEED = 0.04;
	private static final int ENCHANT_TICKS = 80;     // 4 seconds of enchant particles
	private static final int TRANSFORM_TICKS = 20;   // 1 second transform flash then drop
	/** Sound was played at enchant start */
	private boolean enchantSoundPlayed;

	public EntityFlyingBook(World worldIn) {
		super(worldIn);
		this.setSize(0.25f, 0.5f);
		this.noClip = true;
	}

	public EntityFlyingBook(World worldIn, double x, double y, double z,
						   double targetX, double targetY, double targetZ, float poolRadius, UUID owner) {
		this(worldIn);
		this.setPosition(x, y, z);
		this.dataManager.set(TARGET_X, (float) targetX);
		this.dataManager.set(TARGET_Y, (float) targetY);
		this.dataManager.set(TARGET_Z, (float) targetZ);
		this.dataManager.set(POOL_RADIUS, poolRadius);
		this.ownerUUID = owner;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(PHASE, PHASE_FLYING);
		this.dataManager.register(TARGET_X, 0f);
		this.dataManager.register(TARGET_Y, 0f);
		this.dataManager.register(TARGET_Z, 0f);
		this.dataManager.register(POOL_RADIUS, 1f);
	}

	// ---- Getters ----

	public byte getPhase() {
		return this.dataManager.get(PHASE);
	}

	public double getTargetX() { return this.dataManager.get(TARGET_X); }
	public double getTargetY() { return this.dataManager.get(TARGET_Y); }
	public double getTargetZ() { return this.dataManager.get(TARGET_Z); }
	public float getPoolRadius() { return this.dataManager.get(POOL_RADIUS); }

	// ---- Update Logic ----

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		// Safety: kill after 600 ticks (30 seconds) no matter what
		if (this.ticksExisted > 600) {
			dropBookSafety();
			this.setDead();
			return;
		}

		byte phase = getPhase();

		switch (phase) {
			case PHASE_FLYING:
				updateFlying();
				break;
			case PHASE_ENCHANTING:
				updateEnchanting();
				break;
			case PHASE_TRANSFORMING:
				updateTransforming();
				break;
			case PHASE_COMPLETE:
				break;
		}

		// Client-side particles
		if (this.world.isRemote && !ArsMagica.config.NoGFX()) {
			spawnFlightParticles();
		}
	}

	private void updateFlying() {
		// Target: pool center, 1 block above the surface
		double targetX = getTargetX();
		double targetY = getTargetY();
		double targetZ = getTargetZ();

		double dx = targetX - this.posX;
		double dy = targetY - this.posY;
		double dz = targetZ - this.posZ;
		double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (dist < 0.3) {
			// Arrived at pool center
			this.setPosition(targetX, targetY, targetZ);
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			phaseTimer = 0;
			this.dataManager.set(PHASE, PHASE_ENCHANTING);
			return;
		}

		// Move toward target
		double factor = FLIGHT_SPEED / dist;
		this.motionX = dx * factor;
		this.motionY = dy * factor;
		this.motionZ = dz * factor;

		this.posX += this.motionX;
		this.posY += this.motionY;
		this.posZ += this.motionZ;
		this.setPosition(this.posX, this.posY, this.posZ);
	}

	private void updateEnchanting() {
		phaseTimer++;

		// Gentle bob while hovering over the pool
		this.posY += Math.sin(phaseTimer * 0.15) * 0.003;
		this.setPosition(this.posX, this.posY, this.posZ);

		if (!this.world.isRemote) {
			// Play sound at start of enchanting phase
			if (!enchantSoundPlayed) {
				this.world.playSound(null, this.posX, this.posY, this.posZ,
						AMSounds.CAST_ARCANE, SoundCategory.AMBIENT, 1.0f, 0.8f);
				enchantSoundPlayed = true;
			}

			// Play looping sound periodically
			if (phaseTimer % 30 == 0) {
				this.world.playSound(null, this.posX, this.posY, this.posZ,
						AMSounds.LOOP_ARCANE, SoundCategory.AMBIENT, 0.6f, 1.0f);
			}

			if (phaseTimer >= ENCHANT_TICKS) {
				phaseTimer = 0;
				this.dataManager.set(PHASE, PHASE_TRANSFORMING);
			}
		}
	}

	private void updateTransforming() {
		phaseTimer++;

		if (!this.world.isRemote && phaseTimer >= TRANSFORM_TICKS) {
			completeTransformation();
		}
	}

	private void completeTransformation() {
		if (this.world.isRemote || itemResolved) return;

		itemResolved = true;
		this.dataManager.set(PHASE, PHASE_COMPLETE);

		// Spawn the Arcane Compendium as an item entity
		EntityItem compendiumItem = new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(AMItems.arcane_compendium));
		compendiumItem.motionX = 0;
		compendiumItem.motionY = 0.05;
		compendiumItem.motionZ = 0;
		compendiumItem.setPickupDelay(20);
		this.world.spawnEntity(compendiumItem);

		// Play completion sound
		this.world.playSound(null, this.posX, this.posY, this.posZ,
				AMSounds.CRAFTING_ALTAR_CREATE_SPELL, SoundCategory.AMBIENT, 1.0f, 1.2f);

		// Set the player's discovery flag and send message
		if (ownerUUID != null) {
			EntityPlayer owner = this.world.getPlayerEntityByUUID(ownerUUID);
			if (owner != null) {
				EntityExtension ext = EntityExtension.For(owner);
				if (ext != null) {
					ext.setHasDiscoveredCompendium(true);
				}
				owner.sendMessage(new TextComponentTranslation("arsmagica2.compendium.discovery.complete"));
			}
			CompendiumDiscoveryHandler.onSequenceComplete(ownerUUID);
		}

		this.setDead();
	}

	/** Drops a Book item at this entity's position (safety net for far-range or unexpected death). */
	private void dropBookItem() {
		if (this.world.isRemote || itemResolved) return;
		itemResolved = true;

		EntityItem bookItem = new EntityItem(this.world, this.posX, this.posY, this.posZ, new ItemStack(Items.BOOK));
		bookItem.motionX = 0;
		bookItem.motionY = 0.05;
		bookItem.motionZ = 0;
		bookItem.setPickupDelay(10);
		this.world.spawnEntity(bookItem);

		if (ownerUUID != null) {
			CompendiumDiscoveryHandler.onSequenceComplete(ownerUUID);
		}
	}

	/** Called when the entity dies unexpectedly — ensures the book is returned. */
	private void dropBookSafety() {
		if (this.world.isRemote || itemResolved) return;
		byte phase = getPhase();
		// Only drop a book if we haven't completed the transformation
		if (phase != PHASE_COMPLETE) {
			dropBookItem();
		}
	}

	@Override
	public void setDead() {
		dropBookSafety();
		super.setDead();
	}

	// ---- Client Particles ----

	private void spawnFlightParticles() {
		byte phase = getPhase();
		if (phase == PHASE_COMPLETE) return;

		if (phase == PHASE_FLYING && this.ticksExisted % 2 == 0) {
			// Trail particles during flight
			double offsetX = (this.world.rand.nextDouble() - 0.5) * 0.3;
			double offsetY = (this.world.rand.nextDouble() - 0.5) * 0.3;
			double offsetZ = (this.world.rand.nextDouble() - 0.5) * 0.3;

			am2.client.particles.AMParticle particle = (am2.client.particles.AMParticle)
					ArsMagica.proxy.particleManager.spawn(this.world, "arcane",
							this.posX + offsetX, this.posY + offsetY, this.posZ + offsetZ);
			if (particle != null) {
				particle.setMaxAge(15 + this.world.rand.nextInt(10));
				particle.setRGBColorF(0.8f + this.world.rand.nextFloat() * 0.2f,
						0.8f + this.world.rand.nextFloat() * 0.2f, 1.0f);
				particle.setRandomScale(0.03f, 0.08f);
				particle.AddParticleController(new am2.client.particles.ParticleFloatUpward(
						particle, 0, 0.02f, 1, false));
			}
			return;
		}
		int gfx = ArsMagica.config.getGFXLevel();
		if (phase == PHASE_ENCHANTING) {
			// Spawn white sparkle particles streaming from pool surface up to the book
			float poolRadius = getPoolRadius();
			double surfaceY = getTargetY() - 0.5; // liquid surface level
			final float itemRadius = 0.15F;

			for (int i = 0; i < 4 * gfx; i++) {
				float angle = 2F * this.world.rand.nextFloat() * (float)Math.PI;
				float ex = MathHelper.cos(angle);
				float ez = MathHelper.sin(angle);
				float dist = (0.2F + 0.8F * this.world.rand.nextFloat()) * poolRadius;

				double px = this.posX + ex * dist;
				double pz = this.posZ + ez * dist;
				double py = surfaceY + this.world.rand.nextDouble() * 0.1;

				am2.client.particles.AMParticle sparkle = (am2.client.particles.AMParticle)  ArsMagica.proxy.particleManager.spawn(this.world, "sparkle2", px, py, pz);
				if (sparkle != null) {
					sparkle.setIgnoreMaxAge(true);
					sparkle.setRGBColorF(1.0F, 1.0F, 1.0F);
					sparkle.setRandomScale(0.02F, 0.06F);
					sparkle.AddParticleController(new am2.client.particles.ParticleApproachPoint(
							sparkle, this.posX + ex * itemRadius, this.posY + 0.25 + 0.25 * rand.nextDouble(), this.posZ + ez * itemRadius,
							0.04F, itemRadius, 1, false)
							.setKillParticleOnFinish(true));
				}
			}
			return;
		}

		if (phase == PHASE_TRANSFORMING && this.ticksExisted % 2 == 0) {
			// Burst of arcane symbols during the brief transform
			for (int i = 0; i < gfx; i++) {
				double offsetX = -1.0 + 2.0 * this.world.rand.nextDouble();
				double offsetZ = -1.0 + 2.0 * this.world.rand.nextDouble();
				double offsetY = -0.25 + this.world.rand.nextDouble();

				am2.client.particles.AMParticle particle = (am2.client.particles.AMParticle)
						ArsMagica.proxy.particleManager.spawn(this.world, "symbols",
								this.posX + offsetX, this.posY + 0.5 * height + offsetY, this.posZ + offsetZ);
				if (particle != null) {
					particle.setIgnoreMaxAge(true);
					particle.AddParticleController(new am2.client.particles.ParticleApproachEntity(
							particle, this, 0.03f, 0.05f, 1, false).setKillParticleOnFinish(true));
					particle.setRandomScale(0.05f, 0.12f);
				}
			}
		}
	}

	// ---- NBT ----

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.phaseTimer = compound.getInteger("PhaseTimer");
		this.itemResolved = compound.getBoolean("ItemResolved");
		this.enchantSoundPlayed = compound.getBoolean("EnchantSoundPlayed");
		if (compound.hasKey("OwnerUUID")) {
			this.ownerUUID = UUID.fromString(compound.getString("OwnerUUID"));
		}
		this.dataManager.set(PHASE, compound.getByte("Phase"));
		if (compound.hasKey("TargetX")) {
			this.dataManager.set(TARGET_X, compound.getFloat("TargetX"));
			this.dataManager.set(TARGET_Y, compound.getFloat("TargetY"));
			this.dataManager.set(TARGET_Z, compound.getFloat("TargetZ"));
			this.dataManager.set(POOL_RADIUS, compound.getFloat("PoolRadius"));
		}

		// If loaded from disk mid-animation and not yet resolved, drop a book and die
		byte phase = getPhase();
		if (!itemResolved && phase != PHASE_COMPLETE) {
			// Will be handled in next onUpdate via the 600 tick limit or we force it now
			// Actually just set to die immediately — server restart mid-animation should return book
			if (!this.world.isRemote) {
				dropBookItem();
				this.setDead();
			}
		}
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("PhaseTimer", this.phaseTimer);
		compound.setBoolean("ItemResolved", this.itemResolved);
		compound.setBoolean("EnchantSoundPlayed", this.enchantSoundPlayed);
		if (this.ownerUUID != null) {
			compound.setString("OwnerUUID", this.ownerUUID.toString());
		}
		compound.setByte("Phase", getPhase());
		compound.setFloat("TargetX", this.dataManager.get(TARGET_X));
		compound.setFloat("TargetY", this.dataManager.get(TARGET_Y));
		compound.setFloat("TargetZ", this.dataManager.get(TARGET_Z));
		compound.setFloat("PoolRadius", this.dataManager.get(POOL_RADIUS));
	}

	// ---- Misc ----

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}
}
