package am2.common.lore;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.api.event.PlayerMagicLevelChangeEvent;
import am2.api.event.SkillLearnedEvent;
import am2.api.event.SpellCastEvent;
import am2.api.extensions.IArcaneCompendium;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry.EntityRegistration;

import java.util.Map;

/**
 * This class should handle compendium unlocks wherever possible through events.
 * If it is not possible, then all calls should use the static utility methods here.
 *
 * @author Mithion
 */
public class CompendiumUnlockHandler {
    /**
     * This is a catch all method - it's genericized to attempt to unlock a compendium entry for anything AM2 based that the player picks up
     *
     * @param event
     */
    @SubscribeEvent
    public void onPlayerPickupItem(EntityItemPickupEvent event) {
        if (event.getEntityPlayer().world.isRemote) return;
        IArcaneCompendium instance = ArcaneCompendium.For(event.getEntityPlayer());
        instance.unlockRelatedItems(event.getItem().getItem());
        unlockStructuresFor(event.getEntityPlayer(), event.getItem().getItem());
    }

    /**
     * Any magic level based unlocks should go in here
     *
     * @param event
     */
    @SubscribeEvent
    public void onPlayerMagicLevelChange(PlayerMagicLevelChangeEvent event) {
        if (!event.getEntity().world.isRemote && event.getEntity() instanceof EntityPlayer) {
            applyLevelUnlocks(event.getEntityPlayer(), event.getLevel());
        }
    }

    /**
     * Unlocks everything tied to a magic level at or below the given one. Also called on login so
     * players who were already past a threshold when it was added still receive the entries.
     */
    public static void applyLevelUnlocks(EntityPlayer player, int level) {
        IArcaneCompendium instance = ArcaneCompendium.For(player);
        for (Map.Entry<Integer, String[]> e : CompendiumProgression.LEVEL_UNLOCKS.entrySet()) {
            if (level >= e.getKey())
                for (String id : e.getValue())
                    instance.unlockEntry(id);
        }
        if (level >= CompendiumProgression.RITUAL_LEVEL)
            for (CompendiumEntry entry : CompendiumCategory.MECHANIC_RITUALS.getEntries())
                instance.unlockEntry(entry.getID());
        if (level >= CompendiumProgression.STRUCTURE_FALLBACK_LEVEL)
            for (CompendiumEntry entry : CompendiumCategory.STRUCTURE.getEntries())
                instance.unlockEntry(entry.getID());
    }

    /** Unlocks the structure entry whose controller block the given stack is. */
    public static void unlockStructuresFor(EntityPlayer player, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) return;
        String path = stack.getItem().getRegistryName().getPath();
        for (Map.Entry<String, String> e : CompendiumProgression.STRUCTURE_CONTROLLERS.entrySet()) {
            if (e.getValue().equals(path))
                ArcaneCompendium.For(player).unlockEntry(CompendiumCategory.STRUCTURE.getID() + "." + e.getKey());
        }
    }

    /** Unlocks every entry that shows the given skill or the spell part it represents. */
    public static void unlockEntriesForSkill(EntityPlayer player, Skill skill) {
        Object part = ArsMagicaAPI.getSpellRegistry().getValue(skill.getRegistryName());
        for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
            for (Object obj : entry.getObjects()) {
                if (obj == (part != null ? part : skill))
                    ArcaneCompendium.For(player).unlockEntry(entry.getID());
            }
        }
    }

    /** Server-side catch-up for saves that predate a rule: re-applies level and skill unlocks. */
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (player.world.isRemote) return;
        applyLevelUnlocks(player, EntityExtension.For(player).getCurrentLevel());
        for (Map.Entry<Skill, Integer> e : SkillData.For(player).getSkills().entrySet()) {
            if (e.getValue() > 0)
                unlockEntriesForSkill(player, e.getKey());
        }
    }

    /**
     * This should handle all mobs and the Astral Barrier
     *
     * @param event
     */
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!event.getEntityLiving().world.isRemote && event.getSource().getTrueSource() instanceof EntityPlayer) {
            if (event.getEntity() instanceof EntityEnderman) {
                ArcaneCompendium.For((EntityPlayer) event.getSource().getTrueSource()).unlockEntry("blockastralbarrier");
            } else {
                EntityRegistration reg = EntityRegistry.instance().lookupModSpawn(event.getEntityLiving().getClass(), true);
                if (reg != null && reg.getContainer().matches(ArsMagica.instance)) {
                    String id = reg.getEntityName();
                    ArcaneCompendium.For((EntityPlayer) event.getSource().getTrueSource()).unlockEntry(id);
                }
            }
        }
    }


    /**
     * Any skill-based unlocks should go in here
     *
     * @param event
     */
    @SubscribeEvent
    public void onSkillLearned(SkillLearnedEvent event) {
        if (event.getEntityPlayer().world.isRemote) return;
        IArcaneCompendium instance = ArcaneCompendium.For(event.getEntityPlayer());
        if (event.getSkill().equals(Skill.fromName("summon"))) {
            instance.unlockEntry("crystal_phylactery");
            instance.unlockEntry("summoner");
        } else if (event.getSkill().equals(Skill.fromName("true_sight"))) {
            instance.unlockEntry("illusionBlocks");
        } else if (event.getSkill().getPoint().equals(SkillPoint.SILVER_POINT)) {
            instance.unlockEntry("silver_skills");
        }
    }

    /**
     * Any spell-based unlocks should go here (eg, low mana based unlocks, affinity, etc.)
     *
     * @param event
     */
    @SubscribeEvent
    public void onSpellCast(SpellCastEvent.Pre event) {
        if (event.entityLiving instanceof EntityPlayer && !event.entityLiving.world.isRemote) {
            IArcaneCompendium instance = ArcaneCompendium.For((EntityPlayer) event.entityLiving);
            instance.unlockEntry("unlockingPowers");
            instance.unlockEntry("affinity");
            if (EntityExtension.For(event.entityLiving).getCurrentMana() < EntityExtension.For(event.entityLiving).getMaxMana() / 2)
                instance.unlockEntry("mana_potion");
        }
    }

    /**
     * This is another genericized method, which attempts to unlock any entry for something the player crafts
     */
    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {
        if (!event.player.world.isRemote) {
            IArcaneCompendium instance = ArcaneCompendium.For(event.player);
            instance.unlockRelatedItems(event.crafting);
            unlockStructuresFor(event.player, event.crafting);
        }
    }
}
