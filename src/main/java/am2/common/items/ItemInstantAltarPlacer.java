package am2.common.items;

import am2.common.blocks.BlockLectern;
import am2.common.registry.AMBlocks;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLever.EnumOrientation;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Creative-only testing aid: builds a complete, working Spell Crafting Altar (witchwood, sunstone caps)
 * in front of the player. The layout mirrors the primary structure of {@code TileEntityCraftingAltar}.
 */
public class ItemInstantAltarPlacer extends Item {

    private static final int DISTANCE = 4;

    public ItemInstantAltarPlacer() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("§c" + I18n.format("am2.tooltip.creativeOnly"));
        tooltip.add("§7" + I18n.format("am2.tooltip.instantAltarPlacer"));
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.capabilities.isCreativeMode) return EnumActionResult.PASS;
        if (world.isRemote) return EnumActionResult.SUCCESS;

        // The floor rests on the clicked block, centered a few blocks ahead of the player so nobody is walled in.
        BlockPos altarPos = new BlockPos(player.posX, pos.getY() + 5, player.posZ).offset(player.getHorizontalFacing(), DISTANCE);
        build(world, altarPos);
        player.sendStatusMessage(new TextComponentTranslation("am2.tooltip.instantAltarPlaced"), true);
        return EnumActionResult.SUCCESS;
    }

    private static void build(World world, BlockPos origin) {
        IBlockState planks = AMBlocks.witchwood_planks.getDefaultState();
        IBlockState cap = AMBlocks.sunstone_block.getDefaultState();

        // Clear the footprint first, so leftover terrain can't break the structure
        for (int x = -2; x <= 2; x++)
            for (int y = -4; y <= 0; y++)
                for (int z = -2; z <= 2; z++)
                    world.setBlockState(origin.add(x, y, z), Blocks.AIR.getDefaultState(), 2);

        // Floor (the center is a cap), then the pillar bases
        for (int x = -2; x <= 2; x++)
            for (int z = -2; z <= 2; z++)
                set(world, origin, x, -4, z, x == 0 && z == 0 ? cap : planks);
        for (int x = -1; x <= 1; x += 2)
            for (int z = -2; z <= 2; z += 4) {
                for (int y = -3; y <= -1; y++) set(world, origin, x, y, z, planks);
                set(world, origin, x, 0, z, cap);
            }
        set(world, origin, 0, 0, -1, planks);
        set(world, origin, 0, 0, 1, planks);

        // Stairs
        set(world, origin, -1, 0, -1, stairs(EnumFacing.EAST, false));
        set(world, origin, -1, 0, 0, stairs(EnumFacing.EAST, false));
        set(world, origin, -1, 0, 1, stairs(EnumFacing.EAST, false));
        set(world, origin, 1, 0, -1, stairs(EnumFacing.WEST, false));
        set(world, origin, 1, 0, 0, stairs(EnumFacing.WEST, false));
        set(world, origin, 1, 0, 1, stairs(EnumFacing.WEST, false));
        set(world, origin, 0, 0, -2, stairs(EnumFacing.SOUTH, false));
        set(world, origin, 0, 0, 2, stairs(EnumFacing.NORTH, false));
        set(world, origin, -1, -1, -1, stairs(EnumFacing.NORTH, true));
        set(world, origin, 1, -1, -1, stairs(EnumFacing.NORTH, true));
        set(world, origin, -1, -1, 1, stairs(EnumFacing.SOUTH, true));
        set(world, origin, 1, -1, 1, stairs(EnumFacing.SOUTH, true));

        // Walls between the front/back pillars
        for (int y = -3; y <= -1; y++) {
            set(world, origin, 0, y, -2, AMBlocks.magic_wall.getDefaultState());
            set(world, origin, 0, y, 2, AMBlocks.magic_wall.getDefaultState());
        }

        // Lecterns and levers in the corners
        for (int z = -2; z <= 2; z += 4) {
            set(world, origin, 2, -3, z, AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.EAST));
            set(world, origin, -2, -3, z, AMBlocks.lectern.getDefaultState().withProperty(BlockLectern.FACING, EnumFacing.WEST));
            set(world, origin, 2, -2, z, Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.EAST));
            set(world, origin, -2, -2, z, Blocks.LEVER.getDefaultState().withProperty(BlockLever.FACING, EnumOrientation.WEST));
        }

        // The altar block itself goes last so its structure check sees everything
        world.setBlockState(origin, AMBlocks.crafting_altar.getDefaultState(), 3);
    }

    private static IBlockState stairs(EnumFacing facing, boolean inverted) {
        return AMBlocks.witchwood_stairs.getDefaultState()
                .withProperty(BlockStairs.FACING, facing)
                .withProperty(BlockStairs.HALF, inverted ? EnumHalf.TOP : EnumHalf.BOTTOM);
    }

    private static void set(World world, BlockPos origin, int x, int y, int z, IBlockState state) {
        world.setBlockState(origin.add(x, y, z), state, 2);
    }
}
