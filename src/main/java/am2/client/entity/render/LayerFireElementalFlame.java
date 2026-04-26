package am2.client.entity.render;

import am2.common.entity.EntityFireElemental;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Renders animated fire textures on the torso and forearms of the Fire Elemental.
 * <p>
 * Uses {@link net.minecraft.client.model.ModelRenderer#postRender} to position each
 * fire box in the model part's local space so that forearm fire follows arm-swing animation.
 * Coordinates are in model-pixel units scaled by 0.0625 (1 px = 1/16 block), matching
 * the values used in {@code ModelBiped}'s {@code addBox} calls.
 */
@SideOnly(Side.CLIENT)
public class LayerFireElementalFlame implements LayerRenderer<EntityFireElemental> {

    private static final float SCALE  = 0.0625F;
    private static final float MARGIN = 0.03F;

    // Body: addBox(-4, 0, -2, 8, 12, 4); pivot (0, 0, 0)
    private static final float BODY_X1 = -4 * SCALE - MARGIN;
    private static final float BODY_X2 =  4 * SCALE + MARGIN;
    private static final float BODY_Z1 = -2 * SCALE - MARGIN;
    private static final float BODY_Z2 =  2 * SCALE + MARGIN;
    private static final float BODY_Y1 =  0 * SCALE;
    private static final float BODY_Y2 = 12 * SCALE;

    // Right arm: addBox(-3, -2, -2, 4, 12, 4); forearm = box-y 4..10 from pivot
    private static final float ARM_R_X1 = -3 * SCALE - MARGIN;
    private static final float ARM_R_X2 =  1 * SCALE + MARGIN;
    // Left arm:  addBox(-1, -2, -2, 4, 12, 4); forearm same y bounds
    private static final float ARM_L_X1 = -1 * SCALE - MARGIN;
    private static final float ARM_L_X2 =  3 * SCALE + MARGIN;
    // Shared z bounds and forearm y bounds (relative to each arm's pivot)
    private static final float ARM_Z1       = -2 * SCALE - MARGIN;
    private static final float ARM_Z2       =  2 * SCALE + MARGIN;
    private static final float FOREARM_Y1   =  4 * SCALE;  // lower-half start
    private static final float FOREARM_Y2   = 10 * SCALE;  // wrist end

    // Head: addBox(-4, -8, -4, 8, 8, 8); pivot at (0, 0, 0)
    private static final float HEAD_X1 = -4 * SCALE - MARGIN;
    private static final float HEAD_X2 =  4 * SCALE + MARGIN;
    private static final float HEAD_Z1 = -4 * SCALE - MARGIN;
    private static final float HEAD_Z2 =  4 * SCALE + MARGIN;
    private static final float HEAD_Y1 = -8 * SCALE;
    private static final float HEAD_Y2 =  0 * SCALE;

    // Legs: addBox(-2, 0, -2, 4, 12, 4); boot = lower quarter, y 8..12 from pivot
    private static final float LEG_X1    = -2 * SCALE - MARGIN;
    private static final float LEG_X2    =  2 * SCALE + MARGIN;
    private static final float LEG_Z1    = -2 * SCALE - MARGIN;
    private static final float LEG_Z2    =  2 * SCALE + MARGIN;
    private static final float BOOT_Y1   =  8 * SCALE;
    private static final float BOOT_Y2   = 12 * SCALE;

    private final RenderLivingBase<EntityFireElemental> renderer;

    public LayerFireElementalFlame(RenderLivingBase<EntityFireElemental> renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityFireElemental entity, float limbSwing, float limbSwingAmount,
                               float partialTicks, float ageInTicks, float netHeadYaw,
                               float headPitch, float scale) {

        ModelBiped model = (ModelBiped) renderer.getMainModel();

        TextureAtlasSprite fire0 = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/fire_layer_0");
        TextureAtlasSprite fire1 = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/fire_layer_1");

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);

        // Torso fire
        GlStateManager.pushMatrix();
        model.bipedBody.postRender(SCALE);
        renderBoxFire(BODY_X1, BODY_X2, BODY_Z1, BODY_Z2, BODY_Y1, BODY_Y2, fire0, fire1, false);
        GlStateManager.popMatrix();

        // Right forearm fire — postRender applies the current arm-swing rotation
        GlStateManager.pushMatrix();
        model.bipedRightArm.postRender(SCALE);
        renderBoxFire(ARM_R_X1, ARM_R_X2, ARM_Z1, ARM_Z2, FOREARM_Y1, FOREARM_Y2, fire0, fire1, false);
        GlStateManager.popMatrix();

        // Left forearm fire
        GlStateManager.pushMatrix();
        model.bipedLeftArm.postRender(SCALE);
        renderBoxFire(ARM_L_X1, ARM_L_X2, ARM_Z1, ARM_Z2, FOREARM_Y1, FOREARM_Y2, fire0, fire1, false);
        GlStateManager.popMatrix();

        // Right boot fire
        GlStateManager.pushMatrix();
        model.bipedRightLeg.postRender(SCALE);
        renderBoxFire(LEG_X1, LEG_X2, LEG_Z1, LEG_Z2, BOOT_Y1, BOOT_Y2, fire0, fire1, true);
        GlStateManager.popMatrix();

        // Left boot fire
        GlStateManager.pushMatrix();
        model.bipedLeftLeg.postRender(SCALE);
        renderBoxFire(LEG_X1, LEG_X2, LEG_Z1, LEG_Z2, BOOT_Y1, BOOT_Y2, fire0, fire1, true);
        GlStateManager.popMatrix();

        // Head fire (back and sides only, upward)
        GlStateManager.pushMatrix(); 
        model.bipedHead.postRender(SCALE);
        renderBoxFireNoFront(HEAD_X1, HEAD_X2, HEAD_Z1, HEAD_Z2, HEAD_Y1, HEAD_Y2, fire0, fire1, true);
        GlStateManager.popMatrix();

        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }

    private static void renderBoxFire(float x1, float x2, float z1, float z2,
                                       float yMin, float yMax,
                                       TextureAtlasSprite fire0, TextureAtlasSprite fire1,
                                       boolean flipV) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        float u0 = fire0.getMinU(), u1 = fire0.getMaxU();
        float v0 = flipV ? fire0.getMaxV() : fire0.getMinV();
        float v1 = flipV ? fire0.getMinV() : fire0.getMaxV();
        float w0 = fire1.getMinU(), w1 = fire1.getMaxU();
        float t0 = flipV ? fire1.getMaxV() : fire1.getMinV();
        float t1 = flipV ? fire1.getMinV() : fire1.getMaxV();

        // Front (+Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x1, yMax, z2).tex(u0, v0).endVertex();
        buf.pos(x2, yMax, z2).tex(u1, v0).endVertex();
        buf.pos(x2, yMin, z2).tex(u1, v1).endVertex();
        buf.pos(x1, yMin, z2).tex(u0, v1).endVertex();
        tess.draw();

        // Back (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x2, yMax, z1).tex(u0, v0).endVertex();
        buf.pos(x1, yMax, z1).tex(u1, v0).endVertex();
        buf.pos(x1, yMin, z1).tex(u1, v1).endVertex();
        buf.pos(x2, yMin, z1).tex(u0, v1).endVertex();
        tess.draw();

        // Right (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x2, yMax, z2).tex(w0, t0).endVertex();
        buf.pos(x2, yMax, z1).tex(w1, t0).endVertex();
        buf.pos(x2, yMin, z1).tex(w1, t1).endVertex();
        buf.pos(x2, yMin, z2).tex(w0, t1).endVertex();
        tess.draw();

        // Left (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x1, yMax, z1).tex(w0, t0).endVertex();
        buf.pos(x1, yMax, z2).tex(w1, t0).endVertex();
        buf.pos(x1, yMin, z2).tex(w1, t1).endVertex();
        buf.pos(x1, yMin, z1).tex(w0, t1).endVertex();
        tess.draw();
    }

    private static void renderBoxFireNoFront(float x1, float x2, float z1, float z2,
                                              float yMin, float yMax,
                                              TextureAtlasSprite fire0, TextureAtlasSprite fire1,
                                              boolean flipV) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        float u0 = fire0.getMinU(), u1 = fire0.getMaxU();
        float v0 = flipV ? fire0.getMaxV() : fire0.getMinV();
        float v1 = flipV ? fire0.getMinV() : fire0.getMaxV();
        float w0 = fire1.getMinU(), w1 = fire1.getMaxU();
        float t0 = flipV ? fire1.getMaxV() : fire1.getMinV();
        float t1 = flipV ? fire1.getMinV() : fire1.getMaxV();

        // Back (-Z)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x2, yMax, z1).tex(u0, v0).endVertex();
        buf.pos(x1, yMax, z1).tex(u1, v0).endVertex();
        buf.pos(x1, yMin, z1).tex(u1, v1).endVertex();
        buf.pos(x2, yMin, z1).tex(u0, v1).endVertex();
        tess.draw();

        // Front
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x1, yMax, z2).tex(u0, v0).endVertex();
        buf.pos(x2, yMax, z2).tex(u1, v0).endVertex();
        buf.pos(x2, yMin, z2).tex(u1, v1).endVertex();
        buf.pos(x1, yMin, z2).tex(u0, v1).endVertex();
        tess.draw();

        // Right (+X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x2, yMax, z2).tex(w0, t0).endVertex();
        buf.pos(x2, yMax, z1).tex(w1, t0).endVertex();
        buf.pos(x2, yMin, z1).tex(w1, t1).endVertex();
        buf.pos(x2, yMin, z2).tex(w0, t1).endVertex();
        tess.draw();

        // Left (-X)
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buf.pos(x1, yMax, z1).tex(w0, t0).endVertex();
        buf.pos(x1, yMax, z2).tex(w1, t0).endVertex();
        buf.pos(x1, yMin, z2).tex(w1, t1).endVertex();
        buf.pos(x1, yMin, z1).tex(w0, t1).endVertex();
        tess.draw();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
