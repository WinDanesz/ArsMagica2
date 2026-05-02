package am2.common.potions;

import am2.common.defs.AMPotion;
import am2.common.defs.IDDefs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;

public class BuffEffectFury extends AMPotion {

    private static final AttributeModifier furyMoveMod =
            new AttributeModifier(IDDefs.furyMoveID, "Fury (Movement)", 2, 2).setSaved(false);
    private static final AttributeModifier furyDmgMod =
            new AttributeModifier(IDDefs.furyDmgID, "Fury (Damage)", 5, 2).setSaved(false);

    public BuffEffectFury(boolean isBad, int color) {
        super(isBad, color);
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance moveInst = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (moveInst != null && moveInst.getModifier(IDDefs.furyMoveID) == null) {
            moveInst.applyModifier(furyMoveMod);
        }
        IAttributeInstance dmgInst = map.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (dmgInst != null && dmgInst.getModifier(IDDefs.furyDmgID) == null) {
            dmgInst.applyModifier(furyDmgMod);
        }
    }

    @Override
    public void removeAttributesModifiersFromEntity(EntityLivingBase entity, AbstractAttributeMap map, int amplifier) {
        IAttributeInstance moveInst = map.getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);
        if (moveInst != null) moveInst.removeModifier(IDDefs.furyMoveID);
        IAttributeInstance dmgInst = map.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
        if (dmgInst != null) dmgInst.removeModifier(IDDefs.furyDmgID);
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
