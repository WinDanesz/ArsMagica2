package am2.api.extensions;

import am2.api.SkillPointRegistry;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.common.extensions.SkillData;
import am2.common.utils.NBTUtils;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import am2.common.skill.Discipline;

public interface ISkillData {

    public boolean hasSkill(String name);

    public void unlockSkill(String name);

    public int getSkillLevel(String name);

    public void setSkillLevel(String name, int level);

    public HashMap<Skill, Integer> getSkills();

    public HashMap<SkillPoint, Integer> getSkillPoints();

    public int getSkillPoint(SkillPoint skill);

    public void setSkillPoint(SkillPoint point, int num);

    public int getDisciplineLevel(Discipline discipline);

    public void setDisciplineLevel(Discipline discipline, int level);

    public HashMap<String, Integer> getDisciplineLevels();

    public int getDiscoveryPointsSpent(Discipline discipline);

    public void setDiscoveryPointsSpent(Discipline discipline, int value);

    public int getAvailableDiscoveryPoints(Discipline discipline);

    public void spendDiscoveryPoint(Discipline discipline);

    public HashMap<String, Integer> getDiscoveryPointsSpentMap();

    public boolean canLearn(String name);

    public boolean shouldUpdate();

    public byte[] generateUpdatePacket();

    public void handleUpdatePacket(byte[] bytes);

    public void forceUpdate();


    public static class Storage implements IStorage<ISkillData> {

        @Override
        public NBTBase writeNBT(Capability<ISkillData> capability, ISkillData instance, EnumFacing side) {
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagCompound am2Tag = NBTUtils.getAM2Tag(nbt);
            NBTTagList skillList = NBTUtils.addCompoundList(am2Tag, "Skills");
            NBTTagList skillPointList = NBTUtils.addCompoundList(am2Tag, "SkillPoints");
            for (Entry<Skill, Integer> skill : instance.getSkills().entrySet()) {
                if (skill.getKey() == null)
                    continue;
                NBTTagCompound tmp = new NBTTagCompound();
                tmp.setString("Skill", skill.getKey().getID());
                tmp.setInteger("Level", skill.getValue());
                skillList.appendTag(tmp);
            }
            for (Entry<SkillPoint, Integer> skill : instance.getSkillPoints().entrySet()) {
                if (skill.getKey() == null)
                    continue;
                NBTTagCompound tmp = new NBTTagCompound();
                tmp.setString("Type", skill.getKey().getName());
                tmp.setInteger("Number", skill.getValue());
                skillPointList.appendTag(tmp);
            }
            am2Tag.setTag("Skills", skillList);
            am2Tag.setTag("SkillPoints", skillPointList);
            NBTTagCompound disciplineTag = new NBTTagCompound();
            for (Entry<String, Integer> entry : instance.getDisciplineLevels().entrySet()) {
                disciplineTag.setInteger(entry.getKey(), entry.getValue());
            }
            am2Tag.setTag("Disciplines", disciplineTag);
            NBTTagCompound discoverySpentTag = new NBTTagCompound();
            for (Entry<String, Integer> entry : instance.getDiscoveryPointsSpentMap().entrySet()) {
                discoverySpentTag.setInteger(entry.getKey(), entry.getValue());
            }
            am2Tag.setTag("DiscoveryPointsSpent", discoverySpentTag);
            return nbt;
        }

        @Override
        public void readNBT(Capability<ISkillData> capability, ISkillData instance, EnumFacing side, NBTBase nbt) {
            NBTTagCompound am2Tag = NBTUtils.getAM2Tag((NBTTagCompound) nbt);
            NBTTagList skillList = NBTUtils.addCompoundList(am2Tag, "Skills");
            NBTTagList skillPointList = NBTUtils.addCompoundList(am2Tag, "SkillPoints");
            for (int i = 0; i < skillList.tagCount(); i++) {
                NBTTagCompound tmp = skillList.getCompoundTagAt(i);
                int level = tmp.hasKey("Level") ? tmp.getInteger("Level") : (tmp.getBoolean("Unlocked") ? 1 : 0);
                if (level > 0)
                    instance.setSkillLevel(tmp.getString("Skill"), level);
            }
            for (int i = 0; i < skillPointList.tagCount(); i++) {
                NBTTagCompound tmp = skillPointList.getCompoundTagAt(i);
                instance.setSkillPoint(SkillPointRegistry.fromName(tmp.getString("Type")), tmp.getInteger("Number"));
            }
            if (am2Tag.hasKey("Disciplines")) {
                NBTTagCompound disciplineTag = am2Tag.getCompoundTag("Disciplines");
                for (Discipline d : Discipline.values()) {
                    if (disciplineTag.hasKey(d.getName())) {
                        instance.setDisciplineLevel(d, disciplineTag.getInteger(d.getName()));
                    }
                }
            }
            if (am2Tag.hasKey("DiscoveryPointsSpent")) {
                NBTTagCompound discoverySpentTag = am2Tag.getCompoundTag("DiscoveryPointsSpent");
                for (Discipline d : Discipline.values()) {
                    if (discoverySpentTag.hasKey(d.getName())) {
                        instance.setDiscoveryPointsSpent(d, discoverySpentTag.getInteger(d.getName()));
                    }
                }
            }
        }
    }

    public static class Factory implements Callable<ISkillData> {
        @Override
        public ISkillData call() throws Exception {
            return new SkillData();
        }
    }

    public ArrayList<String> getKnownShapes();

    public ArrayList<String> getKnownComponents();

    public ArrayList<String> getKnownModifiers();
}
