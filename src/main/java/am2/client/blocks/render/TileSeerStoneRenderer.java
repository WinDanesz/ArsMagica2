package am2.client.blocks.render;

import am2.common.blocks.BlockSeerStone;
import am2.common.blocks.tileentity.TileEntitySeerStone;
import am2.common.power.PowerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileSeerStoneRenderer extends TileEntitySpecialRenderer<TileEntitySeerStone> {

    private static final float TextureSize = 64 * 10; // 10 frames, 64px each
    private static final float FrameSize = 64; // 64x64 per frame

    private static final ResourceLocation EYE_TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/custom/eye_bw.png");

    public TileSeerStoneRenderer() {
    }

    @Override
    public void render(TileEntitySeerStone tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (tile.ShouldAnimate()) {
            EnumFacing facing = EnumFacing.UP;
            if (tile.getWorld() != null) {
                facing = tile.getWorld().getBlockState(tile.getPos()).getValue(BlockSeerStone.FACING);
            }
            RenderEye(PowerTypes.NEUTRAL, tile.getAnimationIndex(), x, y, z, facing);
        }
    }

    private void RenderEye(PowerTypes powerType, int index, double d, double d1, double d2, EnumFacing meta) {
        bindTexture(EYE_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(d + 0.5, d1 + 0.5, d2 + 0.5);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

        if (powerType == PowerTypes.DARK) {
            GlStateManager.color(1.0f, 0.0f, 0.0f, 1.0f);
        } else if (powerType == PowerTypes.LIGHT) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        } else if (powerType == PowerTypes.NEUTRAL) {
            GlStateManager.color(0.0f, 0.2f, 1.0f, 1.0f);
        }

        renderArsMagicaEffect(tessellator, index, meta);
        tessellator.draw();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        //GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
    }

    private void renderArsMagicaEffect(Tessellator tessellator, int i, EnumFacing meta) {
        float iof = TextureSize / FrameSize;
        float foi = FrameSize / TextureSize;

        float TLX = (i % iof) * foi;
        float BRX = (i % iof) * foi + foi;
        float TLY = (float) (Math.floor(i / iof) * foi);
        float BRY = (float) (Math.floor(i / iof) * foi + foi);

        float f4 = 1.0F;
        float f5 = 0.5F;
        float f6 = 0.25F;

        //tessellator.getBuffer().putBrightness4(15728864, 15728864, 15728864, 15728864);

        //GL11.glRotatef(90, 0.0f, 1.0f, 0f);
        //GL11.glRotatef(90, 0.0f, 1.0f, 0f);

        switch (meta) {
            case DOWN:
                GlStateManager.translate(0.0f, -0.125f, 0.0f);
                break;
            case UP:
                GlStateManager.translate(0.0f, -0.275f, 0.0f);
                break;
            case SOUTH:
                GlStateManager.translate(0.0f, -0.2f, -0.275f);
                break;
            case NORTH:
                GlStateManager.translate(0.0f, -0.2f, 0.275f);
                break;
            case EAST:
                GlStateManager.translate(-0.02f, -0.2f, 0.0f);
                break;
            case WEST:
                GlStateManager.translate(0.02f, -0.2f, 0.0f);
                break;
        }

        GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.rotate(180F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180F, 0.0F, 0.0F, 1.0F);

        tessellator.getBuffer().pos(0.0F - f5, 0.0F - f6, 0.0D).tex(TLX, BRY).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.getBuffer().pos(f4 - f5, 0.0F - f6, 0.0D).tex(BRX, BRY).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.getBuffer().pos(f4 - f5, f4 - f6, 0.0D).tex(BRX, TLY).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.getBuffer().pos(0.0F - f5, f4 - f6, 0.0D).tex(TLX, TLY).normal(0.0F, 1.0F, 0.0F).endVertex();
    }
}
