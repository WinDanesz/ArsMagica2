package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.spell.SpellData;
import am2.common.entity.EntityAirElemental;
import am2.common.entity.EntityEarthElemental;
import am2.common.entity.EntityFireElemental;
import am2.common.entity.EntityIceElemental;
import am2.common.entity.EntityLightningElemental;
import am2.common.entity.EntityWaterElemental;
import am2.common.extensions.AffinityData;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

/**
 * A more specialized cousin of {@link Summon}: instead of binding to a specific creature via a
 * filled Crystal Phylactery, it conjures an elemental matching the caster's own strongest
 * affinity, provided that affinity is deep enough.
 */
public class Elemental extends Summon {

    public static final float MIN_AFFINITY_DEPTH = 0.25f;

    @Override
    protected Entity createSummonEntity(SpellData spell, EntityLivingBase caster, World world) {
        if (!(caster instanceof EntityPlayer)) return null;
        EntityPlayer player = (EntityPlayer) caster;

        AffinityData affinityData = AffinityData.For(player);
        Affinity highest = affinityData.getHighestAffinities()[0];

        if (highest == Affinities.none || affinityData.getAffinityDepth(highest) < MIN_AFFINITY_DEPTH) {
            player.sendStatusMessage(new TextComponentTranslation("am2.tooltip.elementalInsufficientAffinity"), false);
            return null;
        }

        if (highest == Affinities.fire) return new EntityFireElemental(world);
        if (highest == Affinities.water) return new EntityWaterElemental(world);
        if (highest == Affinities.air) return new EntityAirElemental(world);
        if (highest == Affinities.earth) return new EntityEarthElemental(world);
        if (highest == Affinities.ice) return new EntityIceElemental(world);
        if (highest == Affinities.lightning) return new EntityLightningElemental(world);

        player.sendStatusMessage(new TextComponentTranslation("am2.tooltip.elementalNoAffinityForm"), false);
        return null;
    }

    @Override
    public Object[] getRecipe() {
        // Same reagents as Summon, minus the Crystal Phylactery - the creature is chosen by the
        // caster's affinity at cast time rather than bound to it ahead of time.
        return new Object[]{
                new ItemStack(AMItems.chimerite),
                new ItemStack(AMItems.purified_vinteum_dust),
                AMBlocks.cerublossom,
                AMItems.mob_focus,
                "E:" + PowerTypes.DARK.ID(), 1500
        };
    }
}
