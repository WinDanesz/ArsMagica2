package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityDrainingWell;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockDrainingWell extends BlockAMPowered {

    public BlockDrainingWell() {
        super(Material.IRON);
        defaultRender = true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDrainingWell();
    }

}
