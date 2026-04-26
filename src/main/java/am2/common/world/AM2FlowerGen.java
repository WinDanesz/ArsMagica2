package am2.common.world;

import am2.common.blocks.BlockAMFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class AM2FlowerGen extends WorldGenerator {

    private IBlockState plantBlock;
    private int clusterSize;

    public AM2FlowerGen(BlockAMFlower block) {
        this(block, 48);
    }

    public AM2FlowerGen(BlockAMFlower block, int clusterSize) {
        this.plantBlock = block.getDefaultState();
        this.clusterSize = clusterSize;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int i = 0; i < clusterSize; ++i) {
            BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(blockpos) && (!worldIn.provider.hasSkyLight() || blockpos.getY() < 255) && ((BlockAMFlower) this.plantBlock.getBlock()).canBlockStay(worldIn, blockpos, this.plantBlock)) {
                worldIn.setBlockState(blockpos, this.plantBlock, 2);
            }
        }

        return true;
    }
}
