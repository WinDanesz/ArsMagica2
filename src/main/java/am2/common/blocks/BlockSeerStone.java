package am2.common.blocks;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.common.blocks.tileentity.TileEntitySeerStone;
import am2.common.defs.IDDefs;
import am2.common.utils.KeystoneUtilities;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSeerStone extends BlockAMPowered {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.125, 0.0, 0.125, 0.875, 0.3125, 0.875);
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.125, 0.6875, 0.125, 0.875, 1.0, 0.875);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.125, 0.125, 0.0, 0.875, 0.875, 0.3125);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.125, 0.125, 0.6875, 0.875, 0.875, 1.0);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0, 0.125, 0.125, 0.3125, 0.875, 0.875);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(0.6875, 0.125, 0.125, 1.0, 0.875, 0.875);

    public BlockSeerStone() {
        super(Material.GLASS);
        setHardness(2.0f);
        setResistance(2.0f);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.defaultRender = true;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).ordinal();
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.values()[meta]);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case UP:
                return AABB_UP;
            case DOWN:
                return AABB_DOWN;
            case SOUTH:
                return AABB_SOUTH;
            case NORTH:
                return AABB_NORTH;
            case EAST:
                return AABB_EAST;
            case WEST:
                return AABB_WEST;
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        return worldIn.getBlockState(pos.offset(side.getOpposite())).isSideSolid(worldIn, pos, side);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(meta).withProperty(FACING, facing);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);

        TileEntity te = worldIn.getTileEntity(pos);
        TileEntitySeerStone sste = null;
        if (te != null && te instanceof TileEntitySeerStone) {
            sste = (TileEntitySeerStone) te;
        } else {
            return true;
        }

        if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, sste)) {
            return true;
        }

        if (!KeystoneUtilities.instance.canPlayerAccess(sste, playerIn, KeystoneAccessType.USE)) {
            return true;
        }

        if (playerIn.isSneaking()) {
            sste.invertDetection();
            if (worldIn.isRemote) {
                playerIn.sendMessage(new TextComponentString("Inverting detection mode: " + ((TileEntitySeerStone) te).isInvertingDetection()));
            }
            return true;
        }

        if (HandleSpecialItems(worldIn, playerIn, pos)) {
            return true;
        }
        if (!worldIn.isRemote)
            playerIn.openGui(ArsMagica.instance, IDDefs.GUI_SEER_STONE, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        IKeystoneLockable<?> lockable = (IKeystoneLockable<?>) world.getTileEntity(pos);
        if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int i) {
        return new TileEntitySeerStone();
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity myTE = blockAccess.getTileEntity(pos);
        if (myTE == null || !(myTE instanceof TileEntitySeerStone))
            return 0;
        return ((TileEntitySeerStone) myTE).HasSight() ? 15 : 0;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity myTE = blockAccess.getTileEntity(pos);
        if (myTE == null || !(myTE instanceof TileEntitySeerStone))
            return 0;
        return ((TileEntitySeerStone) myTE).HasSight() ? 15 : 0;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            super.breakBlock(world, pos, state);
            return;
        }
        TileEntitySeerStone myTE = (TileEntitySeerStone) world.getTileEntity(pos);
        if (myTE == null) return;
        for (int l = 0; l < myTE.getSizeInventory() - 3; l++) {
            ItemStack itemstack = myTE.getStackInSlot(l);
            if (itemstack.isEmpty()) {
                continue;
            }
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
                newItem.setTagCompound(itemstack.getTagCompound());
                EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1, pos.getZ() + f2, newItem);
                float f3 = 0.05F;
                entityitem.motionX = (float) world.rand.nextGaussian() * f3;
                entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
                entityitem.motionZ = (float) world.rand.nextGaussian() * f3;
                world.spawnEntity(entityitem);
            } while (true);
        }
        super.breakBlock(world, pos, state);
    }
}
