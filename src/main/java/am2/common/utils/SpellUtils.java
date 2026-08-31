package am2.common.utils;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistryHelper;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.spell.*;
import am2.common.armor.ArmorHelper;
import am2.common.armor.ArsMagicaArmorMaterial;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.entity.EntityDarkMage;
import am2.common.entity.EntityLightMage;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
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

public class SpellUtils {

    public static final String TYPE_SHAPE = "Shape";
    public static final String TYPE_COMPONENT = "Component";
    public static final String TYPE_MODIFIER = "Modifier";
    public static final String TYPE = "Type";
    public static final String ID = "ID";
    public static final String SHAPE_GROUP = "ShapeGroup";
    public static final String STAGE = "Stage_";
    public static final String SPELL_DATA = "SpellData";

    public static int modifyDurationBasedOnArmor(EntityLivingBase caster, int baseDuration) {
        if (!(caster instanceof EntityPlayer)) return baseDuration;
        int armorSet = ArmorHelper.getFullArsMagicaArmorSet((EntityPlayer) caster);
        if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()) {
            baseDuration *= ArsMagica.config.getMageSetDurationMultiplier();
        } else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()) {
            baseDuration *= ArsMagica.config.getBattlemageSetDurationMultiplier();
        } else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()) {
            baseDuration *= ArsMagica.config.getArchmageSetDurationMultiplier();
        }
        return baseDuration;
    }

    public static float modifyDamage(EntityLivingBase caster, float damage) {
        if (caster == null) return damage; // NPCs/projectiles without valid caster reference
        IEntityExtension ext = EntityExtension.For(caster);
        float factor = (float) (ext.getCurrentLevel() < 20 ?
                0.5 + (0.5 * (ext.getCurrentLevel() / 19)) :
                1.0 + (1.0 * (ext.getCurrentLevel() - 20) / 79));
                
        if (caster.getAttributeMap() != null && caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.spellDamageMultiplier) != null) {
            factor *= (float) caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.spellDamageMultiplier).getAttributeValue();
        }
        
        return damage * factor;
    }

    public static boolean attackTargetSpecial(SpellData spellStack, Entity target, DamageSource damagesource, float magnitude) {

        if (target.world.isRemote)
            return true;

        EntityPlayer dmgSrcPlayer = null;

        if (damagesource.getTrueSource() != null) {
            if (damagesource.getTrueSource() instanceof EntityLivingBase) {
                EntityLivingBase source = (EntityLivingBase) damagesource.getTrueSource();
                if ((source instanceof EntityLightMage || source instanceof EntityDarkMage) && target.getClass() == EntityCreeper.class) {
                    return false;
                } else if (source instanceof EntityLightMage && target instanceof EntityLightMage) {
                    return false;
                } else if (source instanceof EntityDarkMage && target instanceof EntityDarkMage) {
                    return false;
                } else if (source instanceof EntityPlayer && target instanceof EntityPlayer && !target.world.isRemote && (!FMLCommonHandler.instance().getMinecraftServerInstance().isPVPEnabled() || ((EntityPlayer) target).capabilities.isCreativeMode)) {
                    return false;
                }

                if (source.isPotionActive(AMPotions.fury))
                    magnitude += 4;
            }

            if (damagesource.getTrueSource() instanceof EntityPlayer) {
                dmgSrcPlayer = (EntityPlayer) damagesource.getTrueSource();
                int armorSet = ArmorHelper.getFullArsMagicaArmorSet(dmgSrcPlayer);
                if (armorSet == ArsMagicaArmorMaterial.MAGE.getMaterialID()) {
                    magnitude *= ArsMagica.config.getMageSetDamageMultiplier();
                } else if (armorSet == ArsMagicaArmorMaterial.BATTLEMAGE.getMaterialID()) {
                    magnitude *= ArsMagica.config.getBattlemageSetDamageMultiplier();
                } else if (armorSet == ArsMagicaArmorMaterial.ARCHMAGE.getMaterialID()) {
                    magnitude *= ArsMagica.config.getArchmageSetDamageMultiplier();
                }

                ItemStack equipped = dmgSrcPlayer.getActiveItemStack();
                if (!equipped.isEmpty() && equipped.getItem() == AMItems.arcane_spellbook) {
                    magnitude *= 1.1f;
                }
            }
        }

        if (target instanceof EntityLivingBase) {
            if (EntityUtils.isSummon((EntityLivingBase) target) && damagesource.damageType.equals("magic")) {
                magnitude *= 3.0f;
            }
        }

        magnitude *= ArsMagica.config.getDamageMultiplier();
        if (dmgSrcPlayer != null) {
            Affinity dominantAffinity = spellStack != null ? spellStack.getMainShift() : null;
            magnitude *= EBWizardryCompatBootstrap.getArtefactPotencyMultiplier(dmgSrcPlayer, dominantAffinity);
        }

        boolean success = false;
        if (target instanceof EntityDragon) {
            success = ((EntityDragon) target).attackEntityFromPart(((EntityDragon) target).dragonPartBody, damagesource, magnitude);
        } else {
            success = target.attackEntityFrom(damagesource, magnitude);
        }

        if (dmgSrcPlayer != null) {
            if (spellStack != null && target instanceof EntityLivingBase) {
                if (!target.world.isRemote &&
                        ((EntityLivingBase) target).getHealth() <= 0 &&
                        spellStack.isModifierPresent(SpellModifiers.DISMEMBERING_LEVEL)) {
                    double chance = spellStack.getModifiedValue(0, SpellModifiers.DISMEMBERING_LEVEL, Operation.ADD, dmgSrcPlayer.world, dmgSrcPlayer, target);
                    if (dmgSrcPlayer.world.rand.nextDouble() <= chance) {
                        dropHead(target, dmgSrcPlayer.world);
                    }
                }
            }
        }

        return success;
    }

    private static void dropHead(Entity target, World world) {
        if (target instanceof EntityWither) {
            dropHead_do(world, target.posX, target.posY, target.posZ, 1);
        }
        if (target.getClass() == EntitySkeleton.class) {
            dropHead_do(world, target.posX, target.posY, target.posZ, 0);
        } else if (target.getClass() == EntityZombie.class) {
            dropHead_do(world, target.posX, target.posY, target.posZ, 2);
        } else if (target.getClass() == EntityCreeper.class) {
            dropHead_do(world, target.posX, target.posY, target.posZ, 4);
        } else if (target instanceof EntityPlayer) {
            dropHead_do(world, target.posX, target.posY, target.posZ, 3);
        }
    }

    private static void dropHead_do(World world, double x, double y, double z, int type) {
        EntityItem item = new EntityItem(world);
        ItemStack stack = new ItemStack(Items.SKULL, 1, type);
        item.setItem(stack);
        item.setPosition(x, y, z);
        world.spawnEntity(item);
    }

    public static NBTTagCompound encode(KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> toEncode) {
        NBTTagCompound group = new NBTTagCompound();
        group.setTag(SPELL_DATA, toEncode.value);
        int stage = 0;
        for (SpellPart part : toEncode.key) {
            NBTTagList stageTag = NBTUtils.addCompoundList(group, STAGE + stage);
            NBTTagCompound tmp = new NBTTagCompound();
            String id = SpellRegistryHelper.getSkillFromPart(part).getID();
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


    /**
     * Transform a flat list of spell parts into a list of stages.
     * Input format: [Shape1, Component1a, Component1b, Modifier1a, Shape2, Component2a, ...]
     * Output format: [[Shape1, Component1a, Component1b, Modifier1a], [Shape2, Component2a, ...]]
     * <p>
     * Each stage begins with a Shape and contains all following components/modifiers
     * until the next Shape is encountered.
     */
    public static List<List<SpellPart>> transformParts(List<SpellPart> parts) {
        List<List<SpellPart>> stages = Lists.newArrayList();
        List<SpellPart> stage = Lists.newArrayList();
        for (SpellPart part : parts) {
            if (part instanceof SpellShape) {
                // When we encounter a shape, finalize any previous stage first
                if (!stage.isEmpty()) {
                    stages.add(stage);
                    stage = new ArrayList<>();
                }
            }
            // Add the part to the current stage
            stage.add(part);
        }
        // Add the final stage if it has content
        if (!stage.isEmpty()) {
            stages.add(stage);
        }
        return stages;
    }

    //BACKWARD COMPAT
    public static ArrayList<SpellPart> getPartsForSpell(ItemStack stack) {
        try {
            ArrayList<SpellPart> mods = new ArrayList<SpellPart>();
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


    public static KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> decode(NBTTagCompound toDecode) {
        if (toDecode == null)
            return null;
        try {
            ArrayList<SpellPart> parts = new ArrayList<>();
            for (int j = 0; j < NBTUtils.getAM2Tag(toDecode).getInteger("StageNum"); j++) {
                NBTTagList stageTag = NBTUtils.addCompoundList(NBTUtils.getAM2Tag(toDecode), STAGE + j);
                for (int i = 0; i < stageTag.tagCount(); i++) {
                    NBTTagCompound tmp = stageTag.getCompoundTagAt(i);
                    String type = tmp.getString(TYPE);
                    if (type.equalsIgnoreCase(TYPE_COMPONENT)) {
                        parts.add(SpellRegistryHelper.getComponentFromName(tmp.getString(ID)));
                    }
                    if (type.equalsIgnoreCase(TYPE_MODIFIER)) {
                        parts.add(SpellRegistryHelper.getModifierFromName(tmp.getString(ID)));
                    }
                    if (type.equalsIgnoreCase(TYPE_SHAPE)) {
                        parts.add(SpellRegistryHelper.getShapeFromName(tmp.getString(ID)));
                    }
                }
            }
            return new KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>(parts, toDecode.getCompoundTag(SPELL_DATA));
        } catch (Exception e) {
            return null;
        }
    }

    public static void updateSpell(ItemStack in) {
        if (in.isEmpty() || !in.hasTagCompound() || in.getTagCompound().getBoolean("Updated") || !in.hasCapability(SpellCaster.INSTANCE, null))
            return;
        ISpellCaster caster = SpellCaster.of(in);
        NBTTagCompound tag = in.getTagCompound();
        if (!tag.hasKey("AM2")) {
            tag.setBoolean("Updated", true);
            return;
        }
        NBTTagCompound am2 = NBTUtils.getAM2Tag(tag);
        NBTTagCompound commonData = am2.getCompoundTag(SPELL_DATA);
        NBTTagList shapeGroupList = am2.getTagList("ShapeGroups", Constants.NBT.TAG_COMPOUND);
        List<List<List<SpellPart>>> shapeGroups = Lists.newArrayList();
        for (int i = 0; i < shapeGroupList.tagCount(); i++) {
            NBTTagCompound group = shapeGroupList.getCompoundTagAt(i);
            NBTTagCompound groupData = group.getCompoundTag(SPELL_DATA);
            int stageCount = group.getInteger("StageNum");
            List<SpellPart> parts = Lists.newArrayList();
            for (int j = 0; j < stageCount; j++) {
                NBTTagList list = group.getTagList(STAGE + j, Constants.NBT.TAG_COMPOUND);
                for (int k = 0; k < list.tagCount(); k++) {
                    NBTTagCompound nbt = list.getCompoundTagAt(k);
                    SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(nbt.getString(ID)));
                    if (part != null)
                        parts.add(part);
                }
            }
            caster.setStoredData(i, groupData);
            shapeGroups.add(transformParts(parts));
        }
        int count = am2.getInteger("StageNum");
        List<SpellPart> parts = Lists.newArrayList();
        for (int i = 0; i < count; i++) {
            NBTTagList list = am2.getTagList(STAGE + i, Constants.NBT.TAG_COMPOUND);
            for (int j = 0; j < list.tagCount(); j++) {
                NBTTagCompound tmp = list.getCompoundTagAt(j);
                SpellPart part = ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tmp.getString(ID)));
                if (part != null)
                    parts.add(part);
            }
        }
        caster.setCommonStoredData(commonData);
        caster.setSpellCommon(transformParts(parts));
        caster.setShapeGroups(shapeGroups);
        tag.setBoolean("Updated", true);
    }

    public static AbstractFlickerFunctionality GetAbstractFlickerFunctionalityFromID(int id) {
        for (AbstractFlickerFunctionality affinity : ArsMagicaAPI.getFlickerFocusRegistry().getValuesCollection()) {
            if (affinity.getID() == id) return affinity;
        }
        return null;
    }

    public static Affinity GetAffinityFromID(int id) {
        for (Affinity affinity : ArsMagicaAPI.getAffinityRegistry().getValuesCollection()) {
            if (affinity.getID() == id) return affinity;
        }
        return null;
    }

}
