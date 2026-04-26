package am2.common.blocks;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityFlickerHabitat;
import am2.common.defs.IDDefs;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFlickerHabitat extends BlockAMPowered {

    public BlockFlickerHabitat() {
        super(Material.ROCK);
        setHardness(2);
        setResistance(3);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityFlickerHabitat && ((TileEntityFlickerHabitat) te).hasFlicker()) {
            return 7;
        }
        return 0;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return new TileEntityFlickerHabitat();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
        ItemStack heldItem = player.getHeldItem(hand);

        if (!heldItem.isEmpty() && heldItem.getItem() == AMItems.crystal_wrench) {
            if (world.isRemote) {
                player.swingArm(hand);
            }
            return false;
        } else {
            player.openGui(ArsMagica.instance, IDDefs.GUI_FLICKER_HABITAT, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        setBlockMode(worldIn, pos);
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        setBlockMode(worldIn, pos);
        super.onBlockAdded(worldIn, pos, state);
    }

    protected void setBlockMode(World world, BlockPos pos) {
        if (world.isRemote)
            return;

        TileEntity ent = world.getTileEntity(pos);
        int habCount = 0;

        if (ent instanceof TileEntityFlickerHabitat) {
            TileEntityFlickerHabitat hab = (TileEntityFlickerHabitat) ent;
            for (EnumFacing direction : EnumFacing.values()) {
                Block block = world.getBlockState(pos.offset(direction)).getBlock();
                TileEntity te = world.getTileEntity(pos.offset(direction));
                if (block == AMBlocks.flicker_habitat && te != null && te instanceof TileEntityFlickerHabitat) {
                    TileEntityFlickerHabitat foundHab = (TileEntityFlickerHabitat) te;
                    if (foundHab.isUpgrade() == false) {
                        habCount++;
                        if (habCount == 1) {
                            hab.setUpgrade(true, direction);
                        } else {
                            world.destroyBlock(pos, true);
                        }
                    } else {
                        world.destroyBlock(pos, true);
                    }
                }
            }
        }
    }

    @Override
    public void onNeighborChange(IBlockAccess iblockaccess, BlockPos pos, BlockPos neighbor) {
        if (!(iblockaccess instanceof World))
            return;
        World world = (World) iblockaccess;
        if (world.isRemote)
            return;

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityFlickerHabitat) {
            TileEntityFlickerHabitat hab = (TileEntityFlickerHabitat) te;

            if (hab.isUpgrade()) {
                int habCount = 0;
                for (EnumFacing direction : EnumFacing.values()) {
                    te = world.getTileEntity(pos.offset(direction));
                    if (te != null && te instanceof TileEntityFlickerHabitat) {
                        TileEntityFlickerHabitat foundHab = (TileEntityFlickerHabitat) te;
                        if (foundHab.isUpgrade() == false) {
                            habCount++;
                            if (habCount == 1) {
                            } else {
                                world.destroyBlock(pos, true);
                            }
                        } else {
                            world.destroyBlock(pos, true);
                        }
                    }
                }

                if (habCount == 0) {
                    world.destroyBlock(pos, true);
                }
            } else {
                hab.scanForNearbyUpgrades();

                if (!hab.isUpgrade()) {
                    hab.scanForNearbyUpgrades();
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntityFlickerHabitat habitat = (TileEntityFlickerHabitat) worldIn.getTileEntity(pos);

        //if there is no habitat at the location break out
        if (habitat == null)
            return;

        //if the habitat has a flicker throw it on the ground
        if (habitat.hasFlicker()) {
            ItemStack stack = habitat.getStackInSlot(0);

            float offsetX = worldIn.rand.nextFloat() * 0.8F + 0.1F;
            float offsetY = worldIn.rand.nextFloat() * 0.8F + 0.1F;
            float offsetZ = worldIn.rand.nextFloat() * 0.8F + 0.1F;
            float force = 0.05F;

            EntityItem entityItem = new EntityItem(worldIn, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, stack);
            entityItem.motionX = (float) worldIn.rand.nextGaussian() * force;
            entityItem.motionY = (float) worldIn.rand.nextGaussian() * force + 0.2F;
            entityItem.motionZ = (float) worldIn.rand.nextGaussian() * force;
            worldIn.spawnEntity(entityItem);
        }

        if (!habitat.isUpgrade()) {
            for (EnumFacing direction : EnumFacing.values()) {
                TileEntity te = worldIn.getTileEntity(pos.offset(direction));
                if (te != null && te instanceof TileEntityFlickerHabitat) {
                    TileEntityFlickerHabitat upgHab = (TileEntityFlickerHabitat) te;

                    if (upgHab.isUpgrade()) {
                        worldIn.destroyBlock(pos.offset(direction), true);
                        worldIn.setTileEntity(pos.offset(direction), null);
                    }
                }
            }
        }

        super.breakBlock(worldIn, pos, state);
        return;
    }
}
