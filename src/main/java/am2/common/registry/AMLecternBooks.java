package am2.common.registry;

import am2.ArsMagica;
import am2.common.ObeliskFuelHelper;
import am2.common.config.AMConfig;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public final class AMLecternBooks {

    private static final Map<Item, Short> books = new HashMap<>();

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
            tryAdd("ebwizardry:spell_book:-1");
        }

        // generic integration
        for (String id : config.getLecternBooks()) {
            tryAdd(id);
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

    private static void tryAdd(String id) {
        ItemStack parsed = parseItemStack(id);
        if(parsed != null)
           add(parsed);
    }

    private static ItemStack parseItemStack(String entry) {
        Logger LOGGER = ArsMagica.LOGGER;
        String configLabel = AMConfig.KEY_LecternBooks;

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
        if (item == null || item == net.minecraft.init.Items.AIR) {
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

}
