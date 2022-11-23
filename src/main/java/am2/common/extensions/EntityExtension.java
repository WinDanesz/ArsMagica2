package am2.common.extensions;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.extensions.IEntityExtension;
import am2.api.math.AMVector2;
import am2.api.spell.SpellData;
import am2.client.particles.AMLineArc;
import am2.common.armor.ArmorHelper;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.armor.infusions.GenericImbuement;
import am2.common.armor.infusions.ImbuementRegistry;
import am2.common.bosses.EntityLifeGuardian;
import am2.common.defs.ItemDefs;
import am2.common.defs.PotionEffectsDefs;
import am2.common.defs.SkillDefs;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMPacketIDs;
import am2.common.spell.ContingencyType;
import am2.common.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.ArrayList;
import java.util.Iterator;

public class EntityExtension implements IEntityExtension, ICapabilityProvider, ICapabilitySerializable<NBTBase> {

	public static final ResourceLocation ID = new ResourceLocation("arsmagica2:ExtendedProp");

	private static final int SYNC_CONTINGENCY = 0x1;
	private static final int SYNC_MARK = 0x2;
	private static final int SYNC_MANA = 0x4;
	private static final int SYNC_FATIGUE = 0x8;
	private static final int SYNC_LEVEL = 0x10;
	private static final int SYNC_XP = 0x20;
	private static final int SYNC_SUMMONS = 0x40;
	private static final int SYNC_FALL_PROTECTION = 0x80;
	private static final int SYNC_FLIP_ROTATION = 0x100;
	private static final int SYNC_INVERSION_STATE = 0x200;
	private static final int SYNC_SHRINK_STATE = 0x400;
	private static final int SYNC_TK_DISTANCE = 0x800;
	private static final int SYNC_MANA_SHIELD = 0x1000;
	private static final int SYNC_SHRINK_PERCENTAGE = 0x2000;
	private static final int SYNC_HEAL_COOLDOWN = 0x4000;
	private static final int SYNC_AFFINITY_HEAL_COOLDOWN = 0x8000;
	private static final int SYNC_DISABLE_GRAVITY = 0x10000;

	private static int baseTicksForFullRegen = 2400;
	private int ticksForFullRegen = baseTicksForFullRegen;
	public boolean isRecoveringKeystone;

	private Entity ent;

	@CapabilityInject(value = IEntityExtension.class)
	public static Capability<IEntityExtension> INSTANCE = null;

	private ArrayList<Integer> summon_ent_ids = new ArrayList<>();
	private EntityLivingBase entity;

	private ArrayList<ManaLinkEntry> manaLinks = new ArrayList<>();
	public AMVector2 originalSize;
	public float shrinkAmount;
	public boolean astralBarrierBlocked = false;
	public float bankedInfusionHelm = 0f;
	public float bankedInfusionChest = 0f;
	public float bankedInfusionLegs = 0f;
	public float bankedInfusionBoots = 0f;

	private int syncCode = 0;

	private ContingencyType contingencyType = ContingencyType.NULL;
	private SpellData contingencyStack = null;
	private double markX;
	private double markY;
	private double markZ;
	private int markDimension = -512;

	private float currentMana;
	private float currentFatigue;
	private float currentXP;
	private int currentLevel;
	private int currentSummons;
	private int healCooldown;
	private int affHealCooldown;
	private boolean isShrunk;
	private boolean isInverted;
	private float fallProtection;
	private float flipRotation;
	private float prevFlipRotation;
	private float shrinkPercentage;
	private float prevShrinkPercentage;
	private float TKDistance;
	private boolean disableGravity;
	private float manaShield;

	public ArrayList<SpellData> runningStacks = new ArrayList<>();


	private void addSyncCode(int code) {
		this.syncCode |= code;
	}

	@Override
	public boolean hasEnoughMana(float cost) {
		return this.entity instanceof EntityPlayer && ((EntityPlayer) this.entity).capabilities.isCreativeMode || !(this.getCurrentMana() + this.getBonusCurrentMana() < cost);
	}

	@Override
	public void setContingency(ContingencyType type, SpellData stack) {
		if (this.contingencyType != type || this.contingencyStack != stack) {
			this.addSyncCode(SYNC_CONTINGENCY);
			this.contingencyType = type;
			this.contingencyStack = stack;
		}
	}

	@Override
	public ContingencyType getContingencyType() {
		return this.contingencyType;
	}

	@Override
	public SpellData getContingencyStack() {
		return this.contingencyStack;
	}

	@Override
	public double getMarkX() {
		return this.markX;
	}

	@Override
	public double getMarkY() {
		return this.markY;
	}

	@Override
	public double getMarkZ() {
		return this.markZ;
	}

	@Override
	public int getMarkDimensionID() {
		return this.markDimension;
	}

	@Override
	public float getCurrentMana() {
		return this.currentMana;
	}

	@Override
	public int getCurrentLevel() {
		return this.currentLevel;
	}

	@Override
	public float getCurrentBurnout() {
		return this.currentFatigue;
	}

	@Override
	public int getCurrentSummons() {
		return this.currentSummons;
	}

	@Override
	public float getCurrentXP() {
		return this.currentXP;
	}

	@Override
	public int getHealCooldown() {
		return this.healCooldown;
	}

	@Override
	public void lowerHealCooldown(int amount) {
		this.setHealCooldown(Math.max(0, this.getHealCooldown() - amount));
	}

	@Override
	public void placeHealOnCooldown() {
		this.setHealCooldown(40);
	}

	@Override
	public void lowerAffinityHealCooldown(int amount) {
		this.setAffinityHealCooldown(Math.max(0, this.getAffinityHealCooldown() - amount));
	}

	@Override
	public int getAffinityHealCooldown() {
		return this.affHealCooldown;
	}

	@Override
	public void placeAffinityHealOnCooldown(boolean full) {
		this.setAffinityHealCooldown(full ? 40 : 20);
	}

	@Override
	public float getMaxMana() {
		float mana = (float) (Math.pow(this.getCurrentLevel(), 1.5f) * (85f * ((float) this.getCurrentLevel() / 100f)) + 500f);
		if (this.entity.isPotionActive(PotionEffectsDefs.MANA_BOOST))
			mana *= 1 + (0.25 * (this.entity.getActivePotionEffect(PotionEffectsDefs.MANA_BOOST).getAmplifier() + 1));
		return (float) (mana + this.entity.getAttributeMap().getAttributeInstance(ArsMagicaAPI.maxManaBonus).getAttributeValue());
	}

	@Override
	public float getMaxXP() {
		return (float) (ArsMagica2.config.getOldXpCalculations() ? Math.pow(0.25 * this.getCurrentLevel(), 1.5) : 0.2 + Math.log(1 + (this.getCurrentLevel() * 0.2)));
	}

	@Override
	public float getMaxBurnout() {
		return this.getCurrentLevel() * 10 + 1;
	}

	@Override
	public void setAffinityHealCooldown(int affinityHealCooldown) {
		if (affinityHealCooldown != this.affHealCooldown) {
			this.addSyncCode(SYNC_AFFINITY_HEAL_COOLDOWN);
			this.affHealCooldown = affinityHealCooldown;
		}
	}

	@Override
	public void setCurrentBurnout(float currentBurnout) {
		if (this.currentFatigue != currentBurnout) {
			this.addSyncCode(SYNC_FATIGUE);
			this.currentFatigue = currentBurnout;
		}
	}

	@Override
	public void setCurrentLevel(int currentLevel) {
		if (currentLevel != this.currentLevel) {
			this.addSyncCode(SYNC_LEVEL);
			this.ticksForFullRegen = (int) Math.round(baseTicksForFullRegen * (0.75 - (0.25 * (this.getCurrentLevel() / 99f))));
			if (this.entity instanceof EntityPlayer) {
				MinecraftForge.EVENT_BUS.post(new PlayerMagicLevelChangeEvent((EntityPlayer) this.entity, currentLevel));
				if (this.currentLevel < currentLevel)
					this.entity.world.playSound(null, this.entity.posX, this.entity.posY, this.entity.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, this.entity.getSoundCategory(), 0.75F, 1.0F);
			}
			this.currentLevel = currentLevel;
		}
	}

	@Override
	public void setCurrentMana(float currentMana) {
		if (this.currentMana != currentMana) {
			this.addSyncCode(SYNC_MANA);
			this.currentMana = currentMana;
		}
	}

	@Override
	public void setCurrentSummons(int currentSummons) {
		if (this.currentSummons != currentSummons) {
			this.addSyncCode(SYNC_SUMMONS);
			this.currentSummons = currentSummons;
		}
	}

	@Override
	public void setCurrentXP(float currentXP) {
		if (this.currentXP != currentXP) {
			while (currentXP >= this.getMaxXP()) {
				currentXP -= this.getMaxXP();
				this.setMagicLevelWithMana(this.getCurrentLevel() + 1);
			}
			this.addSyncCode(SYNC_XP);
			this.currentXP = currentXP;
		}
	}

	@Override
	public void setHealCooldown(int healCooldown) {
		if (this.healCooldown != healCooldown) {
			this.addSyncCode(SYNC_HEAL_COOLDOWN);
			this.healCooldown = healCooldown;
		}
	}

	@Override
	public void setMarkX(double markX) {
		if (this.markX != markX) {
			this.addSyncCode(SYNC_MARK);
			this.markX = markX;
		}
	}

	@Override
	public void setMarkY(double markY) {
		if (this.markY != markY) {
			this.addSyncCode(SYNC_MARK);
			this.markY = markY;
		}
	}

	@Override
	public void setMarkZ(double markZ) {
		if (this.markZ != markZ) {
			this.addSyncCode(SYNC_MARK);
			this.markZ = markZ;
		}
	}

	@Override
	public void setMarkDimensionID(int markDimensionID) {
		if (this.markDimension != markDimensionID) {
			this.addSyncCode(SYNC_MARK);
			this.markDimension = markDimensionID;
		}
	}

	@Override
	public void setMark(double x, double y, double z, int dim) {
		this.setMarkX(x);
		this.setMarkY(y);
		this.setMarkZ(z);
		this.setMarkDimensionID(dim);
	}

	@Override
	public boolean isShrunk() {
		return this.isShrunk;
	}

	@Override
	public void setShrunk(boolean shrunk) {
		if (this.isShrunk != shrunk) {
			this.addSyncCode(SYNC_SHRINK_STATE);
			this.isShrunk = shrunk;
		}
	}

	@Override
	public void setInverted(boolean isInverted) {
		if (this.isInverted != isInverted) {
			this.addSyncCode(SYNC_INVERSION_STATE);
			this.isInverted = isInverted;
		}
	}

	@Override
	public void setFallProtection(float fallProtection) {
		if (this.fallProtection != fallProtection) {
			this.addSyncCode(SYNC_FALL_PROTECTION);
			this.fallProtection = fallProtection;
		}
	}

	@Override
	public boolean isInverted() {
		return this.isInverted;
	}

	@Override
	public float getFallProtection() {
		return this.fallProtection;
	}

	@Override
	public void addEntityReference(EntityLivingBase entity) {
		this.entity = entity;
		this.setOriginalSize(new AMVector2(entity.width, entity.height));
	}

	public void setOriginalSize(AMVector2 amVector2) {
		this.originalSize = amVector2;
	}

	public AMVector2 getOriginalSize() {
		return this.originalSize;
	}

	@Override
	public void init(EntityLivingBase entity) {
		this.addEntityReference(entity);
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

	public static EntityExtension For(EntityLivingBase thePlayer) {
		return (EntityExtension) thePlayer.getCapability(INSTANCE, null);
	}

	@Override
	public NBTBase serializeNBT() {
		return new Storage().writeNBT(INSTANCE, this, null);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		new Storage().readNBT(INSTANCE, this, null, nbt);
	}

	@Override
	public boolean canHeal() {
		return this.getHealCooldown() <= 0;
	}

	@Override
	public int getMaxSummons() {
		return this.entity instanceof EntityPlayer && SkillData.For(this.entity).hasSkill(SkillDefs.EXTRA_SUMMONS.getID()) ? 2 : 3;
	}

	@Override
	public boolean addSummon(EntityCreature entityliving) {
		if (!this.entity.world.isRemote) {
			this.summon_ent_ids.add(entityliving.getEntityId());
			this.setCurrentSummons(this.getCurrentSummons() + 1);
		}
		return true;
	}

	@Override
	public boolean getCanHaveMoreSummons() {
		if (this.entity instanceof EntityLifeGuardian)
			return true;

		this.verifySummons();
		return this.getCurrentSummons() < this.getMaxSummons();
	}

	private void verifySummons() {
		this.setCurrentSummons(this.summon_ent_ids.size());
		for (int i = 0; i < this.summon_ent_ids.size(); ++i) {
			int id = this.summon_ent_ids.get(i);
			Entity e = this.entity.world.getEntityByID(id);
			if (e == null || !(e instanceof EntityLivingBase) || EntityUtils.getOwner((EntityLivingBase) e) != this.entity.getEntityId()) {
				this.summon_ent_ids.remove(i);
				i--;
				this.removeSummon();
			}
		}
	}

	@Override
	public boolean removeSummon() {
		if (this.getCurrentSummons() == 0) {
			return false;
		}
		if (!this.entity.world.isRemote) {
			this.setCurrentSummons(this.getCurrentSummons() - 1);
		}
		return true;
	}

	@Override
	public void updateManaLink(EntityLivingBase entity) {
		ManaLinkEntry mle = new ManaLinkEntry(entity.getEntityId(), 20);
		if (!this.manaLinks.contains(mle))
			this.manaLinks.add(mle);
		else
			this.manaLinks.remove(mle);
		if (!this.entity.world.isRemote)
			AMNetHandler.INSTANCE.sendPacketToAllClientsNear(entity.dimension, entity.posX, entity.posY, entity.posZ, 32, AMPacketIDs.MANA_LINK_UPDATE, this.getManaLinkUpdate());

	}

	@Override
	public void deductMana(float manaCost) {
		if (this.entity instanceof EntityPlayer && ((EntityPlayer) this.entity).capabilities.isCreativeMode)
			return;
		float leftOver = manaCost - this.getCurrentMana();
		this.setCurrentMana(this.getCurrentMana() - manaCost);
		if (leftOver > 0) {
			for (ManaLinkEntry entry : this.manaLinks) {
				leftOver -= entry.deductMana(this.entity.world, this.entity, leftOver);
				if (leftOver <= 0)
					break;
			}
		}
	}

	@Override
	public void cleanupManaLinks() {
		Iterator<ManaLinkEntry> it = this.manaLinks.iterator();
		while (it.hasNext()) {
			ManaLinkEntry entry = it.next();
			Entity e = this.entity.world.getEntityByID(entry.entityID);
			if (e == null)
				it.remove();
		}
	}

	@Override
	public float getBonusCurrentMana() {
		float bonus = 0;
		for (ManaLinkEntry entry : this.manaLinks) {
			bonus += entry.getAdditionalCurrentMana(this.entity.world, this.entity);
		}
		return bonus;
	}

	@Override
	public float getBonusMaxMana() {
		float bonus = 0;
		for (ManaLinkEntry entry : this.manaLinks) {
			bonus += entry.getAdditionalMaxMana(this.entity.world, this.entity);
		}
		return bonus;
	}

	@Override
	public boolean isManaLinkedTo(EntityLivingBase entity) {
		for (ManaLinkEntry entry : this.manaLinks) {
			if (entry.entityID == entity.getEntityId())
				return true;
		}
		return false;
	}

	@Override
	public void spawnManaLinkParticles() {
		if (this.entity.world != null && this.entity.world.isRemote) {
			for (ManaLinkEntry entry : this.manaLinks) {
				Entity e = this.entity.world.getEntityByID(entry.entityID);
				if (e != null && e.getDistanceSq(this.entity) < entry.range && e.ticksExisted % 90 == 0) {
					AMLineArc arc = (AMLineArc) ArsMagica2.proxy.particleManager.spawn(this.entity.world, "textures/blocks/oreblockbluetopaz.png", e, this.entity);
					if (arc != null) {
						arc.setIgnoreAge(false);
						arc.setRBGColorF(0.17f, 0.88f, 0.88f);
					}
				}
			}
		}
	}

	private class ManaLinkEntry {
		private final int entityID;
		private final int range;

		private ManaLinkEntry(int entityID, int range) {
			this.entityID = entityID;
			this.range = range * range;
		}

		private EntityLivingBase getEntity(World world) {
			Entity e = world.getEntityByID(this.entityID);
			if (e == null || !(e instanceof EntityLivingBase))
				return null;
			return (EntityLivingBase) e;
		}

		private float getAdditionalCurrentMana(World world, Entity host) {
			EntityLivingBase e = this.getEntity(world);
			if (e == null || e.getDistanceSq(host) > this.range)
				return 0;
			return For(e).getCurrentMana();
		}

		private float getAdditionalMaxMana(World world, Entity host) {
			EntityLivingBase e = this.getEntity(world);
			if (e == null || e.getDistanceSq(host) > this.range)
				return 0;
			return For(e).getMaxMana();
		}

		public float deductMana(World world, Entity host, float amt) {
			EntityLivingBase e = this.getEntity(world);
			if (e == null || e.getDistanceSq(host) > this.range)
				return 0;
			amt = Math.min(For(e).getCurrentMana(), amt);
			For(e).deductMana(amt);
			return amt;
		}

		@Override
		public int hashCode() {
			return this.entityID;
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof ManaLinkEntry && ((ManaLinkEntry) obj).entityID == this.entityID;
		}
	}

	@Override
	public boolean shouldReverseInput() {
		return this.getFlipRotation() > 0 || this.entity.isPotionActive(PotionEffectsDefs.SCRAMBLE_SYNAPSES);
	}

	@Override
	public boolean getIsFlipped() {
		return this.isInverted();
	}

	@Override
	public float getFlipRotation() {
		return this.flipRotation;
	}

	@Override
	public float getPrevFlipRotation() {
		return this.prevFlipRotation;
	}

	@Override
	public void setFlipRotation(float rot) {
		if (this.flipRotation != rot) {
			this.addSyncCode(SYNC_FLIP_ROTATION);
			this.flipRotation = rot;
		}
	}

	@Override
	public void setPrevFlipRotation(float rot) {
		if (this.prevFlipRotation != rot) {
			this.addSyncCode(SYNC_FLIP_ROTATION);
			this.prevFlipRotation = rot;
		}
	}

	@Override
	public float getShrinkPct() {
		return this.shrinkPercentage;
	}

	@Override
	public float getPrevShrinkPct() {
		return this.prevShrinkPercentage;
	}

	@Override
	public void setTKDistance(float TK_Distance) {
		if (this.TKDistance != TK_Distance) {
			this.addSyncCode(SYNC_TK_DISTANCE);
			this.TKDistance = TK_Distance;
		}
	}

	@Override
	public void addToTKDistance(float toAdd) {
		this.setTKDistance(this.getTKDistance() + toAdd);
	}

	@Override
	public float getTKDistance() {
		return this.TKDistance;
	}

	@Override
	public void syncTKDistance() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.getTKDistance());
		AMNetHandler.INSTANCE.sendPacketToServer(AMPacketIDs.TK_DISTANCE_SYNC, writer.generate());
	}

	@Override
	public void manaBurnoutTick() {
		if (this.isGravityDisabled()) {
			this.entity.motionY = 0;
		}
		float actualMaxMana = this.getMaxMana();
		if (this.getCurrentMana() < actualMaxMana) {
			if (this.entity instanceof EntityPlayer && ((EntityPlayer) this.entity).capabilities.isCreativeMode) {
				this.setCurrentMana(actualMaxMana);
			} else {
				if (this.getCurrentMana() < 0) {
					this.setCurrentMana(0);
				}

				int regenTicks = (int) Math.ceil(this.ticksForFullRegen * this.entity.getAttributeMap()
						.getAttributeInstance(ArsMagicaAPI.manaRegenTimeModifier).getAttributeValue());

				if (this.entity.isPotionActive(PotionEffectsDefs.MANA_REGEN)) {
					PotionEffect pe = this.entity.getActivePotionEffect(PotionEffectsDefs.MANA_REGEN);
					regenTicks *= Math.max(0.01, 1.0f - ((pe.getAmplifier() + 1) * 0.25f));
				}

				if (this.entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) this.entity;
					int armorSet = ArmorHelper.getFullArsMagicaArmorSet(player);
					if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()) {
						regenTicks *= 0.8;
					} else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()) {
						regenTicks *= 0.95;
					} else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()) {
						regenTicks *= 0.5;
					}

					if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_3.getID())) {
						regenTicks *= 0.7f;
					} else if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_2.getID())) {
						regenTicks *= 0.85f;
					} else if (SkillData.For(player).hasSkill(SkillDefs.MANA_REGEN_1.getID())) {
						regenTicks *= 0.95f;
					}

					int numArmorPieces = 0;
					for (int i = 0; i < 4; ++i) {
						ItemStack stack = player.inventory.armorInventory.get(i);
						if (ImbuementRegistry.instance.isImbuementPresent(stack, GenericImbuement.manaRegen))
							numArmorPieces++;
					}
					regenTicks *= 1.0f - (0.15f * numArmorPieces);
				}

				float manaToAdd = (actualMaxMana / regenTicks);

				this.setCurrentMana(this.getCurrentMana() + manaToAdd);
				if (this.getCurrentMana() > this.getMaxMana())
					this.setCurrentMana(this.getMaxMana());
			}
		} else if (this.getCurrentMana() > this.getMaxMana()) {
			float overloadMana = this.getCurrentMana() - this.getMaxMana();
			float toRemove = Math.max(overloadMana * 0.002f, 1.0f);
			this.deductMana(toRemove);
			if (this.entity instanceof EntityPlayer && SkillData.For(this.entity).hasSkill(SkillDefs.SHIELD_OVERLOAD.getID())) {
				this.addMagicShieldingCapped(toRemove / 500F);
			}
		}
		if (this.getManaShielding() > this.getMaxMagicShielding()) {
			float overload = this.getManaShielding() - (this.getMaxMagicShielding());
			float toRemove = Math.max(overload * 0.002f, 1.0f);
			if (this.getManaShielding() - toRemove < this.getMaxMagicShielding())
				toRemove = overload;
			this.setManaShielding(this.getManaShielding() - toRemove);
		}

		if (this.getCurrentBurnout() > 0) {
			int numArmorPieces = 0;
			if (this.entity instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) this.entity;
				for (int i = 0; i < 4; ++i) {
					ItemStack stack = player.inventory.armorInventory.get(i);
					if (stack == null) continue;
					if (ImbuementRegistry.instance.isImbuementPresent(stack, GenericImbuement.burnoutReduction))
						numArmorPieces++;
				}
			}
			float factor = (float) ((0.01f + (0.015f * numArmorPieces)) * this.entity.getAttributeMap()
					.getAttributeInstance(ArsMagicaAPI.burnoutReductionRate).getAttributeValue());
			float decreaseAmt = factor * this.getCurrentLevel();
			this.setCurrentBurnout(this.getCurrentBurnout() - decreaseAmt);
			if (this.getCurrentBurnout() < 0) {
				this.setCurrentBurnout(0);
			}
		}
	}

	private byte[] getManaLinkUpdate() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.entity.getEntityId());
		writer.add(this.manaLinks.size());
		for (ManaLinkEntry entry : this.manaLinks)
			writer.add(entry.entityID);
		return writer.generate();
	}

	public void handleManaLinkUpdate(AMDataReader rdr) {
		this.manaLinks.clear();
		int numLinks = rdr.getInt();
		for (int i = 0; i < numLinks; ++i) {
			Entity e = this.entity.world.getEntityByID(rdr.getInt());
			if (e != null && e instanceof EntityLivingBase)
				this.updateManaLink((EntityLivingBase) e);
		}
	}

	@Override
	public boolean setMagicLevelWithMana(int level) {
		if (level < 0) level = 0;
		this.setCurrentLevel(level);
		this.setCurrentMana(this.getMaxMana());
		this.setCurrentBurnout(0);
		return true;
	}

	@Override
	public void addMagicXP(float xp) {
		this.setCurrentXP(this.getCurrentXP() + xp);
	}

	@Override
	public void setDisableGravity(boolean b) {
		if (this.disableGravity != b) {
			this.addSyncCode(SYNC_DISABLE_GRAVITY);
			this.disableGravity = b;
		}
	}

	@Override
	public boolean isGravityDisabled() {
		return this.disableGravity;
	}

	@Override
	public Entity getInanimateTarget() {
		return this.ent;
	}

	@Override
	public void setInanimateTarget(Entity ent) {
		this.ent = ent;
	}

	public void flipTick() {
		//this.setInverted(true);
		boolean flipped = this.getIsFlipped();

		ItemStack boots = ((EntityPlayer) this.entity).inventory.armorInventory.get(0);
		if (boots == null || boots.getItem() != ItemDefs.enderBoots)
			this.setInverted(false);

		this.setPrevFlipRotation(this.getFlipRotation());
		if (flipped && this.getFlipRotation() < 180)
			this.setFlipRotation(this.getFlipRotation() + 15);
		else if (!flipped && this.getFlipRotation() > 0)
			this.setFlipRotation(this.getFlipRotation() - 15);
	}

	public void setShrinkPct(float shrinkPct) {
		if (this.prevShrinkPercentage != shrinkPct || this.prevShrinkPercentage != this.shrinkPercentage || this.shrinkPercentage != shrinkPct) {
			this.prevShrinkPercentage = this.shrinkPercentage;
			this.shrinkPercentage = shrinkPct;
			this.addSyncCode(SYNC_SHRINK_PERCENTAGE);
		}
	}

	@Override
	public float getManaShielding() {
		return this.manaShield;
	}

	@Override
	public void setManaShielding(float manaShield) {
		manaShield = Math.max(0, manaShield);
		if (manaShield != this.manaShield) {
			this.manaShield = manaShield;
			this.addSyncCode(SYNC_MANA_SHIELD);
		}
	}

	private float getMaxMagicShielding() {
		return this.getCurrentLevel() * 2;
	}

	public float protect(float damage) {
		float left = this.getManaShielding() - damage;
		this.setManaShielding(Math.max(0, left));
		if (left < 0)
			return -left;
		return 0;
	}

	public void addMagicShielding(float manaShield) {
		this.setManaShielding(this.getManaShielding() + manaShield);
	}

	private void addMagicShieldingCapped(float manaShield) {
		this.setManaShielding(Math.min(this.getManaShielding() + manaShield, this.getMaxMagicShielding()));
	}

	@Override
	public boolean shouldUpdate() {
		return this.syncCode != 0;
	}

	@Override
	public byte[] generateUpdatePacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(this.syncCode);
		if ((this.syncCode & SYNC_CONTINGENCY) == SYNC_CONTINGENCY) {
			writer.add(this.contingencyType.name().toLowerCase());
			boolean present = this.contingencyStack != null;
			writer.add(present);
			if (present)
				writer.add(this.contingencyStack.writeToNBT(new NBTTagCompound()));
		}
		if ((this.syncCode & SYNC_MARK) == SYNC_MARK)
			writer.add(this.markX).add(this.markY).add(this.markZ).add(this.markDimension);
		if ((this.syncCode & SYNC_MANA) == SYNC_MANA) writer.add(this.currentMana);
		if ((this.syncCode & SYNC_FATIGUE) == SYNC_FATIGUE) writer.add(this.currentFatigue);
		if ((this.syncCode & SYNC_LEVEL) == SYNC_LEVEL) writer.add(this.currentLevel);
		if ((this.syncCode & SYNC_XP) == SYNC_XP) writer.add(this.currentXP);
		if ((this.syncCode & SYNC_SUMMONS) == SYNC_SUMMONS) writer.add(this.currentSummons);
		if ((this.syncCode & SYNC_FALL_PROTECTION) == SYNC_FALL_PROTECTION) writer.add(this.fallProtection);
		if ((this.syncCode & SYNC_FLIP_ROTATION) == SYNC_FLIP_ROTATION)
			writer.add(this.flipRotation).add(this.prevFlipRotation);
		if ((this.syncCode & SYNC_INVERSION_STATE) == SYNC_INVERSION_STATE) writer.add(this.isInverted);
		if ((this.syncCode & SYNC_SHRINK_STATE) == SYNC_SHRINK_STATE) writer.add(this.isShrunk);
		if ((this.syncCode & SYNC_TK_DISTANCE) == SYNC_TK_DISTANCE) writer.add(this.TKDistance);
		if ((this.syncCode & SYNC_MANA_SHIELD) == SYNC_MANA_SHIELD) writer.add(this.manaShield);
		if ((this.syncCode & SYNC_SHRINK_PERCENTAGE) == SYNC_SHRINK_PERCENTAGE)
			writer.add(this.shrinkPercentage).add(this.prevShrinkPercentage);
		if ((this.syncCode & SYNC_HEAL_COOLDOWN) == SYNC_HEAL_COOLDOWN) writer.add(this.healCooldown);
		if ((this.syncCode & SYNC_AFFINITY_HEAL_COOLDOWN) == SYNC_AFFINITY_HEAL_COOLDOWN)
			writer.add(this.affHealCooldown);
		if ((this.syncCode & SYNC_DISABLE_GRAVITY) == SYNC_DISABLE_GRAVITY) writer.add(this.disableGravity);
		this.syncCode = 0;
		return writer.generate();
	}

	@Override
	public void handleUpdatePacket(byte[] bytes) {
		AMDataReader reader = new AMDataReader(bytes, false);
		int syncCode = reader.getInt();
		if ((syncCode & SYNC_CONTINGENCY) == SYNC_CONTINGENCY) {
			String name = reader.getString();
			this.contingencyType = ContingencyType.fromName(name);
			if (reader.getBoolean())
				this.contingencyStack = SpellData.readFromNBT(reader.getNBTTagCompound());
			else
				this.contingencyStack = null;
		}
		if ((syncCode & SYNC_MARK) == SYNC_MARK) {
			this.markX = reader.getDouble();
			this.markY = reader.getDouble();
			this.markZ = reader.getDouble();
			this.markDimension = reader.getInt();
		}
		if ((syncCode & SYNC_MANA) == SYNC_MANA) this.currentMana = reader.getFloat();
		if ((syncCode & SYNC_FATIGUE) == SYNC_FATIGUE) this.currentFatigue = reader.getFloat();
		if ((syncCode & SYNC_LEVEL) == SYNC_LEVEL) this.currentLevel = reader.getInt();
		if ((syncCode & SYNC_XP) == SYNC_XP) this.currentXP = reader.getFloat();
		if ((syncCode & SYNC_SUMMONS) == SYNC_SUMMONS) this.currentSummons = reader.getInt();
		if ((syncCode & SYNC_FALL_PROTECTION) == SYNC_FALL_PROTECTION) this.fallProtection = reader.getFloat();
		if ((syncCode & SYNC_FLIP_ROTATION) == SYNC_FLIP_ROTATION) {
			this.flipRotation = reader.getFloat();
			this.prevFlipRotation = reader.getFloat();
		}
		if ((syncCode & SYNC_INVERSION_STATE) == SYNC_INVERSION_STATE) this.isInverted = reader.getBoolean();
		if ((syncCode & SYNC_SHRINK_STATE) == SYNC_SHRINK_STATE) this.isShrunk = reader.getBoolean();
		if ((syncCode & SYNC_TK_DISTANCE) == SYNC_TK_DISTANCE) this.TKDistance = reader.getFloat();
		if ((syncCode & SYNC_MANA_SHIELD) == SYNC_MANA_SHIELD) this.manaShield = reader.getFloat();
		if ((syncCode & SYNC_SHRINK_PERCENTAGE) == SYNC_SHRINK_PERCENTAGE) {
			this.shrinkPercentage = reader.getFloat();
			this.prevShrinkPercentage = reader.getFloat();
		}
		if ((syncCode & SYNC_HEAL_COOLDOWN) == SYNC_HEAL_COOLDOWN) this.healCooldown = reader.getInt();
		if ((syncCode & SYNC_AFFINITY_HEAL_COOLDOWN) == SYNC_AFFINITY_HEAL_COOLDOWN)
			this.affHealCooldown = reader.getInt();
		if ((syncCode & SYNC_DISABLE_GRAVITY) == SYNC_DISABLE_GRAVITY) this.disableGravity = reader.getBoolean();
	}

	@Override
	public void forceUpdate() {
		this.syncCode = 0xFFFFFFFF;
	}
}
