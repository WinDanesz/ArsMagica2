package am2.client.blocks.render;

import am2.client.utils.DirectOBJModel;
import am2.common.blocks.BlockCrystalMarker;
import am2.common.blocks.tileentity.TileEntityCrystalMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileCrystalMarkerRenderer extends TileEntitySpecialRenderer<TileEntityCrystalMarker> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/custom/crystalmarker.png");
    private static final ResourceLocation OBJ_RESOURCE = new ResourceLocation("arsmagica2", "models/block/crystal_marker.obj");

    private static final DirectOBJModel model = new DirectOBJModel(OBJ_RESOURCE);

    public TileCrystalMarkerRenderer() {
    }

    @Override
    public void render(TileEntityCrystalMarker tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!model.isLoaded()) {
            model.load();
        }
        if (model.isEmpty()) return;

        EnumFacing facing = tileentity.getFacing();

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();//(GL11.GL_LIGHTING_BIT);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableTexture2D();
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        if (tileentity.getPos() != BlockPos.ORIGIN) {
            switch (facing) {
                case UP: //Bottom, Inventory is above
                    GL11.glTranslated(x + 0.5, y + 1.0 + tileentity.GetConnectedBoundingBox().minY, z + 0.5);
                    GL11.glRotated(90, 1, 0, 0);
                    break;
                case DOWN: //Top, Inventory is below
                    GL11.glTranslated(x + 0.5, y - (1.0 - tileentity.GetConnectedBoundingBox().maxY), z + 0.5);
                    GL11.glRotated(270, 1, 0, 0);
                    break;
                case SOUTH: //North, Inventory is to the south
                    GL11.glTranslated(x + 0.5, y + 0.5, z + 1.0 + (1.0 - tileentity.GetConnectedBoundingBox().maxZ));
                    GL11.glRotated(180, 0, 1, 0);
                    break;
                case NORTH: //South, Inventory is to the north
                    GL11.glTranslated(x + 0.5, y + 0.5, z - tileentity.GetConnectedBoundingBox().minZ);
                    break;
                case EAST: //West, Inventory is to the east
                    GL11.glTranslated(x + 1 + tileentity.GetConnectedBoundingBox().minX, y + 0.5, z + 0.5);
                    GL11.glRotated(270, 0, 1, 0);
                    break;
                case WEST: //East, Inventory is to the west
                    GL11.glTranslated(x - (1.0 - tileentity.GetConnectedBoundingBox().maxX), y + 0.5, z + 0.5);
                    GL11.glRotated(90, 0, 1, 0);
                    break;
            }

            GL11.glScalef(0.5f, 0.5f, 0.5f);
        }
        else {
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            GL11.glRotated(180, 0, 1, 0);
        }

        int blockType = tileentity.getMarkerType();

        // Apply color based on marker type
        switch (blockType) {
            case BlockCrystalMarker.META_IN:
                GL11.glColor3f(0.94f, 0.69f, 0.01f); //yellow
                break;
            case BlockCrystalMarker.META_OUT:
                GL11.glColor3f(0.10f, 0.10f, 0.88f); //blue
                break;
            case BlockCrystalMarker.META_LIKE_EXPORT:
                GL11.glColor3f(0.10f, 0.65f, 0.0f); //green
                break;
            case BlockCrystalMarker.META_SET_EXPORT:
                GL11.glColor3f(0.085f, 0.72f, 0.88f); //light blue
                break;
            case BlockCrystalMarker.META_REGULATE_EXPORT:
                GL11.glColor3f(0.56f, 0.08f, 0.66f); //purple
                break;
            case BlockCrystalMarker.META_REGULATE_MULTI:
                GL11.glColor3f(0.92f, 0.61f, 0.3f); //orange
                break;
            case BlockCrystalMarker.META_SET_IMPORT:
                GL11.glColor3f(1.0f, 0.0f, 0.0f); //red
                break;
            case BlockCrystalMarker.META_SPELL_EXPORT:
                GL11.glColor3f(0.0f, 0.5f, 1.0f); //cyan
                break;
            default:
                GL11.glColor3f(1.0f, 1.0f, 1.0f); //white (for META_FINAL_DEST and any other types)
                break;
        }

        model.render();

        // Reset color to white to avoid affecting subsequent renders
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GlStateManager.popAttrib();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }

}
