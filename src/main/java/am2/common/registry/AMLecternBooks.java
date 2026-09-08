package am2.common.registry;

import am2.ArsMagica;
import am2.common.config.AMConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class AMLecternBooks {

    private static final Map<Item, Short> books = new HashMap<>();
    private static final Map<Item, GuiHandlerSpec> guiHandlers = new HashMap<>();
    private static final Map<Item, BookColors> bookColors = new HashMap<>();

    private AMLecternBooks() {

    }

    public static void init() {
        // default
        add(new ItemStack(Items.WRITTEN_BOOK));
        add(new ItemStack(AMItems.arcane_compendium));

        // EBW config
        AMConfig config = ArsMagica.config;
        if (config.getEBWizDiscoveryEnabled()) {
            add(new ItemStack(Items.WRITABLE_BOOK));
        }
        if (config.getEBWizTranscriptionEnabled()) {
            // TODO
            // Colors (not user-configurable like the other entries below): this placement is
            // conditional on a separate flag the Crafting Altar's transcription ritual owns, so it
            // can't just be a lectern_book_colors default like the others - that list has no way to
            // track a toggle that isn't its own. Same wildcard metadata as before, so this doesn't
            // clobber the plain placement registration it's replacing.
            tryAddBookColors("ebwizardry:spell_book:-1=9C2828,551515,551515");
        }
        // Wizard's Handbook: placement only - BlockLectern's client-only fallback already knows
        // how to open it (EBWizardryCompatHandler references EBWizardry's real GuiWizardHandbook
        // directly, since EBWizardry is a genuine compile dependency, not a reflected one).
        tryAdd("ebwizardry:wizard_handbook");

        // generic integration - placement only
        for (String id : config.getLecternBooks()) {
            tryAdd(id);
        }

        // generic integration - placement + read-in-place GUI (Thaumcraft's Thaumonomicon,
        // Botania's Lexicon, Antique Atlas, etc. are configured as defaults; see
        // AMConfig.KEY_LecternGuiHandlers)
        for (String entry : config.getLecternGuiHandlers()) {
            tryAddGuiHandler(entry);
        }

        // generic integration - per-book recoloring of the floating book model (see AMConfig.KEY_LecternBookColors)
        for (String entry : config.getLecternBookColors()) {
            tryAddBookColors(entry);
        }
    }

    public static boolean isVaild(ItemStack stack) {
        if(stack == null || stack.isEmpty() || !books.containsKey(stack.getItem()))
            return false;
        int metadata = books.get(stack.getItem());
        return metadata == OreDictionary.WILDCARD_VALUE || metadata == stack.getMetadata();
    }

    public static void add(ItemStack stack) {
        books.put(stack.getItem(), (short) stack.getMetadata());
    }

    /**
     * Opens the GUI registered for {@code stack} via the {@code lectern_gui_handlers} config
     * entries (if any), mirroring that item's own onItemRightClick GUI-opening call. Safe to call
     * unconditionally on either logical side: it only touches generic Forge APIs plus whatever
     * reflective call or GUI screen class the matching config entry specified - no compile-time
     * dependency on any mod's classes.
     *
     * @return true if a GUI was (or is being) opened for {@code stack}.
     */
    public static boolean openGui(EntityPlayer player, World world, ItemStack stack) {
        if (stack.isEmpty())
            return false;

        GuiHandlerSpec spec = guiHandlers.get(stack.getItem());
        if (spec == null)
            return false;

        if (spec.call != null) {
            try {
                spec.call.invoke(stack);
            } catch (ReflectiveOperationException e) {
                ArsMagica.LOGGER.info("Failed to invoke lectern GUI handler for {} ({}: {}).", stack.getItem().getRegistryName(), e.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        }

        if (spec.guiId != null) {
            player.openGui(spec.modInstance, spec.guiId, world, 0, 0, 0);
        }

        if (spec.guiScreenCtor != null && world.isRemote) {
            try {
                Minecraft.getMinecraft().displayGuiScreen((GuiScreen) spec.guiScreenCtor.newInstance(stack));
            } catch (ReflectiveOperationException e) {
                ArsMagica.LOGGER.info("Failed to construct lectern GUI screen for {} ({}: {}).", stack.getItem().getRegistryName(), e.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        }

        if (spec.guiScreenField != null && world.isRemote) {
            try {
                Object screen = spec.guiScreenField.get(null);
                if (screen != null) {
                    Minecraft.getMinecraft().displayGuiScreen((GuiScreen) screen);
                }
            } catch (ReflectiveOperationException e) {
                ArsMagica.LOGGER.info("Failed to read lectern GUI screen field for {} ({}: {}).", stack.getItem().getRegistryName(), e.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the custom cover/frame/pixel colors registered for {@code stack} via the
     * {@code lectern_book_colors} config, or {@code null} if none is configured (the vanilla
     * brown book should be used as-is).
     */
    public static BookColors getBookColors(ItemStack stack) {
        return stack.isEmpty() ? null : bookColors.get(stack.getItem());
    }

    private static void tryAdd(String id) {
        ItemStack parsed = parseItemStack(id, AMConfig.KEY_LecternBooks);
        if(parsed != null)
           add(parsed);
    }

    /**
     * Parses a {@code modid:item[:meta]=token[,token]} entry from {@code lectern_gui_handlers}.
     * Each token is either a plain integer (a Forge IGuiHandler gui id, opened via
     * {@code player.openGui(...)}) or a {@code fully.qualified.Class#staticField.instanceMethod}
     * reflective call (invoked with the stack being read) - order doesn't matter, and only the
     * tokens a given mod actually needs have to be present: a gui id alone (Thaumcraft's
     * Thaumonomicon) opens directly; a call alone (Antique Atlas) is invoked with no gui id
     * involved at all; both together (Botania's Lexicon) invoke the call first - to prime the
     * target mod's proxy with the stack - then open the gui id.
     *
     * <p>The item is always registered for placement (like a plain {@code lectern_books} entry)
     * as long as it resolves; the GUI half is layered on top of that only if at least one token
     * also resolves.
     *
     * <p>A third token kind is accepted alongside the two described above: a bare
     * {@code fully.qualified.GuiScreenClass} name (no {@code #}, not a plain integer) naming a
     * client {@link GuiScreen} subclass with an {@code (ItemStack)} constructor. It is
     * instantiated with the stack being read and shown directly via
     * {@code Minecraft.displayGuiScreen(...)} - for mods (like AncientSpellcraft's
     * {@code GuiAncientElementSpellBook}) whose lectern-readable GUI is just a screen built
     * straight from the stack, with no {@code IGuiHandler} gui id or proxy method to hook into.
     *
     * <p>A fourth token kind is a {@code fully.qualified.Class#staticField} spec with no trailing
     * {@code .method} (distinguished from the reflective-call token by having no {@code .} after
     * the {@code #}): the named static field must already hold a ready-to-show {@link GuiScreen}
     * instance (a singleton-style screen, e.g. AbyssalCraft's {@code GuiNecronomicon.currentNecro}),
     * which is read and shown as-is via {@code Minecraft.displayGuiScreen(...)} with no stack or
     * argument involved at all.
     */
    private static void tryAddGuiHandler(String entry) {
        Logger LOGGER = ArsMagica.LOGGER;
        String configLabel = AMConfig.KEY_LecternGuiHandlers;

        entry = entry.trim();
        if (entry.isEmpty())
            return;

        int eq = entry.indexOf('=');
        if (eq < 0) {
            LOGGER.warn("Malformed entry '{}' in {} (expected '...=...'). Skipping.", entry, configLabel);
            return;
        }

        ItemStack stack = parseItemStack(entry.substring(0, eq), configLabel);
        if (stack == null)
            return;
        add(stack); // placement is implied from here on, regardless of whether the GUI half below succeeds

        Integer guiId = null;
        ReflectiveCall call = null;
        Constructor<?> guiScreenCtor = null;
        Field guiScreenField = null;
        for (String token : entry.substring(eq + 1).split(",")) {
            token = token.trim();
            if (token.isEmpty())
                continue;
            int hash = token.indexOf('#');
            if (hash >= 0) {
                if (token.lastIndexOf('.') > hash) {
                    call = tryResolveCall(token, entry, configLabel);
                    if (call == null)
                        return; // already logged; keep placement-only rather than risk a stale/wrong-content GUI
                } else {
                    guiScreenField = tryResolveGuiScreenField(token, entry, configLabel);
                    if (guiScreenField == null)
                        return; // already logged; keep placement-only rather than risk a stale/wrong-content GUI
                }
            } else {
                Integer parsedGuiId = tryParseGuiId(token);
                if (parsedGuiId != null) {
                    guiId = parsedGuiId;
                } else {
                    guiScreenCtor = tryResolveGuiScreenCtor(token, entry, configLabel);
                    if (guiScreenCtor == null)
                        return; // already logged; keep placement-only rather than risk a stale/wrong-content GUI
                }
            }
        }

        if (guiId == null && call == null && guiScreenCtor == null && guiScreenField == null) {
            LOGGER.warn("Entry '{}' in {} has no gui id, reflective call, or GUI screen source - nothing will happen when it's read. Item will still be placeable.", entry, configLabel);
            return;
        }

        Object modInstance = null;
        if (guiId != null) {
            ResourceLocation id = stack.getItem().getRegistryName();
            ModContainer container = id == null ? null : Loader.instance().getIndexedModList().get(id.getNamespace());
            if (container == null)
                return; // can't happen in practice: parseItemStack already resolved this item through that mod's registry
            modInstance = container.getMod();
        }

        guiHandlers.put(stack.getItem(), new GuiHandlerSpec(modInstance, guiId, call, guiScreenCtor, guiScreenField));
    }

    /**
     * Parses a {@code modid:item[:meta]=coverHex,frameHex,pixelsHex[,paperHex]} entry from
     * {@code lectern_book_colors}. Like {@link #tryAddGuiHandler}, the item is always registered
     * for placement as long as it resolves, independently of whether the colors also parse.
     * {@code paperHex} is optional and defaults to white, which is a no-op tint (see
     * {@link BookColors#paper}) so existing 3-color entries keep the vanilla page look.
     */
    private static void tryAddBookColors(String entry) {
        Logger LOGGER = ArsMagica.LOGGER;
        String configLabel = AMConfig.KEY_LecternBookColors;

        entry = entry.trim();
        if (entry.isEmpty())
            return;

        int eq = entry.indexOf('=');
        if (eq < 0) {
            LOGGER.warn("Malformed entry '{}' in {} (expected '...=coverHex,frameHex,pixelsHex[,paperHex]'). Skipping.", entry, configLabel);
            return;
        }

        ItemStack stack = parseItemStack(entry.substring(0, eq), configLabel);
        if (stack == null)
            return;
        add(stack); // placement is implied from here on, regardless of whether the colors below parse

        String[] hexes = entry.substring(eq + 1).split(",", -1);
        if (hexes.length != 3 && hexes.length != 4) {
            LOGGER.warn("Malformed entry '{}' in {} (expected 3 or 4 comma-separated hex colors). Skipping colors.", entry, configLabel);
            return;
        }

        Integer cover = tryParseHexColor(hexes[0], entry, configLabel);
        Integer frame = tryParseHexColor(hexes[1], entry, configLabel);
        Integer pixels = tryParseHexColor(hexes[2], entry, configLabel);
        Integer paper = hexes.length == 4 ? tryParseHexColor(hexes[3], entry, configLabel) : 0xFFFFFF;
        if (cover == null || frame == null || pixels == null || paper == null)
            return; // already logged

        bookColors.put(stack.getItem(), new BookColors(cover, frame, pixels, paper));
    }

    private static Integer tryParseHexColor(String hex, String entry, String configLabel) {
        hex = hex.trim();
        if (hex.startsWith("#"))
            hex = hex.substring(1);
        try {
            return 0xFFFFFF & Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            ArsMagica.LOGGER.warn("Malformed hex color '{}' in '{}' ({}). Skipping colors.", hex, entry, configLabel);
            return null;
        }
    }

    private static Integer tryParseGuiId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Resolves a {@code fully.qualified.Class#staticField.instanceMethod} spec into a callable. */
    private static ReflectiveCall tryResolveCall(String spec, String entry, String configLabel) {
        int hash = spec.indexOf('#');
        int dot = spec.lastIndexOf('.');
        if (dot < hash + 1) {
            ArsMagica.LOGGER.warn("Malformed GUI handler spec '{}' in '{}' ({}). Expected fully.qualified.Class#staticField.instanceMethod. GUI handler skipped.", spec, entry, configLabel);
            return null;
        }

        String className = spec.substring(0, hash);
        String fieldName = spec.substring(hash + 1, dot);
        String methodName = spec.substring(dot + 1);

        try {
            Field field = Class.forName(className).getField(fieldName);
            Method method = field.getType().getMethod(methodName, ItemStack.class);
            return stack -> method.invoke(field.get(null), stack);
        } catch (ReflectiveOperationException e) {
            ArsMagica.LOGGER.info("GUI handler target '{}' in {} not resolvable ({}: {}); mod may not be loaded, or its internals changed. GUI handler skipped for this entry.", spec, configLabel, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Resolves a bare {@code fully.qualified.GuiScreenClass} token from {@code lectern_gui_handlers}
     * into its {@code (ItemStack)} constructor. The class must be a {@link GuiScreen} subclass.
     */
    private static Constructor<?> tryResolveGuiScreenCtor(String className, String entry, String configLabel) {
        try {
            Class<?> guiClass = Class.forName(className);
            if (!GuiScreen.class.isAssignableFrom(guiClass)) {
                ArsMagica.LOGGER.warn("Class '{}' in '{}' ({}) is not a GuiScreen. GUI handler skipped for this entry.", className, entry, configLabel);
                return null;
            }
            return guiClass.getConstructor(ItemStack.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            ArsMagica.LOGGER.info("GUI handler class '{}' in {} not resolvable ({}: {}); mod may not be loaded, or its internals changed. GUI handler skipped for this entry.", className, configLabel, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Resolves a bare {@code fully.qualified.Class#staticField} token (no trailing {@code .method})
     * from {@code lectern_gui_handlers} into the static field itself. The field's declared type
     * must be a {@link GuiScreen} subclass; its value is read fresh each time the book is opened,
     * so mods that mutate the singleton in place (e.g. via their own read flow) stay in sync.
     */
    private static Field tryResolveGuiScreenField(String spec, String entry, String configLabel) {
        int hash = spec.indexOf('#');
        String className = spec.substring(0, hash);
        String fieldName = spec.substring(hash + 1);

        try {
            Field field = Class.forName(className).getField(fieldName);
            if (!GuiScreen.class.isAssignableFrom(field.getType())) {
                ArsMagica.LOGGER.warn("Field '{}' in '{}' ({}) is not a GuiScreen. GUI handler skipped for this entry.", spec, entry, configLabel);
                return null;
            }
            return field;
        } catch (ReflectiveOperationException | LinkageError e) {
            ArsMagica.LOGGER.info("GUI handler field '{}' in {} not resolvable ({}: {}); mod may not be loaded, or its internals changed. GUI handler skipped for this entry.", spec, configLabel, e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private static ItemStack parseItemStack(String entry, String configLabel) {
        Logger LOGGER = ArsMagica.LOGGER;

        entry = entry.trim();
        if (entry.isEmpty())
            return null;

        // Parse itemSpec: "modid:item", "modid:item:meta"
        String[] parts = entry.split(":");
        if (parts.length < 2 || parts.length > 3) {
            LOGGER.warn("Malformed item specifier '{}' in {}. Skipping.", entry, configLabel);
            return null;
        }
        String modid = parts[0];
        String itemid = parts[1];

        ResourceLocation rl = new ResourceLocation(modid, itemid);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null || item == Items.AIR) {
            LOGGER.info("Item '{}' in {} not found (mod not loaded?). Skipping.", rl, configLabel);
            return null;
        }

        Integer metadata = parts.length == 3 ? tryParseInt(parts[parts.length - 1]) : null;
        if (parts.length == 3 && metadata == null) {
            LOGGER.warn("Malformed metadata '{}' ('{}') in {}. Skipping.", parts[2], entry, configLabel);
            return null;
        }

        return new ItemStack(item, 1, metadata == null ? 0 : metadata);
    }

    private static Integer tryParseInt(String contaiter) {
        try {
            int ret = Integer.parseInt(contaiter);
            if (ret < -1)
                return null;
            if (ret == -1)
                return OreDictionary.WILDCARD_VALUE;
        }
        catch (NumberFormatException ignored) {
        }
        return null;
    }

    @FunctionalInterface
    private interface ReflectiveCall {
        void invoke(ItemStack stack) throws ReflectiveOperationException;
    }

    /** RGB (0xRRGGBB, no alpha) colors for the paintable regions of the floating book model. */
    public static final class BookColors {
        public final int cover;
        public final int frame;
        public final int pixels;
        /** Tints the pages' grayscale shading; 0xFFFFFF (white) leaves them unchanged. */
        public final int paper;

        BookColors(int cover, int frame, int pixels, int paper) {
            this.cover = cover;
            this.frame = frame;
            this.pixels = pixels;
            this.paper = paper;
        }
    }

    private static final class GuiHandlerSpec {
        final Object modInstance;
        final Integer guiId;
        final ReflectiveCall call;
        final Constructor<?> guiScreenCtor;
        final Field guiScreenField;

        GuiHandlerSpec(Object modInstance, Integer guiId, ReflectiveCall call, Constructor<?> guiScreenCtor, Field guiScreenField) {
            this.modInstance = modInstance;
            this.guiId = guiId;
            this.call = call;
            this.guiScreenCtor = guiScreenCtor;
            this.guiScreenField = guiScreenField;
        }
    }

}
