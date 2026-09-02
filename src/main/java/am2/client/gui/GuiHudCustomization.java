package am2.client.gui;

import am2.ArsMagica;
import am2.api.math.AMVector2;
import am2.client.gui.controls.GuiButtonVariableDims;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;

public class GuiHudCustomization extends GuiScreen {
    private static final int HUD_STYLE_BARS = 0;
    private static final int HUD_STYLE_ORBS = 1;
    private static final int HUD_STYLE_NONE = 2;

    private GuiButtonVariableDims manaButton;
    private GuiButtonVariableDims burnoutButton;
    private GuiButtonVariableDims levelButton;
    private GuiButtonVariableDims affinityButton;
    private GuiButtonVariableDims positiveBuffs;
    private GuiButtonVariableDims negativeBuffs;
    private GuiButtonVariableDims armorHead;
    private GuiButtonVariableDims armorChest;
    private GuiButtonVariableDims armorLegs;
    private GuiButtonVariableDims armorBoots;
    private GuiButtonVariableDims xpBar;
    private GuiButtonVariableDims contingency;
    private GuiButtonVariableDims manaShielding;

    private GuiButtonVariableDims manaNumeric;
    private GuiButtonVariableDims burnoutNumeric;
    private GuiButtonVariableDims XPNumeric;

    private GuiButtonVariableDims spellBook;

    private GuiButtonVariableDims options;
    private GuiButtonVariableDims showBuffs;
    private GuiButtonVariableDims showNumerics;
    private GuiButtonVariableDims showHudMinimally;
    private GuiButtonVariableDims showArmorUI;
    private GuiButtonVariableDims showXPAlways;
    private GuiButtonVariableDims hudStyleButton;
    private GuiButtonVariableDims resetButton;

    private GuiButtonVariableDims manaBarWidthMinus;
    private GuiButtonVariableDims manaBarWidthPlus;
    private GuiButtonVariableDims manaBarHeightMinus;
    private GuiButtonVariableDims manaBarHeightPlus;
    private GuiButtonVariableDims burnoutBarWidthMinus;
    private GuiButtonVariableDims burnoutBarWidthPlus;
    private GuiButtonVariableDims burnoutBarHeightMinus;
    private GuiButtonVariableDims burnoutBarHeightPlus;
    private GuiButtonVariableDims orbSizeMinus;
    private GuiButtonVariableDims orbSizePlus;
    private GuiButtonVariableDims manaOrbButton;
    private GuiButtonVariableDims burnoutOrbButton;
    private GuiButtonVariableDims orbBlackBg;
    private GuiButtonVariableDims orbAffinityColor;

    private boolean doShowBuffs;
    private boolean doShowNumerics;
    private boolean doShowHudMinimally;
    private boolean doShowArmor;
    private boolean doShowXPAlways;
    private boolean doShowBars;
    private boolean doShowOrbs;
    private int hudStyle;
    private boolean doOrbBlackBg;
    private boolean doOrbAffinityColor;
    private boolean showOptions;

    private GuiButtonVariableDims dragTarget;
    private AMVector2 dragOffset;

    private final HashMap<Integer, Integer> snapData;

    int screenWidth = 1;
    int screenHeight = 1;

    public GuiHudCustomization() {
        snapData = new HashMap<Integer, Integer>();
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        screenWidth = scaledresolution.getScaledWidth();
        screenHeight = scaledresolution.getScaledHeight();
    }

    @Override
    public void initGui() {
        super.initGui();

        doShowBuffs = ArsMagica.config.getShowBuffs();
        doShowNumerics = ArsMagica.config.getShowNumerics();
        doShowHudMinimally = ArsMagica.config.showHudMinimally();
        doShowArmor = ArsMagica.config.showArmorUI();
        doShowXPAlways = ArsMagica.config.showXPAlways();
        hudStyle = ArsMagica.config.showHudBars() ? HUD_STYLE_BARS : (ArsMagica.config.showHudOrbs() ? HUD_STYLE_ORBS : HUD_STYLE_NONE);
        doShowBars = hudStyle == HUD_STYLE_BARS;
        doShowOrbs = hudStyle == HUD_STYLE_ORBS;
        doOrbBlackBg = ArsMagica.config.showOrbBlackBackground();
        doOrbAffinityColor = ArsMagica.config.showOrbAffinityColor();

        manaButton = new GuiButtonVariableDims(0, 0, 0, "").setDimensions(ArsMagica.config.getManaBarWidth(), ArsMagica.config.getManaBarHeight()).setBorderOnly(true);
        burnoutButton = new GuiButtonVariableDims(1, 0, 0, "").setDimensions(ArsMagica.config.getBurnoutBarWidth(), ArsMagica.config.getBurnoutBarHeight()).setBorderOnly(true);
        levelButton = new GuiButtonVariableDims(2, 0, 0, "").setDimensions(10, 10).setBorderOnly(true);
        affinityButton = new GuiButtonVariableDims(3, 0, 0, "").setDimensions(10, 20).setBorderOnly(true).setPopupText(I18n.format("am2.gui.affinity"));
        positiveBuffs = new GuiButtonVariableDims(4, 0, 0, "").setPopupText(I18n.format("am2.gui.positiveBuffs")).setDimensions(10, 10).setBorderOnly(true);
        negativeBuffs = new GuiButtonVariableDims(5, 0, 0, "").setPopupText(I18n.format("am2.gui.negativeBuffs")).setDimensions(10, 10).setBorderOnly(true);
        armorHead = new GuiButtonVariableDims(6, 0, 0, "").setDimensions(10, 10).setPopupText(I18n.format("am2.gui.headwear")).setBorderOnly(true);
        armorChest = new GuiButtonVariableDims(7, 0, 0, "").setDimensions(10, 10).setPopupText(I18n.format("am2.gui.chestplate")).setBorderOnly(true);
        armorLegs = new GuiButtonVariableDims(8, 0, 0, "").setDimensions(10, 10).setPopupText(I18n.format("am2.gui.leggings")).setBorderOnly(true);
        armorBoots = new GuiButtonVariableDims(9, 0, 0, "").setDimensions(10, 10).setPopupText(I18n.format("am2.gui.boots")).setBorderOnly(true);
        xpBar = new GuiButtonVariableDims(10, 0, 0, "").setDimensions(182, 5).setPopupText(I18n.format("am2.gui.xpBar")).setBorderOnly(true);
        contingency = new GuiButtonVariableDims(10, 0, 0, "").setDimensions(16, 16).setPopupText(I18n.format("am2.gui.contingency")).setBorderOnly(true);
        showBuffs = new GuiButtonVariableDims(11, width / 2 - 90, height - 88, I18n.format("am2.gui.buffTimers")).setDimensions(180, 20);
        showNumerics = new GuiButtonVariableDims(12, width / 2 - 90, height - 66, I18n.format("am2.gui.numericValues")).setDimensions(180, 20);
        options = new GuiButtonVariableDims(13, width / 2 - 90, height - 22, I18n.format("am2.gui.options")).setDimensions(180, 20);
        showHudMinimally = new GuiButtonVariableDims(14, width / 2 - 90, height - 110, I18n.format("am2.gui.minimalHud")).setDimensions(180, 20).setPopupText(I18n.format("am2.gui.minimalHudDesc"));
        showArmorUI = new GuiButtonVariableDims(15, width / 2 - 90, height - 132, I18n.format("am2.gui.armorUI")).setDimensions(180, 20);
        showXPAlways = new GuiButtonVariableDims(16, width / 2 - 90, height - 154, I18n.format("am2.gui.xpAlways")).setDimensions(180, 20);
        hudStyleButton = new GuiButtonVariableDims(17, width / 2 - 90, height - 176, "").setDimensions(180, 20);
        resetButton = new GuiButtonVariableDims(31, width / 2 - 90, height - 44, I18n.format("am2.gui.resetUI")).setDimensions(180, 20);

        // Bar resize buttons (IDs 23-30)
        manaBarWidthMinus = new GuiButtonVariableDims(23, width / 2 - 90, height - 198, "-").setDimensions(20, 20);
        manaBarWidthPlus = new GuiButtonVariableDims(24, width / 2 - 65, height - 198, "+").setDimensions(20, 20);
        manaBarHeightMinus = new GuiButtonVariableDims(25, width / 2 - 40, height - 198, "-").setDimensions(20, 20);
        manaBarHeightPlus = new GuiButtonVariableDims(26, width / 2 - 15, height - 198, "+").setDimensions(20, 20);
        burnoutBarWidthMinus = new GuiButtonVariableDims(27, width / 2 + 15, height - 198, "-").setDimensions(20, 20);
        burnoutBarWidthPlus = new GuiButtonVariableDims(28, width / 2 + 40, height - 198, "+").setDimensions(20, 20);
        burnoutBarHeightMinus = new GuiButtonVariableDims(29, width / 2 + 65, height - 198, "-").setDimensions(20, 20);
        burnoutBarHeightPlus = new GuiButtonVariableDims(30, width / 2 + 90, height - 198, "+").setDimensions(20, 20);

        // Orb size buttons (IDs 33-34). Shares the bar-resize row's Y (height - 198): bars and orbs
        // controls are mutually exclusive (only one HUD style is active at a time), so reusing the
        // same vertical band keeps this options panel from growing tall enough to clip off-screen
        // at larger GUI Scale settings.
        orbSizeMinus = new GuiButtonVariableDims(33, width / 2 - 25, height - 198, "-").setDimensions(20, 20);
        orbSizePlus = new GuiButtonVariableDims(34, width / 2 + 5, height - 198, "+").setDimensions(20, 20);

        // Black-background toggle (ID 37) and affinity-color toggle (ID 38)
        orbBlackBg = new GuiButtonVariableDims(37, width / 2 - 90, height - 220, I18n.format("am2.gui.orbBlackBg")).setDimensions(88, 20);
        orbAffinityColor = new GuiButtonVariableDims(38, width / 2 + 2, height - 220, I18n.format("am2.gui.orbAffinityColor")).setDimensions(88, 20);

        // Draggable orb position markers (IDs 39-40), positioned/persisted the same way as manaButton/burnoutButton
        manaOrbButton = new GuiButtonVariableDims(39, 0, 0, "").setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize()).setBorderOnly(true);
        burnoutOrbButton = new GuiButtonVariableDims(40, 0, 0, "").setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize()).setBorderOnly(true);

        manaNumeric = new GuiButtonVariableDims(18, 0, 0, "").setDimensions(25, 10).setPopupText(I18n.format("am2.gui.manaNumeric")).setBorderOnly(true);
        burnoutNumeric = new GuiButtonVariableDims(19, 0, 0, "").setDimensions(25, 10).setPopupText(I18n.format("am2.gui.burnoutNumeric")).setBorderOnly(true);
        XPNumeric = new GuiButtonVariableDims(20, 0, 0, "").setDimensions(25, 10).setPopupText(I18n.format("am2.gui.XPNumeric")).setBorderOnly(true);

        spellBook = new GuiButtonVariableDims(21, 0, 0, I18n.format("item.arsmagica2:spellBook.name")).setBorderOnly(true).setDimensions(106, 15);

        manaShielding = new GuiButtonVariableDims(22, 0, 0, "").setDimensions(90, 9).setBorderOnly(true).setPopupText(I18n.format("am2.gui.manaShielding"));

        showBuffs.displayString = I18n.format("am2.gui.buffTimers") + ": " + ((doShowBuffs) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        showNumerics.displayString = I18n.format("am2.gui.numericValues") + ": " + ((doShowNumerics) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        showHudMinimally.displayString = I18n.format("am2.gui.minimalHud") + ": " + ((doShowHudMinimally) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        showArmorUI.displayString = I18n.format("am2.gui.armorUI") + ": " + ((doShowArmor) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        showXPAlways.displayString = I18n.format("am2.gui.xpAlways") + ": " + ((doShowXPAlways) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        hudStyleButton.displayString = I18n.format("am2.gui.hudStyle") + ": " + getHudStyleName();
        orbBlackBg.displayString = I18n.format("am2.gui.orbBlackBg") + ": " + ((doOrbBlackBg) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
        orbAffinityColor.displayString = I18n.format("am2.gui.orbAffinityColor") + ": " + ((doOrbAffinityColor) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));


        positiveBuffs.enabled = doShowBuffs;
        negativeBuffs.enabled = doShowBuffs;
        manaNumeric.enabled = doShowNumerics;
        burnoutNumeric.enabled = doShowNumerics;
        XPNumeric.enabled = doShowNumerics;

        manaButton.enabled = doShowBars;
        burnoutButton.enabled = doShowBars;
        manaOrbButton.enabled = doShowOrbs;
        burnoutOrbButton.enabled = doShowOrbs;

        armorHead.enabled = doShowArmor;
        armorChest.enabled = doShowArmor;
        armorLegs.enabled = doShowArmor;
        armorBoots.enabled = doShowArmor;
        manaShielding.enabled = doShowArmor;

        initButtonAndSnapData(manaButton, ArsMagica.config.getManaHudPosition());
        initButtonAndSnapData(burnoutButton, ArsMagica.config.getBurnoutHudPosition()); //new AMVector2(0.5, 0.5)
        initButtonAndSnapData(manaOrbButton, ArsMagica.config.getManaOrbPosition());
        initButtonAndSnapData(burnoutOrbButton, ArsMagica.config.getBurnoutOrbPosition());
        initButtonAndSnapData(levelButton, ArsMagica.config.getLevelPosition());
        initButtonAndSnapData(affinityButton, ArsMagica.config.getAffinityPosition());
        initButtonAndSnapData(positiveBuffs, ArsMagica.config.getPositiveBuffsPosition());
        initButtonAndSnapData(negativeBuffs, ArsMagica.config.getNegativeBuffsPosition());
        initButtonAndSnapData(armorHead, ArsMagica.config.getArmorPositionHead());
        initButtonAndSnapData(armorChest, ArsMagica.config.getArmorPositionChest());
        initButtonAndSnapData(armorLegs, ArsMagica.config.getArmorPositionLegs());
        initButtonAndSnapData(armorBoots, ArsMagica.config.getArmorPositionBoots());
        initButtonAndSnapData(xpBar, ArsMagica.config.getXPBarPosition());
        initButtonAndSnapData(contingency, ArsMagica.config.getContingencyPosition());
        initButtonAndSnapData(manaNumeric, ArsMagica.config.getManaNumericPosition());
        initButtonAndSnapData(burnoutNumeric, ArsMagica.config.getBurnoutNumericPosition());
        initButtonAndSnapData(XPNumeric, ArsMagica.config.getXPNumericPosition());
        initButtonAndSnapData(spellBook, ArsMagica.config.getSpellBookPosition());
        initButtonAndSnapData(manaShielding, ArsMagica.config.getManaShieldingPosition());

        setOptionsVisibility(false);

        this.buttonList.add(showBuffs);
        this.buttonList.add(showNumerics);
        this.buttonList.add(options);
        this.buttonList.add(showHudMinimally);
        this.buttonList.add(showArmorUI);
        this.buttonList.add(showXPAlways);
        this.buttonList.add(hudStyleButton);
        this.buttonList.add(manaBarWidthMinus);
        this.buttonList.add(manaBarWidthPlus);
        this.buttonList.add(manaBarHeightMinus);
        this.buttonList.add(manaBarHeightPlus);
        this.buttonList.add(burnoutBarWidthMinus);
        this.buttonList.add(burnoutBarWidthPlus);
        this.buttonList.add(burnoutBarHeightMinus);
        this.buttonList.add(burnoutBarHeightPlus);
        this.buttonList.add(orbSizeMinus);
        this.buttonList.add(orbSizePlus);
        this.buttonList.add(orbBlackBg);
        this.buttonList.add(orbAffinityColor);
        this.buttonList.add(resetButton);
    }

    private void setOptionsVisibility(boolean visible) {
        showBuffs.visible = visible;
        showNumerics.visible = visible;
        showHudMinimally.visible = visible;
        showArmorUI.visible = visible;
        showXPAlways.visible = visible;
        hudStyleButton.visible = visible;
        resetButton.visible = visible;

        boolean barsVisible = visible && hudStyle == HUD_STYLE_BARS;
        boolean orbsVisible = visible && hudStyle == HUD_STYLE_ORBS;

        manaBarWidthMinus.visible = barsVisible;
        manaBarWidthPlus.visible = barsVisible;
        manaBarHeightMinus.visible = barsVisible;
        manaBarHeightPlus.visible = barsVisible;
        burnoutBarWidthMinus.visible = barsVisible;
        burnoutBarWidthPlus.visible = barsVisible;
        burnoutBarHeightMinus.visible = barsVisible;
        burnoutBarHeightPlus.visible = barsVisible;

        orbSizeMinus.visible = orbsVisible;
        orbSizePlus.visible = orbsVisible;
        orbBlackBg.visible = orbsVisible;
        orbAffinityColor.visible = orbsVisible;
    }

    private String getHudStyleName() {
        switch (hudStyle) {
            case HUD_STYLE_BARS:
                return I18n.format("am2.gui.hudStyleBars");
            case HUD_STYLE_ORBS:
                return I18n.format("am2.gui.hudStyleOrbs");
            default:
                return I18n.format("am2.gui.hudStyleNone");
        }
    }

    private void initButtonAndSnapData(GuiButtonVariableDims button, AMVector2 position) {
        int xPos = (int) (position.x * screenWidth);
        int yPos = (int) (screenHeight * position.y);
        int snap = 0;

        if (xPos < width / 2)
            snap |= 0x1;

        if (yPos < height / 2)
            snap |= 0x2;

        button.setPosition(xPos, yPos);
        this.buttonList.add(button);
        snapData.put(button.id, snap);
    }

    private int getSnapData(GuiButtonVariableDims button) {
        Integer i = snapData.get(button.id);
        return i != null ? i : 0;
    }

    private AMVector2 getSnapVector(GuiButtonVariableDims button) {
        Integer snap = snapData.get(button.id);
        if (snap == null) snap = 0;
        float xPos = (float) ((button.getPosition().x) / width);
        float yPos = (float) ((button.getPosition().y) / height);

        return new AMVector2(xPos, yPos);
    }

    private void updateButtonPosition(GuiButtonVariableDims button, int newX, int newY) {
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        int snap = getSnapData(button);
        if (newX < centerX) {
            snap |= 0x1;
        } else {
            snap &= ~0x1;
        }

        if (newY < centerY) {
            snap |= 0x2;
        } else {
            snap &= ~0x2;
        }
        snapData.put(button.id, snap);
        button.setPosition(newX, newY);
    }

    @Override
    protected void mouseReleased(int par1, int par2, int par3) {
        super.mouseReleased(par1, par2, par3);
        if (par3 == 1 || par3 == 0)
            dragTarget = null;
    }

    @Override
    protected void mouseClickMove(int par1, int par2, int par3, long par4) {
        super.mouseClickMove(par1, par2, par3, par4);

        if (dragTarget != null) {
            int newX = par1 - dragOffset.iX;
            int newY = par2 - dragOffset.iY;

            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                newX = (int) (Math.round(newX / 5.0f) * 5.0f);
                newY = (int) (Math.round(newY / 5.0f) * 5.0f);
            }

            updateButtonPosition(dragTarget, newX, newY);
            storeGuiPositions();
        } else {
            dragTarget = null;
        }
    }

    private void storeGuiPositions() {
        ArsMagica.config.setGuiPositions(
                getSnapVector(manaButton),
                getSnapVector(burnoutButton),
                getSnapVector(levelButton),
                getSnapVector(affinityButton),
                getSnapVector(positiveBuffs),
                getSnapVector(negativeBuffs),
                getSnapVector(armorHead),
                getSnapVector(armorChest),
                getSnapVector(armorLegs),
                getSnapVector(armorBoots),
                getSnapVector(xpBar),
                getSnapVector(contingency),
                getSnapVector(manaNumeric),
                getSnapVector(burnoutNumeric),
                getSnapVector(XPNumeric),
                getSnapVector(spellBook),
                getSnapVector(manaShielding),
                doShowBuffs,
                doShowNumerics,
                doShowHudMinimally,
                doShowArmor,
                doShowXPAlways,
                doShowBars,
                ArsMagica.config.getManaBarWidth(),
                ArsMagica.config.getManaBarHeight(),
                ArsMagica.config.getBurnoutBarWidth(),
                ArsMagica.config.getBurnoutBarHeight(),
                doShowOrbs,
                ArsMagica.config.getOrbSize(),
                doOrbBlackBg,
                doOrbAffinityColor,
                getSnapVector(manaOrbButton),
                getSnapVector(burnoutOrbButton));
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3) throws IOException {
        super.mouseClicked(par1, par2, par3);

        for (Object button : this.buttonList) {
            if (button instanceof GuiButtonVariableDims) {
                if (((GuiButtonVariableDims) button).mousePressed(mc, par1, par2)) {
                    if (button == showBuffs) {
                        doShowBuffs = !doShowBuffs;
                        showBuffs.displayString = I18n.format("am2.gui.buffTimers") + ": " + ((doShowBuffs) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        positiveBuffs.enabled = doShowBuffs;
                        negativeBuffs.enabled = doShowBuffs;
                        storeGuiPositions();
                    } else if (button == showNumerics) {
                        doShowNumerics = !doShowNumerics;
                        showNumerics.displayString = I18n.format("am2.gui.numericValues") + ": " + ((doShowNumerics) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        manaNumeric.enabled = doShowNumerics;
                        burnoutNumeric.enabled = doShowNumerics;
                        XPNumeric.enabled = doShowNumerics;
                        storeGuiPositions();
                    } else if (button == showHudMinimally) {
                        doShowHudMinimally = !doShowHudMinimally;
                        showHudMinimally.displayString = I18n.format("am2.gui.minimalHud") + ": " + ((doShowHudMinimally) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        storeGuiPositions();
                    } else if (button == options) {
                        showOptions = !showOptions;
                        setOptionsVisibility(showOptions);
                    } else if (button == showXPAlways) {
                        doShowXPAlways = !doShowXPAlways;
                        showXPAlways.displayString = I18n.format("am2.gui.xpAlways") + ": " + ((doShowXPAlways) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        storeGuiPositions();
                    } else if (button == hudStyleButton) {
                        hudStyle = (hudStyle + 1) % 3;
                        doShowBars = hudStyle == HUD_STYLE_BARS;
                        doShowOrbs = hudStyle == HUD_STYLE_ORBS;
                        hudStyleButton.displayString = I18n.format("am2.gui.hudStyle") + ": " + getHudStyleName();
                        manaButton.enabled = doShowBars;
                        burnoutButton.enabled = doShowBars;
                        manaOrbButton.enabled = doShowOrbs;
                        burnoutOrbButton.enabled = doShowOrbs;
                        setOptionsVisibility(showOptions);
                        storeGuiPositions();
                    } else if (button == orbSizeMinus) {
                        ArsMagica.config.setOrbSize(ArsMagica.config.getOrbSize() - 4);
                        manaOrbButton.setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize());
                        burnoutOrbButton.setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == orbSizePlus) {
                        ArsMagica.config.setOrbSize(ArsMagica.config.getOrbSize() + 4);
                        manaOrbButton.setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize());
                        burnoutOrbButton.setDimensions(ArsMagica.config.getOrbSize(), ArsMagica.config.getOrbSize());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == orbBlackBg) {
                        doOrbBlackBg = !doOrbBlackBg;
                        orbBlackBg.displayString = I18n.format("am2.gui.orbBlackBg") + ": " + ((doOrbBlackBg) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == orbAffinityColor) {
                        doOrbAffinityColor = !doOrbAffinityColor;
                        orbAffinityColor.displayString = I18n.format("am2.gui.orbAffinityColor") + ": " + ((doOrbAffinityColor) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == resetButton) {
                        ArsMagica.config.resetGuiPositions();
                        initGui();
                        return;
                    } else if (button == showArmorUI) {
                        doShowArmor = !doShowArmor;
                        showArmorUI.displayString = I18n.format("am2.gui.armorUI") + ": " + ((doShowHudMinimally) ? I18n.format("am2.gui.yes") : I18n.format("am2.gui.no"));
                        armorHead.enabled = doShowArmor;
                        armorChest.enabled = doShowArmor;
                        armorLegs.enabled = doShowArmor;
                        armorBoots.enabled = doShowArmor;
                        manaShielding.enabled = doShowArmor;
                        storeGuiPositions();
                    } else if (button == manaBarWidthMinus) {
                        ArsMagica.config.setManaBarWidth(ArsMagica.config.getManaBarWidth() - 5);
                        manaButton.setDimensions(ArsMagica.config.getManaBarWidth(), ArsMagica.config.getManaBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == manaBarWidthPlus) {
                        ArsMagica.config.setManaBarWidth(ArsMagica.config.getManaBarWidth() + 5);
                        manaButton.setDimensions(ArsMagica.config.getManaBarWidth(), ArsMagica.config.getManaBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == manaBarHeightMinus) {
                        ArsMagica.config.setManaBarHeight(ArsMagica.config.getManaBarHeight() - 1);
                        manaButton.setDimensions(ArsMagica.config.getManaBarWidth(), ArsMagica.config.getManaBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == manaBarHeightPlus) {
                        ArsMagica.config.setManaBarHeight(ArsMagica.config.getManaBarHeight() + 1);
                        manaButton.setDimensions(ArsMagica.config.getManaBarWidth(), ArsMagica.config.getManaBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == burnoutBarWidthMinus) {
                        ArsMagica.config.setBurnoutBarWidth(ArsMagica.config.getBurnoutBarWidth() - 5);
                        burnoutButton.setDimensions(ArsMagica.config.getBurnoutBarWidth(), ArsMagica.config.getBurnoutBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == burnoutBarWidthPlus) {
                        ArsMagica.config.setBurnoutBarWidth(ArsMagica.config.getBurnoutBarWidth() + 5);
                        burnoutButton.setDimensions(ArsMagica.config.getBurnoutBarWidth(), ArsMagica.config.getBurnoutBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == burnoutBarHeightMinus) {
                        ArsMagica.config.setBurnoutBarHeight(ArsMagica.config.getBurnoutBarHeight() - 1);
                        burnoutButton.setDimensions(ArsMagica.config.getBurnoutBarWidth(), ArsMagica.config.getBurnoutBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (button == burnoutBarHeightPlus) {
                        ArsMagica.config.setBurnoutBarHeight(ArsMagica.config.getBurnoutBarHeight() + 1);
                        burnoutButton.setDimensions(ArsMagica.config.getBurnoutBarWidth(), ArsMagica.config.getBurnoutBarHeight());
                        storeGuiPositions();
                        ArsMagica.config.saveGuiPositions();
                    } else if (!showOptions) {
                        dragTarget = (GuiButtonVariableDims) button;
                        AMVector2 buttonPos = ((GuiButtonVariableDims) button).getPosition();
                        AMVector2 mousePos = new AMVector2(par1, par2);
                        dragOffset = mousePos.subtract(buttonPos);
                    }
                }
            }
        }
    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException {
        if (par2 == Keyboard.KEY_ESCAPE) {
            ArsMagica.config.saveGuiPositions();
            if (showOptions) {
                showOptions = false;
                setOptionsVisibility(false);
                return;
            }
        }
        super.keyTyped(par1, par2);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        super.drawScreen(par1, par2, par3);

        // Draw size/offset labels for whichever style is currently active
        if (showOptions && hudStyle == HUD_STYLE_BARS) {
            int centerX = width / 2;
            int y = height - 220;
            String manaLabel = "Mana Bar: " + ArsMagica.config.getManaBarWidth() + "x" + ArsMagica.config.getManaBarHeight();
            String burnoutLabel = "Burnout Bar: " + ArsMagica.config.getBurnoutBarWidth() + "x" + ArsMagica.config.getBurnoutBarHeight();
            this.drawString(fontRenderer, manaLabel, centerX - fontRenderer.getStringWidth(manaLabel) / 2, y, 0xFFFFFF);
            this.drawString(fontRenderer, burnoutLabel, centerX - fontRenderer.getStringWidth(burnoutLabel) / 2, y + 12, 0xFFFFFF);
        } else if (showOptions && hudStyle == HUD_STYLE_ORBS) {
            int centerX = width / 2;
            String orbLabel = "Orb Size: " + ArsMagica.config.getOrbSize();
            this.drawString(fontRenderer, orbLabel, centerX - fontRenderer.getStringWidth(orbLabel) / 2, height - 210, 0xFFFFFF);
        }

        if (dragTarget != null) {
            int snap = getSnapData(dragTarget);
            //x
            if ((snap & 0x1) == 0x1) {
                drawSnapLeft(dragTarget);
            } else {
                drawSnapRight(dragTarget);
            }

            //y
            if ((snap & 0x2) == 0x2) {
                drawSnapTop(dragTarget);
            } else {
                drawSnapBottom(dragTarget);
            }

            // Print X / Y coordinates next to the dragged element
            String posLabel = "X: " + dragTarget.x + "  Y: " + dragTarget.y;
            int labelX = dragTarget.x + dragTarget.getDimensions().iX + 3;
            int labelY = dragTarget.y + dragTarget.getDimensions().iY / 2 - fontRenderer.FONT_HEIGHT / 2;
            // Flip to left side if it would go off screen
            if (labelX + fontRenderer.getStringWidth(posLabel) > width)
                labelX = dragTarget.x - fontRenderer.getStringWidth(posLabel) - 3;
            this.drawString(fontRenderer, posLabel, labelX, labelY, 0xFFFF55);
        }
    }

    private void drawSnapLeft(GuiButtonVariableDims button) {
        GL11.glColor3f(0, 1, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(4f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0, 0, this.zLevel);
        GL11.glVertex3f(0, screenHeight, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(0, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(5, button.y + button.getDimensions().iY / 2 - 5, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(5, button.y + button.getDimensions().iY / 2 + 5, this.zLevel);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawSnapRight(GuiButtonVariableDims button) {
        GL11.glColor3f(0, 1, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(4f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(screenWidth, 0, this.zLevel);
        GL11.glVertex3f(screenWidth, screenHeight, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(screenWidth, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(screenWidth, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(screenWidth - 5, button.y + button.getDimensions().iY / 2 - 5, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(screenWidth, button.y + button.getDimensions().iY / 2, this.zLevel);
        GL11.glVertex3f(screenWidth - 5, button.y + button.getDimensions().iY / 2 + 5, this.zLevel);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawSnapTop(GuiButtonVariableDims button) {
        GL11.glColor3f(0, 1, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(4f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0, 0, this.zLevel);
        GL11.glVertex3f(screenWidth, 0, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, button.y, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, 0, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, 0, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2 + 5, 5, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, 0, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2 - 5, 5, this.zLevel);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawSnapBottom(GuiButtonVariableDims button) {
        GL11.glColor3f(0, 1, 0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(4f);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(0, screenHeight, this.zLevel);
        GL11.glVertex3f(screenWidth, screenHeight, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, button.y + button.getDimensions().iY, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, screenHeight, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, screenHeight, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2 + 5, screenHeight - 5, this.zLevel);
        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2, screenHeight, this.zLevel);
        GL11.glVertex3f(button.x + button.getDimensions().iX / 2 - 5, screenHeight - 5, this.zLevel);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    protected void actionPerformed(GuiButton par1GuiButton) throws IOException {
        super.actionPerformed(par1GuiButton);
    }

    @Override
    public void onGuiClosed() {
        ArsMagica.config.saveGuiPositions();
        super.onGuiClosed();
    }
}
