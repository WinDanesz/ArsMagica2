package am2.common.registry;

import am2.ArsMagica;
import am2.common.blocks.*;
import am2.common.blocks.tileentity.*;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumRarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public final class AMBlocks {

    public static final Block mana_battery = placeholder();
    public static final Block frost = placeholder();
    public static final Block occulus = placeholder();
    public static final Block magic_wall = placeholder();
    public static final Block invisible_light = placeholder();
    public static final Block invisible_utility = placeholder();

    public static final Block vinteum_ore = new BlockAMOre(); // redstone
    public static final Block chimerite_ore = new BlockAMOre(1); // lapis
    public static final Block blue_topaz_ore = new BlockAMOre(0); // quartz
    public static final Block moonstone_ore = new BlockAMOre(); // space emerald
    public static final Block sunstone_ore = new BlockAMOre(3).setHardness(25.0F).setResistance(1000.0F); // hell emerald

    public static final Block vinteum_block = placeholder();
    public static final Block chimerite_block = placeholder();
    public static final Block blue_topaz_block = placeholder();
    public static final Block moonstone_block = placeholder();
    public static final Block sunstone_block = placeholder();

    public static final Block block_mage_light = placeholder();

    public static final BlockDesertNova desert_nova = new BlockDesertNova();
    public static final BlockAMFlower cerublossom = new BlockAMFlower();
    public static final BlockWakebloom wakebloom = new BlockWakebloom();
    public static final BlockAMFlower aum = new BlockAMFlower();
    public static final BlockTarmaRoot tarma_root = new BlockTarmaRoot();

    public static final Block crafting_altar = placeholder();
    public static final Block wizard_chalk = placeholder();
    public static final Block chalk_arrow = placeholder();
    public static final Block obelisk = placeholder();
    public static final Block black_aurem = placeholder();
    public static final Block celestial_prism = placeholder();
    public static final Block crystal_marker = placeholder();
    public static final Block warding_candle = placeholder();
    public static final Block lectern = placeholder();
    public static final Block inscription_table = placeholder();
    public static final Block armor_imbuer = placeholder();
    public static final Block slipstream_generator = placeholder();
    public static final Block essence_conduit = placeholder();
    public static final Block redstone_inlay = placeholder();
    public static final Block iron_inlay = placeholder();
    public static final Block gold_inlay = placeholder();
    public static final Block vinteum_torch = placeholder();
    public static final Block keystone_receptacle = placeholder();
    public static final Block keystone_trapdoor = placeholder();
    public static final Block keystone_chest = placeholder();
    public static final Block keystone_door = placeholder();
    public static final Block flicker_lure = placeholder();
    public static final Block flicker_habitat = placeholder();
    public static final Block spell_sealed_door = placeholder();
    public static final Block everstone = placeholder();
    public static final Block spell_rune = placeholder();
    public static final Block arcane_deconstructor = placeholder();
    public static final Block arcane_reconstructor = placeholder();
    public static final Block astral_barrier = placeholder();
    public static final Block essence_refiner = placeholder();
    public static final Block illusion_block = placeholder();
    public static final Block seer_stone = placeholder();
    public static final Block broken_power_link = placeholder();
    public static final Block calefactor = placeholder();
    public static final Block magicians_workbench = placeholder();
    public static final Block otherworld_aura = placeholder();
    public static final Block particle_emitter = placeholder();
    public static final Block summoner = placeholder();
    public static final Block ice_effigy = placeholder();
    public static final Block lightning_effigy = placeholder();
    public static final Block mana_drain = placeholder();
    public static final Block phase_shift = placeholder();
    public static final Block witchwood_log = new BlockWitchwoodLog();
    public static final Block witchwood_leaves = new BlockWitchwoodLeaves();
    public static final Block witchwood_sapling = placeholder();
    public static final Block witchwood_planks = placeholder();
    public static final Block witchwood_stairs = placeholder();
    public static final Block witchwood_slab = placeholder();
    public static final Block witchwood_double_slab = placeholder();
    public static final Block liquid_essence_block = placeholder();
    public static final Block inert_spawner = placeholder();

    public static Fluid liquid_essence = new Fluid("liquid_essence",
            new ResourceLocation(ArsMagica.MODID, "blocks/liquid_essence_still"),
            new ResourceLocation(ArsMagica.MODID, "blocks/liquid_essence_flowing"))
            .setRarity(EnumRarity.RARE).setLuminosity(7).setDensity(8).setViscosity(3000);

    private AMBlocks() { } // no instances!

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();

        registerBlock(registry, "mana_battery", new BlockManaBattery());
        registerBlock(registry, "frost", new BlockFrost());
        registerBlock(registry, "occulus", new BlockOcculus());
        registerBlock(registry, "magic_wall", new BlockMagicWall());
        registerBlock(registry, "invisible_light", new BlockLightDecay());
        registerBlock(registry, "invisible_utility", new BlockInvisibleUtility());

        registerBlock(registry, "witchwood_log", new BlockWitchwoodLog());
        registerBlock(registry, "witchwood_leaves", new BlockWitchwoodLeaves());
        registerBlock(registry, "witchwood_planks", new BlockWitchwoodPlanks());
        registerBlock(registry, "witchwood_stairs", new BlockWitchwoodStairs((new BlockWitchwoodPlanks()).getDefaultState()));
        registerBlock(registry, "witchwood_slab", new BlockWitchwoodSlab());
        registerBlock(registry, "witchwood_double_slab", new BlockWitchwoodDoubleSlab(AMBlocks.witchwood_slab));
        registerBlock(registry, "vinteum_ore", vinteum_ore);
        registerBlock(registry, "chimerite_ore", chimerite_ore);
        registerBlock(registry, "blue_topaz_ore", blue_topaz_ore);
        registerBlock(registry, "moonstone_ore", moonstone_ore);
        registerBlock(registry, "sunstone_ore", sunstone_ore);

        registerBlock(registry, "vinteum_block", new BlockAM(Material.IRON, "pickaxe", 2).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.METAL)); // redstone
        registerBlock(registry, "chimerite_block", new BlockAM(Material.IRON, "pickaxe", 1).setHardness(5.0F).setResistance(10.0F).setSoundType(SoundType.STONE)); // lapis
        registerBlock(registry, "blue_topaz_block", new BlockAM(Material.ROCK, "pickaxe", 0).setHardness(0.8F).setSoundType(SoundType.STONE)); // quartz
        registerBlock(registry, "moonstone_block", new BlockAM(Material.IRON, "pickaxe", 2).setHardness(5.0F).setResistance(20.0F).setSoundType(SoundType.METAL)); // space emerald
        registerBlock(registry, "sunstone_block", new BlockAM(Material.IRON, "pickaxe", 2).setHardness(5.0F).setResistance(20.0F).setSoundType(SoundType.METAL)); // hell emerald
        registerBlock(registry, "block_mage_light", new BlockMageLight());

        registerBlock(registry, "witchwood_sapling", new BlockWitchwoodSapling());
        registerBlock(registry, "desert_nova", desert_nova);
        registerBlock(registry, "cerublossom", cerublossom);
        registerBlock(registry, "wakebloom", wakebloom);
        registerBlock(registry, "aum", aum);
        registerBlock(registry, "tarma_root", tarma_root);

        registerBlock(registry, "crafting_altar", new BlockCraftingAltar());
        registerBlock(registry, "calefactor", new BlockCalefactor());
        registerBlock(registry, "wizard_chalk", new BlockWizardsChalk());
        registerBlock(registry, "chalk_arrow", new BlockChalkArrow());
        registerBlock(registry, "obelisk", new BlockObelisk());
        registerBlock(registry, "black_aurem", new BlockBlackAurem());
        registerBlock(registry, "celestial_prism", new BlockCelestialPrism());
        registerBlock(registry, "crystal_marker", new BlockCrystalMarker());
        registerBlock(registry, "warding_candle", new BlockCandle());
        registerBlock(registry, "lectern", new BlockLectern());
        registerBlock(registry, "inscription_table", new BlockInscriptionTable());
        registerBlock(registry, "magicians_workbench", new BlockMagiciansWorkbench());
        registerBlock(registry, "armor_imbuer", new BlockArmorInfuser());
        registerBlock(registry, "essence_refiner", new BlockEssenceRefiner());
        registerBlock(registry, "arcane_reconstructor", new BlockArcaneReconstructor());
        registerBlock(registry, "arcane_deconstructor", new BlockArcaneDeconstructor());
        registerBlock(registry, "slipstream_generator", new BlockSlipstreamGenerator());
        registerBlock(registry, "essence_conduit", new BlockEssenceConduit());
        registerBlock(registry, "vinteum_torch", new BlockVinteumTorch());
        registerBlock(registry, "keystone_receptacle", new BlockKeystoneReceptacle());
        registerBlock(registry, "keystone_trapdoor", new BlockKeystoneTrapdoor());
        registerBlock(registry, "keystone_chest", new BlockKeystoneChest());
        registerBlock(registry, "flicker_lure", new BlockFlickerLure());
        registerBlock(registry, "flicker_habitat", new BlockFlickerHabitat());
        registerBlock(registry, "spell_sealed_door", new BlockSpellSealedDoor());
        registerBlock(registry, "keystone_door", new BlockKeystoneDoor());
        registerBlock(registry, "everstone", new BlockEverstone());
        registerBlock(registry, "spell_rune", new BlockGroundRuneSpell());
        registerBlock(registry, "astral_barrier", new BlockAstralBarrier());
        registerBlock(registry, "illusion_block", new BlockIllusionBlock());
        registerBlock(registry, "seer_stone", new BlockSeerStone());
        registerBlock(registry, "broken_power_link", new BlockBrokenPowerLink());
        registerBlock(registry, "otherworld_aura", new BlockOtherworldAura());
        registerBlock(registry, "particle_emitter", new BlockParticleEmitter());
        registerBlock(registry, "summoner", new BlockSummoner());
        registerBlock(registry, "ice_effigy", new BlockEffigy(Material.ICE), true); // TODO ?
        registerBlock(registry, "lightning_effigy", new BlockEffigy(Material.IRON), true); // TODO ?
        registerBlock(registry, "mana_drain", new BlockManaDrain());
        registerBlock(registry, "phase_shift", new BlockPhaseShift());
        registerBlock(registry, "redstone_inlay", new BlockInlay(BlockInlay.TYPE_REDSTONE));
        registerBlock(registry, "iron_inlay", new BlockInlay(BlockInlay.TYPE_IRON));
        registerBlock(registry, "gold_inlay", new BlockInlay(BlockInlay.TYPE_GOLD));
        registerBlock(registry, "inert_spawner", new BlockInertSpawner());

        FluidRegistry.registerFluid(AMBlocks.liquid_essence);
        FluidRegistry.addBucketForFluid(AMBlocks.liquid_essence);
        AMBlocks.liquid_essence = FluidRegistry.getFluid(AMBlocks.liquid_essence.getName());
        registerBlock(registry, "liquid_essence_block", new BlockLiquidEssence(liquid_essence), true);
    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T placeholder() {
        return null;
    }

    public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block) {
        registerBlock(registry, name, block, false);
    }

    public static void registerBlock(IForgeRegistry<Block> registry, String name, Block block, boolean skipCreativeTab) {
        block.setRegistryName(ArsMagica.MODID, name);
        block.setTranslationKey(block.getRegistryName().toString());
        if (!skipCreativeTab) {
            block.setCreativeTab(AMTabs.AMBLOCKS);
        }
        registry.register(block);
    }

    /**
     * Called from the preInit method in the main mod class to register all the tile entities.
     */
    @SuppressWarnings("deprecation")
    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityArcaneDeconstructor.class, new ResourceLocation(ArsMagica.MODID, "arcane_deconstructor"));
        GameRegistry.registerTileEntity(TileEntityArcaneReconstructor.class, new ResourceLocation(ArsMagica.MODID, "arcane_reconstructor"));
        GameRegistry.registerTileEntity(TileEntityArmorImbuer.class, new ResourceLocation(ArsMagica.MODID, "armor_imbuer"));
        GameRegistry.registerTileEntity(TileEntityAstralBarrier.class, new ResourceLocation(ArsMagica.MODID, "astral_barrier"));
        GameRegistry.registerTileEntity(TileEntityBlackAurem.class, new ResourceLocation(ArsMagica.MODID, "black_aurem"));
        GameRegistry.registerTileEntity(TileEntityBrokenPowerLink.class, new ResourceLocation(ArsMagica.MODID, "broken_power_link"));
        GameRegistry.registerTileEntity(TileEntityCalefactor.class, new ResourceLocation(ArsMagica.MODID, "calefactor"));
        GameRegistry.registerTileEntity(TileEntityCelestialPrism.class, new ResourceLocation(ArsMagica.MODID, "celestial_prism"));
        GameRegistry.registerTileEntity(TileEntityCraftingAltar.class, new ResourceLocation(ArsMagica.MODID, "crafting_altar"));
        GameRegistry.registerTileEntity(TileEntityCrystalMarker.class, new ResourceLocation(ArsMagica.MODID, "crystal_marker"));
        GameRegistry.registerTileEntity(TileEntityCrystalMarkerSpellExport.class, new ResourceLocation(ArsMagica.MODID, "crystal_marker_spell_export"));
        GameRegistry.registerTileEntity(TileEntityEssenceConduit.class, new ResourceLocation(ArsMagica.MODID, "essence_conduit"));
        GameRegistry.registerTileEntity(TileEntityEssenceRefiner.class, new ResourceLocation(ArsMagica.MODID, "essence_refiner"));
        GameRegistry.registerTileEntity(TileEntityEverstone.class, new ResourceLocation(ArsMagica.MODID, "everstone"));
        GameRegistry.registerTileEntity(TileEntityFlickerHabitat.class, new ResourceLocation(ArsMagica.MODID, "flicker_habitat"));
        GameRegistry.registerTileEntity(TileEntityFlickerLure.class, new ResourceLocation(ArsMagica.MODID, "flicker_lure"));
        GameRegistry.registerTileEntity(TileEntityGroundRuneSpell.class, new ResourceLocation(ArsMagica.MODID, "ground_rune_spell"));
        GameRegistry.registerTileEntity(TileEntityIllusionBlock.class, new ResourceLocation(ArsMagica.MODID, "illusion_block"));
        GameRegistry.registerTileEntity(TileEntityInertSpawner.class, new ResourceLocation(ArsMagica.MODID, "inert_spawner"));
        GameRegistry.registerTileEntity(TileEntityInscriptionTable.class, new ResourceLocation(ArsMagica.MODID, "inscription_table"));
        GameRegistry.registerTileEntity(TileEntityKeystoneChest.class, new ResourceLocation(ArsMagica.MODID, "keystone_chest"));
        GameRegistry.registerTileEntity(TileEntityKeystoneDoor.class, new ResourceLocation(ArsMagica.MODID, "keystone_door"));
        GameRegistry.registerTileEntity(TileEntityKeystoneReceptacle.class, new ResourceLocation(ArsMagica.MODID, "keystone_receptacle"));
        GameRegistry.registerTileEntity(TileEntityLectern.class, new ResourceLocation(ArsMagica.MODID, "lectern"));
        GameRegistry.registerTileEntity(TileEntityMagiciansWorkbench.class, new ResourceLocation(ArsMagica.MODID, "magicians_workbench"));
        GameRegistry.registerTileEntity(TileEntityManaBattery.class, new ResourceLocation(ArsMagica.MODID, "mana_battery"));
        GameRegistry.registerTileEntity(TileEntityManaDrain.class, new ResourceLocation(ArsMagica.MODID, "mana_drain"));
        GameRegistry.registerTileEntity(TileEntityObelisk.class, new ResourceLocation(ArsMagica.MODID, "obelisk"));
        GameRegistry.registerTileEntity(TileEntityOcculus.class, new ResourceLocation(ArsMagica.MODID, "occulus"));
        GameRegistry.registerTileEntity(TileEntityOtherworldAura.class, new ResourceLocation(ArsMagica.MODID, "otherworld_aura"));
        GameRegistry.registerTileEntity(TileEntityParticleEmitter.class, new ResourceLocation(ArsMagica.MODID, "particle_emitter"));
        GameRegistry.registerTileEntity(TileEntitySeerStone.class, new ResourceLocation(ArsMagica.MODID, "seer_stone"));
        GameRegistry.registerTileEntity(TileEntitySlipstreamGenerator.class, new ResourceLocation(ArsMagica.MODID, "slipstram_generator"));
        GameRegistry.registerTileEntity(TileEntitySpellSealedDoor.class, new ResourceLocation(ArsMagica.MODID, "spell_sealed_door"));
        GameRegistry.registerTileEntity(TileEntitySummoner.class, new ResourceLocation(ArsMagica.MODID, "summoner"));
        GameRegistry.registerTileEntity(TileEntityPhaseShift.class, new ResourceLocation(ArsMagica.MODID, "phase_shift"));
        GameRegistry.registerTileEntity(TileEntityChalkArrow.class, new ResourceLocation(ArsMagica.MODID, "chalk_arrow"));
    }

}
