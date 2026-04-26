package am2.common.entity;

import am2.ArsMagica;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.AMParticleDefs;
import am2.client.particles.ParticleFadeOut;
import am2.client.particles.ParticleFloatUpward;
import am2.common.utils.DummyEntityPlayer;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * A flat, ground-level spell entity that applies effects to entities walking through it.
 * Inspired by Electroblob's Wizardry EntityDecay.
 */
public class EntitySpellPuddle extends Entity {

    private static final DataParameter<Optional<SpellData>> WATCHER_STACK =
            EntityDataManager.createKey(EntitySpellPuddle.class, SpellData.OPTIONAL_SPELL_DATA);
    private static final DataParameter<Float> WATCHER_RADIUS =
            EntityDataManager.createKey(EntitySpellPuddle.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> WATCHER_COLOR =
            EntityDataManager.createKey(EntitySpellPuddle.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> WATCHER_LIFETIME =
            EntityDataManager.createKey(EntitySpellPuddle.class, DataSerializers.VARINT);

    /** Total lifespan (ticks) – synced to client via DataWatcher; used by the renderer for fade. */
    public int lifetime;

    private int ticksToExist = 100;
    private int ticksBetweenApplications = 20;
    private int tickTimer = 0;

    private SpellData spellStack;
    private EntityPlayer dummycaster;

    public EntitySpellPuddle(World world) {
        super(world);
        this.setSize(0.5f, 0.1f);
        this.noClip = true;
    }

    // ------------------------------------------------------------------ setup

    public void setCasterAndStack(EntityLivingBase caster, SpellData spell) {
        this.spellStack = spell;
        this.dummycaster = DummyEntityPlayer.fromEntityLiving(caster);
        if (spell != null) {
            this.dataManager.set(WATCHER_STACK, Optional.fromNullable(spell));
            int color = spell.getColor(world, caster, null);
            if (color == -1) color = spell.getMainShift().getColor();
            // Manual color overrides for puddle visuals
            if (spell.getMainShift() == am2.common.registry.Affinities.fire)      color = 0xFF7700;
            if (spell.getMainShift() == am2.common.registry.Affinities.lightning) color = 0x4488FF;
            this.dataManager.set(WATCHER_COLOR, color);
        }
    }

    public void setRadius(float radius) {
        this.dataManager.set(WATCHER_RADIUS, radius);
        this.setSize(radius * 2f, 0.1f);
    }

    public void setTicksToExist(int ticks) {
        this.ticksToExist = ticks;
        this.lifetime = ticks;
        this.dataManager.set(WATCHER_LIFETIME, ticks);
    }

    public void setTicksBetweenApplications(int ticks) {
        this.ticksBetweenApplications = ticks;
    }

    // ---------------------------------------------------------------- getters (used by renderer)

    public float getRadius() {
        return this.dataManager.get(WATCHER_RADIUS);
    }

    public int getColor() {
        return this.dataManager.get(WATCHER_COLOR);
    }

    private SpellData getEffectStack() {
        Optional<SpellData> opt = this.dataManager.get(WATCHER_STACK);
        return opt.isPresent() ? opt.get() : null;
    }

    // --------------------------------------------------------------- lifecycle

    public int getLifetime() {
        return this.dataManager.get(WATCHER_LIFETIME);
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(WATCHER_STACK, Optional.absent());
        this.dataManager.register(WATCHER_RADIUS, 2f);
        this.dataManager.register(WATCHER_COLOR, 0xFFFFFF);
        this.dataManager.register(WATCHER_LIFETIME, 100);
    }

    private static final Random PARTICLE_RAND = new Random();

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            if (this.ticksExisted >= this.ticksToExist) {
                this.setDead();
                return;
            }
            // Snap to ground once on first tick to ensure correct placement
            if (this.ticksExisted == 1) {
                snapToGround();
            }
            applyEffects();
        } else {
            spawnAmbientParticles();
        }
    }

    /** Spawns ambient particles on the client, bursting/floating out of the puddle surface. */
    private void spawnAmbientParticles() {
        Optional<SpellData> opt = this.dataManager.get(WATCHER_STACK);
        if (!opt.isPresent()) return;
        SpellData sd = opt.get();

        // Throttle: every 3 ticks on full GFX, every 6 on medium, every 12 on low
        int interval = ArsMagica.config.FullGFX() ? 3 : ArsMagica.config.LowGFX() ? 12 : 6;
        if (this.ticksExisted % interval != 0) return;

        float radius = getRadius();
        int color = getColor();
        String particleName = AMParticleDefs.getParticleForAffinity(sd.getMainShift());

        int count = ArsMagica.config.FullGFX() ? 3 : ArsMagica.config.LowGFX() ? 1 : 2;
        for (int i = 0; i < count; i++) {
            // Random position within a circle
            double angle = PARTICLE_RAND.nextDouble() * Math.PI * 2;
            double dist  = PARTICLE_RAND.nextDouble() * radius;
            double px = posX + Math.cos(angle) * dist;
            double pz = posZ + Math.sin(angle) * dist;
            double py = posY + 0.05;

            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, particleName, px, py, pz);
            if (particle != null) {
                particle.setRGBColorI(color);
                particle.setIgnoreMaxAge(false);
                particle.setMaxAge(15 + PARTICLE_RAND.nextInt(20));
                particle.setParticleScale(0.12f + PARTICLE_RAND.nextFloat() * 0.08f);
                // Upward drift with a small random horizontal kick
                float upSpeed = 0.03f + PARTICLE_RAND.nextFloat() * 0.06f;
                particle.AddParticleController(new ParticleFloatUpward(particle, 0, upSpeed, 1, false));
                particle.AddParticleController(new ParticleFadeOut(particle, 2, false).setFadeSpeed(0.04f));
                particle.addVelocity(
                        (PARTICLE_RAND.nextDouble() - 0.5) * 0.04,
                        0,
                        (PARTICLE_RAND.nextDouble() - 0.5) * 0.04);
            }
        }
    }

    /** Moves the puddle down onto the topmost solid block beneath it. */
    private void snapToGround() {
        BlockPos below = new BlockPos(posX, posY - 0.1, posZ);
        // Walk downward until we find solid ground (max 4 blocks)
        for (int i = 0; i < 4; i++) {
            if (!world.isAirBlock(below) && world.getBlockState(below).isFullBlock()) {
                this.setPosition(posX, below.getY() + 1.0, posZ);
                return;
            }
            below = below.down();
        }
        // Walk upward if we started inside a block
        BlockPos above = new BlockPos(posX, posY, posZ);
        for (int i = 0; i < 4; i++) {
            if (world.isAirBlock(above)) {
                BlockPos groundBelow = above.down();
                if (!world.isAirBlock(groundBelow)) {
                    this.setPosition(posX, above.getY(), posZ);
                    return;
                }
            }
            above = above.up();
        }
    }

    /** Periodically apply spell components to entities within the puddle area. */
    private void applyEffects() {
        if (spellStack == null) {
            spellStack = getEffectStack();
            if (spellStack == null) {
                this.setDead();
                return;
            }
        }
        if (dummycaster == null) {
            dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
        }

        // Gravitate pull runs every 5 ticks for smooth, continuous attraction.
        if (spellStack != null && spellStack.isModifierPresent(SpellModifiers.GRAVITATE) && tickTimer % 5 == 0) {
            float snapRadius = getRadius();
            double pullStrength = spellStack.getModifiedValue(0, SpellModifiers.GRAVITATE, Operation.ADD, world, dummycaster, null);
            double pullRange = snapRadius + 2 + pullStrength * 4;
            AxisAlignedBB pullArea = new AxisAlignedBB(
                    posX - pullRange, posY - 2, posZ - pullRange,
                    posX + pullRange, posY + 3, posZ + pullRange);
            List<Entity> pullTargets = world.getEntitiesWithinAABB(Entity.class, pullArea);
            for (Entity e : pullTargets) {
                if (!(e instanceof EntityLivingBase)) continue;
                double dx = posX - e.posX;
                double dz = posZ - e.posZ;
                double distSq = dx * dx + dz * dz;
                if (distSq < 1.0) continue;
                double dist = Math.sqrt(distSq);
                // Attenuate with distance so far-away entities feel a gentle tug, not a teleport
                double attenuation = Math.min(1.0, 2.0 / dist);
                e.motionX += (dx / dist) * pullStrength * 0.08 * attenuation;
                e.motionZ += (dz / dist) * pullStrength * 0.08 * attenuation;
                e.velocityChanged = true;
            }
        }

        tickTimer++;
        if (tickTimer < ticksBetweenApplications) return;
        tickTimer = 0;

        float radius = getRadius();
        // Flat cylinder check: short Y range so only entities standing IN the puddle are hit
        AxisAlignedBB area = new AxisAlignedBB(
                posX - radius, posY - 0.5, posZ - radius,
                posX + radius, posY + 1.0, posZ + radius);

        List<Entity> targets = world.getEntitiesWithinAABB(Entity.class, area);
        for (Entity e : targets) {
            if (!(e instanceof EntityLivingBase)) continue;
            // Circular distance check (ignore Y)
            double dx = e.posX - posX;
            double dz = e.posZ - posZ;
            if (dx * dx + dz * dz > radius * radius) continue;
            // Y feet check: entity must be standing on or very close to the puddle surface
            if (e.posY - posY > 0.5) continue;
            spellStack.copy().applyComponentsToEntity(world, dummycaster, (EntityLivingBase) e);
        }
        // Also apply to the ground beneath
        spellStack.copy().applyComponentsToGround(world,
                dummycaster,
                new BlockPos(posX, posY - 1, posZ),
                EnumFacing.UP,
                posX, posY, posZ);

    }

    // ------------------------------------------------------------------- NBT

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        this.ticksToExist = compound.getInteger("TicksToExist");
        this.lifetime     = compound.getInteger("Lifetime");
        this.ticksBetweenApplications = compound.getInteger("TickRate");
        if (compound.hasKey("SpellData")) {
            SpellData sd = SpellData.readFromNBT(compound.getCompoundTag("SpellData"));
            if (sd != null) {
                this.spellStack = sd;
                this.dataManager.set(WATCHER_STACK, Optional.of(sd));
            }
        }
        this.dataManager.set(WATCHER_RADIUS, compound.getFloat("Radius"));
        this.dataManager.set(WATCHER_COLOR,  compound.getInteger("Color"));
        this.dataManager.set(WATCHER_LIFETIME, this.lifetime);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        compound.setInteger("TicksToExist", ticksToExist);
        compound.setInteger("Lifetime",     lifetime);
        compound.setInteger("TickRate",     ticksBetweenApplications);
        if (spellStack != null) {
            compound.setTag("SpellData", spellStack.writeToNBT(new NBTTagCompound()));
        }
        compound.setFloat  ("Radius", getRadius());
        compound.setInteger("Color",  getColor());
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }
}
