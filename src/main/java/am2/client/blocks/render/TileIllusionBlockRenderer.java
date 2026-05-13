package am2.client.blocks.render;

import am2.common.blocks.BlockIllusionBlock;
import am2.common.blocks.tileentity.TileEntityIllusionBlock;
import am2.common.registry.AMBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;

public class TileIllusionBlockRenderer extends TileEntitySpecialRenderer<TileEntityIllusionBlock> {

    /**
     * Called during async chunk rebuild — writes geometry into the chunk VBO once.
     * No per-frame draw call overhead. Chunk is rebuilt when:
     *  - the mimic changes (neighborChanged → scanMimicBlock → markBlockForRenderUpdate)
     *  - true sight is gained/lost (ClientTickHandler detects change → marks all illusion chunks dirty)
     *
     * buf.setTranslation() shifts the vertex write position so world-coordinate renderBlock()
     * calls produce vertices at the correct chunk-relative offset within the VBO.
     */
    @Override
    public void renderTileEntityFast(TileEntityIllusionBlock te, double x, double y, double z,
                                     float partialTicks, int destroyStage, float partial, BufferBuilder buf) {
        if (!te.hasWorld()) return;

        IBlockState blockState = te.getWorld().getBlockState(te.getPos());
        boolean hasMimic = te.getMimicBlock() != null && te.getMimicBlock() != Blocks.AIR.getDefaultState();
        boolean revealed = te.isRevealed(blockState);

        buf.setTranslation(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());

        if (hasMimic && !revealed) {
            Minecraft.getMinecraft().getBlockRendererDispatcher()
                    .renderBlock(te.getMimicBlock(), te.getPos(), te.getWorld(), buf);
        } else {
            IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher()
                    .getBlockModelShapes().getModelForState(AMBlocks.illusion_block.getDefaultState());
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
                    .renderModel(te.getWorld(), model, blockState, te.getPos(), buf, true);
        }

        buf.setTranslation(0, 0, 0);
    }

    /**
     * Called only for the block-breaking damage overlay (destroyStage >= 0).
     * Normal rendering is handled by renderTileEntityFast — hasFastRenderer() = true causes
     * vanilla/Forge to skip calling render() entirely for the normal render pass.
     */
    @Override
    public void render(TileEntityIllusionBlock te, double x, double y, double z,
                       float partialTicks, int destroyStage, float alpha) {
        if (destroyStage < 0 || !te.hasWorld()) return;
        boolean hasMimic = te.getMimicBlock() != null && te.getMimicBlock() != Blocks.AIR.getDefaultState();
        if (!hasMimic) return;

        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);
        IBakedModel baseModel = Minecraft.getMinecraft().getBlockRendererDispatcher()
                .getBlockModelShapes().getModelForState(te.getMimicBlock());
        IBakedModel damageModel = net.minecraftforge.client.ForgeHooksClient
                .getDamageModel(baseModel, sprite, te.getMimicBlock(), te.getWorld(), te.getPos());
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(7, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
                .renderModel(te.getWorld(), damageModel, te.getMimicBlock(), te.getPos(), buf, true);
        tessellator.draw();

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }
}
