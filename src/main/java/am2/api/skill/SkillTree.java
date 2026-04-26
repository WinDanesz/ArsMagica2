package am2.api.skill;

import am2.api.ArsMagicaAPI;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class SkillTree {

    private String name;
    private ResourceLocation background;
    private ResourceLocation icon;
    private boolean canRender = true;
    private String unlock = null;

    public SkillTree(String name, ResourceLocation background, ResourceLocation icon) {
        this.name = name.toLowerCase();
        this.background = background;
        this.icon = icon;
    }

    public static ArrayList<Skill> getSkillsForTree(SkillTree tree) {
        ArrayList<Skill> skillList = new ArrayList<Skill>();
        for (Skill skill : ArsMagicaAPI.getSkillRegistry().getValues()) {
            if (skill != null && skill.getTree() != null && skill.getTree().equals(tree))
                skillList.add(skill);
        }
        return skillList;
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getBackground() {
        return background;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public String getUnlocalizedName() {
        return "skilltree." + name;
    }

    public String getLocalizedName() {
        return I18n.format(getUnlocalizedName());
    }

    public SkillTree disableRender(String compendiumUnlock) {
        canRender = false;
        this.unlock = compendiumUnlock;
        return this;
    }

    public String getUnlock() {
        return unlock;
    }

    public boolean canRender() {
        return canRender;
    }
}
