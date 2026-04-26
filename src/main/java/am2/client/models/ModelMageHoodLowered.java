package am2.client.models;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMageHoodLowered extends ModelBiped {

    private ModelRenderer cowl;

    public ModelMageHoodLowered() {
        super(0, 0, 64, 32);

        // Collapsed hood: a strip of bunched fabric at the back base of the head.
        // Head box spans x:[-4,4], y:[-8,0], z:[-4,4]. Back face at z=+4, base at y=0.
        // Tex offset (8, 8): places all UV faces within the solidly opaque
        // center of the helmet face area in the armor texture.
        cowl = new ModelRenderer(this, 8, 8);
        cowl.addBox(-4.5F, -3.0F, 4.0F, 9, 3, 2);
        cowl.setRotationPoint(0F, 0F, 0F);
        cowl.setTextureSize(64, 32);

        // Hide all default parts
        bipedHead.showModel = false;
        bipedHeadwear.showModel = false;
        bipedBody.showModel = false;
        bipedRightArm.showModel = false;
        bipedLeftArm.showModel = false;
        bipedRightLeg.showModel = false;
        bipedLeftLeg.showModel = false;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        cowl.rotateAngleX = bipedHead.rotateAngleX;
        cowl.rotateAngleY = bipedHead.rotateAngleY;
        cowl.rotateAngleZ = bipedHead.rotateAngleZ;
        cowl.render(f5);
    }
}
