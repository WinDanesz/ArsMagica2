package am2.common.utils;

import am2.api.math.AMVector3;
import am2.common.LogHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;
import java.util.Random;

public class RenderUtils {

    private static Random rand = new Random();


    public static void drawBox(float minX, float minZ, float maxX, float maxZ, float zLevel, float minU, float minV, float maxU, float maxV) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder buffer = t.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(minX, minZ + maxZ, zLevel).tex(minU, maxV).endVertex();
        buffer.pos(minX + maxX, minZ + maxZ, zLevel).tex(maxU, maxV).endVertex();
        buffer.pos(minX + maxX, minZ, zLevel).tex(maxU, minV).endVertex();
        buffer.pos(minX, minZ, zLevel).tex(minU, minV).endVertex();
        t.draw();
    }

    public static float getRed(int color) {
        return ((color & 0xFF0000) >> 16) / 255.0f;
    }

    public static float getGreen(int color) {
        return ((color & 0x00FF00) >> 8) / 255.0f;
    }

    public static float getBlue(int color) {
        return (color & 0x0000FF) / 255.0f;
    }

    public static void color(int color) {
        GL11.glColor4f(getRed(color), getGreen(color), getBlue(color), 0.5F);
    }

    public static int getColor(float r, float g, float b) {
        int red = (int) (r * 255f) << 16;
        int green = (int) (g * 255f) << 8;
        int blue = (int) (b * 255f);
        return red + green + blue;
    }

    public static void line2d(float xStart, float yStart, float xEnd, float yEnd, float zLevel, int color) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableDepth();
        GL11.glLineWidth(1f);
        GL11.glColor3d(((color & 0xFF0000) >> 16) / 255.0d, ((color & 0x00FF00) >> 8) / 255.0f, (color & 0x0000FF) / 255.0f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(xStart, yStart, zLevel);
        GL11.glVertex3f(xEnd, yEnd, zLevel);
        GL11.glEnd();
        GL11.glColor3d(1.0f, 1.0f, 1.0f);
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void lineThick2d(float xStart, float yStart, float xEnd, float yEnd, float zLevel, int color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GlStateManager.enableDepth();
        GL11.glLineWidth(4f);
        GL11.glColor3d(((color & 0xFF0000) >> 16) / 255.0d, ((color & 0x00FF00) >> 8) / 255.0f, (color & 0x0000FF) / 255.0f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(xStart, yStart, zLevel);
        GL11.glVertex3f(xEnd, yEnd, zLevel);
        GL11.glEnd();
        GL11.glColor3d(1.0f, 1.0f, 1.0f);
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    public static void fractalLine2df(float xStart, float yStart, float xEnd, float yEnd, float zLevel, int color, float displace, float fractalDetail) {
        if (displace < fractalDetail) {
            line2d(xStart, yStart, xEnd, yEnd, zLevel, color);
        } else {
            int mid_x = (int) ((xEnd + xStart) / 2);
            int mid_y = (int) ((yEnd + yStart) / 2);
            mid_x += (rand.nextFloat() - 0.5) * displace;
            mid_y += (rand.nextFloat() - 0.5) * displace;
            fractalLine2df(xStart, yStart, mid_x, mid_y, zLevel, color, displace / 2f, fractalDetail);
            fractalLine2df(xEnd, yEnd, mid_x, mid_y, zLevel, color, displace / 2f, fractalDetail);
        }
    }

    public static void fractalLine2dd(double xStart, double yStart, double xEnd, double yEnd, float zLevel, int color, float displace, float fractalDetail) {
        fractalLine2df((float) xStart, (float) yStart, (float) xEnd, (float) yEnd, (float) zLevel, color, displace, fractalDetail);
    }

    public static void drawTextInWorldAtOffset(String text, double x, double y, double z) {
        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;

        final int charHeight = 9;
        final int lineSpace = 2;
        String[] texts = text.split("\n");
        int textHalfWidth = Arrays.stream(texts).mapToInt(t -> fontrenderer.getStringWidth(t) / 2).max().orElse(0);
        int lines = texts.length;
        if (lines < 1) return;

        int dy = -charHeight * (lines - 1);
        if(lines > 1) {
            dy -= lineSpace * (lines - 2);
        }

        float f = 1.6F;
        float f1 = 0.016666668F * f * 0.5F;
//        GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_POLYGON_BIT| GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(renderManager.playerViewX, Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-f1, -f1, f1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GlStateManager.depthMask(false);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int Y = charHeight * lines + lineSpace * (lines - 1);
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_COLOR);
        tessellator.getBuffer().pos(-textHalfWidth - 1, -1 + dy, 0.0D).color(0.0F, 0.0F, 0.0F, 0.75F).endVertex();
        tessellator.getBuffer().pos(-textHalfWidth - 1, -1 + Y + dy, 0.0D).color(0.0F, 0.0F, 0.0F, 0.75F).endVertex();
        tessellator.getBuffer().pos(textHalfWidth + 1, -1 + Y + dy, 0.0D).color(0.0F, 0.0F, 0.0F, 0.75F).endVertex();
        tessellator.getBuffer().pos(textHalfWidth + 1, -1 + dy, 0.0D).color(0.0F, 0.0F, 0.0F, 0.75F).endVertex();
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        for(String t: texts) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.depthMask(false);
            fontrenderer.drawString(t, -fontrenderer.getStringWidth(t) / 2, dy, 0x20FFFFFF);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GlStateManager.depthMask(true);
            fontrenderer.drawString(t, -fontrenderer.getStringWidth(t) / 2, dy, 0xFFFFFFFF);
            dy += charHeight + lineSpace;
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public static void RenderRotatedModelGroup(TileEntity te, IBakedModel model, IBlockState defaultState, AMVector3 rotation) {
        GlStateManager.pushMatrix();

        GlStateManager.rotate(rotation.x, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(rotation.y, 1.0f, 1.0f, 0.0f);
        GlStateManager.rotate(rotation.z, 1.0f, 0.0f, 1.0f);
        renderBlockModel(te, model, defaultState);
        GlStateManager.popMatrix();
    }

    public static void renderBlockModel(TileEntity te, IBakedModel model, IBlockState defaultState) {
        try {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
            Tessellator t = Tessellator.getInstance();
            BufferBuilder builder = t.getBuffer();
            builder.begin(7, DefaultVertexFormats.BLOCK);
            World world = te.getWorld();
            if (world == null)
                world = Minecraft.getMinecraft().world;
            IBlockState state = world.getBlockState(te.getPos());
            if (state.getBlock() != defaultState.getBlock())
                state = defaultState;
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, model, state, te.getPos(), builder, true);
            t.draw();
            GlStateManager.popMatrix();
        } catch (Throwable t) {
            LogHelper.error("Error rendering block model: " + t.toString());
        }
    }

}
