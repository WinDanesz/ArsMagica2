package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.registry.AMBlocks;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;

public class BuffEffectIllumination extends AMPotion {

    public BuffEffectIllumination(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (!entity.world.isRemote && entity.ticksExisted % 10 == 0) {
            if (entity.world.isAirBlock(entity.getPosition()) && entity.world.getLight(entity.getPosition()) < 7) {
                entity.world.setBlockState(entity.getPosition(), AMBlocks.invisible_light.getDefaultState());
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        return duration % 10 == 0; // Every 10 ticks
    }
}
