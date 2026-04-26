package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectLeap extends AMPotion {

    public BuffEffectLeap(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // Leap logic handled elsewhere
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
