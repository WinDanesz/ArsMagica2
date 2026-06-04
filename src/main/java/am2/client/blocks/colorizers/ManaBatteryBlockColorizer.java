package am2.client.blocks.colorizers;

import am2.common.blocks.tileentity.TileEntityManaBattery;
import am2.common.power.PowerTypes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;

public class ManaBatteryBlockColorizer implements IBlockColor {

    @Override
    public int colorMultiplier(IBlockState state, @Nullable IBlockAccess world, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex == 0 && world != null && pos != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileEntityManaBattery) {
                TileEntityManaBattery battery = (TileEntityManaBattery) te;
                PowerTypes type = battery.getPowerType();
                if (type != PowerTypes.NONE)
                    return type.getColor();
            }
        }
        return 0xAAAAAA;
    }

}
