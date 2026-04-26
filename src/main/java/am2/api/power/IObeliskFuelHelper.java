package am2.api.power;

import net.minecraft.item.ItemStack;

import java.util.function.Function;

public interface IObeliskFuelHelper {
    /**
     * Register a fuel type adapter for the obelisk.
     *
     * @param func : A function that takes an {@link ItemStack} and gives an integer with the burn value. Values of 0 or less are ignored.
     */
    public void registerFuelType(Function<ItemStack, Integer> func);

    /**
     * Retrieves the burn time for the specified stack.  Returns 0 if it is not a valid fuel.
     */
    public int getFuelBurnTime(ItemStack stack);
}
