package am2.common.registry;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Registers smelting recipes for Ars Magica 2.
 * These need to be registered programmatically in 1.12.2 since JSON smelting recipes are only supported in 1.13+.
 */
public class AMSmeltingRecipes {

    public static void register() {
        // Arcane Compound -> Arcane Ash (2x output)
        GameRegistry.addSmelting(new ItemStack(AMItems.arcane_compound, 1), new ItemStack(AMItems.arcane_ash, 2), 0.0f);

        // Ore Smelting
        GameRegistry.addSmelting(new ItemStack(AMBlocks.vinteum_ore), new ItemStack(AMItems.vinteum_dust), 0.7f);
        GameRegistry.addSmelting(new ItemStack(AMBlocks.sunstone_ore), new ItemStack(AMItems.sunstone), 0.7f);
        GameRegistry.addSmelting(new ItemStack(AMBlocks.blue_topaz_ore), new ItemStack(AMItems.blue_topaz), 0.7f);
        GameRegistry.addSmelting(new ItemStack(AMBlocks.chimerite_ore), new ItemStack(AMItems.chimerite), 0.7f);
        GameRegistry.addSmelting(new ItemStack(AMBlocks.moonstone_ore), new ItemStack(AMItems.moonstone), 0.7f);

        // Witchwood Log -> Coal
        GameRegistry.addSmelting(new ItemStack(AMBlocks.witchwood_log), new ItemStack(Items.COAL), 0.15f);
    }
}
