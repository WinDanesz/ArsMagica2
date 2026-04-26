package am2.common.registry;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;

@ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class Affinities {

    private Affinities() {
    } // no instances

    // This is here because this class is already an event handler.
    @SubscribeEvent
    public static void createRegistry(RegistryEvent.NewRegistry event) {

        RegistryBuilder<Affinity> builder = new RegistryBuilder<>();
        builder.setType(Affinity.class);
        builder.setName(new ResourceLocation(ArsMagica.MODID, "affinities"));
        builder.setIDRange(0, 5000); // Is there any penalty for using a larger number?

        Affinity.registry = builder.create();
    }

    private static final ResourceLocation NONE_LOC = new ResourceLocation("arsmagica2", "none");
    private static final ResourceLocation ARCANE_LOC = new ResourceLocation("arsmagica2", "arcane");
    private static final ResourceLocation WATER_LOC = new ResourceLocation("arsmagica2", "water");
    private static final ResourceLocation ICE_LOC = new ResourceLocation("arsmagica2", "ice");
    private static final ResourceLocation FIRE_LOC = new ResourceLocation("arsmagica2", "fire");
    private static final ResourceLocation EARTH_LOC = new ResourceLocation("arsmagica2", "earth");
    private static final ResourceLocation AIR_LOC = new ResourceLocation("arsmagica2", "air");
    private static final ResourceLocation LIGHTNING_LOC = new ResourceLocation("arsmagica2", "lightning");
    private static final ResourceLocation NATURE_LOC = new ResourceLocation("arsmagica2", "nature");
    private static final ResourceLocation LIFE_LOC = new ResourceLocation("arsmagica2", "life");
    private static final ResourceLocation ENDER_LOC = new ResourceLocation("arsmagica2", "ender");

    public static final Affinity none = placeholder();
    public static final Affinity ender = placeholder();
    public static final Affinity earth = placeholder();
    public static final Affinity fire = placeholder();
    public static final Affinity life = placeholder();
    public static final Affinity nature = placeholder();
    public static final Affinity ice = placeholder();
    public static final Affinity lightning = placeholder();
    public static final Affinity air = placeholder();
    public static final Affinity water = placeholder();
    public static final Affinity arcane = placeholder();

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    private static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void register(RegistryEvent.Register<Affinity> event) {

        IForgeRegistry<Affinity> registry = event.getRegistry();

        registry.register(new Affinity(ArsMagica.MODID, "none", 0xFFFFFF).setDirectOpposite(NONE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "ender", 0x3f043d).setDirectOpposite(LIFE_LOC).addMajorOpposite(NATURE_LOC, LIGHTNING_LOC, WATER_LOC, AIR_LOC).addMinorOpposite(ARCANE_LOC, ICE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "earth", 0x61330b).setDirectOpposite(AIR_LOC).addMajorOpposite(WATER_LOC, ARCANE_LOC, LIFE_LOC, LIGHTNING_LOC).addMinorOpposite(NATURE_LOC, FIRE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "fire", 0xef260b).setDirectOpposite(WATER_LOC).addMajorOpposite(AIR_LOC, ICE_LOC, NATURE_LOC, LIFE_LOC).addMinorOpposite(EARTH_LOC, LIGHTNING_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "life", 0x34e122).setDirectOpposite(ENDER_LOC).addMajorOpposite(ARCANE_LOC, ICE_LOC, FIRE_LOC, EARTH_LOC).addMinorOpposite(NATURE_LOC, LIGHTNING_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "nature", 0x228718).setDirectOpposite(ARCANE_LOC).addMajorOpposite(AIR_LOC, ENDER_LOC, LIGHTNING_LOC, FIRE_LOC).addMinorOpposite(LIFE_LOC, EARTH_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "ice", 0xd3e8fc).setDirectOpposite(LIGHTNING_LOC).addMajorOpposite(LIFE_LOC, FIRE_LOC, AIR_LOC, ARCANE_LOC).addMinorOpposite(WATER_LOC, ENDER_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "lightning", 0xdece19).setDirectOpposite(ICE_LOC).addMajorOpposite(WATER_LOC, ENDER_LOC, NATURE_LOC, EARTH_LOC).addMinorOpposite(LIFE_LOC, FIRE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "air", 0x777777).setDirectOpposite(EARTH_LOC).addMajorOpposite(NATURE_LOC, FIRE_LOC, ICE_LOC, ENDER_LOC).addMinorOpposite(WATER_LOC, ARCANE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "water", 0x0b5cef).setDirectOpposite(FIRE_LOC).addMajorOpposite(LIGHTNING_LOC, EARTH_LOC, ARCANE_LOC, ENDER_LOC).addMinorOpposite(AIR_LOC, ICE_LOC));
        registry.register(new Affinity(ArsMagica.MODID, "arcane", 0xb935cd).setDirectOpposite(NATURE_LOC).addMajorOpposite(LIFE_LOC, EARTH_LOC, WATER_LOC, ICE_LOC).addMinorOpposite(AIR_LOC, ENDER_LOC));

    }

    /**
     * Called in postInit to link essence items to their respective affinities
     */
    public static void postInit() {
        for (Affinity affinity : Affinity.registry.getValuesCollection()) {
            affinity.getEssenceItem(); // This will cache the essence item via the getEssenceItem lookup
        }
    }
}
