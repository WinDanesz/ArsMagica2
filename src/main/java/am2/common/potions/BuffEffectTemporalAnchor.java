package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectTemporalAnchor extends AMPotion {

    // Note: Position/rotation/health/mana must be stored in PotionEffect NBT
    // This is a singleton Potion class, instance state won't work
    // The spell that applies this needs to store the anchor data in the PotionEffect's NBT

    public BuffEffectTemporalAnchor(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        // Store anchor values in the PotionEffect NBT when applying
        // This needs to be handled by the spell/code that creates the PotionEffect
        // TODO: Implement proper NBT-based anchor storage in PotionEffect wrapper
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        // Restore position from PotionEffect NBT when removing
        // This needs custom PotionEffect handling to store/retrieve the anchor data
        // TODO: Implement proper NBT-based anchor restoration in PotionEffect wrapper
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
