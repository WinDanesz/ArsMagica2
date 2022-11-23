package am2.client.items.rendering;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;

public class ModelCullface implements IModel {
	
	IModel parent;
	
	public ModelCullface(IModel parent) {
		this.parent = parent;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return parent.getDependencies();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return parent.getTextures();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, java.util.function.Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		ImmutableMap<TransformType, TRSRTransformation> map = PerspectiveMapWrapper.getTransforms(state);
		return new BakedModelCullface(parent.bake(state, format, bakedTextureGetter), map);
	}

	@Override
	public IModelState getDefaultState() {
		return parent.getDefaultState();
	}

}
