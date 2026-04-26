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

public class Healing extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.HEALING;
    }

    @Override
    public ImbuementTiers getTier() {
        return ImbuementTiers.FOURTH;
    }

    @Override
    public EnumSet<ImbuementApplicationTypes> getApplicationTypes() {
        return EnumSet.of(ImbuementApplicationTypes.ON_HIT);
    }

    @Override
    public boolean applyEffect(EntityPlayer player, World world, ItemStack stack, ImbuementApplicationTypes matchedType, Object... params) {

        if (world.isRemote)
            return false;

        if (player.getHealth() < (player.getMaxHealth() * 0.25f)) {
            player.addPotionEffect(new PotionEffect(AMPotions.regeneration, 240, 2));
            return true;
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.CHEST};
    }

    @Override
    public boolean canApplyOnCooldown() {
        return false;
    }

    @Override
    public int getCooldown() {
        return 6400;
    }

    @Override
    public int getArmorDamage() {
        return 30;
    }
}
