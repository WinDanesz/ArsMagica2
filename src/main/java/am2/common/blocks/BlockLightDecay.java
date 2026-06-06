package am2.common.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockLightDecay extends BlockAM {

    public BlockLightDecay() {
        super(Material.AIR);
        setTickRandomly(true);
        setCreativeTab(null);
    }

    @Override
    public int getLightValue(IBlockState state) {
        return 15;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockStateIn, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
        return null; // Invisible light decay block should not be mouse-over targetable
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
        return new AxisAlignedBB(pos.getX() + 0.45F, pos.getY() + 0.45F, pos.getZ() + 0.45F, pos.getX() + 0.55F, pos.getY() + 0.55F, pos.getZ() + 0.55F);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        worldIn.setBlockToAir(pos);
    }

    @Override
    public BlockAM registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.toString());
        // TODO: registry GameRegistry.register(this, rl);
        return this;
    }
}
