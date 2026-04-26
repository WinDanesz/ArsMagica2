package am2.common.potions;

import am2.common.defs.AMPotion;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class BuffEffectFlight extends AMPotion {

    public BuffEffectFlight(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap, int amplifier) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayer player = (EntityPlayer) entity;
            player.capabilities.allowFlying = true;
            player.sendPlayerAbilities();
        }
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayer player = (EntityPlayer) entity;
            player.capabilities.allowFlying = true;
            player.sendPlayerAbilities();
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap attributeMap, int amplifier) {
        if (entity instanceof EntityPlayerMP) {
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
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}