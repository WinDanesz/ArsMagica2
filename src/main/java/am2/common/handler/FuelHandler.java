package am2.common.handler;

import am2.common.registry.AMBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.IFuelHandler;

/**
 * Created by Orinion on 09.01.2017.
 */
public class FuelHandler implements IFuelHandler {

    @Override
    public int getBurnTime(ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        Block fuelBlock = Block.getBlockFromItem(fuelItem);

        if (fuelBlock == AMBlocks.witchwood_planks) return 300;
        if (fuelBlock == AMBlocks.witchwood_log) return 300;
        if (fuelBlock == AMBlocks.witchwood_sapling) return 100;
        if (fuelBlock == AMBlocks.witchwood_stairs) return 300;

        return 0;
    }
}
