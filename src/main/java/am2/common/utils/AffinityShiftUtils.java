package am2.common.utils;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.event.AffinityChangingEvent;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.spell.*;
import am2.common.armor.ArmorHelper;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.registry.AMSkills;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class AffinityShiftUtils {

    public static void doAffinityShift(EntityLivingBase caster, SpellComponent component, SpellShape governingShape) {
        if (!(caster instanceof EntityPlayer)) return;
        IAffinityData aff = AffinityData.For(caster);
        Set<Affinity> affList = component.getAffinity();
        boolean oldCalculations = ArsMagica.config.getOldXpCalculations();
        for (Affinity affinity : affList) {
            float shift = component.getAffinityShift(affinity) * aff.getDiminishingReturnsFactor() * 5;
            float xp = 0.05f * aff.getDiminishingReturnsFactor();
            if (governingShape.isChanneled()) {
                shift /= 4;
                xp /= 4;
            }

            if (caster instanceof EntityPlayer) {
                if (SkillData.For(caster).hasSkill(AMSkills.affinity_gains.getID())) {
                    shift *= 1.1f;
                }
                
                if (caster.getAttributeMap() != null && caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.affinityGainModifier) != null) {
                    shift *= (float) caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.affinityGainModifier).getAttributeValue();
                }
                ItemStack chestArmor = caster.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
                if (!chestArmor.isEmpty() && ArmorHelper.isInfusionPreset(chestArmor, ImbuementRegistry.MAGIC_XP))
                    xp *= 1.25f;
            }

            if (shift > 0) {
                AffinityChangingEvent event = new AffinityChangingEvent((EntityPlayer) caster, affinity, shift);
                MinecraftForge.EVENT_BUS.post(event);
                if (!event.isCanceled())
                    aff.incrementAffinity(affinity, event.amount);
            }
            if (oldCalculations && xp > 0) {
                xp *= caster.getAttributeMap().getAttributeInstance(ArsMagicaAPI.xpGainModifier).getAttributeValue();
                EntityExtension.For(caster).addMagicXP(xp);
            }
        }
        aff.addDiminishingReturns(governingShape.isChanneled());
    }

    public static ItemStack getEssenceForAffinity(Affinity affinity) {
        Item essenceItem = affinity.getEssenceItem();
        return new ItemStack(essenceItem);
    }

    public static float calculateXPGains(EntityLivingBase caster, SpellData data) {
        IEntityExtension extension = EntityExtension.For(caster);
        float cost = 0F;
        float multiplier = 0.1F;
        float stageMultiplier = 1.0F;
        for (List<SpellPart> parts : data.getStages()) {
            float _cost = 0F;
            float _multiplier = stageMultiplier;
            parts.sort(Comparator.naturalOrder());
            for (SpellPart part : parts) {
                float __multiplier = 0.1F;
                Skill skill = ArsMagicaAPI.getSkillRegistry().getValue(part.getRegistryName());
                if (skill != null) {
                    SkillPoint point = skill.getPoint();
                    if (point.getTier() > 0) {
                        multiplier *= extension.getCurrentLevel() > point.getMinEarnLevel() + 10 ? Math.max(1F / (extension.getCurrentLevel() - point.getMinEarnLevel() - 10F), 0.1F) : 1F;
                    } else
                        multiplier *= 2F;
                }
                if (part instanceof SpellModifier) {
                    multiplier *= (1 + __multiplier);
                } else if (part instanceof SpellShape) {
                    _multiplier *= 10 * __multiplier * (((SpellShape) part).isChanneled() ? 0.1F : 1F);
                    stageMultiplier = 1.0F;
                } else if (part instanceof SpellComponent) {
                    _cost += 10 * Math.log(((SpellComponent) part).getEffectiveMana()) * __multiplier;
                }
            }

            cost += _cost * _multiplier;
        }

        ItemStack chestArmor = caster.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chestArmor.isEmpty() && ArmorHelper.isInfusionPreset(chestArmor, ImbuementRegistry.MAGIC_XP))
            multiplier *= 1.5f;
        return cost * multiplier * 0.05F;
    }
}
