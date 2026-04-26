package am2.client.models;

import am2.api.event.RenderingItemEvent;
import am2.common.utils.ModelUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpecialRenderModelLoader implements ICustomModelLoader {

    public class Baked implements IBakedModel {

        private ItemStack stack = ItemStack.EMPTY;
        private EntityLivingBase entity = null;

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return new ArrayList<>();
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
            return true;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return new Overrides(this);
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            MinecraftForge.EVENT_BUS.post(new RenderingItemEvent(this.stack, cameraTransformType, this.entity));
            return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
        }

    }

    class Overrides extends ItemOverrideList {

        Baked baked;

        public Overrides(Baked baked) {
            super(new ArrayList<>());
            this.baked = baked;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            this.baked.stack = stack;
            this.baked.entity = entity;
            return super.handleItemState(originalModel, stack, world, entity);
        }

    }

    class Model implements IModel {
        public Collection<ResourceLocation> getDependencies() {
            return Lists.newArrayList();
        }

        public Collection<ResourceLocation> getTextures() {
            return Lists.newArrayList();
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, java.util.function.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            return new SpecialRenderModelLoader.Baked();
        }

        public IModelState getDefaultState() {
            return ModelUtils.DEFAULT_ITEM_STATE;
        }
    }

    static ImmutableMap<TransformType, TRSRTransformation> transforms = PerspectiveMapWrapper.getTransforms(ModelUtils.DEFAULT_ITEM_STATE);

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return (modelLocation.toString().contains("nature_scythe") ||
                modelLocation.toString().contains("arcane_spellbook") ||
                modelLocation.toString().contains("bound_shield")) && modelLocation.getNamespace().equals("arsmagica2") && !modelLocation.toString().contains(".obj");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) {
        return new SpecialRenderModelLoader.Model();
    }

}
