package am2.common.config;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.skill.Skill;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;

public class SpellPartConfiguration extends Configuration {

    private final ArrayList<String> disabled = new ArrayList<>();

    public SpellPartConfiguration(File file) {
        super(file);
    }

    public ArrayList<String> getDisabledSkills(boolean reload) {

        if (reload) {
            load();
            disabled.clear();
            for (ResourceLocation rl : ArsMagicaAPI.getSpellRegistry().getKeys()) {
                String name = rl.toString();
                if (name.startsWith(ArsMagica.MODID)) name = rl.getPath();
                Property prop = this.get("enabled_spell_part", name, true);
                prop.setRequiresWorldRestart(true);
                if (!prop.getBoolean())
                    disabled.add(name);
            }
            save();
        }

        return disabled;
    }

    public boolean isSkillDisabled(String name) {
        name = name.replaceAll(ArsMagica.MODID, "");
        for (String str : disabled) {
            if (str.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public int[] getDisabledSkillIDs() {
        ArrayList<Integer> intArray = new ArrayList<>();
        for (String disabled : disabled) {
            ResourceLocation rl = new ResourceLocation(disabled);
            if (!disabled.contains(":")) rl = new ResourceLocation(ArsMagica.MODID + disabled);
            intArray.add(ArsMagicaAPI.getSkillRegistry().getValue(rl).networkID());
        }
        int[] ret = new int[intArray.size()];
        for (int i = 0; i < intArray.size(); i++) {
            int d = intArray.get(i).intValue();
            ret[i] = d;
        }
        return ret;
    }

    public void disableAllSkillsIn(int[] disabledSkills) {
        disabled.clear();
        for (int i : disabledSkills) {
            ResourceLocation rl = Skill.byNetworkID(i).getRegistryName();
            String name = rl.toString();
            if (name.startsWith(ArsMagica.MODID)) name = rl.getPath();
            disabled.add(name);
        }
    }
}
