package am2.client.utils;

import am2.client.bosses.models.ModelPlantGuardianSickle;
import am2.client.bosses.models.ModelWinterGuardianArm;
import am2.client.models.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLibrary {

    public static final ModelLibrary instance = new ModelLibrary();

    private ModelLibrary() {
        sickle.setNoSpin();

        dummyArcaneSpellbook = new ModelArcaneGuardianSpellBook();
        winterGuardianArm = new ModelWinterGuardianArm();
        fireEars = new ModelFireGuardianEars();
        waterOrbs = new ModelWaterGuardianOrbs();
        earthArmor = new ModelEarthGuardianChest();
        airSled = new ModelAirGuardianHoverball();
        wardingCandle = new ModelCandle();
    }

    public final ModelPlantGuardianSickle sickle = new ModelPlantGuardianSickle();

    public final ModelArcaneGuardianSpellBook dummyArcaneSpellbook;
    public final ModelWinterGuardianArm winterGuardianArm;

    public final ModelFireGuardianEars fireEars;
    public final ModelWaterGuardianOrbs waterOrbs;
    public final ModelEarthGuardianChest earthArmor;

    public final ModelAirGuardianHoverball airSled;

    public final ModelCandle wardingCandle;

    public final ModelMageHoodLowered mageHoodLowered = new ModelMageHoodLowered();

    public final ModelMageRobe mageRobe = new ModelMageRobe();
}
