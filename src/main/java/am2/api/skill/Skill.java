package am2.api.skill;

import am2.api.ArsMagicaAPI;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class Skill extends IForgeRegistryEntry.Impl<Skill> {

    /**
     * Forge registry-based replacement for the internal affinity list.
     */
    public static IForgeRegistry<Skill> registry;

    private int posX, posY;
    private SkillTree tree;
    private String[] parents;
    private ResourceLocation icon;
    private SkillPoint point;
    private int maxLevel = 1;

    private Skill(ResourceLocation icon, SkillPoint point, int posX, int posY, SkillTree tree, String... string) {
        this.posX = posX;
        this.posY = posY;
        this.tree = tree;
        this.parents = string;
        this.icon = icon;
        this.point = point;
        this.id = nextSkillId++;
    }

    public Skill(String string, ResourceLocation icon, SkillPoint point, int posX, int posY, SkillTree tree, String... strings) {
        this(icon, point, posX, posY, tree, strings);
        this.setRegistryName(new ResourceLocation(ArsMagicaAPI.getCurrentModId(), string));
    }

    public Skill(String string, ResourceLocation icon, SkillPoint point, int posX, int posY, SkillTree tree, int maxLevel, String... strings) {
        this(icon, point, posX, posY, tree, strings);
        this.setRegistryName(new ResourceLocation(ArsMagicaAPI.getCurrentModId(), string));
        this.maxLevel = maxLevel;
    }

    /**
     * Gets an skill instance from its network ID, or null if no such skill exists.
     */
    public static Skill byNetworkID(int id) {
        if (id < 0 || id >= registry.getValuesCollection().size()) {
            return null;
        }
        return registry.getValuesCollection().stream().filter(s -> s.id == id).findAny().orElse(null);
    }

    private int id;

    public static Skill fromName(String str) {
        return ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation(str));
    }

    public final int networkID() {
        return id;
    }

    private static int nextSkillId = 0;

    public String getID() {
        return getRegistryName().toString();
    }

    public int getPosX() {
        return posX;
    }

    public int getPosY() {
        return posY;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public SkillTree getTree() {
        return tree;
    }

    public String[] getParents() {
        return parents;
    }

    public void writeToNBT(NBTTagCompound tag) {
        tag.setString("ID", getID());
    }

    public SkillPoint getPoint() {
        return point;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public String toString() {
        return getID();
    }

    public String getName() {
        return I18n.translateToLocal("skill." + getID() + ".name");
    }

    public String getOcculusDesc() {
        return I18n.translateToLocal("skill." + getID() + ".occulusdesc");
    }
}
