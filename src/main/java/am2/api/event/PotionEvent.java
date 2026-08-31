package am2.api.event;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base class for AM2 potion-related events.
 *
 * <p>Fields on this class mirror those of {@link PotionEffect} and are populated
 * from the effect for convenience. Subclasses cover specific lifecycle moments:
 * <ul>
 *   <li>{@link EventPotionAdded} – fired when a potion effect is applied to an entity.</li>
 *   <li>{@link EventPotionLoaded} – fired when a potion effect is loaded from NBT.</li>
 * </ul></p>
 *
 * <p>Posted on {@link MinecraftForge#EVENT_BUS}.</p>
 */
public class PotionEvent extends Event {

    /** The potion type of the effect. */
    public Potion id;
    /** Duration of the effect in ticks. */
    public int duration;
    /** Amplifier (level) of the effect, zero-indexed. */
    public int amplifier;
    /** Whether the effect is ambient (e.g. from a beacon). */
    public boolean ambient;
    /** Whether to show particles for this effect. */
    public boolean showParticules;
    /** The full {@link PotionEffect} instance being processed. */
    public PotionEffect effect;

    /**
     * @param id            potion type
     * @param duration      duration in ticks
     * @param amplifier     effect amplifier (zero-indexed)
     * @param ambient       whether the effect is ambient
     * @param showParticules whether to show particles
     */
    protected PotionEvent(Potion id, int duration, int amplifier, boolean ambient, boolean showParticules) {
        this.id = id;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.showParticules = showParticules;
        this.effect = new PotionEffect(id, duration, amplifier, ambient, showParticules);
    }

    /**
     * Constructs the event from an existing {@link PotionEffect}, copying all its fields.
     *
     * @param effect the potion effect being processed
     */
    protected PotionEvent(PotionEffect effect) {
        this.effect = effect;
        id = effect.getPotion();
        duration = effect.getDuration();
        amplifier = effect.getAmplifier();
        showParticules = effect.doesShowParticles();
        ambient = effect.getIsAmbient();
    }

    /** @return the {@link PotionEffect} being processed */
    public PotionEffect getEffect() {
        return effect;
    }

    /**
     * Fired when a potion effect is added to an entity (e.g. via a spell or item).
     *
     * <p>Listeners may modify the public fields on the parent class (such as
     * {@link PotionEvent#duration} or {@link PotionEvent#amplifier}) before the
     * effect is applied.</p>
     */
    public static class EventPotionAdded extends PotionEvent {

        /**
         * @param id            potion type
         * @param duration      duration in ticks
         * @param amplifier     effect amplifier (zero-indexed)
         * @param ambient       whether the effect is ambient
         * @param showParticules whether to show particles
         */
        public EventPotionAdded(Potion id, int duration, int amplifier, boolean ambient, boolean showParticules) {
            super(id, duration, amplifier, ambient, showParticules);
        }

        /**
         * @param effect the potion effect being added
         */
        public EventPotionAdded(PotionEffect effect) {
            super(effect);
        }

    }

    /**
     * Fired when a potion effect is deserialized from NBT (e.g. when loading a player from disk).
     *
     * <p>Listeners can inspect {@link #getCompound()} for the raw NBT data and modify
     * {@link PotionEvent#effect} to substitute a different effect before it is applied.</p>
     *
     * <p>Use the static {@link #post(PotionEffect, NBTTagCompound)} helper to fire this
     * event and retrieve the (possibly modified) effect.</p>
     */
    public static class EventPotionLoaded extends PotionEvent {

        private NBTTagCompound compound;

        /**
         * @param effect   the potion effect loaded from NBT
         * @param compound the raw NBT compound the effect was read from
         */
        public EventPotionLoaded(PotionEffect effect, NBTTagCompound compound) {
            super(effect);
            this.compound = compound;
        }

        /** @return the raw NBT compound the effect was loaded from */
        public NBTTagCompound getCompound() {
            return compound;
        }

        /**
         * Fires an {@code EventPotionLoaded} event and returns the resulting effect,
         * which may have been modified by a listener.
         *
         * @param effect   the potion effect that was loaded
         * @param compound the NBT compound it was loaded from
         * @return the (possibly modified) {@link PotionEffect}
         */
        public static PotionEffect post(PotionEffect effect, NBTTagCompound compound) {
            EventPotionLoaded event = new EventPotionLoaded(effect, compound);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getEffect();
        }
    }

}
