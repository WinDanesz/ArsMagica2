package am2.client.models;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

/**
 * Custom mage robe armor model with full-length sleeves.
 * Uses the standard armor biped with inflate=1 but replaces the arms
 * with 12-pixel-tall versions so the sleeves cover the full arm.
 */
public class ModelMageRobe extends ModelBiped {

    public ModelMageRobe() {
        // inflate=0.5 for a snug fit above the skin layer
        super(0.5F);

        // Replace arms: standard arm is 4x12x4 starting at y=-2 relative to rotation point.
        // Vanilla helmet-layer only renders the top portion because the texture is transparent below.
        // By using the full 12-height box, the arm renders with full length.
        // The texture (40,16) maps to the arm area of the armor texture.

        // Right arm
        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, 0.5F);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);

        // Left arm (mirrored)
        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.mirror = true;
        bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, 0.5F);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
    }
}
