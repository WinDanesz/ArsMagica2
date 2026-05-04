package am2.client.blocks.render;

import am2.common.blocks.tileentity.TileEntityCelestialPrism;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * TESR for Celestial Prism - renders the OBJ crystal model.
 * The block uses the scaledown invisible model in the blockstate,
 * and this TESR renders the actual visual using the 1.7.10-era OBJ.
 * Uses a direct OBJ parser via Minecraft's resource manager to avoid
 * depending on Forge's internal OBJModel API.
 */
public class TileCelestialPrismRenderer extends TileEntitySpecialRenderer<TileEntityCelestialPrism> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/celestial_prism.png");
    private static final ResourceLocation OBJ_RESOURCE = new ResourceLocation("arsmagica2", "models/block/celestial_prism.obj");

    // Parsed OBJ data: vertices, texture coords, and face index references [vertex, texcoord] (1-based)
    private final List<float[]> verts = new ArrayList<>();
    private final List<float[]> uvs = new ArrayList<>();
    private final List<int[][]> faces = new ArrayList<>();
    private boolean loaded = false;

    private void loadOBJ() {
        loaded = true;
        try (
            java.io.InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(OBJ_RESOURCE).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    verts.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                } else if (line.startsWith("vt ")) {
                    String[] p = line.split("\\s+");
                    uvs.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                } else if (line.startsWith("f ")) {
                    String[] p = line.trim().split("\\s+");
                    int[][] face = new int[p.length - 1][2];
                    for (int i = 1; i < p.length; i++) {
                        String[] refs = p[i].split("/");
                        face[i - 1][0] = Integer.parseInt(refs[0]);
                        face[i - 1][1] = (refs.length > 1 && !refs[1].isEmpty()) ? Integer.parseInt(refs[1]) : 0;
                    }
                    faces.add(face);
                }
            }
        } catch (Exception e) {
            am2.ArsMagica.LOGGER.error("CelestialPrismRenderer.loadOBJ exception caught: ", e);
        }
    }

    @Override
    public void render(TileEntityCelestialPrism te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!loaded) {
            loadOBJ();
        }
        if (faces.isEmpty()) return;

        // Use world time for animation; render statically in inventory (no world)
        float brightness;
        float time;
        if (te.getWorld() != null) {
            time = (te.getWorld().getTotalWorldTime() + partialTicks) / 20f;
            brightness = 0.90f + 0.10f * (float) Math.sin(time * Math.PI * 0.6);
        } else {
            brightness = 1.0f;
        }

        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5, y, z + 0.5);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(brightness, brightness, brightness, 0.95f);

        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        for (int[][] face : faces) {
            GL11.glBegin(GL11.GL_POLYGON);
            for (int[] ref : face) {
                int vi = ref[0] - 1;
                int ti = ref[1] - 1;
                if (ti >= 0 && ti < uvs.size()) {
                    float[] uv = uvs.get(ti);
                    GL11.glTexCoord2f(uv[0], 1f - uv[1]);
                }
                float[] v = verts.get(vi);
                GL11.glVertex3f(v[0], v[1], v[2]);
            }
            GL11.glEnd();
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        if (wasBlendEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        GlStateManager.popMatrix();
    }
}
