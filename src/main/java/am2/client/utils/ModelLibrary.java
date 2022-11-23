package am2.client.utils;

import am2.client.bosses.models.ModelPlantGuardianSickle;
import am2.client.bosses.models.ModelWinterGuardianArm;
import am2.client.entity.models.ModelBroom;
import am2.client.models.ModelAirGuardianHoverball;
import am2.client.models.ModelArcaneGuardianSpellBook;
import am2.client.models.ModelCandle;
import am2.client.models.ModelEarthGuardianChest;
import am2.client.models.ModelFireGuardianEars;
import am2.client.models.ModelWaterGuardianOrbs;
import am2.common.entity.EntityBroom;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLibrary{

	public static final ModelLibrary instance = new ModelLibrary();

	private ModelLibrary(){
		dummyBroom = new EntityBroom(Minecraft.getMinecraft().world);
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

	public final ModelBroom magicBroom = new ModelBroom();
	public final EntityBroom dummyBroom;
	public final ModelArcaneGuardianSpellBook dummyArcaneSpellbook;
	public final ModelWinterGuardianArm winterGuardianArm;

	public final ModelFireGuardianEars fireEars;
	public final ModelWaterGuardianOrbs waterOrbs;
	public final ModelEarthGuardianChest earthArmor;

	public final ModelAirGuardianHoverball airSled;

	public final ModelCandle wardingCandle;
}
