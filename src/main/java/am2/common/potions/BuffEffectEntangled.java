package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectEntangled extends AMPotion {

    public BuffEffectEntangled(boolean isBad, int color) {
        super(isBad, color);
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
