package am2.common.utils;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistry;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.AbstractSpellPart;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellShape;
import am2.common.armor.ArmorHelper;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.defs.ItemDefs;
import am2.common.defs.PotionEffectsDefs;
import am2.common.entity.EntityDarkMage;
import am2.common.entity.EntityLightMage;
import am2.common.extensions.EntityExtension;
import am2.common.spell.SpellCaster;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.List;

;

public class SpellUtils {
	
	public static final String TYPE_SHAPE = "Shape";
	public static final String TYPE_COMPONENT = "Component";
	public static final String TYPE_MODIFIER = "Modifier";
	public static final String TYPE = "Type";
	public static final String ID = "ID";
	public static final String SHAPE_GROUP = "ShapeGroup";
	public static final String STAGE = "Stage_";
	public static final String SPELL_DATA = "SpellData";
	
	public static float modifyDamage(EntityLivingBase caster, float damage){
		float factor = (float)(EntityExtension.For(caster).getCurrentLevel() < 20 ?
				0.5 + (0.5 * (EntityExtension.For(caster).getCurrentLevel() / 19)) :
				1.0 + (1.0 * (EntityExtension.For(caster).getCurrentLevel() - 20) / 79));
		return damage * factor;
	}
	
	public static boolean attackTargetSpecial(SpellData spellStack, Entity target, DamageSource damagesource, float magnitude){

		if (target.world.isRemote)
			return true;

		EntityPlayer dmgSrcPlayer = null;

		if (damagesource.getTrueSource() != null){
			if (damagesource.getTrueSource() instanceof EntityLivingBase){
				EntityLivingBase source = (EntityLivingBase)damagesource.getTrueSource();
				if ((source instanceof EntityLightMage || source instanceof EntityDarkMage) && target.getClass() == EntityCreeper.class){
					return false;
				}else if (source instanceof EntityLightMage && target instanceof EntityLightMage){
					return false;
				}else if (source instanceof EntityDarkMage && target instanceof EntityDarkMage){
					return false;
				}else  if (source instanceof EntityPlayer && target instanceof EntityPlayer && !target.world.isRemote && (!FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled() || ((EntityPlayer)target).capabilities.isCreativeMode)){
					return false;
				}

				if (source.isPotionActive(PotionEffectsDefs.FURY))
					magnitude += 4;
			}

			if (damagesource.getTrueSource() instanceof EntityPlayer){
				dmgSrcPlayer = (EntityPlayer)damagesource.getTrueSource();
				int armorSet = ArmorHelper.getFullArsMagicaArmorSet(dmgSrcPlayer);
				if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()){
					magnitude *= 1.05f;
				}else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()){
					magnitude *= 1.025f;
				}else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()){
					magnitude *= 1.1f;
				}

				ItemStack equipped = dmgSrcPlayer.getActiveItemStack();
				if (equipped != null && equipped.getItem() == ItemDefs.arcaneSpellbook){
					magnitude *= 1.1f;
				}
			}
		}

		if (target instanceof EntityLivingBase){
			if (EntityUtils.isSummon((EntityLivingBase)target) && damagesource.damageType.equals("magic")){
				magnitude *= 3.0f;
			}
		}

		magnitude *= ArsMagica2.config.getDamageMultiplier();

//		ItemStack oldItemStack = null;

		boolean success = false;
		if (target instanceof EntityDragon){
			success = ((EntityDragon)target).attackEntityFromPart(((EntityDragon)target).dragonPartBody, damagesource, magnitude);
		}else{
			success = target.attackEntityFrom(damagesource, magnitude);
		}

		if (dmgSrcPlayer != null){
			if (spellStack != null && target instanceof EntityLivingBase){
				if (!target.world.isRemote &&
						((EntityLivingBase)target).getHealth() <= 0 &&
						spellStack.isModifierPresent(SpellModifiers.DISMEMBERING_LEVEL)){
					double chance = spellStack.getModifiedValue(0, SpellModifiers.DISMEMBERING_LEVEL, Operation.ADD, dmgSrcPlayer.world, dmgSrcPlayer, target);
					if (dmgSrcPlayer.world.rand.nextDouble() <= chance){
						dropHead(target, dmgSrcPlayer.world);
					}
				}
			}
		}

		return success;
	}
	
	private static void dropHead(Entity target, World world){
		if (target instanceof EntityWither){
			dropHead_do(world, target.posX, target.posY, target.posZ, 1);
		}
		if (target.getClass() == EntitySkeleton.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 0);
		}else if (target.getClass() == EntityZombie.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 2);
		}else if (target.getClass() == EntityCreeper.class){
			dropHead_do(world, target.posX, target.posY, target.posZ, 4);
		}else if (target instanceof EntityPlayer){
			dropHead_do(world, target.posX, target.posY, target.posZ, 3);
		}
	}
	
	private static void dropHead_do(World world, double x, double y, double z, int type){
		EntityItem item = new EntityItem(world);
		ItemStack stack = new ItemStack(Items.SKULL, 1, type);
		item.setItem(stack);
		item.setPosition(x, y, z);
		world.spawnEntity(item);
	}
	
	public static NBTTagCompound encode(KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> toEncode) {
		NBTTagCompound group = new NBTTagCompound();
		group.setTag(SPELL_DATA, toEncode.value);
		int stage = 0;
		for (AbstractSpellPart part : toEncode.key) {
			NBTTagList stageTag = NBTUtils.addCompoundList(group, STAGE + stage);
			NBTTagCompound tmp = new NBTTagCompound();
			String id = SpellRegistry.getSkillFromPart(part).getID();
			tmp.setString(ID, id);
			String type = "";
			if (part instanceof SpellShape) type = TYPE_SHAPE;
			if (part instanceof SpellModifier) type = TYPE_MODIFIER;
			if (part instanceof SpellComponent) type = TYPE_COMPONENT;
			tmp.setString(TYPE, type);
			if (part instanceof SpellShape) {
				stage++;
			} else {
			}
			stageTag.appendTag(tmp);
		}
		group.setInteger("StageNum", stage);
		return group;
	}

	
	public static List<List<AbstractSpellPart>> transformParts(List<AbstractSpellPart> parts) {
		List<List<AbstractSpellPart>> stages = Lists.newArrayList();
		List<AbstractSpellPart> stage = Lists.newArrayList();
		for (AbstractSpellPart part : parts) {
			stage.add(part);
			if (part instanceof SpellShape) {
				if (stage != null && !stage.isEmpty()) {
					stages.add(stage);
				}
				stage = new ArrayList<>();
			}
		}
		stages.add(stage);
		return stages;
	}
	//BACKWARD COMPAT
	public static ArrayList<AbstractSpellPart> getPartsForSpell (ItemStack stack) {
		try {
			ArrayList<AbstractSpellPart> mods = new ArrayList<AbstractSpellPart>();
			for (int j = 0; j <= NBTUtils.getAM2Tag(stack.getTagCompound()).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(stack.getTagCompound()), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tag = stageTag.getCompoundTagAt(i);
					mods.add(ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tag.getString(ID))));
				}
			}
			return mods;
		} catch (Exception e) {
			return Lists.newArrayList();
		}
	}
	
	
	public static KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound> decode(NBTTagCompound toDecode) {
		if (toDecode == null)
			return null;
		try {
			ArrayList<AbstractSpellPart> parts = new ArrayList<>();
			for (int j = 0; j < NBTUtils.getAM2Tag(toDecode).getInteger("StageNum"); j++) { 
				NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(toDecode), STAGE + j);
				for (int i = 0; i < stageTag.tagCount(); i++) {
					NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
					String type = tmp.getString(TYPE);
					if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
						parts.add(SpellRegistry.getComponentFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_MODIFIER)) {
						parts.add(SpellRegistry.getModifierFromName(tmp.getString(ID)));
					}
					if (type.equalsIgnoreCase(TYPE_SHAPE)) {
						parts.add(SpellRegistry.getShapeFromName(tmp.getString(ID)));
					}
				}
			}
			return new KeyValuePair<ArrayList<AbstractSpellPart>, NBTTagCompound>(parts, toDecode.getCompoundTag(SPELL_DATA));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static void updateSpell(ItemStack in) {
		if (in != null && !in.hasTagCompound() || in.getTagCompound().getBoolean("Updated") || !in.hasCapability(SpellCaster.INSTANCE, null))
			return;
		ISpellCaster caster = in.getCapability(SpellCaster.INSTANCE, null);
		NBTTagCompound tag = in.getTagCompound();
		if (!tag.hasKey("AM2")) {
			tag.setBoolean("Updated", true);
			return;
		}
		NBTTagCompound am2 = NBTUtils.getAM2Tag(tag);
		NBTTagCompound commonData = am2.getCompoundTag(SPELL_DATA);
		NBTTagList shapeGroupList = am2.getTagList("ShapeGroups", Constants.NBT.TAG_COMPOUND);
		List<List<List<AbstractSpellPart>>> shapeGroups = Lists.newArrayList();
		for (int i = 0; i < shapeGroupList.tagCount(); i++) {
			NBTTagCompound group = shapeGroupList.getCompoundTagAt(i);
			NBTTagCompound groupData = group.getCompoundTag(SPELL_DATA);
			int stageCount = group.getInteger("StageNum");
			List<AbstractSpellPart> parts = Lists.newArrayList();
			for (int j = 0; j < stageCount; j++) {
				NBTTagList list = group.getTagList(STAGE + j, Constants.NBT.TAG_COMPOUND);
				for (int k = 0; k < list.tagCount(); k++) {
					NBTTagCompound nbt = list.getCompoundTagAt(k);
					AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(nbt.getString(ID)));
					if (part != null)
						parts.add(part);
				}
			}
			caster.setStoredData(i, groupData);
			shapeGroups.add(transformParts(parts));
		}
		int count = am2.getInteger("StageNum");
		List<AbstractSpellPart> parts = Lists.newArrayList();
		for (int i = 0; i < count; i++) {
			NBTTagList list = am2.getTagList(STAGE + i, Constants.NBT.TAG_COMPOUND);
			for (int j = 0; j < list.tagCount(); j++) {
				NBTTagCompound tmp = list.getCompoundTagAt(j);
				AbstractSpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tmp.getString(ID)));
				if (part != null)
					parts.add(part);				
			}
		}
		caster.setCommonStoredData(commonData);
		caster.setSpellCommon(transformParts(parts));
		caster.setShapeGroups(shapeGroups);
		tag.setBoolean("Updated", true);
	}

//	public static boolean casterHasAllReagents(EntityLivingBase caster, ItemStack spellStack){
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return true;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						boolean foundMatch = false;
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.getCount() >= stack.getCount()) {
//									foundMatch = true;
//									break;
//								}
//							}
//						}
//						if (!foundMatch) return false;
//					}
//				}
//			}
//		}
//		return true;
//	}
//	
//	public static String getMissingReagents(EntityLivingBase caster, ItemStack spellStack) {
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return "";
//			StringBuilder string = new StringBuilder(I18n.format("am2.tooltip.missingReagents"));
//			boolean first = true;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						boolean foundMatch = false;
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.getCount() >= stack.getCount()) {
//									foundMatch = true;
//									break;
//								}
//							}
//						}
//						if (!foundMatch)  {
//							if (!first) string.append(", ");
//							string.append(stack.getCount()).append("x ").append(stack.getDisplayName());
//							first = false;
//						}
//					}
//				}
//			}
//			return string.toString();
//		}
//		return "";		
//	}
//	
//	public static void consumeReagents(EntityLivingBase caster, ItemStack spellStack) {
//		if (caster instanceof EntityPlayer) {
//			EntityPlayer player = (EntityPlayer) caster;
//			if (player.capabilities.isCreativeMode) return;
//			for (SpellComponent part : getComponentsForStage(spellStack, -1)) {
//				if (part.reagents(caster) == null) continue;
//				for (ItemStack stack : part.reagents(caster)) {
//					if (stack != null) {
//						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
//							ItemStack is = player.inventory.getStackInSlot(i);
//							if (is == null) continue;
//							if (is.getItem() == stack.getItem() && (stack.getItemDamage() == OreDictionary.WILDCARD_VALUE || is.getItemDamage() == stack.getItemDamage())) {
//								if (is.getCount() >= stack.getCount()) {
//									is.shrink(stack.getCount();
//									if (is.getCount() <= 0) {
//										player.inventory.setInventorySlotContents(i, null);
//									} else {
//										player.inventory.setInventorySlotContents(i, is);
//									}
//									break;
//								}
//							}
//						}
//					}
//				}
//			}
//		}	
//	}
}
