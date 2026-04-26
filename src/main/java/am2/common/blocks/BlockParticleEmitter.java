package am2.common.blocks;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityParticleEmitter;
import am2.common.items.ItemCrystalWrench;
import am2.common.registry.AMItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockParticleEmitter extends BlockAMContainer {

    public static final PropertyEnum<EnumFacing> FACING = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);
    public static final PropertyBool HIDDEN = PropertyBool.create("hidden");


    public BlockParticleEmitter() {
        super(Material.GLASS);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(HIDDEN, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HIDDEN);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(HIDDEN)) meta |= 0x8;
        return meta;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 0x3)).withProperty(HIDDEN, (meta & 0x8) == 0x8);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        TileEntityParticleEmitter tile = (TileEntityParticleEmitter) world.getTileEntity(pos);
        if (tile != null && !tile.getShow())
            return false;

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        TileEntity te = world.getTileEntity(pos);
        TileEntityParticleEmitter te2 = null;
        if (te instanceof TileEntityParticleEmitter)
            te2 = (TileEntityParticleEmitter) te;
        if (te2 == null)
            super.onBlockExploded(world, pos, explosion);
        if (te2 != null && te2.getShow())
            super.onBlockExploded(world, pos, explosion);
    }


    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityParticleEmitter();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockStateIn, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        if (blockStateIn.getValue(HIDDEN)) {
            return null;
        }
        return super.collisionRayTrace(blockStateIn, worldIn, pos, start, end);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return state.getValue(HIDDEN) ? FULL_BLOCK_AABB : super.getBoundingBox(state, source, pos);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return blockState.getValue(HIDDEN) ? null : super.getCollisionBoundingBox(blockState, worldIn, pos);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            ItemStack heldItem = playerIn.getHeldItem(hand);
            TileEntity te = worldIn.getTileEntity(pos);
            if (te != null && te instanceof TileEntityParticleEmitter) {
                if (heldItem != null
                        && heldItem.getItem() == AMItems.crystal_wrench) {
                    if (ItemCrystalWrench.getMode(heldItem) == 0) {
                        ArsMagica.proxy.openParticleBlockGUI(worldIn, playerIn, (TileEntityParticleEmitter) te);
                    } else {
                        if (ArsMagica.proxy.cwCopyLoc == null) {
                            playerIn.sendMessage(new TextComponentString("Settings Copied."));
                            ArsMagica.proxy.cwCopyLoc = new NBTTagCompound();
                            ((TileEntityParticleEmitter) te).writeSettingsToNBT(ArsMagica.proxy.cwCopyLoc);
                        } else {
                            playerIn.sendMessage(new TextComponentString("Settings Applied."));
                            ((TileEntityParticleEmitter) te).readSettingsFromNBT(ArsMagica.proxy.cwCopyLoc);
                            ((TileEntityParticleEmitter) te).syncWithServer();
                            ArsMagica.proxy.cwCopyLoc = null;
                        }
                    }
                    return true;
                } else {
                    ArsMagica.proxy.openParticleBlockGUI(worldIn, playerIn, (TileEntityParticleEmitter) te);
                    return true;
                }
            }
        }
        return true;
    }
}
