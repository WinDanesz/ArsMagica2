package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.registry.Affinities;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilitySunlightWeakness extends AbstractAffinityAbility {

    // Grace period (in ticks) the malus is kept after sky visibility goes false, so passing
    // under a canopy gap doesn't yank the debuff on/off every tick. Applying is still instant.
    private static final int GRACE_PERIOD_TICKS = 20;

    private final Map<UUID, Integer> graceTicksRemaining = new HashMap<>();

    public AbilitySunlightWeakness() {
        super(new ResourceLocation("arsmagica2", "sunlightweakness"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.65f;
    }

    @Override
    public float getMaximumDepth() {
        return 0.95f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.ender;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        int worldTime = (int) player.world.getWorldTime() % 24000;
        boolean isSunlit = player.world.canBlockSeeSky(player.getPosition()) && (worldTime > 23000 || worldTime < 12500);
        UUID id = player.getUniqueID();
        if (isSunlit) {
            graceTicksRemaining.put(id, GRACE_PERIOD_TICKS);
        } else {
            int remaining = graceTicksRemaining.getOrDefault(id, 0);
            if (remaining > 0)
                graceTicksRemaining.put(id, remaining - 1);
        }
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.sunlightWeakness, graceTicksRemaining.getOrDefault(id, 0) > 0);
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        graceTicksRemaining.remove(player.getUniqueID());
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, AffinityAbilityModifiers.sunlightWeakness, false);
    }

}
