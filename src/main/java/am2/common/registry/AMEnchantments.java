package am2.common.registry;

import am2.ArsMagica;
import am2.common.enchantments.EnchantMagicResist;
import am2.common.enchantments.EnchantmentSoulbound;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(ArsMagica.MODID)
@Mod.EventBusSubscriber(modid = ArsMagica.MODID)
public class AMEnchantments {

    public static EnchantMagicResist magic_resist = placeholder();
    public static EnchantmentSoulbound soulbound = placeholder();

    @Nonnull
    @SuppressWarnings("ConstantConditions")
    public static <T> T placeholder() {
        return null;
    }

    @SubscribeEvent
    public static void registerEnchantments(RegistryEvent.Register<Enchantment> event) {
        IForgeRegistry<Enchantment> registry = event.getRegistry();
        magic_resist = register(registry, "magic_resist", new EnchantMagicResist(Enchantment.Rarity.COMMON));
        soulbound = register(registry, "soulbound", new EnchantmentSoulbound(Enchantment.Rarity.RARE));
    }

    private static <T extends Enchantment> T register(IForgeRegistry<Enchantment> registry, String name, T enchantment) {
        enchantment.setRegistryName(ArsMagica.MODID, name);
        registry.register(enchantment);
        return enchantment;
    }

    public static int GetEnchantmentLevelSpecial(Enchantment ench, ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(ench, stack);
    }
}
