package am2.api.event;

import am2.api.spell.SpellData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Base event fired around spell casting by a living entity.
 *
 * <p>Two lifecycle sub-events are available:
 * <ul>
 *   <li>{@link Pre}  – fired before the spell resolves. Cancelable.</li>
 *   <li>{@link Post} – fired after the spell has resolved.</li>
 * </ul></p>
 *
 * <p>Fields {@link #manaCost} and {@link #burnout} are mutable and may be
 * adjusted in {@link Pre} listeners before the cost is actually deducted.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class SpellCastEvent extends Event {

    /** The spell data describing the spell being cast. */
    public SpellData spell;
    /** The mana cost that will be (or was) deducted from the caster. */
    public float manaCost;
    /** The entity casting the spell. */
    public EntityLivingBase entityLiving;
    /** Burnout applied to the caster after casting (for over-exertion mechanics). */
    public float burnout;

    /**
     * @param caster   the entity casting the spell
     * @param spell    the spell being cast
     * @param manaCost the base mana cost
     */
    public SpellCastEvent(EntityLivingBase caster, SpellData spell, float manaCost) {
        this.spell = spell;
        this.manaCost = manaCost;
        this.entityLiving = caster;
    }

    /**
     * Fired <em>before</em> a spell resolves.
     *
     * <p>This event is {@link net.minecraftforge.fml.common.eventhandler.Cancelable}.
     * Canceling it prevents the spell from being cast and the mana from being deducted.</p>
     */
    public static class Pre extends SpellCastEvent {

        /**
         * @param caster   the entity casting the spell
         * @param spell    the spell being cast
         * @param manaCost the base mana cost
         */
        public Pre(EntityLivingBase caster, SpellData spell, float manaCost) {
            super(caster, spell, manaCost);
        }

    }

    /**
     * Fired <em>after</em> a spell has successfully resolved and its mana cost deducted.
     */
    public static class Post extends SpellCastEvent {

        /**
         * @param caster   the entity that cast the spell
         * @param spell    the spell that was cast
         * @param manaCost the mana cost that was deducted
         */
        public Post(EntityLivingBase caster, SpellData spell, float manaCost) {
            super(caster, spell, manaCost);
        }

    }

}
