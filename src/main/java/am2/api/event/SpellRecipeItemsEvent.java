package am2.api.event;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired to allow add-ons to inspect or modify the crafting altar recipe items for a
 * registered spell part (shape, component, or modifier).
 *
 * <p>Listen to this event to add, remove, or replace the ingredients required to
 * craft a specific spell part at the Crafting Altar.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class SpellRecipeItemsEvent extends Event {
    /** The registered name of the spell part. Used to identify which shape/component/modifier this recipe belongs to. */
    public final String registeredName;

    /**
     * recipe items, in order, that need to be thrown into the crafting altar in order to create the item.
     * <br/>
     * Use Items for items, Blocks for blocks, Strings for OreDictionary items, and E:[type flag] followed by an integer for essence amounts.
     * <br/>
     * Use itemstacks for items/blocks/oredict when meta is required.  By default it is meta 0.  Quantity (stack size) is ignored.
     * <br/>
     * Integer pairs represent the type (*=any, 1=neutral, 2=light, 4=dark, etc.), and the quantity of essence required.  The type can be used as flags,
     * <br/>
     * For example:
     * <pre>
     *     new Object[]{ "E:1|2", 1500 } //require 1500 of neutral or 1500 of light power.
     *     new Object[]{ "E:*", 1500 } //require 1500 of any kind of power
     *     etc.
     * </pre>
     */
    /**
     * The recipe items in-order. Modify this array to change or replace the required ingredients.
     *
     * <p>Supported element types:</p>
     * <ul>
     *   <li>{@link net.minecraft.item.Item} or {@link net.minecraft.block.Block} – matched at meta 0.</li>
     *   <li>{@link String} – treated as an OreDictionary key.</li>
     *   <li>{@link net.minecraft.item.ItemStack} – matched by item and meta; stack size is ignored.</li>
     *   <li>{@code Object[]{"E:flags", int amount}} – etherium cost; flags are OR-able power type
     *       bitmasks ({@code *}=any, {@code 1}=neutral, {@code 2}=light, {@code 4}=dark, etc.).</li>
     * </ul>
     */
    public Object[] recipeItems;

    /**
     * @param name        the registered name of the spell part
     * @param recipeItems the initial array of recipe items required at the Crafting Altar
     */
    public SpellRecipeItemsEvent(String name, Object[] recipeItems) {
        registeredName = name;
        this.recipeItems = recipeItems;
    }

}
