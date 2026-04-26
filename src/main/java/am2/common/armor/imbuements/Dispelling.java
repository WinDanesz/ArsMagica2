package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;

public class Dispelling extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.DISPELLING;
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
        if (player.getActivePotionEffects().isEmpty())
            return false;

        if (player.world.isRemote)
            return false;

        ArrayList<Potion> effectsToRemove = new ArrayList<>();
        Object[] safeCopy = player.getActivePotionEffects().toArray();
        for (Object o : safeCopy) {
            PotionEffect pe = (PotionEffect) o;
            boolean badEffect = pe.getPotion().isBadEffect();
            if (pe.getIsAmbient() || !badEffect) continue;
            effectsToRemove.add(pe.getPotion());
        }

        for (Potion i : effectsToRemove) {
            player.removePotionEffect(i);
        }
        return !effectsToRemove.isEmpty();
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.LEGS};
    }

    @Override
    public boolean canApplyOnCooldown() {
        return false;
    }

    @Override
    public int getCooldown() {
        return 600;
    }

    @Override
    public int getArmorDamage() {
        return 75;
    }
}
