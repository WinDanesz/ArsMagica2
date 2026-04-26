package am2.client.gui;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SkillPointRegistry;
import am2.api.SkillTreeRegistry;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.extensions.ISkillData;
import am2.api.skill.Skill;
import am2.api.skill.SkillPoint;
import am2.api.skill.SkillTree;
import am2.client.gui.controls.GuiButtonSkillTree;
import am2.client.gui.controls.GuiButtonDisciplinePlus;
import am2.client.texture.SpellIconManager;
import am2.common.extensions.AffinityData;
import am2.common.extensions.SkillData;
import am2.common.registry.Affinities;
import am2.common.registry.SkillTrees;
import am2.common.skill.Discipline;
import am2.common.utils.RenderUtils;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketDisciplineLevelUp;
import am2.network.packets.PacketOcculusUnlock;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class GuiOcculus extends GuiScreen {
    int xSize = 210;
    int ySize = 210;
    SkillTree currentTree = SkillTrees.TREE_OFFENSE;
    EntityPlayer player;
    int currentTabId = 0;

    private boolean isDragging = false;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private int page = 0;
    private int maxPage = 0;
    private int offsetX = VIEWPORT_BOUNDS / 2 - 82 + 8;
    private int offsetY = 0;
    private Skill hoverItem = null;

    private static final int DISCIPLINE_BUTTON_ID_START = 1000;
    private static final int DISCIPLINE_CONFIRM_BUTTON_ID = 2000;

    /** The discipline currently focused/zoomed in on; null if none. */
    private Discipline selectedDiscipline = null;

    /**
     * Current zoom level for the skill tree view. 1.0 is default (no zoom).
     */
    private float zoomLevel = 1.0F;
    /**
     * Minimum allowed zoom level (zoomed out).
     */
    private static final float MIN_ZOOM = 0.3F;
    /**
     * Maximum allowed zoom level (zoomed in).
     */
    private static final float MAX_ZOOM = 2.0F;
    /**
     * Amount to adjust zoom per scroll wheel notch.
     */
    private static final float ZOOM_STEP = 0.1F;
    /**
     * Render ratio used for background texture scaling and zoom calculations.
     */
    private static final float RENDER_RATIO = 0.29F;
    /**
     * Maximum value for viewport offsets (bounds of the skill tree background).
     */
    private static final int VIEWPORT_BOUNDS = 568;

    public GuiOcculus(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public void initGui() {
        int tabId = 0;
        int posX = width / 2 - xSize / 2;
        int posY = height / 2 - ySize / 2;
        ImmutableList<SkillTree> testTab = SkillTreeRegistry.getSkillTreeMap();
//		ArrayList<SkillTree> test = new ArrayList<>(testTab);
//		for (int i = 0; i < 16; i++) {
//			test.add(new SkillTree("Tree" + i, new ResourceLocation("textures/blocks/dirt.png"), new ResourceLocation("textures/blocks/stone.png")));
//		}
        for (SkillTree entry : testTab) {
            if (tabId % 16 < 8)
                buttonList.add(new GuiButtonSkillTree(tabId, posX + 7 + ((tabId % 16) * 24), posY - 22, entry, (int) Math.floor((float) tabId / 16F), false));
            else
                buttonList.add(new GuiButtonSkillTree(tabId, posX + 7 + (((tabId % 16) - 8) * 24), posY + 210, entry, (int) Math.floor((float) tabId / 16F), true));

            tabId++;
        }
        maxPage = (int) Math.floor((float) (tabId - 1) / 16F);
        for (GuiButton button : buttonList) {
            if (button instanceof GuiButtonSkillTree) {
                button.visible = (int) Math.floor((float) button.id / 16F) == page;
            }
        }
        // Discipline + buttons (positions updated each frame for panning)
        Discipline[] disciplines = Discipline.values();
        for (int i = 0; i < disciplines.length; i++) {
            GuiButtonDisciplinePlus btn = new GuiButtonDisciplinePlus(DISCIPLINE_BUTTON_ID_START + i, 0, 0, disciplines[i]);
            btn.visible = currentTree == SkillTrees.TREE_DISCIPLINE;
            buttonList.add(btn);
        }
        // Confirm level-up button shown in bottom panel when a discipline is focused
        GuiButtonDisciplinePlus confirmBtn = new GuiButtonDisciplinePlus(DISCIPLINE_CONFIRM_BUTTON_ID, 0, 0, 14, 12, Discipline.FIRE);
        confirmBtn.visible = false;
        buttonList.add(confirmBtn);
        super.initGui();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int posX = width / 2 - xSize / 2;
        int posY = height / 2 - ySize / 2;

        // Disable manual scroll/zoom for discipline page
        if (currentTree == SkillTrees.TREE_DISCIPLINE) return;

        // ...existing code for other trees...
        if (mouseX > posX + 7 && mouseX < posX + 203 && mouseY > posY + 7 && mouseY < posY + 203
                && currentTree != SkillTrees.TREE_AFFINITY) {
            int dWheel = Mouse.getEventDWheel();
            if (dWheel != 0) {
                float oldZoom = zoomLevel;
                if (dWheel > 0) {
                    // Scroll up - zoom in
                    zoomLevel = Math.min(zoomLevel + ZOOM_STEP, MAX_ZOOM);
                } else {
                    // Scroll down - zoom out
                    zoomLevel = Math.max(zoomLevel - ZOOM_STEP, MIN_ZOOM);
                }

                // Adjust offsets to zoom towards mouse position
                if (oldZoom != zoomLevel) {
                    // Calculate relative mouse position in the skill tree viewport (0.0 to 1.0)
                    float relativeX = (mouseX - (posX + 7)) / 196.0F;
                    float relativeY = (mouseY - (posY + 7)) / 196.0F;

                    // Normal trees: fixed RENDER_RATIO
                    float zoomRatio = zoomLevel / oldZoom;
                    float oldOffsetXNormalized = offsetX / (float) VIEWPORT_BOUNDS;
                    float oldOffsetYNormalized = offsetY / (float) VIEWPORT_BOUNDS;
                    float newOffsetXNormalized = oldOffsetXNormalized * zoomRatio + relativeX * (zoomRatio - 1) * RENDER_RATIO;
                    float newOffsetYNormalized = oldOffsetYNormalized * zoomRatio + relativeY * (zoomRatio - 1) * RENDER_RATIO;
                    offsetX = (int) (newOffsetXNormalized * VIEWPORT_BOUNDS);
                    offsetY = (int) (newOffsetYNormalized * VIEWPORT_BOUNDS);

                    // Clamp offsets to valid range
                    offsetX = MathHelper.clamp(offsetX, 0, VIEWPORT_BOUNDS);
                    offsetY = MathHelper.clamp(offsetY, 0, VIEWPORT_BOUNDS);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if (hoverItem != null && !SkillData.For(player).hasSkill(hoverItem.getID())) {
                ISkillData data = SkillData.For(player);
                if (data.canLearn(hoverItem.getID())) {
                    AMNetworkHandler.getNetwork().sendToServer(new PacketOcculusUnlock(hoverItem.getID()));
                }
            } else if (this.currentTree != SkillTrees.TREE_AFFINITY && this.currentTree != SkillTrees.TREE_DISCIPLINE && selectedButton == null)
                isDragging = true;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        isDragging = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button instanceof GuiButtonSkillTree) {
            currentTree = ((GuiButtonSkillTree) button).getTree();
            currentTabId = button.id;
            selectedDiscipline = null;
            if (currentTree == SkillTrees.TREE_DISCIPLINE) {
                offsetX = VIEWPORT_BOUNDS / 2;
                offsetY = VIEWPORT_BOUNDS / 2 - 200;
                zoomLevel = MIN_ZOOM;
            } else {
                offsetX = VIEWPORT_BOUNDS / 2 - 82 + 8;
                offsetY = 0;
                zoomLevel = 1.0F;
            }
            // Toggle discipline buttons visibility
            for (GuiButton b : buttonList) {
                if (b instanceof GuiButtonDisciplinePlus) {
                    b.visible = currentTree == SkillTrees.TREE_DISCIPLINE;
                }
            }
        } else if (button.id == DISCIPLINE_CONFIRM_BUTTON_ID && selectedDiscipline != null) {
            SkillData data = (SkillData) SkillData.For(player);
            if (data.canLevelUpDiscipline(selectedDiscipline)) {
                AMNetworkHandler.getNetwork().sendToServer(new PacketDisciplineLevelUp(selectedDiscipline));
            }
        } else if (button instanceof GuiButtonDisciplinePlus) {
            Discipline discipline = ((GuiButtonDisciplinePlus) button).getDiscipline();
            // Select this discipline and zoom in; actual level-up happens via the confirm button
            selectedDiscipline = discipline;
            // Zoom in and center on the discipline's focus area
            zoomLevel = 1.0F;
            float ratio = RENDER_RATIO / zoomLevel;
            float uvX = discipline.getFocusX() / 1024F;
            float uvY = discipline.getFocusY() / 1024F;
            if (ratio < 1.0F) {
                offsetX = Math.round((uvX - 0.5F * ratio) / (1 - ratio) * VIEWPORT_BOUNDS);
                offsetY = Math.round((uvY - 0.5F * ratio) / (1 - ratio) * VIEWPORT_BOUNDS);
                offsetX = MathHelper.clamp(offsetX, 0, VIEWPORT_BOUNDS);
                offsetY = MathHelper.clamp(offsetY, 0, VIEWPORT_BOUNDS);
            }
        }
    }

    private int calcXOffset(int posX, Skill s) {
        return (int) (posX - this.offsetX + s.getPosX() * zoomLevel);
    }

    private int calcYOffset(int posY, Skill s) {
        return (int) (posY - this.offsetY + s.getPosY() * zoomLevel);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // default gray background
        GlStateManager.disableDepth();
        drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        GlStateManager.enableDepth();
        // default gray background
        
        int posX = width / 2 - xSize / 2;
        int posY = height / 2 - ySize / 2;
        float renderSize = 32F * zoomLevel;
        float renderRatio = 0.29F;
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ArsMagica.MODID, "textures/occulus/overlay.png"));
        //Overlay
        drawTexturedModalRect(posX, posY, 0, 0, 210, 210);
        //Tab Under
        if ((int) Math.floor((float) currentTabId / 16F) == page) {
            if ((currentTabId % 16) < 8)
                drawTexturedModalRect(posX + 7 + ((currentTabId % 16) * 24), posY, 22, 210, 22, 7);
            else
                drawTexturedModalRect(posX + 7 + (((currentTabId % 16) - 8) * 24), posY + 203, 22, 210, 22, 7);
        }
        zLevel = -18F;
        if (isDragging) {
            int dx = lastMouseX - mouseX;
            int dy = lastMouseY - mouseY;

            this.offsetX += dx;
            this.offsetY += dy;

            if (this.offsetX < 0) this.offsetX = 0;
            if (this.offsetX > VIEWPORT_BOUNDS) this.offsetX = VIEWPORT_BOUNDS;

            if (this.offsetY < 0) this.offsetY = 0;
            if (this.offsetY > VIEWPORT_BOUNDS) this.offsetY = VIEWPORT_BOUNDS;
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        float calcYOffest = ((float) offsetY / VIEWPORT_BOUNDS) * (1 - RENDER_RATIO);
        float calcXOffest = ((float) offsetX / VIEWPORT_BOUNDS) * (1 - RENDER_RATIO);
        {
            int maxSize = 0;
            for (SkillPoint point : SkillPointRegistry.getSkillPointMap().values()) {
                if (!point.canRender()) continue;
                maxSize = Math.max(maxSize, fontRenderer.getStringWidth(point.getName() + " : " + SkillData.For(player).getSkillPoint(point)));
            }
            zLevel = 0F;
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ArsMagica.MODID, "textures/occulus/skill_points.png"));
            drawSkillPointBackground(posX, posY, maxSize + 10, 210);
            int pointOffsetX = 5;
            for (SkillPoint point : SkillPointRegistry.getSkillPointMap().values()) {
                if (!point.canRender()) continue;
                fontRenderer.drawString(point.getName() + " : " + SkillData.For(player).getSkillPoint(point), posX + 215, posY + pointOffsetX, point.getColor());
                pointOffsetX += 10;
            }
            GlStateManager.color(1f, 1f, 1f);
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(currentTree.getBackground());
        if (currentTree != SkillTrees.TREE_AFFINITY && currentTree != SkillTrees.TREE_DISCIPLINE) {
            RenderUtils.drawBox(posX + 7, posY + 7, 196, 196, zLevel, calcXOffest, calcYOffest, RENDER_RATIO + calcXOffest, RENDER_RATIO + calcYOffest);
            ArrayList<Skill> skills = SkillTree.getSkillsForTree(currentTree);
            zLevel = 1F;
            ISkillData data = SkillData.For(player);
            for (Skill s : skills) {
                if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
                    continue;
                for (String p : s.getParents()) {
                    if (p == null)
                        continue;
                    Skill parent = Skill.fromName(p);
                    if (parent == null || !skills.contains(parent)) continue;
                    if (!parent.getPoint().canRender() && !data.hasSkill(parent.getID()))
                        continue;
                    int lineOffset = (int) (16 * zoomLevel); // Half of renderSize to center on icon
                    int offsetX = calcXOffset(posX, s) + lineOffset;
                    int offsetY = calcYOffset(posY, s) + lineOffset;
                    int offsetX2 = calcXOffset(posX, parent) + lineOffset;
                    int offsetY2 = calcYOffset(posY, parent) + lineOffset;
                    offsetX = MathHelper.clamp(offsetX, posX + 7, posX + 203);
                    offsetY = MathHelper.clamp(offsetY, posY + 7, posY + 203);
                    offsetX2 = MathHelper.clamp(offsetX2, posX + 7, posX + 203);
                    offsetY2 = MathHelper.clamp(offsetY2, posY + 7, posY + 203);
                    boolean hasPrereq = data.canLearn(s.getID()) || data.hasSkill(s.getID());
                    int color = (!SkillData.For(player).hasSkill(s.getID()) ? s.getPoint().getColor() & 0x999999 : 0x00ff00);
                    if (!hasPrereq) color = 0x000000;
                    if (!(offsetX == posX + 7 || offsetX == posX + 203))
                        RenderUtils.lineThick2d(offsetX, offsetY, offsetX, offsetY2, hasPrereq ? 0 : -1, color);
                    if (!(offsetY2 == posY + 7 || offsetY2 == posY + 203))
                        RenderUtils.lineThick2d(offsetX, offsetY2, offsetX2, offsetY2, hasPrereq ? 0 : -1, color);
                }
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            for (Skill s : skills) {
                if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
                    continue;
                GlStateManager.color(1, 1, 1, 1.0F);
                ISkillData skillData = SkillData.For(player);
                boolean hasPrereq = skillData.canLearn(s.getID()) || data.hasSkill(s.getID());
                int offsetX = calcXOffset(posX, s);
                int offsetY = calcYOffset(posY, s);
                int tick = (player.ticksExisted % 80) >= 40 ? (player.ticksExisted % 40) - 20 : -(player.ticksExisted % 40) + 20;
                float multiplier = 0.75F + tick / 80F;
                TextureAtlasSprite sprite = SpellIconManager.INSTANCE.getSprite(s.getID());
                if (offsetX + renderSize < posX + 7 || offsetX > posX + 203 || offsetY + renderSize < posY + 7 || offsetY > posY + 203 || sprite == null) {
                    continue;
                }
                float spriteXSize = sprite.getMaxU() - sprite.getMinU();
                float spriteYSize = sprite.getMaxV() - sprite.getMinV();
                float xStartMod = 0;
                float yStartMod = 0;
                float xEndMod = 0;
                float yEndMod = 0;
                if (offsetX < posX + 7) {
                    float mod = (posX + 7 - offsetX);
                    xStartMod = mod;
                } else if (offsetX + renderSize > posX + 203) {
                    float mod = renderSize - (posX + 203 - offsetX);
                    xEndMod = mod;
                }
                if (offsetY < posY + 7) {
                    float mod = (posY + 7 - offsetY);
                    yStartMod = mod;
                } else if (offsetY + renderSize > posY + 203) {
                    float mod = renderSize - (posY + 203 - offsetY);
                    yEndMod = mod;
                }
                if (!hasPrereq)
                    GlStateManager.color(0.1F, 0.1F, 0.1F);
                else if (!skillData.hasSkill(s.getID()))
                    GlStateManager.color(Math.max(RenderUtils.getRed(s.getPoint().getColor()), 0.6F) * multiplier, Math.max(RenderUtils.getGreen(s.getPoint().getColor()), 0.6F) * multiplier, Math.max(RenderUtils.getBlue(s.getPoint().getColor()), 0.6F) * multiplier);

                if (ArsMagica.disabledSkills.isSkillDisabled(s.getID()))
                    GlStateManager.color(0.3f, 0.3f, 0.3f);
                RenderUtils.drawBox(offsetX + xStartMod,
                        offsetY + yStartMod,
                        renderSize - xStartMod - xEndMod,
                        renderSize - yStartMod - yEndMod,
                        0,
                        sprite.getMinU() + (xStartMod / renderSize * spriteXSize),
                        sprite.getMinV() + (yStartMod / renderSize * spriteYSize),
                        sprite.getMaxU() - (xEndMod / renderSize * spriteXSize),
                        sprite.getMaxV() - (yEndMod / renderSize * spriteYSize));
                GlStateManager.color(1, 1, 1, 1.0F);
                if (ArsMagica.disabledSkills.isSkillDisabled(s.getID())) {
                    sprite = AMGuiIcons.padlock;
                    spriteXSize = sprite.getMaxU() - sprite.getMinU();
                    spriteYSize = sprite.getMaxV() - sprite.getMinV();
                    xStartMod = 0;
                    yStartMod = 0;
                    xEndMod = 0;
                    yEndMod = 0;
                    float padlockSize = 16 * zoomLevel;
                    float padlockOffset = 8 * zoomLevel;
                    if (offsetX + padlockOffset < posX + 7) {
                        float mod = (posX + 7 - offsetX - padlockOffset);
                        xStartMod = mod;
                    } else if (offsetX + padlockOffset + padlockSize > posX + 203) {
                        float mod = padlockSize - (posX + 203 - offsetX - padlockOffset);
                        xEndMod = mod;
                    }
                    if (offsetY + padlockOffset < posY + 7) {
                        float mod = (posY + 7 - offsetY - padlockOffset);
                        yStartMod = mod;
                    } else if (offsetY + padlockOffset + padlockSize > posY + 203) {
                        float mod = padlockSize - (posY + 203 - offsetY - padlockOffset);
                        yEndMod = mod;
                    }

                    RenderUtils.drawBox(offsetX + xStartMod + padlockOffset,
                            offsetY + yStartMod + padlockOffset,
                            padlockSize - xStartMod - xEndMod,
                            padlockSize - yStartMod - yEndMod,
                            0,
                            sprite.getMinU() + (xStartMod / padlockSize * spriteXSize),
                            sprite.getMinV() + (yStartMod / padlockSize * spriteYSize),
                            sprite.getMaxU() - (xEndMod / padlockSize * spriteXSize),
                            sprite.getMaxV() - (yEndMod / padlockSize * spriteYSize));
                    GlStateManager.color(1, 1, 1, 1.0F);
                }

            }

            //Get the skill

            if (mouseX > posX && mouseX < posX + 210 && mouseY > posY && mouseY < posY + 210) {
                boolean flag = false;
                zLevel = 0F;
                for (Skill s : skills) {
                    if (!s.getPoint().canRender() && !data.hasSkill(s.getID()))
                        continue;
                    int offsetX = calcXOffset(posX, s);
                    int offsetY = calcYOffset(posY, s);
                    if (offsetX > mouseX || offsetX < mouseX - renderSize || offsetY > mouseY || offsetY < mouseY - renderSize)
                        continue;
                    boolean hasPrereq = true;
                    for (String subParent : s.getParents()) {
                        hasPrereq &= data.hasSkill(subParent);
                    }
                    ArrayList<String> list = new ArrayList<String>();
                    list.add(s.getPoint().getChatColor().toString() + s.getName());
                    if (ArsMagica.disabledSkills.isSkillDisabled(s.getID()))
                        list.add(TextFormatting.DARK_RED.toString() + I18n.format("am2.gui.occulus.disabled"));
                    else if (hasPrereq)
                        list.add(TextFormatting.DARK_GRAY.toString() + s.getOcculusDesc());
                    else
                        list.add(TextFormatting.DARK_RED.toString() + I18n.format("am2.gui.occulus.missingrequirements"));

                    drawHoveringText(list, mouseX, mouseY, Minecraft.getMinecraft().fontRenderer);
                    flag = true;
                    hoverItem = s; 
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.color(1.0F, 1.0F, 1.0F);
                }
                if (!flag)
                    hoverItem = null;
            }

        } else if (currentTree == SkillTrees.TREE_DISCIPLINE) {
            // Discipline tab - Wizard Mastery (circular layout, panning background and buttons)
            float disciplineRatio = RENDER_RATIO / zoomLevel;
            float discCalcX = ((float) offsetX / VIEWPORT_BOUNDS) * (1 - disciplineRatio);
            float discCalcY = ((float) offsetY / VIEWPORT_BOUNDS) * (1 - disciplineRatio);
            RenderUtils.drawBox(posX + 7, posY + 7, 196, 196, zLevel, discCalcX, discCalcY, disciplineRatio + discCalcX, disciplineRatio + discCalcY);

            // Title
            String title = I18n.format("discipline.title");
            //fontRenderer.drawStringWithShadow(title, posX + 7 + (196 - fontRenderer.getStringWidth(title)) / 2, posY + 14, 0xFFDD00);

            // Map texture pixel positions to screen positions
            // UV = texPos / 1024, screenPos = viewportStart + ((uv - uvStart) / uvRange) * 196
            final float TEX_SIZE = 1024F;

            SkillData data = (SkillData) SkillData.For(player);
            Discipline[] disciplines = Discipline.values(); 
            for (int i = 0; i < disciplines.length; i++) {
                Discipline d = disciplines[i];
                int level = data.getDisciplineLevel(d);
                SkillPoint required = Discipline.getRequiredSkillPoint(level);
                int buttonSize = GuiButtonDisciplinePlus.BUTTON_SIZE;
                int buttonHalfSize = buttonSize / 2;

                // Texture pixel positions from Discipline enum
                float texX = d.getTexX();
                float texY = d.getTexY();

                float uvX = texX / TEX_SIZE;
                float uvY = texY / TEX_SIZE;
                int btnX = (int) (posX + 7 + ((uvX - discCalcX) / disciplineRatio) * 196) - buttonHalfSize;
                int btnY = (int) (posY + 7 + ((uvY - discCalcY) / disciplineRatio) * 196) - buttonHalfSize;

                // Hide buttons that are outside the viewport
                boolean inBounds = btnX >= posX + 7 && btnX + buttonSize <= posX + 203
                    && btnY >= posY + 7 && btnY + buttonSize <= posY + 203;

                // Update + button position, state and color
                for (GuiButton button : buttonList) {
                    if (button instanceof GuiButtonDisciplinePlus && button.id == DISCIPLINE_BUTTON_ID_START + i) {
                        GuiButtonDisciplinePlus plusBtn = (GuiButtonDisciplinePlus) button;
                        plusBtn.visible = inBounds;
                        plusBtn.x = btnX;
                        plusBtn.y = btnY;
                        boolean canLevel = data.canLevelUpDiscipline(d);
                        plusBtn.enabled = canLevel;
                        plusBtn.setButtonColor(level >= Discipline.MAX_LEVEL ? 0x333333 : required.getColor());
                    }
                }
            }

            // Bottom panel: show selected discipline info + confirm level-up button
            boolean showConfirm = false;
            if (selectedDiscipline != null) {
                Discipline d = selectedDiscipline;
                int level = data.getDisciplineLevel(d);
                SkillPoint required = Discipline.getRequiredSkillPoint(level);
                int panelH = 18;
                int rowY = posY + 203 - panelH;

                // Panel background
                drawRect(posX + 7, rowY, posX + 203, posY + 203, 0xCC111111);

                // Discipline name
                String name = I18n.format(d.getUnlocalizedName());
                fontRenderer.drawStringWithShadow(name, posX + 14, rowY + 4, d.getColor());

                // Level number
                String levelStr = String.valueOf(level);
                fontRenderer.drawString(levelStr, posX + 120 - fontRenderer.getStringWidth(levelStr), rowY + 4, 0xFFFFFF);

                // Progress bar background
                int barX = posX + 125;
                int barWidth = 50;
                int barHeight = 12;
                drawRect(barX, rowY + 1, barX + barWidth, rowY + 1 + barHeight, 0xFF222222);
                // Progress bar fill
                int fillWidth = (int) (barWidth * level / (float) Discipline.MAX_LEVEL);
                if (fillWidth > 0) {
                    drawRect(barX, rowY + 1, barX + fillWidth, rowY + 1 + barHeight, 0xFF000000 | d.getColor());
                }
                // Progress bar border
                drawHorizontalLine(barX, barX + barWidth - 1, rowY + 1, 0xFF666666);
                drawHorizontalLine(barX, barX + barWidth - 1, rowY + barHeight, 0xFF666666);
                drawVerticalLine(barX, rowY + 1, rowY + barHeight, 0xFF666666);
                drawVerticalLine(barX + barWidth - 1, rowY + 1, rowY + barHeight, 0xFF666666);

                // Update confirm button position, state and color
                for (GuiButton btn : buttonList) {
                    if (btn instanceof GuiButtonDisciplinePlus && btn.id == DISCIPLINE_CONFIRM_BUTTON_ID) {
                        GuiButtonDisciplinePlus confirmBtn2 = (GuiButtonDisciplinePlus) btn;
                        confirmBtn2.x = posX + 179;
                        confirmBtn2.y = rowY + 1;
                        confirmBtn2.visible = level < Discipline.MAX_LEVEL;
                        confirmBtn2.enabled = data.canLevelUpDiscipline(d);
                        confirmBtn2.setButtonColor(level >= Discipline.MAX_LEVEL ? 0x333333 : required.getColor());
                        showConfirm = true;
                        break;
                    }
                }
            }
            if (!showConfirm) {
                for (GuiButton btn : buttonList) {
                    if (btn.id == DISCIPLINE_CONFIRM_BUTTON_ID) {
                        btn.visible = false;
                        break;
                    }
                }
            }

            GlStateManager.color(1, 1, 1);
            RenderHelper.disableStandardItemLighting();
        } else {
            boolean isShiftDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
            RenderUtils.drawBox(posX + 7, posY + 7, 196, 196, zLevel, 0, 0, 1, 1);
            int affNum = ArsMagicaAPI.getAffinityRegistry().getValues().size() - 1;
            int portion = 360 / affNum;
            int currentID = 0;
            int cX = posX + xSize / 2;
            int cY = posY + ySize / 2;
            //float finalPercentage = AffinityData.For(player).getAffinityDepth(SkillDefs.NONE) * 100;
            ArrayList<String> drawString = new ArrayList<>();
            for (Affinity aff : ArsMagicaAPI.getAffinityRegistry().getValues()) {
                if (aff == Affinities.none)
                    continue;
                double depth = AffinityData.For(player).getAffinityDepth(aff);
                double affEndX = Math.cos(Math.toRadians(portion * currentID)) * 10F + Math.cos(Math.toRadians(portion * currentID)) * depth * 60F;
                double affEndY = Math.sin(Math.toRadians(portion * currentID)) * 10F + (Math.sin(Math.toRadians(portion * currentID))) * depth * 60F;
                double affStartX1 = Math.cos(Math.toRadians(portion * currentID - portion / 2)) * 10F;
                double affStartY1 = Math.sin(Math.toRadians(portion * currentID - portion / 2)) * 10F;
                double affStartX2 = Math.cos(Math.toRadians(portion * currentID + portion / 2)) * 10F;
                double affStartY2 = Math.sin(Math.toRadians(portion * currentID + portion / 2)) * 10F;
                double affDrawTextX = Math.cos(Math.toRadians(portion * currentID)) * 80F - 7;
                double affDrawTextY = Math.sin(Math.toRadians(portion * currentID)) * 80F - 7;
                currentID++;

                int displace = (int) ((Math.max(affStartX1, affStartX2) - Math.min(affStartX1, affStartX2) + Math.max(affStartY1, affStartY2) - Math.min(affStartY1, affStartY2)) / 2);
                if (depth > 0.01F) {
                    RenderUtils.fractalLine2dd(affStartX1 + cX, affStartY1 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 0.8F);
                    RenderUtils.fractalLine2dd(affStartX2 + cX, affStartY2 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 0.8F);

                    RenderUtils.fractalLine2dd(affStartX1 + cX, affStartY1 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 1.1F);
                    RenderUtils.fractalLine2dd(affStartX2 + cX, affStartY2 + cY, affEndX + cX, affEndY + cY, zLevel, aff.getColor(), displace, 1.1F);
                } else {
                    RenderUtils.line2d((float) affStartX1 + cX, (float) affStartY1 + cY, (float) affEndX + cX, (float) affEndY + cY, zLevel, aff.getColor());
                    RenderUtils.line2d((float) affStartX2 + cX, (float) affStartY2 + cY, (float) affEndX + cX, (float) affEndY + cY, zLevel, aff.getColor());
                }

                int iconX = (int) (affDrawTextX + cX);
                int iconY = (int) (affDrawTextY + cY);
                net.minecraft.item.Item essenceItem = aff.getEssenceItem();
                if (essenceItem != null && essenceItem != net.minecraft.init.Items.AIR) {
                    RenderHelper.enableGUIStandardItemLighting();
                    GlStateManager.color(1, 1, 1, 1);
                    this.itemRender.renderItemAndEffectIntoGUI(new ItemStack(essenceItem), iconX, iconY);
                    RenderHelper.disableStandardItemLighting();
                    GlStateManager.color(1, 1, 1, 1);
                }
                String depthString = "" + (float) Math.round(depth * 10000) / 100F;
                int depthStrWidth = Minecraft.getMinecraft().fontRenderer.getStringWidth(depthString);
                Minecraft.getMinecraft().fontRenderer.drawString(depthString, iconX + 8 - depthStrWidth / 2, iconY + 16, aff.getColor());
                if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                    drawString.add(TextFormatting.RESET.toString() + aff.getLocalizedName());
                    ArrayList<AbstractAffinityAbility> abilites = Lists.newArrayList(ArsMagicaAPI.getAffinityAbilityRegistry().getValues());
                    abilites.sort(new Comparator<AbstractAffinityAbility>() {

                        @Override
                        public int compare(AbstractAffinityAbility o1, AbstractAffinityAbility o2) {
                            return (int) ((o1.getMinimumDepth() * 100) - (o2.getMinimumDepth() * 100));
                        }
                    });


                    for (AbstractAffinityAbility ability : abilites) {
                        if (ability.getAffinity() == aff) {
                            String advancedTooltip = "";
                            if (isShiftDown) {
                                advancedTooltip = " (Min. : " + Math.round(ability.getMinimumDepth() * 100) + "%" + (ability.hasMax() ? (", Max. : " + Math.round(ability.getMaximumDepth() * 100) + "%") : "") + ")";
                            }
                            drawString.add(TextFormatting.RESET.toString()
                                    + (ability.isEligible(player) ? TextFormatting.GREEN.toString()
                                    : TextFormatting.DARK_RED.toString())
                                    + I18n.format("affinityability."
                                    + ability.getRegistryName().toString().replaceAll("arsmagica2:", "")
                                    + ".name") + advancedTooltip);
                        }
                    }
                }
            }
            if (!drawString.isEmpty()) {
                if (!isShiftDown)
                    drawString.add(TextFormatting.GRAY.toString() + I18n.format("am2.tooltip.shiftForDetails"));
                drawHoveringText(drawString, mouseX, mouseY);
            }
            GlStateManager.color(1, 1, 1);
            RenderHelper.disableStandardItemLighting();
        }

        //Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("missingno"));
//
//		int tier0 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_1);
//		int tier1 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_2);
//		int tier2 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_3);
//		int tier3 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_4);
//		int tier4 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_5);
//		int tier5 = SkillData.For(player).getSkillPoint(SkillPoint.SKILL_POINT_6);
//		GlStateManager.disableDepth();
//		fontRenderer.drawString("" + tier0, posX + 191, posY - 19, SkillPointRegistry.getPointForTier(0).getColor());
//		fontRenderer.drawString("" + tier1, posX + 203, posY - 19, SkillPointRegistry.getPointForTier(1).getColor());
//		fontRenderer.drawString("" + tier2, posX + 197, posY - 9, SkillPointRegistry.getPointForTier(2).getColor());
//		if (SkillPointRegistry.getPointForTier(3) != null)
//			fontRenderer.drawString("" + tier3, posX + 191, posY + 210 + 2, SkillPointRegistry.getPointForTier(3).getColor());
//		if (SkillPointRegistry.getPointForTier(4) != null)
//			fontRenderer.drawString("" + tier4, posX + 203, posY + 210 + 2, SkillPointRegistry.getPointForTier(4).getColor());
//		if (SkillPointRegistry.getPointForTier(5) != null)
//			fontRenderer.drawString("" + tier5, posX + 197, posY + 210 + 12, SkillPointRegistry.getPointForTier(5).getColor());

        GlStateManager.color(1, 1, 1);

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw tooltips for hovered skill tree tab buttons
        for (GuiButton button : buttonList) {
            if (button instanceof GuiButtonSkillTree && button.visible) {
                if (mouseX >= button.x && mouseX < button.x + button.width && mouseY >= button.y && mouseY < button.y + button.height) {
                    String treeName = ((GuiButtonSkillTree) button).getTree().getLocalizedName();
                    drawHoveringText(ImmutableList.of(treeName), mouseX, mouseY, fontRenderer);
                    break;
                }
            }
        }

        // Draw discipline tooltips after buttons so they render on top
        if (currentTree == SkillTrees.TREE_DISCIPLINE) {
            SkillData data = (SkillData) SkillData.For(player);
            for (GuiButton button : buttonList) {
                if (button instanceof GuiButtonDisciplinePlus && button.id != DISCIPLINE_CONFIRM_BUTTON_ID && button.isMouseOver()) {
                    Discipline d = ((GuiButtonDisciplinePlus) button).getDiscipline();
                    int level = data.getDisciplineLevel(d);
                    int discoveryPoints = data.getAvailableDiscoveryPoints(d);
                    SkillPoint required = Discipline.getRequiredSkillPoint(level);
                    ArrayList<String> tooltip = new ArrayList<>();
                    tooltip.add(TextFormatting.GOLD.toString() + I18n.format(d.getUnlocalizedName()));
                    tooltip.add(TextFormatting.GRAY + I18n.format("discipline.level") + ": " + TextFormatting.WHITE + level + "/" + Discipline.MAX_LEVEL);
                    tooltip.add(TextFormatting.GRAY + I18n.format("am2.gui.occulus.discovery_points", discoveryPoints > 0 ? TextFormatting.GREEN.toString() + discoveryPoints : TextFormatting.DARK_GRAY.toString() + discoveryPoints));
                    if (net.minecraftforge.fml.common.Loader.isModLoaded("ebwizardry") && level > 0) {
                        float costPct = am2.ArsMagica.config.getEBWizDisciplineCostReductionPerLevel();
                        float potPct = am2.ArsMagica.config.getEBWizDisciplinePotencyBonusPerLevel();
                        if (costPct > 0) tooltip.add(TextFormatting.BLUE + I18n.format("am2.gui.occulus.ebwiz_cost_reduction", String.format("%.1f", level * costPct)));
                        if (potPct > 0) tooltip.add(TextFormatting.RED + I18n.format("am2.gui.occulus.ebwiz_potency_bonus", String.format("%.1f", level * potPct)));
                    }
                    if (level >= Discipline.MAX_LEVEL) {
                        tooltip.add(TextFormatting.YELLOW + I18n.format("discipline.mastered"));
                    }
                    drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
                }
            }
            // Tooltip for the confirm + button
            for (GuiButton button : buttonList) {
                if (button.id == DISCIPLINE_CONFIRM_BUTTON_ID && button.visible && button.isMouseOver()) {
                    drawHoveringText(ImmutableList.of(I18n.format("discipline.levelup")), mouseX, mouseY, fontRenderer);
                    break;
                }
            }
            // Tooltip for the bottom info panel when hovered
            if (selectedDiscipline != null) {
                int panelH = 18;
                int panelY = (this.height - ySize) / 2 + 203 - panelH;
                int panelX = (this.width - xSize) / 2 + 7;
                if (mouseX >= panelX && mouseX <= panelX + 196 && mouseY >= panelY && mouseY <= panelY + panelH) {
                    int discoveryPoints = data.getAvailableDiscoveryPoints(selectedDiscipline);
                    int level = data.getDisciplineLevel(selectedDiscipline);
                    ArrayList<String> tooltip = new ArrayList<>();
                    tooltip.add(TextFormatting.GOLD.toString() + I18n.format(selectedDiscipline.getUnlocalizedName()));
                    tooltip.add(TextFormatting.GRAY + I18n.format("discipline.level") + ": " + TextFormatting.WHITE + level + "/" + Discipline.MAX_LEVEL);
                    tooltip.add(TextFormatting.GRAY + I18n.format("am2.gui.occulus.discovery_points", discoveryPoints > 0 ? TextFormatting.GREEN.toString() + discoveryPoints : TextFormatting.DARK_GRAY.toString() + discoveryPoints));
                    if (net.minecraftforge.fml.common.Loader.isModLoaded("ebwizardry") && level > 0) {
                        float costPct = am2.ArsMagica.config.getEBWizDisciplineCostReductionPerLevel();
                        float potPct = am2.ArsMagica.config.getEBWizDisciplinePotencyBonusPerLevel();
                        if (costPct > 0) tooltip.add(TextFormatting.BLUE + I18n.format("am2.gui.occulus.ebwiz_cost_reduction", String.format("%.1f", level * costPct)));
                        if (potPct > 0) tooltip.add(TextFormatting.RED + I18n.format("am2.gui.occulus.ebwiz_potency_bonus", String.format("%.1f", level * potPct)));
                    }
                    drawHoveringText(tooltip, mouseX, mouseY, fontRenderer);
                }
            }
        }
    }

    private void drawSkillPointBackground(int startX, int startY, int width, int height) {
        int posX = 210;
        int posY = 0;
        //drawTexturedModalRect(startX + posX, startY + posY, 0, 0, 4, 4);
        drawTexturedModalRect(startX + posX + width - 4, startY + posY, 252, 0, 4, 4);
        //drawTexturedModalRect(startX + posX, startY + posY + height - 4, 0, 252, 4, 4);
        drawTexturedModalRect(startX + posX + width - 4, startY + posY + height - 4, 252, 252, 4, 4);
        int w = width - 4;
        int h = height - 8;
        while (w > 0) {
            int x = 0;
            if (w > 252)
                x = 252;
            else
                x = w;
            while (h > 0) {
                int y = 0;
                if (h > 248)
                    y = 248;
                else
                    y = h;
                drawTexturedModalRect(startX + posX + w - x, startY + posY + 4 + h - y, 4, 4, x, y);
                h -= y;
            }
            w -= x;
        }
        w = width - 4;
        h = height - 8;
        while (w > 0) {
            int x = 0;
            if (w > 252)
                x = 252;
            else
                x = w;
            drawTexturedModalRect(startX + posX + w - x, startY + posY, 4, 0, x, 4);
            drawTexturedModalRect(startX + posX + w - x, startY + posY + height - 4, 4, 252, x, 4);
            w -= x;
        }
        while (h > 0) {
            int y = 0;
            if (h > 248)
                y = 248;
            else
                y = h;
            //drawTexturedModalRect(startX + posX, startY + posY + 4 + h - y, 0, 4, 4, y);
            drawTexturedModalRect(startX + posX + width - 4, startY + posY + 4 + h - y, 252, 4, 4, y);
            h -= y;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

}
