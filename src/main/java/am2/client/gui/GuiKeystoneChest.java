package am2.client.gui;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityKeystoneChest;
import am2.common.container.ContainerKeystoneChest;
import am2.common.container.slot.SlotGhostRune;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Collections;

public class GuiKeystoneChest extends GuiContainer {

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/keystone_chest_gui.png");

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        mc.renderEngine.bindTexture(background);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
    }

    public GuiKeystoneChest(InventoryPlayer inventoryplayer, TileEntityKeystoneChest chest) {
        super(new ContainerKeystoneChest(inventoryplayer, chest));
        xSize = 176;
        ySize = 180;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        Slot slot = getSlotUnderMouse();
        if (slot instanceof SlotGhostRune && !slot.getHasStack()) {
            this.drawHoveringText(Collections.singletonList(I18n.format("am2.tooltip.runeslot")), mouseX, mouseY);
        } else {
            this.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
    }
}
