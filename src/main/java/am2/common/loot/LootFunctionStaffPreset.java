package am2.common.loot;

import am2.common.items.StaffPresets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.Random;

/**
 * Loot function that applies a random {@link StaffPresets} spell to an item.
 * <p>
 * Usage in loot table JSON:
 * <pre>
 * { "function": "arsmagica2:staff_preset" }
 * { "function": "arsmagica2:staff_preset", "tier": 0 }
 * </pre>
 * Omitting {@code tier} picks a random tier.
 */
public class LootFunctionStaffPreset extends LootFunction {

    private final int tier;

    public LootFunctionStaffPreset(LootCondition[] conditions, int tier) {
        super(conditions);
        this.tier = tier;
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        int t = tier < 0 ? rand.nextInt(StaffPresets.getMaxTier() + 1) : tier;
        NBTTagCompound preset = StaffPresets.random(t, rand);
        if (preset.isEmpty()) return stack;

        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        // Store spell data under SpellCasterCaps so ItemStaff.onUpdate() bootstraps it
        stack.getTagCompound().setTag("SpellCasterCaps", preset);

        // Apply display name if the preset includes one
        if (preset.hasKey("display")) {
            NBTTagCompound display = preset.getCompoundTag("display");
            if (display.hasKey("Name")) {
                stack.setStackDisplayName(display.getString("Name"));
            }
            // Remove display from SpellCasterCaps since it's not spell data
            stack.getTagCompound().getCompoundTag("SpellCasterCaps").removeTag("display");
        }

        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionStaffPreset> {
        public Serializer() {
            super(new ResourceLocation("arsmagica2", "staff_preset"), LootFunctionStaffPreset.class);
        }

        @Override
        public LootFunctionStaffPreset deserialize(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            int tier = JsonUtils.getInt(json, "tier", -1);
            return new LootFunctionStaffPreset(conditions, tier);
        }

        @Override
        public void serialize(JsonObject json, LootFunctionStaffPreset value, JsonSerializationContext context) {
            if (value.tier >= 0) {
                json.addProperty("tier", value.tier);
            }
        }
    }
}
