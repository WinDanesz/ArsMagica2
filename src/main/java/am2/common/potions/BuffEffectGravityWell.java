package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectGravityWell extends AMPotion {

    public BuffEffectGravityWell(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.motionY < 0) {
            entity.motionY *= 2;
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
