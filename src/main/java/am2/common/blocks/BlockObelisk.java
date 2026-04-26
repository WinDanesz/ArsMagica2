package am2.common.blocks;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityObelisk;
import am2.common.defs.IDDefs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The standard Obelisk - burns fuel to generate neutral etherium.
 * Only emits light when actively burning fuel or fully charged.
 */

public class BlockObelisk extends BlockEssenceGenerator {

    private static final AxisAlignedBB OBELISK_BASE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 5.0 / 16.0, 1.0);
    private static final AxisAlignedBB OBELISK_COLUMN = new AxisAlignedBB(4.0 / 16.0, 5.0 / 16.0, 4.0 / 16.0, 12.0 / 16.0, 2.0, 12.0 / 16.0);

    private static final int ACTIVE_LIGHT_VALUE = 11; // ~0.73f

    public BlockObelisk() {
        super(Material.CLOTH);
        setBlockBounds(0f, 0.0f, 0f, 1f, 2f, 1f);
        defaultRender = false;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityObelisk) {
            TileEntityObelisk obelisk = (TileEntityObelisk) te;
            if (obelisk.burnTimeRemaining > 0 || obelisk.fullyCharged) {
                return ACTIVE_LIGHT_VALUE;
            }
        }
        return 0;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityObelisk();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (HandleSpecialItems(worldIn, playerIn, pos))
            return true;
        playerIn.openGui(ArsMagica.instance, IDDefs.GUI_OBELISK, worldIn, pos.getX(), pos.getY(), pos.getZ());
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, OBELISK_BASE);
        addCollisionBoxToList(pos, entityBox, collidingBoxes, OBELISK_COLUMN);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
