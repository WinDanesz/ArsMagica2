package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.utils.EntityUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectCharmed extends AMPotion {

    public static final int CHARM_TO_PLAYER = 1;
    public static final int CHARM_TO_MONSTER = 2;

    // Note: Charmer entity must be stored in PotionEffect NBT by the spell that applies this
    // This is a singleton Potion class, instance state won't work

    public BuffEffectCharmed(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        // Charmer must be retrieved from PotionEffect NBT in the code that applies this potion
        // For now, this logic needs to be handled externally where the charm is applied
        // TODO: Implement proper NBT-based charmer storage in PotionEffect wrapper
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        if (entity instanceof EntityCreature) {
            EntityUtils.revertAI((EntityCreature) entity);
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
