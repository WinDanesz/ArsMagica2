package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityReflexes extends AbstractAffinityAbility {

    public AbilityReflexes() {
        super(new ResourceLocation("arsmagica2", "reflexes"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityReflexesMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.lightning;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
        applySpeedBonus(attribute, computeSpeedBonus(depth));
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AttributeModifier existing = attribute.getModifier(AffinityAbilityModifiers.lightningAffinityModifierID);
        if (existing != null) attribute.removeModifier(existing);
    }

    // Ramps linearly from 0 at the minimum depth up to the configured cap at 100% depth,
    // instead of the old flat bonus that switched fully on at the threshold.
    private double computeSpeedBonus(double depth) {
        float minDepth = ArsMagica.config.getAffinityReflexesMinDepth();
        float maxBonus = ArsMagica.config.getAffinityReflexesSpeedBonus();
        float span = Math.max(1e-4f, 1f - minDepth);
        double progress = Math.min(1.0, Math.max(0.0, (depth - minDepth) / span));
        return progress * maxBonus;
    }

    // AttributeModifier has no in-place amount update, so a changed bonus means removing the
    // old modifier and applying a fresh one under the same UUID. Skipped when the amount hasn't
    // changed (depth only moves on spell casts, not every tick) to avoid needless attribute churn.
    private void applySpeedBonus(IAttributeInstance attribute, double bonus) {
        AttributeModifier existing = attribute.getModifier(AffinityAbilityModifiers.lightningAffinityModifierID);
        if (existing != null) {
            if (existing.getAmount() == bonus) return;
            attribute.removeModifier(existing);
        }
        if (bonus > 0) {
            attribute.applyModifier(new AttributeModifier(AffinityAbilityModifiers.lightningAffinityModifierID, "Lightning Reflexes", bonus, AffinityAbilityModifiers.OPERATION_ADD));
        }
    }

}
