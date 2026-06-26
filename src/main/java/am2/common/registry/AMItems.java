package am2.common.registry;

import am2.ArsMagica;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.items.*;
import net.minecraft.block.Block;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public final class AMItems {

    public static final Item spell = placeholder();

    private AMItems() { } // No instances!

    public static final Item infinity_orb = placeholder();
    public static final Item chalk = placeholder();
    public static final Item colored_chalk = placeholder();

    public static final Item spell_parchment = new Item();
    public static final Item etherium = placeholder();
    public static final Item blank_rune = placeholder();
    public static final Item evil_book = placeholder();
    public static final Item wooden_leg = placeholder();
    public static final Item deficit_crystal = placeholder();
    public static final Item workbench_upgrade = placeholder();
    public static final Item spell_part = placeholder();

    public static final Item affinity_tome_none = placeholder();
    public static final Item affinity_tome_ender = placeholder();
    public static final Item affinity_tome_earth = placeholder();
    public static final Item affinity_tome_fire = placeholder();
    public static final Item affinity_tome_life = placeholder();
    public static final Item affinity_tome_nature = placeholder();
    public static final Item affinity_tome_ice = placeholder();
    public static final Item affinity_tome_lightning = placeholder();
    public static final Item affinity_tome_air = placeholder();
    public static final Item affinity_tome_water = placeholder();
    public static final Item affinity_tome_arcane = placeholder();

    public static final Item vinteum_dust = placeholder();
    public static final Item chimerite = placeholder();
    public static final Item blue_topaz = placeholder();
    public static final Item moonstone = placeholder();
    public static final Item sunstone = placeholder();

    public static final Item purified_vinteum_dust = placeholder();
    public static final Item arcane_ash = placeholder();
    public static final Item arcane_compound = placeholder();
    public static final Item animal_fat = placeholder();
    public static final Item vinteum_infused_ingot = placeholder();

    public static final Item essence_none = placeholder();
    public static final Item essence_ender = placeholder();
    public static final Item essence_earth = placeholder();
    public static final Item essence_fire = placeholder();
    public static final Item essence_life = placeholder();
    public static final Item essence_nature = placeholder();
    public static final Item essence_ice = placeholder();
    public static final Item essence_lightning = placeholder();
    public static final Item essence_air = placeholder();
    public static final Item essence_water = placeholder();
    public static final Item essence_arcane = placeholder();

    public static final Item rune = placeholder();

    public static final Item staff = placeholder();
    public static final Item bound_sword = placeholder();
    public static final Item bound_axe = placeholder();
    public static final Item bound_pickaxe = placeholder();
    public static final Item bound_shovel = placeholder();
    public static final Item bound_hoe = placeholder();
    public static final Item bound_bow = placeholder();
    public static final Item bound_shield = placeholder();

    public static final Item mob_focus = placeholder();
    public static final Item lesser_focus = placeholder();
    public static final Item standard_focus = placeholder();
    public static final Item mana_focus = placeholder();
    public static final Item greater_focus = placeholder();
    public static final Item charge_focus = placeholder();
    public static final Item item_focus = placeholder();
    public static final Item player_focus = placeholder();
    public static final Item creature_focus = placeholder();

    public static final Item arcane_compendium = placeholder();
    public static final Item crystal_wrench = placeholder();
    public static final Item magitech_goggles = placeholder();
    public static final Item attuning_staff = placeholder();
    public static final Item flicker_focus = placeholder();
    public static final Item flicker_jar = placeholder();
    public static final Item warding_candle = placeholder();
    public static final Item liquid_essence_bottle = placeholder();
    public static final Item binding_catalyst = placeholder();
    public static final Item inscription_upgrade = placeholder();

    public static final Item mana_cake = placeholder();
    public static final Item bound_arrow = placeholder();
    public static final Item nature_scythe = placeholder();
    public static final Item winter_arm = placeholder();
    public static final Item air_sled = placeholder();
    public static final Item arcane_spellbook = placeholder();
    public static final Item earth_armor = placeholder();
    public static final Item ender_boots = placeholder();
    public static final Item fire_ears = placeholder();
    public static final Item life_ward = placeholder();
    public static final Item lightning_charm = placeholder();
    public static final Item water_orbs = placeholder();
    public static final Item spellbook = placeholder();
    public static final Item magic_broom = placeholder();
    public static final Item keystone = placeholder();
    public static final Item keystone_door = placeholder();
    public static final Item spell_sealed_door = placeholder();
    public static final Item rune_bag = placeholder();
    //public static final Item lost_journal = placeholder();
    public static final Item crystal_phylactery = placeholder();

    /**
     * Wrapper item for binding EBWiz spells into AM2 spell book slots.
     * Only registered (and functional) when Electroblob's Wizardry is loaded.
     * Null when EBWiz is absent; always null-check before use.
     */
    public static final Item ebwiz_spell_binding = placeholder();

    public static final Item core = placeholder();
    public static final Item lesser_mana_potion = placeholder();
    public static final Item standard_mana_potion = placeholder();
    public static final Item greater_mana_potion = placeholder();
    public static final Item epic_mana_potion = placeholder();
    public static final Item legendary_mana_potion = placeholder();
    public static final Item mana_potion_bundle = placeholder();
    public static final Item essence_bag = placeholder();
    public static final Item hell_cow_horn = placeholder();
    public static final Item journal = placeholder();
    public static final Item mana_martini = placeholder();
    public static final Item mage_hood = placeholder();
    public static final Item mage_robe = placeholder();
    public static final Item mage_leggings = placeholder();
    public static final Item mage_boots = placeholder();
    public static final Item battlemage_helmet = placeholder();
    public static final Item battlemage_chestplate = placeholder();
    public static final Item battlemage_leggings = placeholder();
    public static final Item battlemage_boots = placeholder();
    public static final Item crystallized_entity = placeholder();
    public static final Item debug_rune = placeholder();

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {

        IForgeRegistry<Item> registry = event.getRegistry();
        registerItem(registry, "mage_hood", ArsMagica.MODID, new ItemMageHood(AMMaterials.MAGE, ArsMagicaArmorMaterial.MAGE, 0, EntityEquipmentSlot.HEAD));
        registerItem(registry, "mage_robe", ArsMagica.MODID, new ItemMageRobe(AMMaterials.MAGE, ArsMagicaArmorMaterial.MAGE, 0, EntityEquipmentSlot.CHEST));
        registerItem(registry, "mage_leggings", ArsMagica.MODID, new AMArmor(AMMaterials.MAGE, ArsMagicaArmorMaterial.MAGE, 0, EntityEquipmentSlot.LEGS));
        registerItem(registry, "mage_boots", ArsMagica.MODID, new AMArmor(AMMaterials.MAGE, ArsMagicaArmorMaterial.MAGE, 0, EntityEquipmentSlot.FEET));
        registerItem(registry, "battlemage_helmet", ArsMagica.MODID, new ItemMageHood(AMMaterials.BATTLEMAGE, ArsMagicaArmorMaterial.BATTLEMAGE, 0, EntityEquipmentSlot.HEAD));
        registerItem(registry, "battlemage_chestplate", ArsMagica.MODID, new AMArmor(AMMaterials.BATTLEMAGE, ArsMagicaArmorMaterial.BATTLEMAGE, 0, EntityEquipmentSlot.CHEST));
        registerItem(registry, "battlemage_leggings", ArsMagica.MODID, new AMArmor(AMMaterials.BATTLEMAGE, ArsMagicaArmorMaterial.BATTLEMAGE, 0, EntityEquipmentSlot.LEGS));
        registerItem(registry, "battlemage_boots", ArsMagica.MODID, new AMArmor(AMMaterials.BATTLEMAGE, ArsMagicaArmorMaterial.BATTLEMAGE, 0, EntityEquipmentSlot.FEET));
        registerItem(registry, "magitech_goggles", ArsMagica.MODID, new ItemMagitechGoggles());
        registerItem(registry, "crystal_wrench", ArsMagica.MODID, new ItemCrystalWrench());
        registerItem(registry, "attuning_staff", ArsMagica.MODID, new ItemAttuningStaff(0, -1));
        registerItem(registry, "infinity_orb", ArsMagica.MODID, new ItemInfinityOrb());
        registerItem(registry, "arcane_compendium", ArsMagica.MODID, new ItemArcaneCompendium());
        registerItem(registry, "evil_book", ArsMagica.MODID, new Item());
        // When EBWiz is present, use the ISpellCastingItem-implementing subclass so that
        // EntityUtils.isCasting() recognises continuous EBWiz spells cast from spell books
        // (e.g. wing rendering during Flight, looping sounds, shadow ward, etc.).
        // Use the factory to avoid a direct bytecode reference to ItemSpellBookEBWiz in this class,
        // which would cause NoClassDefFoundError on startup when EBWiz is absent.
        registerItem(registry, "spellbook", ArsMagica.MODID, EBWizardryCompatBootstrap.createSpellBookItem());
        registerItem(registry, "arcane_spellbook", ArsMagica.MODID, new ItemArcaneGuardianSpellbook());
        registerItem(registry, "affinity_tome_none", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:none"));
        registerItem(registry, "affinity_tome_ender", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:ender"));
        registerItem(registry, "affinity_tome_earth", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:earth"));
        registerItem(registry, "affinity_tome_fire", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:fire"));
        registerItem(registry, "affinity_tome_life", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:life"));
        registerItem(registry, "affinity_tome_nature", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:nature"));
        registerItem(registry, "affinity_tome_ice", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:ice"));
        registerItem(registry, "affinity_tome_lightning", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:lightning"));
        registerItem(registry, "affinity_tome_air", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:air"));
        registerItem(registry, "affinity_tome_water", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:water"));
        registerItem(registry, "affinity_tome_arcane", ArsMagica.MODID, new ItemAffinityTome("arsmagica2:arcane"));
        registerItem(registry, "spell_parchment", ArsMagica.MODID, new Item());
        registerItem(registry, "etherium", ArsMagica.MODID, new Item(), true);

        registerItem(registry, "vinteum_dust", ArsMagica.MODID, new Item());
        registerItem(registry, "chimerite", ArsMagica.MODID, new Item());
        registerItem(registry, "blue_topaz", ArsMagica.MODID, new Item());
        registerItem(registry, "moonstone", ArsMagica.MODID, new Item());
        registerItem(registry, "sunstone", ArsMagica.MODID, new Item());

        registerItem(registry, "purified_vinteum_dust", ArsMagica.MODID, new Item());
        registerItem(registry, "arcane_ash", ArsMagica.MODID, new Item());
        registerItem(registry, "arcane_compound", ArsMagica.MODID, new Item());
        registerItem(registry, "animal_fat", ArsMagica.MODID, new Item());
        registerItem(registry, "vinteum_infused_ingot", ArsMagica.MODID, new Item());

        //  Affinity objects will be resolved lazily from registry
        registerItem(registry, "essence_ender", ArsMagica.MODID, new ItemEssence("arsmagica2:ender"));
        registerItem(registry, "essence_earth", ArsMagica.MODID, new ItemEssence("arsmagica2:earth"));
        registerItem(registry, "essence_fire", ArsMagica.MODID, new ItemEssence("arsmagica2:fire"));
        registerItem(registry, "essence_life", ArsMagica.MODID, new ItemEssence("arsmagica2:life"));
        registerItem(registry, "essence_nature", ArsMagica.MODID, new ItemEssence("arsmagica2:nature"));
        registerItem(registry, "essence_ice", ArsMagica.MODID, new ItemEssence("arsmagica2:ice"));
        registerItem(registry, "essence_lightning", ArsMagica.MODID, new ItemEssence("arsmagica2:lightning"));
        registerItem(registry, "essence_air", ArsMagica.MODID, new ItemEssence("arsmagica2:air"));
        registerItem(registry, "essence_water", ArsMagica.MODID, new ItemEssence("arsmagica2:water"));
        registerItem(registry, "essence_arcane", ArsMagica.MODID, new ItemEssence("arsmagica2:arcane"));

        registerItem(registry, "blank_rune", ArsMagica.MODID, new Item());
        registerItem(registry, "rune", ArsMagica.MODID, new ItemRune());
        registerItem(registry, "mob_focus", ArsMagica.MODID, new ItemFocusMob());
        registerItem(registry, "lesser_focus", ArsMagica.MODID, new ItemFocusLesser());
        registerItem(registry, "standard_focus", ArsMagica.MODID, new ItemFocusStandard());
        registerItem(registry, "mana_focus", ArsMagica.MODID, new ItemFocusMana());
        registerItem(registry, "greater_focus", ArsMagica.MODID, new ItemFocusGreater());
        registerItem(registry, "charge_focus", ArsMagica.MODID, new ItemFocusCharge());
        registerItem(registry, "item_focus", ArsMagica.MODID, new ItemFocusItem());
        registerItem(registry, "player_focus", ArsMagica.MODID, new ItemFocusPlayer());
        registerItem(registry, "creature_focus", ArsMagica.MODID, new ItemFocusCreature());
        registerItem(registry, "chalk", ArsMagica.MODID, new ItemChalk());
        registerItem(registry, "colored_chalk", ArsMagica.MODID, new ItemColoredChalk());
        registerItem(registry, "flicker_focus", ArsMagica.MODID, new ItemFlickerFocus());
        registerItem(registry, "flicker_jar", ArsMagica.MODID, new ItemFlickerJar());
        registerItem(registry, "warding_candle", ArsMagica.MODID, new ItemCandle());
        registerItem(registry, "liquid_essence_bottle", ArsMagica.MODID, new ItemLiquidEssenceBottle());
        registerItem(registry, "staff", ArsMagica.MODID, new ItemStaff());
        registerItem(registry, "bound_sword", ArsMagica.MODID, new ItemBoundSword(), true);
        registerItem(registry, "bound_axe", ArsMagica.MODID, new ItemBoundAxe(), true);
        registerItem(registry, "bound_pickaxe", ArsMagica.MODID, new ItemBoundPickaxe(), true);
        registerItem(registry, "bound_shovel", ArsMagica.MODID, new ItemBoundShovel(), true);
        registerItem(registry, "bound_hoe", ArsMagica.MODID, new ItemBoundHoe(), true);
        registerItem(registry, "bound_bow", ArsMagica.MODID, new ItemBoundBow(), true);
        registerItem(registry, "bound_shield", ArsMagica.MODID, new ItemBoundShield(), true);
        registerItem(registry, "binding_catalyst", ArsMagica.MODID, new ItemBindingCatalyst(), true);

        registerItem(registry, "spell", ArsMagica.MODID, new ItemSpellBase(), true);
        registerItem(registry, "wooden_leg", ArsMagica.MODID, new Item());
        registerItem(registry, "deficit_crystal", ArsMagica.MODID, new Item());
        registerItem(registry, "workbench_upgrade", ArsMagica.MODID, new Item());
//        registerItem(registry, "spell_component", ArsMagica.MODID, new ItemSpellComponent(), true);

        registerItem(registry, "inscription_upgrade", ArsMagica.MODID, new ItemInscriptionTableUpgrade());
        registerItem(registry, "mana_cake", ArsMagica.MODID, new ItemManaCake());
        registerItem(registry, "bound_arrow", ArsMagica.MODID, new ItemBoundArrow(), true);
        registerItem(registry, "nature_scythe", ArsMagica.MODID, new ItemNatureGuardianSickle());
        registerItem(registry, "winter_arm", ArsMagica.MODID, new ItemWinterGuardianArm());
        registerItem(registry, "air_sled", ArsMagica.MODID, new ItemAirSled());
        registerItem(registry, "earth_armor", ArsMagica.MODID, new ItemEarthGuardianArmor(ItemArmor.ArmorMaterial.GOLD, ArsMagicaArmorMaterial.UNIQUE, 0, EntityEquipmentSlot.CHEST));
        registerItem(registry, "ender_boots", ArsMagica.MODID, new ItemEnderBoots(AMMaterials.ENDER, ArsMagicaArmorMaterial.UNIQUE, 0, EntityEquipmentSlot.FEET));
        registerItem(registry, "fire_ears", ArsMagica.MODID, new ItemFireGuardianEars(ItemArmor.ArmorMaterial.GOLD, ArsMagicaArmorMaterial.UNIQUE, 0, EntityEquipmentSlot.HEAD));
        registerItem(registry, "life_ward", ArsMagica.MODID, new ItemLifeWard());
        registerItem(registry, "lightning_charm", ArsMagica.MODID, new ItemLightningCharm());
        registerItem(registry, "water_orbs", ArsMagica.MODID, new ItemWaterGuardianOrbs(ItemArmor.ArmorMaterial.GOLD, ArsMagicaArmorMaterial.UNIQUE, 0, EntityEquipmentSlot.LEGS));
        registerItem(registry, "magic_broom", ArsMagica.MODID, new ItemMagicBroom());
        registerItem(registry, "keystone", ArsMagica.MODID, new ItemKeystone());
        registerItem(registry, "rune_bag", ArsMagica.MODID, new ItemRuneBag());
        registerItem(registry, "essence_bag", ArsMagica.MODID, new ItemEssenceBag());
        //registerItem(registry, "lost_journal", ArsMagica.MODID, new ItemLostJournal());
        registerItem(registry, "core", ArsMagica.MODID, new ItemCore());
        registerItem(registry, "lesser_mana_potion", ArsMagica.MODID, new ItemManaPotion());
        registerItem(registry, "standard_mana_potion", ArsMagica.MODID, new ItemManaPotion());
        registerItem(registry, "greater_mana_potion", ArsMagica.MODID, new ItemManaPotion());
        registerItem(registry, "epic_mana_potion", ArsMagica.MODID, new ItemManaPotion());
        registerItem(registry, "legendary_mana_potion", ArsMagica.MODID, new ItemManaPotion());
        registerItem(registry, "mana_potion_bundle", ArsMagica.MODID, new ItemManaPotionBundle());
        registerItem(registry, "hell_cow_horn", ArsMagica.MODID, new ItemHellCowHorn());
        registerItem(registry, "journal", ArsMagica.MODID, new ItemJournal());
        registerItem(registry, "mana_martini", ArsMagica.MODID, new ItemManaMartini());
        registerItem(registry, "crystal_phylactery", ArsMagica.MODID, new ItemCrystalPhylactery());
        registerItem(registry, "crystallized_entity", ArsMagica.MODID, new ItemCrystallizedEntity(), true);
        registerItem(registry, "debug_rune", ArsMagica.MODID, new ItemDebugRune());

        // EBWiz compatibility – only register when Electroblob's Wizardry is present.
        // Uses the ElectroblobCompat facade to keep ItemEBWizSpellBinding out of AMItems bytecode.
        EBWizardryCompatBootstrap.registerEBWizItems(registry);

        registerItemBlock(registry, AMBlocks.mana_battery, new ItemBlockManaBattery(AMBlocks.mana_battery));
        registerItemBlock(registry, AMBlocks.occulus);
        registerItemBlock(registry, AMBlocks.magic_wall);

        registerItemBlock(registry, AMBlocks.vinteum_ore);
        registerItemBlock(registry, AMBlocks.chimerite_ore);
        registerItemBlock(registry, AMBlocks.blue_topaz_ore);
        registerItemBlock(registry, AMBlocks.moonstone_ore);
        registerItemBlock(registry, AMBlocks.sunstone_ore);

        registerItemBlock(registry, AMBlocks.vinteum_block);
        registerItemBlock(registry, AMBlocks.chimerite_block);
        registerItemBlock(registry, AMBlocks.blue_topaz_block);
        registerItemBlock(registry, AMBlocks.moonstone_block);
        registerItemBlock(registry, AMBlocks.sunstone_block);
        registerItemBlock(registry, AMBlocks.witchwood_log);
        registerItemBlock(registry, AMBlocks.witchwood_leaves);
        registerItemBlock(registry, AMBlocks.witchwood_sapling);
        registerItemBlock(registry, AMBlocks.witchwood_planks);
        registerItemBlock(registry, AMBlocks.witchwood_stairs);

        registerItemBlock(registry, AMBlocks.witchwood_slab, new SlabItem(AMBlocks.witchwood_slab, AMBlocks.witchwood_double_slab));

        registerItemBlock(registry, AMBlocks.desert_nova);
        registerItemBlock(registry, AMBlocks.cerublossom);
        registerItemBlock(registry, AMBlocks.wakebloom);
        registerItemBlock(registry, AMBlocks.aum);
        registerItemBlock(registry, AMBlocks.tarma_root);

        registerItemBlockBaked(registry, AMBlocks.crafting_altar);
        registerItemBlockBaked(registry, AMBlocks.obelisk);
        registerItemBlock(registry, AMBlocks.black_aurem);
        registerItemBlockBaked(registry, AMBlocks.celestial_prism);
        registerItemBlock(registry, AMBlocks.lectern);
        registerItemBlock(registry, AMBlocks.inscription_table, new ItemInscriptionTable(AMBlocks.inscription_table));
        registerItemBlock(registry, AMBlocks.magicians_workbench);
        registerItemBlock(registry, AMBlocks.armor_imbuer);
        registerItemBlock(registry, AMBlocks.calefactor);
        registerItemBlock(registry, AMBlocks.slipstream_generator);
        registerItemBlock(registry, AMBlocks.flicker_habitat);

        registerItemBlock(registry, AMBlocks.vinteum_torch);

        registerItemBlock(registry, AMBlocks.keystone_trapdoor);
        registerItem(registry, "spell_sealed_door", ArsMagica.MODID, new ItemSpellSealedDoor(), true);
        registerItem(registry, "keystone_door", ArsMagica.MODID, new ItemKeystoneDoor(), true);

        registerItemBlock(registry, AMBlocks.flicker_lure);
        registerItemBlock(registry, AMBlocks.everstone);
        registerItemBlock(registry, AMBlocks.arcane_deconstructor);

        //registerItemBlock(registry, AMBlocks.spell_rune);

        registerItemBlockBaked(registry, AMBlocks.astral_barrier);
        registerItemBlock(registry, AMBlocks.essence_refiner);
        registerItemBlock(registry, AMBlocks.illusion_block, new ItemBlockIllusion(AMBlocks.illusion_block));
//		registerItemBlock(registry, AMBlocks.broken_power_link);

        registerItemBlock(registry, AMBlocks.otherworld_aura);
        registerItemBlock(registry, AMBlocks.particle_emitter);
        registerItemBlock(registry, AMBlocks.mana_drain_block);

        registerItemBlock(registry, AMBlocks.liquid_essence_block);

        /* Baked stuff
         *
         * These must have their models/item/modelfile.json set to the following value otherwise rendering it won't work
         * {
         *     "parent": "builtin/entity"
         * }
         * */
        registerItemBlockBaked(registry, AMBlocks.keystone_chest);
        registerItemBlock(registry, AMBlocks.essence_conduit);  // Uses JSON model with TESR for crystal only
        registerItemBlock(registry, AMBlocks.keystone_receptacle);  // Uses JSON model
        registerItemBlock(registry, AMBlocks.seer_stone);
        registerItemBlockBaked(registry, AMBlocks.arcane_reconstructor);
        registerItemBlockBaked(registry, AMBlocks.summoner);
        registerItemBlock(registry, AMBlocks.crystal_marker, new ItemBlockCrystalMarker(AMBlocks.crystal_marker));
        registerItemBlock(registry, AMBlocks.redstone_inlay);
        registerItemBlock(registry, AMBlocks.iron_inlay);
        registerItemBlock(registry, AMBlocks.gold_inlay);
        registerItemBlock(registry, AMBlocks.ice_effigy);
        registerItemBlock(registry, AMBlocks.lightning_effigy);

    }

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T placeholder() {
        return null;
    }

    // below registry methods are courtesy of EB
    public static Item registerItem(IForgeRegistry<Item> registry, String name, String modid, Item item) {
        return registerItem(registry, name, modid, item, false);
    }

    public static Item registerItem(IForgeRegistry<Item> registry, String name, String modid, Item item, Boolean removeTab) {
        item.setRegistryName(modid, name);
        item.setTranslationKey(Objects.requireNonNull(item.getRegistryName()).toString());
        registry.register(item);
        if (!removeTab) item.setCreativeTab(AMTabs.AMITEMS);
        return item;
    }

    public static void registerItemBlock(IForgeRegistry<Item> registry, Block block) {
        Item itemblock = new ItemBlock(block).setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        registry.register(itemblock);
    }

    public static void registerItemBlockBaked(IForgeRegistry<Item> registry, Block block) {
        Item itemblock = new ItemBlockBaked(block).setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        registry.register(itemblock);
    }

    public static void registerItemBlock(IForgeRegistry<Item> registry, Block block, Item itemblock) {
        itemblock.setRegistryName(Objects.requireNonNull(block.getRegistryName()));
        registry.register(itemblock);
    }
}