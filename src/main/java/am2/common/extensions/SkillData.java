package am2.common.extensions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.SkillRegistry;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellShape;
import am2.common.lore.ArcaneCompendium;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class SkillData implements ISkillData, ICapabilityProvider, ICapabilitySerializable<NBTBase> {
	
	private EntityPlayer player;
	public static final ResourceLocation ID = new ResourceLocation("arsmagica2:SkillData");
	
	private static final int SYNC_SKILLS = 0x1;
	private static final int SYNC_SKILL_POINTS = 0x2;
	
	private int syncCode = 0;
	
	private HashMap<Skill, Boolean> skills;
	private HashMap<SkillPoint, Integer> skillPoints;
	
	@CapabilityInject(value=ISkillData.class)
	public static Capability<ISkillData> INSTANCE = null;
	
	public SkillData() {
		this.skills = new HashMap<>();
		this.skillPoints = new HashMap<>();
	}
	
	public static ISkillData For(EntityLivingBase living) {
		return living.getCapability(INSTANCE, null);
	}
	
	@Override
	public HashMap<Skill, Boolean> getSkills() {
		return this.skills;
	}
	
	@Override
	public boolean hasSkill (String name) {
		if (this.player.capabilities.isCreativeMode) return true;
		if (ArsMagica2.disabledSkills.isSkillDisabled(name)) return true;
		Boolean bool = this.skills.get(SkillRegistry.getSkillFromName(name));
		return bool == null ? false : bool;
	}
	
	@Override
	public void unlockSkill (String name) {
		if (SkillRegistry.getSkillFromName(name) == null)
			return;
		Skill skill = SkillRegistry.getSkillFromName(name);
		
		for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
			if (ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName()) != null) {
				AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
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

		this.setSkillPoint(skill.getPoint(), this.getSkillPoint(skill.getPoint()) - 1);
		this.skills.put(skill, true);
		this.syncCode |= SYNC_SKILLS;
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
			this.skills.put(aff, false);
		}
		for (SkillPoint aff : SkillPointRegistry.getSkillPointMap().values()) {
			this.skillPoints.put(aff, 0);
		}
		this.skillPoints.put(SkillPoint.SKILL_POINT_1, 3);
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
		if (SkillRegistry.getSkillFromName(name) == null) return false;
		if (ArsMagica2.disabledSkills.isSkillDisabled(name)) return false;
		for (String skill : SkillRegistry.getSkillFromName(name).getParents()) {
			Skill s = SkillRegistry.getSkillFromName(skill);
			if (s == null) continue;
			if (this.hasSkill(skill)) continue;
			return false;
		}
		return this.getSkillPoint(SkillRegistry.getSkillFromName(name).getPoint()) > 0;
	}

	@Override
	public ArrayList<String> getKnownShapes() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || this.player.capabilities.isCreativeMode) && part != null && part instanceof SpellShape && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(Comparator.naturalOrder());
		return out;
	}

	@Override
	public ArrayList<String> getKnownComponents() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || this.player.capabilities.isCreativeMode) && part != null && part instanceof SpellComponent && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(Comparator.naturalOrder());
		return out;
	}

	@Override
	public ArrayList<String> getKnownModifiers() {
		ArrayList<String> out = new ArrayList<>();
		for (Skill skill : ArsMagicaAPI.getSkillRegistry()) {
			AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
			if ((this.hasSkill(skill.getRegistryName().toString()) || this.player.capabilities.isCreativeMode) && part != null && part instanceof SpellModifier && !ArsMagica2.disabledSkills.isSkillDisabled(part.getRegistryName().toString()))
				out.add(skill.getID());
		}
		out.sort(Comparator.naturalOrder());
		return out;
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
			for (Entry<Skill, Boolean> entry : this.skills.entrySet()) {
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
				boolean value = reader.getBoolean();
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
	}

	@Override
	public void forceUpdate() {
		this.syncCode = 0xFFFFFFFF;
	}

}
