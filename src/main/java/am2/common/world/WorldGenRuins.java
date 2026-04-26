package am2.common.world;

import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.Random;

public class WorldGenRuins extends WorldGenerator {

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        int numColumns = 2 + rand.nextInt(4);
        boolean generated = false;

        for (int i = 0; i < numColumns; i++) {
            int dx = rand.nextInt(7) - 3;
            int dz = rand.nextInt(7) - 3;
            
            BlockPos surface = world.getHeight(position.add(dx, 0, dz));
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
            if (ground.getY() <= 0) continue;

            int height = 2 + rand.nextInt(4);
            for (int y = 0; y < height; y++) {
                world.setBlockState(surface.up(y), getRandomRuinBlock(rand), 2);
            }
            
            // Sometimes add adjacent blocks on the ground as fallen rubble
            if (rand.nextBoolean()) {
                world.setBlockState(surface.north(), getRandomRuinBlock(rand), 2);
            }
            if (rand.nextBoolean()) {
                world.setBlockState(surface.east(), getRandomRuinBlock(rand), 2);
            }
            if (rand.nextBoolean()) {
                world.setBlockState(surface.south(), getRandomRuinBlock(rand), 2);
            }
            if (rand.nextBoolean()) {
                world.setBlockState(surface.west(), getRandomRuinBlock(rand), 2);
            }
            
            generated = true;
            
            // 10% chance to add a buried chest per column
            if (rand.nextFloat() < 0.10f) {
                BlockPos chestPos = ground.down(1 + rand.nextInt(2));
                if (chestPos.getY() > 0) {
                    world.setBlockState(chestPos, Blocks.CHEST.getDefaultState(), 2);
                    TileEntity tileentity = world.getTileEntity(chestPos);
                    if (tileentity instanceof TileEntityChest) {
                        ((TileEntityChest) tileentity).setLootTable(LootTableList.CHESTS_SIMPLE_DUNGEON, rand.nextLong());
                    }
                }
            }
        }

        return generated;
    }
    
    private IBlockState getRandomRuinBlock(Random rand) {
        int val = rand.nextInt(5);
        switch (val) {
            case 0:
                return Blocks.COBBLESTONE.getDefaultState();
            case 1:
                return Blocks.MOSSY_COBBLESTONE.getDefaultState();
            case 2:
                return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
            case 3:
                return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
            case 4:
            default:
                return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT);
        }
    }
}
