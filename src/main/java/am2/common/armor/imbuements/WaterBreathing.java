package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.registry.AMPotions;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;

public class WaterBreathing extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.WATER_BREATHING;
    }

    @Override
    public ImbuementTiers getTier() {
        return ImbuementTiers.FOURTH;
    }

    @Override
    public EnumSet<ImbuementApplicationTypes> getApplicationTypes() {
        return EnumSet.of(ImbuementApplicationTypes.ON_TICK);
    }

    @Override
    public boolean applyEffect(EntityPlayer player, World world, ItemStack stack, ImbuementApplicationTypes matchedType, Object... params) {
        if (world.isRemote)
            return false;

        if (player.getAir() < 10) {
            if (!player.isPotionActive(AMPotions.water_breathing)) {
                PotionEffect potionEffect = new PotionEffect(AMPotions.water_breathing, 200, 0);
                player.addPotionEffect(potionEffect);
                return true;
            }
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD};
    }

    @Override
    public boolean canApplyOnCooldown() {
        return false;
    }

    @Override
    public int getCooldown() {
        return 4000;
    }

    @Override
    public int getArmorDamage() {
        return 100;
    }
}
