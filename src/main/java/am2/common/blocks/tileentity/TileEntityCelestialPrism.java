package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.blocks.*;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMPotions;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;

public class TileEntityCelestialPrism extends TileEntityObelisk {

    private int particleCounter = 0;

    private boolean onlyChargeAtNight = false;


    @SuppressWarnings("unchecked")
    public TileEntityCelestialPrism() {
        super(ArsMagica.config.getCapacityCelestialPrism());

        powerBase = 1.0f;

        structure = new Multiblock("celestialprism_structure");

        capsGroup = new TypedMultiblockGroup("caps", Lists.newArrayList(
                createMap(Blocks.GLASS.getDefaultState()),
                createMap(Blocks.GOLD_BLOCK.getDefaultState()),
                createMap(Blocks.DIAMOND_BLOCK.getDefaultState()),
                createMap(AMBlocks.moonstone_block.getDefaultState())
        ), false);

        pillars = new MultiblockGroup("pillars", Lists.newArrayList(Blocks.QUARTZ_BLOCK.getDefaultState()), false);

        this.caps = new HashMap<IBlockState, Float>();
        this.caps.put(Blocks.GLASS.getDefaultState(), 1.1f);
        this.caps.put(Blocks.GOLD_BLOCK.getDefaultState(), 1.4f);
        this.caps.put(Blocks.DIAMOND_BLOCK.getDefaultState(), 2f);
        this.caps.put(AMBlocks.moonstone_block.getDefaultState(), 3f);

        MultiblockGroup prism = new MultiblockGroup("prism", Lists.newArrayList(AMBlocks.celestial_prism.getDefaultState()), true);
        prism.addBlock(BlockPos.ORIGIN);

        pillars.addBlock(new BlockPos(-2, 0, -2));
        pillars.addBlock(new BlockPos(-2, 1, -2));

        capsGroup.addBlock(new BlockPos(-2, 2, -2), 0);
        capsGroup.addBlock(new BlockPos(2, 2, -2), 0);
        capsGroup.addBlock(new BlockPos(-2, 2, 2), 0);
        capsGroup.addBlock(new BlockPos(2, 2, 2), 0);

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
        structure.addGroup(prism);
    }

    @Override
    protected void checkNearbyBlockState() {
        List<IMultiblockGroup> groups = structure.getMatchingGroups(world, pos);

        float capsLevel = 1;
        boolean pillarsFound = false;
        boolean wizChalkFound = false;
        boolean capsFound = false;

        for (IMultiblockGroup group : groups) {
            if (group == pillars)
                pillarsFound = true;
            else if (group == wizardChalkCircle)
                wizChalkFound = true;
            else if (group == capsGroup)
                capsFound = true;
        }

        if (pillarsFound && capsFound) {
            IBlockState capState = world.getBlockState(pos.add(2, 2, 2));

            for (IBlockState cap : caps.keySet()) {
                if (capState == cap) {
                    capsLevel = caps.get(cap);
                    if (cap.getBlock() == AMBlocks.moonstone_block)
                        onlyChargeAtNight = true;
                    else
                        onlyChargeAtNight = false;
                    break;
                }
            }
        }

        powerMultiplier = 1;

        if (wizChalkFound)
            powerMultiplier = 1.25f;

        if (pillarsFound)
            powerMultiplier *= capsLevel;
    }

    private boolean isNight() {
        long ticks = world.getWorldTime() % 24000;
        return ticks >= 12500 && ticks <= 23500;
    }

    @Override
    public void update() {

        if (surroundingCheckTicks++ % 100 == 0) {
            checkNearbyBlockState();
            surroundingCheckTicks = 1;
            if (!world.isRemote && PowerNodeRegistry.For(this.world).checkPower(this, this.capacity * 0.1f)) {
                List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.add(-2, 0, -2), pos.add(2, 3, 2)));
                for (EntityPlayer p : nearbyPlayers) {
                    if (p.isPotionActive(AMPotions.mana_regeneration)) continue;
                    p.addPotionEffect(new PotionEffect(AMPotions.mana_regeneration, 600, 0));
                }
            }
        }

        if (onlyChargeAtNight == isNight()) {
            PowerNodeRegistry.For(this.world).insertPower(this, PowerTypes.LIGHT, 0.25f * powerMultiplier);
            if (world.isRemote) {

                if (particleCounter++ % (ArsMagica.config.FullGFX() ? 60 : ArsMagica.config.NoGFX() ? 180 : 120) == 0) {
                    particleCounter = 1;
                    ArsMagica.proxy.particleManager.RibbonFromPointToPoint(world,
                            pos.getX() + world.rand.nextFloat(),
                            pos.getY() + (world.rand.nextFloat() * 2),
                            pos.getZ() + world.rand.nextFloat(),
                            pos.getX() + world.rand.nextFloat(),
                            pos.getY() + (world.rand.nextFloat() * 2),
                            pos.getZ() + world.rand.nextFloat());

                    // if (!ArsMagica.config.NoGFX()) {
                    //     double cx = pos.getX() + 0.5;
                    //     double cy = pos.getY() + 0.5;
                    //     double cz = pos.getZ() + 0.5;
                    //     for (int i = 0; i < 2; i++) {
                    //         AMParticle particle = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "symbols",
                    //                 cx, cy + world.rand.nextFloat() * 1.5, cz);
                    //         if (particle != null) {
                    //             particle.AddParticleController(new ParticleOrbitPoint(particle, cx, cy, cz, 1, false)
                    //                     .SetOrbitSpeed(0.025f + world.rand.nextFloat() * 0.015f)
                    //                     .SetTargetDistance(0.5 + world.rand.nextDouble() * 0.3));
                    //             particle.setMaxAge(150 + world.rand.nextInt(100));
                    //             particle.setParticleScale(0.12f);
                    //         }
                    //     }
                    // }
                }
            }
        }
        super.callSuperUpdate();
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
        return type == PowerTypes.LIGHT;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return Lists.newArrayList(PowerTypes.LIGHT);
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }
}
