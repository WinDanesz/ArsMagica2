package am2.client.gui;

import am2.ArsMagica;
import am2.api.compendium.CompendiumEntry;
import am2.api.compendium.pages.CompendiumPage;
import am2.client.gui.controls.GuiButtonCompendiumNext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;

public class GuiArcaneCompendium extends GuiScreen {

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/arcane_compendium_index_gui.png");
    private static final int xSize = 360;
    private static final int ySize = 256;

    public CompendiumEntry entry;
    private int page = 0;
    private boolean navigatingInternally = false;
    private GuiButtonCompendiumNext nextPage;
    private GuiButtonCompendiumNext prevPage;
    private ArrayList<CompendiumPage<?>> pages = new ArrayList<>();

    public GuiArcaneCompendium(CompendiumEntry entry) {
        this.entry = entry;
        this.pages.addAll(this.entry.getPages());
    }

    public GuiArcaneCompendium(CompendiumEntry entry, int initialPage) {
        this.entry = entry;
        this.pages.addAll(this.entry.getPages());
        // Clamp to valid even page index
        int maxPage = (pages.size() - 1) & ~1;
        this.page = Math.max(0, Math.min(initialPage & ~1, maxPage));
    }

    @Override
    public void initGui() {
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        int idCount = 0;
        prevPage = new GuiButtonCompendiumNext(idCount++, l + 35, i1 + ySize - 25, false);
        nextPage = new GuiButtonCompendiumNext(idCount++, l + 315, i1 + ySize - 25, true);

        prevPage.visible = page > 0;
        nextPage.visible = pages.size() > 2 && page < ((pages.size() - 1) & ~1);

        buttonList.add(nextPage);
        buttonList.add(prevPage);
        buttonList.add(new GuiButtonCompendiumNext(-1, l + 20, i1 + 15, false));

        boolean isRightPage = false;
        int pageID = 0;
        for (CompendiumPage<?> cp : pages) {
            for (GuiButton button : cp.getButtons(idCount, isRightPage ? l + 185 : l + 35, i1)) {
                buttonList.add(button);
                cp.switchButtonDisplay(pageID == this.page || pageID == this.page + 1);
            }
            isRightPage = !isRightPage;
            pageID++;
        }
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;

        // Clear compendium links before rendering pages
        AMGuiHelper.clearCompendiumLinks();

        GlStateManager.color(1.0f, 1.0f, 1.0f);
        mc.renderEngine.bindTexture(background);
        this.drawTexturedModalRect_Classic(l, i1, 0, 0, xSize, ySize, 256, 240);

        // Draw title centered on the left page (first page pair only)
        if (page == 0 && entry != null) {
            String title = "\u00A7n" + entry.getName(); // Underlined title
            int titleWidth = mc.fontRenderer.getStringWidth(title);
            int titleX = l + 35 + (140 - titleWidth) / 2; // Center in left page area (width ~140)
            mc.fontRenderer.drawString(title, titleX, i1 + 18, 0x000000);
        }

        try {
            int page1 = page;
            int page2 = page + 1;
            // First page (page1 == 0) needs extra offset to account for title + spacing
            int page1YOffset = (page == 0) ? i1 + 40 : i1 + 20;
            if (page1 < pages.size())
                pages.get(page1).render(l + 35, page1YOffset, mouseX, mouseY);
            if (page2 < pages.size())
                pages.get(page2).render(l + (xSize / 2) + 5, i1 + 20, mouseX, mouseY);
        } catch (Throwable t) {
        }

        RenderHelper.disableStandardItemLighting();

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, -1);
        //GlStateManager.disableDepth();
        //this.drawDefaultBackground();
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();


        RenderHelper.enableStandardItemLighting();

        // Reset GL color state with full alpha to ensure buttons render with correct brightness
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == nextPage.id) {
            page += 2;
            if (page > pages.size() - 1)
                page = pages.size() - 1;
            page = page & ~1;
            if (page == ((pages.size() - 1) & ~1))
                nextPage.visible = false;
            prevPage.visible = true;
        }
        if (button.id == prevPage.id) {
            page -= 2;
            if (page < 0)
                page = 0;
            if (page == 0)
                prevPage.visible = false;
            if (pages.size() > 2)
                nextPage.visible = true;
        }
        if (button.id == -1) {
            navigatingInternally = true;
            Minecraft.getMinecraft().displayGuiScreen(new GuiCompendiumIndex());
        }
        int pageID = 0;
        for (CompendiumPage<?> page : pages) {
            page.actionPerformed(button);
            page.switchButtonDisplay(this.page == pageID || this.page + 1 == pageID);
            pageID++;
        }
        super.actionPerformed(button);
    }

    @Override
    public void onGuiClosed() {
        if (navigatingInternally) {
            // User navigated within the compendium (back to index), clear the saved entry
            AMGuiHelper.clearCompendiumEntryState();
        } else {
            // User closed the book entirely, save state for next open
            AMGuiHelper.saveCompendiumEntryState(entry, page);
        }
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int pageID = 0;
        for (CompendiumPage<?> page : pages) {
            page.dragMouse(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            page.switchButtonDisplay(this.page == pageID || this.page + 1 == pageID);
            pageID++;
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Only check clicks on the two currently visible pages
        int page1 = this.page;
        int page2 = this.page + 1;

        if (page1 < pages.size()) {
            pages.get(page1).mouseClicked(mouseX, mouseY, mouseButton);
            pages.get(page1).switchButtonDisplay(true);
        }
        if (page2 < pages.size()) {
            pages.get(page2).mouseClicked(mouseX, mouseY, mouseButton);
            pages.get(page2).switchButtonDisplay(true);
        }

        // Update button display for all other pages
        for (int i = 0; i < pages.size(); i++) {
            if (i != page1 && i != page2) {
                pages.get(i).switchButtonDisplay(false);
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        int pageID = 0;
        for (CompendiumPage<?> page : pages) {
            page.mouseReleased(mouseX, mouseY, state);
            page.switchButtonDisplay(this.page == pageID || this.page + 1 == pageID);
            pageID++;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        // Handle mouse back button (button 3) to go back to index
        if (Mouse.getEventButtonState() && Mouse.getEventButton() == 3) {
            navigatingInternally = true;
            Minecraft.getMinecraft().displayGuiScreen(new GuiCompendiumIndex());
        }
    }

    public void drawTexturedModalRect_Classic(int dst_x, int dst_y, int src_x, int src_y, int dst_width, int dst_height, int src_width, int src_height) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;

        Tessellator var9 = Tessellator.getInstance();
        var9.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
        var9.getBuffer().pos(dst_x + 0, dst_y + dst_height, this.zLevel).tex((src_x + 0) * var7, (src_y + src_height) * var8).endVertex();
        var9.getBuffer().pos(dst_x + dst_width, dst_y + dst_height, this.zLevel).tex((src_x + src_width) * var7, (src_y + src_height) * var8).endVertex();
        var9.getBuffer().pos(dst_x + dst_width, dst_y + 0, this.zLevel).tex((src_x + src_width) * var7, (src_y + 0) * var8).endVertex();
        var9.getBuffer().pos(dst_x + 0, dst_y + 0, this.zLevel).tex((src_x + 0) * var7, (src_y + 0) * var8).endVertex();
        var9.draw();
    }
}
