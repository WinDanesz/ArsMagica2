package am2.common.extensions;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellPart;
import am2.api.spell.SpellShape;
import am2.common.advancement.AMAdvancementTriggers;
import am2.common.lore.ArcaneCompendium;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.skill.Discipline;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class SkillData implements ISkillData, ICapabilityProvider, ICapabilitySerializable<NBTBase> {

    private EntityPlayer player;
    public static final ResourceLocation ID = new ResourceLocation("arsmagica2:SkillData");

    private static final int SYNC_SKILLS = 0x1;
    private static final int SYNC_SKILL_POINTS = 0x2;
    private static final int SYNC_DISCIPLINES = 0x4;

    private int syncCode = 0;

    private HashMap<Skill, Integer> skills;
    private HashMap<SkillPoint, Integer> skillPoints;
    private HashMap<String, Integer> disciplineLevels;
    private HashMap<String, Integer> discoveryPointsSpent;

    @CapabilityInject(value = ISkillData.class)
    public static Capability<ISkillData> INSTANCE = null;

    public SkillData() {
        this.skills = new HashMap<>();
        this.skillPoints = new HashMap<>();
        this.disciplineLevels = new HashMap<>();
        this.discoveryPointsSpent = new HashMap<>();
        for (Discipline d : Discipline.values()) {
            this.disciplineLevels.put(d.getName(), 0);
            this.discoveryPointsSpent.put(d.getName(), 0);
        }
    }

    public static ISkillData For(EntityLivingBase living) {
        return living.getCapability(INSTANCE, null);
    }

    @Override
    public HashMap<Skill, Integer> getSkills() {
        return this.skills;
    }

    @Override
    public boolean hasSkill(String name) {
        if (this.player.capabilities.isCreativeMode) return true;
        if (ArsMagica.disabledSkills.isSkillDisabled(name)) return true;
        Integer level = this.skills.get(Skill.fromName(name));
        return level != null && level > 0;
    }

    @Override
    public int getSkillLevel(String name) {
        if (this.player.capabilities.isCreativeMode) {
            Skill s = Skill.fromName(name);
            return s != null ? s.getMaxLevel() : 0;
        }
        Integer level = this.skills.get(Skill.fromName(name));
        return level == null ? 0 : level;
    }

    @Override
    public void setSkillLevel(String name, int level) {
        Skill skill = Skill.fromName(name);
        if (skill == null) return;
        level = Math.max(0, Math.min(level, skill.getMaxLevel()));
        this.skills.put(skill, level);
        this.syncCode |= SYNC_SKILLS;
    }

    @Override
    public void unlockSkill(String name) {
        if (Skill.fromName(name) == null)
            return;
        Skill skill = Skill.fromName(name);

        int currentLevel = this.skills.getOrDefault(skill, 0);
        if (currentLevel >= skill.getMaxLevel()) return;

        if (currentLevel == 0) {
            for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
                if (ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName()) != null) {
                    SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
                    for (Object obj : entry.getObjects()) {
                        if (obj == part) {
                            ArcaneCompendium.For(this.player).unlockEntry(entry.getID());
                        }
                    }
                } else {
                    for (Object obj : entry.getObjects()) {
                        if (obj == skill) {
                            ArcaneCompendium.For(this.player).unlockEntry(entry.getID());
                        }
                    }
                }
            }
        }

        this.setSkillPoint(skill.getPoint(), this.getSkillPoint(skill.getPoint()) - 1);
        this.skills.put(skill, currentLevel + 1);
        this.syncCode |= SYNC_SKILLS;

        if (!this.player.world.isRemote && this.player instanceof EntityPlayerMP) {
            boolean allUnlocked = this.skills.entrySet().stream()
                    .filter(e -> !ArsMagica.disabledSkills.isSkillDisabled(e.getKey().getRegistryName().toString()))
                    .allMatch(e -> e.getValue() >= 1);
            if (allUnlocked) {
                AMAdvancementTriggers.ALL_SKILLS_UNLOCKED.triggerFor(this.player);
            }
        }
    }

    @Override
    public HashMap<SkillPoint, Integer> getSkillPoints() {
        return this.skillPoints;
    }

    @Override
    public int getSkillPoint(SkillPoint point) {
        if (point == null)
            return 0;
        Integer integer = this.skillPoints.get(point);
        return integer == null ? 0 : integer;
    }

    @Override
    public void setSkillPoint(SkillPoint point, int num) {
        if (!this.skillPoints.containsKey(point) || this.skillPoints.get(point) != num) {
            this.skillPoints.put(point, num);
            this.syncCode |= SYNC_SKILL_POINTS;
        }
    }

    public void init(EntityPlayer entity) {
        this.player = entity;
        for (Skill aff : ArsMagicaAPI.getSkillRegistry().getValues()) {
            this.skills.put(aff, 0);
        }
        for (SkillPoint aff : SkillPointRegistry.getSkillPointMap().values()) {
            this.skillPoints.put(aff, 0);
        }
        this.skillPoints.put(SkillPoint.BLUE_SKILL_POINT, 3);
        for (Discipline d : Discipline.values()) {
            this.disciplineLevels.putIfAbsent(d.getName(), 0);
        }
    }

    public int getDisciplineLevel(Discipline discipline) {
        Integer level = this.disciplineLevels.get(discipline.getName());
        return level == null ? 0 : level;
    }

    public void setDisciplineLevel(Discipline discipline, int level) {
        level = Math.max(0, Math.min(level, Discipline.MAX_LEVEL));
        if (!this.disciplineLevels.containsKey(discipline.getName()) || this.disciplineLevels.get(discipline.getName()) != level) {
            this.disciplineLevels.put(discipline.getName(), level);
            this.syncCode |= SYNC_DISCIPLINES;
        }
    }

    public boolean canLevelUpDiscipline(Discipline discipline) {
        int currentLevel = getDisciplineLevel(discipline);
        if (currentLevel >= Discipline.MAX_LEVEL) return false;
        SkillPoint required = Discipline.getRequiredSkillPoint(currentLevel);
        return getSkillPoint(required) > 0;
    }

    public void levelUpDiscipline(Discipline discipline) {
        int currentLevel = getDisciplineLevel(discipline);
        if (currentLevel >= Discipline.MAX_LEVEL) return;
        SkillPoint required = Discipline.getRequiredSkillPoint(currentLevel);
        if (getSkillPoint(required) <= 0) return;
        setSkillPoint(required, getSkillPoint(required) - 1);
        setDisciplineLevel(discipline, currentLevel + 1);
    }

    public HashMap<String, Integer> getDisciplineLevels() {
        return this.disciplineLevels;
    }

    @Override
    public int getDiscoveryPointsSpent(Discipline discipline) {
        Integer spent = this.discoveryPointsSpent.get(discipline.getName());
        return spent == null ? 0 : spent;
    }

    @Override
    public void setDiscoveryPointsSpent(Discipline discipline, int value) {
        value = Math.max(0, value);
        if (!this.discoveryPointsSpent.containsKey(discipline.getName()) || this.discoveryPointsSpent.get(discipline.getName()) != value) {
            this.discoveryPointsSpent.put(discipline.getName(), value);
            this.syncCode |= SYNC_DISCIPLINES;
        }
    }

    @Override
    public int getAvailableDiscoveryPoints(Discipline discipline) {
        return getDisciplineLevel(discipline) - getDiscoveryPointsSpent(discipline);
    }

    @Override
    public void spendDiscoveryPoint(Discipline discipline) {
        if (getAvailableDiscoveryPoints(discipline) > 0) {
            setDiscoveryPointsSpent(discipline, getDiscoveryPointsSpent(discipline) + 1);
        }
    }

    @Override
    public HashMap<String, Integer> getDiscoveryPointsSpentMap() {
        return this.discoveryPointsSpent;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == INSTANCE)
            return (T) this;
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return new ISkillData.Storage().writeNBT(INSTANCE, this, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        new ISkillData.Storage().readNBT(INSTANCE, this, null, nbt);
    }

    @Override
    public boolean canLearn(String name) {
        if (Skill.fromName(name) == null) return false;
        if (ArsMagica.disabledSkills.isSkillDisabled(name)) return false;
        Skill skill = Skill.fromName(name);
        if (getSkillLevel(name) >= skill.getMaxLevel()) return false;
        if (getSkillLevel(name) == 0) {
            for (String parentName : skill.getParents()) {
                Skill s = Skill.fromName(parentName);
                if (s == null) continue;
                if (this.hasSkill(parentName)) continue;
                return false;
            }
        }
        return this.getSkillPoint(skill.getPoint()) > 0;
    }

    private <T extends SpellPart> ArrayList<String> getKnownParts(Class<T> partType) {
        ArrayList<String> out = new ArrayList<>();
        for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
            SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
            if ((this.hasSkill(skill.getRegistryName().toString()) || this.player.capabilities.isCreativeMode) && part != null && partType.isInstance(part) && !ArsMagica.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
                out.add(skill.getID());
        }
        out.sort(Comparator.naturalOrder());
        return out;
    }

    @Override
    public ArrayList<String> getKnownShapes() {
        return getKnownParts(SpellShape.class);
    }

    @Override
    public ArrayList<String> getKnownComponents() {
        return getKnownParts(SpellComponent.class);
    }

    @Override
    public ArrayList<String> getKnownModifiers() {
        return getKnownParts(SpellModifier.class);
    }

    @Override
    public boolean shouldUpdate() {
        return this.syncCode != 0;
    }

    @Override
    public byte[] generateUpdatePacket() {
        AMDataWriter writer = new AMDataWriter();
        writer.add(this.syncCode);
        if ((this.syncCode & SYNC_SKILLS) == SYNC_SKILLS) {
            writer.add(this.skills.size());
            for (Entry<Skill, Integer> entry : this.skills.entrySet()) {
                writer.add(entry.getKey().getRegistryName().toString());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_SKILL_POINTS) == SYNC_SKILL_POINTS) {
            writer.add(this.skillPoints.size());
            for (Entry<SkillPoint, Integer> entry : this.skillPoints.entrySet()) {
                writer.add(entry.getKey().getName());
                writer.add(entry.getValue());
            }
        }
        if ((this.syncCode & SYNC_DISCIPLINES) == SYNC_DISCIPLINES) {
            writer.add(this.disciplineLevels.size());
            for (Entry<String, Integer> entry : this.disciplineLevels.entrySet()) {
                writer.add(entry.getKey());
                writer.add(entry.getValue());
            }
            writer.add(this.discoveryPointsSpent.size());
            for (Entry<String, Integer> entry : this.discoveryPointsSpent.entrySet()) {
                writer.add(entry.getKey());
                writer.add(entry.getValue());
            }
        }
        this.syncCode = 0;
        return writer.generate();
    }

    @Override
    public void handleUpdatePacket(byte[] bytes) {
        AMDataReader reader = new AMDataReader(bytes, false);
        int syncCode = reader.getInt();
        if ((syncCode & SYNC_SKILLS) == SYNC_SKILLS) {
            this.skills.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                Skill key = ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation(reader.getString()));
                int value = reader.getInt();
                if (key != null)
                    this.skills.put(key, value);
            }
        }
        if ((syncCode & SYNC_SKILL_POINTS) == SYNC_SKILL_POINTS) {
            this.skillPoints.clear();
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                SkillPoint key = SkillPointRegistry.fromName(reader.getString());
                int value = reader.getInt();
                if (key != null)
                    this.skillPoints.put(key, value);
            }
        }
        if ((syncCode & SYNC_DISCIPLINES) == SYNC_DISCIPLINES) {
            int size = reader.getInt();
            for (int i = 0; i < size; i++) {
                String key = reader.getString();
                int value = reader.getInt();
                this.disciplineLevels.put(key, value);
            }
            int spentSize = reader.getInt();
            for (int i = 0; i < spentSize; i++) {
                String key = reader.getString();
                int value = reader.getInt();
                this.discoveryPointsSpent.put(key, value);
            }
        }
    }

    @Override
    public void forceUpdate() {
        this.syncCode = 0xFFFFFFFF;
    }

}
