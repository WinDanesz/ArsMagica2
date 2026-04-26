package am2.client.gui;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityArcaneDeconstructor;
import am2.common.container.ContainerArcaneDeconstructor;
import am2.common.container.slot.SlotGhostRune;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;

public class GuiArcaneDeconstructor extends GuiContainer {

    private TileEntityArcaneDeconstructor deconstructor;

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/deconstruction_table_gui.png");

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        mc.renderEngine.bindTexture(background);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);

        int overlayHeight = this.deconstructor.getProgressScaled(18);
        if (overlayHeight > 0)
            this.drawTexturedModalRect(l + 79, i1 + 65, 176, 0, 17, overlayHeight);
    }

    public GuiArcaneDeconstructor(InventoryPlayer inventoryplayer, TileEntityArcaneDeconstructor deconstructorEntity) {
        super(new ContainerArcaneDeconstructor(inventoryplayer, deconstructorEntity));
        this.deconstructor = deconstructorEntity;
        xSize = 176;
        ySize = 198;
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
