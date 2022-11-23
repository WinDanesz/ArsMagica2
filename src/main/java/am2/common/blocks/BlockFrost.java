package am2.common.blocks;

import java.util.Random;

import net.minecraft.block.BlockIce;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockFrost extends BlockIce {
	
	public BlockFrost() {
		super();
		setTickRandomly(true);
		setHardness(0.5F);
		setSoundType(SoundType.GLASS);
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		worldIn.setBlockToAir(pos);
	}

	public BlockFrost registerAndName(ResourceLocation rl) {
		this.setTranslationKey(rl.toString());
		// TODO: registry GameRegistry.register(this, rl);
		// TODO: registry GameRegistry.register(new ItemBlock(this), rl);
		return this;
	}
}
