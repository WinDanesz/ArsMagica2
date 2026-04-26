package am2.client.gui;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistryHelper;
import am2.api.math.AMVector2;
import am2.api.skill.Skill;
import am2.api.spell.*;
import am2.client.gui.controls.GuiButtonVariableDims;
import am2.client.gui.controls.GuiSlideControl;
import am2.client.texture.SpellIconManager;
import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.container.ContainerInscriptionTable;
import am2.common.extensions.SkillData;
import am2.common.spell.SpellValidator.ValidationResult;
import am2.common.spell.modifier.IEBWizExclusive;
import am2.common.spell.shape.MissingShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class GuiInscriptionTable extends GuiContainer {

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/inscription_table_gui.png");

    private final EntityPlayer usingPlayer;

    private final ArrayList<String> knownShapes;
    private final ArrayList<String> knownComponents;
    private final ArrayList<String> knownModifiers;

    private SpellPart hoveredItem;
    private TextureAtlasSprite hoveredIcon;
    private boolean dragging;
    private boolean lowerHover;
    private int lowerHoverIndex;
    private int lowerHoverShapeGroup = -1;

    private int lastMouseX;
    private int lastMouseY;

    private int iconX;
    private int iconY;
    int IIconXStart_upper = 41;
    int IIconYStart_upper = 5;

    int IIconXStart_lower = 41;
    int IIconYStart_lower = 146;

    int shapeGroupWidth = 37;
    int shapeGroupPadding = 3;

    int shapeGroupY = 108;
    int shapeGroupX = 13;

    AMVector2 searchFieldPosition;
    AMVector2 nameFieldPosition;

    AMVector2 searchFieldDimensions;
    AMVector2 nameFieldDimensions;

    int IIconStep = 17;
    int IIconMaxY_upper = 42; // Maximum Y position for visible icons in upper area

    // Spell parts scrollbar constants (design choices for optimal layout)
    private static final int ICONS_PER_ROW = 8; // Icons displayed per row
    private static final int VISIBLE_ROWS = 3; // Rows visible without scrolling

    private ValidationResult result;
    private ValidationResult cachedResult; // Cache validation result
    private int lastRecipeHash = 0; // Track recipe changes

    private GuiTextField searchBar;
    private GuiTextField nameBar;
    private GuiButtonVariableDims createSpellButton;
    private GuiButtonVariableDims resetSpellButton;
    private GuiSlideControl scrollBar;

    private int spellPartScrollOffset = 0;

    private String defaultSearchLabel = "\2477\247o" + I18n.format("am2.gui.search");
    private String defaultNameLabel = "\2477\247o" + I18n.format("am2.gui.name");

    private boolean wasEBWizMode = false;

    public GuiInscriptionTable(InventoryPlayer playerInventory, TileEntityInscriptionTable table) {
        super(new ContainerInscriptionTable(table, playerInventory));
        usingPlayer = playerInventory.player;
        xSize = 220;
        ySize = 252;
        dragging = false;

        knownShapes = SkillData.For(this.usingPlayer).getKnownShapes();
        knownComponents = SkillData.For(this.usingPlayer).getKnownComponents();
        knownModifiers = SkillData.For(this.usingPlayer).getKnownModifiers();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    public void initGui() {
        super.initGui();
        searchFieldPosition = new AMVector2(39, 59);
        searchFieldDimensions = new AMVector2(141, 12);
        searchBar = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, searchFieldPosition.iX, searchFieldPosition.iY, searchFieldDimensions.iX, searchFieldDimensions.iY);

        nameFieldPosition = new AMVector2(39, 93);
        nameFieldDimensions = new AMVector2(141, 12);
        nameBar = new GuiTextField(1, Minecraft.getMinecraft().fontRenderer, nameFieldPosition.iX, nameFieldPosition.iY, nameFieldDimensions.iX, nameFieldDimensions.iY);


        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;

        createSpellButton = new GuiButtonVariableDims(0, l - 65, i1, I18n.format("am2.gui.makeSpell"));
        createSpellButton.setDimensions(60, 20);

        resetSpellButton = new GuiButtonVariableDims(1, l + 120, i1 + 72, I18n.format("am2.gui.resetSpell"));
        resetSpellButton.setDimensions(60, 20);
        resetSpellButton.visible = false;

        this.buttonList.add(createSpellButton);
        if (!usingPlayer.capabilities.isCreativeMode) {
            createSpellButton.visible = false;
        }

        this.buttonList.add(resetSpellButton);

        // Initialize scrollbar for spell parts
        int totalParts = knownShapes.size() + knownComponents.size() + knownModifiers.size();
        int totalRows = (totalParts + ICONS_PER_ROW - 1) / ICONS_PER_ROW;
        int scrollableRows = Math.max(0, totalRows - VISIBLE_ROWS);

        scrollBar = new GuiSlideControl(2, l + 188, i1 + IIconYStart_upper, 50, "", 0, 0, scrollableRows);
        scrollBar.setVertical();
        scrollBar.setButtonOnly();
        scrollBar.setOverrideTexture(background);
        scrollBar.setButtonProperties(184, 193, 190, 193, 6, 15);
        scrollBar.setScale(1.0f);
        scrollBar.setNoDynamicDisplay(true);
        scrollBar.enabled = scrollableRows > 0;
        scrollBar.visible = scrollableRows > 0;

        this.buttonList.add(scrollBar);

        nameBar.setText(((ContainerInscriptionTable) this.inventorySlots).getSpellName());
        if (nameBar.getText().equals("")) {
            nameBar.setText(defaultNameLabel);
        }

        // Prefill spell name from EBWiz binding when GUI opens in EBWiz mode
        wasEBWizMode = isEBWizMode();
        if (wasEBWizMode) {
            setNameBarFromEBWizBinding();
        }

        searchBar.setText(defaultSearchLabel);

        // Ensure slot 0 is at the correct position on first render (no flicker).
        if (isEBWizPreserveSlotEnabled()) {
            this.inventorySlots.getSlot(0).xPos = wasEBWizMode ? 91 : 102;
        }

        result = ((ContainerInscriptionTable) this.inventorySlots).validateCurrentDefinition();
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) {
        if (par1GuiButton == createSpellButton && (usingPlayer.capabilities.isCreativeMode || isEBWizMode())) {
            ((ContainerInscriptionTable) this.inventorySlots).giveSpellToPlayer(usingPlayer);
        } else if (par1GuiButton == resetSpellButton) {
            ((ContainerInscriptionTable) this.inventorySlots).resetSpellNameAndIcon();
        } else if (par1GuiButton == scrollBar) {
            spellPartScrollOffset = Math.round(scrollBar.getShiftedValue());
        }
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        par1 -= l;
        par2 -= i1;

        if (hoveredItem != null && hoveredIcon != null) {
            // Fix inscription table: Use safe validation method
            if (isSpellPartValid(hoveredItem) && !lowerHover) {
                dragging = true;
            } else if (lowerHover) {
                if (lowerHoverShapeGroup == -1 && ((ContainerInscriptionTable) this.inventorySlots).currentRecipeContains(hoveredItem)) {
                    if (hoveredItem instanceof SpellShape) {
                        int index = lowerHoverIndex;
                        int startIndex = index;
                        int count = 0;
                        index++;
                        while (index < ((ContainerInscriptionTable) this.inventorySlots).getCurrentRecipeSize() && !(((ContainerInscriptionTable) this.inventorySlots).getRecipeItemAt(index) instanceof SpellShape)) {
                            count++;
                            index++;
                        }
                        ((ContainerInscriptionTable) this.inventorySlots).removeMultipleRecipeParts(startIndex, count);
                    } else {
                        ((ContainerInscriptionTable) this.inventorySlots).removeSingleRecipePart(lowerHoverIndex);
                    }
                    // Fix inscription table: Use cached validation result
                    result = getCachedValidationResult();
                } else if (lowerHoverShapeGroup >= 0 && !isEBWizMode()) {
                    if (hoveredItem instanceof SpellShape) {
                        int index = lowerHoverIndex;
                        int startIndex = index;
                        int count = 0;
                        index++;
                        while (index < ((ContainerInscriptionTable) this.inventorySlots).getShapeGroupSize(lowerHoverShapeGroup) &&
                                !(((ContainerInscriptionTable) this.inventorySlots).getShapeGroupPartAt(lowerHoverShapeGroup, index) instanceof SpellShape)) {
                            count++;
                            index++;
                        }
                        ((ContainerInscriptionTable) this.inventorySlots).removeMultipleRecipePartsFromGroup(lowerHoverShapeGroup, startIndex, count);
                    } else {
                        ((ContainerInscriptionTable) this.inventorySlots).removeSingleRecipePartFromGroup(lowerHoverShapeGroup, lowerHoverIndex);
                    }
                    // Fix inscription table: Use cached validation result
                    result = getCachedValidationResult();
                }
            }
        } else {
            boolean boxClick = false;
            if (par1 >= searchFieldPosition.iX && par1 <= searchFieldPosition.iX + searchFieldDimensions.iX) {
                if (par2 >= searchFieldPosition.iY && par2 <= searchFieldPosition.iY + searchFieldDimensions.iY) {
                    if (par3 == 1 || searchBar.getText().equals(defaultSearchLabel)) {
                        searchBar.setText("");
                    }
                    if (nameBar.getText().equals("")) {
                        nameBar.setText(defaultNameLabel);
                    }
                    boxClick = true;
                }
            }
            if (par1 >= nameFieldPosition.iX && par1 <= nameFieldPosition.iX + nameFieldDimensions.iX) {
                if (par2 >= nameFieldPosition.iY && par2 <= nameFieldPosition.iY + nameFieldDimensions.iY) {
                    if (par3 == 1 || nameBar.getText().equals(defaultNameLabel)) {
                        nameBar.setText("");
                        ((ContainerInscriptionTable) this.inventorySlots).setSpellName(nameBar.getText());
                    }
                    if (searchBar.getText().equals("")) {
                        searchBar.setText(defaultSearchLabel);
                    }
                    boxClick = true;
                }
            }
            if (!boxClick) {
                if (nameBar.getText().equals("")) {
                    nameBar.setText(defaultNameLabel);
                }
                if (searchBar.getText().equals("")) {
                    searchBar.setText(defaultSearchLabel);
                }
            }
            searchBar.mouseClicked(par1, par2, par3);
            nameBar.mouseClicked(par1, par2, par3);
        }
    }

    @Override
    protected void mouseReleased(int x, int y, int action) {
        super.mouseReleased(x, y, action);

        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        x -= l;
        y -= i1;

        if (action == 0 || action == 1) {
            if (dragging) {
                dragging = false;
                //lower section
                if (x >= IIconXStart_lower && x <= IIconXStart_lower + 150) {
                    if (y >= IIconYStart_lower && y <= IIconYStart_lower + 18) {
                        ((ContainerInscriptionTable) this.inventorySlots).addRecipePart(hoveredItem);
                        // Fix inscription table: Use cached validation result
                        result = getCachedValidationResult();
                    }
                }
                //spell stage groups
                if (!isEBWizMode())
                for (int i = 0; i < ((ContainerInscriptionTable) this.inventorySlots).getNumStageGroups(); ++i) {
                    int SGX = shapeGroupX + ((shapeGroupWidth + shapeGroupPadding) * i);
                    int SGY = shapeGroupY;
                    if (x >= SGX && x <= SGX + shapeGroupWidth) {
                        if (y >= SGY && y <= SGY + shapeGroupWidth) {
                            ((ContainerInscriptionTable) this.inventorySlots).addRecipePartToGroup(i, hoveredItem);
                            // Fix inscription table: Use cached validation result
                            result = getCachedValidationResult();
                        }
                    }
                }
            }
        }
    }

//	private void drawDropZones(){
//
//		int l = (width - xSize) / 2;
//		int i1 = (height - ySize) / 2;
//
//		drawRectangle(l + IIconXStart_upper, i1 + IIconYStart_upper, 150, 60, 0xFF0000);
//		drawRectangle(l + IIconXStart_lower, i1 + IIconYStart_lower, 150, 20, 0xFF0000);
//
//		int sg = ((ContainerInscriptionTable)this.inventorySlots).getNumStageGroups();
//		for (int i = 0; i < sg; ++i){
//			int SGX = l + shapeGroupX + ((shapeGroupWidth + shapeGroupPadding) * i);
//			int SGY = i1 + shapeGroupY;
//
//			drawRectangle(SGX, SGY, shapeGroupWidth, shapeGroupWidth, 0xFF0000);
//		}
//	}
//
//	private void drawRectangle(int x, int y, int width, int height, int color){
//		AMGuiHelper.line2d(x, y, x + width, y, this.zLevel, color);
//		AMGuiHelper.line2d(x + width, y, x + width, y + height, this.zLevel, color);
//		AMGuiHelper.line2d(x, y + height, x + width, y + height, this.zLevel, color);
//		AMGuiHelper.line2d(x, y, x, y + height, this.zLevel, color);
//	}

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, button, timeSinceLastClick);
        if (scrollBar.dragging) {
            spellPartScrollOffset = Math.round(scrollBar.getShiftedValue());
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseWheel = Mouse.getEventDWheel();
        if (mouseWheel != 0 && scrollBar.enabled) {
            int direction = mouseWheel > 0 ? -1 : 1;
            spellPartScrollOffset = Math.max(0, Math.min((int) scrollBar.getMaximum(), spellPartScrollOffset + direction));
            updateSliderValue();
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (searchBar.textboxKeyTyped(par1, par2)) {
            recalculateScrollbar();
        } else if (nameBar.textboxKeyTyped(par1, par2)) {
            ((ContainerInscriptionTable) this.inventorySlots).setSpellName(nameBar.getText());
        } else {
            super.keyTyped(par1, par2);
        }
    }

    private boolean isEBWizMode() {
        return ((ContainerInscriptionTable) this.inventorySlots).isEBWizMode();
    }

    private boolean isEBWizPreserveMode() {
        return ((ContainerInscriptionTable) this.inventorySlots).isEBWizPreserveMode();
    }

    private boolean isEBWizPreserveSlotEnabled() {
        return ArsMagica.config.getEBWizPreserveSpellBook();
    }

    private void setNameBarFromEBWizBinding() {
        String spellName = EBWizardryCompatBootstrap.getEBWizSpellBookDisplayName(
                this.inventorySlots.getSlot(0).getStack());
        if (spellName == null || spellName.isEmpty()) {
            spellName = this.inventorySlots.getSlot(0).getStack().getDisplayName();
        }
        if (spellName != null && !spellName.isEmpty()) {
            nameBar.setText(spellName);
            ((ContainerInscriptionTable) this.inventorySlots).setSpellName(spellName);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        boolean ebwizNow = isEBWizMode();
        if (ebwizNow && !wasEBWizMode) {
            setNameBarFromEBWizBinding();
        }
        // Refresh validation whenever EBWiz mode changes in either direction.
        if (ebwizNow != wasEBWizMode) {
            cachedResult = null; // force re-validation
            result = ((ContainerInscriptionTable) this.inventorySlots).validateCurrentDefinition();
            cachedResult = result;
        }
        wasEBWizMode = ebwizNow;

        // In EBWiz mode the result is always valid; refresh so the button is
        // never stuck greyed-out from a previous non-EBWiz validation.
        if (ebwizNow) {
            result = ((ContainerInscriptionTable) this.inventorySlots).validateCurrentDefinition();
            cachedResult = result;
        }

        // Reposition and show/hide the Make Spell button depending on mode
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        if (ebwizNow) {
            // Center a wider button inside the shape groups bar area
            int btnW = 120;
            int sgTotalW = TileEntityInscriptionTable.getMaxStageGroups() * (shapeGroupWidth + shapeGroupPadding) - shapeGroupPadding;
            int btnX = l + shapeGroupX + (sgTotalW - btnW) / 2;
            int btnY = i1 + shapeGroupY + (shapeGroupWidth - 20) / 2;
            createSpellButton.x = btnX;
            createSpellButton.y = btnY;
            createSpellButton.setDimensions(btnW, 20);
            createSpellButton.visible = true;
        } else {
            // Normal position (left of GUI, creative only)
            createSpellButton.x = l - 65;
            createSpellButton.y = i1;
            createSpellButton.setDimensions(60, 20);
            createSpellButton.visible = usingPlayer.capabilities.isCreativeMode;
        }

        // In EBWiz preserve mode, reposition slot 0 so it is centred when alone
        // and shifts left when the secondary slot is also visible.
        if (isEBWizPreserveSlotEnabled()) {
            this.inventorySlots.getSlot(0).xPos = ebwizNow ? 91 : 102;
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void updateSliderValue() {
        float range = scrollBar.getMaximum() - scrollBar.getMinimum();
        if (range > 0) {
            // Clamp offset to valid range before calculating slider value
            float clampedOffset = clamp(spellPartScrollOffset, scrollBar.getMinimum(), scrollBar.getMaximum());
            scrollBar.setSliderValue((clampedOffset - scrollBar.getMinimum()) / range);
        }
    }

    private int countFilteredSkills(ArrayList<String> skillIds, String filterText, boolean hasFilter) {
        boolean ebwizMode = isEBWizMode();
        int count = 0;
        for (String id : skillIds) {
            Skill part = Skill.fromName(id);
            if (part == null || ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName()) == null)
                continue;
            if (hasFilter && !part.getName().toLowerCase().contains(filterText))
                continue;
            if (ebwizMode) {
                SpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName());
                if (!(spellPart instanceof SpellModifier)) continue;
                SpellModifier mod = (SpellModifier) spellPart;
                if (mod instanceof IEBWizExclusive) { /* always count EBWiz-exclusive in EBWiz mode */ }
                else {
                    EnumSet<SpellModifiers> aspects = mod.getAspectsModified();
                    if (!aspects.contains(SpellModifiers.RANGE) && !aspects.contains(SpellModifiers.DURATION)) continue;
                }
            } else {
                SpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName());
                if (spellPart instanceof IEBWizExclusive) continue; // hide EBWiz-exclusive in normal mode
            }
            count++;
        }
        return count;
    }

    private void recalculateScrollbar() {
        String filterText = searchBar.getText().toLowerCase();
        boolean hasFilter = !filterText.isEmpty() && !filterText.equals(defaultSearchLabel.toLowerCase());

        int filteredCount;
        if (isEBWizMode()) {
            filteredCount = countFilteredSkills(knownModifiers, filterText, hasFilter);
        } else {
            filteredCount = countFilteredSkills(knownShapes, filterText, hasFilter)
                    + countFilteredSkills(knownComponents, filterText, hasFilter)
                    + countFilteredSkills(knownModifiers, filterText, hasFilter);
        }

        int totalRows = (filteredCount + ICONS_PER_ROW - 1) / ICONS_PER_ROW;
        int scrollableRows = Math.max(0, totalRows - VISIBLE_ROWS);

        scrollBar.setMaximum(scrollableRows);
        scrollBar.enabled = scrollableRows > 0;
        scrollBar.visible = scrollableRows > 0;

        // Always clamp the offset to the new valid range
        if (spellPartScrollOffset > scrollableRows) {
            spellPartScrollOffset = scrollableRows;
        }

        // Force update the slider position after filter change
        updateSliderValue();

        // If scrolling is disabled, reset to top
        if (!scrollBar.enabled) {
            spellPartScrollOffset = 0;
            scrollBar.setSliderValue(0);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        mc.renderEngine.bindTexture(background);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, 165);
        drawTexturedModalRect(l + 22, i1 + 165, 0, 165, 176, 87);


        if (!isEBWizMode()) {
            int offsetX = l + shapeGroupX;
            for (int sg = 0; sg < TileEntityInscriptionTable.getMaxStageGroups(); ++sg) {
                if (sg >= ((ContainerInscriptionTable) this.inventorySlots).getNumStageGroups())
                    GL11.glColor3f(0.5f, 0.5f, 0.5f);
                drawTexturedModalRect(offsetX + (sg * (shapeGroupWidth + shapeGroupPadding)), i1 + shapeGroupY, 176, 165, 37, 37);
            }
        }

        GL11.glColor3f(1f, 1f, 1f);

        if (isEBWizPreserveSlotEnabled()) {
            if (isEBWizMode()) {
                // Two centred slots: primary shifted left, secondary to its right.
                drawTexturedModalRect(l + 90,  i1 + 73, 220, 0, 18, 18);
                drawTexturedModalRect(l + 112, i1 + 73, 220, 0, 18, 18);
            } else {
                // Only the primary slot, centred at the normal position.
                drawTexturedModalRect(l + 101, i1 + 73, 220, 0, 18, 18);
            }
        } else {
            drawTexturedModalRect(l + 101, i1 + 73, 220, 0, 18, 18);
        }

        // Draw scrollbar rail background
        if (scrollBar != null && scrollBar.enabled) {
            int scrollX = l + 188;
            int scrollY = i1 + IIconYStart_upper;
            // Draw a darker shaded background for the scrollbar rail
            drawRect(scrollX, scrollY, scrollX + 12, scrollY + 50, 0xAA000000);
        }

        lastMouseX = i - l;
        lastMouseY = j - i1;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);

        ArrayList<String> label = new ArrayList<String>();

        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;

        this.zLevel = 0.0F;

        GL11.glEnable(GL11.GL_ALPHA_TEST);

        drawBookIcon();
        drawWritableBookSlotHint();
        boolean hovering = false;
        if (drawAvailableParts(label)) {
            hovering = true;
            lowerHover = false;
        }
        if (drawCurrentRecipe(label, l, i1)) {
            hovering = true;
            lowerHover = true;
        }
        searchBar.drawTextBox();
        nameBar.drawTextBox();

        if (result.valid) {
            if (((ContainerInscriptionTable) this.inventorySlots).slotHasStack(0)) {
                if (((ContainerInscriptionTable) this.inventorySlots).slotIsBook(0)) {
                    Minecraft.getMinecraft().fontRenderer.drawSplitString(I18n.format("am2.gui.bookOut"), 225, 5, 100, 0xFF7700);
                } else if (!isEBWizMode()) {
                    resetSpellButton.visible = true;
                } else {
                    resetSpellButton.visible = false;
                }

            } else {
                resetSpellButton.visible = false;
            }
            createSpellButton.enabled = true;
        } else {
            if (((ContainerInscriptionTable) this.inventorySlots).slotHasStack(0) && !((ContainerInscriptionTable) this.inventorySlots).slotIsBook(0) && !isEBWizMode()) {
                resetSpellButton.visible = true;
            } else {
                resetSpellButton.visible = false;
            }
            Minecraft.getMinecraft().fontRenderer.drawSplitString(result.message, 225, 5, 100, 0xFF7700);
            // In EBWiz mode the recipe is empty by design.
            // Creative: always enabled when in EBWiz mode.
            // Survival: enabled only when all required items are present.
            if (isEBWizMode()) {
                if (usingPlayer.capabilities.isCreativeMode) {
                    createSpellButton.enabled = true;
                } else {
                    ContainerInscriptionTable cont = (ContainerInscriptionTable) this.inventorySlots;
                    boolean hasBook = cont.slotHasStack(0);
                    // In preserve mode the writable-book placeholder must also be present (container slot 1)
                    boolean hasPlaceholder = !isEBWizPreserveMode() || cont.slotIsBook(1);
                    createSpellButton.enabled = hasBook && hasPlaceholder;
                }
            } else {
                createSpellButton.enabled = false;
            }
        }

        if (!dragging) {
            if (hovering) {
                drawHoveringText(label, lastMouseX, lastMouseY, Minecraft.getMinecraft().fontRenderer);
            } else {
                hoveredItem = null;
                hoveredIcon = null;
            }
        } else {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            drawDraggedItem();
        }

    }

    private void drawBookIcon() {
        // Don't draw the ghost if something is already in the slot.
        if (this.inventorySlots.getSlot(0).getHasStack()) return;

        int bookX = this.inventorySlots.getSlot(0).xPos;
        int bookY = this.inventorySlots.getSlot(0).yPos;

        TextureAtlasSprite icon = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.WRITABLE_BOOK);

        if (AMGuiHelper.instance.getFastTicker() < 20)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.4f);
        else
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.7f);
        AMGuiHelper.DrawIconAtXY(icon, bookX, bookY, this.zLevel, 16, 16, true);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * When the EBWiz preserve-book feature is active, draws a ghost writable-book
     * icon over the secondary slot to hint that a writable book should be placed there.
     * Only drawn when the slot is empty and the table is in EBWiz mode.
     */
    private void drawWritableBookSlotHint() {
        if (!isEBWizPreserveSlotEnabled()) return;
        if (!isEBWizMode()) return;
        // The secondary slot (inventorySlots index 1) holds the writable book in preserve mode.
        if (this.inventorySlots.getSlot(1).getHasStack()) return;

        int slotX = this.inventorySlots.getSlot(1).xPos;
        int slotY = this.inventorySlots.getSlot(1).yPos;

        TextureAtlasSprite icon = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.WRITABLE_BOOK);
        float alpha = AMGuiHelper.instance.getFastTicker() < 20 ? 0.4f : 0.7f;
        GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
        AMGuiHelper.DrawIconAtXY(icon, slotX, slotY, this.zLevel, 16, 16, true);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private boolean drawCurrentRecipe(ArrayList<String> labelText, int l, int i1) {

        iconX = IIconXStart_lower;
        iconY = IIconYStart_lower;

        boolean hovering = false;
        int index = 0;

        //main recipe
        for (int i = 0; i < ((ContainerInscriptionTable) this.inventorySlots).getCurrentRecipeSize(); ++i) {
            SpellPart part = ((ContainerInscriptionTable) this.inventorySlots).getRecipeItemAt(i);
            if (part == new MissingShape())
                continue;
            String name = SpellRegistryHelper.getSkillFromPart(part).getName();

            if (drawIcon(part, false)) {
                labelText.add(name);
                hovering = true;
                lowerHoverIndex = index;
                lowerHoverShapeGroup = -1;
            }
            index++;
        }

        //shape groups
        if (!isEBWizMode())
        for (int i = 0; i < ((ContainerInscriptionTable) this.inventorySlots).getNumStageGroups(); ++i) {
            for (int n = 0; n < ((ContainerInscriptionTable) this.inventorySlots).getShapeGroupSize(i); ++n) {
                SpellPart part = ((ContainerInscriptionTable) this.inventorySlots).getShapeGroupPartAt(i, n);
                String name = SpellRegistryHelper.getSkillFromPart(part).getName();

                int SGX = shapeGroupX + ((shapeGroupWidth + shapeGroupPadding) * i) + 1;
                int SGY = shapeGroupY;

                iconX = SGX + (n % 2) * IIconStep;
                iconY = SGY + (int) Math.floor(n / 2) * IIconStep;

                if (drawIcon(part, false)) {
                    labelText.add(name);
                    hovering = true;
                    lowerHoverIndex = n;
                    lowerHoverShapeGroup = i;
                }
            }
        }
        return hovering;
    }

    private boolean drawAvailableParts(ArrayList<String> labelText) {

        iconX = IIconXStart_upper;
        iconY = IIconYStart_upper - (spellPartScrollOffset * IIconStep);

        boolean b = drawPartIcons(labelText);

        return b;
    }

    private boolean drawPartIcons(ArrayList<String> labelText) {
        boolean hovering = false;
        boolean ebwizMode = isEBWizMode();
        if (!ebwizMode) {
            hovering |= drawIconSet(knownShapes, labelText);
            hovering |= drawIconSet(knownComponents, labelText);
        }
        hovering |= drawIconSet(knownModifiers, labelText);
        return hovering;
    }

    private boolean drawIconSet(ArrayList<String> ids, ArrayList<String> labelText) {
        boolean hovering = false;
        boolean ebwizMode = isEBWizMode();
        for (String i : ids) {
            Skill part = Skill.fromName(i);

            if (part == null || ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName()) == null)// && SkillTreeManager.instance.isSkillDisabled(part))
                continue;

            SpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName());

            // EBWiz mode filtering
            if (ebwizMode) {
                if (!(spellPart instanceof SpellModifier)) continue;
                SpellModifier mod = (SpellModifier) spellPart;
                if (!(mod instanceof IEBWizExclusive)) {
                    EnumSet<SpellModifiers> aspects = mod.getAspectsModified();
                    if (!aspects.contains(SpellModifiers.RANGE) && !aspects.contains(SpellModifiers.DURATION)) continue;
                }
            } else {
                // Normal mode: hide EBWiz-exclusive modifiers
                if (spellPart instanceof IEBWizExclusive) continue;
            }

            String name = part.getName();

            String filterText = searchBar.getText().toLowerCase();
            if (!filterText.isEmpty() && !filterText.equals(defaultSearchLabel.toLowerCase()) && !name.toLowerCase().contains(filterText)) {
                continue;
            }

            // Skip rendering icons that are outside the visible area but continue iterating
            if (iconY < IIconYStart_upper || iconY > IIconMaxY_upper) {
                // Still need to advance position for proper layout
                iconX += IIconStep;
                if (iconX >= 175) {
                    iconX = IIconXStart_upper;
                    iconY += 17;
                }
                continue;
            }

            if (drawIcon(spellPart)) {
                hovering = true;
                labelText.add(name);
            }
        }
        return hovering;
    }

    private boolean spellPartIsValidAddition(SpellPart part) {
        if (isEBWizMode()) {
            if (!(part instanceof SpellModifier)) return false;
            SpellModifier mod = (SpellModifier) part;
            if (mod instanceof IEBWizExclusive) return ((ContainerInscriptionTable) this.inventorySlots).modifierCanBeAdded(mod);
            EnumSet<SpellModifiers> aspects = mod.getAspectsModified();
            if (aspects.contains(SpellModifiers.RANGE) || aspects.contains(SpellModifiers.DURATION))
                return ((ContainerInscriptionTable) this.inventorySlots).modifierCanBeAdded(mod);
            return false;
        }
        // Normal mode: EBWiz-exclusive modifiers can't be added
        if (part instanceof IEBWizExclusive) return false;
        boolean hasShape = false;
        for (int i = 0; i < ((ContainerInscriptionTable) this.inventorySlots).getNumStageGroups(); ++i) {
            for (int n = 0; n < ((ContainerInscriptionTable) this.inventorySlots).getShapeGroupSize(i); ++n) {
                SpellPart groupPart = ((ContainerInscriptionTable) this.inventorySlots).getShapeGroupPartAt(i, n);
                if (groupPart instanceof SpellShape) {
                    hasShape = true;
                    break;
                }
            }
        }
        if (!hasShape && !(part instanceof SpellShape))
            return false;
        if (part instanceof SpellShape && ((ContainerInscriptionTable) this.inventorySlots).currentRecipeContains(part))
            return false;
        if (part instanceof SpellComponent) {
            int index = ((ContainerInscriptionTable) this.inventorySlots).getCurrentRecipeSize() - 1;
            while (index >= 0 && !(((ContainerInscriptionTable) this.inventorySlots).getRecipeItemAt(index) instanceof SpellShape)) {
                SpellPart curPart = ((ContainerInscriptionTable) this.inventorySlots).getRecipeItemAt(index--);
                if (curPart instanceof SpellComponent && SpellRegistryHelper.getSkillFromPart(curPart).getID() == SpellRegistryHelper.getSkillFromPart(part).getID()) {
                    return false;
                }
            }
        }
        if (part instanceof SpellModifier) {
            return ((ContainerInscriptionTable) this.inventorySlots).modifierCanBeAdded((SpellModifier) part);
        }
        return true;
    }

    private boolean drawIcon(SpellPart part) {
        return drawIcon(part, true);
    }

    private boolean drawIcon(SpellPart part, boolean allowDarken) {

        boolean hovering = false;
        TextureAtlasSprite shapeIcon = SpellIconManager.INSTANCE.getSprite(SpellRegistryHelper.getSkillFromPart(part).getID());

        if (shapeIcon == null)
            return false;

        if (!currentSpellDefIsReadOnly()) {
            if (!spellPartIsValidAddition(part) && allowDarken) {
                GL11.glColor3f(0.3f, 0.3f, 0.3f);
            } else {
                GL11.glColor3f(1.0f, 1.0f, 1.0f);
            }
        } else {
            GL11.glColor3f(1.0f, 0.7f, 0.7f);
        }

        AMGuiHelper.DrawIconAtXY(shapeIcon, iconX, iconY, this.zLevel, 16, 16, false);

        if (!dragging) {
            if (lastMouseX > iconX && lastMouseX < iconX + 16) {
                if (lastMouseY > iconY && lastMouseY < iconY + 16) {
                    hoveredItem = part;
                    hoveredIcon = shapeIcon;
                    hovering = true;
                }
            }
        }

        iconX += IIconStep;
        if (iconX >= 175) {
            iconX = IIconXStart_upper;
            iconY += 17;
        }

        return hovering;
    }

    private void drawDraggedItem() {
        AMGuiHelper.DrawIconAtXY(hoveredIcon, lastMouseX - 8, lastMouseY - 8, this.zLevel, 16, 16, false);
    }

    @Override
    protected void drawHoveringText(List<String> par1List, int par2, int par3, FontRenderer font) {
        if (!par1List.isEmpty()) {
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            int k = 0;
            Iterator<String> iterator = par1List.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                int l = font.getStringWidth(s);

                if (l > k) {
                    k = l;
                }
            }

            int i1 = par2 + 12;
            int j1 = par3 - 12;
            int k1 = 8;

            if (par1List.size() > 1) {
                k1 += 2 + (par1List.size() - 1) * 10;
            }

            if (i1 + k > this.width) {
                i1 -= 28 + k;
            }

            if (j1 + k1 + 6 > this.height) {
                j1 = this.height - k1 - 6;
            }

            this.zLevel = 300.0F;
            int l1 = -267386864;
            this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
            this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
            this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
            int i2 = 1347420415;
            int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
            this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
            this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
            this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

            for (int k2 = 0; k2 < par1List.size(); ++k2) {
                String s1 = (String) par1List.get(k2);
                font.drawStringWithShadow(s1, i1, j1, -1);

                if (k2 == 0) {
                    j1 += 2;
                }

                j1 += 10;
            }

            this.zLevel = 0.0F;
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            RenderHelper.enableStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        }
    }

    private boolean currentSpellDefIsReadOnly() {
        return ((ContainerInscriptionTable) this.inventorySlots).currentSpellDefIsReadOnly();
    }

    // Fix inscription table: Add cached validation to improve performance
    private ValidationResult getCachedValidationResult() {
        ContainerInscriptionTable container = (ContainerInscriptionTable) this.inventorySlots;

        // Build a comprehensive hash that includes all spell parts
        int currentHash = container.getCurrentRecipeSize();

        // Hash main recipe parts
        for (int i = 0; i < container.getCurrentRecipeSize(); i++) {
            SpellPart part = container.getRecipeItemAt(i);
            if (part != null) {
                currentHash = currentHash * 31 + part.hashCode();
            }
        }

        // Hash shape groups
        currentHash = currentHash * 31 + container.getNumStageGroups();
        for (int i = 0; i < container.getNumStageGroups(); i++) {
            currentHash = currentHash * 31 + container.getShapeGroupSize(i);
            for (int j = 0; j < container.getShapeGroupSize(i); j++) {
                SpellPart part = container.getShapeGroupPartAt(i, j);
                if (part != null) {
                    currentHash = currentHash * 31 + part.hashCode();
                }
            }
        }

        if (cachedResult == null || lastRecipeHash != currentHash) {
            cachedResult = container.validateCurrentDefinition();
            lastRecipeHash = currentHash;
        }

        return cachedResult;
    }

    // Fix inscription table: Add null-safe spell part validation
    private boolean isSpellPartValid(SpellPart part) {
        if (part == null) return false;

        try {
            return spellPartIsValidAddition(part);
        } catch (Exception e) {
            System.err.println("Error validating spell part: " + e.getMessage());
            return false;
        }
    }
}
