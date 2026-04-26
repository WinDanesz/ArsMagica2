package am2.client.blocks.render;

import am2.client.models.ModelAstralBarrier;
import am2.common.blocks.tileentity.TileEntityAstralBarrier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TileAstralBarrierRenderer extends TileEntitySpecialRenderer<TileEntityAstralBarrier> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2:textures/blocks/custom/astral_barrier.png");

    private ModelAstralBarrier model;

    public TileAstralBarrierRenderer() {
        model = new ModelAstralBarrier();
    }

    @Override
    public void render(TileEntityAstralBarrier tile, double d, double d1, double d2, float f, int destroyStage, float alpha) {
        int i = 0;

        if (destroyStage != -10 && tile.getWorld() != null) {
            i = tile.getBlockMetadata();
        }
        int j = 0;

        if (i == 0) {
            j = 0;
        }

        if (i == 1) {
            j = 90;
        }

        if (i == 2) {
            j = 180;
        }

        if (i == 3) {
            j = 270;
        }

        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        bindTexture(TEXTURE);
        GL11.glTranslatef((float) d + 0.5F, (float) d1 + 1.5F, (float) d2 + 0.5F); //size
        GL11.glRotatef(j, 0.0F, 1.0F, 0.0F); //rotate based on metadata
        GL11.glScalef(1.0F, -1F, -1F); //if you read this comment out this line and you can see what happens
        model.renderModel(0.0625F); //renders and yes 0.0625 is a random number
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

}
