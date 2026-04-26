package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

import java.util.UUID;

public class BuffEffectFrostSlowed extends AMPotion {

    private static final UUID frostSlowID = UUID.fromString("03B0A79B-9569-43AE-BFE3-820D993D4A64");

    public BuffEffectFrostSlowed(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance attributeinstance = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (attributeinstance.getModifier(frostSlowID) != null) {
            attributeinstance.removeModifier(attributeinstance.getModifier(frostSlowID));
        }

        attributeinstance.applyModifier(new AttributeModifier(frostSlowID, "Frost Slow", -0.2 - (0.3 * amplifier), 2));
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance attributeinstance = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);

        if (attributeinstance.getModifier(frostSlowID) != null) {
            attributeinstance.removeModifier(attributeinstance.getModifier(frostSlowID));
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
