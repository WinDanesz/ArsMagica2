package am2.client;

import am2.client.blocks.render.IllusionBakedModel;
import am2.common.blocks.BlockIllusionBlock;
import am2.ArsMagica;
import am2.api.items.IMultiTexturedItem;
import am2.client.compat.electroblob.EBWizSpellBindingRenderer;
import am2.client.render.AMItemStackRenderer;
import am2.client.render.BakedItemModelWrapper;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.items.ItemBlockBaked;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ArsMagica.MODID, value = Side.CLIENT)
public final class AMModels {

    //	public static final ModelResourceLocation mrl = new ModelResourceLocation(ArsMagica2.MODID + ":keystone_chest");
    public static List<ModelResourceLocation> modelResourceLocationList = new ArrayList<>();
    private static final String FLUID_MODEL_PATH = ArsMagica.MODID + ":fluid";

    /**
     * Keeps track of all items whose models have been registered manually to exclude them from automatic registry of
     * standard item models. Internal only, this gets cleared once model registry is complete.
     */
    private static final List<Item> registeredItems = new ArrayList<>();

    /**
     * Registers an item model, using the itemstack-sensitive {@link IMultiTexturedItem#getModelName(ItemStack)} as the
     * model name. This allows items to change their texture based on metadata/NBT. Variant defaults to "normal". Registers the
     * model for metadata 0 automatically, plus all the other metadata values that the item can take, as defined in
     * {@link Item#getSubItems(CreativeTabs, NonNullList)}. The creative tab supplied
     * to the aforementioned method will be whichever one the item is in.
     * Author: Electroblob
     */
    private static <T extends Item & IMultiTexturedItem> void registerMultiTexturedModel(T item) {
        registeredItems.add(item);
    }

    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {

        //registerMultiTexturedModel((ItemInfinityOrb) AMItems.infinity_orb);
        // registerMultiTexturedModel((ItemBindingCatalyst) AMItems.binding_catalyst);

        // Automatic item model registry
        for (Item item : Item.REGISTRY) {

            // all Ars Magica items
            if (!registeredItems.contains(item) && item.getRegistryName().getNamespace().equals(ArsMagica.MODID)) {

                // multitextured models (usually with metadata)
                if (item instanceof IMultiTexturedItem && item.getHasSubtypes()) {
                    NonNullList<ItemStack> items = NonNullList.create();
                    item.getSubItems(item.getCreativeTab(), items);
                    for (ItemStack stack : items) {
                        if (((IMultiTexturedItem) item).getModelName(stack) == null) {
                            // todo log
                        } else {
                            ModelLoader.setCustomModelResourceLocation(item, stack.getMetadata(),
                                    new ModelResourceLocation(((IMultiTexturedItem) item).getModelName(stack), "inventory"));

                        }
                    }
                } else if (item instanceof am2.common.items.ItemBlockCrystalMarker) {
                    // Crystal Marker with metadata variants - use TESR for all variants
                    item.setTileEntityItemStackRenderer(new AMItemStackRenderer());
                    ModelResourceLocation loc = new ModelResourceLocation(((am2.common.items.ItemBlockCrystalMarker) item).getBlock().getRegistryName().toString());
                    // Register model for all metadata variants
                    for (int meta = 0; meta <= 8; meta++) {
                        ModelLoader.setCustomModelResourceLocation(item, meta, loc);
                    }
                    modelResourceLocationList.add(loc);
                } else if (item instanceof ItemBlockBaked) {
                    // ItemBlocks of blocks that doesn't have any json models
                    item.setTileEntityItemStackRenderer(new AMItemStackRenderer());
                    ModelResourceLocation loc = new ModelResourceLocation(((ItemBlockBaked) item).getBlock().getRegistryName().toString());
                    ModelLoader.setCustomModelResourceLocation(item, 0, loc);
                    modelResourceLocationList.add(loc);
                } else if (item == AMItems.spell) {
                    ModelLoader.setCustomMeshDefinition(item, new am2.client.items.rendering.SpellRenderer());
                } else if (AMItems.ebwiz_spell_binding != null && item == AMItems.ebwiz_spell_binding) {
                    // EBWiz spell binding:
                    // - In GUI (inventory): use builtin/entity so TEISR fires and draws the EBWiz spell's
                    //   own icon texture (per-spell, dynamic).
                    // - In hand / world: use a static item/generated model keyed to the EBWiz element.
                    //   Element.FIRE   → fire aspect model (fireball-red icon)
                    //   Element.MAGIC  → arcane aspect model (beam-blue icon)
                    item.setTileEntityItemStackRenderer(new EBWizSpellBindingRenderer());

                    final ModelResourceLocation ebwizGuiMRL  = new ModelResourceLocation(new ResourceLocation(ArsMagica.MODID, "ebwiz_spell_binding"), "inventory");
                    final ModelResourceLocation fireHandMRL   = new ModelResourceLocation(new ResourceLocation(ArsMagica.MODID, "ebwiz_spell_fire"),    "inventory");
                    final ModelResourceLocation arcaneHandMRL = new ModelResourceLocation(new ResourceLocation(ArsMagica.MODID, "ebwiz_spell_arcane"),  "inventory");

                    ModelBakery.registerItemVariants(item, ebwizGuiMRL, fireHandMRL, arcaneHandMRL);
                    ModelLoader.setCustomMeshDefinition(item, stack -> {
                        // Only switch to a SpellBakedModel-wrapped model when NOT rendering inside
                        // a GUI screen (i.e. when held in hand, on the ground, in the hotbar, etc.).
                        // Both element models are identical (builtin/entity + SpellBakedModel);
                        // SpellParticleRender reads the actual affinity via getAM2Affinity() at render
                        // time, so any wrapped model works for any element.
                        if (Minecraft.getMinecraft().currentScreen == null) {
                            if (ItemEBWizSpellBinding.getSpell(stack) != null) {
                                return fireHandMRL;
                            }
                        }
                        // GUI context → builtin/entity triggers TEISR (EBWiz spell icon)
                        return ebwizGuiMRL;
                    });
                } else {
                    registerItemModel(item); // Standard item model
                }
            }
        }

        // register fluids
        registerFluidModel((IFluidBlock) AMBlocks.liquid_essence_block);


        registeredItems.clear(); // Might as well clean this up
    }

    private static void registerFluidModel(IFluidBlock fluidBlock) {
        final Item item = Item.getItemFromBlock((Block) fluidBlock);
        assert item != null;

        ModelBakery.registerItemVariants(item);

        final ModelResourceLocation modelResourceLocation = new ModelResourceLocation(FLUID_MODEL_PATH, fluidBlock.getFluid().getName());

        ModelLoader.setCustomMeshDefinition(item, s -> modelResourceLocation);
        ModelLoader.setCustomStateMapper((Block) fluidBlock, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState p_178132_1_) {
                return modelResourceLocation;
            }
        });
    }

    /**
     * Registers an item model, using the item's registry name as the model name (this convention makes it easier to
     * keep track of everything). Variant defaults to "normal". Registers the model for all metadata values.
     */
    private static void registerItemModel(Item item) {

        // Changing the last parameter from null to "inventory" fixed the item/block model weirdness. No idea why!
        ModelBakery.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        // Assigns the model for all metadata values
        ModelLoader.setCustomMeshDefinition(item, s -> new ModelResourceLocation(item.getRegistryName(), "inventory"));
        registeredItems.add(item);
    }


    @SubscribeEvent
    public static void modelReg(ModelRegistryEvent event) {

//		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeystoneChest.class, new TileKeystoneChestRenderer());
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        // Replace illusion block models with IllusionBakedModel so the mimic appearance is
        // baked into the chunk VBO via IExtendedBlockState — no TESR needed.
        for (BlockIllusionBlock.EnumIllusionType type : BlockIllusionBlock.EnumIllusionType.values()) {
            ModelResourceLocation mrl = new ModelResourceLocation(
                    AMBlocks.illusion_block.getRegistryName(), "illusion_type=" + type.getName());
            IBakedModel fallback = event.getModelRegistry().getObject(mrl);
            if (fallback != null) {
                event.getModelRegistry().putObject(mrl, new IllusionBakedModel(fallback));
            }
        }

        for (ModelResourceLocation modelResourceLocation : modelResourceLocationList) {

            IBakedModel model = event.getModelRegistry().getObject(modelResourceLocation);
            event.getModelRegistry().putObject(modelResourceLocation, new BakedItemModelWrapper(model));
        }

        // Programmatically create and register spell icon models from textures
        for (net.minecraft.util.ResourceLocation iconResource : am2.client.items.rendering.SpellRenderer.resources) {
            ModelResourceLocation mrl = new ModelResourceLocation(iconResource, "inventory");
            // Create a simple item layer model using ItemLayerModel
            com.google.common.collect.ImmutableList<net.minecraft.util.ResourceLocation> textures =
                    com.google.common.collect.ImmutableList.of(new net.minecraft.util.ResourceLocation("arsmagica2", "items/" + iconResource.getPath()));

            net.minecraft.client.renderer.vertex.VertexFormat format = net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM;
            java.util.function.Function<net.minecraft.util.ResourceLocation, net.minecraft.client.renderer.texture.TextureAtlasSprite> textureGetter =
                    location -> net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());

            net.minecraftforge.client.model.ItemLayerModel layerModel = new net.minecraftforge.client.model.ItemLayerModel(textures);
            net.minecraftforge.common.model.TRSRTransformation transform = net.minecraftforge.common.model.TRSRTransformation.identity();
            IBakedModel bakedModel = layerModel.bake(transform, format, textureGetter);

            // Wrap with SpellBakedModel for custom perspective handling
            com.google.common.collect.ImmutableMap<net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType, net.minecraftforge.common.model.TRSRTransformation> transforms =
                    net.minecraftforge.client.model.PerspectiveMapWrapper.getTransforms(transform);
            IBakedModel wrappedModel = new am2.client.items.rendering.SpellBakedModel(bakedModel, transforms);

            event.getModelRegistry().putObject(mrl, wrappedModel);
        }

        // Wrap EBWiz element hand models in SpellBakedModel so SpellParticleRender
        // fires when these items are held, showing the affinity hand-glow animation.
        if (AMItems.ebwiz_spell_binding != null) {
            for (String name : new String[]{"ebwiz_spell_fire", "ebwiz_spell_arcane"}) {
                ModelResourceLocation mrl = new ModelResourceLocation(
                        new ResourceLocation(ArsMagica.MODID, name), "inventory");
                IBakedModel base = event.getModelRegistry().getObject(mrl);
                if (base != null) {
                    com.google.common.collect.ImmutableMap<net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType,
                            net.minecraftforge.common.model.TRSRTransformation> transforms =
                            net.minecraftforge.client.model.PerspectiveMapWrapper.getTransforms(
                                    net.minecraftforge.common.model.TRSRTransformation.identity());
                    event.getModelRegistry().putObject(mrl,
                            new am2.client.items.rendering.SpellBakedModel(base, transforms));
                }
            }
        }

        // Wrap spell book models to render active spell in first person
        for (Object key : event.getModelRegistry().getKeys()) {
            if (key instanceof ModelResourceLocation) {
                ModelResourceLocation mrl = (ModelResourceLocation) key;
                if (mrl.toString().equals("arsmagica2:spellbook#inventory") ||
                        mrl.toString().equals("arsmagica2:arcane_spellbook#inventory")) {
                    IBakedModel originalModel = event.getModelRegistry().getObject(mrl);
                    event.getModelRegistry().putObject(mrl,
                            new am2.client.items.rendering.SpellBookBakedModel(originalModel, event.getModelRegistry()));
                }
            }
        }
    }

//	public static void initModel(Block block, int meta) {
//		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName(), "inventory"));
//	}

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        //	MinecraftForge.EVENT_BUS.register(new am2.common.handler.SpellCa());
    }

}
