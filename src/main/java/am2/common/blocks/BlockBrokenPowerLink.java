package am2.common.blocks;

import am2.ArsMagica;
import am2.common.armor.ArmorHelper;
import am2.common.blocks.tileentity.TileEntityBrokenPowerLink;
import am2.common.registry.AMItems;
import am2.common.registry.ImbuementRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockBrokenPowerLink extends BlockAMContainer {

    public BlockBrokenPowerLink() {
        super(Material.CIRCUITS);
        setBlockUnbreakable(); // can't be broken, but can be directly replaced, and has no collisions
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityBrokenPowerLink();
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if (ArsMagica.proxy.getLocalPlayer() != null &&
                !ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() &&
                (ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItems.magitech_goggles)
                || ArmorHelper.isInfusionPreset(ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD), ImbuementRegistry.MAGITECH_GOGGLE_INTEGRATION))
            return FULL_BLOCK_AABB;
        return NULL_AABB;
    }

    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        if (ArsMagica.proxy.getLocalPlayer() != null &&
                !ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() &&
                (ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == AMItems.magitech_goggles)
                || ArmorHelper.isInfusionPreset(ArsMagica.proxy.getLocalPlayer().getItemStackFromSlot(EntityEquipmentSlot.HEAD), ImbuementRegistry.MAGITECH_GOGGLE_INTEGRATION))
            return FULL_BLOCK_AABB;
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }
}
