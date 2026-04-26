package am2.common.advancement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * Custom advancement trigger that fires when a player unlocks every non-disabled Occulus skill.
 * Call {@link #triggerFor(EntityPlayer)} from {@code SkillData.unlockSkill()} after confirming
 * all skills are unlocked.
 */
public class AllSkillsUnlockedTrigger implements ICriterionTrigger<AllSkillsUnlockedTrigger.Instance> {

    private final ResourceLocation id;
    private final SetMultimap<PlayerAdvancements, Listener<Instance>> listeners = HashMultimap.create();

    public static class Instance extends AbstractCriterionInstance {
        public Instance(ResourceLocation triggerId) {
            super(triggerId);
        }
    }

    public AllSkillsUnlockedTrigger(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancements, Listener<Instance> listener) {
        listeners.put(playerAdvancements, listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancements, Listener<Instance> listener) {
        listeners.remove(playerAdvancements, listener);
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancements) {
        listeners.removeAll(playerAdvancements);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        return new Instance(id);
    }

    public void triggerFor(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            PlayerAdvancements advances = ((EntityPlayerMP) player).getAdvancements();
            listeners.get(advances).forEach(listener -> listener.grantCriterion(advances));
        }
    }
}
