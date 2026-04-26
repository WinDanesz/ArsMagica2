package am2.common.enchantments;

import am2.ArsMagica;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;

public class EnchantMagicResist extends Enchantment {

    public EnchantMagicResist(Rarity rarity) {
        super(rarity, EnumEnchantmentType.ARMOR, new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD});
        setName("magicresist");
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    /**
     * Returns the Enchantment Protection Factor (EPF) contributed by this enchantment for the given damage source.
     * Minecraft sums EPF across all equipped armor pieces and reduces damage by EPF * 4% (capped at 25 EPF = 100%).
     * Returning {@code level} here replicates the original 4%-per-level-per-piece formula and also applies
     * to any magic-typed damage, including ElectrobloBs Wizardry spells that call {@code setMagicDamage()}.
     * Additional damage type strings can be registered via the {@code magic_resist_extra_damage_types} config option.
     */
    @Override
    public int calcModifierDamage(int level, DamageSource source) {
        if (source.isMagicDamage()) return level;
        if (ArsMagica.config != null && ArsMagica.config.getMagicResistExtraDamageTypes().contains(source.getDamageType())) return level;
        return 0;
    }

}
