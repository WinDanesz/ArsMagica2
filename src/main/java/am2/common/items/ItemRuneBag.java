package am2.common.items;

import am2.ArsMagica;
import am2.common.container.InventoryKeyStone;
import am2.common.container.InventoryRuneBag;
import am2.common.defs.IDDefs;
import am2.common.registry.AMItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.Arrays;

public class ItemRuneBag extends Item {

    public ItemRuneBag() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(ArsMagica.instance, IDDefs.GUI_RUNE_BAG, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
        return super.onItemRightClick(world, player, hand);
    }

    public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        for (int i = 0; i < values.length; ++i) {
            ItemStack stack = values[i];
            if (stack == ItemStack.EMPTY) {
                itemStack.getTagCompound().removeTag("runebagmeta" + i);
            } else if (stack.getItem() == AMItems.rune) {
                itemStack.getTagCompound().setInteger("runebagmeta" + i, stack.getItemDamage());
            }
        }

        if (itemStack.getTagCompound().getKeySet().isEmpty()) {
            itemStack.setTagCompound(null);
        }
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    public void UpdateStackTagCompound(ItemStack itemStack, InventoryRuneBag inventory) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        for (int i = 0; i < inventory.getSizeInventory(); ++i) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            } else {
                itemStack.getTagCompound().setInteger("runebagmeta" + i, stack.getItemDamage());
            }
        }
    }

    private static ItemStack[] readFromStackTagCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            ItemStack[] list = new ItemStack[InventoryKeyStone.inventorySize];
            Arrays.fill(list, ItemStack.EMPTY);
            return list;
        }
        ItemStack[] items = new ItemStack[InventoryRuneBag.inventorySize];
        Arrays.fill(items, ItemStack.EMPTY);
        for (int i = 0; i < items.length; ++i) {
            if (!itemStack.getTagCompound().hasKey("runebagmeta" + i) || itemStack.getTagCompound().getInteger("runebagmeta" + i) == -1) {
                items[i] = ItemStack.EMPTY;
                continue;
            }
            int meta = 0;
            meta = itemStack.getTagCompound().getInteger("runebagmeta" + i);
            items[i] = new ItemStack(AMItems.rune, 1, meta);
        }
        return items;
    }

    public static InventoryRuneBag getInventory(ItemStack runeBagStack) {
        if (runeBagStack == ItemStack.EMPTY) {
            return InventoryRuneBag.EMPTY;
        }
        InventoryRuneBag irb = new InventoryRuneBag();
        irb.setBagStack(runeBagStack);
        irb.SetInventoryContents(readFromStackTagCompound(runeBagStack));
        return irb;
    }
}
