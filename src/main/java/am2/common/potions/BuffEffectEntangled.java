package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.defs.IDDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;

public class BuffEffectEntangled extends AMPotion {

    private static final AttributeModifier entangledMod =
            new AttributeModifier(IDDefs.entangledID, "Entangled", -10, 2).setSaved(false);

    public BuffEffectEntangled(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance inst = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (inst != null && inst.getModifier(IDDefs.entangledID) == null) {
            inst.applyModifier(entangledMod);
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance inst = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (inst != null) {
            inst.removeModifier(IDDefs.entangledID);
        }
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.motionX = 0f;
        entity.motionY = 0f;
        entity.motionZ = 0f;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // Every tick
    }
}
