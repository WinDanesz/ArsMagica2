package am2.client.gui.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonTextOnly extends GuiButtonVariableDims {

	public GuiButtonTextOnly(int par1, int par2, int par3, String par4Str) {
		super(par1, par2, par3, par4Str);
	}
	
	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float PartialTicks){
		if (this.visible){
			GlStateManager.color(1, 1, 1, 1);
			boolean isMousedOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			int textColor = 0xFFFFFF;
			if (isMousedOver){
				textColor = 0x6600FF;
			}
			GlStateManager.disableAlpha();
			mc.fontRenderer.drawString(this.displayString, x, y, textColor);
		}
	}

}
