package am2.api.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player's magic level changes (e.g., after accumulating enough magic XP).
 *
 * <p>The new level is available via {@link #getLevel()}. This event is not cancelable;
 * use it for side effects such as granting skill points or triggering visuals.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class PlayerMagicLevelChangeEvent extends PlayerEvent {

    /** The new magic level the player has reached. */
    protected final int level;

    /**
     * @param player the player whose magic level changed
     * @param level  the new magic level
     */
    public PlayerMagicLevelChangeEvent(EntityPlayer player, int level) {
        super(player);
        this.level = level;
    }

    /** @return the new magic level the player has reached */
    public int getLevel() {
        return level;
    }

}
