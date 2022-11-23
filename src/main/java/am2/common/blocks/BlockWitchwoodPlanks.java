package am2.common.blocks;

import java.util.List;

import am2.common.defs.CreativeTabsDefs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockWitchwoodPlanks extends BlockAM{

	public BlockWitchwoodPlanks(){
		super(Material.WOOD);
		this.setHardness(2.0f);
		this.setResistance(2.0f);
		this.setSoundType(SoundType.WOOD);
		this.setHarvestLevel("axe", 2);
		this.setCreativeTab(CreativeTabsDefs.tabAM2Blocks);
	}
	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return 0;
	}
}
