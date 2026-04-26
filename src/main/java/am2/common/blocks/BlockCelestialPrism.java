package am2.common.blocks;

import am2.common.blocks.tileentity.TileEntityCelestialPrism;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

public class BlockCelestialPrism extends BlockEssenceGenerator {

    public BlockCelestialPrism() {
        super(Material.CLOTH);
        setLightLevel(0.8f);
        setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 2f, 1.0f);
        defaultRender = false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCelestialPrism();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
}
