package am2.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DirectOBJModel {
    // Parsed OBJ data: vertices, texture coords, and face index references [vertex, texcoord] (1-based)
    private final List<float[]> verts = new ArrayList<>();
    private final List<float[]> uvs = new ArrayList<>();
    private final List<int[][]> faces = new ArrayList<>();
    private boolean loaded = false;
    private ResourceLocation resourceLocation;

    public DirectOBJModel(ResourceLocation resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public void load() {
        load(false, false);
    }

    public void load(boolean invertU, boolean invertV) {
        if(loaded) return;
        loaded = true;
        try (
                java.io.InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    verts.add(new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2]), Float.parseFloat(p[3])});
                }
                else if (line.startsWith("vt ")) {
                    String[] p = line.split("\\s+");
                    float[] uv = new float[]{Float.parseFloat(p[1]), Float.parseFloat(p[2])};
                    if(invertU) uv[0] = 1.0F - uv[0];
                    if(invertV) uv[1] = 1.0F - uv[1];
                    uvs.add(uv);
                }
                else if (line.startsWith("f ")) {
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
            am2.ArsMagica.LOGGER.error(resourceLocation.toString() + " failed to load: ", e);
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public boolean isEmpty() {
        return faces.isEmpty();
    }

    public void render() {
        for (int[][] face : faces) {
            GL11.glBegin(GL11.GL_POLYGON);
            for (int[] ref : face) {
                int vi = ref[0] - 1;
                int ti = ref[1] - 1;
                if (ti >= 0 && ti < uvs.size()) {
                    float[] uv = uvs.get(ti);
                    GL11.glTexCoord2f(uv[0], uv[1]);
                }
                float[] v = verts.get(vi);
                GL11.glVertex3f(v[0], v[1], v[2]);
            }
            GL11.glEnd();
        }
    }
}
