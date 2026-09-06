package am2.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.Random;

/**
 * Marks a loot-table item as fireproof (see {@link am2.common.utils.EntityUtils#setFireImmune}), persisting
 * across re-drops since the flag lives on the ItemStack's NBT rather than on any one EntityItem instance.
 */
public class LootFunctionFireproof extends LootFunction {

    public static final String NBT_TAG = "AMFireproof";

    public LootFunctionFireproof(LootCondition[] conditions) {
        super(conditions);
    }

    @Override
    public ItemStack apply(ItemStack stack, Random rand, LootContext context) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setBoolean(NBT_TAG, true);
        return stack;
    }

    public static class Serializer extends LootFunction.Serializer<LootFunctionFireproof> {
        public Serializer() {
            super(new ResourceLocation("arsmagica2", "fireproof"), LootFunctionFireproof.class);
        }

        @Override
        public LootFunctionFireproof deserialize(JsonObject json, JsonDeserializationContext context, LootCondition[] conditions) {
            return new LootFunctionFireproof(conditions);
        }

        @Override
        public void serialize(JsonObject json, LootFunctionFireproof value, JsonSerializationContext context) {
            // No extra data to serialize
        }
    }
}
