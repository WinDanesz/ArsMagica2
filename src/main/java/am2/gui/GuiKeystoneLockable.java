package am2.gui;

import org.lwjgl.opengl.GL11;

import am2.api.blocks.IKeystoneLockable;
import am2.container.ContainerKeystoneLockable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiKeystoneLockable extends GuiContainer{

	private static final ResourceLocation background = new ResourceLocation("arsmagica2", "textures/gui/keystone_lockable_gui_generic.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j){
		mc.renderEngine.bindTexture(background);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int l = (width - xSize) / 2;
		int i1 = (height - ySize) / 2;
		drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
	}

	public GuiKeystoneLockable(InventoryPlayer inventoryplayer, IKeystoneLockable<?> lockable){
		super(new ContainerKeystoneLockable(inventoryplayer, lockable));
		xSize = 176;
		ySize = 134;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2){
	}

}
