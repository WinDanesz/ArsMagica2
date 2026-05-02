package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectAgility extends AMPotion {

    public BuffEffectAgility(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.setAIMoveSpeed(entity.getAIMoveSpeed() * 1.2f);
        entity.stepHeight = 1.01f;
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (entity.stepHeight == 1.01f) {
            entity.stepHeight = 0.6f;
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // Every tick
    }
}
