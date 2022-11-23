package am2.common.armor;

import java.util.List;

import am2.client.utils.ModelLibrary;
import am2.common.defs.ItemDefs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ItemWaterGuardianOrbs extends AMArmor{

	public ItemWaterGuardianOrbs(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4){
		super(inheritFrom, enumarmormaterial, par3, par4);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
			ModelBiped _default){
		return ModelLibrary.instance.waterOrbs;
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot){
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		tooltip.add(I18n.format("am2.tooltip.water_orbs"));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){
		return "arsmagica2:textures/mobs/bosses/water_guardian.png";
	}

	@Override
	public void onArmorTick(World world, EntityPlayer plr, ItemStack stack) {
		super.onArmorTick(world, plr, stack);
		Potion waterBreathing = Potion.getPotionFromResourceLocation(new ResourceLocation("water_breathing").toString());
		if (!plr.isPotionActive(waterBreathing))
			plr.addPotionEffect(new PotionEffect(waterBreathing));

		if (plr.isInWater());
			reverseMaterialAcceleration(world, plr);
	}

	public void reverseMaterialAcceleration(World world, EntityPlayer entityIn)
	{
		AxisAlignedBB bb = entityIn.getEntityBoundingBox();

		int minX = MathHelper.floor(bb.minX);
		int maxX = MathHelper.ceil(bb.maxX);
		int minY = MathHelper.floor(bb.minY);
		int maxY = MathHelper.ceil(bb.maxY);
		int minZ = MathHelper.floor(bb.minZ);
		int maxZ = MathHelper.ceil(bb.maxZ);

		Vec3d vec3d = Vec3d.ZERO;
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		for (int k1 = minX; k1 < maxX; ++k1)
			for (int l1 = minY; l1 < maxY; ++l1)
				for (int i2 = minZ; i2 < maxZ; ++i2)
				{
					blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
					IBlockState iblockstate = world.getBlockState(blockpos$pooledmutableblockpos);
					Block block = iblockstate.getBlock();

					Boolean result = block.isEntityInsideMaterial(world, blockpos$pooledmutableblockpos, iblockstate, entityIn, (double)maxY, Material.WATER, false);
					if (result != null && result == true)
					{
						vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
						continue;
					}
					else if (result != null && result == false) continue;
					if (iblockstate.getMaterial() == Material.WATER)
					{
						double d0 = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer)iblockstate.getValue(BlockLiquid.LEVEL)).intValue()));
						if ((double)maxY >= d0)
							vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
					}
				}
		blockpos$pooledmutableblockpos.release();
		if (vec3d.length() > 0.0D && entityIn.isPushedByWater())
		{
			vec3d = vec3d.normalize();
			//double d1 = 0.014D;
			entityIn.motionX -= vec3d.x * 0.014D;
			entityIn.motionY -= vec3d.y * 0.014D;
			entityIn.motionZ -= vec3d.z * 0.014D;
		}

	}
}
