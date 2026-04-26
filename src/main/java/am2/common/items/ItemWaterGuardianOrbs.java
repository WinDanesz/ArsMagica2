package am2.common.items;

import am2.client.utils.ModelLibrary;
import am2.common.armor.ArsMagicaArmorMaterial;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemWaterGuardianOrbs extends AMArmor implements IBauble {

    public ItemWaterGuardianOrbs(ArmorMaterial inheritFrom, ArsMagicaArmorMaterial enumarmormaterial, int par3, EntityEquipmentSlot par4) {
        super(inheritFrom, enumarmormaterial, par3, par4);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
                                    ModelBiped _default) {
        return ModelLibrary.instance.waterOrbs;
    }

    @Override
    public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(I18n.format("am2.tooltip.water_orbs"));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "arsmagica2:textures/entities/bosses/water_guardian.png";
    }

    // ---------------------------------------------------------------
    // Armor slot tick (standard leg-slot usage)
    // ---------------------------------------------------------------

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
        super.onArmorTick(world, player, stack);
        applyWaterOrbEffects(world, player);
    }

    // ---------------------------------------------------------------
    // IBauble – Baubles belt-slot usage
    // ---------------------------------------------------------------

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.BELT;
    }

    @Override
    @Optional.Method(modid = "baubles")
    public void onWornTick(ItemStack stack, EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            applyWaterOrbEffects(player.world, player);
        }
    }

    // ---------------------------------------------------------------
    // Shared logic
    // ---------------------------------------------------------------

    private void applyWaterOrbEffects(World world, EntityPlayer player) {
        if (player.ticksExisted % 20 == 0) {
            player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 80, 0));
        }
        reverseMaterialAcceleration(world, player);
    }

    public void reverseMaterialAcceleration(World world, EntityPlayer entityIn) {
        AxisAlignedBB bb = entityIn.getEntityBoundingBox();

        int minX = MathHelper.floor(bb.minX);
        int maxX = MathHelper.ceil(bb.maxX);
        int minY = MathHelper.floor(bb.minY);
        int maxY = MathHelper.ceil(bb.maxY);
        int minZ = MathHelper.floor(bb.minZ);
        int maxZ = MathHelper.ceil(bb.maxZ);

        Vec3d vec3d = new Vec3d(0, 0, 0);
        BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

        for (int k1 = minX; k1 < maxX; ++k1)
            for (int l1 = minY; l1 < maxY; ++l1)
                for (int i2 = minZ; i2 < maxZ; ++i2) {
                    blockpos$pooledmutableblockpos.setPos(k1, l1, i2);
                    IBlockState iblockstate = world.getBlockState(blockpos$pooledmutableblockpos);
                    Block block = iblockstate.getBlock();

                    Boolean result = block.isEntityInsideMaterial(world, blockpos$pooledmutableblockpos, iblockstate, entityIn, (double) maxY, Material.WATER, false);
                    if (result != null && result == true) {
                        vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
                        continue;
                    } else if (result != null && result == false) continue;
                    if (iblockstate.getMaterial() == Material.WATER) {
                        double d0 = (double) ((float) (l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue()));
                        if ((double) maxY >= d0)
                            vec3d = block.modifyAcceleration(world, blockpos$pooledmutableblockpos, entityIn, vec3d);
                    }
                }
        blockpos$pooledmutableblockpos.release();
        if (vec3d.length() > 0.0D && entityIn.isPushedByWater()) {
            vec3d = vec3d.normalize();
            //double d1 = 0.014D;
            entityIn.motionX -= vec3d.x * 0.014D;
            entityIn.motionY -= vec3d.y * 0.014D;
            entityIn.motionZ -= vec3d.z * 0.014D;
        }

    }
}
