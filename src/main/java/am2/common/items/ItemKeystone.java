package am2.common.items;

import am2.ArsMagica;
import am2.common.container.InventoryKeyStone;
import am2.common.defs.IDDefs;
import am2.common.registry.AMItems;
import am2.common.utils.KeystoneUtilities;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

public class ItemKeystone extends Item {

    public static final int KEYSTONE_INVENTORY_SIZE = 3;

    public ItemKeystone() {
        super();
        setMaxStackSize(1);
    }

    public static void addCombination(ItemStack stack, String name, int[] metas) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        int comboID = numCombinations(stack);
        boolean isNew = true;

        for (int i = 0; i < comboID; ++i) {
            if (name.equals(stack.getTagCompound().getString("Combination_" + i + "_name"))) {
                comboID = i;
                isNew = false;
                break;
            }
        }

        stack.getTagCompound().setString("Combination_" + comboID + "_name", name);
        stack.getTagCompound().setIntArray("Combination_" + comboID + "_metas", metas);

        if (isNew)
            stack.getTagCompound().setInteger("numKeystoneCombinations", comboID + 1);
    }

    public static void removeCombination(ItemStack stack, String name) {
        int c = numCombinations(stack);
        int removedIndex = -1;
        for (int i = 0; i < c; ++i) {
            KeystoneCombination combo = ((ItemKeystone) AMItems.keystone).getCombinationAt(stack, i);
            if (combo.name.equals(name)) {
                removedIndex = i;
                break;
            }
        }

        if (removedIndex == -1)
            return;

        for (int i = removedIndex + 1; i < c; ++i) {
            String tName = stack.getTagCompound().getString("Combination_" + i + "_name");
            int[] tMetas = stack.getTagCompound().getIntArray("Combination_" + i + "_metas");

            stack.getTagCompound().setString("Combination_" + (i - 1) + "_name", tName);
            stack.getTagCompound().setIntArray("Combination_" + (i - 1) + "_metas", tMetas);
        }

        stack.getTagCompound().removeTag("Combination_" + c + "_name");
        stack.getTagCompound().removeTag("Combination_" + c + "_metas");
        stack.getTagCompound().setInteger("numKeystoneCombinations", c - 1);
    }

    public static int numCombinations(ItemStack stack) {
        if (!stack.hasTagCompound()) return 0;
        return stack.getTagCompound().getInteger("numKeystoneCombinations");
    }

    public KeystoneCombination getCombinationAt(ItemStack stack, int index) {
        if (!stack.hasTagCompound()) return null;

        if (numCombinations(stack) <= index) return null;

        String name = stack.getTagCompound().getString("Combination_" + index + "_name");
        int[] metas = stack.getTagCompound().getIntArray("Combination_" + index + "_metas");

        return new KeystoneCombination(name, metas);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(ArsMagica.instance, IDDefs.GUI_KEYSTONE, world, player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private ItemStack[] getMyInventory(ItemStack itemStack) {
        return ReadFromStackTagCompound(itemStack);
    }

    public String getRecipeAsString(ItemStack keystoneStack) {
        String s = "Recipe: ";
        for (ItemStack stack : getMyInventory(keystoneStack)) {
            s += stack.getDisplayName().replace("Rune ", "") + " ";
        }
        return s;
    }

    public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        for (int i = 0; i < values.length; ++i) {
            ItemStack stack = values[i];
            if (stack == ItemStack.EMPTY) {
                itemStack.getTagCompound().removeTag("keystonemeta" + i);
            } else if (stack.getItem() == AMItems.rune) {
                itemStack.getTagCompound().setInteger("keystonemeta" + i, itemStack.getItemDamage());
            }
        }

        if (itemStack.getTagCompound().getKeySet().isEmpty()) {
            itemStack.setTagCompound(null);
        }
    }

    public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            ItemStack[] list = new ItemStack[0];
            //	Arrays.fill(list, ItemStack.EMPTY);
            return list;
        }
        ItemStack[] items = new ItemStack[InventoryKeyStone.inventorySize];
        Arrays.fill(items, ItemStack.EMPTY);
        for (int i = 0; i < items.length; ++i) {
            int meta = 0;
            if (!itemStack.getTagCompound().hasKey("keystonemeta" + i)) {
                items[i] = ItemStack.EMPTY;
                continue;
            } else if (itemStack.getTagCompound().getInteger("keystonemeta" + i) == -1) {
                items[i] = ItemStack.EMPTY;
                continue;
            } else {
                meta = itemStack.getTagCompound().getInteger("keystonemeta" + i);
            }

            items[i] = new ItemStack(AMItems.rune, 1, meta);
        }
        return items;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        ItemStack[] items = getMyInventory(stack);

        StringBuilder s = new StringBuilder(I18n.translateToLocalFormatted("am2.tooltip.open"));
        tooltip.add("\2477" + s);

        if (items.length > 0) {
            s = new StringBuilder(I18n.translateToLocalFormatted("am2.tooltip.runes") + ": ");
            tooltip.add("\2477" + s);
            s = new StringBuilder();
            for (int i = 0; i < KEYSTONE_INVENTORY_SIZE; ++i) {
                if (items[i] == null) continue;
                if (items[i] == ItemStack.EMPTY) {
                    s.append("Empty ");
                } else {
                    s.append(items[i].getDisplayName().replace("Rune", "").trim()).append(" ");
                }
            }
            if (s.toString().equals("")) s = new StringBuilder(I18n.translateToLocalFormatted("am2.tooltip.none"));
            tooltip.add("\2477" + s);
        }
    }

    public long getKey(ItemStack keystoneStack) {
        ItemStack[] inventory = getMyInventory(keystoneStack);
        if (inventory == null) return 0;
        return KeystoneUtilities.instance.getKeyFromRunes(inventory);
    }

    public class KeystoneCombination {
        public int[] metas;
        public String name;

        public KeystoneCombination(String name, int[] metas) {
            this.metas = metas;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof KeystoneCombination) {
                boolean match = ((KeystoneCombination) obj).metas.length == metas.length;
                if (!match) return false;

                for (int i = 0; i < this.metas.length; ++i) {
                    match &= (this.metas[i] == ((KeystoneCombination) obj).metas[i]);
                }

                return match;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int sum = 0;
            for (int i : metas)
                sum += i;
            return sum;
        }
    }

    public InventoryKeyStone getInventory(ItemStack keyStoneStack) {
        InventoryKeyStone iks = new InventoryKeyStone();
        iks.setBagStack(keyStoneStack);
        iks.SetInventoryContents(getMyInventory(keyStoneStack));
        return iks;
    }
}
