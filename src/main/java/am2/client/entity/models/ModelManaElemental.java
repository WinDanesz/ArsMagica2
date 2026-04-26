package am2.client.entity.models;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelManaElemental extends ModelBiped {
    ModelRenderer Core;
    ModelRenderer Pelvis;

    public ModelManaElemental() {
        textureWidth = 64;
        textureHeight = 32;

        // HEAD (resized: height 6 instead of 4)
        bipedHead = new ModelRenderer(this, 32, 0);
        bipedHead.addBox(-4F, -6F, -4F, 8, 6, 8);
        bipedHead.setRotationPoint(0F, -9F, 0F);
        setRotation(bipedHead, 0F, 0F, 0F);

        // BODY
        bipedBody = new ModelRenderer(this, 0, 26);
        bipedBody.addBox(-6F, -1F, -2F, 12, 2, 4);
        bipedBody.setRotationPoint(0F, -7F, 0F);
        setRotation(bipedBody, 0F, 0F, 0F);

        // CORE
        Core = new ModelRenderer(this, 0, 20);
        Core.addBox(-1.5F, 3.5F, -1.5F, 3, 3, 3);
        Core.setRotationPoint(0F, -7F, 0F);
        setRotation(Core, 0F, 0F, 0F);

        // PELVIS
        Pelvis = new ModelRenderer(this, 0, 0);
        Pelvis.addBox(-5F, 9F, -2F, 10, 2, 4);
        Pelvis.setRotationPoint(0F, -7F, 0F);
        setRotation(Pelvis, 0F, 0F, 0F);

        // RIGHT ARM (extended segments)
        bipedRightArm = new ModelRenderer(this, 33, 16);
        bipedRightArm.setRotationPoint(-8F, -7F, 0F);
        bipedRightArm.addBox(-1.5F, 9F, -1.5F, 3, 2, 3); // original
        bipedRightArm.addBox(-1.5F, 5F, -1.5F, 3, 2, 3); // new
        bipedRightArm.addBox(-0.5F, 1F, -1.5F, 3, 2, 3); // new offset piece
        setRotation(bipedRightArm, 0F, 0F, 0F);

        // LEFT ARM (extended segments)
        bipedLeftArm = new ModelRenderer(this, 33, 22);
        bipedLeftArm.setRotationPoint(8F, -7F, 0F);
        bipedLeftArm.addBox(-1.5F, 9F, -1.5F, 3, 2, 3); // original
        bipedLeftArm.addBox(-1.5F, 5F, -1.5F, 3, 2, 3); // new
        bipedLeftArm.addBox(-2.5F, 1F, -1.5F, 3, 2, 3); // mirrored offset
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        // LEGS (unchanged)
        bipedLeftLeg = new ModelRenderer(this, 47, 22);
        bipedLeftLeg.addBox(-1.5F, 14F, -1.5F, 3, 2, 3);
        bipedLeftLeg.setRotationPoint(-4F, 3F, 0F);
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 47, 16);
        bipedRightLeg.addBox(-1.5F, 14F, -1.5F, 3, 2, 3);
        bipedRightLeg.setRotationPoint(4F, 3F, 0F);
        setRotation(bipedRightLeg, 0F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);

        bipedHead.render(f5);
        bipedBody.render(f5);
        Core.render(f5);
        Pelvis.render(f5);
        bipedRightArm.render(f5);
        bipedLeftArm.render(f5);
        bipedRightLeg.render(f5);
        bipedLeftLeg.render(f5);
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);

        setRotation(Core, bipedBody.rotateAngleX, bipedBody.rotateAngleY, bipedBody.rotateAngleZ);
        setRotation(Pelvis, bipedBody.rotateAngleX, bipedBody.rotateAngleY, bipedBody.rotateAngleZ);

        bipedHead.setRotationPoint(0F, -9F, 0F);
        bipedBody.setRotationPoint(0F, -7F, 0F);
        bipedRightArm.setRotationPoint(-8F, -7F, 0F);
        bipedLeftArm.setRotationPoint(8F, -7F, 0F);
        bipedRightLeg.setRotationPoint(4F, 7F, 0F);
        bipedLeftLeg.setRotationPoint(-4F, 7F, 0F);
    }
}