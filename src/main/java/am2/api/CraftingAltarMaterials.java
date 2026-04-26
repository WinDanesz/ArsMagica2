package am2.api;

import am2.common.registry.AMBlocks;
import am2.common.utils.KeyValuePair;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map.Entry;

public class CraftingAltarMaterials {

    private static final HashMap<IBlockState, Integer> caps = new HashMap<>();
    private static final HashMap<KeyValuePair<IBlockState, IBlockState>, Integer> main = new HashMap<>();

    static {
        addCapsMaterial(Blocks.GLASS.getDefaultState(), 1);
        addCapsMaterial(Blocks.COAL_BLOCK.getDefaultState(), 2);
        addCapsMaterial(Blocks.REDSTONE_BLOCK.getDefaultState(), 3);
        addCapsMaterial(Blocks.IRON_BLOCK.getDefaultState(), 4);
        addCapsMaterial(Blocks.LAPIS_BLOCK.getDefaultState(), 5);
        addCapsMaterial(Blocks.GOLD_BLOCK.getDefaultState(), 6);
        addCapsMaterial(Blocks.DIAMOND_BLOCK.getDefaultState(), 7);
        addCapsMaterial(Blocks.EMERALD_BLOCK.getDefaultState(), 8);
        addCapsMaterial(AMBlocks.moonstone_block.getDefaultState(), 9);
        addCapsMaterial(AMBlocks.sunstone_block.getDefaultState(), 10);

        addMainMaterial(Blocks.PLANKS.getDefaultState(), Blocks.OAK_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.ACACIA), Blocks.ACACIA_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.BIRCH), Blocks.BIRCH_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.SPRUCE), Blocks.SPRUCE_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.JUNGLE), Blocks.JUNGLE_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK), Blocks.DARK_OAK_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.NETHER_BRICK.getDefaultState(), Blocks.NETHER_BRICK_STAIRS.getDefaultState(), 3);
        addMainMaterial(Blocks.QUARTZ_BLOCK.getDefaultState(), Blocks.QUARTZ_STAIRS.getDefaultState(), 3);
        addMainMaterial(Blocks.STONEBRICK.getDefaultState(), Blocks.STONE_BRICK_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.SANDSTONE.getDefaultState(), Blocks.SANDSTONE_STAIRS.getDefaultState(), 1);
        addMainMaterial(Blocks.PURPUR_BLOCK.getDefaultState(), Blocks.PURPUR_STAIRS.getDefaultState(), 4);
        addMainMaterial(Blocks.BRICK_BLOCK.getDefaultState(), Blocks.BRICK_STAIRS.getDefaultState(), 2);
        addMainMaterial(Blocks.RED_SANDSTONE.getDefaultState(), Blocks.RED_SANDSTONE_STAIRS.getDefaultState(), 2);
        addMainMaterial(AMBlocks.witchwood_planks.getDefaultState(), AMBlocks.witchwood_stairs.getDefaultState(), 3);
    }

    public static void addCapsMaterial(IBlockState state, int value) {
        caps.put(state, Integer.valueOf(value));
    }

    public static void addMainMaterial(IBlockState state, IBlockState stairs, int value) {
        main.put(new KeyValuePair<>(state, stairs), Integer.valueOf(value));
    }

    public static ImmutableMap<IBlockState, Integer> getCapsMap() {
        return ImmutableMap.copyOf(caps);
    }

    public static ImmutableMap<KeyValuePair<IBlockState, IBlockState>, Integer> getMainMap() {
        return ImmutableMap.copyOf(main);
    }

    public static ImmutableMap<IBlockState, Integer> getSimpleMainMap() {
        ImmutableMap.Builder<IBlockState, Integer> builder = ImmutableMap.builder();
        for (Entry<KeyValuePair<IBlockState, IBlockState>, Integer> entry : getMainMap().entrySet()) {
            builder.put(entry.getKey().key, entry.getValue());
        }
        return builder.build();
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Parses a block specifier like "modid:block" or "modid:block:meta" into an IBlockState.
     * Returns null if the block is not found or the format is invalid.
     */
    private static IBlockState parseBlockState(String spec) {
        String[] parts = spec.split(":");
        if (parts.length < 2 || parts.length > 3) return null;
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(parts[0], parts[1]));
        if (block == null || block == Blocks.AIR) return null;
        int meta = 0;
        if (parts.length == 3) {
            try {
                meta = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return block.getStateFromMeta(meta);
    }

    /**
     * Registers extra cap materials from config entries.
     * Format: "modid:block:meta=tier" or "modid:block=tier".
     * Meta defaults to 0 if omitted.
     */
    public static void registerConfigCaps(String[] entries) {
        if (entries == null) return;
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            int eqIdx = entry.lastIndexOf('=');
            if (eqIdx < 1) {
                LOGGER.warn("Malformed extra_altar_caps entry '{}' – expected format modid:block:meta=tier. Skipping.", entry);
                continue;
            }
            String blockSpec = entry.substring(0, eqIdx).trim();
            String tierStr = entry.substring(eqIdx + 1).trim();
            int tier;
            try {
                tier = Integer.parseInt(tierStr);
            } catch (NumberFormatException e) {
                LOGGER.warn("Malformed tier '{}' in extra_altar_caps entry '{}'. Skipping.", tierStr, entry);
                continue;
            }
            if (tier < 1) {
                LOGGER.warn("Tier must be positive in extra_altar_caps entry '{}'. Skipping.", entry);
                continue;
            }
            IBlockState state = parseBlockState(blockSpec);
            if (state == null) {
                LOGGER.info("Extra altar cap block '{}' not found (mod not loaded?). Skipping.", blockSpec);
                continue;
            }
            addCapsMaterial(state, tier);
            LOGGER.info("Registered extra altar cap material: {} = tier {}", blockSpec, tier);
        }
    }

    /**
     * Registers extra main (structure) materials from config entries.
     * Format: "modid:block:meta,modid:stairs:meta=tier".
     * Meta defaults to 0 if omitted on either specifier.
     */
    public static void registerConfigMain(String[] entries) {
        if (entries == null) return;
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            int eqIdx = entry.lastIndexOf('=');
            if (eqIdx < 1) {
                LOGGER.warn("Malformed extra_altar_main entry '{}' – expected format modid:block:meta,modid:stairs:meta=tier. Skipping.", entry);
                continue;
            }
            String pairSpec = entry.substring(0, eqIdx).trim();
            String tierStr = entry.substring(eqIdx + 1).trim();
            int tier;
            try {
                tier = Integer.parseInt(tierStr);
            } catch (NumberFormatException e) {
                LOGGER.warn("Malformed tier '{}' in extra_altar_main entry '{}'. Skipping.", tierStr, entry);
                continue;
            }
            if (tier < 1) {
                LOGGER.warn("Tier must be positive in extra_altar_main entry '{}'. Skipping.", entry);
                continue;
            }
            String[] pair = pairSpec.split(",");
            if (pair.length != 2) {
                LOGGER.warn("Malformed block pair '{}' in extra_altar_main entry '{}' – expected block,stairs. Skipping.", pairSpec, entry);
                continue;
            }
            IBlockState plankState = parseBlockState(pair[0].trim());
            IBlockState stairState = parseBlockState(pair[1].trim());
            if (plankState == null) {
                LOGGER.info("Extra altar main block '{}' not found (mod not loaded?). Skipping.", pair[0].trim());
                continue;
            }
            if (stairState == null) {
                LOGGER.info("Extra altar main stairs '{}' not found (mod not loaded?). Skipping.", pair[1].trim());
                continue;
            }
            addMainMaterial(plankState, stairState, tier);
            LOGGER.info("Registered extra altar main material: {} + {} = tier {}", pair[0].trim(), pair[1].trim(), tier);
        }
    }
}