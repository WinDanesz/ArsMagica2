# Ars Magica 2 - For Minecraft 1.12.2

This is a port of Mithion's Ars Magica 2 mod for Minecraft version 1.12.2 by WinDanesz. 

It was built on top of the 1.10.2 version of EdwinMindcraft and the original 1.7.10 version of the mod.

## Changes in 1.12.2, compared to 1.7.10

The goal of the port was to remain faithful to the original mechanics and atmosphere of the mod with porting all existing features, but at the same time, modernize assets and visuals to be on par with the most popular and high quality 1.12.2 mods. My personal goal was also to implement integration with Electroblob's Wizardry and resurrect some old AM features.

Many small quality of life changes were added to the new version. The list below is not comprehensive, as I surely forgot to mention a lot them.

Most notable changes include:

- Reworked models and textures. Many of the mod's assets have been updated to be more consistent with vanilla's x16 pixel art. This was done by Foreck (Occulus, Calefactor, Moonstone, etc.) and WinDanesz (Inscription Table, other items and blocks).
- Comprehensive configs. To allow players and pack makers to tweak AM, a large amount of new configs were introduced.
- **Integration with Electroblob's Wizardry**. This was always a big desire for me, to play these two magic mods together as each of them has its own strength. I was aiming to create a seamless blend of the two. In AM 1.12.2 there is now a comprehensive integration to allow playing these mods under a unified mana, casting, levelling, and affinity system. This integration is optional, enabled by default when both mods are present.
  Some Changes:
    - Option to use AM's mana to cast Wizardry spells, disables Wizardry's wand mana
    - Wizardry spells castable from AM spell books or AM spell items (wands still function)
    - Wizardry spells are mapped to AM's affinities. This was a bit challenging as we have 9 affinities and 7 elements, with only a few matching 1:1. I think I created a sensible default mapping, but it can also be freely tweaked in the configs on a per-element and even per-spell level.
    - Condensing Wand Upgrade increases player mana regen rate when the wand is held
    - Wizardry's potency bonus increases AM spell power
    - Artefacts of Wizardry are empowering AM spells (potency and such)
    - Lore: Wizards (EBWizardry) and Mages (AM), different paths of magic than can converge. Wizards rely on study and discipline to use well-known spells that have withstood the test of ages. Mages use their art of craft to create truly unique and unpredictable magical creations. With the AM+EBWiz bridge, both spellcasters rely on their innate mana pool.

## Useful Links


* [1.7.10 version's MinecraftForum page](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/1292222)
* [1.7.10 Unofficial Wiki](http://am2.wikia.com/wiki/Ars_Magica_2_Wiki)

#