package am2.common.blocks.tileentity.flickers;

import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.api.math.AMVector3;
import am2.common.items.ItemBindingCatalyst;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.DummyEntityPlayer;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FlickerOperatorFelledOak extends AbstractFlickerFunctionality {

    public final static FlickerOperatorFelledOak instance = new FlickerOperatorFelledOak();

    private DummyEntityPlayer dummyPlayer;

    private static final int radius_horiz = 6;
    private static final int radius_vert = 1;
    private final int horizRange;
    private final int vertRange;
    private Set<BlockPos> tree;
    private static BlockPos origin;
    //private IBlockState originalBlockType;

    public FlickerOperatorFelledOak() {
        horizRange = radius_horiz + 7;
        vertRange = 30;
    }

    @Override
    public int getID() {
        return 2;
    }

    void destroyTree(World world, BlockPos pos, IBlockState state) {
        for (int xPos = pos.getX() - 1; xPos <= pos.getX() + 1; xPos++) {
            for (int yPos = pos.getY(); yPos <= pos.getY() + 1; yPos++) {
                for (int zPos = pos.getZ() - 1; zPos <= pos.getZ() + 1; zPos++) {
                    BlockPos newPos = new BlockPos(xPos, yPos, zPos);
                    if (tree.contains(newPos) || !isWithinBounds(newPos))
                        continue;
                    IBlockState localblock = world.getBlockState(newPos);
                    if (state.getBlock() == localblock.getBlock()) {
                        int stateMeta = WorldUtils.getBlockMeta(state) % 4;
                        int localblockMeta = WorldUtils.getBlockMeta(localblock) % 4;
                        if (stateMeta == localblockMeta) {
                            state.getBlock().harvestBlock(world, dummyPlayer, newPos, state, null, ItemStack.EMPTY);
                            state.getBlock().onBlockHarvested(world, newPos, state, dummyPlayer);
                            world.destroyBlock(newPos, false);
                            tree.add(newPos);
                            destroyTree(world, newPos, state);
                        }
                    }
                }
            }
        }
    }

    void beginTreeFelling(World world, BlockPos pos) {
        int height = 0;
        IBlockState wood = world.getBlockState(pos);
        while (wood.getBlock().isWood(world, pos)) {
            pos = pos.down();
            wood = world.getBlockState(pos);
        }

        tree = new HashSet<BlockPos>();

        pos = pos.up();
        wood = world.getBlockState(pos);
        if (wood.getBlock().isWood(world, pos)) {
            height = pos.getY();
            //boolean foundTop = false;
            while (true) {
                height++;
                //IBlockState block = world.getBlockState(new BlockPos(pos.getX(), height, pos.getZ()));
                //if (block.getBlock() != wood.getBlock()) break;
                if (!isLog(world, new BlockPos(pos.getX(), height, pos.getZ()))) break;
            }
            height--;
            int numLeaves = 0;
            if (height - pos.getY() < 50) {
                for (int xPos = pos.getX() - 1; xPos <= pos.getX() + 1; xPos++) {
                    for (int yPos = height - 1; yPos <= height + 1; yPos++) {
                        for (int zPos = pos.getZ() - 1; zPos <= pos.getZ() + 1; zPos++) {
                            BlockPos newPos = new BlockPos(xPos, yPos, zPos);
                            IBlockState leaves = world.getBlockState(newPos);
                            if (leaves != null && leaves.getBlock().isLeaves(leaves, world, newPos))
                                numLeaves++;
                        }
                    }
                }
            }
            origin = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
            if (numLeaves > 3)
                destroyTree(world, pos, world.getBlockState(pos));


//			if (!world.isRemote)
//				world.playAuxSFX(2001, pos, Block.getIdFromBlock(wood.getBlock()) + (WorldUtils.getBlockMeta(wood) << 12));
        }
    }

    @SuppressWarnings("deprecation")
    private void plantTree(World world, IFlickerController<?> habitat, boolean powered) {
        if (!powered || world.isRemote)
            return;

        ItemStack sapling = getSaplingFromNearbyChest(world, habitat);
        if (sapling.isEmpty())
            return;

        AMVector3 plantLoc = getPlantLocation(world, habitat, sapling);

        if (plantLoc == null)
            return;

        deductSaplingFromNearbyChest(world, habitat);
        ItemBlock block = (ItemBlock) sapling.getItem();

        world.setBlockState(plantLoc.toBlockPos(), block.getBlock().getStateFromMeta(sapling.getItemDamage()), 3);
    }

    private AMVector3 getPlantLocation(World world, IFlickerController<?> habitat, ItemStack sapling) {
        if (sapling.getItem() instanceof ItemBlock == false)
            return null;
        TileEntity te = (TileEntity) habitat;
        byte[] data = habitat.getMetadata(this);
        AMVector3 offset = null;
        if (data == null || data.length == 0) {
            offset = new AMVector3(te.getPos().getX() - radius_horiz, te.getPos().getY() - radius_vert, te.getPos().getZ() - radius_horiz);
        } else {
            AMDataReader reader = new AMDataReader(data, false);
            offset = new AMVector3(reader.getInt(), te.getPos().getY() - radius_vert, reader.getInt());
        }

        Block treeBlock = ((ItemBlock) sapling.getItem()).getBlock();

        for (int i = (int) offset.x; i <= te.getPos().getX() + radius_horiz; i += 2) {
            for (int k = (int) offset.z; k <= te.getPos().getZ() + radius_horiz; k += 2) {
                for (int j = (int) offset.y; j <= te.getPos().getY() + radius_vert; ++j) {
                    BlockPos newPos = new BlockPos(i, j, k);
                    IBlockState block = world.getBlockState(newPos);
                    if (block.getBlock().isReplaceable(world, newPos) && treeBlock.canPlaceBlockAt(world, newPos)) {
                        AMDataWriter writer = new AMDataWriter();
                        writer.add(i).add(k);
                        habitat.setMetadata(this, writer.generate());
                        return new AMVector3(i, j, k);
                    }
                }
            }
        }

        AMDataWriter writer = new AMDataWriter();
        writer.add(te.getPos().getX() - radius_horiz).add(te.getPos().getZ() - radius_horiz);
        habitat.setMetadata(this, writer.generate());

        return null;
    }

    /**
     * Gets a single sapling from an adjacent chest
     *
     * @return
     */
    private ItemStack getSaplingFromNearbyChest(World world, IFlickerController<?> habitat) {
        for (EnumFacing dir : EnumFacing.values()) {
            IInventory inv = getOffsetInventory(world, habitat, dir);
            if (inv == null)
                continue;
            int index = InventoryUtilities.getInventorySlotIndexFor(inv, new ItemStack(Blocks.SAPLING, 1, Short.MAX_VALUE));
            if (index > -1) {
                ItemStack stack = inv.getStackInSlot(index).copy();
                stack.setCount(1);
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private void deductSaplingFromNearbyChest(World world, IFlickerController<?> habitat) {
        for (EnumFacing dir : EnumFacing.values()) {
            IInventory inv = getOffsetInventory(world, habitat, dir);
            if (inv == null)
                continue;
            int index = InventoryUtilities.getInventorySlotIndexFor(inv, new ItemStack(Blocks.SAPLING, 1, Short.MAX_VALUE));
            if (index > -1) {
                InventoryUtilities.decrementStackQuantity(inv, index, 1);
                return;
            }
        }
    }

    /**
     * Gets an instance of the adjacent IInventory at direction offset.  Returns null if not found or invalid type adjacent.
     */
    private IInventory getOffsetInventory(World world, IFlickerController<?> habitat, EnumFacing direction) {
        TileEntity te = (TileEntity) habitat;
        TileEntity adjacent = world.getTileEntity(te.getPos().offset(direction));
        if (adjacent != null && adjacent instanceof IInventory)
            return (IInventory) adjacent;
        return null;
    }

    @Override
    public boolean RequiresPower() {
        return false;
    }

    @Override
    public int PowerPerOperation() {
        return 100;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered) {
        //int radius = 6;

        dummyPlayer = new DummyEntityPlayer(world);

        for (int i = -radius_horiz; i <= radius_horiz; ++i) {
            for (int j = -radius_horiz; j <= radius_horiz; ++j) {
                BlockPos newPos = ((TileEntity) habitat).getPos().add(i, 0, j);
                Block block = world.getBlockState(newPos).getBlock();
                if (block == Blocks.AIR) continue;
                if (block.isWood(world, newPos)) {
                    if (!world.isRemote)
                        beginTreeFelling(world, newPos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {

        boolean hasNatureAugment = Arrays.asList(flickers).contains(Affinities.nature);
        if (hasNatureAugment) {
            plantTree(world, habitat, powered);
        }

        return DoOperation(world, habitat, powered);
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered) {
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        int base = powered ? 300 : 3600;
        //float augments = 1.0f;
        for (Affinity aff : flickers) {
            if (aff == Affinities.lightning)
                base = (int) Math.ceil(base * 0.5f);
        }
        return base;
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "WG ",
                "NCL",
                " OW",
                Character.valueOf('W'), AMBlocks.witchwood_log,
                Character.valueOf('G'), new ItemStack(AMItems.rune, 1, EnumDyeColor.GREEN.getDyeDamage()),
                Character.valueOf('N'), new ItemStack(AMItems.flicker_jar, 1, Affinities.nature.getID()),
                Character.valueOf('L'), new ItemStack(AMItems.flicker_jar, 1, Affinities.lightning.getID()),
                Character.valueOf('C'), new ItemStack(AMItems.binding_catalyst, 1, ItemBindingCatalyst.META_AXE),
                Character.valueOf('O'), new ItemStack(AMItems.rune, 1, EnumDyeColor.ORANGE.getDyeDamage())
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "flickeroperatorfelledoak");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.nature, Affinities.lightning};
    }

    public static boolean isLog(World world, BlockPos pos) {
        return world.getBlockState(pos).getBlock().isWood(world, pos);
    }

    public boolean isWithinBounds(BlockPos bp) {
        int dist = Math.abs(origin.getX() - bp.getX());
        if (dist > horizRange / 2) return false;
        dist = Math.abs(origin.getZ() - bp.getZ());
        if (dist > horizRange / 2) return false;
        dist = Math.abs(origin.getY() - bp.getY());
        return dist <= vertRange / 2;
    }
}
