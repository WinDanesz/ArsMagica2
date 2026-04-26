package am2.common.registry;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.common.ObeliskFuelHelper;
import am2.common.recipe.RecipeArmorDye;
import am2.common.recipe.RecipeSpellBookDye;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AMRecipes {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void addRecipes() {
        // Obelisk Fuel Registration - Vinteum Dust
        ObeliskFuelHelper.instance.registerFuelType(s -> {
            if (s.getItem() == AMItems.vinteum_dust)
                return 200;
            return 0;
        });

        // Obelisk Fuel Registration - Liquid Essence
        ObeliskFuelHelper.instance.registerFuelType(s -> {
            FluidStack stack = FluidUtil.getFluidContained(s);
            if (stack == null || !stack.getFluid().getName().equals("liquid_essence"))
                return 0;
            return stack.amount * 2;
        });

        // Obelisk Fuel Registration - Extra fuels from config
        registerExtraFuels();

        // Dynamic Flicker Focus Recipes - registered via API
        for (AbstractFlickerFunctionality func : ArsMagicaAPI.getFlickerFocusRegistry()) {
            if (func != null) {
                Object[] recipeItems = func.getRecipe();
                if (recipeItems != null) {
                    ResourceLocation location = new ResourceLocation(ArsMagica.MODID, "flicker_focus_" + func.getID());
                    IRecipe recipe = new ShapedOreRecipe(location, new ItemStack(AMItems.flicker_focus, 1, func.getID()), recipeItems);
                    recipe.setRegistryName(location);
                    ForgeRegistries.RECIPES.register(recipe);
                } else {
                    LOGGER.info("Flicker operator {} was registered with no recipe. It is un-craftable. This may have been intentional.", func.getClass().getSimpleName());
                }
            }
        }

        // Spellbook Dye Recipe - preserves NBT (spells, enchantments, etc.)
        RecipeSpellBookDye spellBookDyeRecipe = new RecipeSpellBookDye();
        spellBookDyeRecipe.setRegistryName(new ResourceLocation(ArsMagica.MODID, "spellbook_dye"));
        ForgeRegistries.RECIPES.register(spellBookDyeRecipe);

        // Armor Dye Recipe - preserves NBT (infusions, damage, enchantments, etc.)
        RecipeArmorDye armorDyeRecipe = new RecipeArmorDye();
        armorDyeRecipe.setRegistryName(new ResourceLocation(ArsMagica.MODID, "armor_dye"));
        ForgeRegistries.RECIPES.register(armorDyeRecipe);
    }

    private static void registerExtraFuels() {
        String[] entries = ArsMagica.config.getObeliskExtraFuels();
        if (entries == null) return;
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            int eqIdx = entry.lastIndexOf('=');
            if (eqIdx < 1) {
                LOGGER.warn("Malformed obelisk_extra_fuels entry '{}' – expected format modid:item=burntime or modid:item:meta=burntime. Skipping.", entry);
                continue;
            }
            String itemSpec = entry.substring(0, eqIdx).trim();
            String burnStr = entry.substring(eqIdx + 1).trim();
            int burnTime;
            try {
                burnTime = Integer.parseInt(burnStr);
            } catch (NumberFormatException e) {
                LOGGER.warn("Malformed burn time '{}' in obelisk_extra_fuels entry '{}'. Skipping.", burnStr, entry);
                continue;
            }
            if (burnTime <= 0) {
                LOGGER.warn("Burn time must be positive in obelisk_extra_fuels entry '{}'. Skipping.", entry);
                continue;
            }

            // Parse itemSpec: "modid:item" or "modid:item:meta"
            String[] parts = itemSpec.split(":");
            if (parts.length < 2 || parts.length > 3) {
                LOGGER.warn("Malformed item specifier '{}' in obelisk_extra_fuels entry '{}'. Skipping.", itemSpec, entry);
                continue;
            }
            ResourceLocation rl = new ResourceLocation(parts[0], parts[1]);
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null || item == net.minecraft.init.Items.AIR) {
                LOGGER.info("Obelisk extra fuel item '{}' not found (mod not loaded?). Skipping.", rl);
                continue;
            }

            int meta = -1; // -1 = any metadata
            if (parts.length == 3) {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Malformed metadata '{}' in obelisk_extra_fuels entry '{}'. Skipping.", parts[2], entry);
                    continue;
                }
            }

            final int finalMeta = meta;
            final int finalBurnTime = burnTime;
            ObeliskFuelHelper.instance.registerFuelType(s -> {
                if (s.getItem() != item) return 0;
                if (finalMeta >= 0 && s.getMetadata() != finalMeta) return 0;
                return finalBurnTime;
            });
            LOGGER.info("Registered obelisk extra fuel: {} = {} ticks", itemSpec, burnTime);
        }
    }
}
