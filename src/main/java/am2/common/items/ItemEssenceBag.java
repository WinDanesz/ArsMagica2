package am2.common.items;

import am2.ArsMagica;
import am2.common.container.InventoryEssenceBag;
import am2.common.defs.IDDefs;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class ItemEssenceBag extends Item {

    public ItemEssenceBag() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("am2.tooltip.rupees"));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        playerIn.openGui(ArsMagica.instance, IDDefs.GUI_ESSENCE_BAG, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
        return new ActionResult<>(EnumActionResult.PASS, itemStackIn);
    }

    public void updateStackTagCompound(ItemStack itemStack, ItemStack[] values) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        for (int i = 0; i < values.length; ++i) {
            ItemStack stack = values[i];
            if (stack == ItemStack.EMPTY) {
                itemStack.getTagCompound().removeTag("essencebagstacksize" + i);
                itemStack.getTagCompound().removeTag("essencebagitem" + i);
            } else if (stack.getItem() instanceof ItemEssence) {
                itemStack.getTagCompound().setInteger("essencebagstacksize" + i, stack.getCount());
                itemStack.getTagCompound().setString("essencebagitem" + i, stack.getItem().getRegistryName().toString());
            }
        }

        if (itemStack.getTagCompound().getKeySet().isEmpty()) {
            itemStack.setTagCompound(null);
        }
    }

    private static ItemStack[] readFromStackTagCompound(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            ItemStack[] list = new ItemStack[InventoryEssenceBag.inventorySize];
            Arrays.fill(list, ItemStack.EMPTY);
            return list;
        }
        ItemStack[] items = new ItemStack[InventoryEssenceBag.inventorySize];
        for (int i = 0; i < items.length; ++i) {
            if (!itemStack.getTagCompound().hasKey("essencebagitem" + i)) {
                items[i] = ItemStack.EMPTY;
                continue;
            }
            int stacksize = itemStack.getTagCompound().getInteger("essencebagstacksize" + i);
            String itemName = itemStack.getTagCompound().getString("essencebagitem" + i);
            net.minecraft.item.Item essenceItem = net.minecraft.item.Item.getByNameOrId(itemName);
            if (essenceItem != null) {
                items[i] = new ItemStack(essenceItem, stacksize);
            } else {
                items[i] = ItemStack.EMPTY;
            }
        }
        return items;
    }

    public static InventoryEssenceBag getInventory(ItemStack essenceBagStack) {
        InventoryEssenceBag inventoryEssenceBag = new InventoryEssenceBag();
        inventoryEssenceBag.setInventoryContents(readFromStackTagCompound(essenceBagStack));
        return inventoryEssenceBag;
    }
}
