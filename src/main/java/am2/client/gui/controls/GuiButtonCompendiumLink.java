package am2.client.gui.controls;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.client.gui.AMGuiHelper;
import am2.client.gui.AMGuiIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GuiButtonCompendiumLink extends GuiButton {
    private final FontRenderer fontRenderer;
    private final CompendiumEntry entry;
    private final CompendiumCategory category;
    private boolean isNewItem = false;
    private boolean displayOnAllPages = false;
    private int page = 0;

    public GuiButtonCompendiumLink(int id, int xPos, int yPos, FontRenderer fontRenderer, int page, CompendiumEntry entry, CompendiumCategory category) {
        super(id, xPos, yPos, fontRenderer.getStringWidth(entry == null ? category.getCategoryName() : entry.getName()), 10, entry == null ? category.getCategoryName() : entry.getName());
        this.fontRenderer = fontRenderer;
        this.entry = entry;
        this.category = category;
        this.page = page;
    }

    public void setNewItem() {
        isNewItem = true;
    }

    public boolean getDisplayOnAllPages() {
        return this.displayOnAllPages;
    }

    public int getPage() {
        return page;
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public CompendiumEntry getEntry() {
        return entry;
    }

    public CompendiumCategory getCategory() {
        return category;
    }

    public void setShowOnAllPages() {
        this.displayOnAllPages = true;
    }

    /**
     * Draws this button to the screen.
     */
    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            GlStateManager.color(1, 1, 1, 1);
            boolean isMousedOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int textColor = 0x000000;
            if (category != null)
                textColor = 0x660000;
            if (isMousedOver) {
                textColor = 0x6600FF;
            }

            fontRenderer.drawString(this.displayString, x, y, textColor);
            //GL11.glDisable(GL11.GL_LIGHTING);
            if (isNewItem) {
                GL11.glColor4f(1, 1, 1, 1);
                AMGuiHelper.DrawIconAtXY(AMGuiIcons.newEntry, x - 6, y + 2, this.zLevel, 5, 5, true);
            }
            //GL11.glEnable(GL11.GL_LIGHTING);
        }
    }
}
