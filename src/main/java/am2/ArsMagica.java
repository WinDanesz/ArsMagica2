package am2;

import am2.api.spell.SpellPart;
import am2.common.CommonProxy;
import am2.common.commands.CommandArsMagica;
import am2.common.config.AMConfig;
import am2.common.config.SpellPartConfig;
import am2.common.config.SpellPartConfiguration;
import am2.common.items.StaffPresets;
import am2.common.loot.LootFunctionSoulbind;
import am2.common.loot.LootFunctionStaffPreset;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMMaterials;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import am2.common.animation.AnimationAPI;

import java.io.File;
import java.util.Properties;

@Mod(modid = ArsMagica.MODID,
        name = ArsMagica.NAME,
        version = ArsMagica.VERSION,
        acceptedMinecraftVersions = "1.12.2",
        guiFactory = "am2.client.config.AMGuiFactory")
public class ArsMagica {

    // Enable universal bucket before fluid registration
    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static final String MODID = "arsmagica2";
    public static final String NAME = "Ars Magica 2";
    public static final String VERSION = "GRADLE:VERSION" + "GRADLE:BUILD";
    public static final Logger LOGGER = LogManager.getLogger("ArsMagica2");

    // Location of the proxy code, used by Forge.
    @SidedProxy(clientSide = "am2.client.ClientProxy", serverSide = "am2.common.CommonProxy")
    public static CommonProxy proxy;

    // The instance of ArsMagica2 that Forge uses.
    @Mod.Instance(ArsMagica.MODID)
    public static ArsMagica instance;

    public static AMConfig config;
    public static SpellPartConfiguration disabledSkills;
    /**
     * Config file that allows overriding the crafting-altar recipe of any spell part.
     */
    public static SpellPartConfig spellRecipeConfig;
    private File configDir;

    public String getVersion() {
        return VERSION;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Properties props = System.getProperties();
        props.setProperty("forge.verboseMissingModelLoggingCount", "200");

        configDir = new File(event.getModConfigurationDirectory(), ArsMagica.MODID);
        config = new AMConfig(new File(configDir, "am2.cfg"));
        disabledSkills = new SpellPartConfiguration(new File(configDir, "skills.cfg"));
        spellRecipeConfig = new SpellPartConfig(new File(configDir, "spell_parts.cfg"));
        proxy.preInit();
        AMMaterials.preInit();
        AMBlocks.registerTileEntities();
        LootFunctionManager.registerFunction(new LootFunctionSoulbind.Serializer());
        LootFunctionManager.registerFunction(new LootFunctionStaffPreset.Serializer());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(instance);
        proxy.init();
        AnimationAPI.init();
        CommonProxy.initOreDict();
        am2.common.advancement.AMAdvancementTriggers.register();
        config.init();
        StaffPresets.init(config.getStaffPresets());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
        AnimationAPI.postInit();
        // Load spell recipe overrides after the spell registry is fully populated.
        spellRecipeConfig.reload();
        SpellPart.setOverrides(spellRecipeConfig);
    }

    @EventHandler
    public void serverStartup(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandArsMagica());
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent e) {
        if (!e.getModID().equals(MODID)) return;
        config.save();
        config.init();
        StaffPresets.init(config.getStaffPresets());
        disabledSkills.save();
        disabledSkills.getDisabledSkills(true);
        if (spellRecipeConfig != null) {
            spellRecipeConfig.reload();
        }
    }
}
