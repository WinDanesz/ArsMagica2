package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class BuffEffectFury extends AMPotion {

    public BuffEffectFury(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (!entity.world.isRemote) {
            entity.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200, 1));
            entity.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 1));
        }
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return false;
    }
}
