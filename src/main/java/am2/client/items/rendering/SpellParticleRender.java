package am2.client.items.rendering;

import am2.api.affinity.Affinity;
import am2.api.extensions.ISpellCaster;
import am2.client.particles.AMParticleIcons;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.items.ItemSpellBase;
import am2.common.items.ItemSpellBook;
import am2.common.registry.AMItems;
import am2.common.registry.Affinities;
import am2.common.spell.SpellCaster;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class SpellParticleRender extends ItemOverrideList {
    private final Minecraft mc;
    private final Map<Affinity, TextureAtlasSprite> icons;
    private boolean setupIcons = false;
    private static final ResourceLocation rLoc = TextureMap.LOCATION_BLOCKS_TEXTURE;

    public SpellParticleRender(List<ItemOverride> overridesIn) {
        super(overridesIn);
        new ModelBiped(0.0F);
        mc = Minecraft.getMinecraft();
        icons = new HashMap<Affinity, TextureAtlasSprite>();
    }

    @Override
    public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
        if (world == null || entity == null || !renderItem(stack, entity))
            return super.handleItemState(originalModel, stack, world, entity);
        return new BakedBlank();
    }

    private class BakedBlank implements IBakedModel {

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            return Lists.newArrayList();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return new ItemOverrideList(new ArrayList<>());
        }
    }

    public boolean renderItem(ItemStack item, EntityLivingBase entity) {

        if (mc.player.isPotionActive(MobEffects.INVISIBILITY))
            return true;

        // EBWiz compat: map element → hand icon name and show the hand glow without
        // going through the SpellCaster capability (which EBWiz items do not have).
        // Uses getHandIconName() so that elements with dedicated icons (SORCERY, HEALING)
        // get their own textures rather than an AM2 affinity fallback.
        if (AMItems.ebwiz_spell_binding != null && item.getItem() == AMItems.ebwiz_spell_binding) {
            String iconName = ItemEBWizSpellBinding.getHandIconName(item);
            if (iconName == null) return false;
            renderEffectNamed(iconName, true, entity);
            return true;
        }

        ItemStack scrollStack = ItemStack.EMPTY;
        ;
        if (item.getItem() instanceof ItemSpellBase) {
            scrollStack = item;
        } else if (item.getItem() instanceof ItemSpellBook) {
            scrollStack = ((ItemSpellBook) item.getItem()).getActiveScrollInventory(item)[((ItemSpellBook) item.getItem()).getActiveSlot(item)];
        }

        if (scrollStack.isEmpty()) {
            return false;
        }

        if (!scrollStack.hasCapability(SpellCaster.INSTANCE, null)) {
            return false;
        }

        ISpellCaster caster = scrollStack.getCapability(SpellCaster.INSTANCE, null);
        if (caster == null) {
            return false;
        }

        Affinity affinity = caster.createSpellData(scrollStack).getMainShift();

        renderEffect(affinity, true, entity);
        return true;
    }

    public void renderEffect(Affinity affinity, boolean includeArm, EntityLivingBase entity) {

        if (!setupIcons) {
            setupAffinityIcons();
            setupIcons = true;
        }

        GL11.glPushMatrix();
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float scale = 3f;
        if (entity == mc.player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {

            GL11.glPushMatrix();

//			if (((EntityPlayer)entity).getItemInUseCount() > 0){
//				GL11.glRotatef(120, 1, 0, 1);
//				GL11.glRotatef(-10, 0, 1, 0);
//				GL11.glTranslatef(2f, 0f, 0.8f);
//			}else{
//				GL11.glTranslatef(3f, -1.2f, -2.5f);
//				GL11.glScalef(scale, scale, scale);
//				GL11.glRotatef(-45, 0, 1, 0);
//			}
//			GlStateManager.rotate(90, 0, 1, 0);
            RenderHelper.disableStandardItemLighting();
            GL11.glTranslatef(0.0f, 0.0f, -2.5f);
            RenderByAffinity(affinity);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();

            if (includeArm && mc.currentScreen == null) {
                Minecraft.getMinecraft().renderEngine.bindTexture(mc.player.getLocationSkin());
                renderFirstPersonArm(mc.player);
            }
        } else if (mc.currentScreen == null) {
            GL11.glTranslatef(-0.25f, 0.0f, 0.0f);
            scale = 0.5f;
            GL11.glScalef(scale, scale, scale);
            //GL11.glRotatef(45, 0, 0, 0);
            RenderByAffinity(affinity);
        }

        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(3042);

        GL11.glPopMatrix();
    }

    private void setupAffinityIcons() {
        icons.put(Affinities.air, AMParticleIcons.instance.getIconByName("air_hand"));
        icons.put(Affinities.arcane, AMParticleIcons.instance.getIconByName("arcane_hand"));
        icons.put(Affinities.earth, AMParticleIcons.instance.getIconByName("earth_hand"));
        icons.put(Affinities.ender, AMParticleIcons.instance.getIconByName("ender_hand"));
        icons.put(Affinities.fire, AMParticleIcons.instance.getIconByName("fire_hand"));
        icons.put(Affinities.ice, AMParticleIcons.instance.getIconByName("ice_hand"));
        icons.put(Affinities.life, AMParticleIcons.instance.getIconByName("life_hand"));
        icons.put(Affinities.lightning, AMParticleIcons.instance.getIconByName("lightning_hand"));
        icons.put(Affinities.nature, AMParticleIcons.instance.getIconByName("nature_hand"));
        icons.put(Affinities.none, AMParticleIcons.instance.getIconByName("none_hand"));
        icons.put(Affinities.water, AMParticleIcons.instance.getIconByName("water_hand"));
    }

    /** Renders the hand-glow effect using an icon looked up by name from {@link AMParticleIcons}. */
    public void renderEffectNamed(String iconName, boolean includeArm, EntityLivingBase entity) {

        if (!setupIcons) {
            setupAffinityIcons();
            setupIcons = true;
        }

        GL11.glPushMatrix();
        GL11.glEnable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float scale = 3f;
        if (entity == mc.player && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {

            GL11.glPushMatrix();
            RenderHelper.disableStandardItemLighting();
            GL11.glTranslatef(0.0f, 0.0f, -2.5f);
            RenderByIconName(iconName);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();

            if (includeArm && mc.currentScreen == null) {
                Minecraft.getMinecraft().renderEngine.bindTexture(mc.player.getLocationSkin());
                renderFirstPersonArm(mc.player);
            }
        } else if (mc.currentScreen == null) {
            GL11.glTranslatef(-0.25f, 0.0f, 0.0f);
            scale = 0.5f;
            GL11.glScalef(scale, scale, scale);
            RenderByIconName(iconName);
        }

        GL11.glDisable(32826 /*GL_RESCALE_NORMAL_EXT*/);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(3042);

        GL11.glPopMatrix();
    }

    /** Renders the hand-glow sprite looked up by name from {@link AMParticleIcons}. */
    public void RenderByIconName(String iconName) {
        Minecraft.getMinecraft().renderEngine.bindTexture(rLoc);
        TextureAtlasSprite icon = AMParticleIcons.instance.getIconByName(iconName);
        if (icon == null) return;
        float TLX = icon.getMinU();
        float BRX = icon.getMaxU();
        float TLY = icon.getMinV();
        float BRY = icon.getMaxV();
        doRender(TLX, TLY, BRX, BRY);
    }

    public void RenderByAffinity(Affinity affinity) {

        Minecraft.getMinecraft().renderEngine.bindTexture(rLoc);

        TextureAtlasSprite icon = icons.get(affinity);
        if (icon == null) return;

        float TLX = icon.getMinU();
        float BRX = icon.getMaxU();
        float TLY = icon.getMinV();
        float BRY = icon.getMaxV();

        doRender(TLX, TLY, BRX, BRY);
    }


    private void doRender(float TLX, float TLY, float BRX, float BRY) {
        //ItemRenderer.renderItemIn2D(Tessellator.instance, TLX, TLY, BRX, BRY, 1, 1, 0.0625F);
        Tessellator t = Tessellator.getInstance();
        t.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        t.getBuffer().pos(0.0D, 0.0D, 0.0D).tex(TLX, BRY).normal(0.0F, 0.0F, 1.0F).endVertex();
        t.getBuffer().pos(1.0D, 0.0D, 0.0D).tex(BRX, BRY).normal(0.0F, 0.0F, 1.0F).endVertex();
        t.getBuffer().pos(1.0D, 1.0D, 0.0D).tex(BRX, TLY).normal(0.0F, 0.0F, 1.0F).endVertex();
        t.getBuffer().pos(0.0D, 1.0D, 0.0D).tex(TLX, TLY).normal(0.0F, 0.0F, 1.0F).endVertex();
        t.draw();
    }

    private void renderFirstPersonArm(EntityPlayerSP player) {
        EnumHandSide hand = EnumHandSide.LEFT;
        boolean flag = hand != EnumHandSide.LEFT;
        float f = flag ? 1.0F : -1.0F;
        float f1 = MathHelper.sqrt(0);
        GlStateManager.rotate(f * 45.0F, 0.0F, 1.0F, 0.0F);
        float f5 = MathHelper.sin(0 * 0 * (float) Math.PI);
        float f6 = MathHelper.sin(f1 * (float) Math.PI);
        GlStateManager.rotate(f * f6 * 70.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f * f5 * -20.0F, 0.0F, 0.0F, 1.0F);
        AbstractClientPlayer abstractclientplayer = this.mc.player;
        this.mc.getTextureManager().bindTexture(abstractclientplayer.getLocationSkin());
        GlStateManager.translate(f * -1.0F, 3.6F, 3.5F);
        GlStateManager.rotate(f * 120.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f * -135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(f * 5.6F, 0.0F, 0.0F);
        Object renderObject = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(abstractclientplayer);
        RenderPlayer renderplayer = (renderObject instanceof RenderPlayer) ? (RenderPlayer) renderObject : null;
        GlStateManager.disableCull();
        if (renderplayer != null) {
            if (flag) {
                renderplayer.renderRightArm(abstractclientplayer);
            } else {
                renderplayer.renderLeftArm(abstractclientplayer);
            }
        }

        GlStateManager.enableCull();
    }

}
