package am2.client.compat.electroblob;

import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * Renders an {@link ItemEBWizSpellBinding} by binding the bound EBWiz spell's
 * own icon texture (e.g. {@code ebwizardry:textures/spells/fireball.png})
 * and drawing a flat unit quad in item space.
 *
 * <p>Using a TEISR is the only way in 1.12.2 to render a per-NBT arbitrary
 * texture without pre-registering one model JSON per spell variant. The model
 * JSON for this item therefore uses {@code "parent": "builtin/entity"}.
 */
@SideOnly(Side.CLIENT)
public final class EBWizSpellBindingRenderer extends TileEntityItemStackRenderer {

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        Spell spell = ItemEBWizSpellBinding.getSpell(stack);
        if (spell == null) return;

        Minecraft.getMinecraft().getTextureManager().bindTexture(spell.getIcon());

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        // Unit quad (0,0)-(1,1) in item space.
        // RenderItem applies translate(-0.5,-0.5,-0.5) before calling TEISR, so this quad
        // occupies [-0.5,0.5]^2 in model space, filling one 16x16 item slot.
        // disableCull ensures both faces render (e.g. when the item spins on the ground).
        // V is flipped (1→0) because the GUI applies scale(16,-16,16) which inverts Y.
        buf.pos(0.0D, 0.0D, 0.0D).tex(0, 1).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.pos(1.0D, 0.0D, 0.0D).tex(1, 1).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.pos(1.0D, 1.0D, 0.0D).tex(1, 0).normal(0.0F, 0.0F, 1.0F).endVertex();
        buf.pos(0.0D, 1.0D, 0.0D).tex(0, 0).normal(0.0F, 0.0F, 1.0F).endVertex();
        tessellator.draw();
       // GlStateManager.translate(0F,1F,1F);

        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }
}
