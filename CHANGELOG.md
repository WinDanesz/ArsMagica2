# Changelog

## 1.6.2

### Features

Add /am respec command returning spent occulus points and unlearning skills
feat: Added Druids with trading. Druids spawn in the world with dryads and defend them
feat: Add new affinity abilities
feat: Added Air Elemental and Nature Elemental
feat: Added Healing Touch affinity ability for Life
feat: Added a bunch of new Attribute Modifiers!
  - am2.maxSummons: Bonus to max summons (applied additively)
  - am2.spellDamageMultiplier: Multiplier for spell damage (base 1.0, applied multiplicatively)
  - am2.manaCostMultiplier: Multiplier for mana costs (base 1.0, applied multiplicatively)
  - am2.burnoutGenerationMultiplier: Multiplier for burnout generation (base 1.0, applied multiplicatively)
  - am2.affinityGainModifier: Multiplier for affinity gain (base 1.0, minimum 0.0 to halt gain, applied multiplicatively) See the README.md for more details
feat: Added compendium entry for the Elemental Disciplines Occulus tab (Wizard Mastery, only active with EbWizardry)
feat: Added extended tooltips for affinities in the Occulus
feat: Added orbs-style mana and burnout UI display elements, can be changed from bar type
feat: Adjusted spell beam position to align better with the hands
feat: Buffed Expanded Lungs water affinity effect. Now scales with affinity depth, up to x3 of the normal breath timer
feat: Clear Caster (Arcane) affinity now grants Clarity. While Clarity is active, the next spell consumes Clarity, and costs no mana to cast.
feat: Compendium discovery changes
feat: Fix Wall Climb and add Rimeguard abilities. Tweaked Short Circuit, nerfed Lightning Reflexes, nerfed Cold Blooded
feat: Ice affinity's Cold Blooded disadvantage is also negated by walking on snow (in addition to ice)
feat: If EBWiz compatibility is active, Cone uses its ice particles
feat: Improve affinity UI with details and compendium now supports scrolling and right-clicking for navigation
feat: Items now rotate slower in the Compendium previews
feat: Mana/Burnout UI orb improvements
feat: More compendium entries
feat: New Crystal Marker model (texture by Foreck)
feat: New name for Mana Drain block -> Draining Well. Fixed item texture of Draining Well, and added new texture for the block.
fix: AM Spell books no longer directly accept and convert EbWizardry spells into AM spell items. Craft those books into spell items using the Inscription Table -> Crafting Altar
fix: Changed Fire affinity's water weakness: now it deals damage like it did in 1.7.10 instead of reducing max hp
fix: Changed Water affinity's fire weakness: now it deals damage like it did in 1.7.10 instead of reducing max hp
fix: Corrected more affinity buff/debuff quirks, updated compendium docs to be more concrete.
fix: Fix shape count enforcement on Inscription Table
fix: Fixed Alchemical Infusion requiring Splash Uncraftable Potion (now needs Splash Potion of Weakness)
fix: Fixed Fire Punch
fix: Fixed Lectern cycling through all dye colors in the preview item instead of the required color during spell crafting
fix: Fixed Lesser Mana Potion and Mage Hood recipes
fix: Fixed Thunder Punch. Added 15s cooldown for Thunder Punch
fix: Fixed Winter Guardian and Lighting Guardian summoning ritual
fix: Fixed essence refiner recipe of water essence
fix: Fixed mob previews in Arcane Compendium
fix: Fixed spell cast showing the wrong hand skins and improved hand pose
fix: Fixed water and sun weakness affinity modifiers
fix: GUIs now shows tooltips (Spell books, keystone, rift storage, rune bag, essence bag, crystal marker)
fix: Inscription Table now only accepts max 1 EbWizardry spell book (removing the risk of deleting a stack of it) when crafting
fix: Occulus now displays the affinity of components
fix: Prevent drawing empty or null text in world rendering
fix: Prevent null player or world references during client tick. Fixes #36
fix: Protect guardian loot from fire
fix: Render crystal markers with blockstate models