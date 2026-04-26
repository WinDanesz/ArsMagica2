package am2.common.blocks;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.KeystoneAccessType;
import am2.common.blocks.tileentity.TileEntityKeystoneDoor;
import am2.common.defs.IDDefs;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.utils.KeystoneUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockKeystoneDoor extends BlockDoor implements ITileEntityProvider {

    public BlockKeystoneDoor() {
        super(Material.WOOD);
        this.setHardness(2.5f);
        this.setResistance(2.0f);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos oldPos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = playerIn.getHeldItem(hand);
        BlockPos pos = oldPos;
        if (worldIn.getBlockState(pos.down()).getBlock() == AMBlocks.keystone_door)
            pos = oldPos.down();

        TileEntity te = worldIn.getTileEntity(pos);
        playerIn.swingArm(hand);

        if (KeystoneUtilities.HandleKeystoneRecovery(playerIn, (IKeystoneLockable<?>) te))
            return true;

        if (KeystoneUtilities.instance.canPlayerAccess((IKeystoneLockable<?>) te, playerIn, KeystoneAccessType.USE)) {
            if (playerIn.isSneaking()) {
                if (!worldIn.isRemote)
                    playerIn.openGui(ArsMagica.instance, IDDefs.GUI_KEYSTONE_LOCKABLE, worldIn, pos.getX(), pos.getY(), pos.getZ());
            } else {
                activateNeighbors(worldIn, pos, playerIn, hand, heldItem, side, hitX, hitY, hitZ);
                //CompendiumUnlockHandler.unlockEntry(this.getUnlocalizedName().replace("arsmagica2:", "").replace("tile.", ""));
                return super.onBlockActivated(worldIn, oldPos, state, playerIn, hand, side, hitX, hitY, hitZ);
            }
        }
        return false;
    }

    private void activateNeighbors(World world, BlockPos pos, EntityPlayer player, EnumHand hand, ItemStack held, EnumFacing direction, float xOffset, float yOffset, float zOffset) {
        if (world.getBlockState(pos.east()).getBlock() == AMBlocks.keystone_door)
            super.onBlockActivated(world, pos.east(), world.getBlockState(pos.east()), player, hand, direction, xOffset, yOffset, zOffset);

        if (world.getBlockState(pos.west()).getBlock() == AMBlocks.keystone_door)
            super.onBlockActivated(world, pos.west(), world.getBlockState(pos.west()), player, hand, direction, xOffset, yOffset, zOffset);

        if (world.getBlockState(pos.north()).getBlock() == AMBlocks.keystone_door)
            super.onBlockActivated(world, pos.north(), world.getBlockState(pos.north()), player, hand, direction, xOffset, yOffset, zOffset);

        if (world.getBlockState(pos.south()).getBlock() == AMBlocks.keystone_door)
            super.onBlockActivated(world, pos.south(), world.getBlockState(pos.south()), player, hand, direction, xOffset, yOffset, zOffset);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(AMItems.keystone_door);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (world.isRemote)
            return false;

        if (world.getBlockState(pos.down()).getBlock() == AMBlocks.keystone_door)
            pos = pos.down();

        IKeystoneLockable<?> lockable = (IKeystoneLockable<?>) world.getTileEntity(pos);

        if (lockable == null)
            return false;

        if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK)) return false;

        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (worldIn.isRemote)
            return;

        if (worldIn.getBlockState(pos.down()).getBlock() == AMBlocks.keystone_door)
            pos = pos.down();

        IKeystoneLockable<?> lockable = (IKeystoneLockable<?>) worldIn.getTileEntity(pos);

        if (lockable == null)
            return;

        if (!KeystoneUtilities.instance.canPlayerAccess(lockable, player, KeystoneAccessType.BREAK))
            return;
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return AMItems.keystone_door;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityKeystoneDoor();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        // Only the lower half should have a tile entity to store the keystone slots.
        // The upper half is just the visual top of the door and doesn't need tile entity data.
        // This prevents duplication of inventory data and ensures proper tile entity lifecycle.
        return state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
    }

    public Block registerAndName(ResourceLocation rl) {
        this.setTranslationKey(rl.getPath());
        // TODO: registry GameRegistry.register(this, rl);
        return this;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
}
