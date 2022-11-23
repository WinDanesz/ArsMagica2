package am2.api.compendium.pages;

import java.io.IOException;

import am2.api.blocks.IMultiblock;
import am2.api.compendium.AdvancedBlockRenderer;
import am2.api.compendium.BlockRenderWorld;
import am2.client.gui.AMGuiHelper;
import am2.client.gui.controls.GuiButtonCompendiumNext;
import am2.client.gui.controls.GuiButtonVariableDims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class PageMultiblock extends CompendiumPage<IMultiblock> {

	private int curLayer = -1;
	private int maxLayers = 0;
	private GuiButtonCompendiumNext prevLayer;
	private GuiButtonCompendiumNext nextLayer;
	private GuiButtonVariableDims pauseCycling;
	private ItemStack stackTip = null;
	//private static final ResourceLocation red = new ResourceLocation("arsmagica2", "textures/blocks/red.png");

	public PageMultiblock(IMultiblock element) {
		super(element);
		maxLayers = element.getHeight();
	}
	
	@Override
	public GuiButton[] getButtons(int id, int posX, int posY) {
		prevLayer = new GuiButtonCompendiumNext(id++, posX, posY + 19, false);
		nextLayer = new GuiButtonCompendiumNext(id++, posX + 125, posY + 19, true);
		pauseCycling = new GuiButtonVariableDims(5, posX + 105, posY + 190, AMGuiHelper.instance.runCompendiumTicker ? I18n.format("am2.gui.pause") : I18n.format("am2.gui.cycle")).setDimensions(40, 20);
		prevLayer.visible = true;
		nextLayer.visible = true;
		pauseCycling.visible = true;
		return new GuiButton[] {prevLayer, nextLayer, pauseCycling};
	}
	
	@Override
	public void switchButtonDisplay(boolean shouldShow) {
		if (shouldShow) {
			prevLayer.visible = true;
			nextLayer.visible = true;
			pauseCycling.visible = true;
		} else {
			prevLayer.visible = false;
			nextLayer.visible = false;
			pauseCycling.visible = false;
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		if (button == nextLayer) {
			curLayer++;
			if (curLayer >= maxLayers - 1){
				curLayer = -1;
			}
		} else if (button == prevLayer) {
			curLayer--;
			if (curLayer < -1){
				curLayer = maxLayers - 2;
			}
		} else if (button == pauseCycling) {
			AMGuiHelper.instance.runCompendiumTicker = !AMGuiHelper.instance.runCompendiumTicker;
			pauseCycling.displayString = AMGuiHelper.instance.runCompendiumTicker ? I18n.format("am2.gui.pause") : I18n.format("am2.gui.cycle");
		}
		super.actionPerformed(button);
	}
	
	@Override
	protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
		stackTip = null;
		int cx = posX + 60;
		int cy = posY + 92;
		String label = String.format("%s: %s", I18n.format("am2.gui.layer"), curLayer == -1 ? I18n.format("am2.gui.all") : "" + curLayer);

		mc.fontRenderer.drawString(label, cx - mc.fontRenderer.getStringWidth(label) / 2, cy - 90, 0x000000);

		GlStateManager.pushMatrix();
		GlStateManager.pushAttrib();
		GlStateManager.disableCull();
		GlStateManager.disableLighting();

		/*ArrayList<IBlockState> blox = entryMultiblock.getAllowedBlocksAt(entryMultiblock.new BlockPos(0, 0, 0));
		if (blox != null){
			renderBlock(Block.blocksList[blox.get(0).getID()], blox.get(0).getMeta(), cx, cy);
		}*/
//		BlockPos pickedBlock = getPickedBlock(cx, cy, mouseX, mouseY);
		AdvancedBlockRenderer abr = new AdvancedBlockRenderer(new BlockRenderWorld());
		GlStateManager.pushMatrix();
		float scale = (float) Math.min(Math.sqrt((float)(150 * 150) / (float)(element.getLength() * element.getLength() + element.getWidth() * element.getWidth())), 20D);
		float yMove = (float) (Math.sqrt(scale * scale) / 2F);
		GlStateManager.translate(posX, posY, 0);
		GlStateManager.translate(60 - scale, 92 - scale - (yMove * 0.5 * element.getHeight()) + (yMove * element.getMaxY()), 0);
		GlStateManager.translate(0, 0, 300);
		GlStateManager.scale(scale, scale, scale);
		GlStateManager.translate(1.0F, 0.5F, 1.0F);
		GlStateManager.scale(1.0F, 1.0F, -1.0F);
		GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		GlStateManager.disableAlpha();
		GlStateManager.enableAlpha();
		abr.renderMultiblock(this.element, curLayer, true);
		GlStateManager.popMatrix();
		if (stackTip != null)
			renderItemToolTip(stackTip, mouseX, mouseY);
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
	}
	
//	private BlockPos getPickedBlock(int cx, int cy, int mousex, int mousey){
//		BlockPos block = null;
//
//		float step_x = 14f;
//		float step_y = -16.0f;
//		float step_z = 7f;
//
//		cy -= step_y * element.getMinY() / 2;
//		cy -= step_y * element.getMaxY() / 2;
//
//		int start = curLayer == -1 ? element.getMinY() : element.getMinY() + curLayer;
//		int end = curLayer == -1 ? element.getMaxY() : element.getMinY() + curLayer;
//
//		for (int i = start; i <= end; ++i){
//			TreeMap<BlockPos, List<IBlockState>> layerBlocksSorted = getMultiblockLayer(i);
//
//			float px = cx - (step_x * (element.getWidth() / 2));
//			float py = cy - (step_z * (element.getLength() / 2));
//						
//			for (BlockPos bc : layerBlocksSorted.keySet()){
//				float x = px + ((bc.getX() - bc.getZ()) * step_x);
//				float y = py + ((bc.getZ() + bc.getX()) * step_z) + (step_y * i);
//
//				x += 20;
//				y -= 10;
//
//				if (mousex > x && mousex < x + 32){
//					if (mousey > y && mousey < y + 32){
//						block = bc;
//					}
//				}
//			}
//		}
//		return block;
//	}
}
