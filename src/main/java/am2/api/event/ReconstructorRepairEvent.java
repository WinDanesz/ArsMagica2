package am2.api.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired each tick that the Arcane Reconstructor block spends repairing an item.
 *
 * <p>This event is {@link Cancelable}.
 * Cancel it to skip that repair tick (the item durability will not be restored for
 * the current tick, though the reconstructor keeps running).</p>
 *
 * <p>{@link #item} may be modified by listeners to swap the repaired stack.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
@Cancelable
public class ReconstructorRepairEvent extends Event {

    /**
     * The item currently being repaired. May be replaced by a listener.
     */
    public ItemStack item;

    /**
     * @param item the item being repaired by the Arcane Reconstructor
     */
    public ReconstructorRepairEvent(ItemStack item) {
        this.item = item;
    }
}

