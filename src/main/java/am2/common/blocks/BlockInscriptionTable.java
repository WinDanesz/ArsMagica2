package am2.common.blocks;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.defs.IDDefs;
import am2.common.registry.AMItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockInscriptionTable extends BlockAMSpecialRenderContainer {

    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final PropertyBool LEFT = PropertyBool.create("left");
    public static final PropertyBool TIER_1 = PropertyBool.create("tier_1");
    public static final PropertyBool TIER_2 = PropertyBool.create("tier_2");
    public static final PropertyBool TIER_3 = PropertyBool.create("tier_3");

    public BlockInscriptionTable() {
        super(Material.WOOD);

        //setTextureFile(AMCore.proxy.getOverrideBlockTexturePath());
        this.setHardness(2.0f);
        this.setResistance(2.0f);
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(LEFT, false).withProperty(TIER_1, false).withProperty(TIER_2, false).withProperty(TIER_3, false));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().rotateY());
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        BlockPos placePos = pos.offset(state.getValue(FACING));
        if (worldIn.isAirBlock(placePos) || worldIn.getBlockState(placePos).getBlock().isReplaceable(worldIn, placePos))
            worldIn.setBlockState(pos.offset(state.getValue(FACING)), state.withProperty(LEFT, true), 3);
        else {
            if (!worldIn.isRemote && placer instanceof EntityPlayer && !((EntityPlayer) placer).capabilities.isCreativeMode) {
                ((EntityPlayer) placer).inventory.addItemStackToInventory(new ItemStack(this));
            }
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(TIER_2) ? 12 : 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
        if (worldIn.isRemote) {
            return true;
        }

        TileEntityInscriptionTable te = (TileEntityInscriptionTable) worldIn.getTileEntity(pos);
        TileEntityInscriptionTable tealt = te;

        if (te != null) {
            if (!state.getValue(LEFT)) {
                pos = pos.offset(state.getValue(FACING));
                te = (TileEntityInscriptionTable) worldIn.getTileEntity(pos);
            } else {
                tealt = (TileEntityInscriptionTable) worldIn.getTileEntity(pos.offset(state.getValue(FACING).getOpposite()));
            }
        }

        if (te == null || tealt == null)
            return true;

        if (te.isInUse(playerIn)) {
            playerIn.sendMessage(new TextComponentString("Someone else is using this."));
            return true;
        }

        ItemStack curItem = playerIn.getHeldItem(hand);
        if (!curItem.isEmpty() && curItem.getItem() == AMItems.inscription_upgrade) {
            if (te.getUpgradeState() == curItem.getItemDamage()) {
                playerIn.inventory.setInventorySlotContents(playerIn.inventory.currentItem, ItemStack.EMPTY);
                te.incrementUpgradeState();
                tealt.incrementUpgradeState();
                return true;
            }
        }

        playerIn.openGui(ArsMagica.instance, IDDefs.GUI_INSCRIPTION_TABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World par1World, int i) {
        return new TileEntityInscriptionTable();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityInscriptionTable insc = (TileEntityInscriptionTable) world.getTileEntity(pos);

        if (insc == null) return;
        BlockPos placePos = pos.offset(state.getValue(FACING), state.getValue(LEFT) ? -1 : 1);

        if (world.getBlockState(placePos).getBlock() == this && state.getValue(LEFT)) {
            this.breakBlock(world, placePos, state);
            world.setBlockToAir(placePos);
        } else if (world.getBlockState(placePos).getBlock() == this) {
            super.breakBlock(world, placePos, state);
            world.setBlockToAir(placePos);
        }

        if (!world.isRemote && !state.getValue(LEFT)) {
            for (int l = 0; l < insc.getSizeInventory(); l++) {
                ItemStack itemstack = insc.getStackInSlot(l);
                if (itemstack.isEmpty()) {
                    continue;
                }
                this.spawnItemOnBreak(world, pos, itemstack);
            }

            int stat = insc.getUpgradeState();
            for (int m = 0; m < stat; ++m)
                this.spawnItemOnBreak(world, pos, new ItemStack(AMItems.inscription_upgrade, 1, m));
        }

        super.breakBlock(world, pos, state);
    }

    private void spawnItemOnBreak(World world, BlockPos pos, ItemStack itemstack) {
        float f = world.rand.nextFloat() * 0.8F + 0.1F;
        float f1 = world.rand.nextFloat() * 0.8F + 0.1F;
        float f2 = world.rand.nextFloat() * 0.8F + 0.1F;
        do {
            if (itemstack.getCount() <= 0) {
                break;
            }
            int i1 = world.rand.nextInt(21) + 10;
            if (i1 > itemstack.getCount()) {
                i1 = itemstack.getCount();
            }
            itemstack.shrink(i1);
            ItemStack newItem = new ItemStack(itemstack.getItem(), i1, itemstack.getItemDamage());
            EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
            float f3 = 0.05F;
            entityitem.motionX = (float) world.rand.nextGaussian() * f3;
            entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
            entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
            world.spawnEntity(entityitem);
        } while (true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (stateIn.getValue(LEFT) || !stateIn.getValue(TIER_2)) return;

        double px, py, pz;
        py = pos.getY() + 1.55;

        switch (stateIn.getValue(FACING)) {
            case WEST:
                px = pos.getX() + 0.76;
                pz = pos.getZ() + 0.5;
                break;
            case NORTH:
                px = pos.getX() + 0.46;
                pz = pos.getZ() + 0.76;
                break;
            case EAST:
                px = pos.getX() + 0.25;
                pz = pos.getZ() + 0.5;
                break;
            case SOUTH:
                px = pos.getX() + 0.51;
                pz = pos.getZ() + 0.25;
                break;
            default:
                return;
        }

        worldIn.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, px, py, pz, 0.0D, 0.0D, 0.0D);
        worldIn.spawnParticle(EnumParticleTypes.FLAME, px, py, pz, 0.0D, 0.0D, 0.0D);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LEFT, TIER_1, TIER_2, TIER_3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).ordinal() - 2;
        if (state.getValue(LEFT))
            meta |= 0x8;
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.values()[2 + (meta % 4)]).withProperty(LEFT, (meta & 0x8) == 0x8);
    }

    @Override
    public BlockAMContainer registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        // TODO: registry GameRegistry.register(new ItemInscriptionTable(this), rl);
        return this;
    }
}
