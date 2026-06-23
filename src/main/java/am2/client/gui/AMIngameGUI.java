package am2.client.gui;

import am2.ArsMagica;
import am2.api.SpellRegistryHelper;
import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import am2.api.extensions.IEntityExtension;
import am2.api.extensions.ISpellCaster;
import am2.api.items.IBoundItem;
import am2.api.math.AMVector2;
import am2.api.spell.SpellPart;
import am2.client.commands.ConfigureAMUICommand;
import am2.client.texture.SpellIconManager;
import am2.common.armor.ArmorHelper;
import am2.common.blocks.BlockManaBattery;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemSpellBook;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.spell.ContingencyType;
import am2.common.spell.SpellCaster;
import am2.common.utils.AffinityShiftUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Comparator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class AMIngameGUI extends Gui {
    private final Minecraft mc;
    //	private final RenderItem itemRenderer;
    private float zLevel;

    private static final short MANA_BAR_FLASH_SLOT = 4;
//	private final PotionEffectDurationComparator durationComparator = new PotionEffectDurationComparator();

    //	private static final ResourceLocation inv_top = new ResourceLocation(ArsMagica2.MODID, "textures/gui/inventory_top.png");
    private static final ResourceLocation mc_gui = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation spellbook_ui = new ResourceLocation(ArsMagica.MODID, "textures/gui/spellbook_ui.png");
    private static final ResourceLocation mana_bar_background = new ResourceLocation(ArsMagica.MODID, "textures/gui/mana_bar_background.png");
    private static final ResourceLocation mana_bar_full = new ResourceLocation(ArsMagica.MODID, "textures/gui/mana_bar_full.png");
    private static final ResourceLocation burnout_bar_background = new ResourceLocation(ArsMagica.MODID, "textures/gui/burnout_bar_background.png");
    private static final ResourceLocation burnout_bar_full = new ResourceLocation(ArsMagica.MODID, "textures/gui/burnout_bar_full.png");
//	private static final ResourceLocation inventory = new ResourceLocation("textures/gui/container/inventory.png");

    public AMIngameGUI() {
        this.mc = Minecraft.getMinecraft();
//		itemRenderer = mc.getRenderItem();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent()
    public void renderGameOverlay(RenderGameOverlayEvent.Post e) {
        if (e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE)
            return;
        if (this.mc.currentScreen instanceof GuiHudCustomization || this.mc.inGameHasFocus) {
            ItemStack ci = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            ItemStack spellBookStack = ItemSpellBook.findSpellBook(Minecraft.getMinecraft().player);
            boolean hasSpellBook = !spellBookStack.isEmpty();
            boolean drawAMHud = !ArsMagica.config.showHudMinimally() || (ci != null && (ci.getItem() == AMItems.spellbook || ci.getItem() == AMItems.spell || ci.getItem() == AMItems.arcane_spellbook || ci.getItem() == AMItems.staff || ci.getItem() instanceof IBoundItem)) || hasSpellBook;
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaledWidth();
            int j = scaledresolution.getScaledHeight();
            // Fix mod compatibility: Save current GL state more carefully
            GlStateManager.pushAttrib();
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
//		if (drawAMHud)
//			RenderBuffs(i, j);
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            if (drawAMHud)
                this.RenderContingency(i, j);
            if (drawAMHud)
                this.RenderArsMagicaGUIItems(i, j, this.mc.fontRenderer);
            if (drawAMHud)
                this.RenderAffinity(i, j);
            this.RenderArmorStatus(i, j, this.mc, this.mc.fontRenderer);
            if (drawAMHud)
                this.RenderMagicXP(i, j);
            if (hasSpellBook) {
                this.RenderSpellBookUI(i, j, this.mc.fontRenderer, spellBookStack);
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            // Fix mod compatibility: Restore GL state properly
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
            GlStateManager.popAttrib();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA);
            GlStateManager.enableDepth();
            GlStateManager.disableAlpha();
            GlStateManager.resetColor();
            ConfigureAMUICommand.showIfQueued();
        }
    }

    private void RenderArsMagicaGUIItems(int i, int j, FontRenderer fontRenderer) {
        if (EntityExtension.For(this.mc.player).getCurrentLevel() > 0 || this.mc.player.capabilities.isCreativeMode) {
            this.RenderManaBar(i, j, fontRenderer);
        }
    }

    private void RenderSpellBookUI(int i, int j, FontRenderer fontrenderer, ItemStack bookStack) {
        this.mc.renderEngine.bindTexture(spellbook_ui);

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        AMVector2 spellbookVec = this.getShiftedVector(ArsMagica.config.getSpellBookPosition(), i, j);

        int spellUI_x = spellbookVec.iX;
        int spellUI_y = spellbookVec.iY;
        int spellUI_width = 148;
        int spellUI_height = 22;
        float activeSpellSize = 15f;

        int bookActiveSlot = ((ItemSpellBook) bookStack.getItem()).getActiveSlot(bookStack);

        float x = spellUI_x + bookActiveSlot * 12.9f;
        float y = spellUI_y;

        this.zLevel = -5;
        this.drawTexturedModalRect_Classic(spellUI_x, spellUI_y, 0, 0, 106, 15, spellUI_width, spellUI_height);

        ItemStack[] activeScrolls = ((ItemSpellBook) bookStack.getItem()).getActiveScrollInventory(bookStack);

        Minecraft.getMinecraft().getTextureMapBlocks();
        this.mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        this.zLevel = 0;
        for (int n = 0; n < 8; ++n) {
            float IIconX = spellUI_x + 1.5f + n * 12.9f;
            ItemStack stackItem = activeScrolls[n];
            if (stackItem.isEmpty()) {
                continue;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(IIconX, spellUI_y + 1.5, this.zLevel);
            GlStateManager.scale(12f / 16f, 12f / 16f, 12f / 16f);
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stackItem, 0, 0);
            GlStateManager.popMatrix();
        }
        this.mc.renderEngine.bindTexture(spellbook_ui);
        this.zLevel = 1000;
        this.drawTexturedModalRect_Classic(x, y, 148, 0, activeSpellSize, activeSpellSize, 20, 20);
        this.zLevel = 0;

        this.mc.renderEngine.bindTexture(mc_gui);
    }

    private void RenderManaBar(int i, int j, FontRenderer fontRenderer) {

        int barWidth = i / 8;

        AMVector2 Burnout_hud = this.getShiftedVector(ArsMagica.config.getBurnoutHudPosition(), i, j);
        AMVector2 mana_hud = this.getShiftedVector(ArsMagica.config.getManaHudPosition(), i, j);

        // Prevent bars from going completely off-screen edges.
        // mana_hud.iX = Math.max(0, Math.min(mana_hud.iX, i - 2));
        // mana_hud.iY = Math.max(0, Math.min(mana_hud.iY, j - 2));
        // Burnout_hud.iX = Math.max(0, Math.min(Burnout_hud.iX, i - 2));
        // Burnout_hud.iY = Math.max(0, Math.min(Burnout_hud.iY, j - 2));

        float green = 0.5f;
        float blue = 1.0f;
        float red = 0.126f;

        IEntityExtension props = EntityExtension.For(this.mc.player);

        //mana bar
        float mana = props.getCurrentMana();
        float bonusMana = props.getBonusCurrentMana();
        float maxMana = props.getMaxMana();

        float BurnoutBarWidth = barWidth;
        float Burnout = props.getCurrentBurnout();
        float maxBurnout = props.getMaxBurnout();

        float renderMana = mana + bonusMana;

        if (renderMana > maxMana)
            renderMana = maxMana;

        // Safety check: ensure maxMana is valid to prevent broken rendering
        if (maxMana <= 0) {
            maxMana = 100; // Use default value if not synced yet
            renderMana = 0;
        }

        float progressScaled = Math.max(0.0f, Math.min(1.0f, renderMana / maxMana));

        boolean hasBonusMana = bonusMana > 0;
        boolean hasOverloadMana = mana > (maxMana + 1);

        if (ArsMagica.config.showHudBars()) {
            //handle flashing of mana bar
            float flashTimer = AMGuiHelper.instance.getFlashTimer(MANA_BAR_FLASH_SLOT);
            if (flashTimer > 0) {
                green = 0.0f;
                float redShift = 1.0f - red;

                float halfFlash = AMGuiHelper.instance.flashDuration / 2;

                if (flashTimer > halfFlash) {
                    float pct = (flashTimer - halfFlash) / halfFlash;
                    red += redShift - (redShift * pct);
                } else {
                    float pct = flashTimer / halfFlash;
                    red += (redShift * pct);
                }
                GlStateManager.color(red, green, blue);
            } else if (hasBonusMana)
                GlStateManager.color(0.2f, 0.9f, 0.6f);
            else if (hasOverloadMana)
                GlStateManager.color(1f, 0.0f, 0.0f);
            ItemStack curItem = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            // Check main hand first, then baubles charm slot for spell book
            if (curItem.isEmpty() || (curItem.getItem() != AMItems.spell && !(curItem.getItem() instanceof ItemSpellBook))) {
                curItem = ItemSpellBook.findSpellBook(Minecraft.getMinecraft().player);
            }
            if (!curItem.isEmpty() && (curItem.getItem() == AMItems.spell || curItem.getItem() instanceof ItemSpellBook)) {
                ItemStack spellStack = curItem.getItem() == AMItems.spell ? curItem : ((ItemSpellBook) curItem.getItem()).getActiveItemStack(curItem);
                if (!spellStack.isEmpty() && spellStack.hasCapability(SpellCaster.INSTANCE, null)) {
                    ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
                    // Fix client crash on spell cast: Add bounds checking for shape groups
                    List<List<List<SpellPart>>> shapeGroups = caster.getShapeGroups();
                    int currentGroup = caster.getCurrentShapeGroup();
                    if (shapeGroups == null || currentGroup < 0 || currentGroup >= shapeGroups.size()) {
                        return; // Skip rendering if invalid shape group
                    }
                    List<List<SpellPart>> parts = shapeGroups.get(currentGroup);
                    if (parts == null) {
                        return; // Skip rendering if parts are null
                    }
                    // Render shape group icons above the mana bar in a horizontal row
                    int iconSize = 16;
                    int iconSpacing = 1;
                    int manaBarH = ArsMagica.config.getManaBarHeight();
                    int sx = mana_hud.iX;
                    int sy = mana_hud.iY - manaBarH - iconSize - 2;
                    for (List<SpellPart> p : parts) {
                        for (SpellPart part : p) {
                            TextureAtlasSprite icon = SpellIconManager.INSTANCE.getSprite(SpellRegistryHelper.getSkillFromPart(part).getID());
                            if (icon != null) {
                                this.DrawIconAtXY(icon, "items", sx, sy, false);
                                sx += iconSize + iconSpacing;
                            }
                        }
                    }
                }
            }

            // Render mana bar (scalable with 9-slice, configurable size)
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int manaBarWidth = ArsMagica.config.getManaBarWidth();
            int manaBarHeight = ArsMagica.config.getManaBarHeight();

            // Draw mana bar background
            this.mc.renderEngine.bindTexture(mana_bar_background);
            this.drawTexturedModalRect9Slice(mana_hud.iX, mana_hud.iY, manaBarWidth, manaBarHeight);

            // Draw mana bar fill (progressive from left to right)
            if (progressScaled > 0) {
                this.mc.renderEngine.bindTexture(mana_bar_full);
                this.drawTexturedModalRect9SlicePartial(mana_hud.iX, mana_hud.iY, manaBarWidth, manaBarHeight, progressScaled);
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

            // Safety check: ensure maxBurnout is valid to prevent broken rendering
            if (maxBurnout <= 0) {
                maxBurnout = 100; // Use default value if not synced yet
                Burnout = 0;
            }

            progressScaled = Math.max(0.0f, Math.min(1.0f, Burnout / maxBurnout));

            // Render burnout bar (scalable with 9-slice, configurable size)
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            int burnoutBarWidth = ArsMagica.config.getBurnoutBarWidth();
            int burnoutBarHeight = ArsMagica.config.getBurnoutBarHeight();

            // Draw burnout bar background
            this.mc.renderEngine.bindTexture(burnout_bar_background);
            this.drawTexturedModalRect9Slice(Burnout_hud.iX, Burnout_hud.iY, burnoutBarWidth, burnoutBarHeight);

            // Draw burnout bar fill (progressive from left to right)
            if (progressScaled > 0) {
                this.mc.renderEngine.bindTexture(burnout_bar_full);
                this.drawTexturedModalRect9SlicePartial(Burnout_hud.iX, Burnout_hud.iY, burnoutBarWidth, burnoutBarHeight, progressScaled);
            }

            green = 0.5f;
            blue = 1.0f;
            red = 0.126f;
            //magic level
            int manaBarColor = Math.round(red * 255);
            manaBarColor = (manaBarColor << 8) + Math.round(green * 255);
            manaBarColor = (manaBarColor << 8) + Math.round(blue * 255);

            String magicLevel = (new StringBuilder()).append("").append(EntityExtension.For(this.mc.player).getCurrentLevel()).toString();
            AMVector2 magicLevelPos = this.getShiftedVector(ArsMagica.config.getLevelPosition(), i, j);
            magicLevelPos.iX -= Minecraft.getMinecraft().fontRenderer.getStringWidth(magicLevel) / 2;
            fontRenderer.drawStringWithShadow(magicLevel, magicLevelPos.iX, magicLevelPos.iY, manaBarColor);

            if (flashTimer > 0) {
                GlStateManager.color(1.0f, 1.0f, 1.0f);
            }
        }

        if (ArsMagica.config.getShowNumerics()) {
            GlStateManager.enableBlend();
            String spellcost = "";
            ItemStack curItem = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if (!curItem.isEmpty() && (curItem.getItem() == AMItems.spell
                    || curItem.getItem() == AMItems.spellbook || curItem.getItem() == AMItems.arcane_spellbook)) {
                ItemStack spellStack = curItem.getItem() == AMItems.spell ? curItem : ((ItemSpellBook) curItem.getItem()).getActiveItemStack(curItem);
                if (!spellStack.isEmpty()) {
                    ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
                    if (caster != null) {
                        float manaCost = caster.getManaCost(Minecraft.getMinecraft().world, Minecraft.getMinecraft().player);
                        if (manaCost == 0 && spellStack.hasTagCompound()
                                && spellStack.getTagCompound().hasKey(am2.common.items.ItemSpellBase.KEY_MANA_COST_CACHED)) {
                            // Capability not yet synced to client; use the cached value baked into the tagCompound.
                            manaCost = spellStack.getTagCompound().getFloat(am2.common.items.ItemSpellBase.KEY_MANA_COST_CACHED);
                            IEntityExtension ext = EntityExtension.For(Minecraft.getMinecraft().player);
                            if (ext != null && ext.getMaxBurnout() > 0) manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
                        }
                        spellcost = (EntityExtension.For(Minecraft.getMinecraft().player).hasEnoughMana(manaCost) ? ChatFormatting.AQUA.toString() : ChatFormatting.DARK_RED.toString()) + " (" + (int) (manaCost) + ")";
                        spellcost += ChatFormatting.RESET.toString();
                    } else if (AMItems.ebwiz_spell_binding != null && spellStack.getItem() == AMItems.ebwiz_spell_binding) {
                        float manaCost = EBWizardryCompatBootstrap.getEBWizSpellBindingManaCost(spellStack, Minecraft.getMinecraft().player);
                        if (manaCost >= 0) {
                            IEntityExtension ext = EntityExtension.For(Minecraft.getMinecraft().player);
                            if (ext != null && ext.getMaxBurnout() > 0) manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
                            spellcost = (EntityExtension.For(Minecraft.getMinecraft().player).hasEnoughMana(manaCost) ? ChatFormatting.AQUA.toString() : ChatFormatting.DARK_RED.toString()) + " (" + (int) (manaCost) + ")";
                            spellcost += ChatFormatting.RESET.toString();
                        }
                    }
                }
            } else if (EBWizardryCompatBootstrap.isEBWizWand(curItem)) {
                float manaCost = EBWizardryCompatBootstrap.getEBWizWandCurrentSpellManaCost(curItem, Minecraft.getMinecraft().player);
                if (manaCost >= 0) {
                    IEntityExtension ext = EntityExtension.For(Minecraft.getMinecraft().player);
                    if (ext != null && ext.getMaxBurnout() > 0) manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
                    spellcost = (EntityExtension.For(Minecraft.getMinecraft().player).hasEnoughMana(manaCost) ? ChatFormatting.AQUA.toString() : ChatFormatting.DARK_RED.toString()) + " (" + (int) (manaCost) + ")";
                    spellcost += ChatFormatting.RESET.toString();
                }
            } else if (AMItems.ebwiz_spell_binding != null && curItem.getItem() == AMItems.ebwiz_spell_binding) {
                float manaCost = EBWizardryCompatBootstrap.getEBWizSpellBindingManaCost(curItem, Minecraft.getMinecraft().player);
                if (manaCost >= 0) {
                    IEntityExtension ext = EntityExtension.For(Minecraft.getMinecraft().player);
                    if (ext != null && ext.getMaxBurnout() > 0) manaCost *= (1 + (ext.getCurrentBurnout() / ext.getMaxBurnout()));
                    spellcost = (EntityExtension.For(Minecraft.getMinecraft().player).hasEnoughMana(manaCost) ? ChatFormatting.AQUA.toString() : ChatFormatting.DARK_RED.toString()) + " (" + (int) (manaCost) + ")";
                    spellcost += ChatFormatting.RESET.toString();
                }
            }

            String manaStr = I18n.format("am2.gui.mana") + ": " + Math.round(mana + bonusMana) + "/" + Math.round(maxMana) + spellcost;
            String burnoutStr = I18n.format("am2.gui.burnout") + ": " + Math.round(props.getCurrentBurnout()) + "/" + Math.round(props.getMaxBurnout());
            AMVector2 manaNumericPos = this.getShiftedVector(ArsMagica.config.getManaNumericPosition(), i, j);
            AMVector2 burnoutNumericPos = this.getShiftedVector(ArsMagica.config.getBurnoutNumericPosition(), i, j);
            fontRenderer.drawString(manaStr, manaNumericPos.iX, manaNumericPos.iY, hasBonusMana ? 0xeae31c : hasOverloadMana ? 0xFF2020 : 0x2080FF);
            fontRenderer.drawString(burnoutStr, burnoutNumericPos.iX + 25 - fontRenderer.getStringWidth(burnoutStr), burnoutNumericPos.iY, 0xFF2020);
        }
        //Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

//	private ItemStack getSpellFromStack(ItemStack stack){
//		if (stack.getItem() == ItemDefs.spell)
//			return stack;
//		else if (stack.getItem() == ItemDefs.spellBook || stack.getItem() == ItemDefs.arcaneSpellbook)
//			return ((ItemSpellBook)stack.getItem()).GetActiveItemStack(stack);
//		else
//			return null;
//	}

    private void RenderArmorStatus(int i, int j, Minecraft mc, FontRenderer fontRenderer) {
        if (!ArsMagica.config.showArmorUI())
            return;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());

        for (int slot = 0; slot < 4; ++slot) {
            if (ArmorHelper.PlayerHasArmorInSlot(mc.player, EntityEquipmentSlot.values()[5 - slot])) {
                AMVector2 position = this.getArmorSlotPosition(slot, scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
                int blackoutTimer = AMGuiHelper.instance.getBlackoutTimer(3 - slot);
                int blackoutMaxTimer = AMGuiHelper.instance.getBlackoutTimerMax(3 - slot);
                GlStateManager.color(1.0f, 1.0f, 1.0f);
                ItemStack armor = mc.player.inventory.armorInventory.get(3 - slot);
                float lineweight = 4f;
                int barYOffset = 13; // spacing below the armor icon
                //durability
                if (armor.isItemDamaged() && armor.getMaxDamage() > 0) {
                    float pct = 1 - (float) armor.getItemDamage() / (float) armor.getMaxDamage();
                    AMGuiHelper.line2d(position.iX, position.iY + barYOffset, position.iX + 10, position.iY + barYOffset, this.zLevel + 102, lineweight, 0);

                    // Color gradient: green (full) -> yellow (half) -> red (near breaking)
                    int red, green;
                    if (pct > 0.5f) {
                        // Green to yellow: pct 1.0->0.5, red 0->255, green stays 255
                        red = (int) (255.0f * 2.0f * (1.0f - pct));
                        green = 255;
                    } else {
                        // Yellow to red: pct 0.5->0, red stays 255, green 255->0
                        red = 255;
                        green = (int) (255.0f * 2.0f * pct);
                    }
                    int color = (red << 16) | (green << 8);

                    AMGuiHelper.line2d(position.iX, position.iY + barYOffset, position.iX + (10 * pct), position.iY + barYOffset, this.zLevel + 103, lineweight, color);
                }
                //cooldown
                if (blackoutMaxTimer > 0) {
                    float pct = (float) (blackoutMaxTimer - blackoutTimer) / (float) blackoutMaxTimer;
                    AMGuiHelper.line2d(position.iX, position.iY + barYOffset + 4, position.iX + 10, position.iY + barYOffset + 4, this.zLevel + 100, lineweight, 0);
                    AMGuiHelper.line2d(position.iX, position.iY + barYOffset + 4, position.iX + (10 * pct), position.iY + barYOffset + 4, this.zLevel + 101, lineweight, 0xFF0000);
                }

                //flash
                float green = 0.5f;
                float blue = 1.0f;
                float red = 0.126f;
                float flashTimer = AMGuiHelper.instance.getFlashTimer(3 - slot);
                if (flashTimer > 0) {
                    green = 0.0f;
                    float redShift = 1.0f - red;

                    float halfFlash = AMGuiHelper.instance.flashDuration / 2;

                    if (flashTimer > halfFlash) {
                        float pct = (flashTimer - halfFlash) / halfFlash;
                        red += redShift - (redShift * pct);
                    } else {
                        float pct = flashTimer / halfFlash;
                        red += (redShift * pct);
                    }
                    GlStateManager.color(red, green, blue);
                }

//				if (icon != null && icon != Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()){
//					AMGuiHelper.DrawIconAtXY(icon, position.iX, position.iY, this.zLevel, 10, 10, true);
//				}else{
                GlStateManager.pushMatrix();
                AMGuiHelper.DrawItemAtXY(mc.player.inventory.armorInventory.get(3 - slot), position.iX, position.iY, this.zLevel, 0.63f);
                GlStateManager.popMatrix();
//				}
            }
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f);

        mc.renderEngine.bindTexture(new ResourceLocation(ArsMagica.MODID, "textures/gui/overlay.png"));
        AMVector2 shieldPos = this.getShiftedVector(ArsMagica.config.getManaShieldingPosition(), scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        int shielding = (int) EntityExtension.For(mc.player).getManaShielding();
        if (shielding <= 20) {
            for (int iter = 0; iter < shielding; iter += 2) {
                this.drawTexturedModalRect(shieldPos.iX + (iter * 8 / 2), shieldPos.iY, 0, 0, 9, 9);
            }
            for (int iter = 0; iter < shielding; iter += 2) {
                boolean half = iter + 2 > shielding && (shielding & 0x1) == 0x1;
                this.drawTexturedModalRect(shieldPos.iX + (iter * 8 / 2), shieldPos.iY, half ? 18 : 9, 0, 9, 9);
            }
        } else {
            this.drawTexturedModalRect(shieldPos.iX, shieldPos.iY, 0, 0, 9, 9);
            this.drawTexturedModalRect(shieldPos.iX, shieldPos.iY, 18, 0, 9, 9);

            mc.fontRenderer.drawString("x" + shielding, shieldPos.iX + 10, shieldPos.iY, 0x007aff);
        }
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    }

    private AMVector2 getArmorSlotPosition(int slot, int screenWidth, int screenHeight) {
        switch (slot) {
            case 0:
                return this.getShiftedVector(ArsMagica.config.getArmorPositionHead(), screenWidth, screenHeight);
            case 1:
                return this.getShiftedVector(ArsMagica.config.getArmorPositionChest(), screenWidth, screenHeight);
            case 2:
                return this.getShiftedVector(ArsMagica.config.getArmorPositionLegs(), screenWidth, screenHeight);
            case 3:
                return this.getShiftedVector(ArsMagica.config.getArmorPositionBoots(), screenWidth, screenHeight);
        }
        return new AMVector2(0, 0);
    }

    public void RenderAffinity(int i, int j) {
        AMVector2 affinityPos = this.getShiftedVector(ArsMagica.config.getAffinityPosition(), i, j);

        int x = affinityPos.iX;
        int y = affinityPos.iY;

        IAffinityData ad = AffinityData.For(Minecraft.getMinecraft().player);
        for (Affinity affinity : ad.getHighestAffinities()) {
            if (affinity == null || affinity == Affinities.none) continue;
            GlStateManager.color(1.0f, 1.0f, 1.0f);
            ItemStack essenceStack = AffinityShiftUtils.getEssenceForAffinity(affinity);
            AMGuiHelper.DrawIconAtXY(this.mc.getRenderItem().getItemModelMesher().getParticleIcon(essenceStack.getItem()), x, y, j, 12, 12, true);

            if (ArsMagica.config.getShowNumerics()) {
                GlStateManager.enableBlend();
                String display = String.format("%.2f%%", AffinityData.For(this.mc.player).getAffinityDepth(affinity) * 100f);
                if (x < i / 2)
                    Minecraft.getMinecraft().fontRenderer.drawString(display, x + 14, y + 2, affinity.getColor());
                else
                    Minecraft.getMinecraft().fontRenderer.drawString(display, x - 2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(display), y + 2, affinity.getColor());
            }
            y += 15;
        }
    }

    public void RenderContingency(int i, int j) {

        AMVector2 contingencyPos = this.getShiftedVector(ArsMagica.config.getContingencyPosition(), i, j);
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite icon = null;
        ContingencyType type = EntityExtension.For(Minecraft.getMinecraft().player).getContingencyType();
        switch (type) {
            case DAMAGE:
                icon = SpellIconManager.INSTANCE.getSprite("arsmagica2:contingency_damage");
                break;
            case FALL:
                icon = SpellIconManager.INSTANCE.getSprite("arsmagica2:contingency_fall");
                break;
            case HEALTH:
                icon = SpellIconManager.INSTANCE.getSprite("arsmagica2:contingency_health");
                break;
            case FIRE:
                icon = SpellIconManager.INSTANCE.getSprite("arsmagica2:contingency_fire");
                break;
            case DEATH:
                icon = SpellIconManager.INSTANCE.getSprite("arsmagica2:contingency_death");
                break;
            case NULL:
            default:
                return;
        }
        //LogHelper.info(icon);
        this.DrawIconAtXY(icon, "items", contingencyPos.iX, contingencyPos.iY, 16, 16, true);
        //GL11.glColor3f(1.0f, 1.0f, 1.0f);
    }

//	public void RenderBuffs(int i, int j){
//
//		if (!ArsMagica2.config.getShowBuffs()){
//			return;
//		}
//
//		int barWidth = i / 8;
//
//		AMVector2 posBuffStart = getShiftedVector(ArsMagica2.config.getPositiveBuffsPosition(), i, j);
//		AMVector2 negBuffStart = getShiftedVector(ArsMagica2.config.getNegativeBuffsPosition(), i, j);
//
//		int positive_buff_x = posBuffStart.iX;
//		int positive_buff_y = posBuffStart.iY;
//
//		int negative_buff_x = negBuffStart.iX;
//		int negative_buff_y = negBuffStart.iY;
//		for (PotionEffect pe : getPotionEffectsByTimeRemaining()){
//			this.mc.renderEngine.bindTexture(inventory);
//
//			int potionID = pe.getPotionID();
//			if (potionID < 0 || potionID >= Potion.potionTypes.length)
//				continue;
//
//			Potion potion = Potion.potionTypes[potionID];
//
//			if (potion == null)
//				continue;
//
//			if (potion.isBadEffect()){
//				if (potion.hasStatusIcon()){
//					int l = potion.getStatusIconIndex();
//					if (pe.getDuration() < 100){
//						GL11.glColor4f(1.0f, 1.0f, 1.0f, AMGuiHelper.instance.fastFlashAlpha);
//					}else if (pe.getDuration() < 200){
//						GL11.glColor4f(1.0f, 1.0f, 1.0f, AMGuiHelper.instance.slowFlashAlpha);
//					}
//					this.drawTexturedModalRect_Classic(negative_buff_x, negative_buff_y, 0 + l % 8 * 18, 198 + l / 8 * 18, 10, 10, 18, 18);
//					GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//					negative_buff_x -= 12;
//					if (negative_buff_x <= negBuffStart.iX - 48){
//						negative_buff_x = negBuffStart.iX;
//						negative_buff_y += 12;
//					}
//				}
//			}else{
//				this.mc.renderEngine.bindTexture(inventory);
//				if (potion.hasStatusIcon()){
//					int l = potion.getStatusIconIndex();
//					if (pe.getDuration() < 100){
//						GL11.glColor4f(1.0f, 1.0f, 1.0f, AMGuiHelper.instance.fastFlashAlpha);
//					}else if (pe.getDuration() < 200){
//						GL11.glColor4f(1.0f, 1.0f, 1.0f, AMGuiHelper.instance.slowFlashAlpha);
//					}
//					this.drawTexturedModalRect_Classic(positive_buff_x, positive_buff_y, 0 + l % 8 * 18, 198 + l / 8 * 18, 10, 10, 18, 18);
//					GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
//					positive_buff_x += 12;
//					if (positive_buff_x >= posBuffStart.iX + 48){
//						positive_buff_x = posBuffStart.iX;
//						positive_buff_y += 12;
//					}
//				}
//			}
//		}
//	}

    public void RenderMagicXP(int i, int j) {
        IEntityExtension props = EntityExtension.For(Minecraft.getMinecraft().player);
        if (props.getCurrentLevel() > 0) {
            GlStateManager.enableBlend();
            AMVector2 position = this.getShiftedVector(ArsMagica.config.getXPBarPosition(), i, j);
            AMVector2 dimensions = new AMVector2(182, 5);
            Minecraft.getMinecraft().renderEngine.bindTexture(mc_gui);
            GlStateManager.color(0.5f, 0.5f, 1.0f, ArsMagica.config.showXPAlways() ? 1.0f : AMGuiHelper.instance.getMagicXPBarAlpha());

            //base XP bar
            this.drawTexturedModalRect_Classic(position.iX, position.iY, 0, 64, dimensions.iX, dimensions.iY, dimensions.iX, dimensions.iY);

            if (props.getCurrentXP() > 0) {
                float pctXP = props.getCurrentXP() / props.getMaxXP();
                if (pctXP > 1)
                    pctXP = 1;
                int width = (int) ((dimensions.iX + 1) * pctXP);
                this.drawTexturedModalRect_Classic(position.iX, position.iY, 0, 69, width, dimensions.iY, width, dimensions.iY);
            }

            if (ArsMagica.config.getShowNumerics() && (ArsMagica.config.showXPAlways() || AMGuiHelper.instance.getMagicXPBarAlpha() > 0)) {
                String xpStr = I18n.format("am2.gui.xp") + ": " + +(int) (props.getCurrentXP() * 100) + "/" + (int) (props.getMaxXP() * 100);
                AMVector2 numericPos = this.getShiftedVector(ArsMagica.config.getXPNumericPosition(), i, j);
                Minecraft.getMinecraft().fontRenderer.drawString(xpStr, numericPos.iX, numericPos.iY, 0x999999);
            }
        }
    }

//	private ArrayList<PotionEffect> getPotionEffectsByTimeRemaining(){
//		Iterator i = mc.player.getActivePotionEffects().iterator();
//		ArrayList<PotionEffect> potions = new ArrayList<PotionEffect>();
//
//		while (i.hasNext())
//			potions.add((PotionEffect)i.next());
//
//		Collections.sort(potions, durationComparator);
//		return potions;
//	}

    public void drawTexturedModalRect_Classic(int par1, int par2, int par3, int par4, int par5, int par6) {
        float var7 = 0.00390625F;
        float var8 = 0.00390625F;

        Tessellator var9 = Tessellator.getInstance();
        var9.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
        var9.getBuffer().pos(par1 + 0, par2 + par6, this.zLevel).tex((par3 + 0) * var7, (par4 + par6) * var8).endVertex();
        var9.getBuffer().pos(par1 + par5, par2 + par6, this.zLevel).tex((par3 + par5) * var7, (par4 + par6) * var8).endVertex();
        var9.getBuffer().pos(par1 + par5, par2 + 0, this.zLevel).tex((par3 + par5) * var7, (par4 + 0) * var8).endVertex();
        var9.getBuffer().pos(par1 + 0, par2 + 0, this.zLevel).tex((par3 + 0) * var7, (par4 + 0) * var8).endVertex();
        var9.draw();
    }

    /**
     * Draw a section of the currently bound texture to the screen.
     *
     * @param dst_x      The x coordinate on the screen to draw to
     * @param dst_y      The y coordinate on the screen to draw to
     * @param src_x      The x coordinate on the texture to pull from
     * @param src_y      The y coordinate on the texture to pull from
     * @param dst_width  The width on screen to draw
     * @param dst_height The height on screen to draw
     * @param src_width  The width of the texture section
     * @param src_height The height of the texture section
     */
    public void drawTexturedModalRect_Classic(float dst_x, float dst_y, float src_x, float src_y, float dst_width, float dst_height, float src_width, float src_height) {
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

    @Override
    public void drawTexturedModalRect(int par1, int par2, int par3, int par4, int par5, int par6) {
        float f = 1f / 256f;
        float var9 = par3 * f;
        float var10 = (par3 + par5) * f;
        float var11 = par4 * f;
        float var12 = (par4 + par5) * f;

        Tessellator var8 = Tessellator.getInstance();
        //GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0F);
        var8.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        var8.getBuffer().pos(par1 + 0, par2 + par6, this.zLevel).tex(var9, var12).normal(0.0F, 1.0F, 0.0F).endVertex();
        var8.getBuffer().pos(par1 + par5, par2 + par6, this.zLevel).tex(var10, var12).normal(0.0F, 1.0F, 0.0F).endVertex();
        var8.getBuffer().pos(par1 + par5, par2 + 0, this.zLevel).tex(var10, var11).normal(0.0F, 1.0F, 0.0F).endVertex();
        var8.getBuffer().pos(par1 + 0, par2 + 0, this.zLevel).tex(var9, var11).normal(0.0F, 1.0F, 0.0F).endVertex();
        var8.draw();
    }

//	private void renderPortalOverlay(float par1, int par2, int par3){
//
//	}

    private void DrawIconAtXY(TextureAtlasSprite icon, String base, float x, float y, boolean semitransparent) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        this.DrawIconAtXY(icon, base, x, y, 16, 16, semitransparent);
    }

    private void DrawIconAtXY(TextureAtlasSprite IIcon, String base, float x, float y, int w, int h, boolean semitransparent) {
        if (IIcon == null) return;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);
        tessellator.getBuffer().pos(x, y + h, this.zLevel).tex(IIcon.getMinU(), IIcon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + w, y + h, this.zLevel).tex(IIcon.getMaxU(), IIcon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + w, y, this.zLevel).tex(IIcon.getMaxU(), IIcon.getMinV()).endVertex();
        tessellator.getBuffer().pos(x, y, this.zLevel).tex(IIcon.getMinU(), IIcon.getMinV()).endVertex();
        tessellator.draw();
    }

    private void DrawPartialIconAtXY(TextureAtlasSprite IIcon, float pct_x, float pct_y, float x, float y, float w, float h, boolean semitransparent) {
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (IIcon == null) return;

        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX);

        tessellator.getBuffer().pos(x, y + (h * pct_y), this.zLevel).tex(IIcon.getMinU(), IIcon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + (w * pct_x), y + (h * pct_y), this.zLevel).tex(IIcon.getMaxU(), IIcon.getMaxV()).endVertex();
        tessellator.getBuffer().pos(x + (w * pct_x), y, this.zLevel).tex(IIcon.getMaxU(), IIcon.getMinV()).endVertex();
        tessellator.getBuffer().pos(x, y, this.zLevel).tex(IIcon.getMinU(), IIcon.getMinV()).endVertex();

        tessellator.draw();
    }

    private void drawTexturedModalRectSimple(int x, int y, int width, int height) {
        // Draw a full texture using normalized UV coordinates (0-1 range)
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(0, 1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(1, 1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(1, 0).endVertex();
        buffer.pos(x, y, this.zLevel).tex(0, 0).endVertex();
        tessellator.draw();
    }

    private void drawTexturedModalRectPartial(int x, int y, int width, int height, float progressPct) {
        // Draw a partial texture (left to right) using normalized UV coordinates
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(0, 1).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(progressPct, 1).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(progressPct, 0).endVertex();
        buffer.pos(x, y, this.zLevel).tex(0, 0).endVertex();
        tessellator.draw();
    }

    /**
     * Draw a 9-slice texture with pixel-perfect tiling (no stretching/compression).
     * Original texture is 100x10 with 1px border on all sides.
     * When the target size differs from 100x10, the center is tiled/cropped at 1:1 pixel ratio.
     *
     * @param x            Screen X position
     * @param y            Screen Y position
     * @param targetWidth  Desired width (minimum 2 for borders)
     * @param targetHeight Desired height (minimum 2 for borders)
     */
    private void drawTexturedModalRect9Slice(int x, int y, int targetWidth, int targetHeight) {
        drawTexturedModalRect9SliceTiled(x, y, targetWidth, targetHeight, targetWidth);
    }

    /**
     * Draw a 9-slice texture with pixel-perfect tiling, clipped to show only a percentage.
     * Used for the fill bars that need to clip horizontally based on progress.
     *
     * @param x            Screen X position
     * @param y            Screen Y position
     * @param targetWidth  Full desired width
     * @param targetHeight Desired height
     * @param progressPct  Percentage to show (0.0 to 1.0)
     */
    private void drawTexturedModalRect9SlicePartial(int x, int y, int targetWidth, int targetHeight, float progressPct) {
        if (progressPct <= 0) return;
        int clipWidth = Math.min((int) Math.ceil(targetWidth * progressPct), targetWidth);
        if (clipWidth < 1) return;
        drawTexturedModalRect9SliceTiled(x, y, targetWidth, targetHeight, clipWidth);
    }

    /**
     * Core 9-slice renderer with pixel-perfect tiling and optional horizontal clipping.
     * Each texel maps to exactly one screen pixel. Centers are tiled when larger
     * than the source texture, or cropped when smaller. No stretching occurs.
     *
     * @param x            Screen X position
     * @param y            Screen Y position
     * @param targetWidth  Full bar width
     * @param targetHeight Full bar height
     * @param clipWidth    How many pixels wide to actually draw (for partial fill)
     */
    private void drawTexturedModalRect9SliceTiled(int x, int y, int targetWidth, int targetHeight, int clipWidth) {
        if (targetWidth < 2 || targetHeight < 2 || clipWidth < 1) return;

        final float TEX_W = 100.0f;
        final float TEX_H = 10.0f;
        final int BORDER = 1;
        final int CENTER_SRC_W = (int) (TEX_W - 2 * BORDER); // 98
        final int CENTER_SRC_H = (int) (TEX_H - 2 * BORDER); // 8

        // UV coordinates for the 9-slice regions
        float uBorderL = BORDER / TEX_W;          // 0.01
        float uBorderR = (TEX_W - BORDER) / TEX_W; // 0.99
        float vBorderT = BORDER / TEX_H;           // 0.1
        float vBorderB = (TEX_H - BORDER) / TEX_H; // 0.9

        boolean fullBar = (clipWidth >= targetWidth);
        int rightBorder = fullBar ? BORDER : 0;
        int centerDstW = clipWidth - BORDER - rightBorder;
        int centerDstH = targetHeight - 2 * BORDER;
        if (centerDstW < 0) centerDstW = 0;
        if (centerDstH < 0) centerDstH = 0;

        // Screen coordinates for the border edges
        int sx1 = x + BORDER;           // left border ends
        int sy1 = y + BORDER;           // top border ends
        int sy2 = y + targetHeight - BORDER; // bottom border starts
        int rx = x + targetWidth - BORDER;   // right border starts (only used when full)

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX);

        // --- Left border column (1px wide) ---
        if (clipWidth >= BORDER) {
            // Top-left corner
            nineSliceQuad(buffer, x, y, BORDER, BORDER, 0, 0, uBorderL, vBorderT);
            // Left edge (tiled vertically)
            addTiledQuads(buffer, x, sy1, BORDER, centerDstH, 0, vBorderT, BORDER, CENTER_SRC_H, TEX_W, TEX_H);
            // Bottom-left corner
            nineSliceQuad(buffer, x, sy2, BORDER, BORDER, 0, vBorderB, uBorderL, 1.0f);
        }

        // --- Center columns (tiled horizontally and vertically) ---
        if (centerDstW > 0) {
            // Top edge
            addTiledQuads(buffer, sx1, y, centerDstW, BORDER, uBorderL, 0, CENTER_SRC_W, BORDER, TEX_W, TEX_H);
            // Center fill
            addTiledQuads(buffer, sx1, sy1, centerDstW, centerDstH, uBorderL, vBorderT, CENTER_SRC_W, CENTER_SRC_H, TEX_W, TEX_H);
            // Bottom edge
            addTiledQuads(buffer, sx1, sy2, centerDstW, BORDER, uBorderL, vBorderB, CENTER_SRC_W, BORDER, TEX_W, TEX_H);
        }

        // --- Right border column (1px wide, only when fully filled) ---
        if (fullBar) {
            // Top-right corner
            nineSliceQuad(buffer, rx, y, BORDER, BORDER, uBorderR, 0, 1.0f, vBorderT);
            // Right edge (tiled vertically)
            addTiledQuads(buffer, rx, sy1, BORDER, centerDstH, uBorderR, vBorderT, BORDER, CENTER_SRC_H, TEX_W, TEX_H);
            // Bottom-right corner
            nineSliceQuad(buffer, rx, sy2, BORDER, BORDER, uBorderR, vBorderB, 1.0f, 1.0f);
        }

        tessellator.draw();
    }

    /**
     * Add a single quad to the buffer builder for 9-slice rendering.
     */
    private void nineSliceQuad(BufferBuilder buffer, int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
        buffer.pos(x, y + h, this.zLevel).tex(u1, v2).endVertex();
        buffer.pos(x + w, y + h, this.zLevel).tex(u2, v2).endVertex();
        buffer.pos(x + w, y, this.zLevel).tex(u2, v1).endVertex();
        buffer.pos(x, y, this.zLevel).tex(u1, v1).endVertex();
    }

    /**
     * Tile a texture sub-region across a screen rectangle at 1:1 pixel ratio.
     * When the screen area is smaller than the source, only a portion of the texture is shown (cropped).
     * When larger, the source pattern repeats (tiled).
     *
     * @param buffer    BufferBuilder to append quads to
     * @param screenX   Screen X start
     * @param screenY   Screen Y start
     * @param screenW   Screen width to fill
     * @param screenH   Screen height to fill
     * @param srcU      UV X start of the source region
     * @param srcV      UV Y start of the source region
     * @param srcTexW   Source region width in texels
     * @param srcTexH   Source region height in texels
     * @param texTotalW Total texture width in pixels
     * @param texTotalH Total texture height in pixels
     */
    private void addTiledQuads(BufferBuilder buffer, int screenX, int screenY, int screenW, int screenH,
                               float srcU, float srcV, int srcTexW, int srcTexH, float texTotalW, float texTotalH) {
        if (screenW <= 0 || screenH <= 0) return;
        int curX = 0;
        while (curX < screenW) {
            int tileW = Math.min(srcTexW, screenW - curX);
            float uEnd = srcU + tileW / texTotalW;
            int curY = 0;
            while (curY < screenH) {
                int tileH = Math.min(srcTexH, screenH - curY);
                float vEnd = srcV + tileH / texTotalH;
                nineSliceQuad(buffer, screenX + curX, screenY + curY, tileW, tileH, srcU, srcV, uEnd, vEnd);
                curY += tileH;
            }
            curX += tileW;
        }
    }

    private AMVector2 getShiftedVector(AMVector2 configVec, int screenWidth, int screenHeight) {
        int x = (int) Math.round(configVec.x * screenWidth);
        int y = (int) Math.round(configVec.y * screenHeight);

        return new AMVector2(x, y);
    }

    class PotionEffectDurationComparator implements Comparator<PotionEffect> {


        public PotionEffectDurationComparator() {
        }

        @Override
        public int compare(PotionEffect o1, PotionEffect o2) {
            if (o1.getDuration() < o2.getDuration()) return -1;
            else if (o1.getDuration() > o2.getDuration()) return 1;
            else return 0;
        }

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent e) {
        if (e.getItemStack().getItem() instanceof ItemBlock) {
            if (((ItemBlock) e.getItemStack().getItem()).getBlock() instanceof BlockManaBattery)
                if (e.getItemStack().getTagCompound() != null) {
                    float charge = e.getItemStack().getTagCompound().getFloat("mana_battery_charge");
                    PowerTypes powerType = PowerTypes.getByID(e.getItemStack().getTagCompound().getInteger("mana_battery_powertype"));
                    e.getToolTip().add(String.format("\u00A7r\u00A79Contains \u00A75%.2f %s%s \u00A79etherium", charge, powerType.getChatColor(), powerType.name()));
                }
        }
    }
}
