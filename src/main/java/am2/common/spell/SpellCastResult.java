package am2.common.spell;

public enum SpellCastResult {
    SUCCESS,
    NOT_ENOUGH_MANA,
    REAGENTS_MISSING,
    MALFORMED_SPELL_STACK,
    EFFECT_FAILED,
    SUCCESS_REDUCE_MANA,
    /** Spell succeeded but mana should NOT be deducted (e.g. Glyph detonation). */
    FREE_CAST,
    SILENCED
}
