package am2.common.entity;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.spell.Operation;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.*;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCastResult;
import am2.common.utils.AMLineSegment;
import am2.common.utils.DummyEntityPlayer;
import am2.common.utils.MathUtilities;
import am2.common.utils.SpellUtils;
import com.google.common.base.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntitySpellEffect extends Entity {

    private float rotation;
    private final float rotationSpeed;

    private int ticksToEffect = 20;
    private int maxTicksToEffect = 20;
    private int maxTicksToEffect_wall = 5;

    private int ticksToExist = 100;

    private SpellData spellStack;
    private EntityPlayer dummycaster;
    private int casterEntityID;
    private float moveSpeed;    //used by waves only

    private static final DataParameter<Optional<SpellData>> WATCHER_STACK = EntityDataManager.createKey(EntitySpellEffect.class, SpellData.OPTIONAL_SPELL_DATA);
    private static final DataParameter<Float> WATCHER_RADIUS = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> WATCHER_GRAVITY = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> WATCHER_TYPE = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.VARINT); //0 == zone, 1 == rain of fire, 2 == blizzard, 3 == wall, 4 == wave
    private static final DataParameter<Boolean> WATCHER_ROF_IGNITE = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> WATCHER_DAMAGEBONUS = EntityDataManager.createKey(EntitySpellEffect.class, DataSerializers.FLOAT);

    private static final int TYPE_ZONE = 0;
    private static final int TYPE_ROF = 1;
    private static final int TYPE_BLIZ = 2;
    private static final int TYPE_WALL = 3;
    private static final int TYPE_WAVE = 4;

    private boolean firstApply = true;

    public EntitySpellEffect(World par1World) {
        super(par1World);
        this.rotation = 0;
        this.rotationSpeed = 10f;
        this.setSize(0.25f, 0.25f);
    }

    public void SetCasterAndStack(EntityLivingBase caster, SpellData spellScroll) {
        this.spellStack = spellScroll;
        this.dummycaster = DummyEntityPlayer.fromEntityLiving(caster);
        casterEntityID = caster.getEntityId();
        if (spellStack != null)
            this.dataManager.set(WATCHER_STACK, Optional.fromNullable(spellStack));
    }

    public void setRadius(float newRadius) {
        this.dataManager.set(WATCHER_RADIUS, newRadius);
    }

    public void setTickRate(int newTickRate) {
        this.maxTicksToEffect = newTickRate;
    }

    public void setTicksToExist(int ticks) {
        this.ticksToExist = ticks;
    }

    public void setGravity(double gravity) {
        dataManager.set(WATCHER_GRAVITY, (float) gravity);
    }

    public void setDamageBonus(float damageBonus) {
        this.dataManager.set(WATCHER_DAMAGEBONUS, damageBonus);
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setWall(float rotation) {
        this.setRotation(rotation, 0);
        this.dataManager.set(WATCHER_TYPE, TYPE_WALL);
    }

    public void setWave(float rotation, float speed) {
        this.setRotation(rotation, 0);
        this.dataManager.set(WATCHER_TYPE, TYPE_WAVE);
        this.moveSpeed = speed;
        this.stepHeight = 0.6f;
        maxTicksToEffect_wall = 1;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(WATCHER_RADIUS, 3f);
        this.dataManager.register(WATCHER_STACK, Optional.absent());
        this.dataManager.register(WATCHER_GRAVITY, 0F);
        this.dataManager.register(WATCHER_TYPE, 0);
        this.dataManager.register(WATCHER_ROF_IGNITE, false);
        this.dataManager.register(WATCHER_DAMAGEBONUS, 1.0f);
    }

    @Override
    public void onUpdate() {

        if (dummycaster != null && dummycaster instanceof DummyEntityPlayer)
            dummycaster.onUpdate();

        switch (this.dataManager.get(WATCHER_TYPE)) {
            case TYPE_ZONE:
                zoneUpdate();
                break;
            case TYPE_ROF:
                rainOfFireUpdate();
                break;
            case TYPE_BLIZ:
                blizzardUpdate();
                break;
            case TYPE_WALL:
                wallUpdate();
                break;
            case TYPE_WAVE:
                waveUpdate();
                break;
        }

        if (!world.isRemote && this.ticksExisted >= this.ticksToExist) {
            this.setDead();
        }
    }

    @Override
    public void setDead() {
        if (dummycaster instanceof DummyEntityPlayer)
            dummycaster.setDead();
        super.setDead();
    }

    private void zoneUpdate() {
        if (this.world.isRemote) {
            if (!ArsMagica.config.NoGFX()) {
                this.rotation += this.rotationSpeed;
                this.rotation %= 360;

                double dist = getRadius();
                double _rotation = rotation;

                if (spellStack == null) {
                    spellStack = getEffectStack();
                    if (spellStack == null) {
                        return;
                    }
                }
                spellStack = spellStack.copy();

                int color = spellStack.getColor(world, null, null) & 0xFFFFFF;

                boolean isIceZone = Affinities.ice.equals(spellStack.getMainShift());
                if ((ArsMagica.config.FullGFX() && this.ticksExisted % 2 == 0) || this.ticksExisted % 8 == 0) {
                    for (int i = 0; i < 4; ++i) {
                        _rotation = (rotation + (90 * i)) % 360;
                        double x = this.posX - Math.cos(3.141 / 180 * (_rotation)) * dist;
                        double z = this.posZ - Math.sin(3.141 / 180 * (_rotation)) * dist;
                        if (!isIceZone || !EBWizardryCompatBootstrap.spawnFrostParticles(world, x, posY, z, 1, rand, 0.15, 0.05)) {
                            AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(spellStack.getMainShift()), x, posY, z);
                            if (effect != null) {
                                effect.setIgnoreMaxAge(false);
                                effect.setMaxAge(20);
                                effect.setParticleScale(0.15f);
                                effect.setRGBColorI(color);
                                effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
                                if (ArsMagica.config.LowGFX()) {
                                    effect.AddParticleController(new ParticleOrbitPoint(effect, posX, posY, posZ, 2, false).setIgnoreYCoordinate(true).SetOrbitSpeed(0.05f).SetTargetDistance(dist).setRotateDirection(true));
                                }
                            }
                        }
                    }
                }
            }
        }

        this.move(MoverType.SELF, 0, (float) this.dataManager.get(WATCHER_GRAVITY), 0);

        ticksToEffect--;
        if (spellStack == null) {
            if (!world.isRemote) {
                this.setDead();
            }
            return;
        }
        if (dummycaster == null) {
            dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
        }
        // Gravitate pull runs every 5 ticks for smooth, continuous attraction.
        if (spellStack != null && spellStack.isModifierPresent(SpellModifiers.GRAVITATE) && ticksToEffect % 5 == 0) {
            float snapRadius = this.dataManager.get(WATCHER_RADIUS);
            double pullStrength = spellStack.getModifiedValue(0, SpellModifiers.GRAVITATE, Operation.ADD, world, dummycaster, null);
            double pullRange = snapRadius + 2 + pullStrength * 4;
            AxisAlignedBB pullArea = new AxisAlignedBB(posX - pullRange, posY - 4, posZ - pullRange, posX + pullRange, posY + 4, posZ + pullRange);
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

        if (ticksToEffect <= 0) {
            ticksToEffect = maxTicksToEffect;
            float radius = this.dataManager.get(WATCHER_RADIUS);
            List<Entity> possibleTargets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posX - radius, posY - 3, posZ - radius, posX + radius, posY + 3, posZ + radius));
            for (Entity e : possibleTargets) {
                if (e instanceof EntityLivingBase)
                    spellStack.copy().execute(world, dummycaster, (EntityLivingBase) e, e.posX, e.posY - 1, e.posZ, null);
            }
            if (this.dataManager.get(WATCHER_GRAVITY) < 0 && !firstApply)
                spellStack.copy().execute(world, dummycaster, null, posX, posY - 1, posZ, null);
            else
                spellStack.copy().execute(world, dummycaster, null, posX, posY, posZ, null);
            firstApply = false;
            for (float i = -radius; i <= radius; i++) {
                for (int j = -3; j <= 3; j++) {
                    Vec3d[] blocks = getAllBlockLocationsBetween(new Vec3d(posX + i, posY + j, posZ - radius), new Vec3d(posX + i, posY + j, posZ + radius));
                    for (Vec3d vec : blocks) {
                        spellStack.pop().applyComponentsToGround(world, dummycaster, new BlockPos(vec), EnumFacing.UP, vec.x + 0.5, vec.y + 0.5, vec.z + 0.5);
                    }
                }
            }
        }
    }

    private void rainOfFireUpdate() {
        float radius = this.dataManager.get(WATCHER_RADIUS);
        if (world.isRemote) {

            if (spellStack == null) {
                spellStack = getEffectStack();
                if (spellStack == null) {
                    return;
                }
            }
            spellStack = spellStack.copy();

            int color = spellStack.getColor(world, null, null) & 0xFFFFFF;

            for (int i = 0; i < 10; ++i) {
                double x = this.posX - radius + (rand.nextDouble() * radius * 2);
                double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
                double y = this.posY + 10;

                AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "explosion_2", x, y, z);
                if (particle != null) {
                    particle.setMaxAge(20);
                    particle.addVelocity(rand.nextDouble() * 0.2f, 0, rand.nextDouble() * 0.2f);
                    particle.setAffectedByGravity();
                    particle.setDontRequireControllers();
                    particle.setRGBColorI(color);
                }
            }

            //TODO: SoundHelper.instance.loopSound(world, (float)posX, (float)posY, (float)posZ, "arsmagica2:spell.loop.fire", 1.0f);
        } else {
            List<Entity> possibleTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));
            for (Entity e : possibleTargets) {
                if (e != dummycaster) {

                    double lastVelX = e.motionX;
                    double lastVelY = e.motionY;
                    double lastVelZ = e.motionZ;

                    float damage = 0.75f * this.dataManager.get(WATCHER_DAMAGEBONUS);

                    if (SpellUtils.attackTargetSpecial(null, e, DamageSources.causeFireDamage(dummycaster), damage) && !(e instanceof EntityPlayer))
                        e.hurtResistantTime = 10;
                    e.addVelocity(-(e.motionX - lastVelX), -(e.motionY - lastVelY), -(e.motionZ - lastVelZ));
                }
            }
            if (canRoFIgnite() && rand.nextInt(10) < 2) {
                int pX = (int) (posX - radius + rand.nextInt((int) Math.ceil(radius) * 2));
                int pY = (int) posY;
                int pZ = (int) (posZ - radius + rand.nextInt((int) Math.ceil(radius) * 2));
                if (world.isAirBlock(new BlockPos(pX, pY, pZ)))
                    world.setBlockState(new BlockPos(pX, pY, pZ), Blocks.FIRE.getDefaultState());
            }

        }
    }

    private void blizzardUpdate() {
        float radius = this.dataManager.get(WATCHER_RADIUS);
        if (world.isRemote) {

            if (spellStack == null) {
                spellStack = getEffectStack();
                if (spellStack == null) {
                    return;
                }
            }
            spellStack = spellStack.copy();

            int color = spellStack.getColor(world, null, null) & 0xFFFFFF;

            for (int i = 0; i < 20; ++i) {
                double x = this.posX - radius + (rand.nextDouble() * radius * 2);
                double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
                double y = this.posY + 10;

                double vx = rand.nextDouble() * 0.2f - 0.1f;
                double vz = rand.nextDouble() * 0.2f - 0.1f;
                if (!EBWizardryCompatBootstrap.spawnFrostParticles(world, x, y, z, 1, rand)) {
                    AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "snowflakes", x, y, z);
                    if (particle != null) {
                        particle.setMaxAge(20);
                        particle.setParticleScale(0.1f);
                        particle.addVelocity(vx, 0, vz);
                        particle.setAffectedByGravity();
                        particle.setRGBColorI(color);
                        particle.setDontRequireControllers();
                    }
                }
            }

            double x = this.posX - radius + (rand.nextDouble() * radius * 2);
            double z = this.posZ - radius + (rand.nextDouble() * radius * 2);
            double y = this.posY + rand.nextDouble();
            AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "smoke", x, y, z);
            if (particle != null) {
                particle.setParticleScale(2.0f);
                particle.setMaxAge(20);
                //particle.setRGBColorF(0.5f, 0.92f, 0.92f);
                particle.setRGBColorF(0.5098f, 0.7843f, 0.7843f);
                particle.SetParticleAlpha(0.6f);
                particle.AddParticleController(new ParticleFleePoint(particle, new Vec3d(x, y, z), 0.1f, 3f, 1, false));
            }

            //TODO: SoundHelper.instance.loopSound(world, (float)posX, (float)posY, (float)posZ, "arsmagica2:spell.loop.air", 1.0f);
        } else {
            List<Entity> possibleTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));
            for (Entity e : possibleTargets) {
                if (e != dummycaster) {

                    if (e instanceof EntityLivingBase)
                        ((EntityLivingBase) e).addPotionEffect(new PotionEffect(AMPotions.frost_slow, 80, 3));

                    float damage = 1 * this.dataManager.get(WATCHER_DAMAGEBONUS);

                    double lastVelX = e.motionX;
                    double lastVelY = e.motionY;
                    double lastVelZ = e.motionZ;
                    if (SpellUtils.attackTargetSpecial(null, e, DamageSources.causeFrostDamage(dummycaster), damage) && !(e instanceof EntityPlayer))
                        e.hurtResistantTime = 15;
                    e.addVelocity(-(e.motionX - lastVelX), -(e.motionY - lastVelY), -(e.motionZ - lastVelZ));
                }
            }

            if (rand.nextInt(10) < 2) {
                int pX = (int) (posX - radius + rand.nextInt((int) Math.ceil(radius) * 2));
                int pY = (int) posY + rand.nextInt(2);
                int pZ = (int) (posZ - radius + rand.nextInt((int) Math.ceil(radius) * 2));
                BlockPos pos = new BlockPos(pX, pY, pZ);
                if (world.isAirBlock(pos) && !world.isAirBlock(pos.down()) && world.getBlockState(pos).isOpaqueCube())
                    world.setBlockState(pos, Blocks.SNOW.getDefaultState());
            }
        }
    }

    private void wallUpdate() {
        if (world.isRemote) {
            if (spellStack == null) {
                spellStack = getEffectStack();
                if (spellStack == null) {
                    return;
                }
            }
            spellStack = spellStack.copy();

            double dist = getRadius();

            int color = spellStack.getColor(world, null, null) & 0xFFFFFF;

            double px = Math.cos(3.141 / 180 * (rotationYaw + 90)) * 0.1f;
            double pz = Math.sin(3.141 / 180 * (rotationYaw + 90)) * 0.1f;
            double py = 0.1f;

            boolean isIceSweep = Affinities.ice.equals(spellStack.getMainShift());

            // Only spawn particles on certain ticks to avoid overwhelming batches
            if (!ArsMagica.config.NoGFX() && ((ArsMagica.config.FullGFX() && this.ticksExisted % 2 == 0) || this.ticksExisted % 4 == 0)) {
                // Spawn random particles along the wall
                int particlesToSpawn = 10 + rand.nextInt(8); // 10-17 particles per tick

                for (int p = 0; p < particlesToSpawn; p++) {
                    float i = (float) (rand.nextDouble() * dist);

                    double x = this.posX - Math.cos(3.141 / 180 * (rotationYaw)) * i;
                    double z = this.posZ - Math.sin(3.141 / 180 * (rotationYaw)) * i;

                    if (!isIceSweep || !EBWizardryCompatBootstrap.spawnFrostParticles(world, x, posY, z, 1, rand, 0)) {
                        AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(spellStack.getMainShift()), x, posY, z);
                        if (effect != null) {
                            effect.setIgnoreMaxAge(false);
                            effect.setMaxAge(15 + rand.nextInt(10));
                            effect.addRandomOffset(1, 1, 1);
                            effect.setParticleScale(0.15f);
                            effect.setRGBColorI(color);
                            if (dataManager.get(WATCHER_TYPE) == TYPE_WALL) {
                                effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
                            } else {
                                effect.setAffectedByGravity();
                                effect.setDontRequireControllers();
                                effect.addVelocity(px, py, pz);
                            }
                        }
                    }

                    x = this.posX - Math.cos(Math.toRadians(rotationYaw)) * -i;
                    z = this.posZ - Math.sin(Math.toRadians(rotationYaw)) * -i;

                    if (!isIceSweep || !EBWizardryCompatBootstrap.spawnFrostParticles(world, x, posY, z, 1, rand, 0)) {
                        AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, AMParticleDefs.getParticleForAffinity(spellStack.getMainShift()), x, posY, z);
                        if (effect != null) {
                            effect.setIgnoreMaxAge(false);
                            effect.addRandomOffset(1, 1, 1);
                            effect.setMaxAge(15 + rand.nextInt(10));
                            effect.setParticleScale(0.15f);
                            effect.setRGBColorI(color);
                            if (dataManager.get(WATCHER_TYPE) == TYPE_WALL) {
                                effect.AddParticleController(new ParticleFloatUpward(effect, 0, 0.07f, 1, false));
                            } else {
                                effect.setAffectedByGravity();
                                effect.setDontRequireControllers();
                                effect.addVelocity(px, py, pz);
                            }
                        }
                    }
                }
            }

        } else {

            ticksToEffect--;
            if (spellStack == null) {
                if (!world.isRemote) {
                    this.setDead();
                }
                return;
            }

            if (dummycaster == null) {
                dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
            }
            if (ticksToEffect <= 0) {
                ticksToEffect = maxTicksToEffect_wall;
                float radius = this.dataManager.get(WATCHER_RADIUS);
                List<Entity> possibleTargets = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posX - radius, posY - 1, posZ - radius, posX + radius, posY + 3, posZ + radius));

                for (Entity e : possibleTargets) {
                    if (e == this || e == dummycaster || e.getEntityId() == casterEntityID) continue;

                    Vec3d target = new Vec3d(e.posX, e.posY, e.posZ);

                    double dirX = Math.cos(3.141 / 180 * (rotationYaw));
                    double dirZ = Math.sin(3.141 / 180 * (rotationYaw));

                    Vec3d a = new Vec3d(this.posX - dirX * radius, this.posY, this.posZ - dirZ * radius);
                    Vec3d b = new Vec3d(this.posX - dirX * -radius, this.posY, this.posZ - dirZ * -radius);

                    Vec3d closest = new AMLineSegment(a, b).closestPointOnLine(target);

                    closest = new Vec3d(closest.x, 0, closest.z);
                    target = new Vec3d(target.x, 0, target.z);

                    double hDistance = closest.distanceTo(target);
                    double vDistance = Math.abs(this.posY - e.posY);

                    if (e instanceof EntityLivingBase && hDistance < 1.5f && vDistance < 2) {
                        SpellData copy = spellStack.copy();
                        //Execute the next stage of the spell (e.g., Touch shape)
                        SpellCastResult result = copy.execute(world, dummycaster, (EntityLivingBase) e, e.posX, e.posY, e.posZ, null);
                    }
                }
            }
        }
    }

    private void waveUpdate() {
        ticksToEffect = 0;
        wallUpdate();
        double dx = Math.cos(Math.toRadians(this.rotationYaw + 90));
        double dz = Math.sin(Math.toRadians(this.rotationYaw + 90));

        this.move(MoverType.SELF, dx * moveSpeed, 0, dz * moveSpeed);

        double dxH = Math.cos(Math.toRadians(this.rotationYaw));
        double dzH = Math.sin(Math.toRadians(this.rotationYaw));

        float radius = this.dataManager.get(WATCHER_RADIUS);

        if (dummycaster == null) {
            dummycaster = DummyEntityPlayer.fromEntityLiving(new EntityDummyCaster(world));
        }

        for (int j = 0; j <= 2; j++) {
            Vec3d a = new Vec3d((this.posX + dx) - dxH * radius, this.posY + j, (this.posZ + dz) - dzH * radius);
            Vec3d b = new Vec3d((this.posX + dx) - dxH * -radius, this.posY + j, (this.posZ + dz) - dzH * -radius);

            Vec3d[] vecs = getAllBlockLocationsBetween(a, b);
            for (Vec3d vec : vecs) {
                spellStack.copy().pop().applyComponentsToGround(world, dummycaster, new BlockPos(vec), EnumFacing.UP, vec.x + 0.5, vec.y + 0.5, vec.z + 0.5);
            }
        }

    }

    private Vec3d[] getAllBlockLocationsBetween(Vec3d a, Vec3d b) {
        a = MathUtilities.floorToI(a);
        b = MathUtilities.floorToI(b);

        double stepX = a.x < b.x ? 0.2f : -0.2f;
        double stepZ = a.z < b.z ? 0.2f : -0.2f;
        ArrayList<Vec3d> vecList = new ArrayList<Vec3d>();
        Vec3d curPos = new Vec3d(a.x, a.y, a.z);
        for (int i = 0; i < this.height; ++i) {
            vecList.add(new Vec3d(curPos.x, curPos.y + i, curPos.z));
        }

        while (stepX != 0 || stepZ != 0) {
            if ((stepX < 0 && curPos.x <= b.x) || (stepX > 0 && curPos.x >= b.x))
                stepX = 0;
            if ((stepZ < 0 && curPos.z <= b.z) || (stepZ > 0 && curPos.z >= b.z))
                stepZ = 0;
            curPos = new Vec3d(curPos.x + stepX, curPos.y, curPos.z + stepZ);
            Vec3d tempPos = new Vec3d(curPos.x, curPos.y, curPos.z);
            tempPos = MathUtilities.roundToI(tempPos);
            if (!vecList.contains(tempPos)) {
                for (int i = 0; i < this.height; ++i) {
                    vecList.add(new Vec3d(tempPos.x, tempPos.y + i, tempPos.z));
                }
            }
        }

        return vecList.toArray(new Vec3d[vecList.size()]);
    }

    @Override
    public void onEntityUpdate() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound var1) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound var1) {
    }

    public void setRainOfFire(boolean ignite) {
        this.dataManager.set(WATCHER_TYPE, TYPE_ROF);
        if (ignite)
            this.dataManager.set(WATCHER_ROF_IGNITE, true);
    }

    public void setBlizzard() {
        this.dataManager.set(WATCHER_TYPE, TYPE_BLIZ);
    }

    public boolean isBlizzard() {
        return this.dataManager.get(WATCHER_TYPE) == TYPE_BLIZ;
    }

    public boolean isRainOfFire() {
        return this.dataManager.get(WATCHER_TYPE) == TYPE_ROF;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    public double getRadius() {
        return (double) dataManager.get(WATCHER_RADIUS);
    }

    public SpellData getEffectStack() {
        return dataManager.get(WATCHER_STACK).orNull();
    }

    public int getType() {
        return dataManager.get(WATCHER_TYPE);
    }

    public double getGravity() {
        return (double) dataManager.get(WATCHER_GRAVITY);
    }

    public float getBonusDamage() {
        return dataManager.get(WATCHER_DAMAGEBONUS);
    }

    public boolean canRoFIgnite() {
        return dataManager.get(WATCHER_ROF_IGNITE);
    }
}
