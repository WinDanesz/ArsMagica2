package am2.common.compat.electroblob;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistryHelper;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISkillData;
import am2.api.skill.SkillPoint;
import am2.api.spell.SpellData;
import am2.api.spell.SpellPart;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.compat.electroblob.item.ItemSpellBookEBWiz;
import am2.common.compat.electroblob.spells.IceStatue;
import am2.common.compat.electroblob.spells.Metamorphosis;
import am2.common.compat.electroblob.spells.PlaceTemporaryBlock;
import am2.common.config.AMConfig;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.items.ItemSpellBook;
import am2.common.registry.AMItems;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import am2.common.registry.SkillTrees;
import am2.common.skill.Discipline;
import am2.common.spell.modifier.EBWizBlast;
import com.google.common.collect.Sets;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.client.gui.GuiSpellBook;
import electroblob.wizardry.client.gui.handbook.GuiWizardHandbook;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Forge event handler that bridges Electroblob's Wizardry and Ars Magica 2.
 *
 * <h3>Mana bridge</h3>
 * When an EBWiz spell is cast by an entity that has an AM2
 * {@link IEntityExtension} capability with sufficient mana:
 * <ol>
 *   <li>The EBWiz wand mana cost is reduced to zero via the
 *       {@link SpellModifiers#COST} modifier (the wand is not drained).</li>
 *   <li>The equivalent AM2 mana is deducted after the spell succeeds
 *       (in {@code SpellCastEvent.Post}).</li>
 * </ol>
 * If the caster does not have enough AM2 mana the cast is cancelled outright
 * (the spell fails rather than silently falling back to wand mana).
 *
 * <h3>Event bridge</h3>
 * Both AM2's {@code am2.api.event.SpellCastEvent} and EBWiz's
 * {@code electroblob.wizardry.event.SpellCastEvent} are fired on the shared
 * Forge event bus, so listeners registered with either system can observe
 * spell-cast events from both mods without additional wiring.
 *
 * <p>This class is only instantiated when EBWiz is detected (see
 * {@link EBWizardryCompatBootstrap#register()}) and must <em>not</em> be annotated
 * with {@code @Mod.EventBusSubscriber} – that would cause the class to be
 * loaded even when EBWiz is absent, triggering a {@link NoClassDefFoundError}.
 */
public final class EBWizardryCompatHandler {

    // Double-supplier keeps ItemSpellBookEBWiz class resolution deferred until actual item creation.
    private static final Supplier<Supplier<Item>> SPELLBOOK_ITEM_FACTORY = () -> ItemSpellBookEBWiz::new;

    /**
     * Returns the AM2 mana cost for the EBWiz spell bound in {@code stack},
     * applying the configured multiplier. Returns {@code -1} if the stack
     * holds no valid EBWiz spell.
     */
    public static float getEBWizSpellBindingManaCost(ItemStack stack) {
        return getEBWizSpellBindingManaCost(stack, null);
    }

    /**
     * Returns the armor-discounted AM2 mana cost for the EBWiz spell bound in
     * {@code stack} for the given {@code caster}, applying the configured
     * multiplier and any EBWiz wizard-armour cost reductions that apply to the
     * spell's element.  Pass {@code null} for {@code caster} to skip armor lookup.
     */
    public static float getEBWizSpellBindingManaCost(ItemStack stack, @Nullable EntityLivingBase caster) {
        Spell spell = ItemEBWizSpellBinding.getSpell(stack);
        if (spell == null) return -1f;
        float costMultiplier = 1.0f;
        if (caster != null) {
            // Simulate the SpellCastEvent.Pre modifier pass so that EBWiz wizard-armour
            // cost reductions (e.g. fire mage robes reducing fireball cost) are reflected.
            SpellModifiers modifiers = new SpellModifiers();
            modifiers.set(SpellModifiers.COST, 1.0f, false);
            electroblob.wizardry.event.SpellCastEvent.Pre fakeEvent = new electroblob.wizardry.event.SpellCastEvent.Pre(electroblob.wizardry.event.SpellCastEvent.Source.WAND, spell, caster, modifiers);
            ItemWizardArmour.onSpellCastPreEvent(fakeEvent);
            costMultiplier = modifiers.get(SpellModifiers.COST);
        }
        float disciplineCostMult = caster != null ? getDisciplineCostMultiplier(caster, spell) : 1.0f;
        return spell.getCost() * costMultiplier * ArsMagica.config.getEBWizManaCostMultiplier() * disciplineCostMult;
    }

    /**
     * Search radius (blocks) used when counting EBWiz summons near a caster.
     */
    private static final double SUMMON_SEARCH_RADIUS = 28.0;

    /**
     * Counts the number of EBWiz {@link ISummonedCreature}
     * entities currently alive within {@value #SUMMON_SEARCH_RADIUS} blocks of
     * {@code caster} that are owned by {@code caster}.
     * Returns 0 if {@code caster} or its world is {@code null}.
     *
     * <p>Uses an AABB search rather than a full world entity scan so that only
     * entities in nearby chunks are considered, keeping the call cheap.
     * EBWiz minions follow their owner and will not normally be farther away
     * than this radius.
     */
    public static int countSummonsFor(EntityLivingBase caster) {
        if (caster == null || caster.world == null) return 0;
        AxisAlignedBB box = caster.getEntityBoundingBox().grow(SUMMON_SEARCH_RADIUS);
        return caster.world.getEntitiesWithinAABB(Entity.class, box, e -> e instanceof ISummonedCreature && ((ISummonedCreature) e).getCaster() == caster).size();
    }

    /**
     * Tracks AM2 mana that has been pledged to pay for an in-flight EBWiz
     * spell cast.  Keyed by caster UUID; the entry is removed once the spell's
     * Post event fires (or when the player disconnects).
     *
     * <p>Uses {@link ConcurrentHashMap} because EBWiz fires its spell events
     * separately on both the client and the server thread.
     */
    private final Map<UUID, Float> pendingManaDeductions = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // EntityJoinWorldEvent – enforce AM2 summon cap for EBWiz summons
    // -------------------------------------------------------------------------

    /**
     * When an EBWiz summoned creature joins the world, enforces the AM2 summon
     * cap. If the caster already has as many summons as the cap allows (counting
     * both AM2 and EBWiz summons), the new EBWiz summon is immediately despawned
     * and the join event is cancelled.
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) return;
        if (!(event.getEntity() instanceof ISummonedCreature)) return;

        ISummonedCreature summon = (ISummonedCreature) event.getEntity();
        EntityLivingBase caster = summon.getCaster();
        if (caster == null) return;

        IEntityExtension am2Data = EntityExtension.For(caster);
        if (am2Data == null) return;

        // EBWizardry registers summons in its own tracking after EntityJoinWorldEvent,
        // so countSummonsFor(caster) does NOT yet include the joining entity here.
        int ebwizBefore = countSummonsFor(caster);
        int am2Count = am2Data.getCurrentSummons();
        if (am2Count + ebwizBefore >= am2Data.getMaxSummons()) {
            event.getEntity().setDead();
            event.setCanceled(true);
            if (caster instanceof EntityPlayer) {
                ((EntityPlayer) caster).sendStatusMessage(new TextComponentTranslation("am2.tooltip.noMoreSummons"), false);
            }
        }
    }

    // -------------------------------------------------------------------------
    // EBWiz SpellCastEvent.Pre
    // -------------------------------------------------------------------------

    /**
     * Intercepts EBWiz spell-cast pre-events.
     *
     * <p>If the caster holds enough AM2 mana to cover the spell's cost the
     * wand's mana-cost modifier is zeroed, ensuring the wand is not drained.
     * The AM2 mana amount is stored and will be deducted in
     * {@link #onEBWizSpellCastPost}.  If the caster does not have enough AM2
     * mana the event is cancelled so the spell fails cleanly instead of
     * silently consuming wand mana as a free fallback.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEBWizSpellCastPre(electroblob.wizardry.event.SpellCastEvent.Pre event) {
        if (event.isCanceled()) return;

        EntityLivingBase caster = event.getCaster();
        if (caster == null) return;

        // AM2 Silence also prevents EBWiz spells – cancel before any mana checks.
        if (caster.isPotionActive(AMPotions.silence)) {
            event.setCanceled(true);
            return;
        }

        IEntityExtension am2Data = EntityExtension.For(caster);
        if (am2Data == null) return;

        // Apply discipline potency bonus to EBWiz spell modifiers
        float disciplinePotency = getDisciplinePotencyMultiplier(caster, event.getSpell().getElement().ordinal());
        if (disciplinePotency != 1.0f) {
            float currentPotency = event.getModifiers().get(SpellModifiers.POTENCY);
            event.getModifiers().set(SpellModifiers.POTENCY, currentPotency * disciplinePotency, false);
        }

        float costModifier = event.getModifiers().get(SpellModifiers.COST);
        float burnoutMultiplier = 1f + (am2Data.getCurrentBurnout() / Math.max(1f, am2Data.getMaxBurnout()));
        float disciplineCostMult = getDisciplineCostMultiplier(caster, event.getSpell());
        float am2ManaCost = event.getSpell().getCost() * costModifier * ArsMagica.config.getEBWizManaCostMultiplier() * burnoutMultiplier * disciplineCostMult;

        // Scrolls have no wand-mana fallback of their own, so by default they
        // require AM2 mana (configurable via EBWiz_Scroll_Requires_Mana).
        boolean requireAM2Mana = ArsMagica.config.getEBWizDisableWandMana() || (event.getSource() == electroblob.wizardry.event.SpellCastEvent.Source.SCROLL && ArsMagica.config.getEBWizScrollRequiresMana());
        if (requireAM2Mana) {
            // The spell can only proceed if the caster has enough AM2 mana;
            // otherwise cancel it outright.
            if (am2ManaCost <= 0 || !am2Data.hasEnoughMana(am2ManaCost)) {
                event.setCanceled(true);
                return;
            }
            event.getModifiers().set(SpellModifiers.COST, 0.0f, false);
            pendingManaDeductions.put(caster.getUniqueID(), am2ManaCost);
            return;
        }

        if (am2ManaCost <= 0) return; // spell has no AM2 cost, let wand handle it normally

        if (!am2Data.hasEnoughMana(am2ManaCost)) {
            // Not enough AM2 mana – cancel the cast outright instead of silently
            // falling back to wand mana, which would allow free infinite casts.
            event.setCanceled(true);
            return;
        }

        // AM2 mana will cover this cast – zero out the EBWiz wand cost so the
        // wand is not drained.  The third parameter (needsSyncing=false) means
        // this modifier change does not need to be sent to the client; the
        // server-side mana check and deduction are sufficient.
        event.getModifiers().set(SpellModifiers.COST, 0.0f, false);
        pendingManaDeductions.put(caster.getUniqueID(), am2ManaCost);
    }

    // -------------------------------------------------------------------------
    // AM2 SpellCastEvent.Pre
    // -------------------------------------------------------------------------

    /**
     * Intercepts AM2 spell-cast pre-events.
     * Applies EBWiz wizard-armour cost reductions (e.g. pyromancer robes
     * reducing the cost of fire spells) to AM2 spells based on their dominant
     * affinity.
     */
    @SubscribeEvent
    public void onAM2SpellCastPre(am2.api.event.SpellCastEvent.Pre event) {
        if (event.entityLiving == null || event.spell == null) return;
        event.manaCost *= getAM2SpellDiscount(event.entityLiving, event.spell);
    }

    /**
     * Computes the AM2 spell-cost multiplier contributed by equipped EBWiz
     * wizard armour for the given player and spell.
     */
    private static float getElementalCostReduction(ItemWizardArmour.ArmourClass armourClass) {
        switch (armourClass) {
            case WIZARD, WARLOCK:
                return 0.1f;
            case SAGE:
                return 0.2f;
            case BATTLEMAGE:
                return 0.05f;
            default:
                return 0.0f;
        }
    }

    public static float getAM2SpellDiscount(EntityLivingBase caster, SpellData data) {
        if (caster == null || data == null) return 1.0f;
        Affinity dominantAffinity = data.getMainShift();
        Spell representative = spellForAffinity(dominantAffinity);
        if (representative == null || representative == Spells.none) {
            return 1.0f;
        }

        float costModifier = 1.0f;
        boolean hasSageSet = true;
        boolean hasMana = true;
        Element setElement = null;

        for (EntityEquipmentSlot slot : InventoryUtils.ARMOUR_SLOTS) {
            ItemStack stack = caster.getItemStackFromSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemWizardArmour)) {
                hasSageSet = false;
                hasMana = false;
                continue;
            }

            ItemWizardArmour armour = (ItemWizardArmour) stack.getItem();
            if (setElement == null) setElement = armour.element;
            else if (setElement != armour.element) hasSageSet = false;

            if (armour.armourClass != ItemWizardArmour.ArmourClass.SAGE) {
                hasSageSet = false;
            }

            if (armour.isManaEmpty(stack)) {
                hasMana = false;
            }

            if (representative.getElement() == armour.element) {
                costModifier -= getElementalCostReduction(armour.armourClass);
            }
        }

        if (hasSageSet && hasMana && setElement != null) {
            if (representative.getElement() == setElement || representative.getElement() == Element.MAGIC || representative.getElement() == Element.HEALING) {
                costModifier = Math.min(costModifier, 0.8f);
            }
        }

        return Math.max(0.0f, costModifier);
    }
    // -------------------------------------------------------------------------
    // EBWiz SpellCastEvent.Post
    // -------------------------------------------------------------------------

    /**
     * Intercepts EBWiz spell-cast post-events.
     *
     * <p>If AM2 mana was pledged during the matching Pre event, it is
     * deducted here now that the spell has succeeded.
     */
    @SubscribeEvent
    public void onEBWizSpellCastPost(electroblob.wizardry.event.SpellCastEvent.Post event) {
        EntityLivingBase caster = event.getCaster();
        if (caster == null) return;

        Float am2ManaCost = pendingManaDeductions.remove(caster.getUniqueID());
        if (am2ManaCost == null) return;

        IEntityExtension am2Data = EntityExtension.For(caster);
        if (am2Data == null) {
            return;
        }
        am2Data.deductMana(am2ManaCost);
        float burnout = am2ManaCost * ArsMagica.config.getManaBurnoutRatio();
        am2Data.setCurrentBurnout(Math.min(am2Data.getMaxBurnout(), am2Data.getCurrentBurnout() + burnout));

        // Grant magic XP proportional to the mana spent, using the same log-
        // based approximation AM2 uses for its own spell components.
        // Unlike AM2 projectile spells (which grant XP only on hit in the old
        // XP system), we cannot track EBWiz projectile hits, so XP is always
        // granted immediately on a successful cast regardless of oldXpCalculations.
        if (am2ManaCost > 0) {
            float xp = (float) (Math.log(am2ManaCost) * ArsMagica.config.getEBWizMagicXPMultiplier());
            if (xp > 0) am2Data.addMagicXP(xp);
        }

        // Apply affinity gain for casting EBWiz spells.  Uses the same
        // resolveWeights() spell-to-affinity lookup as ItemEBWizSpellBinding,
        // distributing the configured gain amount proportionally across all
        // weighted affinities for the spell.
        // Guard with isRemote: EBWiz fires spell events on both sides; running
        // this only server-side matches how native AM2 components handle it.
        if (!caster.world.isRemote) {
            if (ArsMagica.config.getEBWizAffinityGainEnabled()) {
                AffinityData affinityData = AffinityData.For(caster);
                if (affinityData != null && !affinityData.isLocked()) {
                    List<AMConfig.WeightedAffinity> weights = resolveWeights(event.getSpell());
                    float totalWeight = 0f;
                    for (AMConfig.WeightedAffinity wa : weights) totalWeight += wa.weight;
                    if (totalWeight > 0f) {
                        float gainAmount = (float) ArsMagica.config.getEBWizAffinityGainAmount();
                        for (AMConfig.WeightedAffinity wa : weights) {
                            Affinity aff = lookupAffinity(wa.affinityName);
                            affinityData.incrementAffinity(aff, gainAmount * wa.weight / totalWeight);
                        }
                    }
                }
            }

            // Affinity *ability* effects (e.g. Clearcaster) normally trigger off
            // am2.api.event.SpellCastEvent.Post, which is only fired by AM2's own
            // SpellCaster and never by EBWiz. Invoke the ability directly here
            // (bypassing the event bus, not re-posting AM2's event) so it still
            // procs for EBWiz-cast spells. Clearcaster's applySpellCast() only
            // reads entityLiving/world, so a null SpellData is safe here.
            if (caster instanceof EntityPlayer) {
                AbstractAffinityAbility clearCaster = ArsMagicaAPI.getAffinityAbilityRegistry()
                        .getValue(new ResourceLocation("arsmagica2", "clearcaster"));
                if (clearCaster != null && clearCaster.canApply((EntityPlayer) caster)) {
                    clearCaster.applySpellCast((EntityPlayer) caster, new am2.api.event.SpellCastEvent.Post(caster, null, 0f));
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------------------------

    /**
     * Removes any pending mana deduction entry when a player logs out,
     * guarding against edge cases where a Pre event fired but Post did not
     * (e.g. due to a disconnection mid-cast or a third-party cancellation
     * happening after our Pre handler ran).
     */
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        pendingManaDeductions.remove(event.player.getUniqueID());
    }

    // -------------------------------------------------------------------------
    // Tooltip filtering
    // -------------------------------------------------------------------------

    /**
     * Strips the EBWiz mana line ({@code item.ebwizardry:wand.mana}) and the
     * vanilla durability line ({@code item.durability}) from wand tooltips
     * when {@code EBWiz_Hide_Wand_Mana_Tooltip} is enabled.
     *
     * <p>Uses locale-aware prefix matching so the correct lines are removed
     * regardless of the active language pack.
     */
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        if (stack.getItem() instanceof ItemWizardArmour) {
            ItemWizardArmour armour = (ItemWizardArmour) stack.getItem();
            List<Affinity> buffs = new ArrayList<>();
            for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValuesCollection()) {
                if (aff == Affinities.none) continue;
                Spell rep = spellForAffinity(aff);
                if (rep != null && rep != Spells.none) {
                    if (rep.getElement() == armour.element) {
                        buffs.add(aff);
                    } else if (armour.armourClass == ItemWizardArmour.ArmourClass.SAGE && (rep.getElement() == Element.MAGIC || rep.getElement() == Element.HEALING)) {
                        if (!buffs.contains(aff)) buffs.add(aff);
                    }
                }
            }

            if (!buffs.isEmpty()) {
                event.getToolTip().add("");
                event.getToolTip().add(TextFormatting.DARK_AQUA + "AM2 Affinity Bonus:");
                for (Affinity aff : buffs) {
                    event.getToolTip().add(TextFormatting.GRAY + " - " + aff.getLocalizedName());
                }
            }
        }

        if (!ArsMagica.config.getEBWizHideWandTooltip()) return;
        if (!"electroblob.wizardry.item.ItemWand".equals(stack.getItem().getClass().getName())) return;

        // Derive the locale-correct prefix for each line by formatting the
        // translation key with sentinel characters, then taking the part that
        // comes before the first sentinel.
        String manaFmt = I18n.translateToLocalFormatted("item.ebwizardry:wand.mana", "\u0000", "\u0001");
        String manaPrefix = manaFmt.split("[\u0000\u0001]")[0];

        String durFmt = I18n.translateToLocalFormatted("item.durability", "\u0000", "\u0001");
        String durPrefix = durFmt.split("[\u0000\u0001]")[0];

        event.getToolTip().removeIf(line -> {
            String plain = TextFormatting.getTextWithoutFormattingCodes(line);
            return plain != null && (plain.startsWith(manaPrefix) || plain.startsWith(durPrefix));
        });
    }

    // -------------------------------------------------------------------------
    // Spell-book binding helpers (called from ContainerSpellBook)
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if {@code stack} is an EBWiz {@code ItemSpellBook}
     * and the config allows binding EBWiz spells into AM2 spell books.
     *
     * <p>Uses a class-name string comparison rather than {@code instanceof} to
     * avoid loading the EBWiz class when EBWiz is absent (safe soft-dep
     * pattern: the EBWiz class is only loaded if this method returns
     * {@code true}).
     */
    public static boolean isEBWizSpellBookItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!ArsMagica.config.getEBWizSpellsInSpellBook()) return false;
        if (!Loader.isModLoaded(EBWizardryCompatBootstrap.MODID)) return false;
        return "electroblob.wizardry.item.ItemSpellBook".equals(stack.getItem().getClass().getName());
    }

    /**
     * Opens EBWiz book GUIs used by AM2 lectern interactions.
     * Returns true if a supported GUI was opened.
     */
    @SideOnly(Side.CLIENT)
    public static boolean openLecternGuiClient(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null || !"ebwizardry".equals(id.getNamespace())) return false;

        if ("spell_book".equals(id.getPath())) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiSpellBook(stack));
            return true;
        }

        if ("wizard_handbook".equals(id.getPath())) {
            if (Wizardry.settings.loadHandbook) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiWizardHandbook());
            }
            return true;
        }

        return false;
    }

    /**
     * Converts an EBWiz {@code ItemSpellBook} stack into an
     * {@link ItemEBWizSpellBinding} stack so it can be stored in and cast from
     * an AM2 spell book slot.
     *
     * <p>Returns {@code stack} unchanged if it is not an EBWiz spell book or
     * EBWiz is not loaded.  This method is safe to call unconditionally; the
     * EBWiz class references inside are only evaluated after
     * {@link #isEBWizSpellBookItem} has confirmed EBWiz is present.
     */
    public static ItemStack convertEBWizSpellBook(ItemStack stack) {
        if (!isEBWizSpellBookItem(stack)) return stack;
        // Safe to reference EBWiz classes here – EBWiz is confirmed loaded.
        Spell spell = Spell.byMetadata(stack.getMetadata());
        if (spell == null || spell == Spells.none) return stack;
        return ItemEBWizSpellBinding.createForSpell(spell);
    }

    /**
     * Returns the amount of neutral etherium ({@link AMItems#etherium} with neutral metadata)
     * required to transcribe an EBWiz {@code ItemSpellBook} at the AM2 crafting altar.
     *
     * <p>Cost formula: {@code 500 + (tierOrdinal + 1) * 500}, i.e. a flat 500 base
     * plus 500 per tier level:
     * <ul>
     *   <li>NOVICE     – 1 000</li>
     *   <li>APPRENTICE – 1 500</li>
     *   <li>ADVANCED   – 2 000</li>
     *   <li>MASTER     – 2 500</li>
     * </ul>
     * <p>
     * /**
     * Returns the amount of neutral etherium required to transcribe an EBWiz spell book
     * at the AM2 Crafting Altar.  Both the base cost and the per-tier increment are
     * configured via {@link AMConfig}:
     * {@code EBWiz_Transcription_Cost_Base} (default 500) and
     * {@code EBWiz_Transcription_Cost_Per_Tier} (default 500).
     *
     * <p>Formula: {@code base + (tierOrdinal + 1) * perTier}
     *
     * <p>Returns {@code 0} if {@code stack} is not a valid EBWiz spell book.
     */
    public static int getEBWizSpellBookEssenceCost(ItemStack stack) {
        if (!isEBWizSpellBookItem(stack)) return 0;
        Spell spell = Spell.byMetadata(stack.getMetadata());
        if (spell == null || spell == Spells.none) return 0;
        Tier tier = spell.getTier();
        int base = ArsMagica.config != null ? ArsMagica.config.getEBWizTranscriptionCostBase() : 500;
        int perTier = ArsMagica.config != null ? ArsMagica.config.getEBWizTranscriptionCostPerTier() : 500;
        return base + (tier.ordinal() + 1) * perTier;
    }

    /**
     * Creates an {@link ItemEBWizSpellBinding} from a Written Book produced by
     * the Scribing Desk in EBWiz mode. Reads the spell registry name from the
     * {@code EBWizSpell} NBT key and copies the {@code AM2Modifiers} compound.
     */
    /**
     * Returns the registry name string (e.g. {@code "ebwizardry:fire_bolt"}) of
     * the spell stored in an EBWiz {@code ItemSpellBook}, or an empty string if
     * the stack is not a valid EBWiz spell book.
     */
    public static String getEBWizSpellBookRegistryName(ItemStack stack) {
        if (!isEBWizSpellBookItem(stack)) return "";
        Spell spell = Spell.byMetadata(stack.getMetadata());
        if (spell == null || spell == Spells.none) return "";
        return spell.getRegistryName().toString();
    }

    /**
     * Returns the localised display name of the spell in an EBWiz {@code ItemSpellBook},
     * or an empty string if the stack is not a valid EBWiz spell book.
     */
    public static String getEBWizSpellBookDisplayName(ItemStack stack) {
        if (!isEBWizSpellBookItem(stack)) return "";
        Spell spell = Spell.byMetadata(stack.getMetadata());
        if (spell == null || spell == Spells.none) return "";
        return spell.getDisplayName();
    }

    // -------------------------------------------------------------------------
    // Affinity weight helpers (also used by ItemEBWizSpellBinding)
    // -------------------------------------------------------------------------

    /**
     * Resolves the weighted affinity list for an EBWiz spell.
     * <ol>
     *   <li>Checks the per-spell override map (keyed by full registry name, e.g.
     *       {@code "ebwizardry:blink"}).</li>
     *   <li>Falls back to the element default map (keyed by element enum name, e.g.
     *       {@code "FIRE"}, {@code "HEALING"}).</li>
     *   <li>Final fallback: {@code arcane:100}.</li>
     * </ol>
     */
    public static List<AMConfig.WeightedAffinity> resolveWeights(Spell spell) {
        // 1. Per-spell override
        if (spell.getRegistryName() != null) {
            List<AMConfig.WeightedAffinity> override = ArsMagica.config.getEBWizSpellAffinityOverrides().get(spell.getRegistryName().toString());
            if (override != null && !override.isEmpty()) return override;
        }
        // 2. Element default
        List<AMConfig.WeightedAffinity> elementList = ArsMagica.config.getEBWizElementAffinityMap().get(spell.getElement().name());
        if (elementList != null && !elementList.isEmpty()) return elementList;
        // 3. Final fallback
        return Collections.singletonList(new AMConfig.WeightedAffinity("arcane", 100f));
    }

    /**
     * Maps an affinity name string (case-insensitive) to the corresponding
     * AM2 {@link Affinity} object.  Unknown names default to {@link Affinities#arcane}.
     */
    public static Affinity lookupAffinity(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "fire":
                return Affinities.fire;
            case "ice":
                return Affinities.ice;
            case "lightning":
                return Affinities.lightning;
            case "earth":
                return Affinities.earth;
            case "water":
                return Affinities.water;
            case "life":
                return Affinities.life;
            case "nature":
                return Affinities.nature;
            case "ender":
                return Affinities.ender;
            default:
                return Affinities.arcane;
        }
    }

    /**
     * Spawns EBWizardry frost particles (snowflakes and ice shards) at the given position.
     * Must only be called client-side ({@code world.isRemote} must be {@code true}).
     *
     * @param count    The number of snowflake particles to spawn; roughly one ice shard is added per 3 snowflakes.
     * @param spread   Half-width of the random position offset applied around (x, y, z). Pass 0 to spawn exactly at the given position.
     * @param velScale Multiplier on random velocity (0–1). Pass 0 for stationary particles, 1 for full drift.
     */
    public static void spawnFrostParticles(World world, double x, double y, double z, int count, Random rand, double spread, double velScale) {
        for (int i = 0; i < count; i++) {
            double vx = (rand.nextDouble() * 0.2 - 0.1) * velScale;
            double vy = rand.nextDouble() * 0.1 * velScale;
            double vz = (rand.nextDouble() * 0.2 - 0.1) * velScale;
            double ox = spread > 0 ? rand.nextDouble() * spread * 2 - spread : 0;
            double oy = spread > 0 ? rand.nextDouble() * spread : 0;
            double oz = spread > 0 ? rand.nextDouble() * spread * 2 - spread : 0;
            ParticleBuilder.create(ParticleBuilder.Type.SNOW).pos(x + ox, y + oy, z + oz).vel(vx, vy, vz).spawn(world);
            if (i % 3 == 0) {
                ParticleBuilder.create(ParticleBuilder.Type.ICE).pos(x + ox, y + oy, z + oz).vel(vx, vy, vz).spawn(world);
            }
        }
    }

    /**
     * Overload with explicit spread, default velocity scale of 1.
     */
    public static void spawnFrostParticles(World world, double x, double y, double z, int count, Random rand, double spread) {
        spawnFrostParticles(world, x, y, z, count, rand, spread, 1.0);
    }

    /**
     * Convenience overload with default spread of 0.4 blocks and full velocity.
     */
    public static void spawnFrostParticles(World world, double x, double y, double z, int count, Random rand) {
        spawnFrostParticles(world, x, y, z, count, rand, 0.4, 1.0);
    }

    public static void spawnDirectedFrostParticle(World world, double x, double y, double z, double vx, double vy, double vz) {
        ParticleBuilder.create(ParticleBuilder.Type.SNOW).pos(x, y, z).vel(vx, vy, vz).spawn(world);
        if (world.rand.nextInt(3) == 0) {
            ParticleBuilder.create(ParticleBuilder.Type.ICE).pos(x, y, z).vel(vx, vy, vz).spawn(world);
        }
    }

    /**
     * Spawns one EBWiz {@code CLOUD} particle at the given position with the given velocity.
     * Intended for Witchwood ground mist. Only call from the compat package.
     */
    public static void spawnMistCloudParticle(World world, double x, double y, double z, double vx, double vy, double vz, float scale, int lifetime) {
        ParticleBuilder.create(ParticleBuilder.Type.CLOUD).pos(x, y, z).vel(vx, vy, vz).clr(0.82f, 0.85f, 0.92f).scale(scale).time(lifetime).spawn(world);
    }

    /**
     * Applies the EBWizardry {@code frost} potion effect to the given entity.
     * Only call this from within the compat package (EBWiz classes are referenced directly).
     */
    public static void applyFrostEffect(EntityLivingBase target, int durationTicks, int amplifier) {
        target.addPotionEffect(new PotionEffect(WizardryPotions.frost, durationTicks, amplifier, false, false));
    }

    /**
     * Checks both hand slots of {@code player} for an EBWiz wand with a
     * Condenser upgrade and returns the cumulative mana-regen speed bonus
     * (fraction of regen-ticks to subtract). Only the highest-level wand
     * (main hand or off hand) contributes; two wands do not double-dip.
     */
    public static float getCondenserRegenMultiplier(EntityPlayer player) {
        int maxLevel = 0;
        for (EnumHand hand : EnumHand.values()) {
            ItemStack held = player.getHeldItem(hand);
            if (held.isEmpty()) continue;
            if (!(held.getItem() instanceof ItemWand)) continue;
            int level = WandHelper.getUpgradeLevel(held, WizardryItems.condenser_upgrade);
            if (level > maxLevel) maxLevel = level;
        }
        if (maxLevel <= 0) return 0f;
        float bonusPerLevel = (float) ArsMagica.config.getEBWizCondenserManaRegenBonusPerLevel();
        return Math.min(0.9f, maxLevel * bonusPerLevel);
    }

    /**
     * Returns 0.2 if the player has the EBWiz {@code ring_condensing} artefact
     * active, otherwise 0. Used as a fraction to subtract from 1 before
     * multiplying {@code regenTicks} (i.e. 20% faster AM2 mana regen).
     */
    public static float getRingCondensingRegenMultiplier(EntityPlayer player) {
        if (ItemArtefact.isArtefactActive(player, WizardryItems.ring_condensing)) {
            return 0.20f;
        }
        return 0f;
    }

    public static ItemStack createBindingFromBook(ItemStack book) {
        if (!book.hasTagCompound()) return ItemStack.EMPTY;
        NBTTagCompound tag = book.getTagCompound();
        String spellName = tag.getString("EBWizSpell");
        if (spellName.isEmpty()) return ItemStack.EMPTY;
        Spell spell = Spell.registry.getValue(new ResourceLocation(spellName));
        if (spell == null || spell == Spells.none) return ItemStack.EMPTY;
        ItemStack binding = ItemEBWizSpellBinding.createForSpell(spell);
        if (tag.hasKey("AM2Modifiers")) {
            if (!binding.hasTagCompound()) binding.setTagCompound(new NBTTagCompound());
            binding.getTagCompound().setTag("AM2Modifiers", tag.getCompoundTag("AM2Modifiers").copy());
        }
        return binding;
    }

    /**
     * Creates the EBWiz-aware spell book item.
     * Only called from {@link EBWizardryCompatBootstrap#createSpellBookItem()} after
     * confirming EBWiz is loaded.
     */
    public static Item createSpellBookItem() {
        try {
            return SPELLBOOK_ITEM_FACTORY.get().get();
        } catch (Throwable ignored) {
        }
        return new ItemSpellBook();
    }

    /**
     * Registers all EBWiz-exclusive spell parts.
     * Only called from {@link EBWizardryCompatBootstrap#registerSpellParts} after confirming
     * EBWiz is loaded, so direct EBWiz class references here are safe.
     */
    public static void registerSpellParts(IForgeRegistry<SpellPart> registry) {
        SpellRegistryHelper.registerSpellModifier(registry, "ebwiz_blast", new ResourceLocation(ArsMagica.MODID, "items/spells/modifiers/ebwiz_blast"), SkillPoint.SILVER_POINT, new EBWizBlast(), SkillTrees.TREE_OFFENSE, 75, 270);
        SpellRegistryHelper.registerSpellComponent(registry, "ice_statue", new ResourceLocation(ArsMagica.MODID, "items/spells/components/ice_statue"), SkillPoint.RED_SKILL_POINT, new IceStatue(), SkillTrees.TREE_OFFENSE, 75, 225);
        SpellRegistryHelper.registerSpellComponent(registry, "cobweb_spell", new ResourceLocation(ArsMagica.MODID, "items/spells/components/cobweb_spell"), SkillPoint.GREEN_SKILL_POINT, new PlaceTemporaryBlock(WizardryBlocks.vanishing_cobweb, 400, 40, () -> Sets.newHashSet(Affinities.nature), 0.03f, Blocks.WEB), SkillTrees.TREE_DEFENSE, 132, 290, "arsmagica2:entangle");
        SpellRegistryHelper.registerSpellComponent(registry, "metamorphosis", new ResourceLocation(ArsMagica.MODID, "items/spells/components/metamorphosis"), SkillPoint.GREEN_SKILL_POINT, new Metamorphosis(), SkillTrees.TREE_OFFENSE, 75, 315);
    }

    /**
     * Registers EBWiz-only items into the item registry.
     * Only called from {@link EBWizardryCompatBootstrap#registerEBWizItems} after
     * confirming EBWiz is loaded, so referencing {@link ItemEBWizSpellBinding} here is safe.
     */
    public static void registerEBWizItems(IForgeRegistry<Item> registry) {
        AMItems.registerItem(registry, "ebwiz_spell_binding", ArsMagica.MODID, new ItemEBWizSpellBinding(), true);
    }

    /**
     * Returns the EBWiz-specific crafting-altar ingredient defaults for AM2 spell parts.
     * These replace the AM2 rune-based defaults in {@code spell_parts.cfg} when EBWiz is
     * loaded, substituting EBWiz {@code magic_crystal} items (by element/meta) for whatever
     * AM2-specific item previously acted as the "type indicator" in each recipe.
     *
     * <p>Meta values for {@code ebwizardry:magic_crystal}:
     * 0 = Magic (arcane) · 1 = Fire · 2 = Ice · 3 = Lightning · 4 = Necromancy ·
     * 5 = Earth · 6 = Sorcery · 7 = Healing
     *
     * <p>Ingredient string format matches {@code spell_parts.cfg}:
     * {@code modid:item} (meta 0) or {@code modid:item:meta} (meta N).
     *
     * <p>Only called from {@link EBWizardryCompatBootstrap#getEBWizRecipeDefaults()} after
     * confirming EBWiz is loaded.
     */
    public static Map<String, String[]> getEBWizRecipeDefaults() {
        Map<String, String[]> map = new LinkedHashMap<>();

        // ---- Fire (magic_crystal:1) ----------------------------------------
        // fire_rain uses 2x essence_fire in its recipe – not substituted.
        map.put("arsmagica2:fire_damage", r("ebwizardry:magic_crystal:1", "minecraft:flint_and_steel", "arsmagica2:vinteum_dust"));
        map.put("arsmagica2:ignition", r("ebwizardry:magic_crystal:1", "minecraft:flint_and_steel"));
        map.put("arsmagica2:forge", r("ebwizardry:magic_crystal:1", "minecraft:furnace"));

        // ---- Ice (magic_crystal:2) ------------------------------------------
        // blizzard uses 2x essence_ice in its recipe – not substituted.
        map.put("arsmagica2:frost_damage", r("ebwizardry:magic_crystal:2", "minecraft:snowball", "arsmagica2:blue_topaz"));
        map.put("arsmagica2:freeze", r("ebwizardry:magic_crystal:2", "minecraft:snow"));
        map.put("arsmagica2:drown", r("ebwizardry:magic_crystal:2", "ebwizardry:magic_crystal:2", "minecraft:water_bucket", "minecraft:string", "arsmagica2:blue_topaz"));
        map.put("arsmagica2:create_water", r("ebwizardry:magic_crystal:2", "minecraft:water_bucket"));
        map.put("arsmagica2:water_breathing", r("ebwizardry:magic_crystal:2", "minecraft:reeds"));
        map.put("arsmagica2:swift_swim", r("ebwizardry:magic_crystal:2", "minecraft:fish", "minecraft:fishing_rod"));
        map.put("arsmagica2:watery_grave", r("ebwizardry:magic_crystal:2", "minecraft:stone", "minecraft:leather_boots"));

        // ---- Lightning (magic_crystal:3) ------------------------------------
        map.put("arsmagica2:lightning_damage", r("ebwizardry:magic_crystal:3", "minecraft:iron_ingot", "minecraft:stick", "arsmagica2:vinteum_dust"));
        map.put("arsmagica2:storm", r("ebwizardry:magic_crystal:3", "arsmagica2:blue_topaz", "minecraft:ghast_tear"));

        // ---- Necromancy (magic_crystal:4) -----------------------------------
        map.put("arsmagica2:life_drain", r("ebwizardry:magic_crystal:4", "arsmagica2:sunstone"));
        map.put("arsmagica2:life_tap", r("ebwizardry:magic_crystal:4", "arsmagica2:aum"));

        // ---- Earth (magic_crystal:5) ----------------------------------------
        map.put("arsmagica2:entangle", r("ebwizardry:magic_crystal:5", "minecraft:vine", "minecraft:slime_ball"));
        map.put("arsmagica2:grow", r("ebwizardry:magic_crystal:5", "minecraft:dye:15"));
        map.put("arsmagica2:plant", r("ebwizardry:magic_crystal:5", "minecraft:wheat_seeds", "minecraft:sapling:*", "minecraft:wheat_seeds"));
        map.put("arsmagica2:physical_damage", r("ebwizardry:magic_crystal:5", "minecraft:iron_sword"));
        map.put("arsmagica2:knockback", r("ebwizardry:magic_crystal:5", "minecraft:piston"));
        map.put("arsmagica2:plow", r("ebwizardry:magic_crystal:5", "minecraft:stone_hoe"));
        map.put("arsmagica2:harvest_plants", r("ebwizardry:magic_crystal:5", "minecraft:shears"));
        map.put("arsmagica2:gravity_well", r("ebwizardry:magic_crystal:5", "minecraft:string", "minecraft:stone"));

        // ---- Sorcery (magic_crystal:6) --------------------------------------
        map.put("arsmagica2:blink", r("ebwizardry:magic_crystal:6", "minecraft:ender_pearl"));
        map.put("arsmagica2:random_teleport", r("ebwizardry:magic_crystal:6", "minecraft:ender_pearl"));
        map.put("arsmagica2:transplace", r("ebwizardry:magic_crystal:6", "minecraft:compass", "ebwizardry:magic_crystal:6", "minecraft:ender_pearl"));
        map.put("arsmagica2:recall", r("ebwizardry:magic_crystal:6", "minecraft:compass", "minecraft:map", "minecraft:ender_pearl"));
        map.put("arsmagica2:astral_distortion", r("ebwizardry:magic_crystal:6", "minecraft:ender_eye"));
        map.put("arsmagica2:flight", r("ebwizardry:magic_crystal:6", "minecraft:nether_star", "minecraft:ghast_tear"));
        map.put("arsmagica2:levitate", r("ebwizardry:magic_crystal:6", "arsmagica2:tarma_root"));
        map.put("arsmagica2:leap", r("ebwizardry:magic_crystal:6", "minecraft:hopper"));
        map.put("arsmagica2:slowfall", r("ebwizardry:magic_crystal:6", "minecraft:feather"));
        map.put("arsmagica2:fling", r("ebwizardry:magic_crystal:6", "minecraft:piston"));
        map.put("arsmagica2:rift", r("ebwizardry:magic_crystal:6", "ebwizardry:magic_crystal", "minecraft:chest", "minecraft:ender_eye"));
        map.put("arsmagica2:charm", r("ebwizardry:magic_crystal:6", "arsmagica2:essence_life", "arsmagica2:crystal_phylactery"));
        map.put("arsmagica2:invisibility", r("ebwizardry:magic_crystal:6", "arsmagica2:chimerite", "ore:listAllpotionItem"));
        map.put("arsmagica2:slow", r("ebwizardry:magic_crystal:6", "minecraft:slime_ball"));
        map.put("arsmagica2:haste", r("ebwizardry:magic_crystal:6", "minecraft:redstone", "minecraft:glowstone_dust"));
        map.put("arsmagica2:accelerate", r("ebwizardry:magic_crystal:6", "minecraft:leather_boots", "minecraft:redstone"));
        map.put("arsmagica2:mana_drain", r("ebwizardry:magic_crystal:6", "arsmagica2:moonstone", "arsmagica2:vinteum_dust"));
        map.put("arsmagica2:silence", r("ebwizardry:magic_crystal:6", "arsmagica2:arcane_ash", "minecraft:jukebox", "ebwizardry:magic_crystal:6"));
        map.put("arsmagica2:disarm", r("ebwizardry:magic_crystal:6", "minecraft:iron_sword"));
        map.put("arsmagica2:telekinesis", r("ebwizardry:magic_crystal:6", "minecraft:sticky_piston", "minecraft:chest"));

        // ---- Healing (magic_crystal:7) --------------------------------------
        map.put("arsmagica2:heal", r("ebwizardry:magic_crystal:7", "arsmagica2:aum"));
        map.put("arsmagica2:regeneration", r("ebwizardry:magic_crystal:7", "minecraft:golden_apple"));
        map.put("arsmagica2:absorption", r("ebwizardry:magic_crystal:7", "minecraft:golden_apple", "minecraft:shield"));
        map.put("arsmagica2:night_vision", r("ebwizardry:magic_crystal:7", "ore:listAllpotionItem"));
        map.put("arsmagica2:true_sight", r("ebwizardry:magic_crystal:7", "arsmagica2:chimerite", "minecraft:glass_pane"));

        // ---- Magic / Arcane (magic_crystal:0) --------------------------------
        // falling_star uses 2x essence_arcane in its recipe – not substituted.
        map.put("arsmagica2:magic_damage", r("ebwizardry:magic_crystal", "minecraft:dye:4", "minecraft:book", "minecraft:stone_sword"));
        map.put("arsmagica2:mana_blast", r("ebwizardry:magic_crystal", "arsmagica2:mana_focus", "arsmagica2:greater_focus"));
        map.put("arsmagica2:dispel", r("ebwizardry:magic_crystal", "arsmagica2:arcane_ash", "arsmagica2:blue_topaz", "minecraft:milk_bucket"));
        map.put("arsmagica2:light", r("ebwizardry:magic_crystal", "arsmagica2:cerublossom", "minecraft:torch", "arsmagica2:vinteum_torch"));
        map.put("arsmagica2:attract", r("ebwizardry:magic_crystal", "minecraft:iron_ingot"));
        map.put("arsmagica2:repel", r("ebwizardry:magic_crystal", "minecraft:water_bucket"));
        map.put("arsmagica2:shield", r("ebwizardry:magic_crystal", "minecraft:iron_chestplate"));
        map.put("arsmagica2:reflect", r("ebwizardry:magic_crystal", "minecraft:glass", "minecraft:iron_block"));
        map.put("arsmagica2:mana_shield", r("ebwizardry:magic_crystal", "arsmagica2:mana_focus", "arsmagica2:battlemage_chestplate", "arsmagica2:mage_robe"));

        return map;
    }

    /**
     * Convenience varargs helper for building recipe string arrays.
     */
    private static String[] r(String... items) {
        return items;
    }

    // -------------------------------------------------------------------------
    // Artefact potency → AM2 spell power
    // -------------------------------------------------------------------------

    /**
     * Computes the AM2 spell-damage multiplier contributed by active EBWiz
     * artefacts for the given player.
     *
     * <p>Internally fires a fake {@link electroblob.wizardry.event.SpellCastEvent.Pre}
     * (using {@link Spells#none} as the sentinel spell)
     * through {@link ItemArtefact#onSpellCastPreEvent} so that
     * all active artefacts have a chance to apply their {@link SpellModifiers#POTENCY}
     * bonus. Spell-specific artefacts (e.g. {@code ring_fire_biome}) will not
     * match the sentinel element and therefore do not contribute, which keeps
     * this a lightweight "basic" check covering only universal potency artefacts
     * such as {@code ring_battlemage}.
     *
     * <p>The {@code ratio} parameter controls how much of the EBWiz potency excess
     * (above {@code 1.0}) is translated into an AM2 damage bonus. A ratio of
     * {@code 0.5f} means that a {@code 1.1×} EBWiz potency becomes a {@code 1.05×}
     * AM2 damage multiplier.
     *
     * @param player the player whose equipped artefacts are evaluated
     * @param ratio  how much of the EBWiz potency excess carries over; {@code 0} disables
     * @return a multiplier ≥ {@code 1.0f} to apply to AM2 spell damage
     */
    public static float computeArtefactPotencyMultiplier(EntityPlayer player, float ratio, @Nullable Affinity dominantAffinity) {
        SpellModifiers fakeModifiers = new SpellModifiers();
        fakeModifiers.set(SpellModifiers.POTENCY, 1.0f, false);
        Spell representative = spellForAffinity(dominantAffinity);
        electroblob.wizardry.event.SpellCastEvent.Pre fakeEvent = new electroblob.wizardry.event.SpellCastEvent.Pre(electroblob.wizardry.event.SpellCastEvent.Source.WAND, representative, player, fakeModifiers);
        // Invoke ItemArtefact's handler directly instead of posting to the shared Forge event
        // bus. Posting a real SpellCastEvent.Pre here is indistinguishable from an actual wand
        // cast to every other listener on the bus - notably EBWiz's own Forfeit.onSpellCastPreEvent
        // (spell-discovery mechanic), which would roll its forfeit chance against this fake event
        // since the sentinel spell is never "discovered", spuriously punishing the player.
        ItemArtefact.onSpellCastPreEvent(fakeEvent);
        float potency = fakeModifiers.get(SpellModifiers.POTENCY);
        if (potency <= 1.0f) return 1.0f;
        return 1.0f + (potency - 1.0f) * ratio;
    }

    private static Spell spellForAffinity(@Nullable Affinity affinity) {
        if (affinity == null || affinity == Affinities.none) return Spells.magic_missile;
        if (affinity == Affinities.fire) return Spells.fireball;
        if (affinity == Affinities.ice) return Spells.ice_shard;
        if (affinity == Affinities.lightning) return Spells.arc;
        if (affinity == Affinities.earth) return Spells.dart;
        if (affinity == Affinities.life) return Spells.heal;
        if (affinity == Affinities.ender) return Spells.summon_zombie;
        if (affinity == Affinities.arcane) return Spells.telekinesis;
        // water, nature, arcane, ender, and air have no direct EBWiz element
        return Spells.magic_missile;
    }

    /**
     * Adds an {@link EntityAIEBWizArcAttack} AI task to the given entity so it
     * will periodically cast the EBWiz Arc spell at its attack target.
     *
     * @param entity   the entity to add the AI task to
     * @param priority the AI task priority
     * @param cooldown cooldown in ticks between casts
     */
    public static void addArcAttackAI(EntityLiving entity, int priority, int cooldown) {
        entity.tasks.addTask(priority, new EntityAIEBWizArcAttack(entity, cooldown));
    }

    /**
     * Registers AM2 book textures for the EBWiz bookshelf block model.
     * Must be called during preInit (before block registration) so the slot
     * count is correct when {@link BlockBookshelf}
     * is registered.
     */
    public static void registerBookshelfModelTextures() {
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_ender, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_ender"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_earth, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_earth"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_fire, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_fire"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_life, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_life"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_nature, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_nature"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_ice, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_ice"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_lightning, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_lightning"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_air, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_air"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_water, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_water"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_arcane, new ResourceLocation(ArsMagica.MODID, "blocks/books_affinity_tome_arcane"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.arcane_compendium, new ResourceLocation("ebwizardry", "blocks/books_purple"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.evil_book, new ResourceLocation("ebwizardry", "blocks/books_purple"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.journal, new ResourceLocation("ebwizardry", "blocks/books_purple"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.spellbook, new ResourceLocation("ebwizardry", "blocks/books_brown"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.arcane_spellbook, new ResourceLocation("ebwizardry", "blocks/books_purple"));
        BlockBookshelf.registerBookModelTexture(() -> AMItems.affinity_tome_none, new ResourceLocation("ebwizardry", "blocks/books_purple"));
    }

    /**
     * Registers AM2 book items as valid items for the EBWiz bookshelf container.
     * Must be called during init (after item registry).
     */
    public static void registerBookshelfItems() {
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_none);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_ender);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_earth);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_fire);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_life);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_nature);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_ice);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_lightning);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_air);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_water);
        ContainerBookshelf.registerBookItem(AMItems.affinity_tome_arcane);
        ContainerBookshelf.registerBookItem(AMItems.arcane_compendium);
        ContainerBookshelf.registerBookItem(AMItems.evil_book);
        ContainerBookshelf.registerBookItem(AMItems.journal);
        ContainerBookshelf.registerBookItem(AMItems.spellbook);
        ContainerBookshelf.registerBookItem(AMItems.arcane_spellbook);
    }

    // -------------------------------------------------------------------------
    // EBWiz Discovery Ritual helpers
    // -------------------------------------------------------------------------

    /**
     * Parses a config string array of {@code modid:item=value} or {@code modid:item:meta=value}
     * entries and looks up the given stack against them.
     *
     * @return the mapped integer value, or {@code -1} if no match found
     */
    private static int lookupItemInConfig(ItemStack stack, String[] configEntries) {
        if (stack.isEmpty() || configEntries == null) return -1;
        ResourceLocation regName = stack.getItem().getRegistryName();
        if (regName == null) return -1;
        String itemId = regName.toString();
        int stackMeta = stack.getMetadata();

        for (String entry : configEntries) {
            int eqIdx = entry.lastIndexOf('=');
            if (eqIdx < 0) continue;
            String itemPart = entry.substring(0, eqIdx).trim();
            String valuePart = entry.substring(eqIdx + 1).trim();
            int value;
            try {
                value = Integer.parseInt(valuePart);
            } catch (NumberFormatException e) {
                continue;
            }

            // Parse item part: modid:item or modid:item:meta
            // Split on colon but need to handle modid:item vs modid:item:meta
            int firstColon = itemPart.indexOf(':');
            if (firstColon < 0) continue;
            int secondColon = itemPart.indexOf(':', firstColon + 1);
            String cfgItemId;
            int cfgMeta = -1; // -1 means wildcard
            if (secondColon > 0) {
                cfgItemId = itemPart.substring(0, secondColon);
                try {
                    cfgMeta = Integer.parseInt(itemPart.substring(secondColon + 1));
                } catch (NumberFormatException e) {
                    continue;
                }
            } else {
                cfgItemId = itemPart;
            }

            if (cfgItemId.equals(itemId) && (cfgMeta == -1 || cfgMeta == stackMeta)) {
                return value;
            }
        }
        return -1;
    }

    /**
     * Maps a thrown item to an EBWiz {@link Element} ordinal
     * using the {@code EBWiz_Discovery_Element_Items} config.
     * Returns {@code -1} if the item does not represent any element.
     */
    public static int getElementOrdinalForItem(ItemStack stack) {
        return lookupItemInConfig(stack, ArsMagica.config.getEBWizDiscoveryElementItems());
    }

    /**
     * Maps an EBWiz Element ordinal (1–7) to the corresponding AM2 {@link Discipline}.
     * Returns {@code null} for ordinal 0 (MAGIC) or any unrecognised value.
     */
    public static Discipline getDisciplineForElementOrdinal(int elementOrdinal) {
        switch (elementOrdinal) {
            case 1:
                return Discipline.FIRE;
            case 2:
                return Discipline.ICE;
            case 3:
                return Discipline.LIGHTNING;
            case 4:
                return Discipline.NECROMANCY;
            case 5:
                return Discipline.EARTH;
            case 6:
                return Discipline.SORCERY;
            case 7:
                return Discipline.HEALING;
            default:
                return null;
        }
    }

    /**
     * Returns the discipline-based cost multiplier for an EBWiz spell cast by {@code caster}.
     * E.g. at Fire discipline level 20 with 0.5% per level, Fire spells cost {@code 1 - 20*0.005 = 0.90} (10% discount).
     * Returns {@code 1.0f} if the caster is not a player or has no discipline level.
     */
    public static float getDisciplineCostMultiplier(EntityLivingBase caster, Spell spell) {
        if (!(caster instanceof EntityPlayer)) return 1.0f;
        Discipline d = getDisciplineForElementOrdinal(spell.getElement().ordinal());
        if (d == null) return 1.0f;
        ISkillData skillData = SkillData.For(caster);
        if (skillData == null) return 1.0f;
        int level = skillData.getDisciplineLevel(d);
        float reductionPerLevel = ArsMagica.config.getEBWizDisciplineCostReductionPerLevel() / 100f;
        return Math.max(0f, 1.0f - level * reductionPerLevel);
    }

    /**
     * Returns the discipline-based potency multiplier for an EBWiz spell element.
     * E.g. at Fire discipline level 20 with 0.3% per level, Fire spells deal {@code 1 + 20*0.003 = 1.06} (6% bonus).
     * Returns {@code 1.0f} if the caster is not a player or has no discipline level.
     */
    public static float getDisciplinePotencyMultiplier(EntityLivingBase caster, int elementOrdinal) {
        if (!(caster instanceof EntityPlayer)) return 1.0f;
        Discipline d = getDisciplineForElementOrdinal(elementOrdinal);
        if (d == null) return 1.0f;
        ISkillData skillData = SkillData.For(caster);
        if (skillData == null) return 1.0f;
        int level = skillData.getDisciplineLevel(d);
        float bonusPerLevel = ArsMagica.config.getEBWizDisciplinePotencyBonusPerLevel() / 100f;
        return 1.0f + level * bonusPerLevel;
    }

    /**
     * Maps a thrown tier-catalyst item to an EBWiz {@link Tier} ordinal
     * using the {@code EBWiz_Discovery_Tier_Catalysts} config.
     * Returns {@code -1} if the item is not a valid tier catalyst.
     */
    public static int getTierOrdinalForItem(ItemStack stack) {
        return lookupItemInConfig(stack, ArsMagica.config.getEBWizDiscoveryTierCatalysts());
    }

    /**
     * Returns the AM2 essence {@link ItemStack} corresponding to an EBWiz element ordinal,
     * used as a required ingredient in Advanced and Master discovery recipes.
     */
    public static ItemStack getAM2EssenceForElement(int elementOrdinal) {
        switch (elementOrdinal) {
            case 1:
                return new ItemStack(AMItems.essence_fire);
            case 2:
                return new ItemStack(AMItems.essence_ice);
            case 3:
                return new ItemStack(AMItems.essence_lightning);
            case 4:
                return new ItemStack(AMItems.essence_ender);
            case 5:
                return new ItemStack(AMItems.essence_earth);
            case 6:
                return new ItemStack(AMItems.essence_arcane);
            case 7:
                return new ItemStack(AMItems.essence_life);
            default:
                return ItemStack.EMPTY;
        }
    }

    /**
     * Builds the ordered ingredient list for the discovery ritual after the element
     * and tier catalyst have already been consumed.
     *
     * @param elementOrdinal EBWiz Element ordinal (1–7)
     * @param tierOrdinal    EBWiz Tier ordinal (0–3)
     * @return array of ItemStacks the player must throw in order; last is always spell_parchment
     */
    public static ItemStack[] getDiscoveryIngredients(int elementOrdinal, int tierOrdinal) {
        List<ItemStack> list = new ArrayList<>();
        int crystalMeta = elementOrdinal; // element ordinal == magic_crystal metadata

        list.add(new ItemStack(AMItems.vinteum_dust));

        if (tierOrdinal >= 1) { // APPRENTICE+
            list.add(new ItemStack(WizardryItems.magic_crystal, 1, crystalMeta));
        }
        if (tierOrdinal >= 2) { // ADVANCED+
            for (int i = 0; i < 4; i++)
                list.add(new ItemStack(WizardryItems.spectral_dust, 1, crystalMeta));
            list.add(getAM2EssenceForElement(elementOrdinal));
            list.add(new ItemStack(AMItems.arcane_ash));
        }
        if (tierOrdinal >= 3) { // MASTER
            list.add(new ItemStack(WizardryItems.astral_diamond));
        }

        list.add(new ItemStack(AMItems.spell_parchment));
        return list.toArray(new ItemStack[0]);
    }

    /**
     * Returns the neutral etherium cost for the discovery ritual at the given tier,
     * read from the {@code EBWiz_Discovery_Power_Costs} config.
     */
    public static int getDiscoveryPowerCost(int tierOrdinal) {
        int[] costs = ArsMagica.config.getEBWizDiscoveryPowerCosts();
        if (tierOrdinal >= 0 && tierOrdinal < costs.length) return costs[tierOrdinal];
        return costs.length > 0 ? costs[0] : 1000;
    }

    /**
     * Selects a random undiscovered EBWiz spell of the given element and tier for the player.
     * Returns an {@code ebwizardry:spell_book} ItemStack, or {@link ItemStack#EMPTY} if
     * all spells of that element+tier have already been discovered.
     *
     * @param player         the discovering player (for WizardData lookup)
     * @param elementOrdinal EBWiz Element ordinal (1–7)
     * @param tierOrdinal    EBWiz Tier ordinal (0–3)
     */
    public static ItemStack selectRandomUndiscoveredSpell(EntityPlayer player, int elementOrdinal, int tierOrdinal) {
        Element element = Element.values()[elementOrdinal];
        Tier tier = Tier.values()[tierOrdinal];
        WizardData data = WizardData.get(player);

        List<Spell> candidates = new ArrayList<>();
        for (Spell spell : Spell.getAllSpells()) {
            if (spell.getTier() != tier) continue;
            if (spell.getElement() != element) continue;
            if (!spell.isEnabled()) continue;
            if (data != null && data.hasSpellBeenDiscovered(spell)) continue;
            candidates.add(spell);
        }

        if (candidates.isEmpty()) return ItemStack.EMPTY;

        Spell chosen = candidates.get(player.world.rand.nextInt(candidates.size()));
        return new ItemStack(WizardryItems.spell_book, 1, chosen.metadata());
    }

    /**
     * Marks the spell in the given spell book stack as discovered in the player's WizardData.
     */
    public static void markSpellDiscovered(EntityPlayer player, ItemStack spellBookStack) {
        Spell spell = Spell.byMetadata(spellBookStack.getMetadata());
        if (spell == null || spell == Spells.none) return;
        WizardData data = WizardData.get(player);
        if (data != null) data.discoverSpell(spell);
    }

    /**
     * Returns an ItemStack for the {@code magic_crystal} item (metadata 0 = generic).
     */
    public static ItemStack getMagicCrystalStack() {
        return new ItemStack(WizardryItems.magic_crystal, 1, 0);
    }

    /**
     * Returns a display-friendly element name for the given ordinal.
     */
    public static String getElementDisplayName(int elementOrdinal) {
        if (elementOrdinal < 0 || elementOrdinal >= Element.values().length)
            return "Unknown";
        return Element.values()[elementOrdinal].getDisplayName();
    }

    /**
     * Returns a display-friendly tier name for the given ordinal.
     */
    public static String getTierDisplayName(int tierOrdinal) {
        if (tierOrdinal < 0 || tierOrdinal >= Tier.values().length) return "Unknown";
        return Tier.values()[tierOrdinal].getDisplayName();
    }
}
