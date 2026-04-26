package am2.client.entity.models;

import am2.common.entity.EntityEarthElemental;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelEarthElemental extends ModelZombie {

    public ModelEarthElemental() {
        super();
        // Skins are 64x64 — override the default 64x32 so UV row 48 is in bounds
       // this.textureHeight = 64;
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        if (entityIn instanceof EntityEarthElemental && ((EntityEarthElemental) entityIn).isStrangling()) {
            // Arms extended forward and slightly inward — hug/strangle pose
            this.bipedRightArm.rotateAngleX = -(float) (Math.PI / 2.0);
            this.bipedRightArm.rotateAngleZ = 0.4f;
            this.bipedLeftArm.rotateAngleX = -(float) (Math.PI / 2.0);
            this.bipedLeftArm.rotateAngleZ = -0.4f;
        }
    }
}
