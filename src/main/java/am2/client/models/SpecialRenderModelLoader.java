package am2.client.models;

import am2.api.event.RenderingItemEvent;
import am2.common.utils.ModelUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
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

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class SpecialRenderModelLoader implements ICustomModelLoader{
	
	public class Baked extends PerspectiveMapWrapper {
		
		private ItemStack stack = null;
		private EntityLivingBase entity = null;

		public Baked(IBakedModel parent, ImmutableMap<TransformType, TRSRTransformation> transforms) {
			super(parent, transforms);
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
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
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
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
		@Override
		public Collection<ResourceLocation> getDependencies() {
			return Lists.newArrayList();
		}

		@Override
		public Collection<ResourceLocation> getTextures() {
			return Lists.newArrayList();
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			return null;
		}

		//		@Override
//		public IBakedModel bake(IModelState state, VertexFormat format, java.util.function.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//			return new SpecialRenderModelLoader.Baked();
//		}

		@Override
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
				modelLocation.toString().contains("winter_arm") ||
				modelLocation.toString().contains("air_sled") ||
				modelLocation.toString().contains("fire_ears") ||
				modelLocation.toString().contains("water_orbs") ||
				modelLocation.toString().contains("earth_armor") ||
				modelLocation.toString().contains("arcane_spellbook") ||
				modelLocation.toString().contains("keystone_recepticle") ||
				modelLocation.toString().contains("flicker_habitat") ||
				modelLocation.toString().contains("crystal_marker") ||
				modelLocation.toString().contains("keystone_chest") ||
				modelLocation.toString().contains("magic_broom") ||
				modelLocation.toString().contains("essence_conduit") ||
				modelLocation.toString().contains("arcane_reconstructor") ||
				modelLocation.toString().contains("seer_stone") ||
				modelLocation.toString().contains("bound_shield") ||
				modelLocation.toString().contains("calefactor") ||
				modelLocation.toString().contains("magicians_workbench") ||
				modelLocation.toString().contains("summoner") ||
				modelLocation.toString().contains("astral_barrier") ||
				modelLocation.toString().contains("otherworld_aura")) && modelLocation.getNamespace().equals("arsmagica2") && !modelLocation.toString().contains(".obj");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) {
		return new SpecialRenderModelLoader.Model();
	}

}
