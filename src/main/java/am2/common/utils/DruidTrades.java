package am2.common.utils;

import am2.ArsMagica;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Generates and restocks the Druid's randomized plant trades (villager-style, via {@link
 * net.minecraft.entity.IMerchant}). Every buyable and sellable item has a trade "value" in
 * AMConfig's {@code druid_trades} category; an offer prices itself by matching the sell side's
 * total value (unit value times how many are sold) against the chosen buy item's value, so
 * changing one value in the config reprices every offer that touches that item instead of
 * needing per-offer ranges.
 *
 * <p>Buy items (Bread, Mushroom Stew, Standard Mana Potion, Vinteum Dust, Moonstone) and sell
 * items (Cerublossom, Desert Nova, Tarma Root, Wakebloom, Aum, Nature Essence) are disjoint sets
 * - nothing a druid sells is also something a druid buys - so no offer's output can be fed back
 * in as another offer's input. Neither list touches essence-to-herb backcrafting: Nature Essence
 * only ever appears as an output here, and no recipe in the mod turns it (or Bread, Mushroom
 * Stew, Vinteum Dust, or Moonstone) back into any of the sell items, so there is no buy-craft-
 * sell loop regardless of the configured values.
 */
public class DruidTrades {

    private DruidTrades() {
    }

    private static final class BuyOption {
        final ItemStack sample;
        final int value;
        final boolean cheap;

        BuyOption(ItemStack sample, int value, boolean cheap) {
            this.sample = sample;
            this.value = value;
            this.cheap = cheap;
        }
    }

    private static final class SellOption {
        final ItemStack sample;
        final int value;
        final boolean rare;

        SellOption(ItemStack sample, int value, boolean rare) {
            this.sample = sample;
            this.value = value;
            this.rare = rare;
        }
    }

    private static List<BuyOption> buyOptions() {
        List<BuyOption> list = new ArrayList<>();
        list.add(new BuyOption(new ItemStack(Items.BREAD), ArsMagica.config.getDruidBuyValueBread(), true));
        list.add(new BuyOption(new ItemStack(Items.MUSHROOM_STEW), ArsMagica.config.getDruidBuyValueMushroomStew(), true));
        list.add(new BuyOption(new ItemStack(AMItems.vinteum_dust), ArsMagica.config.getDruidBuyValueVinteumDust(), true));
        list.add(new BuyOption(new ItemStack(AMItems.standard_mana_potion), ArsMagica.config.getDruidBuyValueManaPotion(), false));
        list.add(new BuyOption(new ItemStack(AMItems.moonstone), ArsMagica.config.getDruidBuyValueMoonstone(), false));
        return list;
    }

    private static List<SellOption> sellOptions() {
        List<SellOption> list = new ArrayList<>();
        list.add(new SellOption(new ItemStack(AMBlocks.cerublossom), ArsMagica.config.getDruidSellValueCerublossom(), false));
        list.add(new SellOption(new ItemStack(AMBlocks.desert_nova), ArsMagica.config.getDruidSellValueDesertNova(), false));
        list.add(new SellOption(new ItemStack(AMBlocks.tarma_root), ArsMagica.config.getDruidSellValueTarmaRoot(), false));
        list.add(new SellOption(new ItemStack(AMBlocks.wakebloom), ArsMagica.config.getDruidSellValueWakebloom(), false));
        list.add(new SellOption(new ItemStack(AMBlocks.aum), ArsMagica.config.getDruidSellValueAum(), true));
        list.add(new SellOption(new ItemStack(AMItems.essence_nature), ArsMagica.config.getDruidSellValueNatureEssence(), true));
        return list;
    }

    public static MerchantRecipeList generate(Random rand) {
        List<BuyOption> buys = buyOptions();
        List<SellOption> sells = sellOptions();

        List<int[]> pairs = new ArrayList<>();
        for (int b = 0; b < buys.size(); b++) {
            for (int s = 0; s < sells.size(); s++) {
                pairs.add(new int[]{b, s});
            }
        }
        Collections.shuffle(pairs, rand);

        MerchantRecipeList list = new MerchantRecipeList();
        Set<Integer> soldItems = new HashSet<>(); // each sell item may only appear in one offer

        // Guarantee one accessible offer: a cheap everyday currency (bread/stew/vinteum dust)
        // for a common (non-rare) herb, so every druid has an easy entry-level trade.
        for (int[] pair : pairs) {
            if (buys.get(pair[0]).cheap && !sells.get(pair[1]).rare) {
                list.add(makeOffer(buys.get(pair[0]), sells.get(pair[1]), rand));
                soldItems.add(pair[1]);
                break;
            }
        }

        int offerCount = Math.min(2 + rand.nextInt(3), sells.size()); // 2-4 total, capped at one offer per sell item
        for (int[] pair : pairs) {
            if (list.size() >= offerCount) break;
            if (soldItems.contains(pair[1])) continue;
            list.add(makeOffer(buys.get(pair[0]), sells.get(pair[1]), rand));
            soldItems.add(pair[1]);
        }

        return list;
    }

    /** Mirrors EntityVillager's restock behaviour: disabled offers get more max uses, not a use-count reset. */
    public static void restock(MerchantRecipeList list, Random rand) {
        if (list == null) return;
        for (MerchantRecipe recipe : list) {
            if (!recipe.isRecipeDisabled()) continue;
            boolean rare = isRareSellItem(recipe.getItemToSell());
            int min = rare ? ArsMagica.config.getDruidTradeRareUsesMin() : ArsMagica.config.getDruidTradeOrdinaryUsesMin();
            int max = rare ? ArsMagica.config.getDruidTradeRareUsesMax() : ArsMagica.config.getDruidTradeOrdinaryUsesMax();
            recipe.increaseMaxTradeUses(randRange(rand, min, max));
        }
    }

    private static MerchantRecipe makeOffer(BuyOption buy, SellOption sell, Random rand) {
        int sellQty = sell.rare ? 1 : randRange(rand, ArsMagica.config.getDruidTradeCommonSellQtyMin(), ArsMagica.config.getDruidTradeCommonSellQtyMax());
        int buyQty = Math.max(1, ceilDiv(sell.value * sellQty, buy.value));
        int uses = sell.rare
                ? randRange(rand, ArsMagica.config.getDruidTradeRareUsesMin(), ArsMagica.config.getDruidTradeRareUsesMax())
                : randRange(rand, ArsMagica.config.getDruidTradeOrdinaryUsesMin(), ArsMagica.config.getDruidTradeOrdinaryUsesMax());

        ItemStack buyStack = buy.sample.copy();
        buyStack.setCount(buyQty);
        ItemStack sellStack = sell.sample.copy();
        sellStack.setCount(sellQty);
        return new MerchantRecipe(buyStack, ItemStack.EMPTY, sellStack, 0, uses);
    }

    private static boolean isRareSellItem(ItemStack sellStack) {
        Item item = sellStack.getItem();
        return item == Item.getItemFromBlock(AMBlocks.aum) || item == AMItems.essence_nature;
    }

    private static int ceilDiv(int numerator, int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    private static int randRange(Random rand, int min, int max) {
        if (max <= min) return min;
        return min + rand.nextInt(max - min + 1);
    }
}
