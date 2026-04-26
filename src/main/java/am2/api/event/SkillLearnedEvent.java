package am2.api.event;

import am2.api.skill.Skill;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * Fired when a player successfully learns a new {@link Skill}.
 *
 * <p>By the time this event is fired the skill has already been granted; use it
 * for side-effects such as awarding achievements or triggering animations rather
 * than for validation.</p>
 *
 * <p>Posted on {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.</p>
 */
public class SkillLearnedEvent extends PlayerEvent {

    /** The skill that was just learned. */
    protected final Skill skill;

    /**
     * @param player the player who learned the skill
     * @param skill  the skill that was learned
     */
    public SkillLearnedEvent(EntityPlayer player, Skill skill) {
        super(player);
        this.skill = skill;
    }

    /** @return the skill that was learned */
    public Skill getSkill() {
        return skill;
    }

}
