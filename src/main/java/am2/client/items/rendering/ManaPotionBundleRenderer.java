package am2.client.items.rendering;

import am2.common.registry.AMItems;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class ManaPotionBundleRenderer implements ItemMeshDefinition {

    private ModelResourceLocation defaultLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString(), "inventory");
    private ModelResourceLocation lesserLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString() + "_lesser", "inventory");
    private ModelResourceLocation standardLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString() + "_standard", "inventory");
    private ModelResourceLocation greaterLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString() + "_greater", "inventory");
    private ModelResourceLocation epicLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString() + "_epic", "inventory");
    private ModelResourceLocation legendaryLoc = new ModelResourceLocation(AMItems.mana_potion_bundle.getRegistryName().toString() + "_legendary", "inventory");

    public ManaPotionBundleRenderer() {
        ModelBakery.registerItemVariants(AMItems.mana_potion_bundle, defaultLoc, lesserLoc, standardLoc, greaterLoc, epicLoc, legendaryLoc);

    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        int meta = stack.getItemDamage();
        if (meta < (1 << 8)) return lesserLoc;
        if (meta < (2 << 8)) return standardLoc;
        if (meta < (3 << 8)) return greaterLoc;
        if (meta < (4 << 8)) return epicLoc;
        if (meta < (5 << 8)) return legendaryLoc;
        return defaultLoc;
    }

}
