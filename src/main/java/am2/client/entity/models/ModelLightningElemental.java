package am2.client.entity.models;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.7 - 1.12
// Paste this class into your mod and generate all required imports


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelLightningElemental extends ModelBase {
    public final ModelRenderer bipedHead;
    private final ModelRenderer hornLeft;
    private final ModelRenderer hornRight;
    private final ModelRenderer bipedBody;
    private final ModelRenderer tail;
    private final ModelRenderer bipedRightArm;
    private final ModelRenderer bipedLeftArm;

    public ModelLightningElemental() {
        this(0.0F);
    }

    public ModelLightningElemental(float inflate) {
        textureWidth = 64;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.cubeList.add(new ModelBox(bipedHead, 0, 0, -4.0F, -8.0F, -4.0F, 8, 8, 8, inflate, false));

        hornLeft = new ModelRenderer(this);
        hornLeft.setRotationPoint(0.0F, -8.0F, 0.0F);
        bipedHead.addChild(hornLeft);
        hornLeft.cubeList.add(new ModelBox(hornLeft, 33, 0, -7.0F, -2.0F, -3.0F, 4, 4, 0, inflate, false));

        hornRight = new ModelRenderer(this);
        hornRight.setRotationPoint(4.0F, -8.5F, -2.0F);
        bipedHead.addChild(hornRight);
        hornRight.cubeList.add(new ModelBox(hornRight, 33, 7, -1.0F, -1.5F, -1.0F, 4, 4, 0, inflate, false));

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.cubeList.add(new ModelBox(bipedBody, 16, 16, -4.0F, 0.0F, -2.0F, 8, 12, 4, inflate, false));

        tail = new ModelRenderer(this);
        tail.setRotationPoint(-1.0F, 12.0F, 0.0F);
        bipedBody.addChild(tail);
        tail.cubeList.add(new ModelBox(tail, 0, 33, -2.0F, 0.0F, 0.0F, 6, 9, 0, inflate, false));

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-4.0F, 0.0F, 0.0F);
        bipedRightArm.cubeList.add(new ModelBox(bipedRightArm, 40, 16, -4.0F, 0.0F, -2.0F, 4, 12, 4, inflate, false));

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(4.0F, 0.0F, 0.0F);
        bipedLeftArm.cubeList.add(new ModelBox(bipedLeftArm, 40, 33, 0.0F, 0.0F, -2.0F, 4, 12, 4, inflate, false));
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entityIn, float limbSwing, float limbSwingAmount, float partialTickTime) {
        float age = entityIn.ticksExisted + partialTickTime;

        // Idle arm float – gentle alternating rise/fall
        bipedRightArm.rotateAngleX = MathHelper.sin(age * 0.065F) * 0.08F - 0.30F;
        bipedRightArm.rotateAngleZ = MathHelper.sin(age * 0.055F + 1.0F) * 0.05F - 0.10F;
        bipedLeftArm.rotateAngleX = MathHelper.sin(age * 0.065F + (float) Math.PI) * 0.08F - 0.30F;
        bipedLeftArm.rotateAngleZ = MathHelper.sin(age * 0.055F + 1.0F + (float) Math.PI) * 0.05F + 0.10F;

        // Horn/ear wiggle – slightly offset so they don't move perfectly in sync
        hornLeft.rotateAngleZ  = MathHelper.sin(age * 0.13F) * 0.18F;
        hornLeft.rotateAngleX  = MathHelper.sin(age * 0.09F + 0.5F) * 0.10F;
        hornRight.rotateAngleZ = MathHelper.sin(age * 0.13F + 1.5F) * 0.15F;
        hornRight.rotateAngleX = MathHelper.sin(age * 0.09F + 2.0F) * 0.08F;

        // Tail sway – slow side-to-side with a bit of forward/back
        tail.rotateAngleZ = MathHelper.sin(age * 0.15F) * 0.22F;
        tail.rotateAngleY = MathHelper.sin(age * 0.11F) * 0.18F;
        tail.rotateAngleX = MathHelper.sin(age * 0.08F + 1.0F) * 0.10F;

        // Combat swing – override idle arm rotation when attacking
        float swingProgress = entityIn.getSwingProgress(partialTickTime);
        float swingX = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI * 2.0F) * 0.2F;
        float swingZ = MathHelper.sin(swingProgress * (float) Math.PI);
        bipedRightArm.rotateAngleZ += swingX;
        bipedRightArm.rotateAngleX -= swingZ * 1.2F;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        bipedHead.render(f5);
        bipedBody.render(f5);
        bipedRightArm.render(f5);
        bipedLeftArm.render(f5);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
        bipedHead.rotateAngleY = netHeadYaw * 0.017453292F;
        bipedHead.rotateAngleX = headPitch  * 0.017453292F;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}