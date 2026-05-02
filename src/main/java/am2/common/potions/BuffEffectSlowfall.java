package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectSlowfall extends AMPotion {

    public BuffEffectSlowfall(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.setPosition(entity.posX, entity.posY + (entity.fallDistance / 1.1), entity.posZ);
        entity.fallDistance = 0;
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
