package am2.common.blocks;

import am2.common.registry.AMTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockWitchwoodPlanks extends BlockAM {

    public BlockWitchwoodPlanks() {
        super(Material.WOOD);
        this.setHardness(2.0f);
        this.setResistance(2.0f);
        this.setSoundType(SoundType.WOOD);
        this.setHarvestLevel("axe", 2);
        this.setCreativeTab(AMTabs.AMBLOCKS);
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }
}
