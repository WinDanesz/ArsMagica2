package am2.common.world;

import am2.common.registry.AMBlocks;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class WorldGenFallenLog extends WorldGenerator {
    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        BlockPos surface = world.getHeight(position);
        BlockPos ground = surface.down();

        while (ground.getY() > 0) {
            IBlockState gs = world.getBlockState(ground);
            Material mat = gs.getMaterial();
            if (mat == Material.GROUND || mat == Material.GRASS || mat == Material.ROCK
                    || mat == Material.SAND || mat == Material.CLAY) {
                surface = ground.up();
                break;
            }
            ground = ground.down();
            surface = ground.up();
        }
        if (ground.getY() <= 0) return false;

        int length = 4 + rand.nextInt(4);
        EnumFacing.Axis axis = rand.nextBoolean() ? EnumFacing.Axis.X : EnumFacing.Axis.Z;
        EnumFacing direction = axis == EnumFacing.Axis.X ? EnumFacing.EAST : EnumFacing.SOUTH;
        BlockLog.EnumAxis logAxis = axis == EnumFacing.Axis.X ? BlockLog.EnumAxis.X : BlockLog.EnumAxis.Z;
        
        IBlockState logState = AMBlocks.witchwood_log.getDefaultState().withProperty(BlockLog.LOG_AXIS, logAxis);

        for (int i = 0; i < length; i++) {
            BlockPos current = surface.offset(direction, i);
            if (world.isAirBlock(current) || world.getBlockState(current).getBlock().isReplaceable(world, current)) {
                BlockPos below = current.down();
                if (!world.getBlockState(below).isOpaqueCube()) {
                    current = current.down();
                } else if (world.getBlockState(current).isOpaqueCube()) {
                    current = current.up();
                }
                
                world.setBlockState(current, logState, 2);
                
                if (rand.nextInt(4) == 0 && world.isAirBlock(current.up())) {
                    world.setBlockState(current.up(), rand.nextBoolean() ? Blocks.BROWN_MUSHROOM.getDefaultState() : Blocks.RED_MUSHROOM.getDefaultState(), 2);
                }
            }
        }
        return true;
    }
}
