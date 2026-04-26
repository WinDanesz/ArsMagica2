package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.defs.IDDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class BuffEffectHaste extends AMPotion {

    private static final AttributeModifier hasteSpeedBoost = (new AttributeModifier(IDDefs.hasteID, "Haste Speed Boost", 0.2D, 2));

    public BuffEffectHaste(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance attributeinstance = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (attributeinstance.getModifier(IDDefs.hasteID) != null) {
            attributeinstance.removeModifier(hasteSpeedBoost);
        }

        attributeinstance.applyModifier(new AttributeModifier(IDDefs.hasteID, "Haste Speed Boost", 0.2D + (0.35 * amplifier), 2));
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance attributeinstance = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (attributeinstance.getModifier(IDDefs.hasteID) != null) {
            attributeinstance.removeModifier(hasteSpeedBoost);
        }
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
