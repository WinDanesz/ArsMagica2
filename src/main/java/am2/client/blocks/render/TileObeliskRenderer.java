package am2.client.blocks.render;

import am2.common.blocks.BlockObelisk;
import am2.common.blocks.tileentity.TileEntityObelisk;
import am2.common.registry.AMBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TESR for Obelisk - renders the full OBJ model and animated runes overlay.
 * Uses a direct OBJ parser via Minecraft's resource manager (like TileCelestialPrismRenderer).
 */
public class TileObeliskRenderer extends TileEntitySpecialRenderer<TileEntityObelisk> {

    private static final ResourceLocation TEXTURE_BODY = new ResourceLocation("arsmagica2", "textures/blocks/obelisk.png");
    private static final ResourceLocation TEXTURE_BODY_ACTIVE = new ResourceLocation("arsmagica2", "textures/blocks/obelisk_active.png");
    private static final ResourceLocation TEXTURE_TOP = new ResourceLocation("arsmagica2", "textures/blocks/obelisk_top.png");
    private static final ResourceLocation RUNES_TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/custom/obelisk_runes.png");
    private static final ResourceLocation OBJ_RESOURCE = new ResourceLocation("arsmagica2", "models/block/obelisk.obj");

    private static final String MAT_BODY = "m_e7b1baa7-004b-ab35-9787-fca3c6528051";
    private static final String MAT_TOP = "m_11ff8e9f-1165-0c53-d9c3-8b11a1a7a205";

    // Parsed OBJ data
    private final List<float[]> verts = new ArrayList<>();
    private final List<float[]> uvs = new ArrayList<>();
    private final List<float[]> normals = new ArrayList<>();
    // Faces per material: each face is int[][3] = {vertexIdx, uvIdx, normalIdx} (1-based)
    private final Map<String, List<int[][]>> materialFaces = new HashMap<>();
    private boolean loaded = false;

    private void loadOBJ() {
        loaded = true;
        try (
            java.io.InputStream is = Minecraft.getMinecraft().getResourceManager()
                    .getResource(OBJ_RESOURCE).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
        ) {
            String currentMaterial = "none";
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    verts.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                } else if (line.startsWith("vt ")) {
                    String[] p = line.split("\\s+");
                    uvs.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])});
                } else if (line.startsWith("vn ")) {
                    String[] p = line.split("\\s+");
                    normals.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                } else if (line.startsWith("usemtl ")) {
                    currentMaterial = line.substring(7).trim();
                } else if (line.startsWith("f ")) {
                    String[] p = line.split("\\s+");
                    int[][] face = new int[p.length - 1][3];
                    for (int i = 1; i < p.length; i++) {
                        String[] refs = p[i].split("/");
                        face[i - 1][0] = Integer.parseInt(refs[0]);
                        face[i - 1][1] = (refs.length > 1 && !refs[1].isEmpty()) ? Integer.parseInt(refs[1]) : 0;
                        face[i - 1][2] = (refs.length > 2 && !refs[2].isEmpty()) ? Integer.parseInt(refs[2]) : 0;
                    }
                    materialFaces.computeIfAbsent(currentMaterial, k -> new ArrayList<>()).add(face);
                }
            }
        } catch (Exception e) {
            am2.ArsMagica.LOGGER.error("ObeliskRenderer.loadOBJ exception caught: ", e);
        }
    }

    @Override
    public void render(TileEntityObelisk te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!loaded) loadOBJ();
        if (materialFaces.isEmpty()) return;

        boolean hasWorld = te.hasWorld();

        // Skip rendering if the block is in-world but the blockstate doesn't match
        if (hasWorld) {
            if (!te.getWorld().isBlockLoaded(te.getPos(), false)
                    || te.getWorld().getBlockState(te.getPos()).getBlock() != AMBlocks.obelisk)
                return;
        }

        boolean active = hasWorld && te.burnTimeRemaining > 0;

        GlStateManager.pushMatrix();

        GlStateManager.translate(x, y, z);

        // Handle rotation based on facing direction
        if (hasWorld) {
            IBlockState state = te.getWorld().getBlockState(te.getPos());
            EnumFacing facing = state.getValue(BlockObelisk.FACING);
            GlStateManager.translate(0.5, 0, 0.5);
            GlStateManager.rotate(180 - facing.getHorizontalAngle(), 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(-0.5, 0, -0.5);
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);

        // Render body faces
        Minecraft.getMinecraft().renderEngine.bindTexture(active ? TEXTURE_BODY_ACTIVE : TEXTURE_BODY);
        renderFaces(materialFaces.get(MAT_BODY));

        // Render top faces
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE_TOP);
        renderFaces(materialFaces.get(MAT_TOP));

        // Render runes overlay when active
        if (active) {
            renderRunesOverlay(te);
        }

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        GlStateManager.popMatrix();
    }

    private void renderFaces(List<int[][]> faces) {
        if (faces == null) return;
        for (int[][] face : faces) {
            GL11.glBegin(GL11.GL_POLYGON);
            for (int[] ref : face) {
                int vi = ref[0] - 1;
                int ti = ref[1] - 1;
                int ni = ref[2] - 1;
                if (ti >= 0 && ti < uvs.size()) {
                    float[] uv = uvs.get(ti);
                    GL11.glTexCoord2f(uv[0], 1f - uv[1]);
                }
                if (ni >= 0 && ni < normals.size()) {
                    float[] n = normals.get(ni);
                    GL11.glNormal3f(n[0], n[1], n[2]);
                }
                float[] v = verts.get(vi);
                GL11.glVertex3f(v[0], v[1], v[2]);
            }
            GL11.glEnd();
        }
    }

    private void renderRunesOverlay(TileEntityObelisk te) {
        GlStateManager.depthMask(false);

        long worldTime = te.getWorld().getTotalWorldTime();
        float normy = worldTime / 200.0f;

        // Setup texture matrix for UV scrolling
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.scale(2.0f, 2.0f, 1);
        GlStateManager.translate(0, normy, 0);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        Minecraft.getMinecraft().renderEngine.bindTexture(RUNES_TEXTURE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.color(1, 1, 1, 1);

        // Slightly larger to prevent z-fighting
        GlStateManager.translate(0.5, 0.5, 0.5);
        GlStateManager.scale(1.002f, 1.002f, 1.002f);
        GlStateManager.translate(-0.5, -0.5, -0.5);

        // Render all faces with rune texture
        for (List<int[][]> faces : materialFaces.values()) {
            renderFaces(faces);
        }

        // Restore texture matrix
        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.depthMask(true);
    }
}
