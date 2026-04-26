package am2.api.extensions;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.spell.SpellData;
import am2.api.spell.SpellPart;
import am2.common.utils.NBTUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;

/**
 * Capability for spells.<BR>
 * Doing a write -> read of this will remove any invalid data.
 */
public interface ISpellCaster {
    float getManaCost(@Nullable World world, EntityLivingBase caster);

    SpellData createSpellData(ItemStack source);

    boolean cast(ItemStack source, World world, EntityLivingBase caster);

    //Getters
    List<List<SpellPart>> getSpellCommon();

    List<List<List<SpellPart>>> getShapeGroups();

    Map<Affinity, Float> getAffinityShift();

    int getShapeGroupCount();

    float getBaseManaCost(int shapeGroup);

    UUID getSpellUUID();

    int getCurrentShapeGroup();

    /** Returns true if the given shape group index contains at least one spell part. */
    default boolean isShapeGroupNonEmpty(int group) {
        List<List<List<SpellPart>>> groups = getShapeGroups();
        if (group < 0 || group >= groups.size()) return false;
        for (List<SpellPart> stage : groups.get(group)) {
            if (stage != null && !stage.isEmpty()) return true;
        }
        return false;
    }

    NBTTagCompound getStoredData(int shapeGroup);

    NBTTagCompound getCommonStoredData();

    //Setters
    void setSpellCommon(@Nullable List<List<SpellPart>> data);

    void setShapeGroups(@Nullable List<List<List<SpellPart>>> data);

    void setAffinityShift(@Nullable Map<Affinity, Float> shift);

    void setBaseManaCost(int shapeGroup, float manaCost);

    void setUUID(@Nullable UUID uuid);

    void setCurentShapeGroup(int shapeGroup);

    void setStoredData(int shapeGroup, NBTTagCompound tag);

    void setCommonStoredData(NBTTagCompound tag);

    //Gatherers
    void gatherBaseManaCosts();

    void gatherAffinityShift();

    void generateUUID();

    boolean validate();


    public static class Storage implements IStorage<ISpellCaster> {

        private static final String KEY_BASE_MANA_COST = "BaseManaCost";
        private static final String KEY_UUID_MOST = "UUIDMostSignificantBits";
        private static final String KEY_UUID_LEAST = "UUIDLeastSignificantBits";
        private static final String KEY_SPELL_COMMON = "SpellCommon";
        private static final String KEY_PARTS = "Parts";
        private static final String KEY_ID = "ID";
        private static final String KEY_SHAPE_GROUPS = "ShapeGroups";
        private static final String KEY_GROUP = "Group";
        private static final String KEY_AFFINITY = "AffinityShift";
        private static final String KEY_AFFINITY_TYPE = "Type";
        private static final String KEY_AFFINITY_DEPTH = "Depth";
        private static final String KEY_CURRENT_SHAPE_GROUP = "CurrentShapeGroup";
        private static final String KEY_STORED_DATA = "StoredData";


        @Override
        public NBTBase writeNBT(Capability<ISpellCaster> capability, ISpellCaster instance, EnumFacing side) {
            NBTTagCompound compound = new NBTTagCompound();
            compound.setLong(KEY_UUID_MOST, instance.getSpellUUID().getMostSignificantBits());
            compound.setLong(KEY_UUID_LEAST, instance.getSpellUUID().getLeastSignificantBits());
            compound.setInteger(KEY_CURRENT_SHAPE_GROUP, instance.getCurrentShapeGroup());
            NBTTagList spellCommon = new NBTTagList();
            List<List<SpellPart>> commonStages = instance.getSpellCommon();
            if (commonStages != null) {
                for (int i = 0; i < commonStages.size(); i++) {
                    NBTTagCompound tmp = new NBTTagCompound();
                    tmp.setInteger(KEY_ID, i);
                    NBTTagList stageTag = new NBTTagList();
                    List<SpellPart> parts = commonStages.get(i);
                    if (parts != null) {
                        for (SpellPart p : parts) {
                            stageTag.appendTag(new NBTTagString(p.getRegistryName().toString()));
                        }
                    }
                    tmp.setTag(KEY_PARTS, stageTag);
                    spellCommon.appendTag(tmp);
                }
            }
            compound.setTag(KEY_SPELL_COMMON, spellCommon);

            List<List<List<SpellPart>>> shapeGroups = instance.getShapeGroups();
            NBTTagList shapeGroupsTag = new NBTTagList();
            if (shapeGroups != null) {
                for (int i = 0; i < shapeGroups.size(); i++) {
                    NBTTagCompound groupTag = new NBTTagCompound();
                    groupTag.setInteger(KEY_ID, i);
                    groupTag.setTag(KEY_STORED_DATA, instance.getStoredData(i));
                    groupTag.setFloat(KEY_BASE_MANA_COST, instance.getBaseManaCost(i));
                    NBTTagList stagesTag = new NBTTagList();
                    List<List<SpellPart>> stages = shapeGroups.get(i);
                    if (stages != null) {
                        for (int j = 0; j < stages.size(); j++) {
                            NBTTagCompound tmp = new NBTTagCompound();
                            tmp.setInteger(KEY_ID, j);
                            NBTTagList stageTag = new NBTTagList();
                            List<SpellPart> parts = stages.get(j);
                            if (parts != null) {
                                for (SpellPart p : parts) {
                                    stageTag.appendTag(new NBTTagString(p.getRegistryName().toString()));
                                }
                            }
                            tmp.setTag(KEY_PARTS, stageTag);
                            stagesTag.appendTag(tmp);
                        }
                    }
                    groupTag.setTag(KEY_GROUP, stagesTag);
                    shapeGroupsTag.appendTag(groupTag);
                }
            }
            compound.setTag(KEY_SHAPE_GROUPS, shapeGroupsTag);
            NBTTagList affinityShift = new NBTTagList();
            for (Entry<Affinity, Float> entry : instance.getAffinityShift().entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    NBTTagCompound tmp = new NBTTagCompound();
                    tmp.setString(KEY_AFFINITY_TYPE, entry.getKey().getRegistryName().toString());
                    tmp.setFloat(KEY_AFFINITY_DEPTH, entry.getValue().floatValue());
                    affinityShift.appendTag(tmp);
                }
            }
            compound.setTag(KEY_AFFINITY, affinityShift);
            compound.setTag(KEY_STORED_DATA, instance.getCommonStoredData());
            return compound;
        }

        @Override
        public void readNBT(Capability<ISpellCaster> capability, ISpellCaster instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound compound = (NBTTagCompound) nbt;
            instance.setUUID(new UUID(compound.getLong(KEY_UUID_MOST), compound.getLong(KEY_UUID_LEAST)));
            // NOTE: do NOT call setCurentShapeGroup here – shapeGroups is still empty,
            // so MathHelper.clamp would always produce 0. We apply it after setShapeGroups below.
            int savedShapeGroup = compound.getInteger(KEY_CURRENT_SHAPE_GROUP);
            NBTTagList spellCommon = compound.getTagList(KEY_SPELL_COMMON, Constants.NBT.TAG_COMPOUND);
            ArrayList<List<SpellPart>> commonStages = new ArrayList<>(spellCommon.tagCount());
            for (int i = 0; i < spellCommon.tagCount(); i++) {
                NBTTagCompound tmp = spellCommon.getCompoundTagAt(i);
                int id = tmp.getInteger(KEY_ID);
                NBTTagList parts = tmp.getTagList(KEY_PARTS, Constants.NBT.TAG_STRING);
                ArrayList<SpellPart> pts = new ArrayList<>();
                for (int j = 0; j < parts.tagCount(); j++) {
                    SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(parts.getStringTagAt(j)));
                    if (part != null) {
                        pts.add(part);
                    }
                }
                NBTUtils.ensureSize(commonStages, id + 1);
                commonStages.set(id, pts);
            }
            instance.setSpellCommon(commonStages);
            NBTTagList shapeGroupsTag = compound.getTagList(KEY_SHAPE_GROUPS, Constants.NBT.TAG_COMPOUND);
            ArrayList<List<List<SpellPart>>> shapeGroups = new ArrayList<>(shapeGroupsTag.tagCount());
            for (int i = 0; i < shapeGroupsTag.tagCount(); i++) {
                NBTTagCompound group = shapeGroupsTag.getCompoundTagAt(i);
                int gid = group.getInteger(KEY_ID);
                instance.setBaseManaCost(gid, group.getFloat(KEY_BASE_MANA_COST));
                instance.setStoredData(gid, group.getCompoundTag(KEY_STORED_DATA));
                NBTTagList stagesLs = group.getTagList(KEY_GROUP, Constants.NBT.TAG_COMPOUND);
                ArrayList<List<SpellPart>> stages = new ArrayList<>(stagesLs.tagCount());
                for (int j = 0; j < stagesLs.tagCount(); j++) {
                    NBTTagCompound tmp = stagesLs.getCompoundTagAt(j);
                    int id = tmp.getInteger(KEY_ID);
                    NBTTagList parts = tmp.getTagList(KEY_PARTS, Constants.NBT.TAG_STRING);
                    ArrayList<SpellPart> pts = new ArrayList<>();
                    for (int k = 0; k < parts.tagCount(); k++) {
                        SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(parts.getStringTagAt(k)));
                        if (part != null) {
                            pts.add(part);
                        }
                    }
                    NBTUtils.ensureSize(stages, id + 1);
                    stages.set(id, pts);
                }
                NBTUtils.ensureSize(shapeGroups, gid + 1);
                shapeGroups.set(gid, stages);
            }
            instance.setShapeGroups(shapeGroups);
            // Now that shapeGroups is populated, clamp works correctly.
            instance.setCurentShapeGroup(savedShapeGroup);
            NBTTagList affinityShift = compound.getTagList(KEY_AFFINITY, Constants.NBT.TAG_COMPOUND);
            HashMap<Affinity, Float> affMap = new HashMap<>();
            for (int i = 0; i < affinityShift.tagCount(); i++) {
                NBTTagCompound tmp = affinityShift.getCompoundTagAt(i);
                Affinity aff = ArsMagicaAPI.getAffinityRegistry().getValue(new ResourceLocation(tmp.getString(KEY_AFFINITY_TYPE)));
                float depth = tmp.getFloat(KEY_AFFINITY_DEPTH);
                if (depth != 0 && aff != null)
                    affMap.put(aff, depth);
            }
            instance.setAffinityShift(affMap);
            instance.setCommonStoredData(compound.getCompoundTag(KEY_STORED_DATA));
        }
    }


}
