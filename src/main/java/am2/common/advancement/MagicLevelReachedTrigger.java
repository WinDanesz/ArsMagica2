package am2.common.advancement;

import am2.ArsMagica;
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
 * Custom advancement trigger that fires when a player's magic level reaches or exceeds
 * a percentage of the configured magic level cap.
 * JSON format: {@code "conditions": { "level_percent": 50 }} (0–100).
 * Call {@link #triggerFor(EntityPlayer, int)} from the {@code PlayerMagicLevelChangeEvent} handler.
 */
public class MagicLevelReachedTrigger implements ICriterionTrigger<MagicLevelReachedTrigger.Instance> {

    private final ResourceLocation id;
    private final SetMultimap<PlayerAdvancements, Listener<Instance>> listeners = HashMultimap.create();

    public static class Instance extends AbstractCriterionInstance {
        private final int levelPercent;

        public Instance(ResourceLocation triggerId, int levelPercent) {
            super(triggerId);
            this.levelPercent = levelPercent;
        }

        public boolean test(int currentLevel) {
            int required = (int) Math.ceil(levelPercent / 100.0 * ArsMagica.config.getMagicLevelCap());
            return currentLevel >= required;
        }
    }

    public MagicLevelReachedTrigger(ResourceLocation id) {
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
        int levelPercent = json.has("level_percent") ? json.get("level_percent").getAsInt() : 100;
        return new Instance(id, levelPercent);
    }

    public void triggerFor(EntityPlayer player, int currentLevel) {
        if (player instanceof EntityPlayerMP) {
            PlayerAdvancements advances = ((EntityPlayerMP) player).getAdvancements();
            for (Listener<Instance> listener : listeners.get(advances)) {
                if (listener.getCriterionInstance().test(currentLevel)) {
                    listener.grantCriterion(advances);
                }
            }
        }
    }
}
