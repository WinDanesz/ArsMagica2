package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;

public class BuffEffectLevitation extends AMPotion {

    public BuffEffectLevitation(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (entity instanceof EntityPlayer) {
            ((EntityPlayer) entity).capabilities.allowFlying = true;
            ((EntityPlayer) entity).sendPlayerAbilities();
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (!player.capabilities.isCreativeMode) {
                player.capabilities.allowFlying = false;
                player.capabilities.isFlying = false;
                player.fallDistance = 0f;
                player.sendPlayerAbilities();
            }
        }
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity instanceof EntityPlayer) {
            if (((EntityPlayer) entity).capabilities.isFlying) {
                float factor = 0.4f;
                entity.motionX *= factor;
                entity.motionZ *= factor;
                entity.motionY *= 0.0001f;
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true; // Every tick
    }
}
