package am2.common.fluids;

import am2.ArsMagica;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class EssenceFluid extends Fluid {

    public static final String name = "liquid_essence";
    public static final EssenceFluid instance = new EssenceFluid();

    public EssenceFluid() {
        super(name, new ResourceLocation(ArsMagica.MODID, "blocks/liquid_essence_still"), new ResourceLocation(ArsMagica.MODID, "blocks/liquid_essence_flowing"));
        setDensity(8);
        setViscosity(3000);
    }

}