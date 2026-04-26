package am2.common.spell;


import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistryHelper;
import am2.api.event.SpellCastEvent;
import am2.api.skill.Skill;
import am2.api.spell.SpellData;
import am2.api.spell.SpellPart;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.registry.AMSkills;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class SpellUnlockManager {

    private ArrayList<UnlockEntry> entries;

    public SpellUnlockManager() {
        init();
    }

    @SubscribeEvent
    public void onSpellCast(SpellCastEvent.Pre event) {
        if (event.entityLiving instanceof EntityPlayer) {
            if (EntityExtension.For(event.entityLiving).getCurrentMana() < event.manaCost)
                return;
            for (UnlockEntry entry : entries) {
                //check unlocks
                if (!event.entityLiving.world.isRemote) {
                    if (entry.willSpellUnlock(event.spell)) {
                        entry.unlockFor((EntityPlayer) event.entityLiving);
                    }
                }
            }
        }
    }

    public void init() {
        entries = new ArrayList<UnlockEntry>();
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:falling_star")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:magic_damage"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:gravity"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:astral_distortion")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:blizzard")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:storm"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:frost_damage"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:freeze"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:damage")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:fire_rain")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:storm"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:fire_damage"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:ignition"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:damage")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:mana_blast")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:magic_damage"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:damage")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:dismembering")),
                SpellRegistryHelper.getModifierFromName("arsmagica2:piercing"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:damage")));

        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:mana_link")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:mana_drain"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:entangle")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:mana_shield")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:shield"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:reflect"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:life_tap")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:buff_power")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:haste"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:slowfall"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:swift_swim"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:gravity_well"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:leap")));

        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:daylight")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:true_sight"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:divine_intervention"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:light")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:moonrise")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:night_vision"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:ender_intervention"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:lunar")));
        entries.add(new UnlockEntry(ArsMagicaAPI.getSkillRegistry().getValue(new ResourceLocation("arsmagica2:prosperity")),
                SpellRegistryHelper.getComponentFromName("arsmagica2:dig"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:feather_touch"),
                SpellRegistryHelper.getModifierFromName("arsmagica2:mining_power")));

        entries.add(new UnlockEntry(AMSkills.shield_overload,
                SpellRegistryHelper.getComponentFromName("arsmagica2:mana_shield"),
                SpellRegistryHelper.getComponentFromName("arsmagica2:mana_drain")));

    }

    class UnlockEntry {
        private Skill unlock;
        private SpellPart[] requiredComponents;

        public UnlockEntry(Skill unlock, SpellPart... components) {
            this.unlock = unlock;
            this.requiredComponents = components;
        }

        public boolean partIsInStage(SpellData spell, SpellPart part, int stage) {
            if (part == null)
                return false;
            for (List<SpellPart> parts : spell.getStages())
                for (SpellPart p : parts)
                    if (part.getClass().isInstance(p))
                        return true;

            return false;
        }

        public boolean willSpellUnlock(SpellData spell) {
            boolean found = true;
            for (SpellPart part : requiredComponents) {
                if (!partIsInStage(spell, part, 0)) {
                    found = false;
                    break;
                }
            }
            if (found)
                return true;
            return false;
        }

        public void unlockFor(EntityPlayer player) {
            if (!player.world.isRemote) {
                SkillData.For(player).unlockSkill(unlock.getID());
            }
        }
    }
}
