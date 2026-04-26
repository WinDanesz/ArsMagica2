package am2.common.compat.electroblob.item;

import am2.ArsMagica;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.electroblob.EBWizardryCompatHandler;
import am2.common.config.AMConfig;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemSpellBase;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Wrapper item that allows an Electroblob's Wizardry spell to be bound into an
 * AM2 spell book slot and cast using the EBWiz casting pipeline (which in turn
 * consumes AM2 mana via {@link EBWizardryCompatHandler}) - aka the spell item.
 *
 * <p>Each ItemStack stores the EBWiz spell's registry name in the NBT key
 * {@value #NBT_KEY}.  The item extends {@link ItemSpellBase} so it is accepted
 * by the spell book's {@code SlotOneItemClassOnly(ItemSpellBase.class)} slots
 * without any slot-class changes.
 *
 * <p>This class is only instantiated when EBWiz is detected (see
 * {@link EBWizardryCompatBootstrap}) and it replaces the AM spell book item.  It must <em>not</em> be referenced from code
 * that runs unconditionally (unless the reference is lazy / guarded by
 * {@code Loader.isModLoaded}).
 */
public final class ItemEBWizSpellBinding extends ItemSpellBase implements ISpellCastingItem {

    /** NBT key used to persist the bound EBWiz spell's registry name. */
    static final String NBT_KEY = "EBWizSpell";

    // -------------------------------------------------------------------------
    // Factory / accessors
    // -------------------------------------------------------------------------

    /**
     * Creates an {@link ItemStack} of this item pre-loaded with the given
     * EBWiz spell.
     *
     * @param spell the EBWiz {@link Spell} to bind; must not be null or
     *              {@code Spells.none}
     * @return an ItemStack whose NBT holds the spell's registry name
     */
    public static ItemStack createForSpell(Spell spell) {
        ItemStack stack = new ItemStack(AMItems.ebwiz_spell_binding);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NBT_KEY, spell.getRegistryName().toString());
        stack.setTagCompound(tag);
        return stack;
    }

    /**
     * Maps the EBWiz element of the spell in {@code stack} to the closest AM2
     * {@link Affinity}, used for the held-item hand-glow animation.
     * Returns {@code null} if the stack carries no valid spell.
     * <p>Uses the same weighted affinity map as the affinity-gain system, picking
     * the highest-weight entry as the representative affinity.
     */
    @Nullable
    public static Affinity getAM2Affinity(ItemStack stack) {
        Spell spell = getSpell(stack);
        if (spell == null) return null;
        List<AMConfig.WeightedAffinity> weights = EBWizardryCompatHandler.resolveWeights(spell);
        if (weights.isEmpty()) return Affinities.arcane;
        // Pick the highest-weight entry as representative affinity for hand-glow rendering.
        AMConfig.WeightedAffinity best = weights.get(0);
        for (AMConfig.WeightedAffinity wa : weights) {
            if (wa.weight > best.weight) best = wa;
        }
        return EBWizardryCompatHandler.lookupAffinity(best.affinityName);
    }

    /**
     * Returns the {@code AMParticleIcons} hand-icon name for the EBWiz element of
     * the spell in {@code stack}, covering all elements including those without a
     * direct AM2 affinity counterpart (SORCERY, HEALING).
     * Returns {@code null} if the stack carries no valid spell.
     */
    @Nullable
    public static String getHandIconName(ItemStack stack) {
        Spell spell = getSpell(stack);
        if (spell == null) return null;
        Element element = spell.getElement();
        if (element == Element.FIRE)       return "fire_hand";
        if (element == Element.ICE)        return "ice_hand";
        if (element == Element.MAGIC)      return "arcane_hand";
        if (element == Element.LIGHTNING)  return "lightning_hand";
        if (element == Element.EARTH)      return "earth_hand";
        if (element == Element.NECROMANCY) return "ender_hand";
        if (element == Element.SORCERY)    return "sorcery_hand";
        if (element == Element.HEALING)    return "healing_hand";
        return "arcane_hand";
    }

    /**
     * Returns the EBWiz {@link Spell} stored in the given stack, or
     * {@code null} if the NBT is absent or the registry name is unknown.
     */
    @Nullable
    public static Spell getSpell(ItemStack stack) {
        if (!stack.hasTagCompound()) return null;
        String registryName = stack.getTagCompound().getString(NBT_KEY);
        if (registryName.isEmpty()) return null;
        return Spell.registry.getValue(new ResourceLocation(registryName));
    }

    // -------------------------------------------------------------------------
    // AM2 modifier support
    // -------------------------------------------------------------------------

    /**
     * Reads AM2 modifier counts stored by the Scribing Desk and applies them to
     * the EBWiz {@link SpellModifiers} object before casting. This allows Range,
     * Duration, and Blast modifiers applied at the desk to affect the spell.
     */
    private static void applyStoredModifiers(ItemStack stack, SpellModifiers modifiers) {
        if (!stack.hasTagCompound()) return;
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("AM2Modifiers")) return;
        NBTTagCompound mods = tag.getCompoundTag("AM2Modifiers");
        int range    = mods.getInteger("range");
        int duration = mods.getInteger("duration");
        int blast    = mods.getInteger("blast");
        if (range    > 0) modifiers.set(WizardryItems.range_upgrade,    1.0f + range    * Constants.RANGE_INCREASE_PER_LEVEL,        true);
        if (duration > 0) modifiers.set(WizardryItems.duration_upgrade, 1.0f + duration * Constants.DURATION_INCREASE_PER_LEVEL,     true);
        if (blast    > 0) modifiers.set(WizardryItems.blast_upgrade,    1.0f + blast    * Constants.BLAST_RADIUS_INCREASE_PER_LEVEL, true);
    }

    // -------------------------------------------------------------------------
    // ISpellCastingItem implementation
    // -------------------------------------------------------------------------

    /**
     * Returns the EBWiz spell bound in this stack, or {@code Spells.none} if
     * none is stored.  This makes {@link electroblob.wizardry.util.EntityUtils#isCasting}
     * aware of our item, enabling wing rendering, loop sounds, shadow-ward, etc.
     */
    @Override
    @Nonnull
    public Spell getCurrentSpell(ItemStack stack) {
        Spell spell = getSpell(stack);
        return spell != null ? spell : Spells.none;
    }

    /** AM2 provides its own spell HUD; suppress the EBWiz one. */
    @Override
    public boolean showSpellHUD(EntityPlayer player, ItemStack stack) {
        return false;
    }

    /**
     * Performs the mana guard and fires the appropriate cast event (Pre on
     * tick&nbsp;0, Tick on subsequent ticks).
     *
     * <p>The mana check runs on <em>both</em> sides: the client has a synced
     * copy of the player's mana (used by the AM2 HUD) and must also return
     * {@code false} when mana is depleted so that it calls
     * {@code stopActiveHand()} locally.  Without this, the client keeps the
     * item "in use" and continually re-sends right-click packets, making the
     * spell appear to continue indefinitely after the server has stopped it.
     * The Pre/Tick events are only fired server-side to avoid handlers running
     * twice.
     */
    @Override
    public boolean canCast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand,
                           int castingTick, SpellModifiers modifiers) {
        IEntityExtension am2Data = EntityExtension.For(caster);
        if (am2Data == null) return false;
        float baseCost = EBWizardryCompatHandler.getEBWizSpellBindingManaCost(stack, caster);
        if (baseCost < 0) baseCost = spell.getCost() * ArsMagica.config.getEBWizManaCostMultiplier();
        float burnoutMul = 1f + (am2Data.getCurrentBurnout() / Math.max(1f, am2Data.getMaxBurnout()));
        // Guard with the next due deduction amount.  On tick 0 we charge the
        // full base cost; on subsequent ticks we check half-cost every tick
        // (matching EBWiz's two-intervals-per-second schedule) so casting stops
        // the moment mana drops too low, even between deduction points.
        float costToCheck = castingTick == 0 ? baseCost * burnoutMul : baseCost * burnoutMul / 2f;
        if (!am2Data.hasEnoughMana(costToCheck)) return false;
        // Events are only fired server-side to avoid duplicate handler invocations.
        if (caster.world.isRemote) return true;
        if (castingTick == 0) {
            return !MinecraftForge.EVENT_BUS.post(
                    new SpellCastEvent.Pre(SpellCastEvent.Source.WAND, spell, caster, modifiers));
        } else {
            return !MinecraftForge.EVENT_BUS.post(
                    new SpellCastEvent.Tick(SpellCastEvent.Source.WAND, spell, caster, modifiers, castingTick));
        }
    }

    /**
     * Calls {@code spell.cast()} then handles post-cast bookkeeping
     * server-side: fires {@code SpellCastEvent.Post} on tick&nbsp;0 (which
     * lets {@link EBWizardryCompatHandler} deduct the first tick's mana and
     * grant XP), and directly deducts the half-second mana portion at every
     * subsequent multiple of 10 ticks.
     */
    @Override
    public boolean cast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand,
                        int castingTick, SpellModifiers modifiers) {
        if (!spell.cast(caster.world, caster, hand, castingTick, modifiers)) return false;
        if (!caster.world.isRemote) {
            if (castingTick == 0) {
                // ElectroblobCompatHandler.onEBWizSpellCastPost deducts mana and grants XP.
                MinecraftForge.EVENT_BUS.post(
                        new SpellCastEvent.Post(SpellCastEvent.Source.WAND, spell, caster, modifiers));
            } else if (castingTick % 10 == 0) {
                IEntityExtension am2Data = EntityExtension.For(caster);
                if (am2Data != null) {
                    float baseCost = EBWizardryCompatHandler.getEBWizSpellBindingManaCost(stack, caster);
                    if (baseCost < 0) baseCost = spell.getCost() * ArsMagica.config.getEBWizManaCostMultiplier();
                    float burnoutMul = 1f + (am2Data.getCurrentBurnout() / Math.max(1f, am2Data.getMaxBurnout()));
                    float halfCost = baseCost * burnoutMul / 2f;
                    am2Data.deductMana(halfCost);
                    am2Data.setCurrentBurnout(Math.min(am2Data.getMaxBurnout(),
                            am2Data.getCurrentBurnout() + halfCost * ArsMagica.config.getManaBurnoutRatio()));
                }
            }
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Item overrides
    // -------------------------------------------------------------------------

    /**
     * This item does not carry an AM2 {@code SpellCaster} capability; casting
     * is handled through the EBWiz spell system in
     * {@link #onPlayerStoppedUsing} and {@link #onUsingTick}.
     */
    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack,
                                                @Nullable NBTTagCompound nbt) {
        return null;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Spell spell = getSpell(stack);
        if (spell != null) {
            Element element = spell.getElement();
            return element.getFormattingCode() + spell.getDisplayName();
        }
        return super.getItemStackDisplayName(stack);
    }

    /**
     * Overrides the parent's right-click handler so that EBWiz spell-bindings
     * always start charging (setActiveHand) instead of inadvertently opening
     * the AM2 spell-customization GUI (which the parent does when the item has
     * no custom display name in NBT).
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        Spell spell = getSpell(stack);
        if (spell == null || !spell.isEnabled()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    /**
     * Casts the bound EBWiz spell (non-continuous) when the player releases
     * the use key.
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world,
                                     EntityLivingBase entityLiving, int timeLeft) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        Spell spell = getSpell(stack);
        if (spell == null || !spell.isEnabled() || spell.isContinuous) return;

        EntityPlayer player = (EntityPlayer) entityLiving;
        // Guard: require enough AM2 mana before allowing the cast.
        if (!world.isRemote) {
            // Use armor-discounted cost for the guard check so that wearing
            // matching wizard armour (e.g. fire mage robes) is correctly reflected.
            float cost = EBWizardryCompatHandler.getEBWizSpellBindingManaCost(stack, player);
            if (cost < 0) cost = spell.getCost() * ArsMagica.config.getEBWizManaCostMultiplier();
            if (!EntityExtension.For(player).hasEnoughMana(cost)) return;
        }
        EnumHand hand = entityLiving.getActiveHand();
        if (hand == null) hand = EnumHand.MAIN_HAND;
        // COST must be 1.0 so the Pre handler sees a non-zero mana cost to deduct.
        SpellModifiers modifiers = new SpellModifiers();
        modifiers.set(SpellModifiers.COST, 1.0f, false);
        applyStoredModifiers(stack, modifiers);
        // Fire Pre so ElectroblobCompatHandler can pledge the AM2 mana deduction.
        if (MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(SpellCastEvent.Source.WAND, spell, player, modifiers))) return;
        if (spell.cast(world, player, hand, 0, modifiers)) {
            // Fire Post only on success so ElectroblobCompatHandler deducts the mana and adds burnout.
            MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(SpellCastEvent.Source.WAND, spell, player, modifiers));
        }
    }

    /**
     * Drives continuous EBWiz spells each tick while the spell book is held
     * down.  Delegates to {@link #canCast} and {@link #cast} (the
     * {@link ISpellCastingItem} methods) so that all casting logic, including
     * the wing-renderer check via
     * {@link electroblob.wizardry.util.EntityUtils#isCasting}, goes through
     * the standard EBWiz pathway.
     *
     * <p>Continuous spells must also run on the <em>client</em> so that
     * particle and audio effects are displayed. {@link #canCast} checks mana
     * on both sides and stops the hand when mana is depleted, preventing the
     * client from re-initiating casting while out of mana.
     */
    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase entityLiving, int count) {
        if (!(entityLiving instanceof EntityPlayer)) return;
        Spell spell = getSpell(stack);
        if (spell == null || !spell.isEnabled() || !spell.isContinuous) return;

        EntityPlayer player = (EntityPlayer) entityLiving;
        EnumHand hand = entityLiving.getActiveHand();
        if (hand == null) hand = EnumHand.MAIN_HAND;
        int ticksInUse = getMaxItemUseDuration(stack) - count;

        // COST must be 1.0 so ElectroblobCompatHandler sees a non-zero value.
        SpellModifiers modifiers = new SpellModifiers();
        modifiers.set(SpellModifiers.COST, 1.0f, false);
        applyStoredModifiers(stack, modifiers);

        if (canCast(stack, spell, player, hand, ticksInUse, modifiers)) {
            cast(stack, spell, player, hand, ticksInUse, modifiers);
        } else {
            // Stop on both sides: server authoritative, client stops immediately
            // so there is no gap where the client re-initiates use.
            player.stopActiveHand();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn,
                               List<String> tooltip, ITooltipFlag flagIn) {
        Spell spell = getSpell(stack);

        // Affinity gain
        if (spell != null) {
            List<AMConfig.WeightedAffinity> weights = EBWizardryCompatHandler.resolveWeights(spell);
            if (!weights.isEmpty()) {
                float totalWeight = 0f;
                for (AMConfig.WeightedAffinity wa : weights) totalWeight += wa.weight;
                if (totalWeight > 0f) {
                    final float gainAmount = (float) ArsMagica.config.getEBWizAffinityGainAmount();
                    tooltip.add("\u00a77Affinity Gain:");
                    // Sort descending by weight then display actual per-cast gain amount.
                    List<AMConfig.WeightedAffinity> sorted = new java.util.ArrayList<>(weights);
                    sorted.sort((a, b) -> Float.compare(b.weight, a.weight));
                    for (AMConfig.WeightedAffinity wa : sorted) {
                        if (wa.weight <= 0f) continue;
                        Affinity aff = EBWizardryCompatHandler.lookupAffinity(wa.affinityName);
                        String name = aff != null ? aff.getLocalizedName() : wa.affinityName;
                        float actualGain = gainAmount * wa.weight / totalWeight;
                        String gainStr = String.format("%.4f", actualGain).replaceAll("0+$", "").replaceAll("\\.$", "");
                        tooltip.add(String.format("\u00a75  %s: \u00a7d%s", name, gainStr));
                    }
                }
            }
        }

        // AM2 modifiers applied at the Scribing Desk (only shown when present)
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("AM2Modifiers")) {
                NBTTagCompound mods = tag.getCompoundTag("AM2Modifiers");
                int range    = mods.getInteger("range");
                int duration = mods.getInteger("duration");
                int blast    = mods.getInteger("blast");
                if (range > 0 || duration > 0 || blast > 0) {
                    tooltip.add("\u00a77Modifiers:");
                    if (range    > 0) tooltip.add("\u00a76  +" + range    + " Range");
                    if (duration > 0) tooltip.add("\u00a76  +" + duration + " Duration");
                    if (blast    > 0) tooltip.add("\u00a76  +" + blast    + " Blast");
                }
            }
        }
    }
}
