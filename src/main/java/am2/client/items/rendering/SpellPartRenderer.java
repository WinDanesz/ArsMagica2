package am2.client.items.rendering;

import am2.api.skill.Skill;
import am2.common.items.ItemSpellComponent;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;

public class SpellPartRenderer implements ItemMeshDefinition {

    public SpellPartRenderer() {
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
        int metadata = stack.getMetadata();
        Skill s = ItemSpellComponent.getSkillByID(metadata);
        return new ModelResourceLocation(s.getRegistryName(), "inventory");
    }

}
