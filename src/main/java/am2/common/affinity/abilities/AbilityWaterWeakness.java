package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.registry.Affinities;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityWaterWeakness extends AbstractAffinityAbility {

    public String affinity;

    public AbilityWaterWeakness(String affinity) {
        super(new ResourceLocation("arsmagica2", "waterweakness_" + affinity));
        this.affinity = affinity;
    }

    @Override
    public float getMinimumDepth() {
        return 0.5f;
    }

    @Override
    public float getMaximumDepth() {
        return 0.9f;
    }

    @Override
    public Affinity getAffinity() {
        // ugly but works
        switch (this.affinity) {
            case "ender":
                return Affinities.ender;
            case "lightning":
                return Affinities.lightning;
            case "fire":
                return Affinities.fire;
        }
        return Affinities.water;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.waterWeakness, player.isWet());
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.waterWeakness, false);
    }

}
