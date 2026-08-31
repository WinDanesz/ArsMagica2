package am2.client.items.rendering;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.List;

public class SpellBakedModel extends PerspectiveMapWrapper {

    private IBakedModel parent;
    private ImmutableMap<TransformType, TRSRTransformation> transforms;

    public SpellBakedModel(IBakedModel parent, ImmutableMap<TransformType, TRSRTransformation> transforms) {
        super(parent, transforms);
        this.parent = parent;
        this.transforms = transforms;
    }

    @Override
    public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
        return parent.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return parent.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return parent.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return parent.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return parent.getParticleTexture();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return parent.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new SpellParticleRender(parent.getOverrides().getOverrides());
    }


    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        // Apply custom scaling for dropped items on the ground
        if (cameraTransformType == TransformType.GROUND) {
            TRSRTransformation transform = new TRSRTransformation(
                    new Vector3f(0, 0, 0),          // translation
                    null,                                          // rotation (null = identity)
                    new Vector3f(0.3f, 0.3f, 0.3f), // scale to 50%
                    null                                           // right rotation
            );
            return Pair.of(this, transform.getMatrix());
        }
        // Use default transforms for other perspectives
        return PerspectiveMapWrapper.handlePerspective(this, transforms, cameraTransformType);
    }

}
