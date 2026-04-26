package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectSilence extends AMPotion {

    public BuffEffectSilence(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // Silence logic handled via checks elsewhere
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
