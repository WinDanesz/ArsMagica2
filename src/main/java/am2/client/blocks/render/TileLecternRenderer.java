package am2.client.blocks.render;

import am2.ArsMagica;
import am2.common.blocks.BlockLectern;
import am2.common.blocks.tileentity.TileEntityLectern;
import am2.common.registry.AMLecternBooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TileLecternRenderer extends TileEntitySpecialRenderer<TileEntityLectern> {

    public static final ResourceLocation TEXTURE = new ResourceLocation("arsmagica2", "textures/blocks/custom/lectern.png");
    private static final ResourceLocation VANILLA_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");

    // Exact vanilla enchanting_table_book.png palette (0xRRGGBB) for the 3 paintable regions -
    // found by inspecting the actual texture. Pixels matching these get swapped for a book's
    // configured colors; everything else (pages, transparency) is left untouched.
    private static final int VANILLA_COVER = 0xB77A35;
    private static final int VANILLA_FRAME = 0x774E22;
    private static final int VANILLA_PIXELS = 0xFFD800;
    // A handful of pixels form a lit highlight along the spine seam, a shade of their own rather
    // than a plain mix of the two above. Recolored as a lightened frame tint so it still reads as
    // a highlight instead of a fixed beige streak clashing with whatever cover color is chosen.
    private static final int VANILLA_SEAM_HIGHLIGHT = 0xDBCDBE;

    private final ModelBook enchantmentBook = new ModelBook();
    private final Map<Item, ResourceLocation> recoloredBookTextures = new HashMap<>();


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

        if (stack.getItem() == Items.DYE && stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            int dye = ((podium.tickCount) / 100) % 16;
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

    /**
     * Returns the book texture to render for {@code stack}: a lazily-built, cached recolor of the
     * vanilla enchanting-table book if {@code stack}'s item has colors configured via
     * {@code lectern_book_colors}, or the plain vanilla texture otherwise.
     */
    private ResourceLocation getBookTexture(ItemStack stack) {
        AMLecternBooks.BookColors colors = AMLecternBooks.getBookColors(stack);
        if (colors == null)
            return VANILLA_BOOK_TEXTURE;

        return recoloredBookTextures.computeIfAbsent(stack.getItem(), item -> buildRecoloredBookTexture(item, colors));
    }

    private ResourceLocation buildRecoloredBookTexture(Item item, AMLecternBooks.BookColors colors) {
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(VANILLA_BOOK_TEXTURE);
            BufferedImage source = TextureUtil.readBufferedImage(resource.getInputStream());
            // enchanting_table_book.png has ~11 distinct colors, so PNG encoders commonly store it
            // indexed/palette-based; ImageIO.read() preserves that. Writing an arbitrary new color
            // into an indexed BufferedImage doesn't store it - it silently snaps to the closest
            // existing palette entry, which is why setRGB below needs a true ARGB raster to write to.
            BufferedImage image = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            g.drawImage(source, 0, 0, null);
            g.dispose();
            recolor(image, VANILLA_COVER, colors.cover);
            recolor(image, VANILLA_FRAME, colors.frame);
            recolor(image, VANILLA_PIXELS, colors.pixels);
            recolor(image, VANILLA_SEAM_HIGHLIGHT, lighten(colors.frame, 0.5f));
            tintPaper(image, colors.paper);

            ResourceLocation registryName = item.getRegistryName();
            String name = registryName == null ? "lectern_book" : registryName.toString().replace(':', '_');
            return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(name, new DynamicTexture(image));
        } catch (IOException e) {
            ArsMagica.LOGGER.warn("Failed to build recolored lectern book texture for {}; using the vanilla book instead.", item.getRegistryName(), e);
            return VANILLA_BOOK_TEXTURE;
        }
    }

    /** Blends {@code rgb} toward white by fraction {@code t} (0 = unchanged, 1 = white). */
    private static int lighten(int rgb, float t) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r += (255 - r) * t;
        g += (255 - g) * t;
        b += (255 - b) * t;
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Tints every grayscale pixel (the pages, plus the texture's unused transparent padding) toward
     * {@code paperRGB}, treating each pixel's own grey value as a luminance/shading level to preserve
     * rather than flattening it to one color. White (the default when no paper color is configured)
     * is a no-op, since multiplying by white changes nothing.
     */
    private static void tintPaper(BufferedImage image, int paperRGB) {
        if (paperRGB == 0xFFFFFF)
            return;

        int pr = (paperRGB >> 16) & 0xFF;
        int pg = (paperRGB >> 8) & 0xFF;
        int pb = paperRGB & 0xFF;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                if (r != g || g != b)
                    continue; // not grayscale - covers, frame, and studs are already handled above

                int nr = r * pr / 255;
                int ng = g * pg / 255;
                int nb = b * pb / 255;
                image.setRGB(x, y, (argb & 0xFF000000) | (nr << 16) | (ng << 8) | nb);
            }
        }
    }

    /** Replaces every pixel matching {@code fromRGB} (ignoring alpha) with {@code toRGB}, in place. */
    private static void recolor(BufferedImage image, int fromRGB, int toRGB) {
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb & 0xFFFFFF) == fromRGB) {
                    image.setRGB(x, y, (argb & 0xFF000000) | toRGB);
                }
            }
        }
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
        bindTexture(getBookTexture(podium.getStack()));
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
