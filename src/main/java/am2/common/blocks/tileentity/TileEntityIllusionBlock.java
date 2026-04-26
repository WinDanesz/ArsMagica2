package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.common.blocks.BlockIllusionBlock;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMPotions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityIllusionBlock extends TileEntity implements ITickable {

    private IBlockState mimicBlock;

    public TileEntityIllusionBlock() {

    }

    @SideOnly(Side.CLIENT)
    public boolean isRevealed(IBlockState state) {
        return BlockIllusionBlock.getIllusionType(state).canBeRevealed() && ArsMagica.proxy.getLocalPlayer().isPotionActive(AMPotions.true_sight);
    }

    @Override
    public void update() {
        BlockPos pos = this.pos.down();
        IBlockState blockBellow = world.getBlockState(pos);
        while (blockBellow.getBlock() == AMBlocks.illusion_block) {
            pos = pos.down();
            blockBellow = world.getBlockState(pos);
        }
        mimicBlock = blockBellow;
    }

    public IBlockState getMimicBlock() {
        return mimicBlock;
    }
}
