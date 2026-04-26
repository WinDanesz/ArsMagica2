package am2.common.blocks;

import am2.common.entity.EntitySpellProjectile;
import am2.common.registry.AMTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMagicWall extends BlockAM {

    public BlockMagicWall() {
        super(Material.BARRIER);
        setCreativeTab(AMTabs.AMBLOCKS);
        setHardness(3.0F);
        setResistance(5.0F);
        setHarvestLevel("pickaxe", -1);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
        if (entityIn instanceof EntitySpellProjectile) {
            EntitySpellProjectile projectile = (EntitySpellProjectile) entityIn;
            projectile.setBounces(projectile.getBounces() + 1);
        }
    }


    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
