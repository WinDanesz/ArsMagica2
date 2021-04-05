package am2.gui;

import org.lwjgl.opengl.GL11;

import am2.blocks.tileentity.TileEntityObelisk;
import am2.container.ContainerObelisk;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiObelisk extends GuiContainer{

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/obelisk_gui.png");
	private final TileEntityObelisk obelisk;

	public GuiObelisk(TileEntityObelisk obelisk, EntityPlayer player){
		super(new ContainerObelisk(obelisk, player));
		this.obelisk = obelisk;
		xSize = 176;
		ySize = 165;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		mc.renderEngine.bindTexture(background);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);

		int overlayHeight = this.obelisk.getCookProgressScaled(14);
		if (overlayHeight > 0)
			this.drawTexturedModalRect(l + 79, i1 + 30 + 14 - overlayHeight, 176, 14 - overlayHeight, 14, overlayHeight + 2);
	}

}
