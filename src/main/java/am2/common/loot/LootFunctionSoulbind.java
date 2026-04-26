package am2.common.loot;

import am2.common.enchantments.AMEnchantmentHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.Random;

public class LootFunctionSoulbind extends LootFunction {

    public LootFunctionSoulbind(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        return AMEnchantmentHelper.soulbindStack(stack);
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionSoulbind> {
        public Serializer() {
            super(new ResourceLocation("arsmagica2", "soulbind"), LootFunctionSoulbind.class);
        }

        @Override
        public LootFunctionSoulbind deserialize(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new LootFunctionSoulbind(conditions);
        }

        @Override
        public void serialize(JsonObject json, LootFunctionSoulbind value, JsonSerializationContext context) {
            // No extra data to serialize
        }
    }
}
