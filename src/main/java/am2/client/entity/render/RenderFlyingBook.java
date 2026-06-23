package am2.client.entity.render;

import am2.common.entity.EntityFlyingBook;
import am2.common.registry.AMItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderFlyingBook extends Render<EntityFlyingBook> {

	private ItemStack bookStack = new ItemStack(Items.BOOK);
	private ItemStack compendiumStack = new ItemStack(AMItems.arcane_compendium);

	public RenderFlyingBook(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityFlyingBook entity, double x, double y, double z, float entityYaw, float partialTicks) {
		byte phase = entity.getPhase();
		if (phase == EntityFlyingBook.PHASE_COMPLETE) return;

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();

		GlStateManager.translate(x, y + 0.2, z);

		// Slow rotation
		float rotation = (entity.ticksExisted + partialTicks) * 3.0f;
		GlStateManager.rotate(rotation, 0, 1, 0);

		// Gentle bobbing
		float bob = (float) Math.sin((entity.ticksExisted + partialTicks) * 0.1) * 0.05f;
		GlStateManager.translate(0, bob, 0);

		// Scale
		float scale = 1.0f;

		// During enchanting phase, gentle pulse
		if (phase == EntityFlyingBook.PHASE_ENCHANTING) {
			float pulse = 1.0f + (float) Math.sin((entity.ticksExisted + partialTicks) * 0.15) * 0.05f;
			scale *= pulse;
		}

		// During transform phase, expanding pulse
		if (phase == EntityFlyingBook.PHASE_TRANSFORMING) {
			float pulse = 1.0f + (float) Math.sin((entity.ticksExisted + partialTicks) * 0.3) * 0.15f;
			scale *= pulse;
		}

		GlStateManager.scale(scale, scale, scale);

		// Show compendium model during transform phase
		ItemStack stackToRender = (phase == EntityFlyingBook.PHASE_TRANSFORMING) ? compendiumStack : bookStack;

		// Render the item
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.enableStandardItemLighting();

		Minecraft.getMinecraft().getRenderItem().renderItem(stackToRender, ItemCameraTransforms.TransformType.GROUND);

		RenderHelper.disableStandardItemLighting();

		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFlyingBook entity) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}
}
