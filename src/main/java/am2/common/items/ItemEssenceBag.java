package am2.common.items;

import java.util.List;

import am2.ArsMagica2;
import am2.common.container.InventoryEssenceBag;
import am2.common.defs.IDDefs;
import am2.common.defs.ItemDefs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemEssenceBag extends ItemArsMagica{

	public ItemEssenceBag(){
		super();
		setMaxStackSize(1);
		setMaxDamage(0);
	}

	@Override
	public boolean getShareTag(){
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("am2.tooltip.rupees"));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand){
		ItemStack itemStackIn = playerIn.getHeldItem(hand);
		FMLNetworkHandler.openGui(playerIn, ArsMagica2.instance, IDDefs.GUI_ESSENCE_BAG, worldIn, (int)playerIn.posX, (int)playerIn.posY, (int)playerIn.posZ);
		return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
	}

	private ItemStack[] getMyInventory(ItemStack itemStack){
		return ReadFromStackTagCompound(itemStack);
	}

	public void UpdateStackTagCompound(ItemStack itemStack, ItemStack[] values){
		if (itemStack.getTagCompound() == null){
			itemStack.setTagCompound(new NBTTagCompound());
		}
		for (int i = 0; i < values.length; ++i){
			ItemStack stack = values[i];
			if (stack == null){
				itemStack.getTagCompound().removeTag("essencebagstacksize" + i);
				itemStack.getTagCompound().removeTag("essencebagmeta" + i);
				continue;
			}else{
				itemStack.getTagCompound().setInteger("essencebagstacksize" + i, stack.getCount());
				itemStack.getTagCompound().setInteger("essencebagmeta" + i, stack.getItemDamage());
			}
		}
	}

	public void UpdateStackTagCompound(ItemStack itemStack, InventoryEssenceBag inventory){
		if (itemStack.getTagCompound() == null){
			itemStack.setTagCompound(new NBTTagCompound());
		}
		for (int i = 0; i < inventory.getSizeInventory(); ++i){
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null){
				continue;
			}else{
				itemStack.getTagCompound().setInteger("essencebagstacksize" + i, stack.getCount());
				itemStack.getTagCompound().setInteger("essencebagmeta" + i, stack.getItemDamage());
			}
		}
	}

	public ItemStack[] ReadFromStackTagCompound(ItemStack itemStack){
		if (itemStack.getTagCompound() == null){
			return new ItemStack[InventoryEssenceBag.inventorySize];
		}
		ItemStack[] items = new ItemStack[InventoryEssenceBag.inventorySize];
		for (int i = 0; i < items.length; ++i){
			if (!itemStack.getTagCompound().hasKey("essencebagmeta" + i) || itemStack.getTagCompound().getInteger("essencebagmeta" + i) == -1){
				items[i] = null;
				continue;
			}
			int stacksize = itemStack.getTagCompound().getInteger("essencebagstacksize" + i);
			int meta = 0;
			if (itemStack.getTagCompound().hasKey("essencebagmeta" + i))
				meta = itemStack.getTagCompound().getInteger("essencebagmeta" + i);
			items[i] = new ItemStack(ItemDefs.essence, stacksize, meta);
		}
		return items;
	}

	public InventoryEssenceBag ConvertToInventory(ItemStack essenceBagStack){
		InventoryEssenceBag ieb = new InventoryEssenceBag();
		ieb.SetInventoryContents(getMyInventory(essenceBagStack));
		return ieb;
	}
}
