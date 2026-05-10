package am2.common.blocks.tileentity.flickers;

import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.common.blocks.BlockInvisibleUtility;
import am2.common.blocks.BlockInvisibleUtility.EnumInvisibleType;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlickerOperatorContainment extends AbstractFlickerFunctionality {

    public final static FlickerOperatorContainment instance = new FlickerOperatorContainment();

    protected static final int BASE_RADIUS = 6;

    protected void setUtilityBlock(World world, BlockPos pos, EnumInvisibleType meta) {

        if (world.getBlockState(pos).getBlock() == AMBlocks.invisible_utility) {
            EnumInvisibleType exMeta = BlockInvisibleUtility.getType(world.getBlockState(pos));
            if (meta == exMeta) return;
            if ((meta == EnumInvisibleType.COLLISION_POSITIVE_X || meta == EnumInvisibleType.COLLISION_NEGATIVE_X) && exMeta == EnumInvisibleType.COLLISION_ALL_X)
                return;
            if ((meta == EnumInvisibleType.COLLISION_NEGATIVE_Z || meta == EnumInvisibleType.COLLISION_POSITIVE_Z) && exMeta == EnumInvisibleType.COLLISION_ALL_Z)
                return;
            if ((meta == EnumInvisibleType.COLLISION_NEGATIVE_X && exMeta == EnumInvisibleType.COLLISION_POSITIVE_X) || (meta == EnumInvisibleType.COLLISION_POSITIVE_X && exMeta == EnumInvisibleType.COLLISION_NEGATIVE_X))
                meta = EnumInvisibleType.COLLISION_ALL_X;
            else if ((meta == EnumInvisibleType.COLLISION_NEGATIVE_Z && exMeta == EnumInvisibleType.COLLISION_POSITIVE_Z) || (meta == EnumInvisibleType.COLLISION_POSITIVE_Z && exMeta == EnumInvisibleType.COLLISION_NEGATIVE_Z))
                meta = EnumInvisibleType.COLLISION_ALL_Z;

            world.setBlockState(pos, AMBlocks.invisible_utility.getDefaultState().withProperty(BlockInvisibleUtility.TYPE, meta));
        } else {
            if (world.isAirBlock(pos))
                world.setBlockState(pos, AMBlocks.invisible_utility.getDefaultState().withProperty(BlockInvisibleUtility.TYPE, meta));
        }
    }

    protected void clearUtilityBlock(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == AMBlocks.invisible_utility) {
            world.setBlockToAir(pos);
        }
    }

    protected void setLastRadius(IFlickerController<?> habitat, int radius) {
        habitat.setMetadata(this, new AMDataWriter().add(radius).generate());
    }

    protected int getLastRadius(IFlickerController<?> habitat) {
        byte[] meta = habitat.getMetadata(this);
        if (meta == null || meta.length == 0)
            return BASE_RADIUS;
        AMDataReader rdr = new AMDataReader(meta, false);
        return rdr.getInt();
    }

    @Override
    public int getID() {
        return 1;
    }


    protected int calculateRadius(Affinity[] flickers) {
        int rad = BASE_RADIUS;
        for (Affinity aff : flickers) {
            if (aff == Affinities.ice) {
                rad++;
            }
        }
        return rad;
    }

    @Override
    public boolean RequiresPower() {
        return true;
    }

    @Override
    public int PowerPerOperation() {
        return 5;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered) {
        return false;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        if (world.isRemote)
            return true;

        int lastRadius = getLastRadius(habitat);
        int calcRadius = calculateRadius(flickers);

        if (lastRadius != calcRadius) {
            RemoveOperator(world, habitat, powered, flickers);
        }

        boolean hasArcaneAugment = false;
        for (Affinity aff : flickers) {
            if (aff == Affinities.arcane) {
                hasArcaneAugment = true;
                break;
            }
        }

        for (int i = 0; i < calcRadius * 2 + 1; ++i) {

            if (hasArcaneAugment) {
                //-x
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - calcRadius, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + calcRadius + 1 - i), EnumInvisibleType.COLLISION_ALL);
                //+x
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + calcRadius + 1, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - calcRadius + i), EnumInvisibleType.COLLISION_ALL);
                //-z
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - calcRadius + i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - calcRadius), EnumInvisibleType.COLLISION_ALL);
                //+z
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + calcRadius + 1 - i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + calcRadius + 1), EnumInvisibleType.COLLISION_ALL);
            } else {
                //-x
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - calcRadius, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + calcRadius + 1 - i), i == 0 ? EnumInvisibleType.COLLISION_ALL : EnumInvisibleType.COLLISION_POSITIVE_X);
                //+x
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + calcRadius + 1, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - calcRadius + i), i == 0 ? EnumInvisibleType.COLLISION_ALL : EnumInvisibleType.COLLISION_NEGATIVE_X);
                //-z
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - calcRadius + i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - calcRadius), i == 0 ? EnumInvisibleType.COLLISION_ALL : EnumInvisibleType.COLLISION_POSITIVE_Z);
                //+z
                setUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + calcRadius + 1 - i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + calcRadius + 1), i == 0 ? EnumInvisibleType.COLLISION_ALL : EnumInvisibleType.COLLISION_NEGATIVE_Z);
            }
        }

        setLastRadius(habitat, calcRadius);

        return true;
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered) {
        int radius = getLastRadius(habitat);

        for (int i = 0; i < radius * 2 + 1; ++i) {
            //-x
            clearUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - radius, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + radius + 1 - i));
            //+x
            clearUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + radius + 1, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - radius + i));
            //-z
            clearUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() - radius + i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() - radius));
            //+z
            clearUtilityBlock(world, new BlockPos(((TileEntity) habitat).getPos().getX() + radius + 1 - i, ((TileEntity) habitat).getPos().getY(), ((TileEntity) habitat).getPos().getZ() + radius + 1));
        }
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        return 200;
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        RemoveOperator(world, habitat, powered);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "FWF",
                "ARN",
                "IWI",
                Character.valueOf('F'), "fenceWood",
                Character.valueOf('W'), Blocks.COBBLESTONE_WALL,
                Character.valueOf('A'), new ItemStack(AMItems.flicker_jar, 1, Affinities.air.getID()),
                Character.valueOf('R'), new ItemStack(AMItems.rune, 1, EnumDyeColor.BLUE.getDyeDamage()),
                Character.valueOf('N'), new ItemStack(AMItems.flicker_jar, 1, Affinities.ender.getID()),
                Character.valueOf('I'), Blocks.IRON_BARS

        };
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "flickeroperatorcontainment");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.air, Affinities.ender};
    }

}
