package am2.api.affinity;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.common.registry.Affinities;
import am2.common.utils.NBTUtils;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.ArrayList;

/**
 * Affinity :<BR>
 * When creating an affinity you are going to need a few textures :<BR>
 * -An essence texture, located at (domain):essence_(name)<BR>
 * -An affinity tome texture, located at (domain):affinity_tome_(name)<BR>
 * -A rune texture, located at (domain):runes/rune_(name)<BR>
 *
 * @author EdwinMindcraft, WinDanesz
 *
 */
public class Affinity extends IForgeRegistryEntry.Impl<Affinity> implements Comparable<Affinity> {

    /**
     * Forge registry-based replacement for the internal affinity list.
     */
    public static IForgeRegistry<Affinity> registry;

    /**
     * Returns the metadata for this affinity, which corresponds to its registry ID, or -1 if the affinity has not been
     * registered.<br>
     * <br>
     * Because of how the registry system works, this won't change once assigned for a given world so is guaranteed to
     * be backwards-compatible by design. <b>However, for this reason if affinities are removed there may be gaps in the ID
     * numbers.</b> If a continuous set of IDs is required (for networking), use {@link Affinity#networkID()}.
     */
    public final int metadata() {
        return ((ForgeRegistry<Affinity>) registry).getID(this);
    }

    /**
     * Returns this affinity's network ID number, similar to mod-specific entity IDs.<br>
     * <br>
     * Unlike {@link Affinity#metadata()}, this is guaranteed to be sequential so is suitable for indexed lookup.
     * <b>However, it may change if affinities are removed so is not backwards-compatible.</b> This means it should not be
     * used for data storage.
     */
    public final int networkID() {
        return id;
    }

    private static int nextAffinityId = 0;

    /**
     * The affinity's integer ID, mainly used for networking.
     */
    private int id;

    private int color;
    private String name;
    private ResourceLocation directOpposite;
    private ArrayList<ResourceLocation> majorOpposites = new ArrayList<>();
    private ArrayList<ResourceLocation> minorOpposites = new ArrayList<>();

    /**
     * The essence item for this affinity, set after item registration.
     */
    private Item essenceItem;

    /**
     * The unlocalised name of the affinity.
     */
    private final String unlocalisedName;

    public Affinity(String modID, String name, int color) {
        this.setRegistryName(modID, name);
        this.unlocalisedName = this.getRegistryName().toString();
        this.color = color;
        this.name = name;
        this.id = nextAffinityId++;
    }

    /**
     * Gets an affinity instance from its network ID, or the none affinity if no such affinity exists.
     */
    public static Affinity byNetworkID(int id) {
        if (id < 0 || id >= registry.getValuesCollection().size()) {
            return Affinities.none;
        }
        return registry.getValuesCollection().stream().filter(s -> s.id == id).findAny().orElse(Affinities.none);
    }

    /**
     * Rendering purpose
     * Will return the color used by the occulus to render this affinity depth
     *
     * @return the color of the affinity
     */
    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getRegistryName().toString();
    }


    /**
     *
     * @return the localized name of the affinity
     */
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public String getLocalizedName() {
        return net.minecraft.client.resources.I18n.format(getTranslationKey());
    }

    /**
     *
     * @return the unlocalized name of the affinity
     */
    public String getUnlocalisedName() {
        return unlocalisedName;
    }

    /**
     * Returns the translation key for this spell.
     */
    public String getTranslationKey() {
        return "affinity." + unlocalisedName;
    }

    /**
     * Returns the translated display name of the affinity, without formatting (i.e. not coloured). <b>Client-side
     * only!</b> On the server side, use {@link TextComponentTranslation} (see {@link Spell#getNameForTranslation()}).
     */

    /**
     * Returns a {@code TextComponentTranslation} which will be translated to the display name of the spell, without
     * formatting (i.e. not coloured).
     */
    public ITextComponent getNameForTranslation() {
        return new TextComponentTranslation(getTranslationKey());
    }

    public static Affinity byMetadata(int metadata) {
        Affinity spell = ((ForgeRegistry<Affinity>) registry).getValue(metadata);
        return spell == null ? Affinities.none : spell;
    }

    public String getDisplayName() {
        return ArsMagica.proxy.translate(getTranslationKey());
    }

    /**
     * Will write to an existing {@link NBTTagCompound} the data of the affinity
     *
     * @param tag      Root tag of the entity / item
     * @param affinity The affinity to add
     * @param depth    The the depth of this affinity
     */
    public static void writeToNBT(NBTTagCompound tag, Affinity affinity, double depth) {
        NBTTagList affinityTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(tag), "Affinity");
        NBTTagCompound tmp = new NBTTagCompound();
        tmp.setString("Name", affinity.getRegistryName().toString());
        tmp.setDouble("Depth", depth);
        affinityTag.appendTag(tmp);
        NBTUtils.getAM2Tag(tag).setTag("Affinity", affinityTag);
    }

    /**
     * Will list all the affinities the player / item has if given a correct {@link NBTTagCompound}
     *
     * @param tag Root tag of the entity / item
     * @return A list of all the affinities this player / item has
     */
    public static ArrayList<Affinity> readFromNBT(NBTTagCompound tag) {
        NBTTagList affinityTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(tag), "Affinity");
        ArrayList<Affinity> affinities = new ArrayList<Affinity>();
        for (int i = 0; i < affinityTag.tagCount(); i++) {
            NBTTagCompound tmp = affinityTag.getCompoundTagAt(i);
            Affinity aff = ArsMagicaAPI.getAffinityRegistry().getValue(new ResourceLocation(tmp.getString("Name")));
            affinities.add(aff);
        }
        return affinities;
    }

    /**
     * Will give the depth of an affinity the player / item has if given a correct {@link NBTTagCompound}
     *
     * @param tag Root tag of the entity / item
     * @return Depth in the affinity
     */
    public double readDepth(NBTTagCompound tag) {
        NBTTagList affinityTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(tag), "Affinity");
        for (int i = 0; i < affinityTag.tagCount(); i++) {
            NBTTagCompound tmp = affinityTag.getCompoundTagAt(i);
            if (tmp.getString("Name").equals(this.getRegistryName().toString()))
                return tmp.getDouble("Depth");
        }
        return 0.0F;
    }


    public ArrayList<Affinity> getMinorOpposingAffinities() {
        ArrayList<Affinity> returnList = new ArrayList<>();
        for (ResourceLocation rl : minorOpposites) {
            Affinity aff = ArsMagicaAPI.getAffinityRegistry().getValue(rl);
            if (aff != Affinities.none)
                returnList.add(aff);
        }
        return returnList;
    }

    public ArrayList<Affinity> getMajorOpposingAffinities() {
        ArrayList<Affinity> returnList = new ArrayList<>();
        for (ResourceLocation rl : majorOpposites) {
            Affinity aff = ArsMagicaAPI.getAffinityRegistry().getValue(rl);
            if (aff != Affinities.none)
                returnList.add(aff);
        }
        return returnList;
    }

    public ArrayList<Affinity> getAdjacentAffinities() {
        ArrayList<Affinity> returnList = new ArrayList<>();
        for (ResourceLocation rl : ArsMagicaAPI.getAffinityRegistry().getKeys()) {
            Affinity aff = ArsMagicaAPI.getAffinityRegistry().getValue(rl);
            if (aff == Affinities.none || majorOpposites.contains(rl) || minorOpposites.contains(rl) || directOpposite == rl || aff == this)
                continue;
            returnList.add(aff);
        }
        return returnList;
    }

    public Affinity getOpposingAffinity() {
        return ArsMagicaAPI.getAffinityRegistry().getValue(directOpposite);
    }

    public Affinity setDirectOpposite(ResourceLocation directOpposite) {
        this.directOpposite = directOpposite;
        return this;
    }

    public Affinity addMajorOpposite(ResourceLocation... rls) {
        for (ResourceLocation rl : rls)
            majorOpposites.add(rl);
        return this;
    }

    public Affinity addMinorOpposite(ResourceLocation... rls) {
        for (ResourceLocation rl : rls)
            minorOpposites.add(rl);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Affinity)
            return ((Affinity) obj).delegate.equals(this.delegate);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return this.getRegistryName().hashCode();
    }

    @Override
    public int compareTo(Affinity o) {
        return 0;
    }

    public int getID() {
        return id;
    }

    public void setEssenceItem(net.minecraft.item.Item item) {
        this.essenceItem = item;
    }

    public net.minecraft.item.Item getEssenceItem() {
        if (essenceItem == null) {
            ResourceLocation location = new ResourceLocation(this.getRegistryName().getNamespace(), "essence_" + this.getRegistryName().getPath());
            essenceItem = ForgeRegistries.ITEMS.getValue(location);
        }
        return essenceItem;
    }
}
