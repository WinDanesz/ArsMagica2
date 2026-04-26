package am2.common;

import am2.api.power.IObeliskFuelHelper;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Function;

public class ObeliskFuelHelper implements IObeliskFuelHelper {
    public static final am2.common.ObeliskFuelHelper instance = new am2.common.ObeliskFuelHelper();

    private final List<Function<ItemStack, Integer>> validFuels = Lists.newArrayList();

    public void registerFuelType(Function<ItemStack, Integer> func) {
        if (func != null)
            this.validFuels.add(func);
    }

    public int getFuelBurnTime(ItemStack stack) {
        if (stack.isEmpty())
            return 0;
        for (Function<ItemStack, Integer> possibleFuel : this.validFuels) {
            int val = possibleFuel.apply(stack);
            if (val > 0)
                return val;
        }
        return 0;
    }
}
