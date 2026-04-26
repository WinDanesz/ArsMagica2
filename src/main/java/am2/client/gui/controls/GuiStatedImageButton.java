package am2.client.gui.controls;

import am2.api.math.AMVector2;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class GuiStatedImageButton extends GuiButtonVariableDims {

    public enum States {
        IDLE,
        MOUSEOVER,
        HOVER,
        CLICK,
        ACTIVE
    }

    private HashMap<States, AMVector2> stateImages;
//	private ResourceLocation buttonTextureLocation;

    private boolean isActive = false;
    private boolean currentlyClicked = false;

    public GuiStatedImageButton(int id, int x, int y, ResourceLocation textureLoc, int idleU, int idleV) {
        super(id, x, y, "");
        stateImages = new HashMap<GuiStatedImageButton.States, AMVector2>();
        addStateCoords(States.IDLE, idleU, idleV);
    }

    public void addStateCoords(States state, int u, int v) {
        stateImages.put(state, new AMVector2(u, v));
    }

    public void toggle() {
        this.isActive = !this.isActive;
    }

    public boolean getActive() {
        return this.isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float PartialTicks) {
        boolean isMousedOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        if (!renderBorderOnly) {
            if (currentlyClicked) {
                drawButtonFromState(States.CLICK);
            } else if (isMousedOver) {
                drawButtonFromState(States.MOUSEOVER);
            } else if (getActive()) {
                drawButtonFromState(States.ACTIVE);
            } else {
                drawButtonFromState(States.IDLE);
            }
        } else {
            int color = this.enabled ? 0xFFFFFF : 0x660000;
            line(this.x, this.y, this.x + this.width, this.y, color);
            line(this.x + this.width, this.y, this.x + this.width, this.y + this.height, color);
            line(this.x + this.width, this.y + this.height, this.x, this.y + this.height, color);
            line(this.x, this.y + this.height, this.x, this.y, color);
        }

        if (isMousedOver && !this.hoverTextLines.isEmpty()) {
            drawHoveringText(hoverTextLines, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
        }
    }

    private void drawButtonFromState(States state) {
        AMVector2 vec = this.stateImages.get(state);
        if (vec == null)
            vec = this.stateImages.get(States.IDLE);

        drawTexturedModalRect(this.x, this.y, vec.iX, vec.iY, width, height);
    }

    @Override
    public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
        boolean isMousedOver = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;
        if (isMousedOver)
            currentlyClicked = true;
        return super.mousePressed(par1Minecraft, par2, par3);
    }

    @Override
    public void mouseReleased(int par1, int par2) {
        currentlyClicked = false;
        super.mouseReleased(par1, par2);
    }

}
