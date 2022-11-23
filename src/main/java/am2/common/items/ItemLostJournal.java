package am2.common.items;

import java.util.List;

import am2.client.gui.AMGuiHelper;
import am2.common.lore.Story;
import am2.common.lore.StoryManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLostJournal extends ItemWritableBook{

	public ItemLostJournal(){
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack par1ItemStack){
		String title = super.getItemStackDisplayName(par1ItemStack);
		if (par1ItemStack.hasTagCompound()){
			NBTTagCompound compound = par1ItemStack.getTagCompound();
			int part = compound.getInteger("story_part");
			String nbtTitle = compound.getString("title");

			title = nbtTitle + " Volume " + part;
		}

		return title;
	}

	public Story getStory(ItemStack stack){
		if (stack.hasTagCompound()){
			NBTTagCompound compound = stack.getTagCompound();
			String nbtTitle = compound.getString("title");
			Story s = StoryManager.INSTANCE.getByTitle(nbtTitle);
			return s;
		}
		return null;
	}

	public short getStoryPart(ItemStack stack){
		if (stack.hasTagCompound()){
			NBTTagCompound compound = stack.getTagCompound();
			int part = compound.getInteger("story_part");
			return (short)part;
		}
		return -1;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (worldIn.isRemote){
			AMGuiHelper.OpenBookGUI(stack);
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		super.getSubItems(tab, items);

		int sCount = 0;
		for (Story s : StoryManager.INSTANCE.allStories()){
			int meta = sCount << 16;
			for (short i = 0; i < s.getNumParts(); ++i){
				meta = sCount + i;
				ItemStack stack = new ItemStack(this, 1, meta);
				stack.setTagCompound(new NBTTagCompound());
				s.WritePartToNBT(stack.getTagCompound(), i);
				stack.getTagCompound().setString("title", s.getTitle());
				items.add(stack);
			}
		}
	}
	
	public ItemLostJournal registerAndName(String name) {
		this.setTranslationKey(new ResourceLocation("arsmagica2", name).toString());
		//// TODO: registry GameRegistry.register(this, new ResourceLocation("arsmagica2", name));
		return this;
	}
}