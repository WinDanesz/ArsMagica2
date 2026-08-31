package am2.client.blocks.render;

import am2.ArsMagica;
import am2.api.math.AMVector3;
import am2.client.bosses.renderers.RenderItemNoBob;
import am2.client.gui.AMGuiHelper;
import am2.common.blocks.tileentity.TileEntityArcaneReconstructor;
import am2.common.registry.AMBlocks;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.TRSRTransformation;
import org.lwjgl.opengl.GL11;

public class TileArcaneReconstructorRenderer extends TileEntitySpecialRenderer<TileEntityArcaneReconstructor> {

    private ItemStack renderStack;
    RenderEntityItem renderItem;

    IModel mainModel;
    IModel ring1Model;
    IModel ring2Model;
    IModel ring3Model;
    IBakedModel mainBakedModel;
    IBakedModel ring1BakedModel;
    IBakedModel ring2BakedModel;
    IBakedModel ring3BakedModel;

    private void bake() {
        try {
            mainModel = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/reconstructor/main.obj"));
            ring1Model = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/reconstructor/ring1.obj"));
            ring2Model = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/reconstructor/ring2.obj"));
            ring3Model = ModelLoaderRegistry.getModel(new ResourceLocation("arsmagica2", "block/reconstructor/ring3.obj"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Function<ResourceLocation, TextureAtlasSprite> getter = location -> Minecraft.getMinecraft()
                .getTextureMapBlocks().getAtlasSprite(location.toString());
        mainBakedModel = mainModel.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
        ring1BakedModel = ring1Model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
        ring2BakedModel = ring2Model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
        ring3BakedModel = ring3Model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, getter);
    }

    public TileArcaneReconstructorRenderer() {

        renderStack = new ItemStack(Items.WOODEN_SHOVEL);

    }

    @Override
    public void render(TileEntityArcaneReconstructor te, double x, double y, double z, float partialTicks, int destructionStage, float alpha) {
        if (renderItem == null)
            renderItem = new RenderItemNoBob(Minecraft.getMinecraft().getRenderManager());
        bake();
        float floatingOffset = te.getOffset();//(float) (Math.sin(te.getOffset()) * (Math.PI/ 180F) * 1.4f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        if ((destructionStage != -10) && te.shouldRenderItemStack()) {
            renderStack = te.getCurrentItem();
            if (!renderStack.isEmpty())
                RenderItemAtCoords(renderStack, x + 0.5f, y + 0.85f - 0.125f, z + 0.5f, partialTicks);
        }
        RenderHelper.disableStandardItemLighting();

        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix(); //start
        GL11.glTranslatef((float) x + 0.5f, (float) y + 0.22f, (float) z + 0.5f);

        renderGroup(te, mainBakedModel);

        GL11.glTranslatef(0, 0.22f + floatingOffset, 0);
        RenderRotatedModelGroup(te, ring3BakedModel, te.getInnerRingRotation());
        RenderRotatedModelGroup(te, ring1BakedModel, te.getMiddleRingRotation());
        RenderRotatedModelGroup(te, ring2BakedModel, te.getOuterRingRotation());

        if (te.shouldRenderRotateOffset()) {
            GlStateManager.depthMask(false);

            RenderRotatedModelGroupAlpha(ring3BakedModel, te.getInnerRingRotation().copy().sub(te.getInnerRingRotationSpeed().copy().scale(te.getRotateOffset())), 0.4f);
            RenderRotatedModelGroupAlpha(ring1BakedModel, te.getMiddleRingRotation().copy().sub(te.getMiddleRingRotationSpeed().copy().scale(te.getRotateOffset())), 0.4f);
            RenderRotatedModelGroupAlpha(ring2BakedModel, te.getOuterRingRotation().copy().sub(te.getOuterRingRotationSpeed().copy().scale(te.getRotateOffset())), 0.4f);

            RenderRotatedModelGroupAlpha(ring3BakedModel, te.getInnerRingRotation().copy().sub(te.getInnerRingRotationSpeed().copy().scale(te.getRotateOffset() * 2)), 0.15f);
            RenderRotatedModelGroupAlpha(ring1BakedModel, te.getMiddleRingRotation().copy().sub(te.getMiddleRingRotationSpeed().copy().scale(te.getRotateOffset() * 2)), 0.15f);
            RenderRotatedModelGroupAlpha(ring2BakedModel, te.getOuterRingRotation().copy().sub(te.getOuterRingRotationSpeed().copy().scale(te.getRotateOffset() * 2)), 0.15f);

            GlStateManager.depthMask(true);
        }

        //GL11.glEnable(GL11.GL_LIGHTING);
        GlStateManager.popMatrix(); //end
        RenderHelper.enableStandardItemLighting();
    }

    private void RenderRotatedModelGroup(TileEntityArcaneReconstructor te, IBakedModel model, AMVector3 rotation) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotation.x, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(rotation.y, 1.0f, 1.0f, 0.0f);
        GlStateManager.rotate(rotation.z, 1.0f, 0.0f, 1.0f);
        renderGroup(te, model);
        GlStateManager.popMatrix();
    }

    private void RenderRotatedModelGroupAlpha(IBakedModel model, AMVector3 rotation, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.rotate(rotation.x, 1.0f, 0.0f, 0.0f);
        GlStateManager.rotate(rotation.y, 1.0f, 1.0f, 0.0f);
        GlStateManager.rotate(rotation.z, 1.0f, 0.0f, 1.0f);
        renderGroupWithAlpha(model, alpha);
        GlStateManager.popMatrix();
    }

    private void renderGroup(TileEntityArcaneReconstructor te, IBakedModel model) {
        try {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
            Tessellator t = Tessellator.getInstance();
            BufferBuilder wr = t.getBuffer();
            wr.begin(7, DefaultVertexFormats.BLOCK);
            World world = te.getWorld();
            if (world == null)
                world = Minecraft.getMinecraft().world;
            IBlockState state = world.getBlockState(te.getPos());
            if (state.getBlock() != AMBlocks.arcane_reconstructor)
                state = AMBlocks.arcane_reconstructor.getDefaultState();
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(world, model, state, te.getPos(), wr, false);
            t.draw();
            GlStateManager.popMatrix();
        } catch (Throwable trowable) {
            ArsMagica.LOGGER.error("ArcaneReconstructorRenderer.renderGroup exception caught: ", trowable);
        }
    }

    /**
     * Renders a baked model's quads with transparency using POSITION_TEX format.
     * Uses raw GL11 calls to bypass GlStateManager's stale cached state,
     * and glColor4f for alpha since POSITION_TEX has no per-vertex color attribute.
     */
    private void renderGroupWithAlpha(IBakedModel model, float alpha) {
        // Use raw GL11 calls to force state - GlStateManager cache may be stale after block model renderer
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_CURRENT_BIT | GL11.GL_COLOR_BUFFER_BIT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        Tessellator t = Tessellator.getInstance();
        BufferBuilder wr = t.getBuffer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);

        int intsPerVertex = DefaultVertexFormats.ITEM.getIntegerSize();

        for (EnumFacing face : EnumFacing.values()) {
            for (BakedQuad quad : model.getQuads(null, face, 0L)) {
                addQuadVertices(wr, quad.getVertexData(), intsPerVertex);
            }
        }
        for (BakedQuad quad : model.getQuads(null, null, 0L)) {
            addQuadVertices(wr, quad.getVertexData(), intsPerVertex);
        }

        t.draw();

        GL11.glPopAttrib();
    }

    private void addQuadVertices(BufferBuilder wr, int[] vertexData, int intsPerVertex) {
        for (int v = 0; v < 4; v++) {
            int base = v * intsPerVertex;
            float px = Float.intBitsToFloat(vertexData[base]);
            float py = Float.intBitsToFloat(vertexData[base + 1]);
            float pz = Float.intBitsToFloat(vertexData[base + 2]);
            float u = Float.intBitsToFloat(vertexData[base + 4]);
            float vt = Float.intBitsToFloat(vertexData[base + 5]);
            wr.pos(px, py, pz).tex(u, vt).endVertex();
        }
    }

    private void RenderItemAtCoords(ItemStack item, double x, double y, double z, float partialTick) {
        item.setCount(1);
        AMGuiHelper.instance.dummyItem.setItem(item);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        renderItem.doRender(AMGuiHelper.instance.dummyItem, 0, 0, 0, AMGuiHelper.instance.dummyItem.rotationYaw, partialTick);
        GlStateManager.popMatrix();
    }
}
