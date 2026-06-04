package am2.client.items.colorizers;

import am2.common.power.PowerTypes;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ManaBatteryItemColorizer implements IItemColor {

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        if (tintIndex == 0 && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            PowerTypes type = PowerTypes.getByID(tag.getInteger("mana_battery_powertype"));
            if (type != PowerTypes.NONE)
                return type.getColor();
        }
        return 0xAAAAAA;
    }

}
