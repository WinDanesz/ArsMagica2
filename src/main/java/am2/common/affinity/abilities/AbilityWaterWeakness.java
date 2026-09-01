package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.affinity.AffinityAbilityModifiers;
import am2.common.registry.Affinities;
import net.minecraft.block.material.Material;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityWaterWeakness extends AbstractAffinityAbility {

    // Grace period (in ticks) the malus is kept after the player is no longer wet, so bobbing
    // at the water's surface doesn't yank the debuff on/off every tick. Applying is still instant.
    private static final int GRACE_PERIOD_TICKS = 20;

    public String affinity;
    private final Map<UUID, Integer> graceTicksRemaining = new HashMap<>();

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

    // Each affinity variant gets its own modifier: these instances are ticked independently
    // by AffinityAbilityHelper, and a shared modifier gets ripped out by whichever sibling
    // instance is currently ineligible, fighting the one that IS eligible every tick.
    private AttributeModifier getModifier() {
        return switch (this.affinity) {
            case "fire" -> AffinityAbilityModifiers.waterWeaknessFire;
            case "lightning" -> AffinityAbilityModifiers.waterWeaknessLightning;
            default -> AffinityAbilityModifiers.waterWeakness;
        };
    }

    // player.isWet()/inWater is only refreshed when the server reprocesses a movement packet
    // from the client; a client that's holding perfectly still stops sending those, so the
    // cached flag goes stale. Query the block material at the player's actual position instead,
    // matching vanilla's own handleWaterMovement() check but independent of movement packets.
    private boolean isSubmergedInWater(EntityPlayer player) {
        return player.world.isMaterialInBB(player.getEntityBoundingBox().grow(0.0D, -0.4000000059604645D, 0.0D).shrink(0.001D), Material.WATER)
                || player.isWet();
    }

    @Override
    public void applyTick(EntityPlayer player) {
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        UUID id = player.getUniqueID();
        if (isSubmergedInWater(player)) {
            graceTicksRemaining.put(id, GRACE_PERIOD_TICKS);
        } else {
            int remaining = graceTicksRemaining.getOrDefault(id, 0);
            if (remaining > 0)
                graceTicksRemaining.put(id, remaining - 1);
        }
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, getModifier(), graceTicksRemaining.getOrDefault(id, 0) > 0);
    }

    @Override
    public void removeEffects(EntityPlayer player) {
        graceTicksRemaining.remove(player.getUniqueID());
        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        AffinityAbilityModifiers.instance.applyOrRemoveModifier(attribute, getModifier(), false);
    }

}
