package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class BuffEffectAgility extends AMPotion {

    public BuffEffectAgility(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        entity.setAIMoveSpeed(entity.getAIMoveSpeed() * 1.2f);
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // Every tick
    }
}
