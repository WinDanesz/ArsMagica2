package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectWaterBreathing extends AMPotion {

    public BuffEffectWaterBreathing(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.isInWater()) {
            entity.setAir(300); // Max air
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
