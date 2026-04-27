package am2.common.config;

import am2.ArsMagica;
import am2.api.math.AMVector2;
import am2.api.skill.SkillPoint;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleController;
import am2.common.LogHelper;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.util.*;

public class AMConfig extends Configuration {

    /**
     * Simple data holder for a named affinity and its relative weight.
     * Intentionally has no EBWiz imports so it can be referenced freely
     * in any class, including those that may load before EBWiz.
     */
    public static class WeightedAffinity {
        public final String affinityName;
        public final float weight;
        public WeightedAffinity(String affinityName, float weight) {
            this.affinityName = affinityName;
            this.weight = weight;
        }
    }

    private final String KEY_PlayerSpellsDamageTerrain = "player_spells_destroy_terrain";
    private final String KEY_NpcSpellsDamageTerrain = "npc_spells_destroy_terrain";
    private final String KEY_RetroactiveWorldGen = "retroactive_worldgen";

    /** @deprecated Use {@link #getManaBurnoutRatio()} for the configurable value. Kept for compile-compat only. */
    @Deprecated
    public static final float MANA_BURNOUT_RATIO = 0.38f;

    private final String KEY_OldCalculations = "old_xp_calculations";
    private final String KEY_BurnoutHungerDepletion = "burnout_hunger_depletion";
    private final String KEY_JumpBoostFactor = "jump_boost_factor";

    private final String KEY_SecondarySkillTreeTierCap = "secondary_skill_tree_tier_cap";
    private final String KEY_MagicLevelCap = "magic_level_cap";
    private final String KEY_DigBreaksTEs = "dig_breaks_tile_entities";
    private final String KEY_DisplayManaInInventory = "display_mana_in_inventory";
    private final String KEY_ManaCap = "mana_cap";
    private final String KEY_BaseMana = "base_mana";
    private final String KEY_MageSpawnRate = "mage_spawn_rate";
    private final String KEY_WaterElementalSpawnRate = "water_elemental_spawn_rate";
    private final String KEY_HecateSpawnRate = "hecate_spawn_rate";
    private final String KEY_DryadSpawnRate = "dryad_spawn_rate";
    private final String KEY_DryadCropGrowthEnabled = "dryad_crop_growth_enabled";
    private final String KEY_DryadBonemealEnabled = "dryad_bonemeal_enabled";
    private final String KEY_DryadPlantGrowthRate = "dryad_plant_growth_rate";
    private final String KEY_ManaElementalSpawnRate = "mana_elemental_spawn_rate";
    private final String KEY_ManaCreeperSpawnRate = "mana_creeper_spawn_rate";
    private final String KEY_DarklingSpawnRate = "darkling_spawn_rate";
    private final String KEY_EarthElementalSpawnRate = "earth_elemental_spawn_rate";
    private final String KEY_EarthElementalSpawnUnderground = "earth_elemental_spawn_underground";
    private final String KEY_FireElementalSpawnRate = "fire_elemental_spawn_rate";
    private final String KEY_LightningElementalSpawnRate = "lightning_elemental_spawn_rate";
    private final String KEY_IceElementalSpawnRate = "ice_elemental_spawn_rate";
    private final String KEY_FlickerSpawnRate = "flicker_spawn_rate";

    /** Mob Stats **/
    private final String KEY_FireElementalMaxHealth = "fire_elemental_max_health";
    private final String KEY_FireElementalAttackDamage = "fire_elemental_attack_damage";
    private final String KEY_EarthElementalMaxHealth = "earth_elemental_max_health";
    private final String KEY_EarthElementalAttackDamage = "earth_elemental_attack_damage";
    private final String KEY_EarthElementalVariantMaxHealth = "earth_elemental_variant_max_health";
    private final String KEY_EarthElementalVariantAttackDamage = "earth_elemental_variant_attack_damage";
    private final String KEY_IceElementalMaxHealth = "ice_elemental_max_health";
    private final String KEY_IceElementalAttackDamage = "ice_elemental_attack_damage";
    private final String KEY_LightningElementalMaxHealth = "lightning_elemental_max_health";
    private final String KEY_LightningElementalAttackDamage = "lightning_elemental_attack_damage";
    private final String KEY_WaterElementalMaxHealth = "water_elemental_max_health";
    private final String KEY_ManaElementalMaxHealth = "mana_elemental_max_health";
    private final String KEY_DisableManaElementalParticles = "disable_mana_elemental_particles";
    private final String KEY_DarkMageMaxHealth = "dark_mage_max_health";
    private final String KEY_LightMageMaxHealth = "light_mage_max_health";
    private final String KEY_HecateMaxHealth = "hecate_max_health";
    private final String KEY_HecateAttackDamage = "hecate_attack_damage";
    private final String KEY_HellCowMaxHealth = "hell_cow_max_health";
    private final String KEY_HellCowAttackDamage = "hell_cow_attack_damage";
    private final String KEY_DarklingMaxHealth = "darkling_max_health";
    private final String KEY_ManaCreeperMaxHealth = "mana_creeper_max_health";
    private final String KEY_DryadMaxHealth = "dryad_max_health";
    private final String KEY_DarkMageArmor = "dark_mage_armor";
    private final String KEY_EarthElementalArmor = "earth_elemental_armor";
    private final String KEY_HecateArmor = "hecate_armor";
    private final String KEY_IceElementalArmor = "ice_elemental_armor";
    private final String KEY_FireElementalArmor = "fire_elemental_armor";
    private final String KEY_LightMageArmor = "light_mage_armor";
    private final String KEY_LightningElementalArmor = "lightning_elemental_armor";
    private final String KEY_ManaElementalArmor = "mana_elemental_armor";
    private final String KEY_HellCowArmor = "hell_cow_armor";
    /** End Mob Stats **/

    /** Skill Point Progression **/
    private final String KEY_SkillPointBlueMinLevel = "skill_point_blue_min_level";
    private final String KEY_SkillPointBlueLevelsPerPoint = "skill_point_blue_levels_per_point";
    private final String KEY_SkillPointGreenMinLevel = "skill_point_green_min_level";
    private final String KEY_SkillPointGreenLevelsPerPoint = "skill_point_green_levels_per_point";
    private final String KEY_SkillPointRedMinLevel = "skill_point_red_min_level";
    private final String KEY_SkillPointRedLevelsPerPoint = "skill_point_red_levels_per_point";
    private final String KEY_SkillPointYellowMinLevel = "skill_point_yellow_min_level";
    private final String KEY_SkillPointYellowLevelsPerPoint = "skill_point_yellow_levels_per_point";
    private final String KEY_SkillPointMagentaMinLevel = "skill_point_magenta_min_level";
    private final String KEY_SkillPointMagentaLevelsPerPoint = "skill_point_magenta_levels_per_point";
    private final String KEY_SkillPointCyanMinLevel = "skill_point_cyan_min_level";
    private final String KEY_SkillPointCyanLevelsPerPoint = "skill_point_cyan_levels_per_point";
    /** End Skill Point Progression **/

    private final String KEY_DamageMultiplier = "damage_multiplier";
    private final String KEY_ImbueEnabled = "imbue_enchant_enabled";

    private final String KEY_UseSpecialRenderers = "use_special_renderers";
    private final String KEY_FrictionCoefficient = "friction_coefficient";

    private final String KEY_MageVillagerProfessionID = "mage_villager_profession_id";

    private final String KEY_DigDisabledBlocks = "dig_blacklist";
    private final String KEY_WorldgenBlacklist = "worldgen_blacklist";

    private final String KEY_DisarmAffectsPlayers = "disarm_affects_players";

    private final String KEY_WitchwoodForestBiomeID = "witchwood_forest_biome_id";
    private final String KEY_WitchwoodForestRarity = "witchwood_forest_biome_rarity";

    private final String KEY_ForgeSmeltsVillagers = "forge_smelts_villagers";
    private final String KEY_EverstoneRepairRate = "everstone_repair_rate";
    private final String KEY_ManaMartiniDuration = "mana_martini_duration";
    private final String KEY_ManaCakeRegenDuration = "mana_cake_regen_duration";
    private final String KEY_ManaCakeFoodAmount = "mana_cake_food_amount";

    private final String KEY_LesserManaPotionMana = "lesser_mana_potion_mana";
    private final String KEY_LesserManaPotionRegenLevel = "lesser_mana_potion_regen_level";
    private final String KEY_LesserManaPotionRegenDuration = "lesser_mana_potion_regen_duration";
    private final String KEY_StandardManaPotionMana = "standard_mana_potion_mana";
    private final String KEY_StandardManaPotionRegenLevel = "standard_mana_potion_regen_level";
    private final String KEY_StandardManaPotionRegenDuration = "standard_mana_potion_regen_duration";
    private final String KEY_GreaterManaPotionMana = "greater_mana_potion_mana";
    private final String KEY_GreaterManaPotionRegenLevel = "greater_mana_potion_regen_level";
    private final String KEY_GreaterManaPotionRegenDuration = "greater_mana_potion_regen_duration";
    private final String KEY_EpicManaPotionMana = "epic_mana_potion_mana";
    private final String KEY_EpicManaPotionRegenLevel = "epic_mana_potion_regen_level";
    private final String KEY_EpicManaPotionRegenDuration = "epic_mana_potion_regen_duration";
    private final String KEY_LegendaryManaPotionMana = "legendary_mana_potion_mana";
    private final String KEY_LegendaryManaPotionRegenLevel = "legendary_mana_potion_regen_level";
    private final String KEY_LegendaryManaPotionRegenDuration = "legendary_mana_potion_regen_duration";

    private final String KEY_WitchwoodLeavesFall = "witchwood_leaf_particles";
    private final String KEY_CandlesAreRovingLights = "candles_are_roving_lights";
    private final String KEY_ColoredSpellBookNames = "colored_spell_book_names";
    private final String KEY_AppropriationBlockBlacklist = "appropriation_block_blacklist";
    private final String KEY_AppropriationMobBlacklist = "appropriation_mob_blacklist";

    private final String KEY_CrystallizeMaxHealth = "crystallize_max_health";
    private final String KEY_CrystallizeHealthThreshold = "crystallize_health_threshold";
    private final String KEY_CrystallizeMobBlacklist = "crystallize_mob_blacklist";

    private final String KEY_MeteorMinSpawnLevel = "meteor_spawn_min_level";
    private final String KEY_MeteorSpawnBaseChance = "meteor_spawn_base_chance";
    private final String KEY_MeteorSpawnMoonPhaseMultiplier = "meteor_spawn_moon_phase_multiplier";
    private final String KEY_MeteorSpawnCooldownMin = "meteor_spawn_cooldown_min";
    private final String KEY_MeteorSpawnCooldownMax = "meteor_spawn_cooldown_max";
    private final String KEY_MeteorSpawnRadiusPlayer = "meteor_spawn_radius_player";
    private final String KEY_MeteorSpawnRadiusAttractor = "meteor_spawn_radius_attractor";
    private final String KEY_HazardousGateways = "hazardous_gateways";
    private final String KEY_CanDryadsDespawn = "can_dryads_despawn";
    private final String KEY_MaxOrbsPerPlayer = "max_orbs_per_player";

    private final String KEY_ArmorXpInfusionFactor = "armor_xp_infusion_factor";
    private final String KEY_ArmorInfusionCostMultiplier = "armor_infusion_cost_multiplier";

    private final String KEY_MageSetDurationMultiplier = "mage_set_duration_multiplier";
    private final String KEY_BattlemageSetDurationMultiplier = "battlemage_set_duration_multiplier";
    private final String KEY_ArchmageSetDurationMultiplier = "archmage_set_duration_multiplier";
    private final String KEY_MageSetDamageMultiplier = "mage_set_damage_multiplier";
    private final String KEY_BattlemageSetDamageMultiplier = "battlemage_set_damage_multiplier";
    private final String KEY_ArchmageSetDamageMultiplier = "archmage_set_damage_multiplier";

    private final String KEY_EBWizSpellsInSpellBook = "EBWiz_Spells_In_SpellBook";
    private final String KEY_EBWizManaCostMultiplier = "EBWiz_Mana_Cost_Multiplier";
    private final String KEY_EBWizDisableWandMana = "EBWiz_Disable_Wand_Mana";
    private final String KEY_EBWizScrollRequiresMana = "EBWiz_Scroll_Requires_Mana";
    private final String KEY_EBWizHideWandTooltip = "EBWiz_Hide_Wand_Mana_Tooltip";
    private final String KEY_EBWizMagicXPMultiplier = "EBWiz_Magic_XP_Multiplier";
    private final String KEY_EBWizPreserveSpellBook = "EBWiz_Preserve_Spell_Book";
    private final String KEY_EBWizAffinityGainEnabled = "EBWiz_Affinity_Gain_Enabled";
    private final String KEY_EBWizAffinityGainAmount = "EBWiz_Affinity_Gain_Amount";
    private final String KEY_EBWizElementAffinityMap = "EBWiz_Element_Affinity_Map";
    private final String KEY_EBWizSpellAffinityOverrides = "EBWiz_Spell_Affinity_Overrides";
    private final String KEY_EBWizCondenserManaRegenEnabled = "EBWiz_Condenser_Mana_Regen_Enabled";
    private final String KEY_EBWizCondenserManaRegenBonusPerLevel = "EBWiz_Condenser_Mana_Regen_Bonus_Per_Level";
    private final String KEY_EBWizDiscoveryEnabled = "EBWiz_Discovery_Enabled";
    private final String KEY_EBWizDiscoveryElementItems = "EBWiz_Discovery_Element_Items";
    private final String KEY_EBWizDiscoveryTierCatalysts = "EBWiz_Discovery_Tier_Catalysts";
    private final String KEY_EBWizDiscoveryPowerCosts = "EBWiz_Discovery_Power_Costs";
    private final String KEY_EBWizTranscriptionEnabled = "EBWiz_Transcription_Enabled";
    private final String KEY_EBWizTranscriptionCostBase = "EBWiz_Transcription_Cost_Base";
    private final String KEY_EBWizTranscriptionCostPerTier = "EBWiz_Transcription_Cost_Per_Tier";
    private final String KEY_EBWizDisciplineCostReductionPerLevel = "EBWiz_Discipline_Cost_Reduction_Per_Level";
    private final String KEY_EBWizDisciplinePotencyBonusPerLevel = "EBWiz_Discipline_Potency_Bonus_Per_Level";
    private final String KEY_EBWizArtefactPotencyRatio = "EBWiz_Artefact_Potency_Ratio";
    private final String KEY_EBWizEvilWizardOrbDrop = "EBWiz_Evil_Wizard_Orb_Drop";
    private final String KEY_EBWizElementalCrystalDrops = "EBWiz_Elemental_Crystal_Drops";
    private final String KEY_ManaDrainRatio = "mana_drain_ratio";
    private final String KEY_SavePowerOnWorldSave = "save_power_on_world_save";
    private final String KEY_MagicResistExtraDamageTypes = "magic_resist_extra_damage_types";
    private final String KEY_ObeliskExtraFuels = "obelisk_extra_fuels";
    private final String KEY_ExtraAltarCaps = "extra_altar_caps";
    private final String KEY_ExtraAltarMain = "extra_altar_main";

    /** Winter Arm **/
    private final String KEY_WinterArmFlightTicks = "winter_arm_flight_ticks";

    /** Life Ward **/
    private final String KEY_LifeWardTickInterval = "life_ward_tick_interval";
    private final String KEY_LifeWardMaxAbsorption = "life_ward_max_absorption";
    private final String KEY_LifeWardAbsorptionPerTick = "life_ward_absorption_per_tick";
    private final String KEY_LifeWardEnabled = "life_ward_enabled";

    /** Spell Damage Base Values **/
    private final String KEY_FrostDamageBase = "frost_damage_base";
    private final String KEY_FireDamageBase = "fire_damage_base";
    private final String KEY_LifeDrainBase = "life_drain_base";
    private final String KEY_ManaDrainBase = "mana_drain_base";
    private final String KEY_MagicDamageBase = "magic_damage_base";
    private final String KEY_LightningDamageBase = "lightning_damage_base";
    private final String KEY_DrownDamageBase = "drown_damage_base";
    private final String KEY_PhysicalDamageBase = "physical_damage_base";
    private final String KEY_BlizzardDamageMultiplier = "blizzard_damage_multiplier";
    private final String KEY_LifeTapDamageMultiplier = "life_tap_damage_multiplier";
    private final String KEY_FireRainDamageMultiplier = "fire_rain_damage_multiplier";
    private final String KEY_FallingStarDamageMultiplier = "falling_star_damage_multiplier";
    private final String KEY_DisarmDamageMultiplier = "disarm_damage_multiplier";
    /** End Spell Damage Base Values **/

    /** Boss Stats **/
    private final String KEY_BossMaxDamagePerHit = "boss_max_damage_per_hit";
    private final String KEY_BossHurtResistantTime = "boss_hurt_resistant_time";
    private final String KEY_FireGuardianMaxHealth = "fire_guardian_max_health";
    private final String KEY_FireGuardianArmor = "fire_guardian_armor";
    private final String KEY_WaterGuardianMaxHealth = "water_guardian_max_health";
    private final String KEY_WaterGuardianArmor = "water_guardian_armor";
    private final String KEY_AirGuardianMaxHealth = "air_guardian_max_health";
    private final String KEY_AirGuardianArmor = "air_guardian_armor";
    private final String KEY_EarthGuardianMaxHealth = "earth_guardian_max_health";
    private final String KEY_EarthGuardianArmor = "earth_guardian_armor";
    private final String KEY_LightningGuardianMaxHealth = "lightning_guardian_max_health";
    private final String KEY_LightningGuardianArmor = "lightning_guardian_armor";
    private final String KEY_LifeGuardianMaxHealth = "life_guardian_max_health";
    private final String KEY_LifeGuardianArmor = "life_guardian_armor";
    private final String KEY_EnderGuardianMaxHealth = "ender_guardian_max_health";
    private final String KEY_EnderGuardianArmor = "ender_guardian_armor";
    private final String KEY_ArcaneGuardianMaxHealth = "arcane_guardian_max_health";
    private final String KEY_ArcaneGuardianArmor = "arcane_guardian_armor";
    private final String KEY_NatureGuardianMaxHealth = "nature_guardian_max_health";
    private final String KEY_NatureGuardianArmor = "nature_guardian_armor";
    private final String KEY_WinterGuardianMaxHealth = "winter_guardian_max_health";
    private final String KEY_WinterGuardianArmor = "winter_guardian_armor";
    /** End Boss Stats **/

    /** Mana & Progression **/
    private final String KEY_BaseTicksForFullRegen = "base_ticks_for_full_regen";
    private final String KEY_ManaCoefficient = "mana_coefficient";
    private final String KEY_RegenScalingBase = "regen_scaling_base";
    private final String KEY_RegenScalingPerLevel = "regen_scaling_per_level";
    private final String KEY_XPRateMultiplier = "xp_rate_multiplier";
    /** End Mana & Progression **/

    /** Power System **/
    private final String KEY_PowerSearchRadius = "power_search_radius";
    private final String KEY_MaxPowerSearchRadius = "max_power_search_radius";
    private final String KEY_LightInputCostMultiplier = "light_input_cost_multiplier";
    private final String KEY_NeutralInputCostMultiplier = "neutral_input_cost_multiplier";
    private final String KEY_DarkInputCostMultiplier = "dark_input_cost_multiplier";
    private final String KEY_AstralBarrierCostPerRadius = "astral_barrier_cost_per_radius";
    private final String KEY_AstralBarrierDarkCost = "astral_barrier_dark_cost";
    private final String KEY_CapacityManaBattery = "capacity_mana_battery";
    private final String KEY_CapacityBlackAurem = "capacity_black_aurem";
    private final String KEY_CapacityObelisk = "capacity_obelisk";
    private final String KEY_CapacityCelestialPrism = "capacity_celestial_prism";
    private final String KEY_CapacitySummoner = "capacity_summoner";
    private final String KEY_CapacityEssenceRefiner = "capacity_essence_refiner";
    private final String KEY_CapacityManaDrain = "capacity_mana_drain";
    private final String KEY_CapacityCraftingAltar = "capacity_crafting_altar";
    private final String KEY_CapacityArcaneReconstructor = "capacity_arcane_reconstructor";
    private final String KEY_CapacityArcaneDeconstructor = "capacity_arcane_deconstructor";
    private final String KEY_CapacityInertSpawner = "capacity_inert_spawner";
    private final String KEY_CapacityKeystoneReceptacle = "capacity_keystone_receptacle";
    private final String KEY_CapacityAstralBarrier = "capacity_astral_barrier";
    private final String KEY_CapacityFlickerLure = "capacity_flicker_lure";
    private final String KEY_CapacityOtherworldAura = "capacity_otherworld_aura";
    private final String KEY_CapacityCalefactor = "capacity_calefactor";
    private final String KEY_CapacitySeerStone = "capacity_seer_stone";
    private final String KEY_CapacitySlipstreamGenerator = "capacity_slipstream_generator";
    /** End Power System **/

    /** Affinity Ability Thresholds & Effects **/
    private final String KEY_AffinityAgileMinDepth = "affinity_agile_min_depth";
    private final String KEY_AffinityAgileJumpBoost = "affinity_agile_jump_boost";
    private final String KEY_AffinityColdBloodedMinDepth = "affinity_cold_blooded_min_depth";
    private final String KEY_AffinityThornsMinDepth = "affinity_thorns_min_depth";
    private final String KEY_AffinityThornsDamage1 = "affinity_thorns_damage_1";
    private final String KEY_AffinityThornsDamage2 = "affinity_thorns_damage_2";
    private final String KEY_AffinityThornsDamage3 = "affinity_thorns_damage_3";
    private final String KEY_AffinityFulminationMinDepth = "affinity_fulmination_min_depth";
    private final String KEY_AffinityFulminationLightningChance = "affinity_fulmination_lightning_chance";
    /** End Affinity Ability Thresholds **/

    /** Burnout Formula **/
    private final String KEY_ManaBurnoutRatio = "mana_burnout_ratio";
    private final String KEY_BurnoutPerLevel = "burnout_per_level";
    private final String KEY_BurnoutBase = "burnout_base";
    private final String KEY_AffinityHealCooldownFull = "affinity_heal_cooldown_full";
    private final String KEY_AffinityHealCooldownPartial = "affinity_heal_cooldown_partial";
    /** End Burnout Formula **/

    /** Flicker Generation Weights **/
    private final String KEY_FlickerWeightAir = "flicker_weight_air";
    private final String KEY_FlickerWeightArcane = "flicker_weight_arcane";
    private final String KEY_FlickerWeightEarth = "flicker_weight_earth";
    private final String KEY_FlickerWeightEnder = "flicker_weight_ender";
    private final String KEY_FlickerWeightFire = "flicker_weight_fire";
    private final String KEY_FlickerWeightIce = "flicker_weight_ice";
    private final String KEY_FlickerWeightLife = "flicker_weight_life";
    private final String KEY_FlickerWeightLightning = "flicker_weight_lightning";
    private final String KEY_FlickerWeightNature = "flicker_weight_nature";
    private final String KEY_FlickerWeightWater = "flicker_weight_water";
    /** End Flicker Generation Weights **/

    /** Beam Shape Tick Rates **/
    private final String KEY_BeamBlockTickRate = "beam_block_tick_rate";
    private final String KEY_BeamEntityTickRate = "beam_entity_tick_rate";
    private final String KEY_ChannelTickRate = "channel_tick_rate";
    /** End Beam Shape Tick Rates **/

    /** Processing Block Costs **/
    private final String KEY_ReconstructorRepairCost = "reconstructor_repair_cost";
    private final String KEY_DeconstructorTime = "deconstructor_time";
    private final String KEY_DeconstructorPowerCost = "deconstructor_power_cost";
    /** End Processing Block Costs **/

    /** Spell Balance **/
    private final String KEY_ZoneDefaultRadius = "zone_default_radius";
    private final String KEY_ZoneDefaultDuration = "zone_default_duration";
    private final String KEY_WallDefaultRadius = "wall_default_radius";
    private final String KEY_WallDefaultDuration = "wall_default_duration";
    private final String KEY_PuddleDefaultRadius = "puddle_default_radius";
    private final String KEY_PuddleDefaultDuration = "puddle_default_duration";
    private final String KEY_PuddleDefaultRange = "puddle_default_range";
    private final String KEY_WaveDefaultRadius = "wave_default_radius";
    private final String KEY_WaveSpeedMultiplier = "wave_speed_multiplier";
    private final String KEY_WaveDefaultDuration = "wave_default_duration";
    private final String KEY_WaveGravityPerModifier = "wave_gravity_per_modifier";
    private final String KEY_ConeAngle = "cone_angle";
    private final String KEY_SpeedModifierValue = "speed_modifier_value";
    private final String KEY_HealingModifierValue = "healing_modifier_value";
    private final String KEY_RangeModifierValue = "range_modifier_value";
    private final String KEY_DurationModifierValue = "duration_modifier_value";
    private final String KEY_GravityModifierValue = "gravity_modifier_value";
    private final String KEY_PiercingModifierValue = "piercing_modifier_value";
    private final String KEY_SolarDamageBase = "solar_damage_base";
    private final String KEY_SolarDurationBase = "solar_duration_base";
    private final String KEY_SolarHealingBase = "solar_healing_base";
    private final String KEY_SolarRangeBase = "solar_range_base";
    private final String KEY_LunarDamageBase = "lunar_damage_base";
    private final String KEY_LunarDurationBase = "lunar_duration_base";
    private final String KEY_LunarHealingBase = "lunar_healing_base";
    private final String KEY_LunarRangeBase = "lunar_range_base";
    private final String KEY_DefaultBuffDuration = "default_buff_duration";
    private final String KEY_BuffPowerDurationBonus = "buff_power_duration_bonus";
    private final String KEY_HealCooldown = "heal_cooldown";
    private final String KEY_MaxStageGroups = "max_stage_groups";
    private final String KEY_MaxRecipeSize = "max_recipe_size";
    /** End Spell Balance **/

    private final String KEY_EnableWitchwoodForest = "enable_witchwood_forest";

    private final String KEY_FairyRingMushroomBlocks = "fairy_ring_mushroom_blocks";

    private final String KEY_GatewayRuinEnabled = "gateway_ruin_enabled";
    private final String KEY_GatewayRuinFrequency = "gateway_ruin_frequency";
    private final String KEY_GatewayRuinBiomes = "gateway_ruin_biomes";
    private final String KEY_GatewayRuinBiomeBlacklist = "gateway_ruin_biome_blacklist";
    private final String KEY_GatewayRuinChest = "gateway_ruin_chest";

    private final String KEY_LostArchiveEnabled = "lost_archive_enabled";
    private final String KEY_LostArchiveFrequency = "lost_archive_frequency";
    private final String KEY_LostArchiveBiomeBlacklist = "lost_archive_biome_blacklist";

    private final String KEY_AllowCreativeTargets = "allow_creative_targets";
    private final String KEY_StaffDurability = "staff_durability";
    private final String KEY_StaffPresets = "staff_presets";

    private final String KEY_EssenceLakeFrequency = "essence_lake_frequency";
    private final String KEY_TarmaRootFrequency = "tarma_root_frequency";
    private final String KEY_WitchwoodFrequency = "witchwood_frequency";
    private final String KEY_WakebloomFrequency = "wakebloom_frequency";
    private final String KEY_CeruBlossomFrequency = "cerublossom_frequency";
    private final String KEY_DesertNovaFrequency = "desert_nova_frequency";
    private final String KEY_EssencePuddleFrequency = "essence_puddle_frequency";
    private final String KEY_BlueTopazFrequency = "blue_topaz_frequency";
    private final String KEY_BlueTopazVeinSize = "blue_topaz_vein_size";
    private final String KEY_BlueTopazMinHeight = "blue_topaz_min_height";
    private final String KEY_BlueTopazMaxHeight = "blue_topaz_max_height";
    private final String KEY_VinteumFrequency = "vinteum_frequency";
    private final String KEY_VinteumVeinSize = "vinteum_vein_size";
    private final String KEY_VinteumMinHeight = "vinteum_min_height";
    private final String KEY_VinteumMaxHeight = "vinteum_max_height";
    private final String KEY_ChimeriteFrequency = "chimerite_frequency";
    private final String KEY_ChimeriteVeinSize = "chimerite_vein_size";
    private final String KEY_ChimeriteMinHeight = "chimerite_min_height";
    private final String KEY_ChimeriteMaxHeight = "chimerite_max_height";
    private final String KEY_SunstoneFrequency = "sunstone_frequency";
    private final String KEY_SunstoneVeinSize = "sunstone_vein_size";
    private final String KEY_SunstoneMinHeight = "sunstone_min_height";
    private final String KEY_SunstoneMaxHeight = "sunstone_max_height";

    /** Beta Particles **/
    private final String KEY_AuraType = "aura_type";
    private final String KEY_AuraBehaviour = "aura_behaviour";
    private final String KEY_AuraScale = "aura_scale";
    private final String KEY_AuraColor = "aura_color";
    private final String KEY_AuraQuanity = "aura_quantity";
    private final String KEY_AuraDelay = "aura_delay";
    private final String KEY_AuraSpeed = "aura_speed";
    private final String KEY_AuraAlpha = "aura_alpha";
    private final String KEY_AuraColorRandomize = "aura_color_randomize";
    private final String KEY_AuraColorDefault = "aura_color_default";
    /** End Beta Particles **/

    /** GUI Config **/
    private final String KEY_ManaHudPositionX = "mana_hud_position_x";
    private final String KEY_BurnoutHudPositionX = "burnout_hud_position_x";
    private final String KEY_BuffsPositivePositionX = "buffs_positive_position_x";
    private final String KEY_BuffsNegativePositionX = "buffs_negative_position_x";
    private final String KEY_LevelPositionX = "level_position_x";
    private final String KEY_AffinityPositionX = "affinity_position_x";
    private final String KEY_ArmorPositionHeadX = "armor_position_head_x";
    private final String KEY_ArmorPositionChestX = "armor_position_chest_x";
    private final String KEY_ArmorPositionLegsX = "armor_position_legs_x";
    private final String KEY_ArmorPositionBootsX = "armor_position_boots_x";

    private final String KEY_ManaHudPositionY = "mana_hud_position_y";
    private final String KEY_BurnoutHudPositionY = "burnout_hud_position_y";
    private final String KEY_BuffsPositivePositionY = "buffs_positive_position_y";
    private final String KEY_BuffsNegativePositionY = "buffs_negative_position_y";
    private final String KEY_LevelPositionY = "level_position_y";
    private final String KEY_AffinityPositionY = "affinity_position_y";
    private final String KEY_ArmorPositionHeadY = "armor_position_head_y";
    private final String KEY_ArmorPositionChestY = "armor_position_chest_y";
    private final String KEY_ArmorPositionLegsY = "armor_position_legs_y";
    private final String KEY_ArmorPositionBootsY = "armor_position_boots_y";
    private final String KEY_XPBarPositionX = "xp_bar_position_x";
    private final String KEY_XPBarPositionY = "xp_bar_position_y";
    private final String KEY_ContingencyPositionX = "contingency_position_x";
    private final String KEY_ContingencyPositionY = "contingency_position_y";
    private final String KEY_ManaNumericPositionX = "mana_numeric_x";
    private final String KEY_ManaNumericPositionY = "mana_numeric_y";
    private final String KEY_BurnoutNumericPositionX = "burnout_numeric_x";
    private final String KEY_BurnoutNumericPositionY = "burnout_numeric_y";
    private final String KEY_XPNumericPositionX = "xp_numeric_x";
    private final String KEY_XPNumericPositionY = "xp_numeric_y";
    private final String KEY_SpellBookPositionX = "spell_book_x";
    private final String KEY_SpellBookPositionY = "spell_book_y";
    private final String KEY_EnderAffinityAbilityCooldown = "ender_affinity_ability_cd";

    private final String KEY_ManaShieldingPositionX = "mana_shielding_x";
    private final String KEY_ManaShieldingPositionY = "mana_shielding_y";

    private final String KEY_StagedCompendium = "staged_compendium";

    private final String KEY_ShowHudMinimally = "show_hud_minimally";
    private final String KEY_ShowArmorUI = "show_armor_ui";
    private final String KEY_MoonstoneMeteorsDestroyTerrain = "moonstone_meteor_destroy_terrain";

    private final String KEY_ManaBarWidth = "mana_bar_width";
    private final String KEY_ManaBarHeight = "mana_bar_height";
    private final String KEY_BurnoutBarWidth = "burnout_bar_width";
    private final String KEY_BurnoutBarHeight = "burnout_bar_height";

    private final String KEY_ShowBuffs = "show_buff_timers";
    private final String KEY_ShowNumerics = "show_numeric_values";
    private final String KEY_ShowXPAlways = "show_xp_always";
    private final String KEY_ShowHUDBars = "show_hud_bars";
    private final String KEY_ColourblindMode = "colourblind_mode";
    /** End GUI Config **/

    private static final String CATEGORY_BETA = "beta";
    private static final String CATEGORY_MOBS = "mobs";
    private static final String CATEGORY_UI = "guis";
    private static final String CATEGORY_WORLDGEN = "worldgen";
    private static final String CATEGORY_INTEGRATION = "integration";
    private static final String CATEGORY_SPELL_DAMAGE = "spell_damage";
    private static final String CATEGORY_BOSSES = "bosses";
    private static final String CATEGORY_POWER = "power";
    private static final String CATEGORY_SPELL_BALANCE = "spell_balance";
    private static final String CATEGORY_AFFINITY = "affinity_abilities";
    private static final String CATEGORY_FLICKER = "flicker_generation";

    private int GFXLevel;
    private boolean PlayerSpellsDamageTerrain;
    private boolean NPCSpellsDamageTerrain;
    private float DamageMultiplier;
    private boolean IsImbueEnabled;
    private boolean UseSpecialRenderers;
    private boolean DisplayManaInInventory;
    private boolean RetroWorldGen;
    private boolean moonstoneMeteorsDestroyTerrain;
    private boolean suggestSpellNames;
    private int staffDurability;
    private String[] staffPresets;

    /** Skill Point Progression **/
    private int skillPointBlueMinLevel;
    private int skillPointBlueLevelsPerPoint;
    private int skillPointGreenMinLevel;
    private int skillPointGreenLevelsPerPoint;
    private int skillPointRedMinLevel;
    private int skillPointRedLevelsPerPoint;
    private int skillPointYellowMinLevel;
    private int skillPointYellowLevelsPerPoint;
    private int skillPointMagentaMinLevel;
    private int skillPointMagentaLevelsPerPoint;
    private int skillPointCyanMinLevel;
    private int skillPointCyanLevelsPerPoint;
    /** End Skill Point Progression **/

    private boolean forgeSmeltsVillagers;
    private boolean witchwoodLeafParticles;
    private int everstoneRepairRate;
    private int manaMartiniDuration;
    private int manaCakeRegenDuration;
    private int manaCakeFoodAmount;

    private float lesserManaPotionMana;
    private int lesserManaPotionRegenLevel;
    private int lesserManaPotionRegenDuration;
    private float standardManaPotionMana;
    private int standardManaPotionRegenLevel;
    private int standardManaPotionRegenDuration;
    private float greaterManaPotionMana;
    private int greaterManaPotionRegenLevel;
    private int greaterManaPotionRegenDuration;
    private float epicManaPotionMana;
    private int epicManaPotionRegenLevel;
    private int epicManaPotionRegenDuration;
    private float legendaryManaPotionMana;
    private int legendaryManaPotionRegenLevel;
    private int legendaryManaPotionRegenDuration;

    private int winterArmFlightTicks;

    private int lifeWardTickInterval;
    private int lifeWardMaxAbsorption;
    private float lifeWardAbsorptionPerTick;
    private boolean lifeWardEnabled;

    private int witchwoodForestID;

    private float FrictionCoefficient;
    private float JumpBoostFactor;

    private int secondarySkillTreeTierCap;
    private int magicLevelCap;
    private int mageVillagerProfessionID;
    private String[] digBlacklist;
    private int[] worldgenBlacklist;
    private boolean enableWitchwoodForest;
    private int witchwoodForestRarity;

    private String[] fairyRingMushroomBlocks;

    private boolean gatewayRuinEnabled;
    private int gatewayRuinFrequency;
    private String[] gatewayRuinBiomes;
    private String[] gatewayRuinBiomeBlacklist;
    private boolean gatewayRuinChest;

    private boolean lostArchiveEnabled;
    private int lostArchiveFrequency;
    private String[] lostArchiveBiomeBlacklist;

    private boolean allowCreativeTargets;

    private int essenceLakeFrequency;
    private int tarmaRootFrequency;
    private int witchwoodFrequency;
    private int wakebloomFrequency;
    private int ceruBlossomFrequency;
    private int desertNovaFrequency;
    private int essencePuddleFrequency;
    private int blueTopazFrequency;
    private int blueTopazVeinSize;
    private int blueTopazMinHeight;
    private int blueTopazMaxHeight;
    private int vinteumFrequency;
    private int vinteumVeinSize;
    private int vinteumMinHeight;
    private int vinteumMaxHeight;
    private int chimeriteFrequency;
    private int chimeriteVeinSize;
    private int chimeriteMinHeight;
    private int chimeriteMaxHeight;
    private int sunstoneFrequency;
    private int sunstoneVeinSize;
    private int sunstoneMinHeight;
    private int sunstoneMaxHeight;

    private String[] appropriationBlockBlacklist;
    private Class<? extends Entity>[] appropriationMobBlacklist;

    private int crystallizeMaxHealth;
    private float crystallizeHealthThreshold;
    private String[] crystallizeMobBlacklist;

    private int AuraType;
    private int AuraBehaviour;
    private float AuraScale;
    private float AuraAlpha;
    private int AuraColor;
    private int AuraDelay;
    private int AuraQuantity;
    private double AuraSpeed;
    private boolean AuraRandomColor;
    private boolean AuraDefaultColor;
    private double ArmorXPInfusionFactor;
    private double armorInfusionCostMultiplier;

    private float mageSetDurationMultiplier;
    private float battlemageSetDurationMultiplier;
    private float archmageSetDurationMultiplier;
    private float mageSetDamageMultiplier;
    private float battlemageSetDamageMultiplier;
    private float archmageSetDamageMultiplier;
    private double manaCap;
    private double baseMana;
    private int enderAffinityAbilityCooldown;

    private AMVector2 manaHudPosition;
    private AMVector2 burnoutHudPosition;
    private int manaBarWidth = 100;
    private int manaBarHeight = 10;
    private int burnoutBarWidth = 100;
    private int burnoutBarHeight = 10;
    private AMVector2 positiveBuffsPosition;
    private AMVector2 negativeBuffsPosition;
    private AMVector2 levelPosition;
    private AMVector2 affinityPosition;
    private AMVector2 armorPositionHead;
    private AMVector2 armorPositionChest;
    private AMVector2 armorPositionLegs;
    private AMVector2 armorPositionBoots;
    private AMVector2 xpBarPosition;
    private AMVector2 contingencyPosition;
    private AMVector2 manaNumericPosition;
    private AMVector2 burnoutNumericPosition;
    private AMVector2 XPNumericPosition;
    private AMVector2 SpellBookPosition;
    private AMVector2 manaShieldingPosition;
    private boolean showBuffs;
    private boolean showNumerics;
    private boolean showHudMinimally;
    private boolean showXPAlways;
    private boolean showArmorUI;
    private boolean stagedCompendium;
    private boolean showHudBars;
    private boolean colourblindMode;
    private boolean candlesAreRovingLights;
    private boolean coloredSpellBookNames;
    private int meteorMinSpawnLevel;
    private int meteorSpawnBaseChance;
    private int meteorSpawnMoonPhaseMultiplier;
    private int meteorSpawnCooldownMin;
    private int meteorSpawnCooldownMax;
    private int meteorSpawnRadiusPlayer;
    private int meteorSpawnRadiusAttractor;
    private boolean hazardousGateways;
    private boolean disarmAffectsPlayers;
    private boolean digBreaksTileEntities;
    private int maxOrbsPerPlayer;
    private boolean savePowerOnWorldSave;

    private boolean canDryadsDespawn;

    private boolean oldXpCalculations;
    private boolean burnoutHungerDepletion;
    private boolean ebwizSpellsInSpellBook;
    private float ebwizManaCostMultiplier;
    private boolean ebwizDisableWandMana;
    private boolean ebwizScrollRequiresMana;
    private boolean ebwizHideWandTooltip;
    private float ebwizMagicXPMultiplier;
    private boolean ebwizPreserveSpellBook;
    private boolean ebwizAffinityGainEnabled;
    private double ebwizAffinityGainAmount;
    private Map<String, List<WeightedAffinity>> ebwizElementAffinityMap;
    private Map<String, List<WeightedAffinity>> ebwizSpellAffinityOverrides;
    private boolean ebwizCondenserManaRegenEnabled;
    private float ebwizCondenserManaRegenBonusPerLevel;
    private boolean ebwizDiscoveryEnabled;
    private String[] ebwizDiscoveryElementItems;
    private String[] ebwizDiscoveryTierCatalysts;
    private int[] ebwizDiscoveryPowerCosts;
    private boolean ebwizTranscriptionEnabled;
    private int ebwizTranscriptionCostBase;
    private int ebwizTranscriptionCostPerTier;
    private float ebwizDisciplineCostReductionPerLevel;
    private float ebwizDisciplinePotencyBonusPerLevel;
    private float ebwizArtefactPotencyRatio;
    private boolean ebwizEvilWizardOrbDrop;
    private boolean ebwizElementalCrystalDrops;
    private boolean ebwizMageTomeDrop;
    private double manaDrainRatio;
    private java.util.Set<String> magicResistExtraDamageTypes;
    private String[] obeliskExtraFuels;
    private String[] extraAltarCaps;
    private String[] extraAltarMain;

    private double frostDamageBase;
    private double fireDamageBase;
    private double lifeDrainBase;
    private double manaDrainBase;
    private double magicDamageBase;
    private double lightningDamageBase;
    private double drownDamageBase;
    private double physicalDamageBase;
    private double blizzardDamageMultiplier;
    private double lifeTapDamageMultiplier;
    private double fireRainDamageMultiplier;
    private double fallingStarDamageMultiplier;
    private double disarmDamageMultiplier;

    /** Mob Stats **/
    private double fireElementalMaxHealth;
    private double fireElementalAttackDamage;
    private double earthElementalMaxHealth;
    private double earthElementalAttackDamage;
    private double earthElementalVariantMaxHealth;
    private double earthElementalVariantAttackDamage;
    private double iceElementalMaxHealth;
    private double iceElementalAttackDamage;
    private double lightningElementalMaxHealth;
    private double lightningElementalAttackDamage;
    private double waterElementalMaxHealth;
    private double manaElementalMaxHealth;
    private boolean disableManaElementalParticles;
    private double darkMageMaxHealth;
    private double lightMageMaxHealth;
    private double hecateMaxHealth;
    private double hecateAttackDamage;
    private double hellCowMaxHealth;
    private double hellCowAttackDamage;
    private double darklingMaxHealth;
    private double manaCreeperMaxHealth;
    private double dryadMaxHealth;
    private double darkMageArmor;
    private double earthElementalArmor;
    private double hecateArmor;
    private double iceElementalArmor;
    private double fireElementalArmor;
    private double lightMageArmor;
    private double lightningElementalArmor;
    private double manaElementalArmor;
    private double hellCowArmor;
    /** End Mob Stats **/

    /** Boss Stats **/
    private float bossMaxDamagePerHit;
    private int bossHurtResistantTime;
    private double fireGuardianMaxHealth;
    private double fireGuardianArmor;
    private double waterGuardianMaxHealth;
    private double waterGuardianArmor;
    private double airGuardianMaxHealth;
    private double airGuardianArmor;
    private double earthGuardianMaxHealth;
    private double earthGuardianArmor;
    private double lightningGuardianMaxHealth;
    private double lightningGuardianArmor;
    private double lifeGuardianMaxHealth;
    private double lifeGuardianArmor;
    private double enderGuardianMaxHealth;
    private double enderGuardianArmor;
    private double arcaneGuardianMaxHealth;
    private double arcaneGuardianArmor;
    private double natureGuardianMaxHealth;
    private double natureGuardianArmor;
    private double winterGuardianMaxHealth;
    private double winterGuardianArmor;
    /** End Boss Stats **/

    /** Mana & Progression **/
    private int baseTicksForFullRegen;
    private double manaCoefficient;
    private double regenScalingBase;
    private double regenScalingPerLevel;
    private double xpRateMultiplier;
    /** End Mana & Progression **/

    /** Power System **/
    private int powerSearchRadius;
    private int maxPowerSearchRadius;
    private float lightInputCostMultiplier;
    private float neutralInputCostMultiplier;
    private float darkInputCostMultiplier;
    private float astralBarrierCostPerRadius;
    private float astralBarrierDarkCost;
    private int capacityManaBattery;
    private int capacityBlackAurem;
    private int capacityObelisk;
    private int capacityCelestialPrism;
    private int capacitySummoner;
    private int capacityEssenceRefiner;
    private int capacityManaDrain;
    private int capacityCraftingAltar;
    private int capacityArcaneReconstructor;
    private int capacityArcaneDeconstructor;
    private int capacityInertSpawner;
    private int capacityKeystoneReceptacle;
    private int capacityAstralBarrier;
    private int capacityFlickerLure;
    private int capacityOtherworldAura;
    private int capacityCalefactor;
    private int capacitySeerStone;
    private int capacitySlipstreamGenerator;
    /** End Power System **/

    /** Spell Balance **/
    private int zoneDefaultRadius;
    private int zoneDefaultDuration;
    private int wallDefaultRadius;
    private int wallDefaultDuration;
    private int puddleDefaultRadius;
    private int puddleDefaultDuration;
    private float puddleDefaultRange;
    private int waveDefaultRadius;
    private double waveSpeedMultiplier;
    private int waveDefaultDuration;
    private double waveGravityPerModifier;
    private int coneAngle;
    private float speedModifierValue;
    private float healingModifierValue;
    private float rangeModifierValue;
    private float durationModifierValue;
    private float gravityModifierValue;
    private int piercingModifierValue;
    private float solarDamageBase;
    private float solarDurationBase;
    private float solarHealingBase;
    private float solarRangeBase;
    private float lunarDamageBase;
    private float lunarDurationBase;
    private float lunarHealingBase;
    private float lunarRangeBase;
    private int defaultBuffDuration;
    private int buffPowerDurationBonus;
    private int healCooldown;
    private int maxStageGroups;
    private int maxRecipeSize;
    /** End Spell Balance **/

    /** Affinity Ability Thresholds & Effects **/
    private float affinityAgileMinDepth;
    private float affinityAgileJumpBoost;
    private float affinityColdBloodedMinDepth;
    private float affinityThornsMinDepth;
    private int affinityThornsDamage1;
    private int affinityThornsDamage2;
    private int affinityThornsDamage3;
    private float affinityFulminationMinDepth;
    private float affinityFulminationLightningChance;
    /** End Affinity Ability Thresholds **/

    /** Burnout Formula **/
    private float manaBurnoutRatio;
    private int burnoutPerLevel;
    private int burnoutBase;
    private int affinityHealCooldownFull;
    private int affinityHealCooldownPartial;
    /** End Burnout Formula **/

    /** Flicker Generation Weights **/
    private int flickerWeightAir;
    private int flickerWeightArcane;
    private int flickerWeightEarth;
    private int flickerWeightEnder;
    private int flickerWeightFire;
    private int flickerWeightIce;
    private int flickerWeightLife;
    private int flickerWeightLightning;
    private int flickerWeightNature;
    private int flickerWeightWater;
    /** End Flicker Generation Weights **/

    /** Beam Shape Tick Rates **/
    private int beamBlockTickRate;
    private int beamEntityTickRate;
    private int channelTickRate;
    /** End Beam Shape Tick Rates **/

    /** Processing Block Costs **/
    private float reconstructorRepairCost;
    private int deconstructorTime;
    private float deconstructorPowerCost;
    /** End Processing Block Costs **/

    public AMConfig(File file) {
        super(file);
        this.load();
        this.addCustomCategoryComment(CATEGORY_BETA, "This applies to those who have beta auras unlocked only");
        this.addCustomCategoryComment(CATEGORY_MOBS, "Spawn control and base stats for different AM mobs.");
        this.addCustomCategoryComment(CATEGORY_WORLDGEN, "Controls ore, plant, and structure generation in the world.");
        this.addCustomCategoryComment(CATEGORY_INTEGRATION, "Settings for compatibility and integration with other mods.");
        this.addCustomCategoryComment(CATEGORY_SPELL_DAMAGE, "Base damage values for spell damage components. Additive components use these as the starting damage; multiplicative components use these as the base multiplier.");
        this.addCustomCategoryComment(CATEGORY_BOSSES, "Stats and balance settings for AM2 boss guardians. Set max_health to 0 to use the hardcoded default.");
        this.addCustomCategoryComment(CATEGORY_POWER, "Power system parameters: search radii, type multipliers, and astral barrier costs.");
        this.addCustomCategoryComment(CATEGORY_SPELL_BALANCE, "Spell shape defaults, modifier values, buff durations, and inscription table limits. Tweak these to rebalance spell mechanics.");
        this.addCustomCategoryComment(CATEGORY_AFFINITY, "Affinity ability activation thresholds and effect magnitudes. Adjust these to rebalance passive affinity bonuses.");
        this.addCustomCategoryComment(CATEGORY_FLICKER, "Spawn weights for flicker affinities in the Flicker Lure. Higher weight = more common.");
        this.getCategory(CATEGORY_UI).setShowInGui(false);
    }

    @SuppressWarnings("unchecked")
    public void init() {

        this.PlayerSpellsDamageTerrain = this.get(CATEGORY_GENERAL, this.KEY_PlayerSpellsDamageTerrain, true).getBoolean(true);
        this.NPCSpellsDamageTerrain = this.get(CATEGORY_GENERAL, this.KEY_NpcSpellsDamageTerrain, false).getBoolean(false);

        this.DamageMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_DamageMultiplier, 1.0, "How much the damage in Ars Magica is scaled.").getDouble(1.0);
        this.IsImbueEnabled = this.get(CATEGORY_GENERAL, this.KEY_ImbueEnabled, true, "Enable or disable the armor imbuement enchant system entirely.").getBoolean(true);

        this.UseSpecialRenderers = this.get(CATEGORY_GENERAL, this.KEY_UseSpecialRenderers, true, "Render spell effects on equipped scrolls rather than the scroll itself (only applies to the in-game one, the one on your hotbar remains unchanged)").getBoolean(true);

        boolean def = !Loader.isModLoaded("NotEnoughItems");
        this.DisplayManaInInventory = this.get(CATEGORY_GENERAL, this.KEY_DisplayManaInInventory, def, "This will toggle mana display on and off in your inventory.  Default 'O' key in game.").getBoolean(def);

        this.FrictionCoefficient = (float) this.get(CATEGORY_GENERAL, this.KEY_FrictionCoefficient, 0.8, "This is the multiplier used to determine velocity lost when a spell projectile bounces. 0.0 is a complete stop, 1.0 is no loss.").getDouble(0.8);
        this.JumpBoostFactor = (float) this.get(CATEGORY_GENERAL, this.KEY_JumpBoostFactor, 1.0, "How much the jump height is increased by the Jump Boost armor imbuement, and per level of Leap. Default: 1.0").getDouble(1.0);

        Property retroWorldGenProp = this.get(CATEGORY_WORLDGEN, this.KEY_RetroactiveWorldGen, false, "Set this to true to enable retroactive worldgen for Ars Magica structures and ores.  *WARNING* This may break your save!  Do a backup first!  Note: This will automatically turn off after running the game once.");
        this.RetroWorldGen = retroWorldGenProp.getBoolean(false);

        if (this.RetroWorldGen) {
            retroWorldGenProp.set(false);
        }

        this.secondarySkillTreeTierCap = this.get(CATEGORY_GENERAL, this.KEY_SecondarySkillTreeTierCap, 99, "Sets how far a player may progress into secondary skill trees.").getInt();
        this.magicLevelCap = this.get(CATEGORY_GENERAL, this.KEY_MagicLevelCap, 99, "Maximum magic level a player can reach. Milestone advancements scale proportionally to this value.").getInt();
        this.mageVillagerProfessionID = this.get(CATEGORY_GENERAL, this.KEY_MageVillagerProfessionID, 29).getInt();

        this.manaHudPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_ManaHudPositionX, 0.7104166746139526).getDouble(0.7104166746139526), this.get(CATEGORY_UI, this.KEY_ManaHudPositionY, 0.9137254953384399).getDouble(0.9137254953384399));
        this.burnoutHudPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_BurnoutHudPositionX, 0.13333334028720856).getDouble(0.13333334028720856), this.get(CATEGORY_UI, this.KEY_BurnoutHudPositionY, 0.9176470637321472).getDouble(0.9176470637321472));
        this.positiveBuffsPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_BuffsPositivePositionX, 0.5145833492279053).getDouble(0.5145833492279053), this.get(CATEGORY_UI, this.KEY_BuffsPositivePositionY, 0.47843137383461).getDouble(0.47843137383461));
        this.negativeBuffsPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_BuffsNegativePositionX, 0.46666666865348816).getDouble(0.46666666865348816), this.get(CATEGORY_UI, this.KEY_BuffsNegativePositionY, 0.47843137383461).getDouble(0.47843137383461));
        this.levelPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_LevelPositionX, 0.49791666865348816).getDouble(0.49791666865348816), this.get(CATEGORY_UI, this.KEY_LevelPositionY, 0.8117647171020508).getDouble(0.8117647171020508));
        this.affinityPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_AffinityPositionX, 0.9770833253860474).getDouble(0.9770833253860474), this.get(CATEGORY_UI, this.KEY_AffinityPositionY, 0.9).getDouble(0.9));
        this.armorPositionChest = new AMVector2(this.get(CATEGORY_UI, this.KEY_ArmorPositionChestX, 0.004166666883975267).getDouble(0.004166666883975267), this.get(CATEGORY_UI, this.KEY_ArmorPositionChestY, 0.5568627715110779).getDouble(0.5568627715110779));
        this.armorPositionHead = new AMVector2(this.get(CATEGORY_UI, this.KEY_ArmorPositionHeadX, 0.004166666883975267).getDouble(0.004166666883975267), this.get(CATEGORY_UI, this.KEY_ArmorPositionHeadY, 0.5176470875740051).getDouble(0.5176470875740051));
        this.armorPositionLegs = new AMVector2(this.get(CATEGORY_UI, this.KEY_ArmorPositionLegsX, 0.004166666883975267).getDouble(0.004166666883975267), this.get(CATEGORY_UI, this.KEY_ArmorPositionLegsY, 0.5960784554481506).getDouble(0.5960784554481506));
        this.armorPositionBoots = new AMVector2(this.get(CATEGORY_UI, this.KEY_ArmorPositionBootsX, 0.004166666883975267).getDouble(0.004166666883975267), this.get(CATEGORY_UI, this.KEY_ArmorPositionBootsY, 0.6352941393852234).getDouble(0.6352941393852234));
        this.xpBarPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_XPBarPositionX, 0.31041666865348816).getDouble(0.31041666865348816), this.get(CATEGORY_UI, this.KEY_XPBarPositionY, 0.7843137383460999).getDouble(0.7843137383460999));
        this.contingencyPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_ContingencyPositionX, 0.0020833334419876337).getDouble(0.0020833334419876337), this.get(CATEGORY_UI, this.KEY_ContingencyPositionY, 0.9333333373069763).getDouble(0.9333333373069763));

        this.manaNumericPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_ManaNumericPositionX, 0.7437499761581421).getDouble(0.7437499761581421), this.get(CATEGORY_UI, this.KEY_ManaNumericPositionY, 0.8941176533699036).getDouble(0.8941176533699036));
        this.burnoutNumericPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_BurnoutNumericPositionX, 0.21041665971279144).getDouble(0.21041665971279144), this.get(CATEGORY_UI, this.KEY_BurnoutNumericPositionY, 0.9058823585510254).getDouble(0.9058823585510254));
        this.manaBarWidth = this.get(CATEGORY_UI, this.KEY_ManaBarWidth, 100).getInt();
        this.manaBarHeight = this.get(CATEGORY_UI, this.KEY_ManaBarHeight, 10).getInt();
        this.burnoutBarWidth = this.get(CATEGORY_UI, this.KEY_BurnoutBarWidth, 100).getInt();
        this.burnoutBarHeight = this.get(CATEGORY_UI, this.KEY_BurnoutBarHeight, 10).getInt();
        this.XPNumericPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_XPNumericPositionX, 0.47083333134651184).getDouble(0.47083333134651184), this.get(CATEGORY_UI, this.KEY_XPNumericPositionY, 0.7450980544090271).getDouble(0.7450980544090271));
        this.SpellBookPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_SpellBookPositionX, 0.0).getDouble(0.0), this.get(CATEGORY_UI, this.KEY_SpellBookPositionY, 0.0).getDouble(0.0));
        this.manaShieldingPosition = new AMVector2(this.get(CATEGORY_UI, this.KEY_ManaShieldingPositionX, 0.7104166746139526).getDouble(0.7104166746139526), this.get(CATEGORY_UI, this.KEY_ManaShieldingPositionY, 0.9352226853370667).getDouble(0.9352226853370667));
        this.showHudMinimally = this.get(CATEGORY_UI, this.KEY_ShowHudMinimally, false, "Set this to true to only show the AM HUD when a spell is equipped").getBoolean(false);
        this.showArmorUI = this.get(CATEGORY_UI, this.KEY_ShowArmorUI, true).getBoolean(true);
        this.showBuffs = this.get(CATEGORY_UI, this.KEY_ShowBuffs, true).getBoolean(true);
        this.showNumerics = this.get(CATEGORY_UI, this.KEY_ShowNumerics, false).getBoolean(false);
        this.showXPAlways = this.get(CATEGORY_UI, this.KEY_ShowXPAlways, false).getBoolean(false);
        this.showHudBars = this.get(CATEGORY_UI, this.KEY_ShowHUDBars, true).getBoolean(true);

        this.witchwoodForestID = this.get(CATEGORY_WORLDGEN, this.KEY_WitchwoodForestBiomeID, -1, "Sets the biome ID that Witchwood Forest will use. Default: -1 (automatic))").getInt();
        this.witchwoodLeafParticles = this.get(CATEGORY_GENERAL, this.KEY_WitchwoodLeavesFall, true, "Disable this if you experience low FPS in witchwood forests").getBoolean(true);
        this.enableWitchwoodForest = this.get(CATEGORY_WORLDGEN, this.KEY_EnableWitchwoodForest, true, "Disable this if you prefer the witchwood forest to not generate").getBoolean(true);
        this.witchwoodForestRarity = this.get(CATEGORY_WORLDGEN, this.KEY_WitchwoodForestRarity, 6, "Sets how rare witchwood forests are.  Lower is more rare.").getInt();

        this.allowCreativeTargets = this.get(CATEGORY_GENERAL, this.KEY_AllowCreativeTargets, true, "Disable this to prevent spell effects on creative players").getBoolean(true);

        this.moonstoneMeteorsDestroyTerrain = this.get(CATEGORY_GENERAL, this.KEY_MoonstoneMeteorsDestroyTerrain, true, "Should moonstone meteors destroy terrain when landing?  Keep in mind they will never land on anything other than grass.").getBoolean(true);

        this.suggestSpellNames = this.get(CATEGORY_GENERAL, this.KEY_MoonstoneMeteorsDestroyTerrain, true, "Set this to true to allow AM2 to get random spell names from Seventh Sanctum, and suggest them when naming spells.  Naturally, an internet connection is required.  Keep in mind, while I try to keep things family friendly, it's possible that not all names generated are so.").getBoolean(true);

        this.staffDurability = this.get(CATEGORY_GENERAL, this.KEY_StaffDurability, 30, "Durability (number of casts) for the Magic Staff item. Default: 30").getInt();

        this.staffPresets = this.get(CATEGORY_GENERAL, this.KEY_StaffPresets,
                new String[] {
                        "0:arsmagica2:projectile:arsmagica2:fire_damage:Staff of Fireball",
                        "0:arsmagica2:projectile:arsmagica2:frost_damage:Staff of Frostbolt",
                        "0:arsmagica2:projectile:arsmagica2:lightning_damage:Staff of Lightning Bolt",
                        "0:arsmagica2:projectile:arsmagica2:magic_damage:Staff of Magic Missile",
                        "0:arsmagica2:projectile:arsmagica2:physical_damage:Staff of Force"
                },
                "Defines premade spell presets for the Magic Staff. "
                + "Format: tier:modid:part1:modid:part2:...:Display Name. "
                + "Registry names use full modid:name pairs. The last token (if unpaired) is the display name. "
                + "Example: 0:arsmagica2:projectile:arsmagica2:fire_damage:Fire Staff").getStringList();
        if (this.staffPresets == null) this.staffPresets = new String[0];

        this.forgeSmeltsVillagers = this.get(CATEGORY_GENERAL, this.KEY_ForgeSmeltsVillagers, true, "Set this to true to have the forge component smelt villagers into emeralds.  This counts as an attack and lowers your reputation.").getBoolean(true);

        this.everstoneRepairRate = this.get(CATEGORY_GENERAL, this.KEY_EverstoneRepairRate, 180).getInt();

        this.manaMartiniDuration = this.get(CATEGORY_GENERAL, this.KEY_ManaMartiniDuration, 6000, "Duration of the Mana Martini burnout reduction effect in ticks. Default: 6000 (5 minutes)").getInt();

        this.manaCakeRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_ManaCakeRegenDuration, 1000, "Duration of Mana Cake's mana regeneration effect in ticks. Default: 1000 (50 seconds)").getInt();
        this.manaCakeFoodAmount = this.get(CATEGORY_GENERAL, this.KEY_ManaCakeFoodAmount, 4, "Food points (half-shanks) restored by eating a Mana Cake. Default: 4").getInt();

        this.lesserManaPotionMana = (float) this.get(CATEGORY_GENERAL, this.KEY_LesserManaPotionMana, 100.0, "Mana instantly restored by the Lesser Mana Potion. Default: 100").getDouble();
        this.lesserManaPotionRegenLevel = this.get(CATEGORY_GENERAL, this.KEY_LesserManaPotionRegenLevel, 0, "Mana Regeneration amplifier for the Lesser Mana Potion (0 = level I). Default: 0").getInt();
        this.lesserManaPotionRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_LesserManaPotionRegenDuration, 600, "Mana Regeneration duration in ticks for the Lesser Mana Potion. Default: 600 (30s)").getInt();

        this.standardManaPotionMana = (float) this.get(CATEGORY_GENERAL, this.KEY_StandardManaPotionMana, 250.0, "Mana instantly restored by the Standard Mana Potion. Default: 250").getDouble();
        this.standardManaPotionRegenLevel = this.get(CATEGORY_GENERAL, this.KEY_StandardManaPotionRegenLevel, 0, "Mana Regeneration amplifier for the Standard Mana Potion (0 = level I). Default: 0").getInt();
        this.standardManaPotionRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_StandardManaPotionRegenDuration, 1200, "Mana Regeneration duration in ticks for the Standard Mana Potion. Default: 1200 (60s)").getInt();

        this.greaterManaPotionMana = (float) this.get(CATEGORY_GENERAL, this.KEY_GreaterManaPotionMana, 2000.0, "Mana instantly restored by the Greater Mana Potion. Default: 2000").getDouble();
        this.greaterManaPotionRegenLevel = this.get(CATEGORY_GENERAL, this.KEY_GreaterManaPotionRegenLevel, 1, "Mana Regeneration amplifier for the Greater Mana Potion (0 = level I). Default: 1").getInt();
        this.greaterManaPotionRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_GreaterManaPotionRegenDuration, 1800, "Mana Regeneration duration in ticks for the Greater Mana Potion. Default: 1800 (90s)").getInt();

        this.epicManaPotionMana = (float) this.get(CATEGORY_GENERAL, this.KEY_EpicManaPotionMana, 5000.0, "Mana instantly restored by the Epic Mana Potion. Default: 5000").getDouble();
        this.epicManaPotionRegenLevel = this.get(CATEGORY_GENERAL, this.KEY_EpicManaPotionRegenLevel, 1, "Mana Regeneration amplifier for the Epic Mana Potion (0 = level I). Default: 1").getInt();
        this.epicManaPotionRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_EpicManaPotionRegenDuration, 2400, "Mana Regeneration duration in ticks for the Epic Mana Potion. Default: 2400 (120s)").getInt();

        this.legendaryManaPotionMana = (float) this.get(CATEGORY_GENERAL, this.KEY_LegendaryManaPotionMana, 10000.0, "Mana instantly restored by the Legendary Mana Potion. Default: 10000").getDouble();
        this.legendaryManaPotionRegenLevel = this.get(CATEGORY_GENERAL, this.KEY_LegendaryManaPotionRegenLevel, 2, "Mana Regeneration amplifier for the Legendary Mana Potion (0 = level I). Default: 2").getInt();
        this.legendaryManaPotionRegenDuration = this.get(CATEGORY_GENERAL, this.KEY_LegendaryManaPotionRegenDuration, 3000, "Mana Regeneration duration in ticks for the Legendary Mana Potion. Default: 3000 (150s)").getInt();

        this.winterArmFlightTicks = this.get(CATEGORY_GENERAL, this.KEY_WinterArmFlightTicks, 60, "How many ticks the Winter Guardian Arm flies before returning. Lower = shorter range. Default: 60 (3 seconds)").getInt();

        this.lifeWardEnabled = this.get(CATEGORY_GENERAL, this.KEY_LifeWardEnabled, true, "Set this to false to disable the Life Ward item's absorption effect. Note that this makes the item essentially useless.").getBoolean(true);
        this.lifeWardTickInterval = this.get(CATEGORY_GENERAL, this.KEY_LifeWardTickInterval, 80, "How often (in ticks) the Life Ward grants absorption. Default: 80 (4 seconds)").getInt();
        this.lifeWardMaxAbsorption = this.get(CATEGORY_GENERAL, this.KEY_LifeWardMaxAbsorption, 20, "Maximum absorption hearts the Life Ward can grant. Default: 20 (=10 hearts)").getInt();
        this.lifeWardAbsorptionPerTick = (float) this.get(CATEGORY_GENERAL, this.KEY_LifeWardAbsorptionPerTick, 1.0, "Amount of absorption added each tick interval. Default: 1.0").getDouble();

        this.stagedCompendium = this.get(CATEGORY_GENERAL, this.KEY_StagedCompendium, true, "Set this to false to have the compendium show everything, and not unlock as you go.").getBoolean(true);

        this.colourblindMode = this.get(CATEGORY_GENERAL, this.KEY_ColourblindMode, false, "Set this to true to have AM2 list out colours for skill points and essence types rather than showing them as a colour.").getBoolean(false);

        this.candlesAreRovingLights = this.get(CATEGORY_GENERAL, this.KEY_CandlesAreRovingLights, true, "Set this to false to disable candles being able to act as roving lights, which improves performance.").getBoolean(true);

        this.coloredSpellBookNames = this.get(CATEGORY_GENERAL, this.KEY_ColoredSpellBookNames, true, "Set this to true to color spell book item names based on the book's cover color.").getBoolean(true);

        this.meteorMinSpawnLevel = this.get(CATEGORY_GENERAL, this.KEY_MeteorMinSpawnLevel, 10, "You must reach this magic level before Moonstone meteors will fall near you.").getInt();
        this.meteorSpawnBaseChance = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnBaseChance, 2500, "Base spawn chance for meteors. Higher values make meteors more rare. Default: 2500").getInt();
        this.meteorSpawnMoonPhaseMultiplier = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnMoonPhaseMultiplier, 1000, "Moon phase multiplier for meteor spawn chance. This value is multiplied by the current moon phase (0-7). Default: 1000").getInt();
        this.meteorSpawnCooldownMin = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnCooldownMin, 12000, "Minimum cooldown in ticks between meteor spawns. Default: 12000 (10 minutes)").getInt();
        this.meteorSpawnCooldownMax = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnCooldownMax, 48000, "Maximum cooldown in ticks between meteor spawns. Default: 48000 (40 minutes)").getInt();
        this.meteorSpawnRadiusPlayer = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnRadiusPlayer, 64, "Radius in blocks around the player where meteors can spawn. Default: 64").getInt();
        this.meteorSpawnRadiusAttractor = this.get(CATEGORY_GENERAL, this.KEY_MeteorSpawnRadiusAttractor, 4, "Radius in blocks around a moonstone attractor where meteors can spawn. Default: 4").getInt();

        this.hazardousGateways = this.get(CATEGORY_GENERAL, this.KEY_HazardousGateways, true, "Set this to false in order to disable gateways sending you partial distances if you don't have enough power.").getBoolean(true);

        this.maxOrbsPerPlayer = this.get(CATEGORY_GENERAL, this.KEY_MaxOrbsPerPlayer, 6, "Maximum number of spell orbs that can orbit a single player at the same time. Default: 6").getInt();

        this.ArmorXPInfusionFactor = this.get(CATEGORY_GENERAL, this.KEY_ArmorXpInfusionFactor, 1.0, "Alter this to change the rate at which armor XP infuses.").getDouble();
        this.armorInfusionCostMultiplier = this.get(CATEGORY_GENERAL, this.KEY_ArmorInfusionCostMultiplier, 5.0, "Multiplier for armor self-repair mana cost. Higher values make armor repair cost more mana.").getDouble();

        this.mageSetDurationMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_MageSetDurationMultiplier, 1.25, "Spell duration multiplier when wearing a full Mage armor set.").getDouble(1.25);
        this.battlemageSetDurationMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_BattlemageSetDurationMultiplier, 1.1, "Spell duration multiplier when wearing a full Battlemage armor set.").getDouble(1.1);
        this.archmageSetDurationMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_ArchmageSetDurationMultiplier, 2.0, "Spell duration multiplier when wearing a full Archmage armor set.").getDouble(2.0);
        this.mageSetDamageMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_MageSetDamageMultiplier, 1.05, "Spell damage multiplier when wearing a full Mage armor set.").getDouble(1.05);
        this.battlemageSetDamageMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_BattlemageSetDamageMultiplier, 1.025, "Spell damage multiplier when wearing a full Battlemage armor set.").getDouble(1.025);
        this.archmageSetDamageMultiplier = (float) this.get(CATEGORY_GENERAL, this.KEY_ArchmageSetDamageMultiplier, 1.1, "Spell damage multiplier when wearing a full Archmage armor set.").getDouble(1.1);
        this.disarmAffectsPlayers = this.get(CATEGORY_GENERAL, this.KEY_DisarmAffectsPlayers, true, "If false, disarm won't work on players.").getBoolean(true);
        this.manaCap = this.get(CATEGORY_GENERAL, this.KEY_ManaCap, 0, "Sets the maximum mana a player can have (0 for no cap)").getDouble(0);
        this.baseMana = this.get(CATEGORY_GENERAL, this.KEY_BaseMana, 75.0, "The base amount of mana a player starts with at level 1. Additional mana is gained from leveling up.").getDouble(75.0);

        this.digBreaksTileEntities = this.get(CATEGORY_GENERAL, this.KEY_DigBreaksTEs, true, "Can the dig component break blocks that have a tile entity?").getBoolean(true);

        this.savePowerOnWorldSave = this.get(CATEGORY_GENERAL, this.KEY_SavePowerOnWorldSave, true, "Set this to false if you are experiencing tick lage due to AM2 saving power data alongside the world save.  This will instead cache the power data in memory to be saved later.  This comes with more risk in the event of a crash, and a larger memory footprint, but increased performance. Can be used alongside chunk unload save config. Power data is still always saved at world unload (server shutdown).").getBoolean(true);

        this.canDryadsDespawn = this.get(CATEGORY_MOBS, this.KEY_CanDryadsDespawn, true, "Set this to false if you don't want dryads to despawn.").getBoolean(true);

        // Mob Stats
        this.fireElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_FireElementalMaxHealth, 30.0, "Fire Elemental max HP. Default: 30").getDouble(30.0);
        this.fireElementalAttackDamage = this.get(CATEGORY_MOBS, this.KEY_FireElementalAttackDamage, 5.0, "Fire Elemental attack damage. Default: 5").getDouble(5.0);
        this.earthElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_EarthElementalMaxHealth, 25.0, "Earth Elemental max HP. Default: 25").getDouble(25.0);
        this.earthElementalAttackDamage = this.get(CATEGORY_MOBS, this.KEY_EarthElementalAttackDamage, 4.0, "Earth Elemental attack damage. Default: 4").getDouble(4.0);
        this.earthElementalVariantMaxHealth = this.get(CATEGORY_MOBS, this.KEY_EarthElementalVariantMaxHealth, 40.0, "Earth Elemental boss variant (stone golem) max HP. Default: 40").getDouble(40.0);
        this.earthElementalVariantAttackDamage = this.get(CATEGORY_MOBS, this.KEY_EarthElementalVariantAttackDamage, 6.0, "Earth Elemental boss variant (stone golem) attack damage. Default: 6").getDouble(6.0);
        this.iceElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_IceElementalMaxHealth, 25.0, "Ice Elemental max HP. Default: 25").getDouble(25.0);
        this.iceElementalAttackDamage = this.get(CATEGORY_MOBS, this.KEY_IceElementalAttackDamage, 4.0, "Ice Elemental attack damage. Default: 4").getDouble(4.0);
        this.lightningElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_LightningElementalMaxHealth, 25.0, "Lightning Elemental max HP. Default: 25").getDouble(25.0);
        this.lightningElementalAttackDamage = this.get(CATEGORY_MOBS, this.KEY_LightningElementalAttackDamage, 4.0, "Lightning Elemental attack damage. Default: 4").getDouble(4.0);
        this.waterElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_WaterElementalMaxHealth, 20.0, "Water Elemental max HP. Default: 20").getDouble(20.0);
        this.manaElementalMaxHealth = this.get(CATEGORY_MOBS, this.KEY_ManaElementalMaxHealth, 20.0, "Mana Elemental max HP. Default: 20").getDouble(20.0);
        this.disableManaElementalParticles = this.get(CATEGORY_MOBS, this.KEY_DisableManaElementalParticles, false, "Set this to true to disable the particle effects on Mana Elementals, which may improve performance.").getBoolean(false);
        this.darkMageMaxHealth = this.get(CATEGORY_MOBS, this.KEY_DarkMageMaxHealth, 20.0, "Dark Mage max HP. Default: 20").getDouble(20.0);
        this.lightMageMaxHealth = this.get(CATEGORY_MOBS, this.KEY_LightMageMaxHealth, 22.0, "Light Mage max HP. Default: 22").getDouble(22.0);
        this.hecateMaxHealth = this.get(CATEGORY_MOBS, this.KEY_HecateMaxHealth, 12.0, "Hecate max HP. Default: 12").getDouble(12.0);
        this.hecateAttackDamage = this.get(CATEGORY_MOBS, this.KEY_HecateAttackDamage, 5.0, "Hecate attack damage. Default: 5").getDouble(5.0);
        this.hellCowMaxHealth = this.get(CATEGORY_MOBS, this.KEY_HellCowMaxHealth, 50.0, "Hell Cow max HP. Default: 50").getDouble(50.0);
        this.hellCowAttackDamage = this.get(CATEGORY_MOBS, this.KEY_HellCowAttackDamage, 10.0, "Hell Cow attack damage. Default: 10").getDouble(10.0);
        this.darklingMaxHealth = this.get(CATEGORY_MOBS, this.KEY_DarklingMaxHealth, 7.0, "Darkling max HP. Default: 7").getDouble(7.0);
        this.manaCreeperMaxHealth = this.get(CATEGORY_MOBS, this.KEY_ManaCreeperMaxHealth, 20.0, "Mana Creeper max HP. Default: 20").getDouble(20.0);
        this.dryadMaxHealth = this.get(CATEGORY_MOBS, this.KEY_DryadMaxHealth, 20.0, "Dryad max HP. Default: 20").getDouble(20.0);
        this.darkMageArmor = this.get(CATEGORY_MOBS, this.KEY_DarkMageArmor, 5.0, "Dark Mage armor value. Default: 5").getDouble(5.0);
        this.earthElementalArmor = this.get(CATEGORY_MOBS, this.KEY_EarthElementalArmor, 18.0, "Earth Elemental armor value. Default: 18").getDouble(18.0);
        this.hecateArmor = this.get(CATEGORY_MOBS, this.KEY_HecateArmor, 5.0, "Hecate armor value. Default: 5").getDouble(5.0);
        this.iceElementalArmor = this.get(CATEGORY_MOBS, this.KEY_IceElementalArmor, 12.0, "Ice Elemental armor value. Default: 12").getDouble(12.0);
        this.fireElementalArmor = this.get(CATEGORY_MOBS, this.KEY_FireElementalArmor, 5.0, "Fire Elemental armor value. Default: 5").getDouble(5.0);
        this.lightMageArmor = this.get(CATEGORY_MOBS, this.KEY_LightMageArmor, 1.0, "Light Mage armor value. Default: 1").getDouble(1.0);
        this.lightningElementalArmor = this.get(CATEGORY_MOBS, this.KEY_LightningElementalArmor, 8.0, "Lightning Elemental armor value. Default: 8").getDouble(8.0);
        this.manaElementalArmor = this.get(CATEGORY_MOBS, this.KEY_ManaElementalArmor, 12.0, "Mana Elemental armor value. Default: 12").getDouble(12.0);
        this.hellCowArmor = this.get(CATEGORY_MOBS, this.KEY_HellCowArmor, 15.0, "Hell Cow armor value. Default: 15").getDouble(15.0);

        this.enderAffinityAbilityCooldown = this.get(CATEGORY_GENERAL, this.KEY_EnderAffinityAbilityCooldown, 100, "Set this to the number of ticks between ender affinity teleports.").getInt();

        this.oldXpCalculations = this.get(CATEGORY_GENERAL, this.KEY_OldCalculations, true, "Enable Old XP calculations (before 1.5.0C-8)").setRequiresMcRestart(true).getBoolean(true);

        this.burnoutHungerDepletion = this.get(CATEGORY_GENERAL, this.KEY_BurnoutHungerDepletion, true, "If true, having burnout over 75%% will increase hunger depletion, scaling with burnout severity.").getBoolean(true);

        this.essenceLakeFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_EssenceLakeFrequency, 28, "How rare underground essence lakes are. Higher values = rarer. A value of N means roughly 1-in-N chance per chunk in magical/forest biomes. Default: 28.").getInt(28);
        this.tarmaRootFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_TarmaRootFrequency, 10, "How rare tarma root is. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 10.").getInt(10);
        this.witchwoodFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_WitchwoodFrequency, 105, "How rare witchwood trees are. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 105.").getInt(105);
        this.wakebloomFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_WakebloomFrequency, 3, "How rare wakebloom is in eligible biomes (beach, swamp, jungle, plains, water). Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 3.").getInt(3);
        this.ceruBlossomFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_CeruBlossomFrequency, 5, "How rare cerublossom is. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 5.").getInt(5);
        this.desertNovaFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_DesertNovaFrequency, 5, "How rare desert nova is in desert biomes. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 5.").getInt(5);
        this.essencePuddleFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_EssencePuddleFrequency, 67, "How rare surface essence puddles (2x2 pools) are. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 67.").getInt(67);
        this.blueTopazFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_BlueTopazFrequency, 5, "The number of blue topaz veins generated per chunk. Default: 5.").getInt(5);
        this.blueTopazVeinSize = this.get(CATEGORY_WORLDGEN, this.KEY_BlueTopazVeinSize, 5, "The number of blocks in a vein of blue topaz. Default: 5.").getInt(5);
        this.blueTopazMinHeight = this.get(CATEGORY_WORLDGEN, this.KEY_BlueTopazMinHeight, 10, "The minimum height for blue topaz to generate. Default: 10.").getInt(10);
        this.blueTopazMaxHeight = this.get(CATEGORY_WORLDGEN, this.KEY_BlueTopazMaxHeight, 80, "The maximum height for blue topaz to generate. Default: 80.").getInt(80);
        this.vinteumFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_VinteumFrequency, 6, "The number of vinteum veins generated per chunk. Default: 6.").getInt(6);
        this.vinteumVeinSize = this.get(CATEGORY_WORLDGEN, this.KEY_VinteumVeinSize, 4, "The number of blocks in a vein of vinteum. Default: 4.").getInt(4);
        this.vinteumMinHeight = this.get(CATEGORY_WORLDGEN, this.KEY_VinteumMinHeight, 10, "The minimum height for vinteum to generate. Default: 10.").getInt(10);
        this.vinteumMaxHeight = this.get(CATEGORY_WORLDGEN, this.KEY_VinteumMaxHeight, 45, "The maximum height for vinteum to generate. Default: 45.").getInt(45);
        this.chimeriteFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_ChimeriteFrequency, 8, "The number of chimerite veins generated per chunk. Default: 8.").getInt(8);
        this.chimeriteVeinSize = this.get(CATEGORY_WORLDGEN, this.KEY_ChimeriteVeinSize, 6, "The number of blocks in a vein of chimerite. Default: 6.").getInt(6);
        this.chimeriteMinHeight = this.get(CATEGORY_WORLDGEN, this.KEY_ChimeriteMinHeight, 10, "The minimum height for chimerite to generate. Default: 10.").getInt(10);
        this.chimeriteMaxHeight = this.get(CATEGORY_WORLDGEN, this.KEY_ChimeriteMaxHeight, 80, "The maximum height for chimerite to generate. Default: 80.").getInt(80);
        this.sunstoneFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_SunstoneFrequency, 20, "The number of sunstone veins generated per chunk (overworld and nether). Default: 20.").getInt(20);
        this.sunstoneVeinSize = this.get(CATEGORY_WORLDGEN, this.KEY_SunstoneVeinSize, 3, "The number of blocks in a vein of sunstone. Default: 3.").getInt(3);
        this.sunstoneMinHeight = this.get(CATEGORY_WORLDGEN, this.KEY_SunstoneMinHeight, 5, "The minimum height for sunstone to generate. Default: 5.").getInt(5);
        this.sunstoneMaxHeight = this.get(CATEGORY_WORLDGEN, this.KEY_SunstoneMaxHeight, 120, "The maximum height for sunstone to generate. Default: 120.").getInt(120);

        this.fairyRingMushroomBlocks = this.get(CATEGORY_WORLDGEN, this.KEY_FairyRingMushroomBlocks,
                new String[]{"minecraft:brown_mushroom:0", "minecraft:red_mushroom:0"},
                "Blocks that can be placed as mushrooms in fairy rings, as modid:registryname:meta entries.").getStringList();

        this.gatewayRuinEnabled = this.get(CATEGORY_WORLDGEN, this.KEY_GatewayRuinEnabled, true, "Enable or disable the spawning of ruined gateway structures in the world.").getBoolean(true);
        this.gatewayRuinFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_GatewayRuinFrequency, 400, "How rare ruined gateways are. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 400.").getInt(400);
        this.gatewayRuinBiomes = this.get(CATEGORY_WORLDGEN, this.KEY_GatewayRuinBiomes, new String[0], "Whitelist of BiomeDictionary type names (e.g. PLAINS, FOREST, DESERT) where gateway ruins can spawn. Empty = all biomes allowed.").getStringList();
        this.gatewayRuinBiomeBlacklist = this.get(CATEGORY_WORLDGEN, this.KEY_GatewayRuinBiomeBlacklist, new String[]{"NETHER", "END"}, "Blacklist of BiomeDictionary type names where gateway ruins will NOT spawn.").getStringList();
        this.gatewayRuinChest = this.get(CATEGORY_WORLDGEN, this.KEY_GatewayRuinChest, true, "If true, gateway ruins will generate with a loot chest. Set to false to disable the chest.").getBoolean(true);

        this.lostArchiveEnabled = this.get(CATEGORY_WORLDGEN, this.KEY_LostArchiveEnabled, true, "Enable or disable the spawning of Lost Archive structures underground.").getBoolean(true);
        this.lostArchiveFrequency = this.get(CATEGORY_WORLDGEN, this.KEY_LostArchiveFrequency, 300, "How rare Lost Archives are. Higher values = rarer. A value of N means a 1-in-N chance per chunk. Default: 300.").getInt(10);
        this.lostArchiveBiomeBlacklist = this.get(CATEGORY_WORLDGEN, this.KEY_LostArchiveBiomeBlacklist, new String[]{"NETHER", "END"}, "Blacklist of BiomeDictionary type names where Lost Archives will NOT spawn.").getStringList();

        this.ebwizSpellsInSpellBook = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizSpellsInSpellBook, true,
                "If true and Electroblob's Wizardry is installed, EBWiz spell books can be placed into AM2 spell book slots and cast using AM2 mana.").getBoolean(true);
        this.ebwizManaCostMultiplier = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizManaCostMultiplier, 10.0,
                "Multiplier applied to an EBWiz spell's base mana cost when paying with AM2 mana. Default: 3.0").getDouble(3.0);
        this.ebwizDisableWandMana = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDisableWandMana, false,
                "If true, EBWiz wand mana is always zeroed when a spell is cast – the wand never consumes its own mana charge. "
                + "AM2 mana is still deducted as normal when the caster has sufficient mana.").getBoolean(false);
        this.ebwizScrollRequiresMana = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizScrollRequiresMana, true,
                "If true (default), casting EBWiz spells from scrolls always requires AM2 mana; the cast is cancelled if the "
                + "caster does not have enough mana (and the scroll is not consumed). "
                + "Set to false to allow scrolls to be used freely without AM2 mana.").getBoolean(true);
        this.ebwizHideWandTooltip = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizHideWandTooltip, false,
                "If true, the Mana and Durability lines are removed from EBWiz wand tooltips. "
                + "Useful together with EBWiz_Disable_Wand_Mana to avoid showing misleading wand charge info.").getBoolean(false);
        this.ebwizMagicXPMultiplier = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizMagicXPMultiplier, 0.01,
                "Scales the AM2 magic XP granted per EBWiz spell cast. XP is computed as log(am2ManaCost) * this value. "
                + "Default: 0.01 (much lower than direct AM2 spells because EBWiz spells can be cast far more frequently).").getDouble(0.01);
        this.ebwizPreserveSpellBook = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizPreserveSpellBook, true,
                "If true, in EBWiz mode the inscription table shows a second slot for the writable book. "
                + "When the spell is created, the original EBWiz spell book in the first slot is preserved and "
                + "the writable book in the second slot becomes the spell recipe written book instead.").getBoolean(true);
        this.ebwizAffinityGainEnabled = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizAffinityGainEnabled, true,
                "If true, casting EBWiz spells grants AM2 affinity depth gains based on the spell's element.").getBoolean(true);
        this.ebwizAffinityGainAmount = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizAffinityGainAmount, 0.03,
                "Flat affinity depth gain per EBWiz spell cast (before weight normalisation). Default: 0.03").getDouble(0.03);
        String[] defaultElementMap = {
                "FIRE=fire:100",
                "ICE=ice:100",
                "LIGHTNING=lightning:100",
                "MAGIC=arcane:100",
                "EARTH=earth:80|water:20",
                "SORCERY=arcane:50|ender:50",
                "NECROMANCY=ender:50|ice:25|arcane:25",
                "HEALING=life:70|water:15|nature:15"
        };
        this.ebwizElementAffinityMap = parseWeightedAffinityMap(
                this.get(CATEGORY_INTEGRATION, this.KEY_EBWizElementAffinityMap, defaultElementMap,
                        "Maps EBWiz element names to weighted AM2 affinity gains. "
                        + "Format: ELEMENT=affinityName:weight|affinityName:weight\n"
                        + "Weights are relative and do not need to sum to 100.").getStringList());
        this.ebwizSpellAffinityOverrides = parseWeightedAffinityMap(
                this.get(CATEGORY_INTEGRATION, this.KEY_EBWizSpellAffinityOverrides, new String[0],
                        "Per-spell affinity-gain overrides (takes precedence over the element map). "
                        + "Format: ebwizardry:spell_registry_name=affinityName:weight|affinityName:weight"
                        + "\nExample: ebwizardry:blink=arcane:50|ender:50").getStringList());
        this.ebwizCondenserManaRegenEnabled = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizCondenserManaRegenEnabled, true,
                "If true, holding an EBWiz wand with a Condenser upgrade in either hand slightly increases AM2 mana regeneration rate.").getBoolean(true);
        this.ebwizCondenserManaRegenBonusPerLevel = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizCondenserManaRegenBonusPerLevel, 0.08,
                "AM2 mana regen speed bonus per Condenser upgrade level on the held wand (stacks additively). "
                + "0.08 means each level makes regen 8% faster; a fully upgraded wand (3 levels) gives 24% faster regen. "
                + "Valid range: 0.0–1.0.").getDouble(0.08);

        this.ebwizDiscoveryEnabled = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDiscoveryEnabled, true,
                "If true, players can discover new EBWiz spells at the Crafting Altar by placing a writable book " +
                "on the lectern and throwing a blank rune, an element item, a tier catalyst, and additional ingredients. " +
                "The altar picks a random undiscovered spell of the chosen element and tier.").getBoolean(true);
        this.ebwizDiscoveryElementItems = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDiscoveryElementItems,
                new String[] {
                    "minecraft:coal:1=1",      // charcoal  -> FIRE
                    "minecraft:snowball=2",     // snowball  -> ICE
                    "minecraft:redstone=3",     // redstone  -> LIGHTNING
                    "minecraft:bone=4",         // bone      -> NECROMANCY
                    "minecraft:stone:1=5",      // granite   -> EARTH
                    "arsmagica2:cerublossom=6", // cerublossom -> SORCERY
                    "arsmagica2:aum=7"          // aum       -> HEALING
                },
                "Maps items to EBWiz element ordinals for the Discovery Ritual. "
                + "Format: modid:item=ordinal or modid:item:meta=ordinal. "
                + "Element ordinals: 1=FIRE, 2=ICE, 3=LIGHTNING, 4=NECROMANCY, 5=EARTH, 6=SORCERY, 7=HEALING."
        ).getStringList();
        if (this.ebwizDiscoveryElementItems == null) this.ebwizDiscoveryElementItems = new String[0];
        this.ebwizDiscoveryTierCatalysts = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDiscoveryTierCatalysts,
                new String[] {
                    "ebwizardry:magic_crystal:0=0", // magic crystal -> NOVICE
                    "arsmagica2:lesser_focus=1",     // lesser focus  -> APPRENTICE
                    "arsmagica2:standard_focus=2",   // standard focus -> ADVANCED
                    "arsmagica2:greater_focus=3"     // greater focus -> MASTER
                },
                "Maps items to EBWiz tier ordinals for the Discovery Ritual. "
                + "Format: modid:item=ordinal or modid:item:meta=ordinal. "
                + "Tier ordinals: 0=NOVICE, 1=APPRENTICE, 2=ADVANCED, 3=MASTER."
        ).getStringList();
        if (this.ebwizDiscoveryTierCatalysts == null) this.ebwizDiscoveryTierCatalysts = new String[0];
        {
            int[] defaultCosts = {1000, 1500, 2000, 3000};
            int[] parsed = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDiscoveryPowerCosts,
                    defaultCosts,
                    "Neutral etherium costs for each Discovery Ritual tier, in order: Novice, Apprentice, Advanced, Master."
            ).getIntList();
            this.ebwizDiscoveryPowerCosts = (parsed != null && parsed.length == 4) ? parsed : defaultCosts;
        }
        this.ebwizTranscriptionEnabled = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizTranscriptionEnabled, true,
                "If true, EBWiz spell books can be transcribed into AM2 spell bindings at the Crafting Altar " +
                "by placing the book on the lectern and throwing a blank rune while the altar has enough neutral etherium.").getBoolean(true);
        this.ebwizTranscriptionCostBase = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizTranscriptionCostBase, 500,
                "Base neutral etherium cost for transcribing an EBWiz spell book at the Crafting Altar, " +
                "before the per-tier increment is added. Default: 500.").getInt(500);
        this.ebwizTranscriptionCostPerTier = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizTranscriptionCostPerTier, 500,
                "Additional neutral etherium cost multiplied by the spell's tier level (Novice=1, Apprentice=2, Advanced=3, Master=4). " +
                "Total cost = Base + (tierLevel * PerTier). " +
                "Defaults give: Novice 1000, Apprentice 1500, Advanced 2000, Master 2500.").getInt(500);
        this.ebwizDisciplineCostReductionPerLevel = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDisciplineCostReductionPerLevel, 0.5,
                "Percentage of EBWiz spell cost reduced per discipline level for the matching element. " +
                "At 0.5 and level 20, Fire spells cost 10% less mana. Set to 0 to disable. Default: 0.5.").getDouble(0.5);
        this.ebwizDisciplinePotencyBonusPerLevel = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizDisciplinePotencyBonusPerLevel, 0.3,
                "Percentage of EBWiz spell potency gained per discipline level for the matching element. " +
                "At 0.3 and level 20, Fire spells are 6% more potent. Set to 0 to disable. Default: 0.3.").getDouble(0.3);
        this.ebwizArtefactPotencyRatio = (float) this.get(CATEGORY_INTEGRATION, this.KEY_EBWizArtefactPotencyRatio, 0.5,
                "How much of the EBWiz artefact potency bonus (above 1.0) carries over as AM2 spell damage. " +
                "For example, ring_battlemage gives 1.1x EBWiz potency; with ratio 0.5 that becomes a 1.05x AM2 spell damage bonus. " +
                "Set to 0 to disable. Default: 0.5.").getDouble(0.5);
        this.ebwizEvilWizardOrbDrop = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizEvilWizardOrbDrop, true,
                "If true and Electroblob's Wizardry is loaded, evil wizards have a rare chance to drop a blue Infinity Orb.").getBoolean(true);
        this.ebwizElementalCrystalDrops = this.get(CATEGORY_INTEGRATION, this.KEY_EBWizElementalCrystalDrops, true,
                "If true and Electroblob's Wizardry is loaded, AM2 Earth Elementals have a chance to drop a Verdant Crystal "
                + "and Fire Elementals have a chance to drop a Fiery Crystal.").getBoolean(true);
        this.manaDrainRatio = this.get(CATEGORY_GENERAL, this.KEY_ManaDrainRatio, 1.0,
                "The conversion rate of mana to etherium in the Mana Drain. Default: 1.0 (1 mana = 1 neutral etherium)").getDouble(1.0);
        this.ebwizMageTomeDrop = this.get(CATEGORY_INTEGRATION, "EBWiz_Mage_Tome_Drop", true,
                "If true and Electroblob's Wizardry is loaded, AM2 Dark Mages and Light Mages have a 10% chance to drop an Arcane Tome.").getBoolean(true);
        this.magicResistExtraDamageTypes = new java.util.HashSet<>(java.util.Arrays.asList(
                this.get(CATEGORY_INTEGRATION, this.KEY_MagicResistExtraDamageTypes, new String[0],
                        "Additional damage-type strings (from DamageSource.getDamageType()) that the Magic Resist enchantment should protect against, "
                        + "beyond sources that already call setMagicDamage(). "
                        + "Use this to cover mods that add spell damage without flagging it as magic. "
                        + "Example entry: \"taint\" for Thaumcraft taint damage.").getStringList()));
        this.obeliskExtraFuels = this.get(CATEGORY_GENERAL, this.KEY_ObeliskExtraFuels,
                new String[] { "ebwizardry:magic_crystal=300" },
                "Additional items that can be used as obelisk fuel. "
                + "Format: modid:item=burntime or modid:item:meta=burntime. "
                + "Omitting :meta accepts all metadata values. Items from unloaded mods are silently skipped. "
                + "Example: ebwizardry:magic_crystal=300, minecraft:diamond=500, minecraft:dye:4=100").getStringList();
        if (this.obeliskExtraFuels == null) this.obeliskExtraFuels = new String[0];

        this.extraAltarCaps = this.get(CATEGORY_GENERAL, this.KEY_ExtraAltarCaps,
                new String[] {
                    "ebwizardry:crystal_block:0=4",
                    "ebwizardry:crystal_block:1=4",
                    "ebwizardry:crystal_block:2=4",
                    "ebwizardry:crystal_block:3=4",
                    "ebwizardry:crystal_block:4=4",
                    "ebwizardry:crystal_block:5=4",
                    "ebwizardry:crystal_block:6=4",
                    "ebwizardry:crystal_block:7=4"
                },
                "Additional blocks that can be used as crafting altar cap (pillar-top) materials. "
                + "Format: modid:block=tier or modid:block:meta=tier. "
                + "Meta defaults to 0 if omitted. Blocks from unloaded mods are silently skipped. "
                + "Example: ebwizardry:crystal_block:0=4, thaumcraft:amber_block=3").getStringList();
        if (this.extraAltarCaps == null) this.extraAltarCaps = new String[0];

        this.extraAltarMain = this.get(CATEGORY_GENERAL, this.KEY_ExtraAltarMain,
                new String[0],
                "Additional block+stairs pairs that can be used as crafting altar structure materials. "
                + "Format: modid:block:meta,modid:stairs:meta=tier. "
                + "Meta defaults to 0 if omitted. Blocks from unloaded mods are silently skipped. "
                + "Example: biomesoplenty:planks_0:0,biomesoplenty:sacred_oak_stairs:0=1").getStringList();
        if (this.extraAltarMain == null) this.extraAltarMain = new String[0];

        String digBlacklistString = this.get(CATEGORY_GENERAL, this.KEY_DigDisabledBlocks, "", "Comma-separated list of block IDs that dig cannot break.  If a block is flagged as unbreackable in code, Dig will already be unable to break it.  There is no need to set it here (eg, bedrock, etc.).  Dig also makes use of Forge block harvest checks.  This is mainly for fine-tuning.").getString();
        this.digBlacklist = digBlacklistString.split(",");

        String worldgenBlackList = this.get(CATEGORY_WORLDGEN, this.KEY_WorldgenBlacklist, "-27,-28,-29", "Comma-separated list of dimension IDs that AM should *not* do worldgen in.").getString();
        String[] split = worldgenBlackList.split(",");
        this.worldgenBlacklist = new int[split.length];
        int count = 0;
        for (String s : split) {
            if (s.equals("")) continue;
            try {
                this.worldgenBlacklist[count] = Integer.parseInt(s.trim());
            } catch (Throwable t) {
                ArsMagica.LOGGER.error("Malformed item in worldgen blacklist ({}).  Skipping.", s, t);
                this.worldgenBlacklist[count] = -1;
            } finally {
                count++;
            }
        }

        String apBlockBL = this.get(CATEGORY_GENERAL, this.KEY_AppropriationBlockBlacklist, "", "Comma-separated list of block IDs that appropriation cannot pick up.").getString();
        this.appropriationBlockBlacklist = apBlockBL.split(",");

        String apEntBL = this.get(CATEGORY_GENERAL, this.KEY_AppropriationMobBlacklist, "", "Comma-separated list of *fully qualified* Entity class names that appropriation cannot pick up - example, am2.entities.EntityDryad.  They are case sensitive.").getString();
        split = apEntBL.split(",");
        this.appropriationMobBlacklist = new Class[split.length];
        count = 0;
        for (String s : split) {
            if (s.equals("")) continue;
            try {
                this.appropriationMobBlacklist[count] = (Class<? extends Entity>) Class.forName(s);
            } catch (Throwable t) {
                ArsMagica.LOGGER.error("Malformed item in appropriation entity blacklist ({}).  Skipping.", s, t);
                this.appropriationMobBlacklist[count] = null;
            } finally {
                count++;
            }
        }

        this.crystallizeMaxHealth = this.get(CATEGORY_GENERAL, this.KEY_CrystallizeMaxHealth, 20, "Maximum max-health (in half-hearts) a creature can have to be crystallized. Default: 20").getInt(20);
        this.crystallizeHealthThreshold = (float) this.get(CATEGORY_GENERAL, this.KEY_CrystallizeHealthThreshold, 0.33, "The fraction of max HP the target must be at or below to be crystallized (0.0–1.0). Default: 0.33").getDouble(0.33);
        String[] crystBL = this.get(CATEGORY_GENERAL, this.KEY_CrystallizeMobBlacklist, new String[0], "List of entity registry names (e.g. minecraft:ender_dragon) that cannot be crystallized.").getStringList();
        this.crystallizeMobBlacklist = crystBL != null ? crystBL : new String[0];

        this.frostDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_FrostDamageBase, 10.0, "Base frost damage applied by the Frost Damage component (additive). Default: 10").getDouble(10.0);
        this.fireDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_FireDamageBase, 6.0, "Base fire damage applied by the Fire Damage component (additive). Default: 6").getDouble(6.0);
        this.lifeDrainBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_LifeDrainBase, 4.0, "Base magnitude of the Life Drain component (additive). Default: 4").getDouble(4.0);
        this.manaDrainBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_ManaDrainBase, 250.0, "Base mana stolen by the Mana Drain component (additive). Default: 250").getDouble(250.0);
        this.magicDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_MagicDamageBase, 6.0, "Base magic damage applied by the Magic Damage component (additive). Default: 6").getDouble(6.0);
        this.lightningDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_LightningDamageBase, 10.0, "Base lightning damage applied by the Lightning Damage component (additive). Default: 10").getDouble(10.0);
        this.drownDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_DrownDamageBase, 10.0, "Base damage applied by the Drown component (additive). Default: 10").getDouble(10.0);
        this.physicalDamageBase = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_PhysicalDamageBase, 8.0, "Base physical damage applied by the Physical Damage component (additive). Default: 8").getDouble(8.0);
        this.blizzardDamageMultiplier = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_BlizzardDamageMultiplier, 1.0, "Base damage multiplier for the Blizzard component (multiplicative). Default: 1").getDouble(1.0);
        this.lifeTapDamageMultiplier = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_LifeTapDamageMultiplier, 2.0, "Base damage multiplier for the Life Tap component (multiplicative). Default: 2").getDouble(2.0);
        this.fireRainDamageMultiplier = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_FireRainDamageMultiplier, 1.0, "Base damage multiplier for the Fire Rain component (multiplicative). Default: 1").getDouble(1.0);
        this.fallingStarDamageMultiplier = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_FallingStarDamageMultiplier, 15.0, "Base damage multiplier for the Falling Star component (multiplicative). Default: 15").getDouble(15.0);
        this.disarmDamageMultiplier = this.get(CATEGORY_SPELL_DAMAGE, this.KEY_DisarmDamageMultiplier, 1.0, "Base damage multiplier for the Disarm component (multiplicative). Default: 1").getDouble(1.0);

        // Boss Stats
        this.bossMaxDamagePerHit = (float) this.get(CATEGORY_BOSSES, this.KEY_BossMaxDamagePerHit, 7.0, "Maximum damage a single hit can deal to any AM2 boss. Set to 0 to disable the cap. Default: 7").getDouble(7.0);
        this.bossHurtResistantTime = this.get(CATEGORY_BOSSES, this.KEY_BossHurtResistantTime, 40, "Invulnerability ticks after a boss takes a hit. Lower = can be hit more frequently. Default: 40 (2 seconds)").getInt();
        this.fireGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_FireGuardianMaxHealth, 250.0, "Fire Guardian max HP. Default: 250").getDouble(250.0);
        this.fireGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_FireGuardianArmor, 17.0, "Fire Guardian armor value. Default: 17").getDouble(17.0);
        this.waterGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_WaterGuardianMaxHealth, 75.0, "Water Guardian max HP per clone. Default: 75").getDouble(75.0);
        this.waterGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_WaterGuardianArmor, 10.0, "Water Guardian armor value. Default: 10").getDouble(10.0);
        this.airGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_AirGuardianMaxHealth, 220.0, "Air Guardian max HP. Default: 220").getDouble(220.0);
        this.airGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_AirGuardianArmor, 14.0, "Air Guardian armor value. Default: 14").getDouble(14.0);
        this.earthGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_EarthGuardianMaxHealth, 140.0, "Earth Guardian max HP. Default: 140").getDouble(140.0);
        this.earthGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_EarthGuardianArmor, 23.0, "Earth Guardian armor value. Default: 23").getDouble(23.0);
        this.lightningGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_LightningGuardianMaxHealth, 250.0, "Lightning Guardian max HP. Default: 250").getDouble(250.0);
        this.lightningGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_LightningGuardianArmor, 18.0, "Lightning Guardian armor value. Default: 18").getDouble(18.0);
        this.lifeGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_LifeGuardianMaxHealth, 200.0, "Life Guardian max HP. Default: 200").getDouble(200.0);
        this.lifeGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_LifeGuardianArmor, 0.0, "Life Guardian armor value. Default: 0").getDouble(0.0);
        this.enderGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_EnderGuardianMaxHealth, 490.0, "Ender Guardian max HP. Default: 490").getDouble(490.0);
        this.enderGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_EnderGuardianArmor, 16.0, "Ender Guardian armor value. Default: 16").getDouble(16.0);
        this.arcaneGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_ArcaneGuardianMaxHealth, 115.0, "Arcane Guardian max HP. Default: 115").getDouble(115.0);
        this.arcaneGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_ArcaneGuardianArmor, 9.0, "Arcane Guardian armor value. Default: 9").getDouble(9.0);
        this.natureGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_NatureGuardianMaxHealth, 500.0, "Nature Guardian max HP. Default: 500").getDouble(500.0);
        this.natureGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_NatureGuardianArmor, 20.0, "Nature Guardian armor value. Default: 20").getDouble(20.0);
        this.winterGuardianMaxHealth = this.get(CATEGORY_BOSSES, this.KEY_WinterGuardianMaxHealth, 290.0, "Winter Guardian max HP. Default: 290").getDouble(290.0);
        this.winterGuardianArmor = this.get(CATEGORY_BOSSES, this.KEY_WinterGuardianArmor, 23.0, "Winter Guardian armor value. Default: 23").getDouble(23.0);

        // Mana & Progression
        this.baseTicksForFullRegen = this.get(CATEGORY_GENERAL, this.KEY_BaseTicksForFullRegen, 2400, "Ticks for full mana regeneration at level 1 with no bonuses. Lower = faster regen. Default: 2400 (120 seconds)").getInt();
        this.manaCoefficient = this.get(CATEGORY_GENERAL, this.KEY_ManaCoefficient, 85.0, "Coefficient in the max-mana formula: maxMana = (level^1.5) * (coefficient * level/100) + baseMana. Higher = more mana per level. Default: 85").getDouble(85.0);
        this.regenScalingBase = this.get(CATEGORY_GENERAL, this.KEY_RegenScalingBase, 0.75, "Base factor in the regen speed formula: regenTicks = baseTicks * (base - perLevel * level/levelCap). Default: 0.75").getDouble(0.75);
        this.regenScalingPerLevel = this.get(CATEGORY_GENERAL, this.KEY_RegenScalingPerLevel, 0.25, "Per-level scaling factor in the regen speed formula. Higher = more regen improvement at high levels. Default: 0.25").getDouble(0.25);
        this.xpRateMultiplier = this.get(CATEGORY_GENERAL, this.KEY_XPRateMultiplier, 1.0, "Multiplier for magic XP gain rate. Values above 1.0 make leveling faster, below 1.0 make it slower. Default: 1.0").getDouble(1.0);

        // Power System
        this.powerSearchRadius = this.get(CATEGORY_POWER, this.KEY_PowerSearchRadius, 10, "The search radius in blocks for finding nearby power nodes. Default: 10").getInt();
        this.maxPowerSearchRadius = this.get(CATEGORY_POWER, this.KEY_MaxPowerSearchRadius, 100, "The maximum distance in blocks that two power nodes can be linked. Default: 100").getInt();
        this.lightInputCostMultiplier = (float) this.get(CATEGORY_POWER, this.KEY_LightInputCostMultiplier, 1.25, "Input cost multiplier for Light etherium. Higher values mean power generators produce less effective etherium. Default: 1.25").getDouble(1.25);
        this.neutralInputCostMultiplier = (float) this.get(CATEGORY_POWER, this.KEY_NeutralInputCostMultiplier, 1.0, "Input cost multiplier for Neutral etherium. Default: 1.0").getDouble(1.0);
        this.darkInputCostMultiplier = (float) this.get(CATEGORY_POWER, this.KEY_DarkInputCostMultiplier, 0.75, "Input cost multiplier for Dark etherium. Default: 0.75").getDouble(0.75);
        this.astralBarrierCostPerRadius = (float) this.get(CATEGORY_POWER, this.KEY_AstralBarrierCostPerRadius, 0.35, "Etherium cost per tick per block of radius for the Astral Barrier. Default: 0.35").getDouble(0.35);
        this.astralBarrierDarkCost = (float) this.get(CATEGORY_POWER, this.KEY_AstralBarrierDarkCost, 50.0, "Dark etherium cost for the Astral Barrier to damage a blocked entity. Default: 50").getDouble(50.0);
        this.capacityManaBattery = this.get(CATEGORY_POWER, this.KEY_CapacityManaBattery, 250000, "Etherium storage capacity for the Mana Battery. Default: 250000").getInt();
        this.capacityBlackAurem = this.get(CATEGORY_POWER, this.KEY_CapacityBlackAurem, 10000, "Etherium storage capacity for the Black Aurem. Default: 10000").getInt();
        this.capacityObelisk = this.get(CATEGORY_POWER, this.KEY_CapacityObelisk, 5000, "Etherium storage capacity for the Obelisk. Default: 5000").getInt();
        this.capacityCelestialPrism = this.get(CATEGORY_POWER, this.KEY_CapacityCelestialPrism, 2500, "Etherium storage capacity for the Celestial Prism. Default: 2500").getInt();
        this.capacitySummoner = this.get(CATEGORY_POWER, this.KEY_CapacitySummoner, 2500, "Etherium storage capacity for the Summoner. Default: 2500").getInt();
        this.capacityEssenceRefiner = this.get(CATEGORY_POWER, this.KEY_CapacityEssenceRefiner, 1000, "Etherium storage capacity for the Essence Refiner. Default: 1000").getInt();
        this.capacityManaDrain = this.get(CATEGORY_POWER, this.KEY_CapacityManaDrain, 10000, "Etherium storage capacity for the Mana Drain. Default: 10000").getInt();
        this.capacityCraftingAltar = this.get(CATEGORY_POWER, this.KEY_CapacityCraftingAltar, 500, "Etherium storage capacity for the Crafting Altar. Default: 500").getInt();
        this.capacityArcaneReconstructor = this.get(CATEGORY_POWER, this.KEY_CapacityArcaneReconstructor, 500, "Etherium storage capacity for the Arcane Reconstructor. Default: 500").getInt();
        this.capacityArcaneDeconstructor = this.get(CATEGORY_POWER, this.KEY_CapacityArcaneDeconstructor, 500, "Etherium storage capacity for the Arcane Deconstructor. Default: 500").getInt();
        this.capacityInertSpawner = this.get(CATEGORY_POWER, this.KEY_CapacityInertSpawner, 500, "Etherium storage capacity for the Inert Spawner. Default: 500").getInt();
        this.capacityKeystoneReceptacle = this.get(CATEGORY_POWER, this.KEY_CapacityKeystoneReceptacle, 250000, "Etherium storage capacity for the Keystone Receptacle. Default: 250000").getInt();
        this.capacityAstralBarrier = this.get(CATEGORY_POWER, this.KEY_CapacityAstralBarrier, 250, "Etherium storage capacity for the Astral Barrier. Default: 250").getInt();
        this.capacityFlickerLure = this.get(CATEGORY_POWER, this.KEY_CapacityFlickerLure, 200, "Etherium storage capacity for the Flicker Lure. Default: 200").getInt();
        this.capacityOtherworldAura = this.get(CATEGORY_POWER, this.KEY_CapacityOtherworldAura, 200, "Etherium storage capacity for the Otherworld Aura. Default: 200").getInt();
        this.capacityCalefactor = this.get(CATEGORY_POWER, this.KEY_CapacityCalefactor, 100, "Etherium storage capacity for the Calefactor. Default: 100").getInt();
        this.capacitySeerStone = this.get(CATEGORY_POWER, this.KEY_CapacitySeerStone, 100, "Etherium storage capacity for the Seer Stone. Default: 100").getInt();
        this.capacitySlipstreamGenerator = this.get(CATEGORY_POWER, this.KEY_CapacitySlipstreamGenerator, 100, "Etherium storage capacity for the Slipstream Generator. Default: 100").getInt();

        // Apply power type multipliers
        PowerTypes.LIGHT.setInputCostMultiplier(this.lightInputCostMultiplier);
        PowerTypes.NEUTRAL.setInputCostMultiplier(this.neutralInputCostMultiplier);
        PowerTypes.DARK.setInputCostMultiplier(this.darkInputCostMultiplier);

        // Apply power search radii
        PowerNodeRegistry.applyConfig();

        // Spell Balance — Shape Defaults
        this.zoneDefaultRadius = this.get(CATEGORY_SPELL_BALANCE, this.KEY_ZoneDefaultRadius, 2, "Base radius of the Zone spell shape before modifiers. Default: 2").getInt();
        this.zoneDefaultDuration = this.get(CATEGORY_SPELL_BALANCE, this.KEY_ZoneDefaultDuration, 100, "Base duration (ticks) of the Zone spell shape before modifiers. Default: 100").getInt();
        this.wallDefaultRadius = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WallDefaultRadius, 3, "Base radius of the Wall spell shape before modifiers. Default: 3").getInt();
        this.wallDefaultDuration = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WallDefaultDuration, 100, "Base duration (ticks) of the Wall spell shape before modifiers. Default: 100").getInt();
        this.puddleDefaultRadius = this.get(CATEGORY_SPELL_BALANCE, this.KEY_PuddleDefaultRadius, 2, "Base radius of the Puddle spell shape before modifiers. Default: 2").getInt();
        this.puddleDefaultDuration = this.get(CATEGORY_SPELL_BALANCE, this.KEY_PuddleDefaultDuration, 180, "Base duration (ticks) of the Puddle spell shape before modifiers. Default: 180").getInt();
        this.puddleDefaultRange = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_PuddleDefaultRange, 10.0, "Maximum raytrace range for placing a Puddle when cast directly. Default: 10").getDouble(10.0);
        this.waveDefaultRadius = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WaveDefaultRadius, 1, "Base radius of the Wave spell shape before modifiers. Default: 1").getInt();
        this.waveSpeedMultiplier = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WaveSpeedMultiplier, 0.5, "Multiplier applied to the Wave's speed modifier value. Default: 0.5").getDouble(0.5);
        this.waveDefaultDuration = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WaveDefaultDuration, 20, "Base duration (ticks) of the Wave spell shape before modifiers. Default: 20").getInt();
        this.waveGravityPerModifier = this.get(CATEGORY_SPELL_BALANCE, this.KEY_WaveGravityPerModifier, 0.5, "Gravity added per gravity modifier on the Wave shape. Default: 0.5").getDouble(0.5);
        this.coneAngle = this.get(CATEGORY_SPELL_BALANCE, this.KEY_ConeAngle, 10, "Half-angle in degrees for the Cone spell shape (total width = angle * 2). Default: 10").getInt();

        // Spell Balance — Modifier Values
        this.speedModifierValue = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_SpeedModifierValue, 2.6, "Value returned by the Speed modifier per application. Default: 2.6").getDouble(2.6);
        this.healingModifierValue = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_HealingModifierValue, 2.0, "Value returned by the Healing modifier per application. Default: 2.0").getDouble(2.0);
        this.rangeModifierValue = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_RangeModifierValue, 4.0, "Value returned by the Range modifier per application. Default: 4.0").getDouble(4.0);
        this.durationModifierValue = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_DurationModifierValue, 2.2, "Value returned by the Duration modifier per application. Default: 2.2").getDouble(2.2);
        this.gravityModifierValue = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_GravityModifierValue, -0.06, "Value returned by the Gravity modifier per application (negative = upward). Default: -0.06").getDouble(-0.06);
        this.piercingModifierValue = this.get(CATEGORY_SPELL_BALANCE, this.KEY_PiercingModifierValue, 2, "Number of extra pierces granted per Piercing modifier. Default: 2").getInt();
        this.solarDamageBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_SolarDamageBase, 2.4, "Base damage value used by the Solar modifier's time-of-day formula. Default: 2.4").getDouble(2.4);
        this.solarDurationBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_SolarDurationBase, 5.0, "Base duration value used by the Solar modifier's time-of-day formula. Default: 5.0").getDouble(5.0);
        this.solarHealingBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_SolarHealingBase, 2.0, "Base healing value used by the Solar modifier's time-of-day formula. Default: 2.0").getDouble(2.0);
        this.solarRangeBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_SolarRangeBase, 3.0, "Base range/radius value used by the Solar modifier's inverse lunar cycle formula. Default: 3.0").getDouble(3.0);
        this.lunarDamageBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_LunarDamageBase, 2.4, "Base damage value used by the Lunar modifier's time-of-day formula. Default: 2.4").getDouble(2.4);
        this.lunarDurationBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_LunarDurationBase, 5.0, "Base duration value used by the Lunar modifier's time-of-day formula. Default: 5.0").getDouble(5.0);
        this.lunarHealingBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_LunarHealingBase, 2.0, "Base healing value used by the Lunar modifier's time-of-day formula. Default: 2.0").getDouble(2.0);
        this.lunarRangeBase = (float) this.get(CATEGORY_SPELL_BALANCE, this.KEY_LunarRangeBase, 3.0, "Base range/radius value used by the Lunar modifier's lunar cycle formula. Default: 3.0").getDouble(3.0);

        // Spell Balance — Buff Durations
        this.defaultBuffDuration = this.get(CATEGORY_SPELL_BALANCE, this.KEY_DefaultBuffDuration, 600, "Base duration (ticks) for buff spell components before modifiers. Default: 600 (30 seconds)").getInt();
        this.buffPowerDurationBonus = this.get(CATEGORY_SPELL_BALANCE, this.KEY_BuffPowerDurationBonus, 3600, "Duration bonus (ticks) per Buff Power modifier level when a ritual is active. Default: 3600 (3 minutes)").getInt();
        this.healCooldown = this.get(CATEGORY_SPELL_BALANCE, this.KEY_HealCooldown, 60, "Cooldown (ticks) between non-channeled heal applications on the same target. Default: 60 (3 seconds)").getInt();

        // Spell Balance — Inscription Table
        this.maxStageGroups = this.get(CATEGORY_SPELL_BALANCE, this.KEY_MaxStageGroups, 5, "Maximum number of shape groups (spell stages) on the Inscription Table. Default: 5").getInt();
        this.maxRecipeSize = this.get(CATEGORY_SPELL_BALANCE, this.KEY_MaxRecipeSize, 16, "Maximum number of spell parts in a single spell recipe. Default: 16").getInt();

        // Skill Point Progression
        this.skillPointBlueMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointBlueMinLevel, 0, "Minimum magic level to start earning Blue skill points. Default: 0").getInt();
        this.skillPointBlueLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointBlueLevelsPerPoint, 1, "Levels per Blue skill point. Default: 1").getInt();
        this.skillPointGreenMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointGreenMinLevel, 20, "Minimum magic level to start earning Green skill points. Default: 20").getInt();
        this.skillPointGreenLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointGreenLevelsPerPoint, 2, "Levels per Green skill point. Default: 2").getInt();
        this.skillPointRedMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointRedMinLevel, 30, "Minimum magic level to start earning Red skill points. Default: 30").getInt();
        this.skillPointRedLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointRedLevelsPerPoint, 2, "Levels per Red skill point. Default: 2").getInt();
        this.skillPointYellowMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointYellowMinLevel, 40, "Minimum magic level to start earning Yellow skill points. Default: 40").getInt();
        this.skillPointYellowLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointYellowLevelsPerPoint, 3, "Levels per Yellow skill point. Default: 3").getInt();
        this.skillPointMagentaMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointMagentaMinLevel, 50, "Minimum magic level to start earning Magenta skill points. Default: 50").getInt();
        this.skillPointMagentaLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointMagentaLevelsPerPoint, 3, "Levels per Magenta skill point. Default: 3").getInt();
        this.skillPointCyanMinLevel = this.get(CATEGORY_GENERAL, this.KEY_SkillPointCyanMinLevel, 60, "Minimum magic level to start earning Cyan skill points. Default: 60").getInt();
        this.skillPointCyanLevelsPerPoint = this.get(CATEGORY_GENERAL, this.KEY_SkillPointCyanLevelsPerPoint, 4, "Levels per Cyan skill point. Default: 4").getInt();
        SkillPoint.loadFromConfig(this);

        // Affinity Ability Thresholds & Effects
        this.affinityAgileMinDepth = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityAgileMinDepth, 0.5, "Minimum air affinity depth to activate Agile (jump boost). Default: 0.5").getDouble(0.5);
        this.affinityAgileJumpBoost = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityAgileJumpBoost, 0.35, "Jump boost factor multiplied by air affinity depth. Default: 0.35").getDouble(0.35);
        this.affinityColdBloodedMinDepth = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityColdBloodedMinDepth, 0.5, "Minimum ice affinity depth for Cold Blooded tiered effects (slowness/freeze). The speed penalty activates at depth 0.1. Default: 0.5").getDouble(0.5);
        this.affinityThornsMinDepth = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityThornsMinDepth, 0.5, "Minimum nature affinity depth for Thorns tier 1. Tier 2 at 0.75, tier 3 at 1.0. Default: 0.5").getDouble(0.5);
        this.affinityThornsDamage1 = this.get(CATEGORY_AFFINITY, this.KEY_AffinityThornsDamage1, 1, "Thorns damage at tier 1 (depth >= min). Default: 1").getInt();
        this.affinityThornsDamage2 = this.get(CATEGORY_AFFINITY, this.KEY_AffinityThornsDamage2, 2, "Thorns damage at tier 2 (depth >= 0.75). Default: 2").getInt();
        this.affinityThornsDamage3 = this.get(CATEGORY_AFFINITY, this.KEY_AffinityThornsDamage3, 3, "Thorns damage at tier 3 (depth == 1.0). Default: 3").getInt();
        this.affinityFulminationMinDepth = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityFulminationMinDepth, 0.7, "Minimum lightning affinity depth for creeper supercharge chance. Default: 0.7").getDouble(0.7);
        this.affinityFulminationLightningChance = (float) this.get(CATEGORY_AFFINITY, this.KEY_AffinityFulminationLightningChance, 0.05, "Chance (0.0-1.0) per tick for creeper supercharge at fulmination depth. Default: 0.05 (5%)").getDouble(0.05);

        // Burnout Formula
        this.manaBurnoutRatio = (float) this.get(CATEGORY_GENERAL, this.KEY_ManaBurnoutRatio, 0.38, "Fraction of mana cost added as burnout when casting a spell. Default: 0.38").getDouble(0.38);
        this.burnoutPerLevel = this.get(CATEGORY_GENERAL, this.KEY_BurnoutPerLevel, 10, "Burnout capacity gained per magic level (maxBurnout = level * this + base). Default: 10").getInt();
        this.burnoutBase = this.get(CATEGORY_GENERAL, this.KEY_BurnoutBase, 1, "Base burnout capacity at level 0. Default: 1").getInt();
        this.affinityHealCooldownFull = this.get(CATEGORY_AFFINITY, this.KEY_AffinityHealCooldownFull, 40, "Cooldown ticks for affinity heal when placed on full cooldown. Default: 40").getInt();
        this.affinityHealCooldownPartial = this.get(CATEGORY_AFFINITY, this.KEY_AffinityHealCooldownPartial, 20, "Cooldown ticks for affinity heal when placed on partial cooldown. Default: 20").getInt();

        // Flicker Generation Weights
        this.flickerWeightAir = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightAir, 50, "Spawn weight for Air flickers. Default: 50").getInt();
        this.flickerWeightArcane = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightArcane, 25, "Spawn weight for Arcane flickers. Default: 25").getInt();
        this.flickerWeightEarth = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightEarth, 50, "Spawn weight for Earth flickers. Default: 50").getInt();
        this.flickerWeightEnder = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightEnder, 5, "Spawn weight for Ender flickers. Default: 5").getInt();
        this.flickerWeightFire = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightFire, 50, "Spawn weight for Fire flickers. Default: 50").getInt();
        this.flickerWeightIce = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightIce, 25, "Spawn weight for Ice flickers. Default: 25").getInt();
        this.flickerWeightLife = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightLife, 5, "Spawn weight for Life flickers. Default: 5").getInt();
        this.flickerWeightLightning = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightLightning, 25, "Spawn weight for Lightning flickers. Default: 25").getInt();
        this.flickerWeightNature = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightNature, 25, "Spawn weight for Nature flickers. Default: 25").getInt();
        this.flickerWeightWater = this.get(CATEGORY_FLICKER, this.KEY_FlickerWeightWater, 50, "Spawn weight for Water flickers. Default: 50").getInt();

        // Beam Shape Tick Rates
        this.beamBlockTickRate = this.get(CATEGORY_SPELL_BALANCE, this.KEY_BeamBlockTickRate, 5, "Beam/Cone block effect is applied every N ticks. Default: 5").getInt();
        this.beamEntityTickRate = this.get(CATEGORY_SPELL_BALANCE, this.KEY_BeamEntityTickRate, 10, "Beam/Cone entity effect is applied every N ticks. Default: 10").getInt();
        this.channelTickRate = this.get(CATEGORY_SPELL_BALANCE, this.KEY_ChannelTickRate, 10, "Channel self-effect is applied every N ticks. Default: 10").getInt();

        // Processing Block Costs
        this.reconstructorRepairCost = (float) this.get(CATEGORY_POWER, this.KEY_ReconstructorRepairCost, 250.0, "Etherium cost per damage point for the Arcane Reconstructor (before focus discounts). Default: 250").getDouble(250.0);
        this.deconstructorTime = this.get(CATEGORY_POWER, this.KEY_DeconstructorTime, 200, "Ticks to deconstruct an item in the Arcane Deconstructor. Default: 200").getInt();
        this.deconstructorPowerCost = (float) this.get(CATEGORY_POWER, this.KEY_DeconstructorPowerCost, 1.25, "Etherium cost per tick for the Arcane Deconstructor. Default: 1.25").getDouble(1.25);

        this.initDirectProperties();

        this.save();
    }

    @SideOnly(Side.CLIENT)
    public void clientInit() {
        this.AuraType = this.get(CATEGORY_BETA, this.KEY_AuraType, 15).getInt(15);
        //AuraType %= AMParticle.particleTypes.length;
        this.AuraBehaviour = this.get(CATEGORY_BETA, this.KEY_AuraBehaviour, 0).getInt(0);
        this.AuraBehaviour %= ParticleController.AuraControllerOptions.length;
        this.AuraAlpha = (float) (this.get(CATEGORY_BETA, this.KEY_AuraAlpha, 1.0D)).getDouble(1.0D);
        this.AuraScale = (float) (this.get(CATEGORY_BETA, this.KEY_AuraScale, 1.0D).getDouble(1.0));
        this.AuraColor = this.get(CATEGORY_BETA, this.KEY_AuraColor, 0xFFFFFF).getInt(0xFFFFFF);
        this.AuraQuantity = this.get(CATEGORY_BETA, this.KEY_AuraQuanity, 1).getInt(1);
        this.AuraDelay = this.get(CATEGORY_BETA, this.KEY_AuraDelay, 5).getInt(5);
        this.AuraSpeed = this.get(CATEGORY_BETA, this.KEY_AuraSpeed, 0.02D).getDouble(0.02D);
        this.AuraRandomColor = this.get(CATEGORY_BETA, this.KEY_AuraColorRandomize, true).getBoolean(true);
        this.AuraDefaultColor = this.get(CATEGORY_BETA, this.KEY_AuraColorDefault, true).getBoolean(true);

        this.GFXLevel = 2 - Minecraft.getMinecraft().gameSettings.particleSetting;

        this.save();
    }

    //====================================================================================
    // Getters - Cached
    //====================================================================================

    public boolean FullGFX() {
        return this.GFXLevel == 2;
    }

    public boolean LowGFX() {
        return this.GFXLevel == 1;
    }

    public boolean NoGFX() {
        return this.GFXLevel == 0;
    }

    public boolean NPCSpellsDamageTerrain() {
        return this.NPCSpellsDamageTerrain;
    }

    public boolean PlayerSpellsDamageTerrain() {
        return this.PlayerSpellsDamageTerrain;
    }

    public int getGFXLevel() {
        return this.GFXLevel;
    }

    public float getDamageMultiplier() {
        return this.DamageMultiplier;
    }

    public boolean getIsImbueEnchantEnabled() {
        return this.IsImbueEnabled;
    }

    public int getImbueProcCost(int enchantID) {
        return 0;
    }

    public boolean useSpecialRenderers() {
        return this.UseSpecialRenderers;
    }

    public boolean displayManaInInventory() {
        return this.DisplayManaInInventory;
    }

    public double getFrictionCoefficient() {
        return this.FrictionCoefficient;
    }

    public double getJumpBoostFactor() {
        return this.JumpBoostFactor;
    }

    public boolean retroactiveWorldgen() {
        return this.RetroWorldGen;
    }

    public int getSkillTreeSecondaryTierCap() {
        return this.secondarySkillTreeTierCap;
    }

    public int getMagicLevelCap() {
        return this.magicLevelCap;
    }

    public int getVillagerProfessionID() {
        return this.mageVillagerProfessionID;
    }

    public AMVector2 getManaHudPosition() {
        return this.manaHudPosition;
    }

    public AMVector2 getBurnoutHudPosition() {
        return this.burnoutHudPosition;
    }

    public int getManaBarWidth() {
        return Math.max(2, Math.min(100, this.manaBarWidth));
    }

    public int getManaBarHeight() {
        return Math.max(2, Math.min(10, this.manaBarHeight));
    }

    public int getBurnoutBarWidth() {
        return Math.max(2, Math.min(100, this.burnoutBarWidth));
    }

    public int getBurnoutBarHeight() {
        return Math.max(2, Math.min(10, this.burnoutBarHeight));
    }

    public void setManaBarWidth(int width) {
        this.manaBarWidth = Math.max(2, Math.min(100, width));
    }

    public void setManaBarHeight(int height) {
        this.manaBarHeight = Math.max(2, Math.min(10, height));
    }

    public void setBurnoutBarWidth(int width) {
        this.burnoutBarWidth = Math.max(2, Math.min(100, width));
    }

    public void setBurnoutBarHeight(int height) {
        this.burnoutBarHeight = Math.max(2, Math.min(10, height));
    }

    public AMVector2 getPositiveBuffsPosition() {
        return this.positiveBuffsPosition;
    }

    public AMVector2 getNegativeBuffsPosition() {
        return this.negativeBuffsPosition;
    }

    public AMVector2 getLevelPosition() {
        return this.levelPosition;
    }

    public AMVector2 getAffinityPosition() {
        return this.affinityPosition;
    }

    public AMVector2 getArmorPositionHead() {
        return this.armorPositionHead;
    }

    public AMVector2 getArmorPositionChest() {
        return this.armorPositionChest;
    }

    public AMVector2 getArmorPositionLegs() {
        return this.armorPositionLegs;
    }

    public AMVector2 getArmorPositionBoots() {
        return this.armorPositionBoots;
    }

    public AMVector2 getXPBarPosition() {
        return this.xpBarPosition;
    }

    public AMVector2 getContingencyPosition() {
        return this.contingencyPosition;
    }

    public AMVector2 getManaNumericPosition() {
        return this.manaNumericPosition;
    }

    public AMVector2 getBurnoutNumericPosition() {
        return this.burnoutNumericPosition;
    }

    public AMVector2 getXPNumericPosition() {
        return this.XPNumericPosition;
    }

    public AMVector2 getSpellBookPosition() {
        return this.SpellBookPosition;
    }

    public boolean getShowBuffs() {
        return this.showBuffs;
    }

    public boolean getShowNumerics() {
        return this.showNumerics;
    }

    public String[] getDigBlacklist() {
        return this.digBlacklist;
    }

    public int[] getWorldgenBlacklist() {
        return this.worldgenBlacklist;
    }

    public int getEssenceLakeFrequency() {
        return this.essenceLakeFrequency;
    }

    public int getTarmaRootFrequency() {
        return this.tarmaRootFrequency;
    }

    public int getWitchwoodFrequency() {
        return this.witchwoodFrequency;
    }

    public int getWakebloomFrequency() {
        return this.wakebloomFrequency;
    }

    public int getCeruBlossomFrequency() {
        return this.ceruBlossomFrequency;
    }

    public int getDesertNovaFrequency() {
        return this.desertNovaFrequency;
    }

    public int getEssencePuddleFrequency() {
        return this.essencePuddleFrequency;
    }

    public boolean getGatewayRuinEnabled() {
        return this.gatewayRuinEnabled;
    }

    public int getGatewayRuinFrequency() {
        return this.gatewayRuinFrequency;
    }

    public String[] getGatewayRuinBiomes() {
        return this.gatewayRuinBiomes;
    }

    public String[] getGatewayRuinBiomeBlacklist() {
        return this.gatewayRuinBiomeBlacklist;
    }

    public String[] getFairyRingMushroomBlocks() {
        return this.fairyRingMushroomBlocks;
    }

    public boolean getGatewayRuinChest() {
        return this.gatewayRuinChest;
    }

    public boolean getLostArchiveEnabled() {
        return this.lostArchiveEnabled;
    }

    public int getLostArchiveFrequency() {
        return this.lostArchiveFrequency;
    }

    public String[] getLostArchiveBiomeBlacklist() {
        return this.lostArchiveBiomeBlacklist;
    }

    public int getBlueTopazFrequency() {
        return this.blueTopazFrequency;
    }

    public int getBlueTopazVeinSize() {
        return this.blueTopazVeinSize;
    }

    public int getBlueTopazMinHeight() {
        return this.blueTopazMinHeight;
    }

    public int getBlueTopazMaxHeight() {
        return this.blueTopazMaxHeight;
    }

    public int getVinteumFrequency() {
        return this.vinteumFrequency;
    }

    public int getVinteumVeinSize() {
        return this.vinteumVeinSize;
    }

    public int getVinteumMinHeight() {
        return this.vinteumMinHeight;
    }

    public int getVinteumMaxHeight() {
        return this.vinteumMaxHeight;
    }

    public int getChimeriteFrequency() {
        return this.chimeriteFrequency;
    }

    public int getChimeriteVeinSize() {
        return this.chimeriteVeinSize;
    }

    public int getChimeriteMinHeight() {
        return this.chimeriteMinHeight;
    }

    public int getChimeriteMaxHeight() {
        return this.chimeriteMaxHeight;
    }

    public int getSunstoneFrequency() {
        return this.sunstoneFrequency;
    }

    public int getSunstoneVeinSize() {
        return this.sunstoneVeinSize;
    }

    public int getSunstoneMinHeight() {
        return this.sunstoneMinHeight;
    }

    public int getSunstoneMaxHeight() {
        return this.sunstoneMaxHeight;
    }

    public boolean moonstoneMeteorsDestroyTerrain() {
        return this.moonstoneMeteorsDestroyTerrain;
    }

    public boolean suggestSpellNames() {
        return this.suggestSpellNames;
    }

    public int getStaffDurability() {
        return this.staffDurability;
    }

    public String[] getStaffPresets() {
        return this.staffPresets;
    }

    public int getWitchwoodForestID() {
        return this.witchwoodForestID;
    }

    public int getEverstoneRepairRate() {
        return this.everstoneRepairRate;
    }

    public int getManaMartiniDuration() {
        return this.manaMartiniDuration;
    }

    public int getManaCakeRegenDuration() {
        return this.manaCakeRegenDuration;
    }

    public int getManaCakeFoodAmount() {
        return this.manaCakeFoodAmount;
    }

    public float getLesserManaPotionMana() { return this.lesserManaPotionMana; }
    public int getLesserManaPotionRegenLevel() { return this.lesserManaPotionRegenLevel; }
    public int getLesserManaPotionRegenDuration() { return this.lesserManaPotionRegenDuration; }

    public float getStandardManaPotionMana() { return this.standardManaPotionMana; }
    public int getStandardManaPotionRegenLevel() { return this.standardManaPotionRegenLevel; }
    public int getStandardManaPotionRegenDuration() { return this.standardManaPotionRegenDuration; }

    public float getGreaterManaPotionMana() { return this.greaterManaPotionMana; }
    public int getGreaterManaPotionRegenLevel() { return this.greaterManaPotionRegenLevel; }
    public int getGreaterManaPotionRegenDuration() { return this.greaterManaPotionRegenDuration; }

    public float getEpicManaPotionMana() { return this.epicManaPotionMana; }
    public int getEpicManaPotionRegenLevel() { return this.epicManaPotionRegenLevel; }
    public int getEpicManaPotionRegenDuration() { return this.epicManaPotionRegenDuration; }

    public float getLegendaryManaPotionMana() { return this.legendaryManaPotionMana; }
    public int getLegendaryManaPotionRegenLevel() { return this.legendaryManaPotionRegenLevel; }
    public int getLegendaryManaPotionRegenDuration() { return this.legendaryManaPotionRegenDuration; }

    public boolean showHudMinimally() {
        return this.showHudMinimally;
    }

    public boolean stagedCompendium() {
        return this.stagedCompendium;
    }

    public boolean showXPAlways() {
        return this.showXPAlways;
    }

    public boolean showHudBars() {
        return this.showHudBars;
    }

    public boolean witchwoodLeafPFX() {
        return this.witchwoodLeafParticles;
    }

    public boolean colourblindMode() {
        return this.colourblindMode;
    }

    public String[] getAppropriationBlockBlacklist() {
        return this.appropriationBlockBlacklist;
    }

    public Class<? extends Entity>[] getAppropriationMobBlacklist() {
        return this.appropriationMobBlacklist;
    }

    public int getCrystallizeMaxHealth() {
        return this.crystallizeMaxHealth;
    }

    public float getCrystallizeHealthThreshold() {
        return this.crystallizeHealthThreshold;
    }

    public String[] getCrystallizeMobBlacklist() {
        return this.crystallizeMobBlacklist;
    }

    public boolean getHazardousGateways() {
        return this.hazardousGateways;
    }

    public double getArmorXPInfusionFactor() {
        return this.ArmorXPInfusionFactor;
    }

    public double getArmorInfusionCostMultiplier() {
        return this.armorInfusionCostMultiplier;
    }

    public float getMageSetDurationMultiplier() {
        return this.mageSetDurationMultiplier;
    }

    public float getBattlemageSetDurationMultiplier() {
        return this.battlemageSetDurationMultiplier;
    }

    public float getArchmageSetDurationMultiplier() {
        return this.archmageSetDurationMultiplier;
    }

    public float getMageSetDamageMultiplier() {
        return this.mageSetDamageMultiplier;
    }

    public float getBattlemageSetDamageMultiplier() {
        return this.battlemageSetDamageMultiplier;
    }

    public float getArchmageSetDamageMultiplier() {
        return this.archmageSetDamageMultiplier;
    }

    public boolean getDisarmAffectsPlayers() {
        return this.disarmAffectsPlayers;
    }

    public double getManaCap() {
        return this.manaCap;
    }

    public double getBaseMana() {
        return this.baseMana;
    }

    public boolean getDigBreaksTileEntities() {
        return this.digBreaksTileEntities;
    }

    public boolean savePowerDataOnWorldSave() {
        return this.savePowerOnWorldSave;
    }


    public boolean canDraydsDespawn() {
        return this.canDryadsDespawn;
    }

    public int getMeteorMinSpawnLevel() {
        return this.meteorMinSpawnLevel;
    }

    public int getMeteorSpawnBaseChance() {
        return this.meteorSpawnBaseChance;
    }

    public int getMaxOrbsPerPlayer() {
        return this.maxOrbsPerPlayer;
    }

    public int getMeteorSpawnMoonPhaseMultiplier() {
        return this.meteorSpawnMoonPhaseMultiplier;
    }

    public int getMeteorSpawnCooldownMin() {
        return this.meteorSpawnCooldownMin;
    }

    public int getMeteorSpawnCooldownMax() {
        return this.meteorSpawnCooldownMax;
    }

    public int getMeteorSpawnRadiusPlayer() {
        return this.meteorSpawnRadiusPlayer;
    }

    public int getMeteorSpawnRadiusAttractor() {
        return this.meteorSpawnRadiusAttractor;
    }

    public boolean forgeSmeltsVillagers() {
        return this.forgeSmeltsVillagers;
    }

    public boolean showArmorUI() {
        return this.showArmorUI;
    }

    public boolean candlesAreRovingLights() {
        return this.candlesAreRovingLights;
    }

    public int getEnderAffinityAbilityCooldown() {
        return this.enderAffinityAbilityCooldown;
    }

    public boolean getEnableWitchwoodForest() {
        return this.enableWitchwoodForest;
    }

    public int getWitchwoodForestRarity() {
        return this.witchwoodForestRarity;
    }

    public boolean getAllowCreativeTargets() {
        return this.allowCreativeTargets;
    }

    //====================================================================================
    // Getters - Aura
    //====================================================================================

    public int getAuraIndex() {
        return this.AuraType;
    }

    public int getAuraBehaviour() {
        return this.AuraBehaviour;
    }

    public boolean getAuraColorRandom() {
        return this.AuraRandomColor;
    }

    public boolean getAuraColorDefault() {
        return this.AuraDefaultColor;
    }

    public float getAuraScale() {
        return this.AuraScale;
    }

    public int getAuraColor() {
        return this.AuraColor;
    }

    public int getAuraDelay() {
        return this.AuraDelay;
    }

    public int getAuraQuantity() {
        return this.AuraQuantity;
    }

    public float getAuraSpeed() {
        return (float) this.AuraSpeed;
    }

    public float getAuraAlpha() {
        return this.AuraAlpha;
    }

    //====================================================================================
    // Getters - Direct
    //====================================================================================
    //ping the direct properties once so that they show up in config
    public void initDirectProperties() {
        this.get(CATEGORY_MOBS, this.KEY_HecateSpawnRate, 2).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_MageSpawnRate, 1).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_WaterElementalSpawnRate, 12).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_ManaElementalSpawnRate, 12).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_DryadSpawnRate, 5).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_DryadCropGrowthEnabled, true);
        this.get(CATEGORY_MOBS, this.KEY_DryadBonemealEnabled, true);
        this.get(CATEGORY_MOBS, this.KEY_DryadPlantGrowthRate, 300);
        this.get(CATEGORY_MOBS, this.KEY_ManaCreeperSpawnRate, 3).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_DarklingSpawnRate, 5).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_EarthElementalSpawnRate, 12).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_EarthElementalSpawnUnderground, true).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_FireElementalSpawnRate, 12).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_LightningElementalSpawnRate, 12).setRequiresMcRestart(true);
        this.get(CATEGORY_MOBS, this.KEY_FlickerSpawnRate, 1).setRequiresMcRestart(true);
    }

    public int GetHecateSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_HecateSpawnRate, 2).setRequiresMcRestart(true);
        return Math.max(prop.getInt(2), 0);
    }

    public int GetMageSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_MageSpawnRate, 1).setRequiresMcRestart(true);
        return Math.max(prop.getInt(1), 0);
    }

    public int GetWaterElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_WaterElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public int GetManaElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_ManaElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public int GetDryadSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_DryadSpawnRate, 5).setRequiresMcRestart(true);
        return Math.max(prop.getInt(5), 0);
    }

    public boolean GetDryadCropGrowthEnabled() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_DryadCropGrowthEnabled, true);
        return prop.getBoolean(true);
    }

    public boolean GetDryadBonemealEnabled() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_DryadBonemealEnabled, true);
        return prop.getBoolean(true);
    }

    public int GetDryadPlantGrowthRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_DryadPlantGrowthRate, 300);
        return Math.max(prop.getInt(300), 1);
    }

    public int GetManaCreeperSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_ManaCreeperSpawnRate, 3).setRequiresMcRestart(true);
        return Math.max(prop.getInt(3), 0);
    }

    public int GetDarklingSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_DarklingSpawnRate, 5).setRequiresMcRestart(true);
        return Math.max(prop.getInt(5), 0);
    }

    public int GetEarthElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_EarthElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public boolean GetEarthElementalSpawnUnderground() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_EarthElementalSpawnUnderground, true).setRequiresMcRestart(true);
        return prop.getBoolean(true);
    }

    public int GetFireElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_FireElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public int GetLightningElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_LightningElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public int GetIceElementalSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_IceElementalSpawnRate, 12).setRequiresMcRestart(true);
        return Math.max(prop.getInt(12), 0);
    }

    public int GetFlickerSpawnRate() {
        Property prop = this.get(CATEGORY_MOBS, this.KEY_FlickerSpawnRate, 1).setRequiresMcRestart(true);
        return Math.max(prop.getInt(1), 0);
    }

    //====================================================================================
    // Setters
    //====================================================================================

    @SideOnly(Side.CLIENT)
    public void setAuraIndex(int index) {
        if (index < 0) index = 0;
        if (index >= AMParticle.particleTypes.length) index = AMParticle.particleTypes.length - 1;

        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraType, 15);
        prop.set(index);

        this.AuraType = index;
    }

    public void setAuraBehaviour(int index) {
        if (index < 0) index = 0;
        if (index >= ParticleController.AuraControllerOptions.length)
            index = ParticleController.AuraControllerOptions.length - 1;

        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraBehaviour, 0);
        prop.set(index);

        this.AuraBehaviour = index;
    }

    public void setAuraColorRandom(boolean value) {
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraColorRandomize, false);
        prop.set(value);

        this.AuraRandomColor = value;
    }

    public void setAuraColorDefault(boolean value) {
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraColorDefault, true);
        prop.set(value);

        this.AuraDefaultColor = value;
    }

    public void setAuraScale(float scale) {
        if (scale < 1) scale = 1;
        if (scale > 200) scale = 200;
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraScale, 50D);
        prop.set(scale);

        this.AuraScale = scale;
    }

    public void setAuraColor(int color) {
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraColor, 0xFFFFFF);
        prop.set(color);

        this.AuraColor = color;
    }

    public void setAuraAlpha(float alpha) {
        if (alpha < 0) alpha = 0;
        if (alpha > 100) alpha = 100;
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraAlpha, 255D);
        prop.set(alpha);

        this.AuraAlpha = alpha;
    }

    public void setAuraQuantity(int quantity) {
        if (quantity < 1) quantity = 1;
        else if (quantity > 5) quantity = 5;
        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraAlpha, 2);
        prop.set(quantity);

        this.AuraQuantity = quantity;
    }

    public void setAuraDelay(int delay) {
        if (delay < 1) delay = 1;
        else if (delay > 200) delay = 200;

        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraDelay, 5);
        prop.set(delay);

        this.AuraDelay = delay;
    }

    public void setAuraSpeed(float speed) {
        if (speed < 0.01f) speed = 0.01f;
        else if (speed > 10f) speed = 10f;

        Property prop = this.get(CATEGORY_BETA, this.KEY_AuraSpeed, 0.02f);
        prop.set(speed);

        this.AuraSpeed = speed;
    }

    public void setDisplayManaInInventory(boolean value) {
        boolean def = !Loader.isModLoaded("NotEnoughItems");
        Property prop = this.get(CATEGORY_GENERAL, this.KEY_DisplayManaInInventory, def, "This will toggle mana display on and off in your inventory.  Default 'O' key in game.");
        prop.set(value);

        this.DisplayManaInInventory = value;
    }

    public void disableRetroactiveWorldgen() {
        Property prop = this.get(CATEGORY_WORLDGEN, this.KEY_RetroactiveWorldGen, false, "Set this to true to enable retroactive worldgen for Ars Magica structures and ores.  *WARNING* This may break your save!  Do a backup first!");
        prop.set(false);

        this.RetroWorldGen = false;
    }

    public void resetGuiPositions() {
        setGuiPositions(
                new AMVector2(0.7104166746139526, 0.9137254953384399),
                new AMVector2(0.13333334028720856, 0.9176470637321472),
                new AMVector2(0.49791666865348816, 0.8117647171020508),
                new AMVector2(0.9770833253860474, 0.9),
                new AMVector2(0.5145833492279053, 0.47843137383461),
                new AMVector2(0.46666666865348816, 0.47843137383461),
                new AMVector2(0.004166666883975267, 0.5176470875740051),
                new AMVector2(0.004166666883975267, 0.5568627715110779),
                new AMVector2(0.004166666883975267, 0.5960784554481506),
                new AMVector2(0.004166666883975267, 0.6352941393852234),
                new AMVector2(0.31041666865348816, 0.7843137383460999),
                new AMVector2(0.0020833334419876337, 0.9333333373069763),
                new AMVector2(0.7437499761581421, 0.8941176533699036),
                new AMVector2(0.21041665971279144, 0.9058823585510254),
                new AMVector2(0.47083333134651184, 0.7450980544090271),
                new AMVector2(0.0, 0.0),
                new AMVector2(0.7104166746139526, 0.9352226853370667),
                true, false, false, true, false, true,
                100, 10, 100, 10);
        saveGuiPositions();
    }

    public void setGuiPositions(AMVector2 manaHud, AMVector2 burnoutHud, AMVector2 levelHud, AMVector2 affinityHud, AMVector2 posBuffsHud, AMVector2 negBuffsHud, AMVector2 armorHead, AMVector2 armorChest, AMVector2 armorLegs, AMVector2 armorBoots, AMVector2 xpBar, AMVector2 contingency, AMVector2 manaNumeric, AMVector2 burnoutNumeric, AMVector2 XPNumeric, AMVector2 spellBookPos, AMVector2 manaShieldingPos, boolean showBuffs, boolean showNumerics, boolean minimalHud, boolean showArmorUI, boolean showXPAlways, boolean showHudBars, int manaBarWidth, int manaBarHeight, int burnoutBarWidth, int burnoutBarHeight) {
        this.manaHudPosition = manaHud;
        this.burnoutHudPosition = burnoutHud;
        this.levelPosition = levelHud;
        this.affinityPosition = affinityHud;
        this.positiveBuffsPosition = posBuffsHud;
        this.negativeBuffsPosition = negBuffsHud;
        this.armorPositionHead = armorHead;
        this.armorPositionChest = armorChest;
        this.armorPositionLegs = armorLegs;
        this.armorPositionBoots = armorBoots;
        this.xpBarPosition = xpBar;
        this.contingencyPosition = contingency;
        this.manaNumericPosition = manaNumeric;
        this.burnoutNumericPosition = burnoutNumeric;
        this.XPNumericPosition = XPNumeric;
        this.SpellBookPosition = spellBookPos;
        this.manaShieldingPosition = manaShieldingPos;
        this.showBuffs = showBuffs;
        this.showNumerics = showNumerics;
        this.showHudMinimally = minimalHud;
        this.showArmorUI = showArmorUI;
        this.showXPAlways = showXPAlways;
        this.showHudBars = showHudBars;
        this.manaBarWidth = manaBarWidth;
        this.manaBarHeight = manaBarHeight;
        this.burnoutBarWidth = burnoutBarWidth;
        this.burnoutBarHeight = burnoutBarHeight;
    }

    public void saveGuiPositions() {
        this.updateAMVector2(this.KEY_ManaHudPositionX, this.KEY_ManaHudPositionY, this.manaHudPosition);
        this.updateAMVector2(this.KEY_BurnoutHudPositionX, this.KEY_BurnoutHudPositionY, this.burnoutHudPosition);
        this.updateAMVector2(this.KEY_LevelPositionX, this.KEY_LevelPositionY, this.levelPosition);
        this.updateAMVector2(this.KEY_AffinityPositionX, this.KEY_AffinityPositionY, this.affinityPosition);
        this.updateAMVector2(this.KEY_BuffsPositivePositionX, this.KEY_BuffsPositivePositionY, this.positiveBuffsPosition);
        this.updateAMVector2(this.KEY_BuffsNegativePositionX, this.KEY_BuffsNegativePositionY, this.negativeBuffsPosition);
        this.updateAMVector2(this.KEY_ArmorPositionHeadX, this.KEY_ArmorPositionHeadY, this.armorPositionHead);
        this.updateAMVector2(this.KEY_ArmorPositionChestX, this.KEY_ArmorPositionChestY, this.armorPositionChest);
        this.updateAMVector2(this.KEY_ArmorPositionLegsX, this.KEY_ArmorPositionLegsY, this.armorPositionLegs);
        this.updateAMVector2(this.KEY_ArmorPositionBootsX, this.KEY_ArmorPositionBootsY, this.armorPositionBoots);
        this.updateAMVector2(this.KEY_XPBarPositionX, this.KEY_XPBarPositionY, this.xpBarPosition);
        this.updateAMVector2(this.KEY_ContingencyPositionX, this.KEY_ContingencyPositionY, this.contingencyPosition);
        this.updateAMVector2(this.KEY_ManaNumericPositionX, this.KEY_ManaNumericPositionY, this.manaNumericPosition);
        this.updateAMVector2(this.KEY_BurnoutNumericPositionX, this.KEY_BurnoutNumericPositionY, this.burnoutNumericPosition);
        this.updateAMVector2(this.KEY_XPNumericPositionX, this.KEY_XPNumericPositionY, this.XPNumericPosition);
        this.updateAMVector2(this.KEY_SpellBookPositionX, this.KEY_SpellBookPositionY, this.SpellBookPosition);
        this.updateAMVector2(this.KEY_ManaShieldingPositionX, this.KEY_ManaShieldingPositionY, this.manaShieldingPosition);

        Property buffProp;
        buffProp = this.get(CATEGORY_UI, this.KEY_ShowBuffs, true);
        buffProp.set(this.showBuffs);

        Property numProp;
        numProp = this.get(CATEGORY_UI, this.KEY_ShowNumerics, false);
        numProp.set(this.showNumerics);

        Property armorProp;
        armorProp = this.get(CATEGORY_UI, this.KEY_ShowArmorUI, true);
        armorProp.set(this.showArmorUI);

        Property minimalProp;
        minimalProp = this.get(CATEGORY_UI, this.KEY_ShowHudMinimally, false);
        minimalProp.set(this.showHudMinimally);

        Property xpShow;
        xpShow = this.get(CATEGORY_UI, this.KEY_ShowXPAlways, false);
        xpShow.set(this.showXPAlways);

        Property barShow;
        barShow = this.get(CATEGORY_UI, this.KEY_ShowHUDBars, true);
        barShow.set(this.showHudBars);

        Property manaWidth;
        manaWidth = this.get(CATEGORY_UI, this.KEY_ManaBarWidth, 100);
        manaWidth.set(this.manaBarWidth);

        Property manaHeight;
        manaHeight = this.get(CATEGORY_UI, this.KEY_ManaBarHeight, 10);
        manaHeight.set(this.manaBarHeight);

        Property burnoutWidth;
        burnoutWidth = this.get(CATEGORY_UI, this.KEY_BurnoutBarWidth, 100);
        burnoutWidth.set(this.burnoutBarWidth);

        Property burnoutHeight;
        burnoutHeight = this.get(CATEGORY_UI, this.KEY_BurnoutBarHeight, 10);
        burnoutHeight.set(this.burnoutBarHeight);

        this.save();
    }

    public void setSkillTreeSecondaryTierCap(int skillTreeLock) {
        this.secondarySkillTreeTierCap = skillTreeLock;
    }

    private void updateAMVector2(String keyX, String keyY, AMVector2 value) {
        Property prop;
        prop = this.get(CATEGORY_UI, keyX, 0);
        prop.set(value.x);

        prop = this.get(CATEGORY_UI, keyY, 0);
        prop.set(value.y);
    }

    public void setManaCap(double cap) {
        this.manaCap = cap;
    }

    public AMVector2 getManaShieldingPosition() {
        return this.manaShieldingPosition;
    }

    public boolean getOldXpCalculations() {
        return this.oldXpCalculations;
    }

    public void setOldXpCalculations(boolean b) {
        this.oldXpCalculations = oldXpCalculations;
    }

    public boolean getBurnoutHungerDepletion() {
        return this.burnoutHungerDepletion;
    }

    public String[] getObeliskExtraFuels() {
        return this.obeliskExtraFuels;
    }

    public String[] getExtraAltarCaps() {
        return this.extraAltarCaps;
    }

    public String[] getExtraAltarMain() {
        return this.extraAltarMain;
    }

    public boolean getEBWizSpellsInSpellBook() {
        return this.ebwizSpellsInSpellBook;
    }

    public float getEBWizManaCostMultiplier() {
        return this.ebwizManaCostMultiplier;
    }

    public boolean getEBWizDisableWandMana() {
        return this.ebwizDisableWandMana;
    }

    public boolean getEBWizScrollRequiresMana() {
        return this.ebwizScrollRequiresMana;
    }

    public boolean getEBWizHideWandTooltip() {
        return this.ebwizHideWandTooltip;
    }

    public float getEBWizMagicXPMultiplier() {
        return this.ebwizMagicXPMultiplier;
    }

    public boolean getEBWizPreserveSpellBook() {
        return this.ebwizPreserveSpellBook;
    }

    public boolean getEBWizAffinityGainEnabled() {
        return this.ebwizAffinityGainEnabled;
    }

    public double getEBWizAffinityGainAmount() {
        return this.ebwizAffinityGainAmount;
    }

    public Map<String, List<WeightedAffinity>> getEBWizElementAffinityMap() {
        return this.ebwizElementAffinityMap;
    }

    public Map<String, List<WeightedAffinity>> getEBWizSpellAffinityOverrides() {
        return this.ebwizSpellAffinityOverrides;
    }

    public boolean getEBWizCondenserManaRegenEnabled() {
        return this.ebwizCondenserManaRegenEnabled;
    }

    public double getEBWizCondenserManaRegenBonusPerLevel() {
        return this.ebwizCondenserManaRegenBonusPerLevel;
    }

    public double getManaDrainRatio() {
        return this.manaDrainRatio;
    }

    public boolean getEBWizDiscoveryEnabled() {
        return this.ebwizDiscoveryEnabled;
    }

    public String[] getEBWizDiscoveryElementItems() {
        return this.ebwizDiscoveryElementItems;
    }

    public String[] getEBWizDiscoveryTierCatalysts() {
        return this.ebwizDiscoveryTierCatalysts;
    }

    public int[] getEBWizDiscoveryPowerCosts() {
        return this.ebwizDiscoveryPowerCosts;
    }

    public boolean getEBWizTranscriptionEnabled() {
        return this.ebwizTranscriptionEnabled;
    }

    public int getEBWizTranscriptionCostBase() {
        return this.ebwizTranscriptionCostBase;
    }

    public int getEBWizTranscriptionCostPerTier() {
        return this.ebwizTranscriptionCostPerTier;
    }

    public float getEBWizDisciplineCostReductionPerLevel() {
        return this.ebwizDisciplineCostReductionPerLevel;
    }

    public float getEBWizDisciplinePotencyBonusPerLevel() {
        return this.ebwizDisciplinePotencyBonusPerLevel;
    }

    public float getEBWizArtefactPotencyRatio() {
        return this.ebwizArtefactPotencyRatio;
    }

    public boolean getEBWizEvilWizardOrbDrop() {
        return this.ebwizEvilWizardOrbDrop;
    }

    public boolean getEBWizElementalCrystalDrops() {
        return this.ebwizElementalCrystalDrops;
    }

    public boolean getEBWizMageTomeDrop() {
        return this.ebwizMageTomeDrop;
    }

    public java.util.Set<String> getMagicResistExtraDamageTypes() {
        return this.magicResistExtraDamageTypes;
    }

    /**
     * Parses a string array of {@code KEY=name:weight|name:weight} entries into a lookup map.
     * Entries that cannot be parsed are logged and skipped.
     */
    private static Map<String, List<WeightedAffinity>> parseWeightedAffinityMap(String[] entries) {
        Map<String, List<WeightedAffinity>> map = new LinkedHashMap<>();
        for (String entry : entries) {
            int eqIdx = entry.indexOf('=');
            if (eqIdx < 1) continue;
            String key = entry.substring(0, eqIdx).trim();
            String spec = entry.substring(eqIdx + 1).trim();
            List<WeightedAffinity> weights = new ArrayList<>();
            for (String part : spec.split("\\|")) {
                int colIdx = part.lastIndexOf(':');
                if (colIdx < 1) continue;
                String aName = part.substring(0, colIdx).trim();
                try {
                    float w = Float.parseFloat(part.substring(colIdx + 1).trim());
                    if (w > 0) weights.add(new WeightedAffinity(aName, w));
                } catch (NumberFormatException e) {
                    LogHelper.warn("AMConfig: malformed weight in EBWiz affinity map entry '%s' – skipping part '%s'", entry, part);
                }
            }
            if (!weights.isEmpty()) map.put(key, weights);
        }
        return Collections.unmodifiableMap(map);
    }

    public double getFrostDamageBase() { return this.frostDamageBase; }
    public double getFireDamageBase() { return this.fireDamageBase; }
    public double getLifeDrainBase() { return this.lifeDrainBase; }
    public double getManaDrainBase() { return this.manaDrainBase; }
    public double getMagicDamageBase() { return this.magicDamageBase; }
    public double getLightningDamageBase() { return this.lightningDamageBase; }
    public double getDrownDamageBase() { return this.drownDamageBase; }
    public double getPhysicalDamageBase() { return this.physicalDamageBase; }
    public double getBlizzardDamageMultiplier() { return this.blizzardDamageMultiplier; }
    public double getLifeTapDamageMultiplier() { return this.lifeTapDamageMultiplier; }
    public double getFireRainDamageMultiplier() { return this.fireRainDamageMultiplier; }
    public double getFallingStarDamageMultiplier() { return this.fallingStarDamageMultiplier; }
    public double getDisarmDamageMultiplier() { return this.disarmDamageMultiplier; }

    public boolean getColoredSpellBookNames() {
        return this.coloredSpellBookNames;
    }

    /** Winter Arm config getters **/
    public int getWinterArmFlightTicks() { return this.winterArmFlightTicks; }

    /** Life Ward config getters **/
    public boolean getLifeWardEnabled() { return this.lifeWardEnabled; }
    public int getLifeWardTickInterval() { return this.lifeWardTickInterval; }
    public int getLifeWardMaxAbsorption() { return this.lifeWardMaxAbsorption; }
    public float getLifeWardAbsorptionPerTick() { return this.lifeWardAbsorptionPerTick; }

    /** Mob stats config getters **/
    public double getFireElementalMaxHealth() { return this.fireElementalMaxHealth; }
    public double getFireElementalAttackDamage() { return this.fireElementalAttackDamage; }
    public double getEarthElementalMaxHealth() { return this.earthElementalMaxHealth; }
    public double getEarthElementalAttackDamage() { return this.earthElementalAttackDamage; }
    public double getEarthElementalVariantMaxHealth() { return this.earthElementalVariantMaxHealth; }
    public double getEarthElementalVariantAttackDamage() { return this.earthElementalVariantAttackDamage; }
    public double getIceElementalMaxHealth() { return this.iceElementalMaxHealth; }
    public double getIceElementalAttackDamage() { return this.iceElementalAttackDamage; }
    public double getLightningElementalMaxHealth() { return this.lightningElementalMaxHealth; }
    public double getLightningElementalAttackDamage() { return this.lightningElementalAttackDamage; }
    public double getWaterElementalMaxHealth() { return this.waterElementalMaxHealth; }
    public double getManaElementalMaxHealth() { return this.manaElementalMaxHealth; }
    public double getDarkMageMaxHealth() { return this.darkMageMaxHealth; }
    public double getLightMageMaxHealth() { return this.lightMageMaxHealth; }
    public double getHecateMaxHealth() { return this.hecateMaxHealth; }
    public double getHecateAttackDamage() { return this.hecateAttackDamage; }
    public double getHellCowMaxHealth() { return this.hellCowMaxHealth; }
    public double getHellCowAttackDamage() { return this.hellCowAttackDamage; }
    public double getDarklingMaxHealth() { return this.darklingMaxHealth; }
    public double getManaCreeperMaxHealth() { return this.manaCreeperMaxHealth; }
    public double getDryadMaxHealth() { return this.dryadMaxHealth; }
    public double getDarkMageArmor() { return this.darkMageArmor; }
    public double getEarthElementalArmor() { return this.earthElementalArmor; }
    public double getHecateArmor() { return this.hecateArmor; }
    public double getIceElementalArmor() { return this.iceElementalArmor; }
    public double getFireElementalArmor() { return this.fireElementalArmor; }
    public double getLightMageArmor() { return this.lightMageArmor; }
    public double getLightningElementalArmor() { return this.lightningElementalArmor; }
    public double getManaElementalArmor() { return this.manaElementalArmor; }
    public double getHellCowArmor() { return this.hellCowArmor; }

    /** Boss config getters **/
    public float getBossMaxDamagePerHit() { return this.bossMaxDamagePerHit; }
    public int getBossHurtResistantTime() { return this.bossHurtResistantTime; }
    public double getFireGuardianMaxHealth() { return this.fireGuardianMaxHealth; }
    public double getFireGuardianArmor() { return this.fireGuardianArmor; }
    public double getWaterGuardianMaxHealth() { return this.waterGuardianMaxHealth; }
    public double getWaterGuardianArmor() { return this.waterGuardianArmor; }
    public double getAirGuardianMaxHealth() { return this.airGuardianMaxHealth; }
    public double getAirGuardianArmor() { return this.airGuardianArmor; }
    public double getEarthGuardianMaxHealth() { return this.earthGuardianMaxHealth; }
    public double getEarthGuardianArmor() { return this.earthGuardianArmor; }
    public double getLightningGuardianMaxHealth() { return this.lightningGuardianMaxHealth; }
    public double getLightningGuardianArmor() { return this.lightningGuardianArmor; }
    public double getLifeGuardianMaxHealth() { return this.lifeGuardianMaxHealth; }
    public double getLifeGuardianArmor() { return this.lifeGuardianArmor; }
    public double getEnderGuardianMaxHealth() { return this.enderGuardianMaxHealth; }
    public double getEnderGuardianArmor() { return this.enderGuardianArmor; }
    public double getArcaneGuardianMaxHealth() { return this.arcaneGuardianMaxHealth; }
    public double getArcaneGuardianArmor() { return this.arcaneGuardianArmor; }
    public double getNatureGuardianMaxHealth() { return this.natureGuardianMaxHealth; }
    public double getNatureGuardianArmor() { return this.natureGuardianArmor; }
    public double getWinterGuardianMaxHealth() { return this.winterGuardianMaxHealth; }
    public double getWinterGuardianArmor() { return this.winterGuardianArmor; }

    /** Mana & Progression config getters **/
    public int getBaseTicksForFullRegen() { return this.baseTicksForFullRegen; }
    public double getManaCoefficient() { return this.manaCoefficient; }
    public double getRegenScalingBase() { return this.regenScalingBase; }
    public double getRegenScalingPerLevel() { return this.regenScalingPerLevel; }
    public double getXPRateMultiplier() { return this.xpRateMultiplier; }

    /** Power System config getters **/
    public int getPowerSearchRadius() { return this.powerSearchRadius; }
    public int getMaxPowerSearchRadius() { return this.maxPowerSearchRadius; }
    public float getLightInputCostMultiplier() { return this.lightInputCostMultiplier; }
    public float getNeutralInputCostMultiplier() { return this.neutralInputCostMultiplier; }
    public float getDarkInputCostMultiplier() { return this.darkInputCostMultiplier; }
    public float getAstralBarrierCostPerRadius() { return this.astralBarrierCostPerRadius; }
    public float getAstralBarrierDarkCost() { return this.astralBarrierDarkCost; }
    public int getCapacityManaBattery() { return this.capacityManaBattery; }
    public int getCapacityBlackAurem() { return this.capacityBlackAurem; }
    public int getCapacityObelisk() { return this.capacityObelisk; }
    public int getCapacityCelestialPrism() { return this.capacityCelestialPrism; }
    public int getCapacitySummoner() { return this.capacitySummoner; }
    public int getCapacityEssenceRefiner() { return this.capacityEssenceRefiner; }
    public int getCapacityManaDrain() { return this.capacityManaDrain; }
    public int getCapacityCraftingAltar() { return this.capacityCraftingAltar; }
    public int getCapacityArcaneReconstructor() { return this.capacityArcaneReconstructor; }
    public int getCapacityArcaneDeconstructor() { return this.capacityArcaneDeconstructor; }
    public int getCapacityInertSpawner() { return this.capacityInertSpawner; }
    public int getCapacityKeystoneReceptacle() { return this.capacityKeystoneReceptacle; }
    public int getCapacityAstralBarrier() { return this.capacityAstralBarrier; }
    public int getCapacityFlickerLure() { return this.capacityFlickerLure; }
    public int getCapacityOtherworldAura() { return this.capacityOtherworldAura; }
    public int getCapacityCalefactor() { return this.capacityCalefactor; }
    public int getCapacitySeerStone() { return this.capacitySeerStone; }
    public int getCapacitySlipstreamGenerator() { return this.capacitySlipstreamGenerator; }

    /** Spell Balance — Shape Defaults **/
    public int getZoneDefaultRadius() { return this.zoneDefaultRadius; }
    public int getZoneDefaultDuration() { return this.zoneDefaultDuration; }
    public int getWallDefaultRadius() { return this.wallDefaultRadius; }
    public int getWallDefaultDuration() { return this.wallDefaultDuration; }
    public int getPuddleDefaultRadius() { return this.puddleDefaultRadius; }
    public int getPuddleDefaultDuration() { return this.puddleDefaultDuration; }
    public float getPuddleDefaultRange() { return this.puddleDefaultRange; }
    public int getWaveDefaultRadius() { return this.waveDefaultRadius; }
    public double getWaveSpeedMultiplier() { return this.waveSpeedMultiplier; }
    public int getWaveDefaultDuration() { return this.waveDefaultDuration; }
    public double getWaveGravityPerModifier() { return this.waveGravityPerModifier; }
    public int getConeAngle() { return this.coneAngle; }

    /** Spell Balance — Modifier Values **/
    public float getSpeedModifierValue() { return this.speedModifierValue; }
    public float getHealingModifierValue() { return this.healingModifierValue; }
    public float getRangeModifierValue() { return this.rangeModifierValue; }
    public float getDurationModifierValue() { return this.durationModifierValue; }
    public float getGravityModifierValue() { return this.gravityModifierValue; }
    public int getPiercingModifierValue() { return this.piercingModifierValue; }
    public float getSolarDamageBase() { return this.solarDamageBase; }
    public float getSolarDurationBase() { return this.solarDurationBase; }
    public float getSolarHealingBase() { return this.solarHealingBase; }
    public float getSolarRangeBase() { return this.solarRangeBase; }
    public float getLunarDamageBase() { return this.lunarDamageBase; }
    public float getLunarDurationBase() { return this.lunarDurationBase; }
    public float getLunarHealingBase() { return this.lunarHealingBase; }
    public float getLunarRangeBase() { return this.lunarRangeBase; }

    /** Spell Balance — Buff Durations **/
    public int getDefaultBuffDuration() { return this.defaultBuffDuration; }
    public int getBuffPowerDurationBonus() { return this.buffPowerDurationBonus; }
    public int getHealCooldown() { return this.healCooldown; }

    /** Spell Balance — Inscription Table **/
    public int getMaxStageGroups() { return this.maxStageGroups; }
    public int getMaxRecipeSize() { return this.maxRecipeSize; }

    /** Skill Point Progression config getters **/
    public int getSkillPointBlueMinLevel() { return this.skillPointBlueMinLevel; }
    public int getSkillPointBlueLevelsPerPoint() { return this.skillPointBlueLevelsPerPoint; }
    public int getSkillPointGreenMinLevel() { return this.skillPointGreenMinLevel; }
    public int getSkillPointGreenLevelsPerPoint() { return this.skillPointGreenLevelsPerPoint; }
    public int getSkillPointRedMinLevel() { return this.skillPointRedMinLevel; }
    public int getSkillPointRedLevelsPerPoint() { return this.skillPointRedLevelsPerPoint; }
    public int getSkillPointYellowMinLevel() { return this.skillPointYellowMinLevel; }
    public int getSkillPointYellowLevelsPerPoint() { return this.skillPointYellowLevelsPerPoint; }
    public int getSkillPointMagentaMinLevel() { return this.skillPointMagentaMinLevel; }
    public int getSkillPointMagentaLevelsPerPoint() { return this.skillPointMagentaLevelsPerPoint; }
    public int getSkillPointCyanMinLevel() { return this.skillPointCyanMinLevel; }
    public int getSkillPointCyanLevelsPerPoint() { return this.skillPointCyanLevelsPerPoint; }

    /** Affinity Ability Thresholds & Effects **/
    public float getAffinityAgileMinDepth() { return this.affinityAgileMinDepth; }
    public float getAffinityAgileJumpBoost() { return this.affinityAgileJumpBoost; }
    public float getAffinityColdBloodedMinDepth() { return this.affinityColdBloodedMinDepth; }
    public float getAffinityThornsMinDepth() { return this.affinityThornsMinDepth; }
    public int getAffinityThornsDamage1() { return this.affinityThornsDamage1; }
    public int getAffinityThornsDamage2() { return this.affinityThornsDamage2; }
    public int getAffinityThornsDamage3() { return this.affinityThornsDamage3; }
    public float getAffinityFulminationMinDepth() { return this.affinityFulminationMinDepth; }
    public float getAffinityFulminationLightningChance() { return this.affinityFulminationLightningChance; }

    /** Burnout Formula **/
    public float getManaBurnoutRatio() { return this.manaBurnoutRatio; }
    public int getBurnoutPerLevel() { return this.burnoutPerLevel; }
    public int getBurnoutBase() { return this.burnoutBase; }
    public int getAffinityHealCooldownFull() { return this.affinityHealCooldownFull; }
    public int getAffinityHealCooldownPartial() { return this.affinityHealCooldownPartial; }

    /** Flicker Generation Weights **/
    public int getFlickerWeightAir() { return this.flickerWeightAir; }
    public int getFlickerWeightArcane() { return this.flickerWeightArcane; }
    public int getFlickerWeightEarth() { return this.flickerWeightEarth; }
    public int getFlickerWeightEnder() { return this.flickerWeightEnder; }
    public int getFlickerWeightFire() { return this.flickerWeightFire; }
    public int getFlickerWeightIce() { return this.flickerWeightIce; }
    public int getFlickerWeightLife() { return this.flickerWeightLife; }
    public int getFlickerWeightLightning() { return this.flickerWeightLightning; }
    public int getFlickerWeightNature() { return this.flickerWeightNature; }
    public int getFlickerWeightWater() { return this.flickerWeightWater; }

    /** Beam Shape Tick Rates **/
    public int getBeamBlockTickRate() { return this.beamBlockTickRate; }
    public int getBeamEntityTickRate() { return this.beamEntityTickRate; }
    public int getChannelTickRate() { return this.channelTickRate; }

    /** Processing Block Costs **/
    public float getReconstructorRepairCost() { return this.reconstructorRepairCost; }
    public int getDeconstructorTime() { return this.deconstructorTime; }
    public float getDeconstructorPowerCost() { return this.deconstructorPowerCost; }
    
    public boolean getDisableManaElementalParticles() { return this.disableManaElementalParticles; }
}
