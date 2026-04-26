package am2.client.items.rendering;

import am2.common.items.ItemSpellBook;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SpellBookBakedModel implements IBakedModel {

    private final IBakedModel bookModel;
    private final IRegistry<ModelResourceLocation, IBakedModel> modelRegistry;
    // Cached from handleItemState so handlePerspective can propagate them to bookModel
    private ItemStack cachedStack = ItemStack.EMPTY;
    private EntityLivingBase cachedEntity = null;

    public SpellBookBakedModel(IBakedModel bookModel, IRegistry<ModelResourceLocation, IBakedModel> modelRegistry) {
        this.bookModel = bookModel;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        return bookModel.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return bookModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return bookModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return bookModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return bookModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return new SpellBookOverrides(this);
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
        // Propagate the current stack/entity into the inner model's overrides so that
        // SpecialRenderModelLoader.Baked (and similar) fires events with the correct stack.
        bookModel.getOverrides().handleItemState(bookModel, cachedStack, null, cachedEntity);
        return bookModel.handlePerspective(cameraTransformType);
    }

    private class SpellBookOverrides extends ItemOverrideList {

        private final SpellBookBakedModel parent;

        public SpellBookOverrides(SpellBookBakedModel parent) {
            super(java.util.Collections.emptyList());
            this.parent = parent;
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            // Cache for propagation to the inner bookModel in handlePerspective
            SpellBookBakedModel.this.cachedStack = stack;
            SpellBookBakedModel.this.cachedEntity = entity;

            // Check if this is being rendered for a player holding the book and has an active spell
            if (entity instanceof EntityPlayer &&
                    stack.getItem() instanceof ItemSpellBook) {

                // Get the active spell from the book
                ItemStack activeSpell = ((ItemSpellBook) stack.getItem()).getActiveItemStack(stack);

                // If there's an active spell, return a wrapper that will use the spell model in first person
                if (!activeSpell.isEmpty() && activeSpell.getItem() != null) {
                    // For spell items, we need to get their actual baked model through Minecraft's render item
                    // because they use custom mesh definitions
                    IBakedModel spellModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(activeSpell, world, entity);

                    if (spellModel != null) {
                        // Return a dynamic model that shows spell in first/third person, book in GUI/inventory
                        return new SpellBookDynamicModel(parent.bookModel, spellModel);
                    }
                }
            }

            // Default: return the book model
            return originalModel;
        }
    }

    /**
     * Dynamic model that renders as spell in first person, book otherwise
     */
    private static class SpellBookDynamicModel implements IBakedModel {
        private final IBakedModel bookModel;
        private final IBakedModel spellModel;
        private boolean useSpellModel = false;

        public SpellBookDynamicModel(IBakedModel bookModel, IBakedModel spellModel) {
            this.bookModel = bookModel;
            this.spellModel = spellModel;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return useSpellModel ? spellModel.getQuads(state, side, rand) : bookModel.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return bookModel.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return bookModel.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return bookModel.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return bookModel.getParticleTexture();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType) {
            // Use spell model for all in-hand views (first and third person)
            useSpellModel = (cameraTransformType == TransformType.FIRST_PERSON_RIGHT_HAND ||
                    cameraTransformType == TransformType.FIRST_PERSON_LEFT_HAND ||
                    cameraTransformType == TransformType.THIRD_PERSON_RIGHT_HAND ||
                    cameraTransformType == TransformType.THIRD_PERSON_LEFT_HAND);

            // Use the appropriate model's perspective
            return useSpellModel ? spellModel.handlePerspective(cameraTransformType) :
                    bookModel.handlePerspective(cameraTransformType);
        }
    }
}
