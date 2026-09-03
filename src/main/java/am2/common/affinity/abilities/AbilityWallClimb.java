package am2.common.affinity.abilities;

import am2.api.affinity.AbstractToggledAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * At full Nature affinity depth, pressing into a wall lets you climb it instead of just standing there.
 * Toggled with a key bind rather than always-on, since the movement can be disorienting when unwanted.
 */
public class AbilityWallClimb extends AbstractToggledAffinityAbility {

    public AbilityWallClimb() {
        super(new ResourceLocation("arsmagica2", "wall_climb"));
    }

    @Override
    public float getMinimumDepth() {
        return 1f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.nature;
    }

    @Override
    protected boolean isEnabled(EntityPlayer player) {
        return AffinityData.For(player).getAbilityBoolean(AffinityData.WALL_CLIMB);
    }

    // Deliberately does NOT override getKey(): the toggle key press is handled directly in
    // Keybindings.onKeyInput (like Dark Vision / Ice Bridge), which flips AffinityData.WALL_CLIMB via
    // PacketAbilityToggle. KeyBinding#isPressed() consumes the press on first read, so also wiring the
    // key through here would race AffinityAbilityHelper's generic getKey()-driven handler for the same
    // KeyInputEvent — whichever ran first would silently eat the press before the other saw it.

    // This must also run client-side (see hasClientTick): the owning client is authoritative over its
    // own position, so a server-only motionY change is overwritten by that client's own movement
    // prediction before it ever takes visible effect.
    @Override
    public boolean hasClientTick() {
        return true;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (!player.collidedHorizontally) return;

        double targetMotionY = player.isSneaking()
                ? player.motionY * 0.79999999
                : (EntityExtension.For(player).getIsFlipped() ? -0.25 : 0.25);
        player.motionY = targetMotionY;
        player.fallDistance = 0;
    }

}
