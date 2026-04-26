package am2.client.blocks.render;

import am2.common.blocks.BlockPhaseShift;
import am2.common.blocks.tileentity.TileEntityPhaseShift;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.nio.FloatBuffer;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class TilePhaseShiftRenderer extends TileEntitySpecialRenderer<TileEntityPhaseShift> {

    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    @Override
    public void render(TileEntityPhaseShift te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.getOriginalState() == null) return;

        // Only render faces bordering solid blocks (skip air and adjacent phase shift blocks)
        boolean[] renderFace = new boolean[6];
        BlockPos pos = te.getPos();
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos adj = pos.offset(face);
            IBlockState adjState = te.getWorld().getBlockState(adj);
            renderFace[face.getIndex()] = !adjState.getBlock().isAir(adjState, te.getWorld(), adj)
                    && !(adjState.getBlock() instanceof BlockPhaseShift);
        }

        // Check if any face should render at all
        boolean anyFace = false;
        for (boolean b : renderFace) if (b) { anyFace = true; break; }
        if (!anyFace) return;

        GlStateManager.disableLighting();
        RANDOM.setSeed(31100L);
        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);

        double d0 = x * x + y * y + z * z;
        int passes = getPasses(d0);
        boolean fogSet = false;

        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-1.0f, -1.0f);

        for (int j = 0; j < passes; ++j) {
            GlStateManager.pushMatrix();
            float f1 = 2.0F / (float) (18 - j);

            if (j == 0) {
                this.bindTexture(END_SKY_TEXTURE);
                f1 = 0.15F;
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (j >= 1) {
                this.bindTexture(END_PORTAL_TEXTURE);
                fogSet = true;
                Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
            }

            if (j == 1) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
            GlStateManager.texGen(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            GlStateManager.texGen(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
            GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
            GlStateManager.popMatrix();

            GlStateManager.matrixMode(5890);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.5F, 0.5F, 0.0F);
            GlStateManager.scale(0.5F, 0.5F, 1.0F);
            float f2 = (float) (j + 1);
            GlStateManager.translate(17.0F / f2, (2.0F + f2 / 1.5F) * ((float) Minecraft.getSystemTime() % 800000.0F / 800000.0F), 0.0F);
            GlStateManager.rotate((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.scale(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
            GlStateManager.multMatrix(PROJECTION);
            GlStateManager.multMatrix(MODELVIEW);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buf = tessellator.getBuffer();
            buf.begin(7, DefaultVertexFormats.POSITION_COLOR);

            float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
            float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
            float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;

            // Green tint for bottom, purple tint for top
            float downR = f3 * 0.3F, downG = f4 * 1.4F, downB = f5 * 0.3F;
            float upR = f3 * 1.2F, upG = f4 * 0.3F, upB = f5 * 1.4F;

            // Render inward-facing quads (reverse winding from vanilla End Portal)
            // DOWN face (visible looking down from inside) - green tint
            if (renderFace[EnumFacing.DOWN.getIndex()]) {
                buf.pos(x, y, z).color(downR, downG, downB, 1.0F).endVertex();
                buf.pos(x, y, z + 1.0D).color(downR, downG, downB, 1.0F).endVertex();
                buf.pos(x + 1.0D, y, z + 1.0D).color(downR, downG, downB, 1.0F).endVertex();
                buf.pos(x + 1.0D, y, z).color(downR, downG, downB, 1.0F).endVertex();
            }

            // UP face (visible looking up from inside) - purple tint
            if (renderFace[EnumFacing.UP.getIndex()]) {
                buf.pos(x, y + 1.0D, z).color(upR, upG, upB, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z).color(upR, upG, upB, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(upR, upG, upB, 1.0F).endVertex();
                buf.pos(x, y + 1.0D, z + 1.0D).color(upR, upG, upB, 1.0F).endVertex();
            }

            // NORTH face (visible looking north from inside)
            if (renderFace[EnumFacing.NORTH.getIndex()]) {
                buf.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            // SOUTH face (visible looking south from inside)
            if (renderFace[EnumFacing.SOUTH.getIndex()]) {
                buf.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
            }

            // WEST face (visible looking west from inside)
            if (renderFace[EnumFacing.WEST.getIndex()]) {
                buf.pos(x, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x, y, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            // EAST face (visible looking east from inside)
            if (renderFace[EnumFacing.EAST.getIndex()]) {
                buf.pos(x + 1.0D, y, z).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(f3, f4, f5, 1.0F).endVertex();
                buf.pos(x + 1.0D, y + 1.0D, z).color(f3, f4, f5, 1.0F).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
            this.bindTexture(END_SKY_TEXTURE);
        }

        GlStateManager.doPolygonOffset(0.0f, 0.0f);
        GlStateManager.disablePolygonOffset();

        GlStateManager.disableBlend();
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
        GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
        GlStateManager.enableLighting();

        if (fogSet) {
            Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileEntityPhaseShift te) {
        return false;
    }

    private int getPasses(double distSq) {
        if (distSq > 36864.0D) return 1;
        if (distSq > 25600.0D) return 3;
        if (distSq > 16384.0D) return 5;
        if (distSq > 9216.0D) return 7;
        if (distSq > 4096.0D) return 9;
        if (distSq > 1024.0D) return 11;
        if (distSq > 576.0D) return 13;
        if (distSq > 256.0D) return 14;
        return 15;
    }

    private FloatBuffer getBuffer(float a, float b, float c, float d) {
        this.buffer.clear();
        this.buffer.put(a).put(b).put(c).put(d);
        this.buffer.flip();
        return this.buffer;
    }
}
