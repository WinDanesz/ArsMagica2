package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.DamageSources;
import am2.api.blocks.IMultiblock;
import am2.api.blocks.Multiblock;
import am2.api.blocks.MultiblockGroup;
import am2.api.blocks.TypedMultiblockGroup;
import am2.client.particles.AMLineArc;
import am2.common.entity.*;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMPotions;
import am2.common.extensions.EntityExtension;
import am2.common.utils.EntityUtils;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TileEntityBlackAurem extends TileEntityObelisk {

    private final HashMap<EntityLivingBase, AMLineArc> arcs;
    private final ArrayList<EntityLivingBase> cachedEntities;
    private int ticksSinceLastEntityScan = 0;

    @SuppressWarnings("unchecked")
    public TileEntityBlackAurem() {
        super(ArsMagica.config.getCapacityBlackAurem());

        arcs = new HashMap<>();

        cachedEntities = new ArrayList<EntityLivingBase>();

        structure = new Multiblock("blackaurem_structure");

        pillars = new MultiblockGroup("pillars", Lists.newArrayList(Blocks.NETHER_BRICK.getDefaultState()), false);

        this.caps = new HashMap<>();
        capsGroup = new TypedMultiblockGroup("caps", Lists.newArrayList(
                createMap(AMBlocks.sunstone_block.getDefaultState()),
                createMap(AMBlocks.chimerite_block.getDefaultState()),
                createMap(Blocks.OBSIDIAN.getDefaultState())), false);
        this.caps.put(AMBlocks.chimerite_block.getDefaultState(), 1.1f);
        this.caps.put(Blocks.OBSIDIAN.getDefaultState(), 1.5f);
        this.caps.put(AMBlocks.sunstone_block.getDefaultState(), 2f);

        MultiblockGroup aurem = new MultiblockGroup("aurem", Lists.newArrayList(AMBlocks.black_aurem.getDefaultState()), true);

        aurem.addBlock(BlockPos.ORIGIN);

        capsGroup.addBlock(new BlockPos(-2, 2, -2), 0);
        capsGroup.addBlock(new BlockPos(2, 2, -2), 0);
        capsGroup.addBlock(new BlockPos(-2, 2, 2), 0);
        capsGroup.addBlock(new BlockPos(2, 2, 2), 0);

        pillars.addBlock(new BlockPos(-2, 0, -2));
        pillars.addBlock(new BlockPos(-2, 1, -2));

        pillars.addBlock(new BlockPos(2, 0, -2));
        pillars.addBlock(new BlockPos(2, 1, -2));

        pillars.addBlock(new BlockPos(-2, 0, 2));
        pillars.addBlock(new BlockPos(-2, 1, 2));

        pillars.addBlock(new BlockPos(2, 0, 2));
        pillars.addBlock(new BlockPos(2, 1, 2));

        wizardChalkCircle = addWizChalkGroupToStructure(structure);
        structure.addGroup(pillars);
        structure.addGroup(capsGroup);
        structure.addGroup(wizardChalkCircle);
        structure.addGroup(aurem);
    }

    @Override
    public void update() {

        if (world.isRemote) {
            Iterator<EntityLivingBase> arcIterator = arcs.keySet().iterator();
            ArrayList<Entity> toRemove = new ArrayList<Entity>();
            while (arcIterator.hasNext()) {
                EntityLivingBase arcEnt = arcIterator.next();
                AMLineArc arc = (AMLineArc) arcs.get(arcEnt);
                if (arcEnt == null || arcEnt.isDead || arc == null || !arc.isAlive() || arcEnt.getDistanceSq(pos) > 100 || EntityUtils.isSummon(arcEnt))
                    toRemove.add(arcEnt);
            }

            for (Entity e : toRemove) {
                arcs.remove(e);
            }
        } else {
            surroundingCheckTicks++;
        }

        if (world.isRemote || ticksSinceLastEntityScan++ > 25) {
            updateNearbyEntities();
            ticksSinceLastEntityScan = 0;
        }

        Iterator<EntityLivingBase> it = cachedEntities.iterator();
        while (it.hasNext()) {

            EntityLivingBase ent = it.next();

            if (!ent.isPotionActive(AMPotions.astral_distortion))
                ent.addPotionEffect(new PotionEffect(AMPotions.astral_distortion, 600, 0));

            if (ent.isDead || ent.getDistanceSq(pos) > 100) {
                it.remove();
                continue;
            }

            RayTraceResult mop = this.world.rayTraceBlocks(new Vec3d(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5), new Vec3d(ent.posX, ent.posY + ent.getEyeHeight(), ent.posZ), false);

            if (EntityUtils.isSummon(ent) || mop != null) {
                continue;
            }

            ent.motionY = 0;
            ent.motionX = 0;
            ent.motionZ = 0;
            double deltaX = this.pos.getX() + 0.5f - ent.posX;
            double deltaZ = this.pos.getZ() + 0.5f - ent.posZ;
            double angle = Math.atan2(deltaZ, deltaX);

            double offsetX = Math.cos(angle) * 0.1;
            double offsetZ = Math.sin(angle) * 0.1;
            double offsetY = 0.05f;

            double distanceHorizontal = deltaX * deltaX + deltaZ * deltaZ;
            double distanceVertical = this.pos.getY() - ent.posY;
            if (distanceHorizontal < 1.3 * Math.max(1, Math.abs(distanceVertical / 2))) {
                if (distanceVertical < -1.5) {
                    if (world.isRemote && world.rand.nextInt(10) < 3) {
                        ArsMagica.proxy.particleManager.BoltFromPointToPoint(world, pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, ent.posX, ent.posY, ent.posZ, 4, 0x000000);
                    }
                }
                if (distanceVertical < -2) {
                    offsetY = 0;
                    if (!world.isRemote) {
                        if (ent.attackEntityFrom(DamageSources.darkNexus, 4)) {
                            if (ent.getHealth() <= 0) {
                                ent.setDead();
                                float power = ((int) Math.ceil((ent.getMaxHealth() * (ent.ticksExisted / 20)) % 5000)) * this.powerMultiplier;
                                PowerNodeRegistry.For(this.world).insertPower(this, PowerTypes.DARK, power);
                            }
                        }
                    }
                }
            }

            if (world.isRemote) {
                if (!arcs.containsKey(ent)) {
                    AMLineArc arc = (AMLineArc) ArsMagica.proxy.particleManager.spawn(world, ArsMagica.MODID + ":textures/blocks/sunstone_ore.png", pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, ent);
                    if (arc != null) {
                        arc.setExtendToTarget();
                        arc.setRBGColorF(1, 1, 1);
                    }
                    arcs.put(ent, arc);
                }
            }
            if (!world.isRemote)
                ent.move(MoverType.SELF, offsetX, offsetY, offsetZ);
        }
        if (surroundingCheckTicks % 100 == 0) {
            checkNearbyBlockState();
            surroundingCheckTicks = 1;
            if (!world.isRemote && PowerNodeRegistry.For(this.world).checkPower(this, this.capacity * 0.1f)) {
                List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.add(-2, 0, -2), pos.add(2, 3, 2)));
                for (EntityPlayer p : nearbyPlayers) {
                    if (p.isPotionActive(AMPotions.mana_regeneration)) continue;
                    p.addPotionEffect(new PotionEffect(AMPotions.mana_regeneration, 600, 2));
                }
            }

            if (!world.isRemote) {
                float charge = PowerNodeRegistry.For(this.world).getPower(this, PowerTypes.DARK);
                float chargeRatio = charge / this.getCapacity();
                if (world.rand.nextDouble() < chargeRatio * 0.01) {
                    // maxSev: 0=MINOR, 1=MODERATE, 2=SEVERE (matches original IllEffectSeverity ordinals)
                    int maxSev = (int) Math.ceil(chargeRatio * 2) + world.rand.nextInt(2);
                    applyDarkNexusEffect(maxSev);
                }
            }
        }

        super.callSuperUpdate();
    }

    private void updateNearbyEntities() {
        List<EntityLivingBase> nearbyEntities = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.add(-10, 0, -10), pos.add(10, 4, 10)));
        for (EntityLivingBase entity : nearbyEntities) {
            if (entity.isEntityInvulnerable(DamageSources.darkNexus) ||
                    !entity.isNonBoss() ||
                    entity instanceof EntityDarkling ||
                    entity instanceof EntityPlayer ||
                    entity instanceof EntityAirSled ||
                    entity instanceof EntityWinterGuardianArm ||
                    entity instanceof EntityThrownSickle ||
                    entity instanceof EntityFlicker ||
                    entity instanceof EntityShadowHelper ||
                    entity instanceof EntityThrownRock ||
                    entity instanceof EntityBroom
            )
                continue;
            if (!cachedEntities.contains(entity))
                cachedEntities.add(entity);
        }

        ticksSinceLastEntityScan = 0;
    }

    @Override
    public IMultiblock getMultiblockStructure() {
        return structure;
    }

    @Override
    public boolean canRequestPower() {
        return false;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return type == PowerTypes.DARK;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return Lists.newArrayList(PowerTypes.DARK);
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    // MINOR: drain all mana from nearby players
    private void effectDrainMana() {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos.add(-3, -3, -3), pos.add(3, 3, 3)));
        for (EntityPlayer player : players) {
            EntityExtension.For(player).setCurrentMana(0);
        }
    }

    // MODERATE: apply wither to all nearby players
    private void effectWither() {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos.add(-3, -3, -3), pos.add(3, 3, 3)));
        for (EntityPlayer player : players) {
            player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 100, 0));
        }
    }

    // MODERATE: deal 1 dark nexus damage to a single random nearby player
    private void effectSpark() {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos.add(-3, -3, -3), pos.add(3, 3, 3)));
        if (players.isEmpty()) return;
        EntityPlayer unlucky = players.get(world.rand.nextInt(players.size()));
        unlucky.attackEntityFrom(DamageSources.darkNexus, 1);
    }

    // SEVERE: deal 1 dark nexus damage to all nearby players
    private void effectSparkStorm() {
        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                new AxisAlignedBB(pos.add(-3, -3, -3), pos.add(3, 3, 3)));
        for (EntityPlayer player : players) {
            player.attackEntityFrom(DamageSources.darkNexus, 1);
        }
    }

    // Selects and applies a random dark nexus effect up to the given severity
    private void applyDarkNexusEffect(int maxSev) {
        List<Runnable> effects = new ArrayList<>();
        effects.add(this::effectDrainMana);
        if (maxSev >= 1) effects.add(this::effectWither);
        if (maxSev >= 1) effects.add(this::effectSpark);
        if (maxSev >= 2) effects.add(this::effectSparkStorm);
        effects.get(world.rand.nextInt(effects.size())).run();
    }
}
