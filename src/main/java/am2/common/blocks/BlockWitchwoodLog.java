package am2.common.blocks;

import am2.common.registry.AMTabs;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockWitchwoodLog extends BlockLog {

    public BlockWitchwoodLog() {
        super();
        setHardness(3.0f);
        setResistance(3.0f);
        setHarvestLevel("axe", 2);
        setCreativeTab(AMTabs.AMBLOCKS);
        setDefaultState(blockState.getBaseState().withProperty(LOG_AXIS, EnumAxis.Y));
    }

    /**
     * Returns the quantity of items to drop on block destruction.
     */
    public int quantityDropped(Random par1Random) {
        return 1;
    }

    /**
     * returns a number between 0 and 3
     */
    public static int limitToValidMetadata(int par0) {
        return par0 & 3;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LOG_AXIS);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LOG_AXIS).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LOG_AXIS, EnumAxis.values()[MathHelper.clamp(meta, 0, 3)]);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5; // same as vanilla logs
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5; // same as vanilla logs
    }

    public BlockWitchwoodLog registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        // TODO: registry GameRegistry.register(new ItemBlockSubtypes(this), rl);
        return this;
    }
}
