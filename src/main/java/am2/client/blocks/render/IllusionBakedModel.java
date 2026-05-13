package am2.client.blocks.render;

import am2.common.blocks.BlockIllusionBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Delegates getQuads() to whichever block the illusion block is mimicking.
 * The mimic is carried via IExtendedBlockState so the result is baked into
 * the chunk VBO — zero per-frame cost compared to a TESR.
 */
@SideOnly(Side.CLIENT)
public class IllusionBakedModel implements IBakedModel {

    private final IBakedModel fallback; // plain illusion_block texture (used when revealed)

    public IllusionBakedModel(IBakedModel fallback) {
        this.fallback = fallback;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IBlockState mimic = ((IExtendedBlockState) state).getValue(BlockIllusionBlock.MIMIC_BLOCK);
            if (mimic != null && mimic.getBlock() != Blocks.AIR) {
                // Only emit quads for the layer the chunk builder is currently processing.
                // This makes transparent mimics (glass) render in TRANSLUCENT and
                // opaque mimics (stone) render in SOLID — matching vanilla behaviour.
                BlockRenderLayer currentLayer = MinecraftForgeClient.getRenderLayer();
                if (currentLayer != null && !mimic.getBlock().canRenderInLayer(mimic, currentLayer)) {
                    return Collections.emptyList();
                }
                return Minecraft.getMinecraft()
                        .getBlockRendererDispatcher()
                        .getBlockModelShapes()
                        .getModelForState(mimic)
                        .getQuads(mimic, side, rand);
            }
        }
        // No mimic set (block revealed by true sight, or scan not yet complete)
        return fallback.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return fallback.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
