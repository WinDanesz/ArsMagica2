package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Hardens bare hands into an effective mining tool against stone and wood: bare-hand speed against
 * those materials ramps linearly from vanilla (1.0) at the minimum depth up to stone pickaxe/axe
 * efficiency at the configured "stone" depth, then continues ramping up to a configurable bonus
 * beyond stone efficiency at 100% depth.
 * <p>
 * Once that stone-tool-equivalent depth is reached, bare hands also count as the correct tool for
 * harvesting (see {@link #applyHarvestCheck}) — without that, Forge's own break-speed formula
 * (ForgeHooks#blockStrength) divides dig speed by {@code hardness * 100} instead of
 * {@code hardness * 30} for a block the game doesn't think is being harvested correctly, on top of
 * dropping nothing, so a bare-handed speed boost alone would still mine ~3.3x slower than a real
 * stone tool and destroy stone blocks without drops.
 */
public class AbilityHardening extends AbstractAffinityAbility {

    public AbilityHardening() {
        super(new ResourceLocation("arsmagica2", "hardening"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityHardeningMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.earth;
    }

    @Override
    public void applyBreakSpeed(EntityPlayer player, PlayerEvent.BreakSpeed event) {
        if (!player.getHeldItemMainhand().isEmpty()) return;

        Material material = event.getState().getMaterial();
        if (material != Material.ROCK && material != Material.WOOD) return;

        double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
        float handEfficiency = computeHandEfficiency((float) depth);

        // originalSpeed already has vanilla's hand baseline of 1.0 folded into any haste/mining
        // fatigue/underwater/airborne multipliers (see EntityPlayer#getDigSpeed), so multiplying by
        // the efficiency here scales it exactly as a real tool with that efficiency would, rather
        // than clobbering those situational multipliers with a flat override.
        event.setNewSpeed(event.getOriginalSpeed() * handEfficiency);
    }

    @Override
    public void applyHarvestCheck(EntityPlayer player, PlayerEvent.HarvestCheck event) {
        if (event.canHarvest()) return;
        if (!player.getHeldItemMainhand().isEmpty()) return;

        Material material = event.getTargetBlock().getMaterial();
        if (material != Material.ROCK && material != Material.WOOD) return;

        // Capped at stone's own harvest level: hands are only ever as good as a stone tool, so
        // anything needing an iron+ pickaxe (iron/diamond/redstone ore, obsidian, ...) still
        // correctly requires a real tool instead of becoming hand-mineable at high depth.
        if (event.getTargetBlock().getBlock().getHarvestLevel(event.getTargetBlock()) > Item.ToolMaterial.STONE.getHarvestLevel())
            return;

        double depth = AffinityData.For(player).getAffinityDepth(getAffinity());
        if (depth >= ArsMagica.config.getAffinityHardeningStoneDepth()) {
            event.setCanHarvest(true);
        }
    }

    private float computeHandEfficiency(float depth) {
        float minDepth = ArsMagica.config.getAffinityHardeningMinDepth();
        float stoneDepth = ArsMagica.config.getAffinityHardeningStoneDepth();
        float maxBonusMultiplier = ArsMagica.config.getAffinityHardeningMaxBonusMultiplier();
        float stoneEfficiency = Item.ToolMaterial.STONE.getEfficiency();

        if (depth <= minDepth) return 1.0f;

        if (depth < stoneDepth) {
            float span = Math.max(1e-4f, stoneDepth - minDepth);
            float progress = Math.min(1f, (depth - minDepth) / span);
            return 1.0f + progress * (stoneEfficiency - 1.0f);
        }

        float span = Math.max(1e-4f, 1f - stoneDepth);
        float progress = Math.min(1f, (depth - stoneDepth) / span);
        return stoneEfficiency + progress * (stoneEfficiency * (maxBonusMultiplier - 1f));
    }
}
