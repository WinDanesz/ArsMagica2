package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.extensions.AffinityData;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityColdBlooded extends AbstractAffinityAbility {

    public AbilityColdBlooded() {
        super(new ResourceLocation("arsmagica2", "coldblooded"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.1f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.ice;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.iceAffinityColdBlooded, !AffinityAbilityModifiers.instance.isOnIce(player));
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker && event.getSource().getTrueSource() instanceof EntityLivingBase) {
            double iceDepth = AffinityData.For(player).getAffinityDepth(Affinities.ice);
            int duration = 40;
            int amplifier = 0;
            if (iceDepth == 1.0f) {
                duration = 200;
                amplifier = 3;
            } else if (iceDepth >= 0.75f) {
                duration = 16;
                amplifier = 2;
            } else if (iceDepth >= ArsMagica.config.getAffinityColdBloodedMinDepth()) {
                duration = 100;
                amplifier = 2;
            }
            if (event.getSource() != null && event.getSource().getTrueSource() != null) {
                PotionEffect effect = new PotionEffect(AMPotions.frost_slow, duration, amplifier);
                ((EntityLivingBase) event.getSource().getTrueSource()).addPotionEffect(effect);
            }
        }
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.iceAffinityColdBlooded, false);
    }

}
