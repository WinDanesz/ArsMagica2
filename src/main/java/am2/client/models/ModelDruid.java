package am2.client.models;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A {@link ModelBiped} with a cloak and a carved staff attached to the animated body parts.
 * The child relationships keep both accessories aligned with the biped pose every frame.
 *
 * UV layout on the 64x128 druid texture: body keeps the classic 64x32 layout (rows 0-31),
 * untouched; cloak at (0,32); staff pole at (0,50); staff butt at (12,50); crown pieces at
 * (24,50), (34,50), and (44,50).
 */
@SideOnly(Side.CLIENT)
public class ModelDruid extends ModelBiped {

    public ModelDruid() {
        super(0.0F, 0.0F, 64, 128);

        ModelRenderer cloak = new ModelRenderer(this, 0, 32);
        cloak.addBox(-5.0F, 0.0F, 0.0F, 10, 16, 1, 0.0F);
        cloak.setRotationPoint(0.0F, 0.2F, 2.4F);
        cloak.rotateAngleX = 0.06F;
        this.bipedBody.addChild(cloak);

        ModelRenderer staff = new ModelRenderer(this);
        // Anchor the shaft at the palm so the staff follows the right arm's natural movement.
        staff.setRotationPoint(-1.0F, 2.5F, -2.0F);
        staff.rotateAngleZ = -0.12F;
        // Counter the resting arm's forward bend to keep the planted shaft near vertical.
        staff.rotateAngleX = 0.35F;
        this.bipedRightArm.addChild(staff);

        ModelRenderer staffPole = new ModelRenderer(this, 0, 50);
        staffPole.addBox(-1.0F, -13.0F, -1.0F, 2, 35, 2, 0.0F);
        staff.addChild(staffPole);

        // The heavier carved butt sits alongside the last six pixels of the extended shaft.
        ModelRenderer staffButt = new ModelRenderer(this, 12, 50);
        staffButt.addBox(-2.0F, 16.0F, -2.0F, 4, 6, 4, 0.0F);
        staff.addChild(staffButt);

        ModelRenderer staffCrown = new ModelRenderer(this, 24, 50);
        staffCrown.addBox(-3.0F, -18.0F, -2.0F, 6, 5, 4, 0.0F);
        staff.addChild(staffCrown);

        ModelRenderer staffCrownLeft = new ModelRenderer(this, 34, 50);
        staffCrownLeft.setRotationPoint(-3.0F, -13.0F, 0.0F);
        staffCrownLeft.addBox(-2.0F, -2.0F, -1.0F, 2, 4, 2, 0.0F);
        staffCrownLeft.rotateAngleZ = -0.25F;
        staff.addChild(staffCrownLeft);

        ModelRenderer staffCrownRight = new ModelRenderer(this, 44, 50);
        staffCrownRight.setRotationPoint(3.0F, -13.0F, 0.0F);
        staffCrownRight.addBox(0.0F, -2.0F, -1.0F, 2, 4, 2, 0.0F);
        staffCrownRight.rotateAngleZ = 0.25F;
        staff.addChild(staffCrownRight);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor,
                                  Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch,
                scaleFactor, entityIn);
        // A small forward torso lean gives the stationary pose a weathered, hunched posture.
        this.bipedBody.rotateAngleX += 0.12F;
        // Keep the hip joints aligned with the tilted bottom of the torso.
        this.bipedRightLeg.rotationPointZ += 1.4F;
        this.bipedLeftLeg.rotationPointZ += 1.4F;
        // The palm meets the arm-anchored shaft at a natural forward bend.
        this.bipedRightArm.rotateAngleX = -0.35F + this.bipedRightArm.rotateAngleX * 0.15F;
        this.bipedRightArm.rotateAngleY = 0.0F;
        this.bipedRightArm.rotateAngleZ = 0.12F + this.bipedRightArm.rotateAngleZ * 0.2F;

        // Successful spell casts synchronize swingProgress to nearby clients. Raise and wave
        // the free hand for that swing, blending back to its walking pose at either end.
        if (this.swingProgress > 0.0F) {
            float castProgress = MathHelper.clamp(this.swingProgress, 0.0F, 1.0F);
            float raise = MathHelper.sin(castProgress * (float) Math.PI);
            float wave = MathHelper.sin(castProgress * (float) Math.PI * 2.0F);
            this.bipedLeftArm.rotateAngleX += (-1.8F - this.bipedLeftArm.rotateAngleX) * raise;
            this.bipedLeftArm.rotateAngleY += 0.35F * wave * raise;
            this.bipedLeftArm.rotateAngleZ += (-0.3F + 0.45F * wave) * raise;
        }
    }
}
