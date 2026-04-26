package am2.common.spell;

import am2.ArsMagica;
import am2.api.extensions.IEntityExtension;
import am2.api.spell.SpellData;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMPotions;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A SpellCaster variant used by the Magic Staff. It executes the spell normally
 * but skips mana deduction, burnout, and reagent checks. Durability cost is
 * handled at the item level ({@link am2.common.items.ItemStaff}).
 */
public class StaffSpellCaster extends SpellCaster {

    @Override
    public boolean cast(ItemStack source, World world, EntityLivingBase caster) {
        if (caster.isPotionActive(AMPotions.silence)) return false;
        if (EBWizardryCompatBootstrap.isArcaneJammed(caster)) return false;

        SpellData data = this.createSpellData(source);
        SpellCastResult result = data.execute(world, caster);
        if (result == SpellCastResult.SUCCESS || result == SpellCastResult.FREE_CAST) {
            if (!ArsMagica.config.getOldXpCalculations()) {
                IEntityExtension ext = EntityExtension.For(caster);
                ext.addMagicXP(AffinityShiftUtils.calculateXPGains(caster, data));
            }
            return true;
        }
        return false;
    }
}
