package am2.client.entity.models;

import am2.common.entity.EntityNatureElemental;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Converted from ModelNatureElemental.bbmodel using tools/export_nature_elemental.py. */
public class ModelNatureElemental extends ModelBase {
    private final ModelRenderer root;
    private final ModelRenderer body;
    private final ModelRenderer head;
    private final ModelRenderer tail;
    private final ModelRenderer leg_front_left;
    private final ModelRenderer leg_front_right;
    private final ModelRenderer leg_back_left;
    private final ModelRenderer leg_back_right;

    public ModelNatureElemental() {
        textureWidth = 128;
        textureHeight = 64;

        root = new ModelRenderer(this);
        root.setRotationPoint(0.0F, 14.0F, 0.0F);
        body = new ModelRenderer(this);
        body.setRotationPoint(0.0F, -5.0F, 0.0F);
        root.addChild(body);
        // body
        addCube(body, 0, 0,
                0.0F, 0.0F, 0.0F,
                -6.0F, -5.0F, -10.0F,
                12, 10, 20, false,
                0.0F, 0.0F, 0.0F);
        // spine_front
        addCube(body, 32, 32,
                0.0F, -4.0F, -5.0F,
                0.0F, -7.0F, -3.0F,
                0, 7, 6, false,
                0.0F, 0.0F, 0.0F);
        // spine_mid
        addCube(body, 32, 32,
                0.0F, -4.0F, 1.0F,
                0.0F, -7.0F, -3.0F,
                0, 7, 6, false,
                0.0F, 0.0F, 0.0F);
        // spine_back
        addCube(body, 32, 32,
                0.0F, -4.0F, 7.0F,
                0.0F, -7.0F, -3.0F,
                0, 7, 6, false,
                0.0F, 0.0F, 0.0F);
        // life_core
        addCube(body, 60, 32,
                0.0F, 1.0F, -10.01F,
                -3.0F, -3.0F, 0.0F,
                6, 6, 0, false,
                0.0F, 0.0F, 0.0F);
        // cube
        addCube(body, 83, 39,
                -5.0F, 8.0F, 7.0F,
                -1.05F, -6.0F, -14.0F,
                0, 7, 17, false,
                0.0F, 0.0F, 0.0F);
        // cube
        addCube(body, 87, 41,
                8.0F, 8.0F, 6.0F,
                -1.95F, -6.0F, -11.0F,
                0, 7, 15, false,
                0.0F, 0.0F, 0.0F);
        head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, -1.0F, -10.0F);
        body.addChild(head);
        // head
        addCube(head, 64, 0,
                0.0F, 0.0F, 0.0F,
                -5.0F, -5.0F, -8.0F,
                10, 9, 10, false,
                0.0F, 0.0F, 0.0F);
        // snout
        addCube(head, 64, 20,
                0.0F, 1.0F, -8.0F,
                -4.0F, -2.0F, -6.0F,
                8, 5, 6, false,
                0.0F, 0.0F, 0.0F);
        // ear_left
        addCube(head, 24, 32,
                3.5F, -4.0F, -4.5F,
                -1.5F, -3.0F, -0.5F,
                3, 4, 1, false,
                0.0F, 0.0F, -0.139626F);
        // ear_right
        addCube(head, 24, 32,
                -3.5F, -4.0F, -4.5F,
                -1.5F, -3.0F, -0.5F,
                3, 4, 1, false,
                0.0F, 0.0F, 0.139626F);
        // cheek_left_thorns
        addCube(head, 48, 32,
                5.0F, 2.0F, -12.9F,
                -2.0F, -3.0F, 0.0F,
                5, 5, 0, false,
                0.0F, -0.314159F, 0.0F);
        // cheek_right_thorns
        addCube(head, 48, 32,
                -5.0F, 2.0F, -12.9F,
                -3.0F, -3.0F, 0.0F,
                5, 5, 0, true,
                0.0F, 0.314159F, 0.0F);
        // cube
        addCube(head, 92, 48,
                0.0F, 10.0F, -2.0F,
                -1.95F, -6.0F, -4.0F,
                0, 7, 8, false,
                0.0F, 1.570796F, 0.0F);

        tail = new ModelRenderer(this);
        tail.setRotationPoint(0.0F, -0.5F, 10.0F);
        body.addChild(tail);
        // tail
        addCube(tail, 0, 32,
                0.0F, 0.0F, 0.0F,
                -1.5F, -1.5F, 0.0F,
                3, 3, 8, false,
                0.20944F, 0.0F, 0.0F);
        // tail_thorns
        addCube(tail, 32, 32,
                0.0F, -0.5F, 2.0F,
                0.0F, -7.0F, 0.0F,
                0, 7, 6, false,
                0.174533F, 0.0F, 0.0F);


        leg_front_left = new ModelRenderer(this);
        leg_front_left.setRotationPoint(3.0F, 0.0F, -5.0F);
        root.addChild(leg_front_left);
        // leg_front_left
        addCube(leg_front_left, 96, 20,
                0.0F, 0.0F, 0.0F,
                -2.0F, 0.0F, -2.0F,
                4, 10, 4, false,
                0.0F, 0.0F, 0.0F);

        leg_front_right = new ModelRenderer(this);
        leg_front_right.setRotationPoint(-3.0F, 0.0F, -5.0F);
        root.addChild(leg_front_right);
        // leg_front_right
        addCube(leg_front_right, 96, 20,
                0.0F, 0.0F, 0.0F,
                -2.0F, 0.0F, -2.0F,
                4, 10, 4, true,
                0.0F, 0.0F, 0.0F);

        leg_back_left = new ModelRenderer(this);
        leg_back_left.setRotationPoint(3.0F, 0.0F, 6.0F);
        root.addChild(leg_back_left);
        // leg_back_left
        addCube(leg_back_left, 96, 20,
                0.0F, 0.0F, 0.0F,
                -2.0F, 0.0F, -2.0F,
                4, 10, 4, false,
                0.0F, 0.0F, 0.0F);

        leg_back_right = new ModelRenderer(this);
        leg_back_right.setRotationPoint(-3.0F, 0.0F, 6.0F);
        root.addChild(leg_back_right);
        // leg_back_right
        addCube(leg_back_right, 96, 20,
                0.0F, 0.0F, 0.0F,
                -2.0F, 0.0F, -2.0F,
                4, 10, 4, true,
                0.0F, 0.0F, 0.0F);

    }

    // END GENERATED GEOMETRY

    private void addCube(ModelRenderer parent, int u, int v, float px, float py, float pz,
                         float x, float y, float z, int width, int height, int depth, boolean mirror,
                         float rx, float ry, float rz) {
        ModelRenderer cube = new ModelRenderer(this, u, v);
        cube.setRotationPoint(px, py, pz);
        cube.mirror = mirror;
        // Zero-thickness foliage planes retain their original box UVs and both faces.
        cube.addBox(x, y, z, width, height, depth);
        cube.rotateAngleX = rx;
        cube.rotateAngleY = ry;
        cube.rotateAngleZ = rz;
        parent.addChild(cube);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        EntityNatureElemental boar = (EntityNatureElemental) entity;
        boolean preparing = boar.isPreparingCharge();
        boolean charging = boar.isCharging();
        float gait = limbSwing * (charging ? 1.1F : 0.6662F);
        float stride = Math.min(limbSwingAmount, 1.0F) * (charging ? 0.95F : 0.7F);
        leg_front_left.rotateAngleX = leg_back_right.rotateAngleX = MathHelper.cos(gait) * stride;
        leg_front_right.rotateAngleX = leg_back_left.rotateAngleX = MathHelper.cos(gait + (float) Math.PI) * stride;
        root.rotationPointY = 14.0F;
        body.rotateAngleX = 0.0F;
        head.rotateAngleX = MathHelper.clamp(headPitch, -20.0F, 25.0F) * 0.017453292F;
        head.rotateAngleY = MathHelper.clamp(netHeadYaw, -35.0F, 35.0F) * 0.017453292F;
        tail.rotateAngleY = MathHelper.sin(ageInTicks * 0.12F) * 0.12F;
        if (preparing) {
            // A lowered head and repeated front-hoof scrape telegraph the upcoming charge.
            head.rotateAngleX = 0.45F;
            head.rotateAngleY = 0.0F;
            leg_front_left.rotateAngleX = Math.max(0.0F, MathHelper.sin(ageInTicks * 0.7F)) * -0.65F;
            leg_front_right.rotateAngleX = leg_back_left.rotateAngleX = leg_back_right.rotateAngleX = 0.0F;
        } else if (charging) {
            head.rotateAngleX = 0.30F;
            head.rotateAngleY = 0.0F;
            root.rotationPointY -= Math.abs(MathHelper.sin(gait)) * stride * 0.5F;
        }
        float partialTicks = MathHelper.clamp(ageInTicks - entity.ticksExisted, 0.0F, 1.0F);
        float progress = boar.getHeadbuttProgress(partialTicks);
        if (!preparing && !charging && progress == 0.0F) {
            float sniff = boar.getSniffAmount(partialTicks);
            // Bend at the neck to bring the snout near the ground; ease in/out of the pose.
            float sniffPitch = 1.05F + MathHelper.sin(ageInTicks * 0.65F) * 0.035F;
            head.rotateAngleX += (sniffPitch - head.rotateAngleX) * sniff;
            head.rotateAngleY += (MathHelper.sin(ageInTicks * 0.18F) * 0.12F - head.rotateAngleY) * sniff;
            body.rotateAngleX = 0.04F * sniff;
        }
        head.rotateAngleX -= MathHelper.sin(progress * (float) Math.PI) * 0.75F;
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        root.render(scale);
    }
}
