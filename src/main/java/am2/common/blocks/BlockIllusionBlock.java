package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityIllusionBlock;
import am2.common.registry.AMPotions;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.List;

public class BlockIllusionBlock extends BlockAMContainer {

    public static final PropertyEnum<EnumIllusionType> ILLUSION_TYPE = PropertyEnum.create("illusion_type", EnumIllusionType.class);

    /**
     * Unlisted property: carries the mimic block state into the baked model during chunk rebuild.
     * Never serialized — only exists in the IExtendedBlockState passed to IBakedModel.getQuads().
     */
    public static final IUnlistedProperty<IBlockState> MIMIC_BLOCK = new IUnlistedProperty<IBlockState>() {
        @Override public String getName()                   { return "mimic_block"; }
        @Override public boolean isValid(IBlockState value) { return true; }
        @Override public Class<IBlockState> getType()       { return IBlockState.class; }
        @Override public String valueToString(IBlockState v){ return v.toString(); }
    };

    public BlockIllusionBlock() {
        super(Material.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ILLUSION_TYPE, EnumIllusionType.DEFAULT));
        this.setLightOpacity(255);

        this.setHardness(3.0f);
        this.setResistance(3.0f);

    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new net.minecraft.block.properties.IProperty[]{ILLUSION_TYPE},
                new IUnlistedProperty[]{MIMIC_BLOCK});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!(state instanceof IExtendedBlockState)) return state;
        IExtendedBlockState extended = (IExtendedBlockState) state;
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityIllusionBlock) {
            TileEntityIllusionBlock illusion = (TileEntityIllusionBlock) te;
            boolean revealed = getIllusionType(state).canBeRevealed()
                    && TileEntityIllusionBlock.trueSightActive;
            if (!revealed) {
                IBlockState mimic = illusion.getMimicBlock();
                if (mimic != null && mimic.getBlock() != Blocks.AIR) {
                    extended = extended.withProperty(MIMIC_BLOCK, mimic);
                }
            }
        }
        return extended;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ILLUSION_TYPE).ordinal();
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        // Check adjacent block on this side
        BlockPos adjacentPos = pos.offset(side);
        IBlockState adjacentState = blockAccess.getBlockState(adjacentPos);

        // If adjacent is also an illusion block, check if we should hide the face
        if (adjacentState.getBlock() == this) {
            TileEntityIllusionBlock thisTe = (TileEntityIllusionBlock) blockAccess.getTileEntity(pos);
            TileEntityIllusionBlock adjacentTe = (TileEntityIllusionBlock) blockAccess.getTileEntity(adjacentPos);

            if (thisTe != null && adjacentTe != null) {
                boolean thisIsTransparent = thisTe.getMimicBlock() == null || thisTe.getMimicBlock() == Blocks.AIR.getDefaultState();
                boolean adjacentIsTransparent = adjacentTe.getMimicBlock() == null || adjacentTe.getMimicBlock() == Blocks.AIR.getDefaultState();

                // Only hide the face if both are transparent (not mimicking)
                if (thisIsTransparent && adjacentIsTransparent) {
                    // Check true sight - isRevealed returns true when player HAS true sight
                    boolean isRevealed = thisTe.isRevealed(blockState);
                    if (!isRevealed) {
                        return false; // Hide the connecting face
                    }
                }
            }
        }

        // Check if mimicking a block
        TileEntityIllusionBlock thisTe = (TileEntityIllusionBlock) blockAccess.getTileEntity(pos);
        if (thisTe != null && thisTe.getMimicBlock() != null && thisTe.getMimicBlock() != Blocks.AIR.getDefaultState()) {
            return thisTe.getMimicBlock().shouldSideBeRendered(blockAccess, pos, side);
        }

        // Default: render the face
        return true;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumIllusionType[] values = EnumIllusionType.values();
        if (meta < 0 || meta >= values.length) meta = 0;
        return this.getDefaultState().withProperty(ILLUSION_TYPE, values[meta]);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityIllusionBlock();
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntityIllusionBlock) {
            ((TileEntityIllusionBlock) te).scanMimicBlock();
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTranslucent(IBlockState state) {
        // Allows faces between two adjacent no-mimic illusion blocks to be culled correctly
        return true;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, net.minecraft.util.BlockRenderLayer layer) {
        // Allow rendering in all layers so transparent/cutout mimics (glass, leaves) work correctly
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes,
                                      @Nullable Entity entityIn, boolean isActualState) {
        if (entityIn instanceof EntityLivingBase && ((EntityLivingBase) entityIn).isPotionActive(AMPotions.true_sight))
            return;
        if (state.getBlock() instanceof BlockIllusionBlock && getIllusionType(state).isSolid())
            addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getCollisionBoundingBox(worldIn, pos));
    }

    public static EnumIllusionType getIllusionType(IBlockState state) {
        return state.getValue(ILLUSION_TYPE);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
    }

    @Override
    public BlockAMContainer registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        // TODO: registry GameRegistry.register(new ItemBlockIllusion(this), rl);
        return this;
    }

    public enum EnumIllusionType implements IStringSerializable {
        DEFAULT(true, true),
        NON_COLLIDE(false, false);

        private final boolean isSolid;
        private final boolean canBeRevealed;

        EnumIllusionType(boolean isSolid, boolean canBeRevealed) {
            this.isSolid = isSolid;
            this.canBeRevealed = canBeRevealed;
        }

        public boolean isSolid() {
            return this.isSolid;
        }

        public boolean canBeRevealed() {
            return this.canBeRevealed;
        }

        @Override
        public String getName() {
            return this.name().toLowerCase();
        }

    }

}
