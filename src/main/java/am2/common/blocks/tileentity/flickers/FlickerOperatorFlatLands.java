package am2.common.blocks.tileentity.flickers;

import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import am2.common.utils.DummyEntityPlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class FlickerOperatorFlatLands extends AbstractFlickerFunctionality {

    public final static FlickerOperatorFlatLands instance = new FlickerOperatorFlatLands();

    @Override
    public boolean RequiresPower() {
        return false;
    }

    @Override
    public int PowerPerOperation() {
        return 10;
    }

    @Override
    public int getID() {
        return 4;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered) {
        int searchesPerLoop = 12;

        int radius = 6;
        int diameter = radius * 2 + 1;

        if (!world.isRemote) {

            boolean actionPerformed = false;

            for (int i = 0; i < searchesPerLoop && !actionPerformed; ++i) {
                int effectX = ((TileEntity) habitat).getPos().getX() - radius + (world.rand.nextInt(diameter));
                int effectZ = ((TileEntity) habitat).getPos().getZ() - radius + (world.rand.nextInt(diameter));
                int effectY = ((TileEntity) habitat).getPos().getY() + world.rand.nextInt(radius);

                BlockPos effectPos = new BlockPos(effectX, effectY, effectZ);

                if (effectPos.equals(((TileEntity) habitat).getPos()))
                    return false;

                IBlockState block = world.getBlockState(effectPos);

                if (block != null && !world.isAirBlock(effectPos) && block.isOpaqueCube() && block.getBlock() != AMBlocks.invisible_utility) {
                    if (ForgeEventFactory.doPlayerHarvestCheck(new DummyEntityPlayer(world), block, true)) {
                        if (block.getBlock().removedByPlayer(block, world, effectPos, new DummyEntityPlayer(world), true)) {
                            block.getBlock().onPlayerDestroy(world, effectPos, block);
                            block.getBlock().dropBlockAsItem(world, effectPos, block, 0);
                            actionPerformed = true;
                        }
                    }
                }
            }

            return actionPerformed;
        } else {
            return false;
        }
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        return DoOperation(world, habitat, powered);
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered) {
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        return powered ? 1 : 20;
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
    }


    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "S P",
                "ENI",
                " R ",
                Character.valueOf('S'), Items.IRON_SHOVEL,
                Character.valueOf('P'), Items.IRON_PICKAXE,
                Character.valueOf('E'), new ItemStack(AMItems.flicker_jar, 1, Affinities.earth.getID()),
                Character.valueOf('N'), AffinityShiftUtils.getEssenceForAffinity(Affinities.earth),
                Character.valueOf('I'), new ItemStack(AMItems.flicker_jar, 1, Affinities.ice.getID()),
                Character.valueOf('R'), new ItemStack(AMItems.rune, 1, EnumDyeColor.BLACK.getDyeDamage())
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "flickeroperatorflatlands");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.earth, Affinities.ice};
    }

}
