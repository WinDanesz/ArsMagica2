package am2.common;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.CraftingAltarMaterials;
import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.*;
import am2.api.flickers.FlickerGenerationPool;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.spell.SpellPart;
import am2.client.particles.ParticleManagerServer;
import am2.common.affinity.AffinityAbilityHelper;
import am2.common.armor.ArmorEventHandler;
import am2.common.blocks.tileentity.*;
import am2.common.compat.ancientspellcraft.AncientSpellcraftCompatBootstrap;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.potioncore.PotioncoreCompatBootstrap;
import am2.common.container.*;
import am2.common.defs.KeystoneLocatons;
import am2.common.extensions.RiftStorage;
import am2.common.handler.*;
import am2.common.items.ItemEssenceBag;
import am2.common.items.ItemKeystone;
import am2.common.items.ItemRuneBag;
import am2.common.items.ItemSpellBook;
import am2.common.lore.CompendiumUnlockHandler;
import am2.common.network.SeventhSanctum;
import am2.common.power.PowerNodeCache;
import am2.common.power.PowerNodeEntry;
import am2.common.power.PowerTypes;
import am2.common.registry.*;
import am2.common.spell.SpellUnlockManager;
import am2.common.trackers.ItemFrameWatcher;
import am2.common.trackers.PlayerTracker;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.NPCSpells;
import am2.common.world.AM2WorldDecorator;
import am2.network.AMNetworkHandler;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import static am2.common.defs.IDDefs.*;


public class CommonProxy implements IGuiHandler {

    public ParticleManagerServer particleManager;
    protected ServerTickHandler serverTickHandler;
    private HashMap<EntityLivingBase, ArrayList<PotionEffect>> deferredPotionEffects = new HashMap<>();
    private HashMap<EntityLivingBase, Integer> deferredDimensionTransfers = new HashMap<>();
    public KeystoneLocatons blocks;
    private int totalFlickerCount = 0;

    public PlayerTracker playerTracker;
    public ItemFrameWatcher itemFrameWatcher;
    private AM2WorldDecorator worldGen;
    public NBTTagCompound cwCopyLoc = null;

    public static HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> powerPathVisuals;

    public CommonProxy() {
        playerTracker = new PlayerTracker();
        itemFrameWatcher = new ItemFrameWatcher();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        switch (ID) {
            case GUI_OCCULUS:
                return null;
            case GUI_SPELL_CUSTOMIZATION:
                return new ContainerSpellCustomization(player);
            case GUI_RIFT:
                return new ContainerRiftStorage(player, RiftStorage.For(player));
            case GUI_SPELL_BOOK:
                ItemStack bookStack = ItemSpellBook.findSpellBook(player);
                if (bookStack.isEmpty()) {
                    return null;
                }
                ItemSpellBook item = (ItemSpellBook) bookStack.getItem();
                boolean fromBaubles = ItemSpellBook.isSpellBookInBaubles(player);
                return new ContainerSpellBook(player.inventory, bookStack, item.getInventory(bookStack), fromBaubles);
            case GUI_OBELISK:
                return new ContainerObelisk((TileEntityObelisk) world.getTileEntity(new BlockPos(x, y, z)), player);
            case GUI_CRYSTAL_MARKER:
                return new ContainerCrystalMarker(player, (TileEntityCrystalMarker) te);
            case GUI_INSCRIPTION_TABLE:
                return new ContainerInscriptionTable((TileEntityInscriptionTable) world.getTileEntity(new BlockPos(x, y, z)), player.inventory);
            case GUI_ARMOR_INFUSION:
                return new ContainerArmorInfuser(player, (TileEntityArmorImbuer) world.getTileEntity(new BlockPos(x, y, z)));
            case GUI_KEYSTONE:
                ItemStack keystoneStack = player.getHeldItemMainhand();
                if (keystoneStack.isEmpty() || !(keystoneStack.getItem() instanceof ItemKeystone)) {
                    return null;
                }
                ItemKeystone keystone = (ItemKeystone) keystoneStack.getItem();

                int runeBagSlot = InventoryUtilities.getInventorySlotIndexFor(player.inventory, AMItems.rune_bag);
                ItemStack runeBag = ItemStack.EMPTY;
                if (runeBagSlot > -1)
                    runeBag = player.inventory.getStackInSlot(runeBagSlot);

                return new ContainerKeystone(player.inventory, player.getHeldItemMainhand(), runeBag, keystone.getInventory(keystoneStack),
                        ((ItemRuneBag) AMItems.rune_bag).getInventory(runeBag), runeBagSlot);

            case GUI_KEYSTONE_LOCKABLE:
                return new ContainerKeystoneLockable(player.inventory, (IKeystoneLockable<?>) te);
            case GUI_SPELL_SEALED_DOOR:
                return new ContainerSpellSealedDoor(player.inventory, (TileEntitySpellSealedDoor) te);
            case GUI_KEYSTONE_CHEST:
                return ((TileEntityKeystoneChest) te).createContainer(player.inventory, player);
            case GUI_RUNE_BAG:
                ItemStack bagStack = player.getHeldItemMainhand();
                if (bagStack.getItem() == null || !(bagStack.getItem() instanceof ItemRuneBag)) {
                    return null;
                }
                ItemRuneBag runebag = (ItemRuneBag) bagStack.getItem();
                return new ContainerRuneBag(player.inventory, player.getHeldItemMainhand(), runebag.getInventory(bagStack));
            case GUI_FLICKER_HABITAT:
                return new ContainerFlickerHabitat(player, (TileEntityFlickerHabitat) te);
            case GUI_ARCANE_DECONSTRUCTOR:
                return new ContainerArcaneDeconstructor(player.inventory, (TileEntityArcaneDeconstructor) te);
            case GUI_ARCANE_RECONSTRUCTOR:
                return new ContainerArcaneReconstructor(player.inventory, (TileEntityArcaneReconstructor) te);
            case GUI_ASTRAL_BARRIER:
                return new ContainerAstralBarrier(player.inventory, (TileEntityAstralBarrier) te);
            case GUI_ESSENCE_REFINER:
                return new ContainerEssenceRefiner(player.inventory, (TileEntityEssenceRefiner) te);
            case GUI_MAGICIANS_WORKBENCH:
                return new ContainerMagiciansWorkbench(player.inventory, (TileEntityMagiciansWorkbench) te);
            case GUI_CALEFACTOR:
                return new ContainerCalefactor(player, (TileEntityCalefactor) te);
            case GUI_SEER_STONE:
                return new ContainerSeerStone(player.inventory, (TileEntitySeerStone) te);
            case GUI_INERT_SPAWNER:
                return new ContainerInertSpawner(player, (TileEntityInertSpawner) te);
            case GUI_SUMMONER:
                return new ContainerSummoner(player.inventory, (TileEntitySummoner) te);
            case GUI_ESSENCE_BAG:
                bagStack = player.getHeldItemMainhand();
                if (bagStack.getItem() == null || !(bagStack.getItem() instanceof ItemEssenceBag)) {
                    return null;
                }
                ItemEssenceBag essenceBag = (ItemEssenceBag) bagStack.getItem();
                return new ContainerEssenceBag(player.inventory, player.getHeldItemMainhand(), essenceBag.getInventory(bagStack));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void preInit() {

        ForgeChunkManager.setForcedChunkLoadingCallback(ArsMagica.instance, AMChunkLoader.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(ArsMagica.instance, this);
        SeventhSanctum.instance.init();

        initHandlers();
        ArsMagica.config.init();
        serverTickHandler = new ServerTickHandler();
        AMNetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(serverTickHandler);
        MinecraftForge.EVENT_BUS.register(new CompendiumUnlockHandler());
        MinecraftForge.EVENT_BUS.register(new EntityHandler());
        MinecraftForge.EVENT_BUS.register(new PotionEffectHandler());
        MinecraftForge.EVENT_BUS.register(new AffinityAbilityHelper());
        MinecraftForge.EVENT_BUS.register(PowerNodeCache.instance);
        MinecraftForge.EVENT_BUS.register(new ArmorEventHandler());
        MinecraftForge.EVENT_BUS.register(playerTracker);
        MinecraftForge.EVENT_BUS.register(new FlickerEvents());
        MinecraftForge.EVENT_BUS.register(new ShrinkHandler());
        MinecraftForge.EVENT_BUS.register(new EventManager());

        registerInfusions();


        worldGen = new AM2WorldDecorator();
        GameRegistry.registerWorldGenerator(worldGen, 0);

        AMSounds.registerSounds();
        GameRegistry.registerFuelHandler(new FuelHandler());
        AMEntities.instance.initializeSpawns();
        SkillTrees.init();

        blocks = new KeystoneLocatons();

        CapabilityManager.INSTANCE.register(IEntityExtension.class, new IEntityExtension.Storage(), new IEntityExtension.Factory());
        CapabilityManager.INSTANCE.register(IAffinityData.class, new IAffinityData.Storage(), new IAffinityData.Factory());
        CapabilityManager.INSTANCE.register(ISkillData.class, new ISkillData.Storage(), new ISkillData.Factory());
        CapabilityManager.INSTANCE.register(IRiftStorage.class, new IRiftStorage.Storage(), new IRiftStorage.Factory());
        CapabilityManager.INSTANCE.register(IArcaneCompendium.class, new IArcaneCompendium.Storage(), new IArcaneCompendium.Factory());
        CapabilityManager.INSTANCE.register(ISpellCaster.class, new ISpellCaster.Storage(), () -> null);
        EBWizardryCompatBootstrap.registerBookshelfPreInit();
    }

    public void init() {
        NPCSpells.init();
        EBWizardryCompatBootstrap.registerBookshelfInit();
    }

    public void postInit() {
        MinecraftForge.EVENT_BUS.register(playerTracker);
        MinecraftForge.EVENT_BUS.register(new SpellUnlockManager());
        AMSounds.createSoundMaps();
        Affinities.postInit();
        FlickerGenerationPool.INSTANCE.loadFromConfig();
        CraftingAltarMaterials.registerConfigCaps(ArsMagica.config.getExtraAltarCaps());
        CraftingAltarMaterials.registerConfigMain(ArsMagica.config.getExtraAltarMain());
        AMRecipes.addRecipes();
        AMSmeltingRecipes.register();
        for (SpellPart part : ArsMagicaAPI.getSpellRegistry().getValues()) {
            if (part == AMSpells.missing_shape) continue;
            if (ArsMagicaAPI.getSkillRegistry().getValue(part.getRegistryName()) == null)
                throw new IllegalStateException("Spell Part " + part.getRegistryName() + " is missing a skill, this would cause severe problems");
        }
        ArsMagica.disabledSkills.getDisabledSkills(true);
        EBWizardryCompatBootstrap.register();
        AncientSpellcraftCompatBootstrap.register();
        PotioncoreCompatBootstrap.register();
    }

    public void initHandlers() {
        particleManager = new ParticleManagerServer();
    }

    public void showCompendiumToast(String entryId) {
        // no-op server-side
    }

    public void addDeferredTargetSet(EntityLiving ent, EntityLivingBase target) {
        serverTickHandler.addDeferredTarget(ent, target);
    }

    public ImmutableMap<EntityLivingBase, ArrayList<PotionEffect>> getDeferredPotionEffects() {
        return ImmutableMap.copyOf(deferredPotionEffects);
    }

    public void clearDeferredPotionEffects() {
        deferredPotionEffects.clear();
    }

    public void clearDeferredDimensionTransfers() {
        deferredDimensionTransfers.clear();
    }

    public ImmutableMap<EntityLivingBase, Integer> getDeferredDimensionTransfers() {
        return ImmutableMap.copyOf(deferredDimensionTransfers);
    }

    public void renderGameOverlay() {
    }

    public void addDeferredDimensionTransfer(EntityLivingBase ent, int dimension) {
        deferredDimensionTransfers.put(ent, dimension);
    }

    public boolean setMouseDWheel(int dwheel) {
        return false;
    }

    public void setTrackedPowerCompound(NBTTagCompound compound) {
    }

    public void setTrackedLocation(AMVector3 location) {
    }

    public boolean hasTrackedLocationSynced() {
        return false;
    }

    public PowerNodeEntry getTrackedData() {
        return null;
    }

    public void drawPowerOnBlockHighlight(EntityPlayer player, RayTraceResult target, float partialTicks) {
    }

    public void receivePowerPathVisuals(HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> nodePaths) {
    }

    public void requestPowerPathVisuals(IPowerNode<?> node, EntityPlayerMP player) {
        // Noop
    }

    public HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> getPowerPathVisuals() {
        return null;
    }

    public void blackoutArmorPiece(EntityPlayerMP player, EntityEquipmentSlot slot, int cooldown) {
        serverTickHandler.blackoutArmorPiece(player, slot, cooldown);
    }

    public void registerInfusions() {
        ImbuementRegistry.registerAll();
    }

    public void flashManaBar() {
    }

    public void incrementFlickerCount() {
        this.totalFlickerCount++;
    }

    public void decrementFlickerCount() {
        this.totalFlickerCount--;
        if (this.totalFlickerCount < 0)
            this.totalFlickerCount = 0;
    }

    public int getTotalFlickerCount() {
        return this.totalFlickerCount;
    }

    public EntityPlayer getLocalPlayer() {
        return null;
    }

    public void openParticleBlockGUI(World worldIn, EntityPlayer playerIn, TileEntityParticleEmitter te) {
    }

    public void addDigParticle(World world, BlockPos pos, IBlockState state) {
    }

    public static void initOreDict() {
        OreDictionary.registerOre("oreBlueTopaz", new ItemStack(AMBlocks.blue_topaz_ore));
        OreDictionary.registerOre("oreVinteum", new ItemStack(AMBlocks.vinteum_ore));
        OreDictionary.registerOre("oreChimerite", new ItemStack(AMBlocks.chimerite_ore));
        OreDictionary.registerOre("oreMoonstone", new ItemStack(AMBlocks.moonstone_ore));
        OreDictionary.registerOre("oreSunstone", new ItemStack(AMBlocks.sunstone_ore));

        OreDictionary.registerOre("blockBlueTopaz", new ItemStack(AMBlocks.blue_topaz_block));
        OreDictionary.registerOre("blockVinteum", new ItemStack(AMBlocks.vinteum_block));
        OreDictionary.registerOre("blockChimerite", new ItemStack(AMBlocks.chimerite_block));
        OreDictionary.registerOre("blockMoonstone", new ItemStack(AMBlocks.moonstone_block));
        OreDictionary.registerOre("blockSunstone", new ItemStack(AMBlocks.sunstone_block));

        OreDictionary.registerOre("chestWood", new ItemStack(Blocks.CHEST));
        OreDictionary.registerOre("craftingTableWood", new ItemStack(Blocks.CRAFTING_TABLE));

        OreDictionary.registerOre("dustVinteum", new ItemStack(AMItems.vinteum_dust));
        OreDictionary.registerOre("arcaneAsh", new ItemStack(AMItems.arcane_ash));
        OreDictionary.registerOre("gemBlueTopaz", new ItemStack(AMItems.blue_topaz));
        OreDictionary.registerOre("gemChimerite", new ItemStack(AMItems.chimerite));
        OreDictionary.registerOre("gemMoonstone", new ItemStack(AMItems.moonstone));
        OreDictionary.registerOre("gemSunstone", new ItemStack(AMItems.sunstone));
        OreDictionary.registerOre("tallow", new ItemStack(AMItems.animal_fat));
        OreDictionary.registerOre("fat", new ItemStack(AMItems.animal_fat));

        OreDictionary.registerOre("logWood", new ItemStack(AMBlocks.witchwood_log));
        OreDictionary.registerOre("plankWood", new ItemStack(AMBlocks.witchwood_planks));
        OreDictionary.registerOre("slabWood", new ItemStack(AMBlocks.witchwood_slab));
        OreDictionary.registerOre("stairWood", new ItemStack(AMBlocks.witchwood_stairs));
        OreDictionary.registerOre("treeLeaves", new ItemStack(AMBlocks.witchwood_leaves));
        OreDictionary.registerOre("treeSapling", new ItemStack(AMBlocks.witchwood_sapling));

    }

    /**
     * Translates the given key with no specified style. Client-side only; on the server this simply returns
     * the given translation key. Useful whenever translation from common classes is required, e.g. item tooltips.
     *
     * @param key  The unlocalised name to be translated.
     * @param args The format arguments to pass into the translation, if any.
     * @return The resulting translated text.
     */
    public String translate(String key, Object... args) {
        return translate(key, new Style(), args);
    }

    /**
     * Translates the given key and formats it with the given style. Client-side only; on the server this simply returns
     * the given translation key. Useful whenever translation from common classes is required, e.g. item tooltips.
     *
     * @param key   The unlocalised name to be translated.
     * @param style The {@link Style} to use for the displayed text.
     * @param args  The format arguments to pass into the translation, if any.
     * @return The resulting translated text.
     */
    public String translate(String key, Style style, Object... args) {
        return key;
    }


}
