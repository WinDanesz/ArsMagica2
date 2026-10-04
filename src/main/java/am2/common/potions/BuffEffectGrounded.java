package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.registry.AMPotions;
import net.minecraft.init.MobEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Prevents flight: creative-mode flight, Flight/Levitation buffs (AM2 and vanilla levitation) and elytra gliding.
 * Enforced every tick by revoking {@code allowFlying}/{@code isFlying}; creative flight is restored when the effect ends.
 */
public class BuffEffectGrounded extends AMPotion {

    public BuffEffectGrounded(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        // Strip AM2 flight/levitation and vanilla levitation from any entity.
        entity.removePotionEffect(AMPotions.flight);
        entity.removePotionEffect(AMPotions.levitation);
        entity.removePotionEffect(MobEffects.LEVITATION);

        if (entity instanceof EntityPlayerMP) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.isSpectator()) return;
            if (player.isElytraFlying()) {
                ((EntityPlayerMP) player).clearElytraFlying();
            }
            if (player.capabilities.allowFlying || player.capabilities.isFlying) {
                player.capabilities.allowFlying = false;
                player.capabilities.isFlying = false;
                player.sendPlayerAbilities();
            }
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, net.minecraft.entity.ai.attributes.AbstractAttributeMap map, int amplifier) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.isCreativeMode) {
                player.capabilities.allowFlying = true;
                player.sendPlayerAbilities();
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
}
