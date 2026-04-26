package am2.client.blocks.render;

import am2.common.blocks.BlockCrystalMarker;
import am2.common.blocks.tileentity.TileEntityCrystalMarker;
import am2.common.registry.AMBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.opengl.GL11;

public class TileCrystalMarkerRenderer extends TileEntitySpecialRenderer<TileEntityCrystalMarker> {
    private IModel model;
    private IBakedModel bakedModel;

    private IBakedModel getBakedModel() {
        if (bakedModel == null) {
            try {
                model = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/crystal_marker.obj"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
        }
        return bakedModel;
    }


    public TileCrystalMarkerRenderer() {
    }

    @Override
    public void render(TileEntityCrystalMarker tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EnumFacing facing = EnumFacing.UP;

        if (tileentity.getWorld() != null) {
            facing = tileentity.getFacing();
            //facing = tileentity.getWorld().getBlockState(tileentity.getPos()).getValue(BlockCrystalMarker.FACING);
        }

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();//(GL11.GL_LIGHTING_BIT);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.disableCull();
        RenderHelper.disableStandardItemLighting();

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
        } else {
            GL11.glTranslated(0.5, 0.5, 0.5);
            GL11.glScalef(1.4f, 1.4f, 1.4f);
            GL11.glRotated(180, 0, 1, 0);
        }

        int blockType = 0;

        if (tileentity.getWorld() != null && destroyStage != -10) {
            blockType = tileentity.getWorld().getBlockState(tileentity.getPos()).getValue(BlockCrystalMarker.TYPE);
        } else {
            blockType = (int) partialTicks;
        }

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

        GlStateManager.pushMatrix();
        GlStateManager.translate(-tileentity.getPos().getX(), -tileentity.getPos().getY(), -tileentity.getPos().getZ());
        Tessellator tesselator = Tessellator.getInstance();
        tesselator.getBuffer().begin(7, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(getWorld(), getBakedModel(), AMBlocks.crystal_marker.getDefaultState().withProperty(BlockCrystalMarker.TYPE, blockType), tileentity.getPos(), tesselator.getBuffer(), false);
        tesselator.draw();
        GlStateManager.popMatrix();
        // Reset color to white to avoid affecting subsequent renders
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
        GlStateManager.enableCull();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }

}
