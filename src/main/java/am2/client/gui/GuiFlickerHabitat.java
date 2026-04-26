package am2.client.gui;

import am2.ArsMagica;
import am2.api.flickers.AbstractFlickerFunctionality;
import am2.common.blocks.tileentity.TileEntityFlickerHabitat;
import am2.common.container.ContainerFlickerHabitat;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.utils.SpellUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @author Zero, Mithion
 */
public class GuiFlickerHabitat extends GuiContainer {

    private static final ResourceLocation background = new ResourceLocation(ArsMagica.MODID, "textures/gui/flicker_habitat.png");
    private final TileEntityFlickerHabitat flickerHabitat;

    public GuiFlickerHabitat(EntityPlayer player, TileEntityFlickerHabitat tileEntityFlickerHabitat) {
        super(new ContainerFlickerHabitat(player, tileEntityFlickerHabitat));
        flickerHabitat = tileEntityFlickerHabitat;
        xSize = 176;
        ySize = 166;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    /* (non-Javadoc)
     * @see net.minecraft.client.gui.inventory.GuiContainer#drawGuiContainerBackgroundLayer(float, int, int)
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        mc.renderEngine.bindTexture(background);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int l = (width - xSize) / 2;
        int i1 = (height - ySize) / 2;
        drawTexturedModalRect(l, i1, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int p_146979_1_, int p_146979_2_) {
        super.drawGuiContainerForegroundLayer(p_146979_1_, p_146979_2_);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        ItemStack stack = flickerHabitat.getStackInSlot(0);

        if (stack.isEmpty()) {
            String hint = flickerHabitat.isUpgrade()
                    ? I18n.format("am2.gui.flicker_habitat_upgrade_empty")
                    : I18n.format("am2.gui.flicker_habitat_empty");
            drawCenteredString(hint, 5);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableBlend();
            return;
        }

        if (stack.getItem() == AMItems.flicker_jar) {
            drawCenteredString(I18n.format("am2.gui.flicker_jar_hint"), 5);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableBlend();
            return;
        }

        AbstractFlickerFunctionality func = SpellUtils.GetAbstractFlickerFunctionalityFromID(stack.getItemDamage());
        if (func == null) {
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.disableBlend();
            return;
        }

        int yPos = 5;

        // Brief per-focus description
        String descKey = "am2.gui.flickerfunc." + func.getClass().getSimpleName() + ".desc";
        String desc = I18n.format(descKey);
        if (!desc.equals(descKey)) {
            drawCenteredString(desc, yPos);
            int lines = this.fontRenderer.listFormattedStringToWidth(desc, 170).size();
            yPos += lines * 9 + 3;
        }

        String colorCode = Minecraft.getMinecraft().world.getStrongPower(flickerHabitat.getPos()) > 0 ? "\2474" : "\2472";
        String curLine;

        if (func.RequiresPower()) {
            curLine = I18n.format("am2.gui.flicker_needspower");
        } else {
            curLine = I18n.format("am2.gui.flicker_doesntneedpower");
        }

        drawCenteredString(curLine, yPos);
        yPos += 12 * (int) Math.ceil(this.fontRenderer.getStringWidth(curLine) / 170.0f);
        yPos = skipSlot(yPos);

        // Color the etherium amount by the dominant stored power type
        PowerNodeRegistry registry = PowerNodeRegistry.For(flickerHabitat.getWorld());
        PowerTypes dominantType = null;
        float dominantAmount = 0f;
        for (PowerTypes type : PowerTypes.all()) {
            float amt = registry.getPower(flickerHabitat, type);
            if (amt > dominantAmount) {
                dominantAmount = amt;
                dominantType = type;
            }
        }
        String etheriumColor = dominantType != null ? dominantType.getChatColor() : "\2477";

        drawCenteredString(I18n.format("am2.gui.flicker_powerperop", String.format("%s%d\2470", etheriumColor, func.PowerPerOperation())), yPos);
        yPos = skipSlot(yPos + 12);

        boolean powered = registry.checkPower(flickerHabitat, func.PowerPerOperation());

        drawCenteredString(I18n.format("am2.gui.flicker_optime", String.format("%s%.2f\2470", colorCode, func.TimeBetweenOperation(powered, flickerHabitat.getNearbyUpgrades()) / 20.0f)), yPos);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableBlend();
    }

    /** Skips y past the central item slot (y=47–63) if it would overlap. */
    private static int skipSlot(int y) {
        return (y > 43 && y < 66) ? 66 : y;
    }

    private void drawCenteredString(String s, int yCoord) {
        int w = this.fontRenderer.getStringWidth(s);
        int xPos = this.xSize / 2 - w / 2;
        if (w > 170) {
            xPos = 3;
        }
        this.fontRenderer.drawSplitString(s, xPos, yCoord, 170, 0);
    }

}
