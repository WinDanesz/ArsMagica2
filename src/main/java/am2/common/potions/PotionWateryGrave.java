package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;

public class PotionWateryGrave extends AMPotion {

    public PotionWateryGrave(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // Pulling down effect is handled in EntityHandler
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // Every tick
    }
}
