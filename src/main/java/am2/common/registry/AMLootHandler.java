package am2.common.registry;

import am2.ArsMagica;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Injects AM2 loot pools into vanilla chest loot tables.
 * Each inject table lives at assets/arsmagica2/loot_tables/inject/<vanilla_table_name>.json
 */
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMLootHandler {

    // EBWizardry-specific loot tables
    private static final ResourceLocation EBWIZ_EVIL_WIZARD = new ResourceLocation("ebwizardry", "entities/evil_wizard");
    private static final ResourceLocation AM2_EARTH_ELEMENTAL = new ResourceLocation("arsmagica2", "entities/earth_elemental");
    private static final ResourceLocation AM2_FIRE_ELEMENTAL = new ResourceLocation("arsmagica2", "entities/fire_elemental");
    private static final ResourceLocation AM2_MANA_ELEMENTAL = new ResourceLocation("arsmagica2", "entities/mana_elemental");
    private static final ResourceLocation AM2_ICE_ELEMENTAL = new ResourceLocation("arsmagica2", "entities/ice_elemental");
    private static final ResourceLocation AM2_DARK_MAGE = new ResourceLocation("arsmagica2", "entities/dark_mage");
    private static final ResourceLocation AM2_LIGHT_MAGE = new ResourceLocation("arsmagica2", "entities/light_mage");

    private static final Map<ResourceLocation, ResourceLocation> INJECT_TABLE_MAP = new HashMap<>();

    static {
        register("minecraft:chests/simple_dungeon", "inject/simple_dungeon");
        register("minecraft:chests/stronghold_corridor", "inject/stronghold_corridor");
        register("minecraft:chests/stronghold_crossing", "inject/stronghold_crossing");
        register("minecraft:chests/mineshaft", "inject/mineshaft");
        if (Loader.isModLoaded(EBWizardryCompatBootstrap.MODID)) {
            register("ebwizardry:entities/evil_wizard", "inject/ebwizardry_evil_wizard");
            register("arsmagica2:entities/earth_elemental", "inject/ebwiz_earth_elemental");
            register("arsmagica2:entities/fire_elemental", "inject/ebwiz_fire_elemental");
            register("arsmagica2:entities/mana_elemental", "inject/ebwiz_mana_elemental");
            register("arsmagica2:entities/dark_mage", "inject/ebwiz_dark_mage");
            register("arsmagica2:entities/light_mage", "inject/ebwiz_light_mage");
            register("arsmagica2:entities/lightning_elemental", "inject/ebwiz_lightning_elemental");
            register("arsmagica2:entities/ice_elemental", "inject/ebwiz_ice_elemental");
        }
    }

    private static void register(String vanilla, String injectPath) {
        INJECT_TABLE_MAP.put(new ResourceLocation(vanilla),
                new ResourceLocation(ArsMagica.MODID, injectPath));
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation injectLoc = INJECT_TABLE_MAP.get(event.getName());
        if (injectLoc == null) return;

        if (EBWIZ_EVIL_WIZARD.equals(event.getName()) && !ArsMagica.config.getEBWizEvilWizardOrbDrop()) return;
        if ((AM2_EARTH_ELEMENTAL.equals(event.getName()) || AM2_FIRE_ELEMENTAL.equals(event.getName()) || AM2_MANA_ELEMENTAL.equals(event.getName()) || AM2_ICE_ELEMENTAL.equals(event.getName()))
                && !ArsMagica.config.getEBWizElementalCrystalDrops()) return;
        if ((AM2_DARK_MAGE.equals(event.getName()) || AM2_LIGHT_MAGE.equals(event.getName()))
                && !ArsMagica.config.getEBWizMageTomeDrop()) return;

        String poolName = "am2_inject_" + injectLoc.getPath().replace('/', '_');
        LootEntry entry = new LootEntryTable(injectLoc, 1, 0, new LootCondition[0], poolName + "_entry");
        LootPool pool = new LootPool(new LootEntry[]{entry}, new LootCondition[0], new RandomValueRange(1), new RandomValueRange(0), poolName);
        event.getTable().addPool(pool);
    }
}
