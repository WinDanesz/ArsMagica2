package am2.client.blocks.render;

import am2.common.blocks.tileentity.TileEntityEverstone;
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
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileEverstoneRenderer extends TileEntitySpecialRenderer<TileEntityEverstone> {

    @Override
    public void render(TileEntityEverstone te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (!te.isSolid() || te.getFacade() == null)
            return;

        IBlockState state = te.getFacade();
        BlockPos pos = te.getPos();

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.translate(x, y, z);
        GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        IBakedModel model = damageModel(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state), state, pos, destroyStage);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(), model,state, pos, Tessellator.getInstance().getBuffer(), true);
        Tessellator.getInstance().draw();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    private static IBakedModel damageModel(IBakedModel model, IBlockState state, BlockPos pos, int destroyStage) {
        if (state == null || destroyStage < 0)
            return model;
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/destroy_stage_" + destroyStage);
        return net.minecraftforge.client.ForgeHooksClient.getDamageModel(model, sprite, state, Minecraft.getMinecraft().world, pos);
    }

}
