package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityRooted extends AbstractAffinityAbility {

    public AbilityRooted() {
        super(new ResourceLocation("arsmagica2", "rooted"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.5f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.nature;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.natureAffinityRoots, true);

        // At full depth, your roots let you climb: pressing into a wall lifts you up it,
        // and resets fall distance so you can safely let go. Sneak to slide down it instead.
        if (AffinityData.For(player).getAffinityDepth(getAffinity()) >= 1f && player.collidedHorizontally) {
            if (!player.isSneaking()) {
                float movement = EntityExtension.For(player).getIsFlipped() ? -0.25f : 0.25f;
                player.move(MoverType.PLAYER, 0, movement, 0);
                player.motionY = 0;
            } else {
                player.motionY *= 0.79999999;
            }
            player.fallDistance = 0;
        }
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.natureAffinityRoots, false);
    }

}
