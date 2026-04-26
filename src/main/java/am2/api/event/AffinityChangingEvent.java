package am2.api.event;

import am2.api.affinity.Affinity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when a player's affinity depth is about to change.
 *
 * <p>This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable}.
 * Cancel it to prevent the affinity change entirely.</p>
 *
 * <p>The {@link #amount} field is mutable and can be adjusted by listeners
 * to scale or cap the depth change before it is applied.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
@Cancelable
public class AffinityChangingEvent extends Event {
    /** The player whose affinity is changing. */
    public final EntityPlayer player;
    /** How much the affinity depth will change. Modify this to scale the change. */
    public float amount;
    /** The affinity being changed. */
    public final Affinity affinity;

    /**
     * @param player  the player whose affinity is changing
     * @param affinity the affinity element being changed
     * @param amt     the amount by which the affinity depth will change
     */
    public AffinityChangingEvent(EntityPlayer player, Affinity affinity, float amt) {
        this.player = player;
        this.amount = amt;
        this.affinity = affinity;
    }
}
