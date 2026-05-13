package am2.common.blocks.tileentity;

import am2.common.blocks.BlockIllusionBlock;
import am2.common.registry.AMBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TileEntityIllusionBlock extends TileEntity {

    /**
     * All loaded illusion block TEs on the client.
     * Used by ClientTickHandler to invalidate chunks when true sight changes.
     */
    public static final Set<TileEntityIllusionBlock> CLIENT_INSTANCES =
            Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Cached true-sight potion state, updated each tick by ClientTickHandler.
     * volatile so chunk-rebuild background threads see fresh values.
     */
    public static volatile boolean trueSightActive = false;

    private volatile IBlockState mimicBlock;

    public TileEntityIllusionBlock() {
    }

    @Override
    public void onLoad() {
        super.onLoad();
        scanMimicBlock();
        if (world != null && world.isRemote) {
            CLIENT_INSTANCES.add(this);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        CLIENT_INSTANCES.remove(this);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        CLIENT_INSTANCES.remove(this);
    }

    /**
     * Scans downward to find the first non-illusion block and caches it as the mimic.
     * Called once on load and whenever a neighbor changes — never every tick.
     */
    public void scanMimicBlock() {
        BlockPos scanPos = this.pos.down();
        IBlockState blockBelow = world.getBlockState(scanPos);
        while (blockBelow.getBlock() == AMBlocks.illusion_block) {
            scanPos = scanPos.down();
            blockBelow = world.getBlockState(scanPos);
        }
        IBlockState newMimic = blockBelow;
        if (newMimic != mimicBlock) {
            mimicBlock = newMimic;
            if (world.isRemote) {
                world.markBlockRangeForRenderUpdate(pos, pos);
            }
            // Propagate upward so stacked illusion blocks also update their mimic
            IBlockState above = world.getBlockState(pos.up());
            if (above.getBlock() == AMBlocks.illusion_block) {
                above.getBlock().neighborChanged(above, world, pos.up(), AMBlocks.illusion_block, pos);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean isRevealed(IBlockState state) {
        return BlockIllusionBlock.getIllusionType(state).canBeRevealed() && trueSightActive;
    }

    public IBlockState getMimicBlock() {
        return mimicBlock;
    }
}
