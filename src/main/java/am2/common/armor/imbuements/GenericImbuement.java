package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.EnumSet;

public class GenericImbuement extends ArmorImbuement {

    private String id = "";
    private ImbuementTiers tier;
    private EntityEquipmentSlot[] validSlots;

    public GenericImbuement(String id, ImbuementTiers tier, EntityEquipmentSlot[] validSlots) {
        this.id = id;
        this.tier = tier;
        this.validSlots = validSlots;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ImbuementTiers getTier() {
        return tier;
    }

    @Override
    public EnumSet<ImbuementApplicationTypes> getApplicationTypes() {
        return EnumSet.of(ImbuementApplicationTypes.NONE);
    }

    @Override
    public boolean applyEffect(EntityPlayer player, World world, ItemStack stack, ImbuementApplicationTypes matchedType, Object... params) {
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return validSlots;
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
