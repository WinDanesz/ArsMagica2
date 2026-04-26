package am2.common.items;

import am2.ArsMagica;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Tiered premade spell NBT templates for the Magic Staff, parsed from config.
 * <p>
 * Config format: {@code tier:modid:part1:modid:part2:...:Display Name}
 * <br>
 * Registry names use full {@code modid:name} format. The last token (if not
 * part of a registry-name pair) is used as the item display name.
 * <p>
 * Example: {@code 0:arsmagica2:projectile:arsmagica2:fire_damage:Fire Staff}
 */
public final class StaffPresets {

    private StaffPresets() {}

    private static Map<Integer, List<Preset>> tiers = new HashMap<>();
    private static int maxTier = 0;

    /**
     * Parses the config entries. Call once during init.
     */
    public static void init(String[] entries) {
        tiers.clear();
        maxTier = 0;

        if (entries == null || entries.length == 0) return;

        for (String entry : entries) {
            if (entry == null || entry.trim().isEmpty()) continue;

            String[] tokens = entry.trim().split(":");
            // Minimum: tier + one registry name (2 tokens) + display name = 4
            if (tokens.length < 4) {
                ArsMagica.LOGGER.warn("StaffPresets: skipping malformed entry '{}' (need tier:modid:part:...:DisplayName)", entry);
                continue;
            }

            int tier;
            try {
                tier = Integer.parseInt(tokens[0].trim());
            } catch (NumberFormatException e) {
                ArsMagica.LOGGER.warn("StaffPresets: skipping entry '{}' - tier '{}' is not a number", entry, tokens[0]);
                continue;
            }

            // After the tier, remaining tokens are pairs (modid:name) plus a trailing display name.
            // Remaining count: tokens.length - 1
            int remaining = tokens.length - 1;
            boolean hasDisplayName = (remaining % 2 != 0);
            int pairCount = remaining / 2;

            if (pairCount < 1) {
                ArsMagica.LOGGER.warn("StaffPresets: skipping entry '{}' - need at least one modid:part pair", entry);
                continue;
            }

            String[] parts = new String[pairCount];
            for (int i = 0; i < pairCount; i++) {
                int base = 1 + i * 2;
                parts[i] = tokens[base].trim() + ":" + tokens[base + 1].trim();
            }

            String displayName = hasDisplayName ? tokens[tokens.length - 1].trim() : null;

            tiers.computeIfAbsent(tier, k -> new ArrayList<>()).add(new Preset(parts, displayName));
            if (tier > maxTier) maxTier = tier;
        }

        ArsMagica.LOGGER.info("StaffPresets: loaded {} tiers, {} total presets",
                tiers.size(), tiers.values().stream().mapToInt(List::size).sum());
    }

    /** The highest tier currently defined. */
    public static int getMaxTier() {
        return maxTier;
    }

    /**
     * Returns a random spell NBT for the given tier.
     */
    public static NBTTagCompound random(int tier) {
        return random(tier, new Random());
    }

    /**
     * Returns a random spell NBT for the given tier using the provided RNG.
     */
    public static NBTTagCompound random(int tier, Random rand) {
        List<Preset> pool = getPool(tier);
        if (pool == null || pool.isEmpty()) return new NBTTagCompound();
        return pool.get(rand.nextInt(pool.size())).build();
    }

    /**
     * Returns the number of presets in the given tier.
     */
    public static int count(int tier) {
        List<Preset> pool = getPool(tier);
        return pool == null ? 0 : pool.size();
    }

    /**
     * Returns a specific preset by tier and index.
     */
    public static NBTTagCompound get(int tier, int index) {
        List<Preset> pool = getPool(tier);
        if (pool == null || pool.isEmpty()) return new NBTTagCompound();
        int i = Math.max(0, Math.min(index, pool.size() - 1));
        return pool.get(i).build();
    }

    // ==================== Internal ====================

    private static List<Preset> getPool(int tier) {
        int t = Math.max(0, Math.min(tier, maxTier));
        List<Preset> pool = tiers.get(t);
        if (pool != null) return pool;
        for (int i = t - 1; i >= 0; i--) {
            pool = tiers.get(i);
            if (pool != null) return pool;
        }
        return null;
    }

    private static final class Preset {
        final String[] parts;
        final String displayName;

        Preset(String[] parts, String displayName) {
            this.parts = parts;
            this.displayName = displayName;
        }

        NBTTagCompound build() {
            NBTTagCompound root = new NBTTagCompound();

            root.setLong("UUIDMostSignificantBits", 0L);
            root.setLong("UUIDLeastSignificantBits", System.nanoTime());
            root.setInteger("CurrentShapeGroup", 0);

            NBTTagList shapeGroups = new NBTTagList();
            NBTTagCompound group = new NBTTagCompound();
            group.setInteger("ID", 0);
            group.setFloat("BaseManaCost", 10.0f);
            group.setTag("StoredData", new NBTTagCompound());

            NBTTagList stages = new NBTTagList();
            NBTTagCompound stage = new NBTTagCompound();
            stage.setInteger("ID", 0);
            NBTTagList partsList = new NBTTagList();
            for (String part : parts) {
                partsList.appendTag(new NBTTagString(part));
            }
            stage.setTag("Parts", partsList);
            stages.appendTag(stage);
            group.setTag("Group", stages);

            shapeGroups.appendTag(group);
            root.setTag("ShapeGroups", shapeGroups);

            root.setTag("SpellCommon", new NBTTagList());
            root.setTag("AffinityShift", new NBTTagList());
            root.setTag("StoredData", new NBTTagCompound());

            if (displayName != null && !displayName.isEmpty()) {
                NBTTagCompound display = new NBTTagCompound();
                display.setString("Name", displayName);
                root.setTag("display", display);
            }

            return root;
        }
    }
}
