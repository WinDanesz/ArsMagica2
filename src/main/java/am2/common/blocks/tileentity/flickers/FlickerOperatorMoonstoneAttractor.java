package am2.common.blocks.tileentity.flickers;

import am2.api.affinity.Affinity;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.api.flickers.IFlickerController;
import am2.api.math.AMVector3;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;

public class FlickerOperatorMoonstoneAttractor extends AbstractFlickerFunctionality {

    public final static FlickerOperatorMoonstoneAttractor instance = new FlickerOperatorMoonstoneAttractor();

    private static final ArrayList<AMVector3> attractors = new ArrayList<AMVector3>();

    public static AMVector3 getMeteorAttractor(AMVector3 target) {
        for (AMVector3 attractor : attractors.toArray(new AMVector3[attractors.size()])) {
            if (attractor.distanceSqTo(target) <= 16384)
                return attractor.copy();
        }
        return null;
    }

    @Override
    public boolean RequiresPower() {
        return true;
    }

    @Override
    public int PowerPerOperation() {
        return 10;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered) {
        AMVector3 vec = new AMVector3((TileEntity) habitat);
        if (powered) {
            if (!attractors.contains(vec)) {
                attractors.add(vec);
            }
            return true;
        } else {
            attractors.remove(vec);
        }
        return false;
    }

    @Override
    public boolean DoOperation(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        return DoOperation(world, habitat, powered);
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered) {
        AMVector3 vec = new AMVector3((TileEntity) habitat);
        attractors.remove(vec);
    }

    @Override
    public int TimeBetweenOperation(boolean powered, Affinity[] flickers) {
        return 100;
    }

    @Override
    public void RemoveOperator(World world, IFlickerController<?> habitat, boolean powered, Affinity[] flickers) {
        RemoveOperator(world, habitat, powered);
    }

    @Override
    public Object[] getRecipe() {
        return new Object[]{
                "RLR",
                "AME",
                "I T",
                Character.valueOf('R'), new ItemStack(AMItems.rune, 1, EnumDyeColor.ORANGE.getDyeDamage()),
                Character.valueOf('L'), new ItemStack(AMItems.flicker_jar, 1, Affinities.lightning.getID()),
                Character.valueOf('A'), new ItemStack(AMItems.flicker_jar, 1, Affinities.arcane.getID()),
                Character.valueOf('E'), new ItemStack(AMItems.flicker_jar, 1, Affinities.earth.getID()),
                Character.valueOf('M'), new ItemStack(AMBlocks.moonstone_ore),
                Character.valueOf('I'), AffinityShiftUtils.getEssenceForAffinity(Affinities.air),
                Character.valueOf('T'), AffinityShiftUtils.getEssenceForAffinity(Affinities.earth)
        };
    }

    @Override
    public ResourceLocation getTexture() {
        return new ResourceLocation("arsmagica2", "FlickerOperatorMoonstoneAttractor");
    }

    @Override
    public Affinity[] getMask() {
        return new Affinity[]{Affinities.lightning, Affinities.arcane, Affinities.earth};
    }

    @Override
    public int getID() {
        return 9;
    }


}
