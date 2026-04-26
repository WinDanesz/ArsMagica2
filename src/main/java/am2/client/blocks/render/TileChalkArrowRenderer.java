package am2.client.blocks.render;

import am2.ArsMagica;
import am2.common.blocks.BlockChalkArrow;
import am2.common.blocks.tileentity.TileEntityChalkArrow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class TileChalkArrowRenderer extends TileEntitySpecialRenderer<TileEntityChalkArrow> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(ArsMagica.MODID, "blocks/chalk_arrow");

    @Override
    public void render(TileEntityChalkArrow te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getWorld() == null) return;
        IBlockState state = te.getWorld().getBlockState(te.getPos());
        if (!(state.getBlock() instanceof BlockChalkArrow)) return;
        if (!state.getValue(BlockChalkArrow.ON_WALL)) return;

        // The wall this arrow is stuck to: FACING points inward (toward the wall block).
        // The outward direction (toward the viewer) is the opposite.
        EnumFacing inward = state.getValue(BlockChalkArrow.FACING);
        EnumFacing outward = inward.getOpposite();

        // Dye color
        EnumDyeColor dye = EnumDyeColor.byMetadata(te.getColorIndex());
        float[] c = dye.getColorComponentValues();

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TEXTURE.toString());
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();
        float maxU = sprite.getMaxU();
        float maxV = sprite.getMaxV();

        // How many 90-degree CW steps to rotate the arrow texture so it points in arrowFacing.
        // The base texture (0 rotation) points NORTH (up on screen when looking south).
        // We need the arrow to point in arrowFacing as seen from outside the wall.
        // The rotation is the horizontal angle from NORTH to arrowFacing (CW).
        EnumFacing arrowFacing = te.getArrowFacing();
        int arrowSteps = (arrowFacing.getHorizontalIndex() - EnumFacing.NORTH.getHorizontalIndex() + 4) % 4;

        // Rotate UV corners by arrowSteps * 90 degrees CW
        float[][] corners = uvRotate(minU, minV, maxU, maxV, arrowSteps);
        // corners[i] = {u, v} for vertex i in order: BL, BR, TR, TL (when looking at the face from outside)

        // Y rotation to align the wall face with the +Z axis (south face), then render a flat quad at Z=0.03
        // outward NORTH -> yRot=0, EAST -> yRot=270, SOUTH -> yRot=180, WEST -> yRot=90
        int yRot;
        switch (outward) {
            case NORTH: yRot = 0;   break;
            case EAST:  yRot = 270; break;
            case SOUTH: yRot = 180; break;
            default:    yRot = 90;  break; // WEST
        }

        int light = te.getWorld().getCombinedLight(te.getPos(), 0);
        int lj = light >> 16 & 0xFFFF;
        int lk = light & 0xFFFF;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        GlStateManager.rotate(yRot, 0, 1, 0);
        // Translate to the face corner; z=0.5 pushes the quad out to the block face (+Z = south face)
        GlStateManager.translate(-0.5, -0.5, 0.5);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableCull();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        RenderHelper.disableStandardItemLighting();

        float depth = 0.032f; // just outside the wall face

        builder.begin(7, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
        // Quad vertices looking from +Z toward -Z (south face, visible from south):
        // BL=(0,0,depth), BR=(1,0,depth), TR=(1,1,depth), TL=(0,1,depth)
        builder.pos(0, 0, depth).tex(corners[0][0], corners[0][1]).lightmap(lj, lk).color(c[0], c[1], c[2], 1.0f).endVertex();
        builder.pos(1, 0, depth).tex(corners[1][0], corners[1][1]).lightmap(lj, lk).color(c[0], c[1], c[2], 1.0f).endVertex();
        builder.pos(1, 1, depth).tex(corners[2][0], corners[2][1]).lightmap(lj, lk).color(c[0], c[1], c[2], 1.0f).endVertex();
        builder.pos(0, 1, depth).tex(corners[3][0], corners[3][1]).lightmap(lj, lk).color(c[0], c[1], c[2], 1.0f).endVertex();
        tessellator.draw();

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    /**
     * Rotate UV coordinates for a quad by {@code steps} * 90 degrees clockwise.
     * Input corners order: BL, BR, TR, TL.
     * Returns rotated corners in the same order.
     */
    private float[][] uvRotate(float minU, float minV, float maxU, float maxV, int steps) {
        // corners: BL=(minU,maxV), BR=(maxU,maxV), TR=(maxU,minV), TL=(minU,minV)
        float[][] corners = {
            {minU, maxV}, // BL
            {maxU, maxV}, // BR
            {maxU, minV}, // TR
            {minU, minV}  // TL
        };
        steps = ((steps % 4) + 4) % 4;
        float[][] rotated = new float[4][2];
        for (int i = 0; i < 4; i++) {
            rotated[i] = corners[(i + steps) % 4];
        }
        return rotated;
    }
}
