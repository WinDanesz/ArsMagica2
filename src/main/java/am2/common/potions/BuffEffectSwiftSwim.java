package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class BuffEffectSwiftSwim extends AMPotion {

    public BuffEffectSwiftSwim(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.isInWater()) {
            if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isFlying) {
                entity.motionX *= (1.133f + 0.03 * amplifier);
                entity.motionZ *= (1.133f + 0.03 * amplifier);

                if (entity.motionY > 0) {
                    entity.motionY *= 1.134;
                }
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
