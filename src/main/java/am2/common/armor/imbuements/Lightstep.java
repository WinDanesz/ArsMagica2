package am2.common.armor.imbuements;

import am2.api.items.armor.ArmorImbuement;
import am2.api.items.armor.ImbuementApplicationTypes;
import am2.api.items.armor.ImbuementTiers;
import am2.common.registry.AMBlocks;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.EnumSet;

public class Lightstep extends ArmorImbuement {

    @Override
    public String getID() {
        return ImbuementRegistry.LIGHTSTEP;
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

        if (player.isSneaking())
            return false;
        BlockPos pos = player.getPosition().up();
        int ll = world.getLightFor(EnumSkyBlock.BLOCK, pos);
        if (ll < 7 && world.isAirBlock(pos)) {
            world.setBlockState(pos, AMBlocks.block_mage_light.getDefaultState(), 2);
            return true;
        }
        return false;
    }

    @Override
    public EntityEquipmentSlot[] getValidSlots() {
        return new EntityEquipmentSlot[]{EntityEquipmentSlot.FEET};
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
        return 1;
    }
}
