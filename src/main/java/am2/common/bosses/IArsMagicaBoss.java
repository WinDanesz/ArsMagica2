package am2.common.bosses;

import net.minecraft.util.SoundEvent;

public interface IArsMagicaBoss {
    /**
     * Should simply return the current action
     */
    public BossActions getCurrentAction();

    /**
     * Should set the current action as well as reset the number of ticks in the current action.
     * If world is not remote should also send a packet to all nearby clients advising action change.
     */
    public void setCurrentAction(BossActions action);

    /**
     * Should return the number of ticks in the current action
     */
    public int getTicksInCurrentAction();

    /**
     * Sets the number of ticks in the current action (used by client-side packet handling)
     */
    public void setTicksInCurrentAction(int ticks);

    /**
     * Is the passed in action valid based on any internal states?  (Ex, plant guardian has thrown sickle)
     */
    public boolean isActionValid(BossActions action);

    /**
     * Gets the sound played when the entity attacks
     */
    public SoundEvent getAttackSound();
}
