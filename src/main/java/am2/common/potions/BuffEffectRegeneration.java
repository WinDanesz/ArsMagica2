package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectRegeneration extends AMPotion {

    public BuffEffectRegeneration(boolean isBad, int color) {
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
        if (!entity.world.isRemote) {
            entity.heal(1);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        double ticks = 80 / Math.pow(2, amplifier);
        return duration > 0 && (duration % ticks) == 0;
    }
}
