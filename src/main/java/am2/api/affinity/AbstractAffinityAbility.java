package am2.api.affinity;

import am2.api.event.SpellCastEvent;
import am2.api.extensions.IAffinityData;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public abstract class AbstractAffinityAbility extends IForgeRegistryEntry.Impl<AbstractAffinityAbility> {

    protected AbstractAffinityAbility(ResourceLocation identifier) {
        this.setRegistryName(identifier);
    }

    /**
     * At which point does this ability enable ?
     *
     * @return a depth.
     */
    public abstract float getMinimumDepth();

    /**
     * At which point does this ability disables ?
     *
     * @return a depth or any value under 0 or over 1 to ignore this.
     */
    public float getMaximumDepth() {
        return -1F;
    }

    /**
     * Setting this to null or NONE will make this class useless.
     *
     * @return the ability that is required.
     */
    public abstract Affinity getAffinity();

    /**
     * If this Affinity Ability uses a key binding, return it, otherwise just return null
     *
     * @return the key binding that this ability uses, or null.
     */
    @Nullable
    public KeyBinding getKey() {
        return null;
    }

    /**
     * Checks if the player can use this ability. Most of the time you won't need change this unless you are using toggle {@link KeyBinding}s, in that case consider using {@link AbstractToggledAffinityAbility}.
     *
     * @param player : the current player.
     * @return if the player can use this ability.
     */
    public boolean canApply(EntityPlayer player) {
        return isEligible(player);
    }

    public boolean isEligible(EntityPlayer player) {
        Affinity aff = this.getAffinity();
        if (aff == Affinities.none || aff == null)
            return false;
        IAffinityData data = AffinityData.For(player);
        double depth = data.getAffinityDepth(aff);
        if (getMaximumDepth() < 0F || getMaximumDepth() > 1F || getMaximumDepth() < getMinimumDepth())
            return depth >= getMinimumDepth();
        return depth >= getMinimumDepth() && depth <= getMaximumDepth();
    }

    /**
     * The thing that this ability does
     *
     * @param player : the current player
     */
    public void applyTick(EntityPlayer player) {
    }

    /**
     * Whether {@link #applyTick} must also run client-side, for the local player only.
     * <p>
     * Needed for abilities that alter the player's own motion: the owning client is authoritative over its
     * own position, so a server-only motion change is invisible to it (overwritten by its own movement
     * prediction next tick) even though it's still correct for everyone else's server-driven view of that
     * player. Abilities that only touch server-synced state (attributes, potion effects, ...) don't need this.
     *
     * @return true if {@link #applyTick} should also be invoked client-side for the local player.
     */
    public boolean hasClientTick() {
        return false;
    }

    public void applyKeyPress(EntityPlayer player) {
    }

    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
    }

    public void applyFall(EntityPlayer player, LivingFallEvent event) {
    }

    /**
     * Fired for players whose flight capability causes vanilla to bypass normal
     * fall damage. Abilities that grant temporary flight can use this to retain
     * fall damage when the player is not actually flying.
     */
    public void applyFlyableFall(EntityPlayer player, PlayerFlyableFallEvent event) {
    }

    public void applySpellCast(EntityPlayer player, SpellCastEvent.Post event) {
    }

    public void applyPreSpellCast(EntityPlayer player, SpellCastEvent.Pre event) {
    }

    public void applyDeath(EntityPlayer player, LivingDeathEvent event) {
    }

    public void applyKill(EntityPlayer player, LivingDeathEvent event) {
    }

    public void applyJump(EntityPlayer player, LivingJumpEvent event) {
    }

    /**
     * Fired when the player right-clicks (interacts with) an entity.
     *
     * @param player : the current player
     * @param event : the interact event, carrying the target entity and hand used.
     */
    public void applyInteractEntity(EntityPlayer player, PlayerInteractEvent.EntityInteract event) {
    }

    public void removeEffects(EntityPlayer player) {
    }

    /**
     * Fired when the player's block-breaking speed is being computed, e.g. to boost bare-hand
     * mining speed against specific materials.
     *
     * @param player : the current player
     * @param event : the break speed event, carrying the block being broken and the speed so far.
     */
    public void applyBreakSpeed(EntityPlayer player, PlayerEvent.BreakSpeed event) {
    }

    /**
     * Fired when checking whether the player can harvest (get drops from) a block, e.g. to let a
     * sufficiently-hardened bare hand count as holding the required tool.
     *
     * @param player : the current player
     * @param event : the harvest check event, carrying the block and whether it currently succeeds.
     */
    public void applyHarvestCheck(EntityPlayer player, PlayerEvent.HarvestCheck event) {
    }

    public boolean hasMax() {
        return getMaximumDepth() >= 0F && getMaximumDepth() <= 1F && getMaximumDepth() > getMinimumDepth();
    }
}
