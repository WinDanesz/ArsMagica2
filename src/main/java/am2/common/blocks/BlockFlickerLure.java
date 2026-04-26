package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityFlickerLure;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockFlickerLure extends BlockAMPowered {

    public BlockFlickerLure() {
        super(Material.ROCK);
        setHardness(2.0f);
        setResistance(2.0f);
        defaultRender = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityFlickerLure();
    }
}
