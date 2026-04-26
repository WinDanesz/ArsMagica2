package am2.api.compendium.pages;

import am2.api.compendium.CompendiumCategory;
import am2.api.compendium.CompendiumEntry;
import am2.client.gui.AMGuiHelper;
import am2.client.gui.GuiArcaneCompendium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.util.List;

public class PageText extends CompendiumPage<String> {

    public PageText(String element) {
        super(element);
    }

    @Override
    protected void renderPage(int posX, int posY, int mouseX, int mouseY) {
//		int y_start_title = posY + 50;
//		int x_start_title = posX + 100 - (mc.fontRenderer.getStringWidth(element) / 2);
//		int x_start_line = posX + 35;
//		int y_start_line = posY + 50;
//		if (page > numPages) page = numPages;
//
//		if (page == 0)
//			fontRendererObj.drawString(entrySkill != null ? entrySkill.getName() : entry.getName(), x_start_title, y_start_title, 0x000000);
        AMGuiHelper.drawCompendiumText(I18n.format(element), posX, posY, 140, 0x000000, mc.fontRenderer);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return; // Only left click

        List<AMGuiHelper.CompendiumLinkInfo> links = AMGuiHelper.getCompendiumLinks();
        for (AMGuiHelper.CompendiumLinkInfo link : links) {
            if (link.contains(mouseX, mouseY)) {
                // Find the compendium entry by name
                CompendiumEntry entry = findEntryByName(link.linkText);
                if (entry != null) {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiArcaneCompendium(entry));
                }
                break;
            }
        }
    }

    private CompendiumEntry findEntryByName(String name) {
        // Search through all compendium entries
        for (CompendiumEntry entry : CompendiumCategory.getAllEntries()) {
            String entryName = I18n.format(entry.getName());
            if (entryName.equalsIgnoreCase(name)) {
                return entry;
            }
        }
        return null;
    }

}
