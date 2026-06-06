package am2.common.blocks;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityEverstone;
import am2.common.registry.AMItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEverstone extends BlockAMPowered {

    public static final PropertyBool IS_SOLID = PropertyBool.create("is_solid");
    public static final PropertyBool MIMIC = PropertyBool.create("mimic");

    public BlockEverstone() {
        super(Material.ROCK);
        setHardness(3.0f);
        setResistance(3.0f);
        setDefaultState(blockState.getBaseState().withProperty(MIMIC, false).withProperty(IS_SOLID, true));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MIMIC, IS_SOLID);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        // bye bye TESR
        TileEntityEverstone te = getTileEntity(worldIn, pos);
        return te.isSolid() && te.getMimicState() != null ? te.getMimicState() : state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityEverstone();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
                                   boolean willHarvest) {
        TileEntityEverstone te = getTileEntity(world, pos);
        if (te == null) {
            if (player.capabilities.isCreativeMode) {
                world.setTileEntity(pos, null);
                world.setBlockToAir(pos);
                return true;
            }
            return false;
        }
        if (player.capabilities.isCreativeMode) {
            world.setTileEntity(pos, null);
            world.setBlockToAir(pos);
            return true;
        }
        te.onBreak();
        return false;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return new ArrayList<>();
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        TileEntityEverstone te = getTileEntity(world, pos);
        if (te != null) {
            te.onBreak();
        }
        return 10000f;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.getHeldItemMainhand() != null) {
            IBlockState block = null;
            TileEntityEverstone te = getTileEntity(world, pos);
            if (te == null) return false;

            if (player.getHeldItemMainhand().getItem() == AMItems.crystal_wrench) {
                if (!world.isRemote) {
                    if (te.getMimicState() != null) {
                        te.setMimicState(null);
                        return true;
                    } else {
                        world.setBlockToAir(pos);
                        this.dropBlockAsItem(world, pos, state, 0);
                        return true;
                    }
                }

            } else if (player.getHeldItemMainhand().getItem() == Item.getItemFromBlock(this)) {
                return true;
            } else if (player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
                ItemBlock itemblock = (ItemBlock) player.getHeldItemMainhand().getItem();
                block = itemblock.getBlock().getStateFromMeta(player.getHeldItemMainhand().getItemDamage());
            }
            if (te.getMimicState() == null && block != null) {
                te.setMimicState(block);
                //world.notifyBlockOfStateChange(pos, this); todo
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        if (state.getValue(IS_SOLID))
            return EnumBlockRenderType.MODEL;
        return EnumBlockRenderType.INVISIBLE;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        TileEntityEverstone everstone = getTileEntity(worldIn, pos);
        if (everstone == null || everstone.isSolid())
            return super.getCollisionBoundingBox(blockState, worldIn, pos);
        return NULL_AABB;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        TileEntityEverstone everstone = getTileEntity(worldIn, pos);
        if (everstone == null || everstone.isSolid())
            return super.getSelectedBoundingBox(state, worldIn, pos);
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockStateIn, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        TileEntityEverstone everstone = getTileEntity(worldIn, pos);
        if (everstone != null && !everstone.isSolid()) {
            return null;
        }
        return super.collisionRayTrace(blockStateIn, worldIn, pos, start, end);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileEntityEverstone everstone = getTileEntity(source, pos);
        if (everstone == null || everstone.isSolid())
            return super.getBoundingBox(state, source, pos);
        return FULL_BLOCK_AABB;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntityEverstone everstone = getTileEntity(world, pos);
        if (everstone == null) return true;
        return everstone.isSolid();
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        TileEntityEverstone te = getTileEntity(worldIn, pos);
        if (te == null) return this.blockHardness;
        IBlockState block = te.getMimicState();
        if (block == null || block == this) return this.blockHardness;
        return block.getBlockHardness(worldIn, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        TileEntityEverstone te = getTileEntity(world, pos);
        for (int i = 0; i < 5 * ArsMagica.config.getGFXLevel(); ++i) {
            IBlockState block;
            if (te == null || te.getMimicState() == null) {
                block = this.getDefaultState();
            } else {
                block = te.getMimicState();
                if (block == null) block = this.getDefaultState();
            }

            manager.addEffect(new ParticleDigging.Factory().createParticle(
                    0, world,
                    pos.getX() + world.rand.nextDouble(),
                    pos.getY() + world.rand.nextDouble(),
                    pos.getZ() + world.rand.nextDouble(),
                    0, 0, 0, Block.getStateId(block))
            );
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
        TileEntityEverstone te = getTileEntity(world, target.getBlockPos());
        IBlockState block;
        if (te == null || te.getMimicState() == null) {
            block = this.getDefaultState();
        }
        else {
            block = te.getMimicState();
            if (block == null) block = this.getDefaultState();
        }

        manager.addEffect(new ParticleDigging.Factory().createParticle(0, world,
                target.getBlockPos().getX() + world.rand.nextDouble(),
                target.getBlockPos().getY() + world.rand.nextDouble(),
                target.getBlockPos().getZ() + world.rand.nextDouble(), 0, 0, 0, Block.getStateId(block)));
        return true;
    }


    public static TileEntityEverstone getTileEntity(IBlockAccess world, BlockPos pos) {
        if (world == null)
            return null;

        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityEverstone))
            return null;

        return (TileEntityEverstone) te;
    }

}
