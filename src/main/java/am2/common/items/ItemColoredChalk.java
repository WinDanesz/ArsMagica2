package am2.common.items;

import am2.common.blocks.BlockChalkArrow;
import am2.common.blocks.tileentity.TileEntityChalkArrow;
import am2.common.registry.AMBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemColoredChalk extends Item {

    public static final int MAX_USES = 32;
    private static final String NBT_USES = "uses";

    public ItemColoredChalk() {
        super();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setHasSubtypes(true);
    }

    private static int getUses(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey(NBT_USES)) {
            return stack.getTagCompound().getInteger(NBT_USES);
        }
        return 0;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getUses(stack) > 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return (double) getUses(stack) / MAX_USES;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0)); // white in creative tab
        } else if (tab == CreativeTabs.SEARCH) {
            for (int i = 0; i < EnumDyeColor.values().length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        int meta = stack.getItemDamage() & 0xF;
        return "item.arsmagica2:colored_chalk." + EnumDyeColor.byMetadata(meta).getName();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        BlockPos targetPos = pos.offset(facing);

        // Target must be air and the clicked face must be solid
        if (!world.isAirBlock(targetPos)) {
            return EnumActionResult.FAIL;
        }
        if (!world.isSideSolid(pos, facing)) {
            return EnumActionResult.FAIL;
        }

        if (!world.isRemote) {
            int colorIndex = stack.getItemDamage() & 0xF;

            IBlockState arrowState;
            if (facing == EnumFacing.UP) {
                // Floor: arrow points in the direction the player is facing
                arrowState = AMBlocks.chalk_arrow.getDefaultState()
                        .withProperty(BlockChalkArrow.FACING, player.getHorizontalFacing())
                        .withProperty(BlockChalkArrow.ON_CEILING, false)
                        .withProperty(BlockChalkArrow.ON_WALL, false);
            } else if (facing == EnumFacing.DOWN) {
                // Ceiling: arrow points in the direction the player is facing
                arrowState = AMBlocks.chalk_arrow.getDefaultState()
                        .withProperty(BlockChalkArrow.FACING, player.getHorizontalFacing())
                        .withProperty(BlockChalkArrow.ON_CEILING, true)
                        .withProperty(BlockChalkArrow.ON_WALL, false);
            } else {
                // Wall: FACING = the face toward the supporting wall (opposite of the clicked face)
                arrowState = AMBlocks.chalk_arrow.getDefaultState()
                        .withProperty(BlockChalkArrow.FACING, facing.getOpposite())
                        .withProperty(BlockChalkArrow.ON_CEILING, false)
                        .withProperty(BlockChalkArrow.ON_WALL, true);
            }

            world.setBlockState(targetPos, arrowState);

            TileEntity te = world.getTileEntity(targetPos);
            if (te instanceof TileEntityChalkArrow) {
                ((TileEntityChalkArrow) te).setColorIndex(colorIndex);
                ((TileEntityChalkArrow) te).setArrowFacing(player.getHorizontalFacing());
            }

            if (!player.capabilities.isCreativeMode) {
                NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
                int uses = nbt.getInteger(NBT_USES) + 1;
                if (uses >= MAX_USES) {
                    stack.setCount(0);
                } else {
                    nbt.setInteger(NBT_USES, uses);
                    stack.setTagCompound(nbt);
                }
            }
        }
        return EnumActionResult.SUCCESS;
    }
}
