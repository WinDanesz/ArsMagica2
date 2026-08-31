package am2.api.spell;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that converts human-readable ingredient strings (as stored in the
 * {@code spell_recipes.cfg} config file) into the {@code Object[]} format expected
 * by the crafting-altar recipe system.
 *
 * <h3>Supported string formats</h3>
 * <table border="1">
 *   <tr><th>Format</th><th>Meaning</th></tr>
 *   <tr><td>{@code modid:item_name}</td><td>Item with metadata 0</td></tr>
 *   <tr><td>{@code modid:item_name:meta}</td><td>Item with explicit metadata</td></tr>
 *   <tr><td>{@code modid:item_name:meta:{nbt...}}</td><td>Item with metadata and raw JSON NBT (everything after the third colon is treated as NBT)</td></tr>
 *   <tr><td>{@code modid:item_name:*}</td><td>Item with wildcard metadata ({@link OreDictionary#WILDCARD_VALUE})</td></tr>
 *   <tr><td>{@code ore:oreDictName}</td><td>OreDict reference – passed as a plain {@code String} so the caller can expand it</td></tr>
 *   <tr><td>{@code E:type:amount}</td><td>Essence requirement – expands to two consecutive elements: the type string ({@code "E:type"}) followed by the integer amount.  Examples: {@code E:*:1500}, {@code E:1|2:1000}</td></tr>
 * </table>
 *
 * <p>Blank/whitespace-only strings are silently ignored.
 */
public final class SpellIngredientParser {

    private static final Logger LOGGER = LogManager.getLogger("ArsMagica2/SpellIngredientParser");

    private SpellIngredientParser() {}

    /**
     * Parses an array of config strings and returns the corresponding recipe {@code Object[]}.
     *
     * @param configStrings raw strings read from the config file
     * @return recipe array suitable for use in the crafting altar; never {@code null}
     */
    public static Object[] parseIngredients(String[] configStrings) {
        List<Object> result = new ArrayList<>();
        for (String raw : configStrings) {
            String s = raw.trim();
            if (s.isEmpty()) continue;

            if (s.startsWith("E:")) {
                parseEssence(s, result);
            } else if (s.startsWith("ore:")) {
                // OreDict – keep as plain string (same as existing convention)
                result.add(s.substring(4));
            } else {
                parseItem(s, result);
            }
        }
        return result.toArray();
    }

    /**
     * Serialises an existing recipe {@code Object[]} back to the config string format.
     * This is used to populate the config file with sensible defaults when a spell part
     * is first encountered.
     *
     * @param recipe the native recipe array
     * @return string representation of each ingredient; never {@code null}
     */
    public static String[] serializeRecipe(Object[] recipe) {
        if (recipe == null) return new String[0];
        List<String> result = new ArrayList<>();
        for (int i = 0; i < recipe.length; i++) {
            Object o = recipe[i];
            if (o instanceof ItemStack) {
                ItemStack stack = (ItemStack) o;
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key == null) continue;
                StringBuilder sb = new StringBuilder(key.toString());
                sb.append(':').append(stack.getMetadata());
                if (stack.hasTagCompound()) {
                    sb.append(':').append(stack.getTagCompound().toString());
                }
                result.add(sb.toString());
            } else if (o instanceof Item) {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey((Item) o);
                if (key != null) result.add(key.toString() + ":0");
            } else if (o instanceof Block) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey((Block) o);
                if (key != null) result.add(key.toString() + ":0");
            } else if (o instanceof String) {
                String s = (String) o;
                if (s.startsWith("E:")) {
                    // Consume the following Integer amount if present
                    if (i + 1 < recipe.length && recipe[i + 1] instanceof Integer) {
                        result.add(s + ":" + recipe[++i]);
                    } else {
                        result.add(s + ":0");
                    }
                } else {
                    // OreDict string
                    result.add("ore:" + s);
                }
            }
        }
        return result.toArray(new String[0]);
    }

    // -------------------------------------------------------------------------
    // Reagent helpers
    // -------------------------------------------------------------------------

    /**
     * Parses reagent config strings (same format as {@link #parseIngredients} but
     * OreDict and Essence entries are silently ignored) into a concrete
     * {@link ItemStack} array suitable for inventory checks.
     *
     * @param configStrings raw strings from the {@code [reagents]} config section
     * @return resolved item stacks; never {@code null}
     */
    public static ItemStack[] parseReagents(String[] configStrings) {
        if (configStrings == null || configStrings.length == 0) return new ItemStack[0];
        List<ItemStack> result = new ArrayList<>();
        for (String raw : configStrings) {
            String s = raw.trim();
            if (s.isEmpty() || s.startsWith("ore:") || s.startsWith("E:")) continue;
            List<Object> tmp = new ArrayList<>();
            parseItem(s, tmp);
            for (Object o : tmp) {
                if (o instanceof ItemStack) result.add((ItemStack) o);
            }
        }
        return result.toArray(new ItemStack[0]);
    }

    /**
     * Serialises an {@link ItemStack} array into the simple
     * {@code modid:item_name:meta} reagent string format.
     *
     * @param stacks the reagent stacks to serialise
     * @return string representation; never {@code null}
     */
    public static String[] serializeReagents(ItemStack[] stacks) {
        if (stacks == null || stacks.length == 0) return new String[0];
        List<String> result = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (key == null) continue;
            result.add(key + ":" + stack.getMetadata());
        }
        return result.toArray(new String[0]);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void parseEssence(String s, List<Object> result) {
        // Format: E:type:amount  e.g.  E:*:1500  or  E:1|2:1000
        // We need to split off the trailing ":amount" while keeping "E:type" intact.
        // The type itself may contain '|' but not ':'.
        String withoutPrefix = s.substring(2); // "type:amount"
        int lastColon = withoutPrefix.lastIndexOf(':');
        if (lastColon < 0) {
            LOGGER.warn("SpellIngredientParser: Missing amount in essence entry '{}' – skipping", s);
            return;
        }
        String typeStr = "E:" + withoutPrefix.substring(0, lastColon); // "E:type"
        String amountStr = withoutPrefix.substring(lastColon + 1).trim();
        try {
            int amount = Integer.parseInt(amountStr);
            result.add(typeStr);
            result.add(amount);
        } catch (NumberFormatException e) {
            LOGGER.warn("SpellIngredientParser: Invalid essence amount '{}' in '{}' – skipping", amountStr, s);
        }
    }

    private static void parseItem(String s, List<Object> result) {
        // Registry name is "namespace:path", so the first colon separates namespace from path.
        // Additional colons delimit optional meta and NBT.
        int firstColon = s.indexOf(':');
        if (firstColon < 0) {
            LOGGER.warn("SpellIngredientParser: Invalid ingredient '{}' (no colon) – skipping", s);
            return;
        }
        int secondColon = s.indexOf(':', firstColon + 1);

        String registryName;
        int meta = 0;
        NBTTagCompound nbt = null;

        if (secondColon < 0) {
            // Plain "namespace:path"
            registryName = s;
        } else {
            registryName = s.substring(0, secondColon);
            String rest = s.substring(secondColon + 1);

            // Check if the meta portion ends at the next colon (start of NBT) or at end-of-string
            int thirdColon = rest.indexOf(':');
            String metaStr;
            if (thirdColon < 0) {
                metaStr = rest;
            } else {
                metaStr = rest.substring(0, thirdColon);
                String nbtStr = rest.substring(thirdColon + 1).trim();
                if (!nbtStr.isEmpty()) {
                    try {
                        nbt = JsonToNBT.getTagFromJson(nbtStr);
                    } catch (NBTException e) {
                        LOGGER.warn("SpellIngredientParser: Invalid NBT in '{}': {} – ignoring NBT", s, e.getMessage());
                    }
                }
            }

            metaStr = metaStr.trim();
            if (metaStr.equals("*") || metaStr.equalsIgnoreCase("wildcard")) {
                meta = OreDictionary.WILDCARD_VALUE;
            } else {
                try {
                    meta = Integer.parseInt(metaStr);
                } catch (NumberFormatException e) {
                    LOGGER.warn("SpellIngredientParser: Invalid metadata '{}' in '{}' – using 0", metaStr, s);
                }
            }
        }

        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        if (item == null) {
            LOGGER.warn("SpellIngredientParser: Unknown item '{}' in '{}' – skipping", registryName, s);
            return;
        }

        ItemStack stack = new ItemStack(item, 1, meta);
        if (nbt != null) {
            stack.setTagCompound(nbt);
        }
        result.add(stack);
    }
}
