package am2.common.armor.imbuements;

import am2.api.extensions.IEntityExtension;
import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMPotions;
import am2.common.registry.ImbuementRegistry;
import am2.common.utils.MathUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.EnumSet;

public class FallProtection extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.FALL_PROTECTION;
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

        int distanceToGround = MathUtilities.getDistanceToGround(player, world);
        IEntityExtension extendedProperties = EntityExtension.For(player);
        if (player.fallDistance >= extendedProperties.getFallProtection() + 4f && distanceToGround < -8 * player.motionY) {
            if (!player.isPotionActive(AMPotions.slowfall) && !player.capabilities.isFlying) {

                PotionEffect potionEffect = new PotionEffect(AMPotions.slowfall, distanceToGround * 3, 1);
                player.addPotionEffect(potionEffect);

                stack.damageItem((int) (player.fallDistance * 6), player);

                player.fallDistance = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET};
    }

    @Override
    public boolean canApplyOnCooldown() {
        return false;
    }

    @Override
    public int getCooldown() {
        return 900;
    }

    @Override
    public int getArmorDamage() {
        return 0;
    }
}
