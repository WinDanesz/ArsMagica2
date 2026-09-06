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

/**
 * Standing your ground: knockback resistance ramps linearly from none at the minimum qualifying
 * depth up to full immunity at 100% depth.
 * <p>
 * Vanilla's KNOCKBACK_RESISTANCE isn't a smooth per-hit reduction — per EntityLivingBase#knockBack,
 * each hit rolls {@code rand.nextDouble() >= resistance} and either applies knockback at full
 * strength or not at all. So the "percent" here is really a per-hit chance to be completely
 * unmoved, which averages out to that fraction of knockback cancelled over many hits, reaching a
 * guaranteed negation at 100% depth (1.0 resistance).
 */
public class AbilityImmovable extends AbstractAffinityAbility {

    public AbilityImmovable() {
        super(new ResourceLocation("arsmagica2", "immovable"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityImmovableMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.earth;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
        applyResistance(attribute, computeResistance(depth));
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
        AttributeModifier existing = attribute.getModifier(AffinityAbilityModifiers.earthAffinityKnockbackResistanceID);
        if (existing != null) attribute.removeModifier(existing);
    }

    // Ramps linearly from 0 at the minimum depth up to the configured cap at 100% depth, same shape
    // as AbilityReflexes's speed bonus and AbilityRimeguard's shield cap.
    private double computeResistance(double depth) {
        float minDepth = ArsMagica.config.getAffinityImmovableMinDepth();
        float maxResistance = ArsMagica.config.getAffinityImmovableMaxResistance();
        float span = Math.max(1e-4f, 1f - minDepth);
        double progress = Math.min(1.0, Math.max(0.0, (depth - minDepth) / span));
        return progress * maxResistance;
    }

    // AttributeModifier has no in-place amount update, so a changed value means removing the old
    // modifier and applying a fresh one under the same UUID (see AbilityReflexes for the same pattern).
    //
    // Deliberately uses the literal 0 rather than AffinityAbilityModifiers.OPERATION_ADD: per vanilla's
    // ModifiableAttributeInstance#computeValue, operation 0 is true addition (base += amount), while
    // OPERATION_ADD is actually defined as 2, which is MULTIPLY_TOTAL (total *= 1 + amount). That's
    // silently wrong for every ability using it, but only fatally so here — knockback resistance's
    // base value is 0, and multiplying 0 by anything is still 0, so this modifier's amount was being
    // discarded outright regardless of depth. The other abilities' non-zero base values are why their
    // use of the same mislabeled constant still produces *some* effect, just at the wrong magnitude.
    private static final int TRUE_ADDITION = 0;

    private void applyResistance(IAttributeInstance attribute, double resistance) {
        AttributeModifier existing = attribute.getModifier(AffinityAbilityModifiers.earthAffinityKnockbackResistanceID);
        if (existing != null) {
            if (existing.getAmount() == resistance) return;
            attribute.removeModifier(existing);
        }
        if (resistance > 0) {
            attribute.applyModifier(new AttributeModifier(AffinityAbilityModifiers.earthAffinityKnockbackResistanceID, "Immovable", resistance, TRUE_ADDITION));
        }
    }
}
