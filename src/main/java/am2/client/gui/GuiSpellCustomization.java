package am2.client.gui;

import am2.ArsMagica;
import am2.client.gui.controls.GuiButtonVariableDims;
import am2.client.gui.controls.GuiSpellImageButton;
import am2.client.models.ArsMagicaModelLoader;
import am2.common.container.ContainerSpellCustomization;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiSpellCustomization extends GuiContainer {

    private int page = 0;
    private int numPages = 0;
    private int curIndex = 0;
    private String curName = "";

    private GuiButtonVariableDims btnNext;
    private GuiButtonVariableDims btnPrev;
    private GuiButtonVariableDims btnFinalize;
    private GuiTextField spellName;

    // 10 rows of icons, leaving room for the finalize button at the bottom
    private static final int ICON_GRID_BOTTOM = 189;
    private static final int TEXTURE_HEIGHT = 255;
    private static final int ySizeShrunk = 222;
    private static final int FOOTER_HEIGHT = 8;

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/spell_customization.png");

    public GuiSpellCustomization(EntityPlayer player) {
        super(new ContainerSpellCustomization(player));
        this.xSize = 176;
        this.ySize = ySizeShrunk;
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (spellName.isFocused()) {
            if (spellName.textboxKeyTyped(par1, par2)) {
                this.curName = spellName.getText();
                ((ContainerSpellCustomization) this.inventorySlots).setNameAndIndex(curName, curIndex);
            }
        } else {
            super.keyTyped(par1, par2);
        }
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) throws IOException {
        super.actionPerformed(par1GuiButton);

        if (par1GuiButton.id == btnFinalize.id) {
            ((ContainerSpellCustomization) this.inventorySlots).sendPacketToServer();
            this.mc.player.closeScreen();
            return;
        }

        if (par1GuiButton.id == btnPrev.id) {
            if (page > 0) {
                page--;
                for (Object btn : this.buttonList) {
                    if (btn instanceof GuiSpellImageButton) {
                        if (((GuiSpellImageButton) btn).getPage() == page) ((GuiSpellImageButton) btn).visible = true;
                        else ((GuiSpellImageButton) btn).visible = false;
                    }
                }
            }
        } else if (par1GuiButton.id == btnNext.id) {
            if (page < numPages) {
                page++;
                for (Object btn : this.buttonList) {
                    if (btn instanceof GuiSpellImageButton) {
                        if (((GuiSpellImageButton) btn).getPage() == page) ((GuiSpellImageButton) btn).visible = true;
                        else ((GuiSpellImageButton) btn).visible = false;
                    }
                }
            }
        }

        if (par1GuiButton instanceof GuiSpellImageButton) {
            this.curIndex = ((GuiSpellImageButton) par1GuiButton).getIndex();
            ((ContainerSpellCustomization) this.inventorySlots).setNameAndIndex(curName, curIndex);
            for (Object btn : this.buttonList) {
                if (btn instanceof GuiSpellImageButton) {
                    ((GuiSpellImageButton) btn).setSelected(false);
                }
            }
            ((GuiSpellImageButton) par1GuiButton).setSelected(true);
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;

        spellName = new GuiTextField(0, fontRenderer, l + 8, i1 + 8, xSize - 16, 16);

        String suggestion = ((ContainerSpellCustomization) this.inventorySlots).getInitialSuggestedName();
        spellName.setText(suggestion);
        if (!suggestion.equals("")) {
            curName = suggestion;
            ((ContainerSpellCustomization) this.inventorySlots).setNameAndIndex(curName, curIndex);
        }


        btnPrev = new GuiButtonVariableDims(0, l + 8, i1 + 26, I18n.format("am2.gui.prev")).setDimensions(48, 20);
        btnNext = new GuiButtonVariableDims(1, l + xSize - 56, i1 + 26, I18n.format("am2.gui.next")).setDimensions(48, 20);

        this.buttonList.add(btnPrev);
        this.buttonList.add(btnNext);

        btnFinalize = new GuiButtonVariableDims(2, l + (xSize - 100) / 2, i1 + ySize - 28, I18n.format("am2.gui.finalizeSpell")).setDimensions(100, 20);
        this.buttonList.add(btnFinalize);

        int IIcon_start_x = l + 12;
        int IIcon_start_y = i1 + 50;

        int btnX = IIcon_start_x;
        int btnY = IIcon_start_y;
        int id = 3;
        int IIconCount = 0;
        int curPage = 0;

        for (ResourceLocation location : ArsMagicaModelLoader.spellIcons) {
            TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
            GuiSpellImageButton spellButton = new GuiSpellImageButton(id++, btnX, btnY, icon, IIconCount++, curPage);
            if (curPage != 0) {
                spellButton.visible = false;
            }
            this.buttonList.add(spellButton);
            btnX += 14;
            if (btnX > (l + xSize) - 15) {
                btnX = IIcon_start_x;
                btnY += 14;
                if (btnY > (i1 + ICON_GRID_BOTTOM)) {
                    btnY = IIcon_start_y;
                    curPage++;
                }
            }
        }

        this.numPages = curPage;
    }

    @Override
    protected void mouseClicked(int x, int y, int par3) throws IOException {
        super.mouseClicked(x, y, par3);
        spellName.mouseClicked(x, y, par3);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        mc.renderEngine.bindTexture(background);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        // draw the top of the texture, then its bottom edge, skipping the removed middle section
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize - FOOTER_HEIGHT);
        drawTexturedModalRect(l, i1 + ySize - FOOTER_HEIGHT, 0, TEXTURE_HEIGHT - FOOTER_HEIGHT, xSize, FOOTER_HEIGHT);
        boolean empty = spellName.getText().isEmpty();
        boolean focused = spellName.isFocused();
        // hide the caret while empty so it doesn't overlap the placeholder
        if (empty && focused) spellName.setFocused(false);
        spellName.drawTextBox();
        if (empty && focused) spellName.setFocused(true);
        if (empty) {
            fontRenderer.drawStringWithShadow(I18n.format("am2.gui.spellNamePlaceholder"), spellName.x + 4, spellName.y + 4, 0x707070);
        }
    }

}
