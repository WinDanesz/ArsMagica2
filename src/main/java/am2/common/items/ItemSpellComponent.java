package am2.common.items;

import am2.api.ArsMagicaAPI;
import am2.api.skill.Skill;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistry;

// Main purpose is to create items with spell icons (to put in item frames, etc...)
public class ItemSpellComponent extends Item {

    public ItemSpellComponent() {
        super();
        setHasSubtypes(true);
        setMaxDamage(0);
        setCreativeTab(null);
    }

    @Override
    public String getTranslationKey(ItemStack stack){
        int metadata = stack.getMetadata();
        Skill s = getSkillByID(metadata);
        return "skill." + s.getRegistryName();
    }

    public static Skill getSkillByID(int id) {
        ForgeRegistry<Skill> registry = ((ForgeRegistry<Skill>) ArsMagicaAPI.getSkillRegistry());
        Skill s = registry.getValue(id);
        return s == null ? registry.getValue(0) : s;
    }

}
