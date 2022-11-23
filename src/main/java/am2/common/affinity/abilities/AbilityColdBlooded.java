package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.buffs.BuffEffectFrostSlowed;
import am2.common.extensions.AffinityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
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
		return Affinity.ICE;
	}
	
	@Override
	public void applyTick(EntityPlayer player) {
		IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.iceAffinityColdBlooded, !AffinityAbilityModifiers.instance.isOnIce(player));
	}
	
	@Override
	public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
		if (!isAttacker && event.getSource().getTrueSource() instanceof EntityLivingBase){
			double iceDepth = AffinityData.For(player).getAffinityDepth(Affinity.ICE);
			BuffEffectFrostSlowed effect = new BuffEffectFrostSlowed(40, 0);
			if (iceDepth == 1.0f){
				effect = new BuffEffectFrostSlowed(200, 3);
			}else if (iceDepth >= 0.75f){
				effect = new BuffEffectFrostSlowed(160, 2);
			}else if (iceDepth >= 0.5f){
				effect = new BuffEffectFrostSlowed(100, 1);
			}
			if (effect != null && event.getSource() != null && event.getSource().getTrueSource() != null){
				try {
					((EntityLivingBase)event.getSource().getTrueSource()).addPotionEffect(effect);
				} catch (Exception ignored) {}
			}
		}
	}
	
	@Override
	public void removeEffects(EntityPlayer player) {
		IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.iceAffinityColdBlooded, false);
	}

}
