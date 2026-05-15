# Changelog

## [1.6.1] - Unreleased

### Features

- Add `/am unlockcompendium` command to manage compendium entries
- Implement illusion block rendering optimization — large amounts of illusion blocks no longer cause performance impact
- Implement AM2 summon compatibility with Electroblob's Wizardry, allowing summons to be treated as allies
- Summons no longer retaliate against their owner
- Summons no longer target Electroblob's Wizardry minions
- Enhance skill system with levels and max level support; More Summons is now a levelable skill (up to 12, configurable)
- Added Rune of Debugging — prints NBT and capability data of the held offhand item
- Performance improvements for `EntityExtension` and `EntityHandler`
- Performance improvements for `ArmorHelper`
- Improve performance of AM potion effects
- Reduce Flicker spawn amount; expose more spawn rate config options
- Add `ItemBlockManaBattery` with mana battery tooltip
- Converted most stereo sounds to mono for better positional audio (by TechnoMysterio)
- Added WIP Hungarian translation

### Fixes

- Fix illusion block transparency
- Fix spell casting event handling with pre and post events; fix silver spell unlocking
- Fix summons attacking all creatures
- Fix Chain spell not relaying damage
- Fix Flicker focus texture paths
- Fix Crafting Altar recipes with etherium
- Fix inscription tables being consumed when there is not enough room for item placement
- Fix lecterns creating ghost items
- Fix Chrono Anchor not preventing death
- Fix UI colors becoming incorrect when looking at an Obelisk (#10)
- Fixed AM armour rating values (#9)
- Fixed crash on dedicated servers (#6)
- Fix crash with Water Guardian
- Fix crash with missing Electroblob's Wizardry soft-dependency
- Fix optional dependency loading
- Fix colored rune recipes using wrong dyes
- Fix held spell hand and spell positioning
- Add missing Air Sled lang key
- Add spell component name to localization and hide it from JEI
- Remove Essence Conduit particle spam
- Prevent allied summons from targeting each other or retaliating against their owner/allies
- Handle null case for `filterItems` in `TileEntityCrystalMarker`
- Rename internal `SpawnClassName` to `SummonType` for consistency
- Refactor summon type handling to use `ResourceLocation` for entity registration
