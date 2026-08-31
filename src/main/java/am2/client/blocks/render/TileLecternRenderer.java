package am2.client.blocks.render;

import am2.ArsMagica;
import am2.common.blocks.BlockLectern;
import am2.common.blocks.tileentity.TileEntityLectern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class TileLecternRenderer extends TileEntitySpecialRenderer<TileEntityLectern> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/custom/lectern.png");

    private final ModelBook enchantmentBook = new ModelBook();


    public void renderTileEntityArchmagePodiumAt(TileEntityLectern podium, double x, double y, double z, float partialTicks) throws Exception {
        Minecraft.getMinecraft().profiler.startSection("Lectern-Render");
        // Old podium model rendering removed - now using JSON block model
        // Only render the book on top via TESR

        Minecraft.getMinecraft().profiler.startSection("book-model");
        if (podium.hasStack()) {
            if (podium.getOverpowered())
                GlStateManager.color(0.7f, 0.2f, 0.2f, 1.0f);
            else
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            renderBook(podium, x, y, z, partialTicks, 0);
        }
        else if (podium.getNeedsBook()) {
            GlStateManager.color(0.7f, 0.2f, 0.2f, 0.2f);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            renderBook(podium, x, y, z, partialTicks, 0);
            GlStateManager.disableBlend();
        }
        else {
            podium.resetParticleAge();
            return;
        }
        renderHelperIcon(podium, x, y, z, partialTicks);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().profiler.endSection();
        Minecraft.getMinecraft().profiler.endSection();
    }

    private void renderHelperIcon(TileEntityLectern podium, double x, double y, double z, float partialTicks) {
        ItemStack sus = podium.getTooltipStack();
        if (sus == null || sus.isEmpty()) {
            podium.resetParticleAge();
            return;
        }

        float deg = (podium.tickCount % 3600);

        ItemStack stack = sus.copy();
        stack.setCount(1);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1.65F, z + 0.5F);
        GlStateManager.rotate(deg, 0, partialTicks, 0);

        int dye = ((podium.tickCount) / 100) % 16;
        if (stack.getItem() == Items.DYE) {
            stack = new ItemStack(Items.DYE, 1, dye);
        }
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1.25F, z + 0.5F);
        final float scale = 0.2F;
        GlStateManager.scale(scale, scale, scale);
        renderRadiant(partialTicks, podium);
        GlStateManager.popMatrix();
    }

    private void renderRadiant(float partialTicks, TileEntityLectern podium) {
		float time = (podium.particleAge + partialTicks) / podium.particleMaxAge;
		float rescale = 0.0F;

		if (time > 0.8F){
			rescale = (time - 0.8F) / 0.2F;
		}

		Random rand = new Random(432L);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_ENABLE_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDepthMask(false);

        Tessellator t= Tessellator.getInstance();

		for (int i = 0; i < 20; ++i){
			GL11.glPushMatrix();
			GL11.glRotatef(podium.tickCount % 360, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(rand.nextFloat() * 180, 1.0F, 0.0F, 0.0F);
			GL11.glRotatef(rand.nextFloat() * -180, 1.0F, 0.0F, 0.0F);
            BufferBuilder buf = t.getBuffer();
            buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
			float w = rand.nextFloat() * 2.0F + 1.0F + rescale * 2.0F;
			float h = rand.nextFloat() * 2.0F + 2.0F + rescale * 0.5F;
            float r = 0.2F, g = 0.2F, b = 1.0F, a = 1.0F;
			if (podium.getOverpowered()) {
                r = 1.0F;
                b = 0.2F;
            }
            buf.pos(0.0D, 0.0D, 0.0D).color(r, g, b, 0.2F).endVertex();
			buf.pos(-0.866D * w, h, -0.5F * w).color(r, g, b, 0.0F).endVertex();
			buf.pos(0.866D * w, h, -0.5F * w).color(r, g, b, 0.0F).endVertex();
			buf.pos(0.0D, h, w).color(r, g, b, 0.0F).endVertex();
			buf.pos(-0.866D * w, h, -0.5F * w).color(r, g, b, 0.0F).endVertex();
			t.draw();
			GL11.glPopMatrix();
		}

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
		RenderHelper.enableStandardItemLighting();
    }

    private void renderBook(TileEntityLectern podium, double x, double y, double z, float partialTicks, int meta) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5F, y + 1.15F, z + 0.5F);
        float var9 = (float) podium.tickCount + partialTicks;
        GlStateManager.translate(0.0F, (MathHelper.sin(var9 * 0.01F) + 1.0F) * 0.01F, 0.0F);
        float f2;

        for (f2 = podium.tRot - podium.bookRotation; f2 >= (float) Math.PI; f2 -= ((float) Math.PI * 2F)) ;

        while (f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2F);
        }

        EnumFacing facing = EnumFacing.NORTH;
        if (podium.hasWorld())
            facing = podium.getWorld().getBlockState(podium.getPos()).getValue(BlockLectern.FACING);
        GlStateManager.rotate(270 - facing.getHorizontalAngle(), 0, 1, 0);

        GlStateManager.rotate(67.0F, 0.0F, 0.0F, 1.0F);
        bindTexture(new ResourceLocation("textures/entity/enchanting_table_book.png"));
        float var12 = podium.pageFlipPrev + (podium.pageFlip - podium.pageFlipPrev) * partialTicks + 0.25F;
        float var13 = podium.pageFlipPrev + (podium.pageFlip - podium.pageFlipPrev) * partialTicks + 0.75F;
        var12 = (var12 - MathHelper.floor(var12)) * 1.6F - 0.3F;
        var13 = (var13 - MathHelper.floor(var13)) * 1.6F - 0.3F;

        var12 = MathHelper.clamp(var12, 0, 1);
        var13 = MathHelper.clamp(var13, 0, 1);

        this.enchantmentBook.setRotationAngles(var9, var12, var13, 1f, 0.0F, 0.0625F, (Entity) null);
        this.enchantmentBook.coverRight.render(0.0625F);
        this.enchantmentBook.coverLeft.render(0.0625F);
        this.enchantmentBook.bookSpine.render(0.0625F);
        this.enchantmentBook.pagesRight.render(0.0625F);
        this.enchantmentBook.pagesLeft.render(0.0625F);
        this.enchantmentBook.flippingPageRight.render(0.0625F);
        this.enchantmentBook.flippingPageLeft.render(0.0625F);
        GlStateManager.popMatrix();
    }

    @Override
    public void render(TileEntityLectern te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        try {
            RenderHelper.disableStandardItemLighting();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            renderTileEntityArchmagePodiumAt(te, x, y, z, partialTicks);
            RenderHelper.enableStandardItemLighting();
        }
        catch (Exception e) {
            ArsMagica.LOGGER.error("LecternRenderer.render exception caught: ", e);
            return;
        }
        GlStateManager.popMatrix();
    }
}
