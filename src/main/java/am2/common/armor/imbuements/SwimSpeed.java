package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.registry.AMPotions;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;

public class SwimSpeed extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.SWIM_SPEED;
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

        if (player.isInsideOfMaterial(Material.WATER) && !player.isPotionActive(AMPotions.swift_swim)) {
            player.addPotionEffect(new PotionEffect(AMPotions.swift_swim, 10, 1));
            return true;
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.LEGS};
    }

    @Override
    public boolean canApplyOnCooldown() {
        return true;
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public int getArmorDamage() {
        return 0;
    }
}
