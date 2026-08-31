package am2.client.blocks.render;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityManaBattery;
import am2.common.power.PowerTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class TileManaBatteryRenderer extends TileEntitySpecialRenderer<TileEntityManaBattery> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2:textures/blocks/mana_battery.png");

    @Override
    public void render(TileEntityManaBattery te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        
        // Disable lighting for glow effect or keep it on depending on whether battery is empty
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder wr = tessellator.getBuffer();
        
        // Floating effect (only when powered)
        float ticks = ArsMagica.proxy.getLocalPlayer().ticksExisted + partialTicks;
        float bobOffset = -2.0f / 16.0f; // Sit on the base block when empty
        if (te != null && te.getPowerType() != PowerTypes.NONE) {
            bobOffset = (float) Math.sin(ticks * 0.05f) * 0.05f;
        }
        
        GlStateManager.translate(0.0f, bobOffset, 0.0f);

        float lastLightX = OpenGlHelper.lastBrightnessX;
        float lastLightY = OpenGlHelper.lastBrightnessY;
        float fullness = 0f;
        if (te != null && te.getPowerType() != PowerTypes.NONE) {
            fullness = Math.max(0, Math.min(1.0f, (float)te.getClientEnergy() / TileEntityManaBattery.storageCapacity));
            float frameTargetX = Math.max(lastLightX, fullness * 192f * 0.3f);
            float frameTargetY = Math.max(lastLightY, fullness * 192f * 0.3f);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, frameTargetX, frameTargetY);
        }

        wr.begin(7, DefaultVertexFormats.POSITION_TEX);

        // 1) crystal_frame
        // [3.9, 16.9, 3.9] to [12.1, 31.1, 12.1]
        // Texture X=11, Y=8
        drawCube(wr, 3.9f, 16.9f, 3.9f, 12.1f, 31.1f, 12.1f, 11f, 8f, 8f, 14f, false);

        // 2) crystal_frame_inverted
        // [12.1, 31.1, 12.1] to [3.9, 16.9, 3.9]
        // Texture X=19, Y=8 (wait, inverted has inverted UVs, let's just pass a flag)
        drawCube(wr, 3.9f, 16.9f, 3.9f, 12.1f, 31.1f, 12.1f, 11f, 8f, 8f, 14f, true);

        tessellator.draw(); // Draw the frame with partial glow

        // 3) crystal
        // X based on power type
        float crystalTexX = 50f; // EMPTY
        if (te != null) {
            PowerTypes type = te.getPowerType();
            if (type == PowerTypes.LIGHT) crystalTexX = 20f;
            else if (type == PowerTypes.NEUTRAL) crystalTexX = 30f;
            else if (type == PowerTypes.DARK) crystalTexX = 40f;
        }
        
        if (te != null && te.getPowerType() != PowerTypes.NONE) {
            float targetX = Math.max(lastLightX, fullness * 192f);
            float targetY = Math.max(lastLightY, fullness * 192f);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, targetX, targetY);
        }
        
        wr.begin(7, DefaultVertexFormats.POSITION_TEX); // Start new draw call for crystal
        drawCube(wr, 4.0f, 17.0f, 4.0f, 12.0f, 31.0f, 12.0f, crystalTexX, 8f, 8f, 14f, false);
        tessellator.draw();
        
        if (te != null && te.getPowerType() != PowerTypes.NONE) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastLightX, lastLightY);
        }
        
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void drawCube(BufferBuilder wr, float x1, float y1, float z1, float x2, float y2, float z2, float u, float v, float tw, float thSide, boolean invert) {
        float minX = x1 / 16f; float minY = y1 / 16f; float minZ = z1 / 16f;
        float maxX = x2 / 16f; float maxY = y2 / 16f; float maxZ = z2 / 16f;
        
        float u1 = u / 64f; float u2 = (u + tw) / 64f;
        float vTop1 = v / 64f; float vTop2 = (v + tw) / 64f; // Top face is tw * tw
        float vSide1 = vTop2; float vSide2 = (v + tw + thSide) / 64f; // Side face is tw * thSide
        float vBot1 = vSide2; float vBot2 = (v + tw + thSide + tw) / 64f; // Bottom face is tw * tw
        
        if (!invert) {
            // NORTH
            wr.pos(minX, maxY, minZ).tex(u2, vSide1).endVertex();
            wr.pos(maxX, maxY, minZ).tex(u1, vSide1).endVertex();
            wr.pos(maxX, minY, minZ).tex(u1, vSide2).endVertex();
            wr.pos(minX, minY, minZ).tex(u2, vSide2).endVertex();
            // SOUTH
            wr.pos(minX, minY, maxZ).tex(u1, vSide2).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u2, vSide2).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u2, vSide1).endVertex();
            wr.pos(minX, maxY, maxZ).tex(u1, vSide1).endVertex();
            // WEST
            wr.pos(minX, minY, minZ).tex(u1, vSide2).endVertex();
            wr.pos(minX, minY, maxZ).tex(u2, vSide2).endVertex();
            wr.pos(minX, maxY, maxZ).tex(u2, vSide1).endVertex();
            wr.pos(minX, maxY, minZ).tex(u1, vSide1).endVertex();
            // EAST
            wr.pos(maxX, maxY, minZ).tex(u2, vSide1).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u1, vSide1).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u1, vSide2).endVertex();
            wr.pos(maxX, minY, minZ).tex(u2, vSide2).endVertex();
            // UP
            wr.pos(minX, maxY, minZ).tex(u1, vTop1).endVertex();
            wr.pos(minX, maxY, maxZ).tex(u1, vTop2).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u2, vTop2).endVertex();
            wr.pos(maxX, maxY, minZ).tex(u2, vTop1).endVertex();
            // DOWN
            wr.pos(minX, minY, minZ).tex(u1, vBot1).endVertex();
            wr.pos(maxX, minY, minZ).tex(u2, vBot1).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u2, vBot2).endVertex();
            wr.pos(minX, minY, maxZ).tex(u1, vBot2).endVertex();
        } else {
            // Inverted means normals are pointing inside, and UVs are mapped differently
            // Looking at the blockbench export for inverted frame:
            // "north": {"uv": [2.75, 7.5, 4.75, 4]} -> this means u1=11, v1=30, u2=19, v2=16
            // "up": {"uv": [4.75, 7.5, 2.75, 9.5]} -> u1=19, v1=30, u2=11, v2=38
            
            // NORTH
            wr.pos(minX, minY, minZ).tex(u2, vSide1).endVertex();
            wr.pos(maxX, minY, minZ).tex(u1, vSide1).endVertex();
            wr.pos(maxX, maxY, minZ).tex(u1, vSide2).endVertex();
            wr.pos(minX, maxY, minZ).tex(u2, vSide2).endVertex();
            // SOUTH
            wr.pos(minX, maxY, maxZ).tex(u1, vSide2).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u2, vSide2).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u2, vSide1).endVertex();
            wr.pos(minX, minY, maxZ).tex(u1, vSide1).endVertex();
            // WEST
            wr.pos(minX, maxY, minZ).tex(u1, vSide2).endVertex();
            wr.pos(minX, maxY, maxZ).tex(u2, vSide2).endVertex();
            wr.pos(minX, minY, maxZ).tex(u2, vSide1).endVertex();
            wr.pos(minX, minY, minZ).tex(u1, vSide1).endVertex();
            // EAST
            wr.pos(maxX, minY, minZ).tex(u2, vSide1).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u1, vSide1).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u1, vSide2).endVertex();
            wr.pos(maxX, maxY, minZ).tex(u2, vSide2).endVertex();
            // UP
            wr.pos(minX, maxY, minZ).tex(u2, vBot1).endVertex();
            wr.pos(maxX, maxY, minZ).tex(u1, vBot1).endVertex();
            wr.pos(maxX, maxY, maxZ).tex(u1, vBot2).endVertex();
            wr.pos(minX, maxY, maxZ).tex(u2, vBot2).endVertex();
            // DOWN
            wr.pos(minX, minY, minZ).tex(u2, vSide2).endVertex();
            wr.pos(minX, minY, maxZ).tex(u2, vTop2).endVertex();
            wr.pos(maxX, minY, maxZ).tex(u1, vTop2).endVertex();
            wr.pos(maxX, minY, minZ).tex(u1, vSide2).endVertex();
        }
    }
}
