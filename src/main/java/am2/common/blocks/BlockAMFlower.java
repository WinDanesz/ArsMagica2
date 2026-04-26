package am2.common.blocks;

import am2.common.registry.AMTabs;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAMFlower extends BlockBush {

    public BlockAMFlower() {
        super();
        setSoundType(SoundType.PLANT);
        setCreativeTab(AMTabs.AMBLOCKS);
    }

    public BlockAMFlower registerAndName(ResourceLocation loc) {
        setTranslationKey(loc.toString());
        // TODO: registry GameRegistry.register(this, loc);
        // TODO: registry GameRegistry.register(new ItemBlock(this), loc);
        return this;
    }

    public boolean canGrowOn(World worldIn, BlockPos pos) {
        return canBlockStay(worldIn, pos, worldIn.getBlockState(pos));
    }

}
