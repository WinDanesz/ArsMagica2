package am2.common.compat.electroblob;

import am2.ArsMagica;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.compat.electroblob.item.ItemSpellBookEBWiz;
import am2.common.items.ItemSpellBook;
import am2.common.registry.CompendiumRegistry;
import am2.common.skill.Discipline;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Bootstrap class for Electroblob's Wizardry (ebwizardry) compatibility.
 * <p>
 * Features provided when both mods are loaded:
 * <ul>
 *   <li>AM2 mana is used to power EBWiz spells – the wand's own mana charge is
 *       not consumed when the caster has sufficient AM2 mana.</li>
 *   <li>EBWiz {@code SpellCastEvent} and AM2 {@code SpellCastEvent} are both fired
 *       on the Forge event bus, so any mod can listen to spell-cast events from
 *       either system without additional wiring.</li>
 * </ul>
 * Call {@link #register()} during post-initialisation (after all mods have
 * registered their capabilities and events).
 */
public final class EBWizardryCompatBootstrap {

    /** Mod ID of Electroblob's Wizardry. */
    public static final String MODID = "ebwizardry";

    private EBWizardryCompatBootstrap() {}

    public static boolean isActive = false;
    /**
     * Registers the EBWiz compatibility handler if Electroblob's Wizardry is
     * present. Safe to call unconditionally – the check is performed here.
     */
    public static void register() {
        if (!am2.ArsMagica.config.getEbwizCompatEnabled()) return;
        if (Loader.isModLoaded(MODID)) {
            isActive = true;
            MinecraftForge.EVENT_BUS.register(new EBWizardryCompatHandler());
            registerCompendiumEntry();
            am2.common.entity.ai.selectors.SummonEntitySelector.ebwizValidator =
                    electroblob.wizardry.util.AllyDesignationSystem::isValidTarget;

            // Tell Wizardry to treat AM2 summons as allies of their owner so that
            // Wizardry minions won't attack them and healing spells will affect them.
            electroblob.wizardry.util.AllyDesignationSystem.registerValidTargetPredicate((attacker, target) -> {
                if (!(target instanceof EntityLivingBase)) return false;
                if (!am2.common.utils.EntityUtils.isSummon((EntityLivingBase) target)) return false;
                int ownerEntityId = am2.common.utils.EntityUtils.getOwner((EntityLivingBase) target);
                if (ownerEntityId == -1) return false;
                net.minecraft.entity.Entity ownerEntity = target.world.getEntityByID(ownerEntityId);
                if (ownerEntity == null) return false;
                // Direct owner — never attack
                if (attacker == ownerEntity) return true;
                // Another AM2 summon of the same owner — don't attack each other
                if (attacker instanceof EntityLivingBase
                        && am2.common.utils.EntityUtils.isSummon((EntityLivingBase) attacker)
                        && am2.common.utils.EntityUtils.getOwner((EntityLivingBase) attacker) == ownerEntityId) return true;
                // Cross-player: attacker is a Wizardry ally of the summon's owner
                if (attacker instanceof net.minecraft.entity.player.EntityPlayer
                        && ownerEntity instanceof net.minecraft.entity.player.EntityPlayer)
                    return electroblob.wizardry.util.AllyDesignationSystem.isPlayerAlly(
                            (net.minecraft.entity.player.EntityPlayer) attacker,
                            (net.minecraft.entity.player.EntityPlayer) ownerEntity);
                return false;
            });

            electroblob.wizardry.util.AllyDesignationSystem.registerAllyPredicate((allyOf, possibleAlly) -> {
                if (!am2.common.utils.EntityUtils.isSummon(possibleAlly)) return false;
                int ownerEntityId = am2.common.utils.EntityUtils.getOwner(possibleAlly);
                if (ownerEntityId == -1) return false;
                net.minecraft.entity.Entity ownerEntity = possibleAlly.world.getEntityByID(ownerEntityId);
                if (ownerEntity == null) return false;
                // Direct owner can always heal/buff their own summons
                if (allyOf == ownerEntity) return true;
                // Cross-player: allyOf is a Wizardry ally of the summon's owner
                if (allyOf instanceof net.minecraft.entity.player.EntityPlayer
                        && ownerEntity instanceof net.minecraft.entity.player.EntityPlayer)
                    return electroblob.wizardry.util.AllyDesignationSystem.isPlayerAlly(
                            (net.minecraft.entity.player.EntityPlayer) allyOf,
                            (net.minecraft.entity.player.EntityPlayer) ownerEntity);
                return false;
            });

        }
    }

    private static void registerCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "wizard_spells");
        entry.setCategory(CompendiumCategory.MECHANIC);
        entry.setName("Wizard Spells");
        entry.addObject(
            "Magic has many forms. Many paths leading to the same horizon, and not all who walk them look alike. !d"
            + "Mages weave spells from raw components, shaping power into something new with every creation. "
            + "Shape, effect, modifier: the building blocks of a living art, fluid and endlessly adaptable. !d"
            + "Wizards walk a different road. They do not compose their own creation, but rather, through discipline and study, they rely on spells that have proven their worth through time. "
            + "A wizard pours their craft into a spell book, binding a single, finished incantation to it permanently. "
            + "Precise, stable, and uncompromising - each book holds one immovable form of power. !d"
            + "Yet the two traditions are not sealed from one another. "
            + "When you wield a wizard's wand, your own mana will will fuel its charge, "
            + "should your reserves be sufficient. Their spells draw from your power as naturally as your own. !d"
            + "And with patience, a wizard's fixed spell can be translated into terms a mage understands. "
            + "To achieve this, bring the spell book to the Inscription Table and copy its essence into a Spell Recipe Book."
            + "Place the Spell Recipe Book on your Crafting Altar's lectern and craft the spell with the required ingredients.  "
            + "Such spells always require a large amount of neutral etherium from a bound Obelisk."
        );
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);

        registerIceStatueCompendiumEntry();
        registerCobwebSpellCompendiumEntry();
        registerConjureBlockCompendiumEntry();
        registerArtefactSpellPowerEntry();
        registerDiscoveryRitualCompendiumEntry();
        registerWizardMasteryCompendiumEntry();
    }

    private static void registerWizardMasteryCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "wizard_mastery");
        entry.setCategory(CompendiumCategory.MECHANIC);
        entry.setName("Wizard Mastery");
        entry.addObject(
            "The Occulus holds a second tab alongside the usual shapes, components, and modifiers - one that has nothing "
            + "to do with building spells at all, and everything to do with a wizard's discipline. !d"
            + "The #4Elemental Disciplines#0 tab appears on the Occulus only while Electroblob's Wizardry is installed. "
            + "It shows seven disciplines, one per element - Fire, Ice, Lightning, Earth, Sorcery, Healing, and Necromancy."
            + "!d #4Levelling a Discipline#0"
            + "!l Select a discipline to focus on it, then spend a skill point to raise its level, "
            + "up to a maximum of 100. The very same #2Blue#0, #2Green#0, and #2Red#0 skill points "
            + "used to unlock shapes, components, and modifiers are spent here: "
            + "levels 0-29 cost Blue points, 30-59 cost Green points, and 60-100 cost Red points, "
            + "so a well-rounded mage will find their late Occulus progress and their Wizard Mastery in competition for the same points."
            + "!d #4What Mastery Grants#0"
            + "!l Every level of a discipline quietly strengthens EBWizardry spells of the matching element when you cast them - "
            + "trimming their mana cost and deepening their potency, with both bonuses scaling up as the level rises."
            + "!d Each level also banks one #5Discovery Point#0 for that element, spent at the Crafting Altar's Spell Discovery Ritual "
            + "to conjure a new spell book of that element and tier. A discipline with no available discovery points cannot fuel the ritual "
            + "until it is levelled further."
        );
        entry.setRelatedEntries("occulus,wizard_spells,ebwiz_discovery_ritual");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    private static void registerArtefactSpellPowerEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "wizard_artefacts");
        entry.setCategory(CompendiumCategory.MECHANIC);
        entry.setName("Wizard Artefacts");
        entry.addObject(
            "A wizard's artefacts are attuned to the act of spellwork itself. "
            + "Rings and charms that sharpen a wizard's incantations will not remain indifferent to a mage's art either - "
            + "when you cast your own spells while wearing them, some of their amplifying force carries over. !d"
        );
        entry.setRelatedEntries("wizard_spells");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    private static void registerDiscoveryRitualCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "ebwiz_discovery_ritual");
        entry.setCategory(CompendiumCategory.MECHANIC);
        entry.setName("Spell Discovery Ritual");
        entry.addObject(
            "Wizards have long catalogued their spells - proven incantations, refined over generations and bound into books by disciplined hands. "
            + "A mage, however, does not study from those same tomes. To bridge that gap, the #5Crafting Altar#0 can be coaxed into channelling "
            + "a wizard's spell through elemental resonance, inscribing it fresh into a book for a caster who has yet to learn it. "
            + "How an altar built for mage-craft reproduces wizardly knowledge remains a mystery, but the results are undeniable."
            + "!d #4Performing the Ritual#0"
            + "!l Begin by placing a #9Book and Quill#0 (writable book) upon the altar's #9Lectern#0 - "
            + "its blank pages will serve as the vessel for the spell to come. "
            + "The lectern will display a hint showing a #9Blank Rune#0. Throw one into the altar to awaken the ritual."
            + "!d #4Step 1 - Declare an Element#0"
            + "!l Every spell is born from an element. Cast a token of your chosen affinity into the altar's heart:"
            + "!l  - Charcoal = Fire"
            + "!l  - Snowball = Ice"
            + "!l  - Redstone = Lightning"
            + "!l  - Bone = Necromancy"
            + "!l  - Granite = Earth"
            + "!l  - #5Cerublossom#0 = Sorcery"
            + "!l  - #5Aum#0 = Healing"
            + "!d #4Step 2 - Set the Tier#0"
            + "!l The depth of the spell is governed by the catalyst you offer. Greater foci call forth more formidable magic:"
            + "!l  - Magic Crystal = Novice"
            + "!l  - #5Lesser Focus#0 = Apprentice"
            + "!l  - #5Standard Focus#0 = Advanced"
            + "!l  - #5Greater Focus#0 = Master"
            + "!d #4Step 3 - Feed the Altar#0"
            + "!l The lectern will reveal each required ingredient in turn - throw them into the altar one by one as it demands them. "
            + "More powerful spells hunger for rarer materials:"
            + "!l  - #2Novice:#0 Vinteum Dust, Spell Parchment"
            + "!l  - #9Apprentice:#0 Vinteum Dust, Magic Crystal (matching element), Spell Parchment"
            + "!l  - #4Advanced:#0 Vinteum Dust, Magic Crystal (matching element), Spectral Dust (matching element) x4, Elemental Essence, Arcane Ash, Spell Parchment"
            + "!l  - #5Master:#0 Vinteum Dust, Magic Crystal (matching element), Astral Diamond, Spectral Dust (matching element) x4, Arcane Ash, Elemental Essence, Spell Parchment"
            + "!d #4Step 4 - Channel Etherium#0"
            + "!l With the ingredients consumed, the altar reaches outward, drawing #9Neutral Etherium#0 from the power network to fuel the final transmutation. "
            + "An #5Obelisk#0 must be connected and well-supplied. "
            + "The etherium cost scales with ambition: 1000 (Novice), 1500 (Apprentice), 2000 (Advanced), 3000 (Master)."
            + "!d #4Result#0"
            + "!l When the last mote of etherium is drawn in, the book is consumed "
            + "and a Wizardry #9Spell Book#0 bearing a random undiscovered spell of the chosen element and tier materialises at the altar. "
            + "The spell's content is discovered by the caster upon reading the book."
            + "!d Should you have already mastered every spell of the chosen element and tier, the ritual will run its course "
            + "but yield nothing - the writable book remains on the lectern, and the ingredients and etherium are still consumed."
            + "!d This feature can be disabled in the AM2 config under #2EBWiz_Discovery_Enabled#0."
        );
        entry.setRelatedEntries("crafting_altar,obelisk");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    private static void registerConjureBlockCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "conjure_block");
        entry.setCategory(am2.api.compendium.CompendiumCategory.SPELL_COMPONENT);
        entry.setName("Conjure Block");
        entry.addObject(
            "I raised my hand and something solid answered. Not quarried, not carried – simply called forth. "
            + "A block of pure arcane matter hung in the air where nothing had been, catching the light strangely, "
            + "as if it had not quite decided to be real yet. !d"
            + "Conjure Block summons EBWizardry's spectral block at the target location or at an entity's feet. "
            + "The block is solid and can be stood upon, but will dissolve on its own after a duration "
            + "controlled by the Duration modifier. !d"
        );
        entry.setRelatedEntries("wizard_spells");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    private static void registerCobwebSpellCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "cobweb_spell");
        entry.setCategory(am2.api.compendium.CompendiumCategory.SPELL_COMPONENT);
        entry.setName("Cobweb");
        entry.addObject(
            "It seemed effortless, the way it spread. A single gesture, and the air between us filled with sticky strands, "
            + "each thread catching the light like something spun from moonlight and malice. "
            + "I had taken one step before I realised I could not take another. !d"
            + "Cobweb conjures EBWizardry's vanishing cobweb at the target location or at an entity's feet, "
            + "slowing anything that passes through it. "
            + "The cobweb dissolves on its own after a duration controlled by the Duration modifier. !d"
        );
        entry.setRelatedEntries("wizard_spells");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    private static void registerIceStatueCompendiumEntry() {
        CompendiumEntry entry = new CompendiumEntry(null, "ice_statue");
        entry.setCategory(am2.api.compendium.CompendiumCategory.SPELL_COMPONENT);
        entry.setName("Ice Statue");
        entry.addObject(
            "They stood perfectly still, their face caught in an expression of pure terror, their skin turned to glimmering ice. "
            + "The mage had not slain them - only preserved the moment, sharp and cold as winter glass. !d"
            + "Ice Statue channels the chill of the frozen wastes into a beam that can lock a living creature within a shell of ice. "
            + "The target, if it is a creature and not a player, is encased in an ice statue for a duration modified by the Duration modifier. !d"
        );
        entry.setRelatedEntries("wizard_spells");
        entry.setUnlocked();
        CompendiumRegistry.registerEntry(entry);
    }

    /**
     * Registers all EBWiz-exclusive spell parts (modifiers and components).
     * Safe to call unconditionally – does nothing when EBWiz is absent.
     */
    public static void registerSpellParts(net.minecraftforge.registries.IForgeRegistry<am2.api.spell.SpellPart> registry) {
        if (!Loader.isModLoaded(MODID)) return;
        EBWizardryCompatHandler.registerSpellParts(registry);
    }

    /**
     * Returns {@code true} if {@code stack} is an EBWiz {@code ItemSpellBook}
     * and EBWiz is loaded. Safe to call unconditionally.
     */
    public static boolean isEBWizSpellBook(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return false;
        return EBWizardryCompatHandler.isEBWizSpellBookItem(stack);
    }

    /**
     * Converts an EBWiz {@code ItemSpellBook} stack into an
     * {@link ItemEBWizSpellBinding} that the AM2 crafting altar can output.
     * Returns {@code stack} unchanged if it is not an EBWiz spell book or
     * EBWiz is not loaded. Safe to call unconditionally.
     */
    public static ItemStack convertEBWizSpellBook(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return stack;
        return EBWizardryCompatHandler.convertEBWizSpellBook(stack);
    }

    /**
     * Returns the AM2 mana cost for the spell currently selected in an EBWiz
     * {@code ItemWand} stack, applying the configured multiplier. Returns
     * {@code -1} if the stack is not an EBWiz wand, has no current spell, or
     * EBWiz is not loaded. Safe to call unconditionally.
     */
    public static float getEBWizWandCurrentSpellManaCost(ItemStack stack) {
        return getEBWizWandCurrentSpellManaCost(stack, null);
    }

    /**
     * Returns the AM2 mana cost for the EBWiz wand's currently-selected spell,
     * applying the discipline cost discount when {@code caster} is non-null.
     */
    public static float getEBWizWandCurrentSpellManaCost(ItemStack stack, @javax.annotation.Nullable EntityLivingBase caster) {
        if (!Loader.isModLoaded(MODID)) return -1f;
        if (stack.isEmpty()) return -1f;
        if (!(stack.getItem() instanceof electroblob.wizardry.item.ItemWand)) return -1f;
        electroblob.wizardry.item.ItemWand wand = (electroblob.wizardry.item.ItemWand) stack.getItem();
        electroblob.wizardry.spell.Spell spell = wand.getCurrentSpell(stack);
        if (spell == null || spell == electroblob.wizardry.registry.Spells.none) return -1f;
        float cost = spell.getCost() * ArsMagica.config.getEBWizManaCostMultiplier();
        if (caster != null) {
            // We use a fake event container to run ItemWizardArmour's internal maths 
            // without actually firing it on the Forge Event Bus. This ensures no side 
            // effects are triggered by other mods during HUD rendering.
            electroblob.wizardry.util.SpellModifiers fakeModifiers = new electroblob.wizardry.util.SpellModifiers();
            fakeModifiers.set(electroblob.wizardry.util.SpellModifiers.COST, 1.0f, false);
            electroblob.wizardry.event.SpellCastEvent.Pre fakeEvent =
                    new electroblob.wizardry.event.SpellCastEvent.Pre(
                            electroblob.wizardry.event.SpellCastEvent.Source.WAND, spell, caster, fakeModifiers);
            electroblob.wizardry.item.ItemWizardArmour.onSpellCastPreEvent(fakeEvent);
            float armourDiscount = fakeModifiers.get(electroblob.wizardry.util.SpellModifiers.COST);

            cost *= EBWizardryCompatHandler.getDisciplineCostMultiplier(caster, spell) * armourDiscount;
        }
        return cost;
    }

    /**
     * Returns {@code true} if {@code stack} is an EBWiz {@code ItemWand}.
     * Safe to call unconditionally.
     */
    public static boolean isEBWizWand(ItemStack stack) {
         if (!Loader.isModLoaded(MODID)) return false;
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof electroblob.wizardry.item.ItemWand;
    }

    /**
     * Returns the number of EBWiz summoned creatures currently alive in the
     * world that are owned by {@code caster}. Returns 0 when EBWiz is not
     * loaded or {@code caster} is {@code null}. Safe to call unconditionally.
     */
    public static int countEBWizSummonsFor(EntityLivingBase caster) {
        if (!Loader.isModLoaded(MODID) || caster == null) return 0;
        return EBWizardryCompatHandler.countSummonsFor(caster);
    }

    /**
     * Returns the AM2 mana cost for the EBWiz spell bound in {@code stack},
     * applying the configured multiplier. Returns {@code -1} if the stack is
     * not an EBWiz spell binding or EBWiz is not loaded.
     * Safe to call unconditionally.
     */
    public static float getEBWizSpellBindingManaCost(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return -1f;
        return EBWizardryCompatHandler.getEBWizSpellBindingManaCost(stack);
    }

    /**
     * Returns the armor-discounted AM2 mana cost for the EBWiz spell bound in
     * {@code stack} for the given {@code caster}. Wizard-armour cost reductions
     * (e.g. fire mage robes reducing fireball cost) are factored in.
     * Returns {@code -1} if EBWiz is not loaded or the stack is invalid.
     */
    public static float getEBWizSpellBindingManaCost(ItemStack stack, net.minecraft.entity.EntityLivingBase caster) {
        if (!Loader.isModLoaded(MODID)) return -1f;
        return EBWizardryCompatHandler.getEBWizSpellBindingManaCost(stack, caster);
    }

    /**
     * Returns the number of neutral essence items required to transcribe
     * {@code stack} (an EBWiz {@code ItemSpellBook}) at the AM2 crafting altar:
     * 500 for Novice, +500 for each higher tier.
     * Returns {@code 0} if the stack is not a valid EBWiz spell book or EBWiz
     * is not loaded. Safe to call unconditionally.
     */
    public static int getEBWizSpellBookEssenceCost(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return 0;
        return EBWizardryCompatHandler.getEBWizSpellBookEssenceCost(stack);
    }

    /**
     * Returns {@code true} if {@code stack} is a Written Book produced by the
     * Scribing Desk from an EBWiz spell binding (contains an {@code EBWizSpell}
     * NBT key). Safe to call unconditionally.
     */
    public static boolean isEBWizBindingBook(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() != net.minecraft.init.Items.WRITTEN_BOOK) return false;
        if (!stack.hasTagCompound()) return false;
        return stack.getTagCompound().hasKey("EBWizSpell");
    }

    /**
     * Creates an {@link ItemEBWizSpellBinding} from a Written Book previously
     * produced by the Scribing Desk in EBWiz mode. The binding will carry the
     * AM2 modifier counts from the book's {@code AM2Modifiers} NBT.
     * Returns {@link ItemStack#EMPTY} if the book is invalid or EBWiz is not loaded.
     * Safe to call unconditionally.
     */
    public static ItemStack createBindingFromBook(ItemStack book) {
        if (!Loader.isModLoaded(MODID)) return ItemStack.EMPTY;
        if (!isEBWizBindingBook(book)) return ItemStack.EMPTY;
        return EBWizardryCompatHandler.createBindingFromBook(book);
    }

    /**
     * Spawns EBWizardry frost particles (snowflakes and ice shards) at the given position when
     * EBWizardry is loaded. Falls back gracefully (returns {@code false}) if it is absent.
     * Must only be called client-side ({@code world.isRemote}).
     *
     * @param count    Number of snowflake particles; roughly one ice shard is spawned per 3 snowflakes.
     * @param spread   Half-width of the random position offset around (x, y, z). Pass 0 to spawn at exactly the given position.
     * @param velScale Multiplier on random velocity (0–1). Pass 0 for stationary particles, 1 for full drift.
     * @return {@code true} if EBWizardry particles were spawned, {@code false} if EBWiz is not loaded.
     */
    public static boolean spawnFrostParticles(net.minecraft.world.World world, double x, double y, double z, int count, java.util.Random rand, double spread, double velScale) {
        if (!Loader.isModLoaded(MODID)) return false;
        EBWizardryCompatHandler.spawnFrostParticles(world, x, y, z, count, rand, spread, velScale);
        return true;
    }

    /** Overload with explicit spread, default velocity scale of 1. */
    public static boolean spawnFrostParticles(net.minecraft.world.World world, double x, double y, double z, int count, java.util.Random rand, double spread) {
        if (!Loader.isModLoaded(MODID)) return false;
        EBWizardryCompatHandler.spawnFrostParticles(world, x, y, z, count, rand, spread);
        return true;
    }

    /** Convenience overload with default spread of 0.4 blocks and full velocity. */
    public static boolean spawnFrostParticles(net.minecraft.world.World world, double x, double y, double z, int count, java.util.Random rand) {
        return spawnFrostParticles(world, x, y, z, count, rand, 0.4, 1.0);
    }

    /**
     * Spawns one EBWiz {@code CLOUD} particle at (x, y, z) with the given velocity, scale, and lifetime.
     * Returns {@code false} and does nothing if EBWizardry is not loaded.
     * Safe to call unconditionally client-side.
     */
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public static boolean spawnMistCloudParticle(net.minecraft.world.World world, double x, double y, double z,
                                                 double vx, double vy, double vz, float scale, int lifetime) {
        if (!Loader.isModLoaded(MODID)) return false;
        EBWizardryCompatHandler.spawnMistCloudParticle(world, x, y, z, vx, vy, vz, scale, lifetime);
        return true;
    }

    /**
     * Applies the EBWizardry {@code frost} potion effect to the target if EBWizardry is loaded.
     * Returns {@code true} if the effect was applied, {@code false} if EBWiz is absent.
     * Safe to call unconditionally.
     */
    public static boolean applyFrostEffect(net.minecraft.entity.EntityLivingBase target, int durationTicks, int amplifier) {
        if (!Loader.isModLoaded(MODID)) return false;
        EBWizardryCompatHandler.applyFrostEffect(target, durationTicks, amplifier);
        return true;
    }

    /**
     * Returns {@code true} if {@code stack} is an {@link ItemEBWizSpellBinding}
     * (an AM2 wrapper around an EBWiz spell that has already been transcribed).
     * Safe to call unconditionally.
     */
    public static boolean isEBWizSpellBinding(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return false;
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof ItemEBWizSpellBinding;
    }

    /**
     * Returns the EBWiz spell registry name from an EBWiz {@code ItemSpellBook}
     * stack (e.g. {@code "ebwizardry:fire_bolt"}), or an empty string if the
     * stack is not a valid EBWiz spell book or EBWiz is not loaded.
     * Safe to call unconditionally.
     */
    public static String getEBWizSpellBookRegistryName(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return "";
        return EBWizardryCompatHandler.getEBWizSpellBookRegistryName(stack);
    }

    /**
     * Returns the localised display name of the spell in an EBWiz {@code ItemSpellBook},
     * or an empty string if the stack is not a valid EBWiz spell book or EBWiz is not loaded.
     * Safe to call unconditionally.
     */
    public static String getEBWizSpellBookDisplayName(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return "";
        return EBWizardryCompatHandler.getEBWizSpellBookDisplayName(stack);
    }

    /**
     * Returns the appropriate {@code spellbook} {@link Item} to register:
     * {@link ItemSpellBookEBWiz} when EBWiz is present (so that EBWiz's
     * {@code EntityUtils.isCasting} recognises the spell book), or the plain
     * {@link ItemSpellBook} otherwise.
     *
     * <p>Safe to call unconditionally. Using this factory keeps
     * {@code AMItems} free of any direct bytecode reference to
     * {@link ItemSpellBookEBWiz}, which would trigger a
     * {@link NoClassDefFoundError} on startup when EBWiz is absent.
     */
    public static Item createSpellBookItem() {
        if (Loader.isModLoaded(MODID)) {
            return EBWizardryCompatHandler.createSpellBookItem();
        }
        return new ItemSpellBook();
    }

    /**
     * Attempts to open an Electroblob's Wizardry lectern-readable GUI for the given
     * stack on the client. Returns {@code true} when a supported EBWiz GUI was opened.
     * Safe to call unconditionally from common code.
     */
    @SideOnly(Side.CLIENT)
    public static boolean openLecternGuiClient(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return false;
        return EBWizardryCompatHandler.openLecternGuiClient(stack);
    }

    /**
     * Registers EBWiz-only items (e.g. {@code ebwiz_spell_binding}) into the
     * item registry when Electroblob's Wizardry is present.
     *
     * <p>Safe to call unconditionally. Using this method keeps {@code AMItems}
     * free of any direct bytecode reference to {@link ItemEBWizSpellBinding},
     * which implements an EBWiz interface and would trigger a
     * {@link NoClassDefFoundError} on startup when EBWiz is absent.
     */
    public static void registerEBWizItems(net.minecraftforge.registries.IForgeRegistry<Item> registry) {
        if (Loader.isModLoaded(MODID)) {
            EBWizardryCompatHandler.registerEBWizItems(registry);
        }
    }

    /**
     * Returns the AM2 mana regen speed multiplier contributed by a Condenser
     * wand upgrade on any wand held in either hand.
     *
     * <p>The return value is a fraction to subtract from 1 before applying to
     * {@code regenTicks}: {@code regenTicks *= (1 - getCondenserRegenMultiplier(player))}.
     * A value of 0 means no bonus.
     *
     * <p>Safe to call unconditionally – returns 0 when EBWiz is absent or the
     * config option is disabled.
     */
    public static float getCondenserRegenMultiplier(net.minecraft.entity.player.EntityPlayer player) {
        if (!Loader.isModLoaded(MODID)) return 0f;
        if (!am2.ArsMagica.config.getEBWizCondenserManaRegenEnabled()) return 0f;
        return EBWizardryCompatHandler.getCondenserRegenMultiplier(player);
    }

    /**
     * Returns the AM2 mana regen speed multiplier contributed by the EBWiz
     * {@code ring_condensing} artefact (0.2 when active, 0 otherwise).
     *
     * <p>Apply as {@code regenTicks *= (1 - getRingCondensingRegenMultiplier(player))}.
     * Safe to call unconditionally – returns 0 when EBWiz is absent.
     */
    public static float getRingCondensingRegenMultiplier(net.minecraft.entity.player.EntityPlayer player) {
        if (!Loader.isModLoaded(MODID)) return 0f;
        return EBWizardryCompatHandler.getRingCondensingRegenMultiplier(player);
    }

    /**
     * Returns {@code true} if {@code caster} is currently affected by EBWiz's
     * Arcane Jammer debuff, which prevents all magic spells from being cast.
     * Safe to call unconditionally – returns {@code false} when EBWiz is absent.
     * Uses a registry lookup rather than a direct class reference so this method
     * can be called from common code without risking a {@link NoClassDefFoundError}.
     */
    public static boolean isArcaneJammed(EntityLivingBase caster) {
        if (!Loader.isModLoaded(MODID)) return false;
        Potion arcaneJammer = ForgeRegistries.POTIONS.getValue(new ResourceLocation("ebwizardry", "arcane_jammer"));
        return arcaneJammer != null && caster.isPotionActive(arcaneJammer);
    }

    /**
     * Returns EBWiz-specific crafting-altar ingredient defaults for AM2 spell parts.
     * When EBWiz is loaded, {@code spell_parts.cfg} will use these strings instead of the
     * vanilla AM2 rune-based defaults, substituting elemental {@code magic_crystal} items
     * for the AM2-specific rune in each recipe.
     *
     * <p>Keys are full registry names (e.g. {@code "arsmagica2:lightning_damage"}).
     * Values are ingredient strings in the same format as {@code spell_parts.cfg}
     * ({@code modid:item} or {@code modid:item:meta}).
     *
     * <p>Returns an empty map when EBWiz is not loaded. Safe to call unconditionally.
     */
    public static java.util.Map<String, String[]> getEBWizRecipeDefaults() {
        if (!Loader.isModLoaded(MODID)) return java.util.Collections.emptyMap();
        return EBWizardryCompatHandler.getEBWizRecipeDefaults();
    }

    /**
     * Returns the AM2 spell-damage multiplier contributed by active EBWiz
     * artefacts for the given player, scaled by the configured
     * {@code EBWiz_Artefact_Potency_Ratio}.
     *
     * <p>Only universal, non-spell-specific potency artefacts (e.g.
     * {@code ring_battlemage}) are captured here because a sentinel/none spell
     * is used for the evaluation. Element-specific biome rings will not apply.
     *
     * <p>Returns {@code 1.0f} (no bonus) when EBWiz is not loaded, the ratio is
     * zero or negative, or the player wears no potency-granting artefacts.
     * Safe to call unconditionally.
     *
     * @param player the casting player
     * @return a multiplier ≥ {@code 1.0f} to apply to AM2 spell damage
     */
    public static float getArtefactPotencyMultiplier(net.minecraft.entity.player.EntityPlayer player,
            @javax.annotation.Nullable am2.api.affinity.Affinity dominantAffinity) {
        if (!Loader.isModLoaded(MODID)) return 1.0f;
        float ratio = am2.ArsMagica.config.getEBWizArtefactPotencyRatio();
        if (ratio <= 0f) return 1.0f;
        return EBWizardryCompatHandler.computeArtefactPotencyMultiplier(player, ratio, dominantAffinity);
    }

    /**
     * Adds an AI task to the given entity that periodically casts the EBWiz
     * {@code Arc} spell at its attack target. Does nothing when EBWiz is absent.
     * Safe to call unconditionally.
     *
     * @param entity   the entity to add the AI task to
     * @param priority the AI task priority
     * @param cooldown cooldown in ticks between casts
     */
    public static void addArcAttackAI(net.minecraft.entity.EntityLiving entity, int priority, int cooldown) {
        if (!Loader.isModLoaded(MODID)) return;
        EBWizardryCompatHandler.addArcAttackAI(entity, priority, cooldown);
    }

    /**
     * Registers AM2 book model textures for the EBWiz bookshelf block.
     * Must be called during {@code preInit} – safe to call unconditionally.
     */
    public static void registerBookshelfPreInit() {
        if (!Loader.isModLoaded(MODID)) return;
        EBWizardryCompatHandler.registerBookshelfModelTextures();
    }

    /**
     * Registers AM2 book items as valid items for the EBWiz bookshelf container.
     * Must be called during {@code init} – safe to call unconditionally.
     */
    public static void registerBookshelfInit() {
        if (!Loader.isModLoaded(MODID)) return;
        EBWizardryCompatHandler.registerBookshelfItems();
    }

    // -------------------------------------------------------------------------
    // EBWiz Discovery Ritual facades
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the EBWiz discovery ritual is enabled and EBWiz is loaded.
     * Safe to call unconditionally.
     */
    public static boolean isDiscoveryRitualEnabled() {
        if (!Loader.isModLoaded(MODID)) return false;
        return ArsMagica.config.getEBWizDiscoveryEnabled();
    }

    /**
     * Maps a thrown item to an EBWiz Element ordinal (1–7).
     * Returns {@code -1} if the item does not represent any element or EBWiz is not loaded.
     */
    public static int getElementOrdinalForItem(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return -1;
        return EBWizardryCompatHandler.getElementOrdinalForItem(stack);
    }

    /**
     * Maps an EBWiz Element ordinal (1–7) to the corresponding AM2 {@link Discipline}.
     * Returns {@code null} for ordinal 0 (MAGIC) or any unrecognised value, or if EBWiz is not loaded.
     */
    public static Discipline getDisciplineForElementOrdinal(int elementOrdinal) {
        if (!Loader.isModLoaded(MODID)) return null;
        return EBWizardryCompatHandler.getDisciplineForElementOrdinal(elementOrdinal);
    }

    /**
     * Maps a thrown tier-catalyst item to an EBWiz Tier ordinal (0–3).
     * Returns {@code -1} if the item is not a valid tier catalyst or EBWiz is not loaded.
     */
    public static int getTierOrdinalForItem(ItemStack stack) {
        if (!Loader.isModLoaded(MODID)) return -1;
        return EBWizardryCompatHandler.getTierOrdinalForItem(stack);
    }

    /**
     * Builds the ordered ingredient list for the discovery ritual.
     * Returns an empty array if EBWiz is not loaded.
     */
    public static ItemStack[] getDiscoveryIngredients(int elementOrdinal, int tierOrdinal) {
        if (!Loader.isModLoaded(MODID)) return new ItemStack[0];
        return EBWizardryCompatHandler.getDiscoveryIngredients(elementOrdinal, tierOrdinal);
    }

    /**
     * Returns the neutral etherium cost for the discovery ritual at the given tier.
     * Returns {@code 0} if EBWiz is not loaded.
     */
    public static int getDiscoveryPowerCost(int tierOrdinal) {
        if (!Loader.isModLoaded(MODID)) return 0;
        return EBWizardryCompatHandler.getDiscoveryPowerCost(tierOrdinal);
    }

    /**
     * Selects a random undiscovered EBWiz spell of the given element and tier.
     * Returns {@link ItemStack#EMPTY} if the pool is exhausted or EBWiz is not loaded.
     */
    public static ItemStack selectRandomUndiscoveredSpell(net.minecraft.entity.player.EntityPlayer player, int elementOrdinal, int tierOrdinal) {
        if (!Loader.isModLoaded(MODID)) return ItemStack.EMPTY;
        return EBWizardryCompatHandler.selectRandomUndiscoveredSpell(player, elementOrdinal, tierOrdinal);
    }

    /**
     * Marks the spell in the given spell book stack as discovered in the player's data.
     * No-op if EBWiz is not loaded.
     */
    public static void markSpellDiscovered(net.minecraft.entity.player.EntityPlayer player, ItemStack spellBookStack) {
        if (!Loader.isModLoaded(MODID)) return;
        EBWizardryCompatHandler.markSpellDiscovered(player, spellBookStack);
    }

    /**
     * Returns an ItemStack for the EBWiz {@code magic_crystal} item.
     * Returns {@link ItemStack#EMPTY} if EBWiz is not loaded.
     */
    public static ItemStack getMagicCrystalStack() {
        if (!Loader.isModLoaded(MODID)) return ItemStack.EMPTY;
        return EBWizardryCompatHandler.getMagicCrystalStack();
    }

    /**
     * Returns a display-friendly element name for the given ordinal.
     * Returns "Unknown" if EBWiz is not loaded.
     */
    public static String getElementDisplayName(int elementOrdinal) {
        if (!Loader.isModLoaded(MODID)) return "Unknown";
        return EBWizardryCompatHandler.getElementDisplayName(elementOrdinal);
    }

    /**
     * Returns a display-friendly tier name for the given ordinal.
     * Returns "Unknown" if EBWiz is not loaded.
     */
    public static String getTierDisplayName(int tierOrdinal) {
        if (!Loader.isModLoaded(MODID)) return "Unknown";
        return EBWizardryCompatHandler.getTierDisplayName(tierOrdinal);
    }
}
