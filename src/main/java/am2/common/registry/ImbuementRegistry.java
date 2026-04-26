package am2.common.registry;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.IImbuementRegistry;
import am2.api.items.armor.ImbuementTiers;
import am2.common.armor.ArmorHelper;
import am2.common.armor.imbuements.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.UUID;

public class ImbuementRegistry implements IImbuementRegistry {

    public static final ImbuementRegistry instance = new ImbuementRegistry();

    public static final int SLOT_BOOTS = 3;
    public static final int SLOT_LEGS = 2;
    public static final int SLOT_CHEST = 1;
    public static final int SLOT_HELM = 0;

    public static final UUID IMBUED_HASTE_ID = UUID.fromString("3b51a94c-8866-470b-8b69-e1d5cb50a72f");
    public static final AttributeModifier IMBUED_HASTE = new AttributeModifier(IMBUED_HASTE_ID, "Imbued Haste", 0.2, 2);

    // Generic imbuements
    public static final String MANA_REGEN = "mana_regen";
    public static final String BURNOUT_REDUCTION = "burnout_reduction";
    public static final String FLICKER_LURE = "flicker_lure";
    public static final String MAGIC_XP = "magic_xp";
    public static final String PINPOINT_ORES = "pinpoint_ores";
    public static final String MAGITECH_GOGGLE_INTEGRATION = "magitech_goggle_integration";
    public static final String STEP_ASSIST = "step_assist";
    public static final String RUN_SPEED = "run_speed";
    public static final String SOULBOUND = "soulbound";

    // Damage reduction imbuements
    public static final String PHYSICAL_DAMAGE_RESISTANCE = "physical_damage_resistance";
    public static final String DROWN_DAMAGE_RESISTANCE = "drown_damage_resistance";
    public static final String FALL_DAMAGE_RESISTANCE = "fall_damage_resistance";
    public static final String EXPLOSION_DAMAGE_RESISTANCE = "explosion_damage_resistance";
    public static final String FIRE_DAMAGE_RESISTANCE = "fire_damage_resistance";
    public static final String FROST_DAMAGE_RESISTANCE = "frost_damage_resistance";
    public static final String MAGIC_DAMAGE_RESISTANCE = "magic_damage_resistance";
    public static final String LIGHTNING_DAMAGE_RESISTANCE = "lightning_damage_resistance";

    // Individual imbuements
    public static final String DISPELLING = "dispelling";
    public static final String FALL_PROTECTION = "fall_protection";
    public static final String FIRE_PROTECTION = "fire_protection";
    public static final String FREEDOM_OF_MOVEMENT = "freedom_of_movement";
    public static final String HEALING = "healing";
    public static final String HUNGER_BOOST = "hunger_boost";
    public static final String JUMP_BOOST = "jump_boost";
    public static final String LIFE_SAVING = "life_saving";
    public static final String LIGHTSTEP = "lightstep";
    public static final String MINING_SPEED = "mining_speed";
    public static final String RECOIL = "recoil";
    public static final String SWIM_SPEED = "swim_speed";
    public static final String WATER_BREATHING = "water_breathing";
    public static final String WATER_WALKING = "water_walking";

    @Override
    public void registerImbuement(ArmorImbuement imbuementInstance) {
        ResourceLocation registryName = new ResourceLocation(ArsMagica.MODID, imbuementInstance.getID());
        imbuementInstance.setRegistryName(registryName);
        ArsMagicaAPI.getArmorImbuementRegistry().register(imbuementInstance);
    }

    @Override
    public ArmorImbuement getImbuementByID(ResourceLocation ID) {
        return ArsMagicaAPI.getArmorImbuementRegistry().getValue(ID);
    }

    @Override
    public ArmorImbuement[] getImbuementsForTier(ImbuementTiers tier, EntityEquipmentSlot armorType) {
        ArrayList<ArmorImbuement> list = new ArrayList<ArmorImbuement>();

        for (ArmorImbuement imbuement : ArsMagicaAPI.getArmorImbuementRegistry().getValues()) {
            if (imbuement.getTier() == tier) {
                for (EntityEquipmentSlot i : imbuement.getValidSlots()) {
                    if (i == armorType) {
                        list.add(imbuement);
                        break;
                    }
                }
            }
        }

        return list.toArray(new ArmorImbuement[list.size()]);
    }

    @Override
    public boolean isImbuementPresent(ItemStack stack, ArmorImbuement imbuement) {
        return isImbuementPresent(stack, imbuement.getID());
    }

    @Override
    public boolean isImbuementPresent(ItemStack stack, String id) {
        return ArmorHelper.isInfusionPreset(stack, id);
    }

    public static void registerAll() {
        // Damage reduction imbuements
        instance.registerImbuement(new DamageReductionImbuement(PHYSICAL_DAMAGE_RESISTANCE, "generic", ImbuementTiers.SECOND));
        instance.registerImbuement(new DamageReductionImbuement(DROWN_DAMAGE_RESISTANCE, "drown", ImbuementTiers.SECOND));
        instance.registerImbuement(new DamageReductionImbuement(FALL_DAMAGE_RESISTANCE, "fall", ImbuementTiers.SECOND));
        instance.registerImbuement(new DamageReductionImbuement(EXPLOSION_DAMAGE_RESISTANCE, "explosion", ImbuementTiers.SECOND));
        instance.registerImbuement(new DamageReductionImbuement(FIRE_DAMAGE_RESISTANCE, "fire", ImbuementTiers.THIRD));
        instance.registerImbuement(new DamageReductionImbuement(FROST_DAMAGE_RESISTANCE, "frost", ImbuementTiers.THIRD));
        instance.registerImbuement(new DamageReductionImbuement(MAGIC_DAMAGE_RESISTANCE, "magic", ImbuementTiers.THIRD));
        instance.registerImbuement(new DamageReductionImbuement(LIGHTNING_DAMAGE_RESISTANCE, "lightning", ImbuementTiers.THIRD));

        // Generic imbuements - all armor
        instance.registerImbuement(new GenericImbuement(MANA_REGEN, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD}));
        instance.registerImbuement(new GenericImbuement(BURNOUT_REDUCTION, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD}));
        instance.registerImbuement(new GenericImbuement(SOULBOUND, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD}));

        // Generic imbuements - chest
        instance.registerImbuement(new GenericImbuement(FLICKER_LURE, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.CHEST}));
        instance.registerImbuement(new GenericImbuement(MAGIC_XP, ImbuementTiers.FOURTH, new EntityEquipmentSlot[]{EntityEquipmentSlot.CHEST}));

        // Generic imbuements - helmet
        instance.registerImbuement(new GenericImbuement(PINPOINT_ORES, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD}));
        instance.registerImbuement(new GenericImbuement(MAGITECH_GOGGLE_INTEGRATION, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD}));

        // Generic imbuements - legs
        instance.registerImbuement(new GenericImbuement(STEP_ASSIST, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.LEGS}));

        // Generic imbuements - boots
        instance.registerImbuement(new GenericImbuement(RUN_SPEED, ImbuementTiers.FIRST, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET}));

        // Individual imbuements
        instance.registerImbuement(new Dispelling());
        instance.registerImbuement(new FallProtection());
        instance.registerImbuement(new FireProtection());
        instance.registerImbuement(new Freedom());
        instance.registerImbuement(new Healing());
        instance.registerImbuement(new HungerBoost());
        instance.registerImbuement(new JumpBoost());
        instance.registerImbuement(new LifeSaving());
        instance.registerImbuement(new Lightstep());
        instance.registerImbuement(new MiningSpeed());
        instance.registerImbuement(new Recoil());
        instance.registerImbuement(new SwimSpeed());
        instance.registerImbuement(new WaterBreathing());
        instance.registerImbuement(new WaterWalking());
    }
}
