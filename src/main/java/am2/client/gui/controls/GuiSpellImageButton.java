package am2.client.gui.controls;

import org.lwjgl.opengl.GL11;

import am2.client.gui.AMGuiIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiSpellImageButton extends GuiButtonVariableDims{
	/**
	 * True for pointing right (next page), false for pointing left (previous page).
	 */
	private final TextureAtlasSprite icon;
	private final int index;
	private boolean isSelected = false;
	private final int page;
	private final static int sourceWidth = 12;
	private final static int sourceHeight = 12;

	public GuiSpellImageButton(int id, int xPos, int yPos, TextureAtlasSprite imageIcon, int index, int page){
		super(id, sourceWidth, sourceHeight, "");
		setPosition(xPos, yPos);
		this.icon = imageIcon;
		this.index = index;
		this.page = page;
		this.setDimensions(12, 12);
	}

	public int getIndex(){
		return this.index;
	}

	public void setSelected(boolean selected){
		this.isSelected = selected;
	}

	public int getPage(){
		return this.page;
	}

	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float PartialTicks){
		if (this.visible){
			boolean isMousedOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			if (isMousedOver){
				GL11.glColor4f(0.6f, 0.6f, 0.6f, 1.0f);
				if (this.hoverTextLines.size() > 0){
					drawHoveringText(hoverTextLines, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
				}
			}

			GL11.glDisable(GL11.GL_LIGHTING);
			this.drawTexturedModelRectFromIcon(this.x, this.y, this.icon, this.width, this.height);
			if (this.isSelected){
				this.drawTexturedModelRectFromIcon(this.x, this.y, AMGuiIcons.frame, this.width, this.height);
			}
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}
	
	public void drawTexturedModelRectFromIcon(int posX, int posY, TextureAtlasSprite sprite, int width, int height) {
		Tessellator t = Tessellator.getInstance();
		t.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
		t.getBuffer().pos(posX + 0, posY + height, this.zLevel).tex(sprite.getMinU(), sprite.getMaxV()).endVertex();
		t.getBuffer().pos(posX + width, posY + height, this.zLevel).tex(sprite.getMaxU(), sprite.getMaxV()).endVertex();
		t.getBuffer().pos(posX + width, posY + 0, this.zLevel).tex(sprite.getMaxU(), sprite.getMinV()).endVertex();
		t.getBuffer().pos(posX + 0, posY + 0, this.zLevel).tex(sprite.getMinU(), sprite.getMinV()).endVertex();
		t.draw();
	}
}
