package am2.common.blocks;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.common.blocks.tileentity.TileEntityKeystoneReceptacle;
import am2.common.defs.IDDefs;
import am2.common.items.ItemKeystone;
import am2.common.utils.KeystoneUtilities;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class BlockKeystoneReceptacle extends BlockAMPowered {

    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);

    public BlockKeystoneReceptacle() {
        super(Material.ROCK);
        setHardness(4.5f);
        setResistance(10f);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.defaultRender = true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int i) {
        return new TileEntityKeystoneReceptacle();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);

        if (HandleSpecialItems(worldIn, playerIn, pos)) {
            return true;
        }


        TileEntity myTE = worldIn.getTileEntity(pos);
        if (myTE == null || !(myTE instanceof TileEntityKeystoneReceptacle)) {
            return true;
        }
        TileEntityKeystoneReceptacle receptacle = (TileEntityKeystoneReceptacle) myTE;

        if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, receptacle)) {
            return true;
        }


        if (playerIn.isSneaking()) {
            if (!worldIn.isRemote && KeystoneUtilities.instance.canPlayerAccess(receptacle, playerIn, KeystoneAccessType.USE)) {
                playerIn.openGui(ArsMagica.instance, IDDefs.GUI_KEYSTONE_LOCKABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        } else {
            if (receptacle.canActivate()) {
                long key = 0;
                ItemStack rightClickItem = playerIn.getHeldItemMainhand();
                if (!rightClickItem.isEmpty() && rightClickItem.getItem() instanceof ItemKeystone) {
                    key = ((ItemKeystone) rightClickItem.getItem()).getKey(rightClickItem);
                }
                receptacle.setActive(key);
            } else if (receptacle.isActive()) {
                receptacle.deactivate();
            }
        }

        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
                                ItemStack stack) {

        ArsMagica.proxy.blocks.registerKeystonePortal(pos, worldIn.provider.getDimension());


        TileEntityKeystoneReceptacle receptacle = (TileEntityKeystoneReceptacle) worldIn.getTileEntity(pos);
        receptacle.onPlaced();

        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        IKeystoneLockable<?> lockable = (IKeystoneLockable<?>) world.getTileEntity(pos);
        if (KeystoneUtilities.instance.getKeyFromRunes(lockable.getRunesInKey()) != 0) {
            if (!world.isRemote)
                player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.clearKey")));
            return false;
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
}
