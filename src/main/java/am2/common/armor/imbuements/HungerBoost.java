package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;

public class HungerBoost extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.HUNGER_BOOST;
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
        if (player.getFoodStats().needFood()) {
            player.getFoodStats().addStats(1, 1.0f);
            return true;
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.HEAD};
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
        return 3;
    }
}
