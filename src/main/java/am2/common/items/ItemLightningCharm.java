package am2.common.items;

import java.util.List;

import am2.api.math.AMVector3;
import am2.common.utils.MathUtilities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemLightningCharm extends ItemArsMagica{

	private static final String KEY_ACTIVE = "IsActive";

	public ItemLightningCharm(){
		super();
	}

	private boolean isActive(ItemStack stack){
		if (!stack.hasTagCompound())
			return false;

		return stack.getTagCompound().getByte(KEY_ACTIVE) == (byte)1;
	}

	private void toggleActive(ItemStack stack){
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		if (isActive(stack))
			stack.getTagCompound().setByte(KEY_ACTIVE, (byte)0);
		else
			stack.getTagCompound().setByte(KEY_ACTIVE, (byte)1);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking())
			toggleActive(stack);
		return EnumActionResult.SUCCESS;
	}

	private void attractItems(World world, Entity ent){
		double distance = 16;
		int hDist = 5;
		List<Entity> entities = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(ent.posX - distance, ent.posY - hDist, ent.posZ - distance, ent.posX + distance, ent.posY + hDist, ent.posZ + distance));
		for (Entity e : entities){
			EntityItem item = (EntityItem)e;
			if (item.getAge() < 10){
				continue;
			}
			AMVector3 movement = MathUtilities.GetMovementVectorBetweenPoints(new AMVector3(e), new AMVector3(ent.posX, ent.posY, ent.posZ));

			if (!world.isRemote){
				float factor = 0.35f;
				if (movement.y > 0) movement.y = 0;
				double x = -(movement.x * factor);
				double y = -(movement.y * factor);
				double z = -(movement.z * factor);
				e.addVelocity(x, y, z);
				item.setPickupDelay(0);
				if (Math.abs(e.motionX) > Math.abs(x * 2)){
					e.motionX = x * (e.motionX / e.motionX);
				}
				if (Math.abs(e.motionY) > Math.abs(y * 2)){
					e.motionY = y * (e.motionY / e.motionY);
				}
				if (Math.abs(e.motionZ) > Math.abs(z * 2)){
					e.motionZ = z * (e.motionZ / e.motionZ);
				}
			}
		}
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5){
		if (isActive(par1ItemStack))
			attractItems(par2World, par3Entity);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack) {
		return isActive(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("am2.tooltip.lightning_charm"));
		super.addInformation(stack, world, tooltip, flag);
	}
}
