package am2.client.gui.controls;

import am2.common.skill.Discipline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

/**
 * A colored button for disciplines in the Occulus.
 * Supports two modes: circular (for the map) and rectangular "+" (for the confirm panel).
 */
public class GuiButtonDisciplinePlus extends GuiButton {

    public static final int BUTTON_SIZE = 24;

    private final Discipline discipline;
    private int buttonColor;
    private boolean circular;

    /**
     * Creates a circular map button (no text).
     */
    public GuiButtonDisciplinePlus(int buttonId, int x, int y, Discipline discipline) {
        super(buttonId, x, y, BUTTON_SIZE, BUTTON_SIZE, "");
        this.discipline = discipline;
        this.buttonColor = 0x0000ff;
        this.circular = true;
    }

    /**
     * Creates a rectangular "+" confirm button.
     */
    public GuiButtonDisciplinePlus(int buttonId, int x, int y, int w, int h, Discipline discipline) {
        super(buttonId, x, y, w, h, "+");
        this.discipline = discipline;
        this.buttonColor = 0x0000ff;
        this.circular = false;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setButtonColor(int color) {
        this.buttonColor = color;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (circular) {
            // Always clickable (even when disabled) for zoom-to-focus; use circular bounds
            float cx = this.x + this.width / 2.0F;
            float cy = this.y + this.height / 2.0F;
            float dx = mouseX - cx;
            float dy = mouseY - cy;
            float radius = this.width / 2.0F;
            return this.visible && dx * dx + dy * dy <= radius * radius;
        }
        return this.visible && this.enabled
                && mouseX >= this.x && mouseY >= this.y
                && mouseX < this.x + this.width && mouseY < this.y + this.height;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) return;

        float r = ((buttonColor >> 16) & 0xFF) / 255.0F;
        float g = ((buttonColor >> 8) & 0xFF) / 255.0F;
        float b = (buttonColor & 0xFF) / 255.0F;

        if (!this.enabled) {
            r *= 0.3F;
            g *= 0.3F;
            b *= 0.3F;
        } else if (this.hovered) {
            r = Math.min(r * 1.3F, 1.0F);
            g = Math.min(g * 1.3F, 1.0F);
            b = Math.min(b * 1.3F, 1.0F);
        }

        if (circular) {
            float cx = this.x + this.width / 2.0F;
            float cy = this.y + this.height / 2.0F;
            float radius = this.width / 2.0F;
            float dx = mouseX - cx;
            float dy = mouseY - cy;
            this.hovered = dx * dx + dy * dy <= radius * radius;

            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            drawCircle(cx, cy, radius, r, g, b, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
        } else {
            this.hovered = mouseX >= this.x && mouseY >= this.y
                    && mouseX < this.x + this.width && mouseY < this.y + this.height;

            GlStateManager.disableTexture2D();
            // Border
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000 | buttonColor);
            // Inner
            int innerColor = ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
            drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0xFF000000 | innerColor);
            GlStateManager.enableTexture2D();

            // Draw "+" centered
            int textColor = this.enabled ? 0xFFFFFF : 0x666666;
            mc.fontRenderer.drawString("+", this.x + (this.width - mc.fontRenderer.getStringWidth("+")) / 2, this.y + (this.height - 8) / 2, textColor);
        }
    }

    private static void drawCircle(float cx, float cy, float radius, float r, float g, float b, float a) {
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buf.pos(cx, cy, 0).color(r, g, b, a).endVertex();
        for (int i = 0; i <= 20; i++) {
            double angle = 2.0 * Math.PI * i / 20;
            buf.pos(cx + Math.cos(angle) * radius, cy + Math.sin(angle) * radius, 0).color(r, g, b, a).endVertex();
        }
        tess.draw();
    }
}
