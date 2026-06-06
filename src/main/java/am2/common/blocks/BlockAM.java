package am2.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAM extends Block {

    public BlockAM(Material material) {
        super(material);
    }

    public BlockAM(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    public BlockAM registerAndName(ResourceLocation rl) {
        //this.setTranslationKey(rl.toString());
        // todo registry
        //// TODO: registry GameRegistry.register(this, rl);
        //// TODO: registry GameRegistry.register(new ItemBlockSubtypes(this), rl);
        return this;
    }

    // TODO this is prob not necessary
    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, getMetaFromState(state));
    }

    protected AxisAlignedBB blockAABB = FULL_BLOCK_AABB;

    public void setBoundingBox(float xStart, float yStart, float zStart, float xEnd, float yEnd, float zEnd) {
        setBoundingBox(new AxisAlignedBB(xStart, yStart, zStart, xEnd, yEnd, zEnd));
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.blockAABB = boundingBox;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return blockAABB;
    }

}
