package am2.client;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.ISpellCaster;
import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.api.spell.SpellPart;
import am2.client.blocks.render.*;
import am2.client.commands.ConfigureAMUICommand;
import am2.client.gui.*;
import am2.client.handlers.ClientTickHandler;
import am2.client.items.colorizers.*;
import am2.client.blocks.colorizers.*;
import am2.client.models.ArsMagicaModelLoader;
import am2.client.models.CullfaceModelLoader;
import am2.client.models.SpecialRenderModelLoader;
import am2.client.particles.AMParticleIcons;
import am2.client.particles.ParticleManagerClient;
import am2.client.texture.SpellIconManager;
import am2.client.utils.ItemRenderer;
import am2.common.CommonProxy;
import am2.common.armor.ArmorHelper;
import am2.common.blocks.tileentity.*;
import am2.common.defs.Keybindings;
import am2.common.extensions.RiftStorage;
import am2.common.handler.BakingHandler;
import am2.common.items.*;
import am2.common.power.PowerNodeEntry;
import am2.common.power.PowerTypes;
import am2.common.registry.*;
import am2.common.spell.SpellCaster;
import am2.common.spell.component.Telekinesis;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.RenderUtils;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketRequestPowerPaths;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.SystemToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.Style;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static am2.common.defs.IDDefs.*;

public class ClientProxy extends CommonProxy {

    public ClientTickHandler clientTickHandler;

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        switch (ID) {
            case GUI_OCCULUS:
                return new GuiOcculus(player);
            case GUI_SPELL_CUSTOMIZATION:
                return new GuiSpellCustomization(player);
            case GUI_RIFT:
                return new GuiRiftStorage(player, RiftStorage.For(player));
            case GUI_SPELL_BOOK:
                ItemStack bookStack = ItemSpellBook.findSpellBook(player);
                if (bookStack.isEmpty()) {
                    return null;
                }
                ItemSpellBook item = (ItemSpellBook) bookStack.getItem();
                boolean fromBaubles = ItemSpellBook.isSpellBookInBaubles(player);
                return new GuiSpellBook(player.inventory, bookStack, item.getInventory(bookStack), fromBaubles);
            case GUI_OBELISK:
                return new GuiObelisk((TileEntityObelisk) world.getTileEntity(new BlockPos(x, y, z)), player);
            case GUI_CRYSTAL_MARKER:
                return new GuiCrystalMarker(player, (TileEntityCrystalMarker) te);
            case GUI_INSCRIPTION_TABLE:
                return new GuiInscriptionTable(player.inventory, (TileEntityInscriptionTable) world.getTileEntity(new BlockPos(x, y, z)));
            case GUI_ARMOR_INFUSION:
                return new GuiArmorImbuer(player, (TileEntityArmorImbuer) world.getTileEntity(new BlockPos(x, y, z)));
            case GUI_KEYSTONE:
                ItemStack keystoneStack = player.getHeldItemMainhand();
                if (!(keystoneStack.getItem() instanceof ItemKeystone)) {
                    return null;
                }
                ItemKeystone keystone = (ItemKeystone) keystoneStack.getItem();

                int runeBagSlot = InventoryUtilities.getInventorySlotIndexFor(player.inventory, AMItems.rune_bag);
                ItemStack runeBag = ItemStack.EMPTY;
                if (runeBagSlot > -1)
                    runeBag = player.inventory.getStackInSlot(runeBagSlot);

                return new GuiKeystone(player.inventory, player.getHeldItemMainhand(), runeBag, keystone.getInventory(keystoneStack), runeBag == null
                        ? null : ((ItemRuneBag) AMItems.rune_bag).getInventory(runeBag), runeBagSlot);

            case GUI_KEYSTONE_LOCKABLE:
                if (!(te instanceof IKeystoneLockable)) {
                    return null;
                }
                return new GuiKeystoneLockable(player.inventory, (IKeystoneLockable<?>) te);
            case GUI_SPELL_SEALED_DOOR:
                return new GuiSpellSealedDoor(player.inventory, (TileEntitySpellSealedDoor) te);
            case GUI_KEYSTONE_CHEST:
                return new GuiKeystoneChest(player.inventory, (TileEntityKeystoneChest) te);
            case GUI_RUNE_BAG:
                ItemStack bagStack = player.getHeldItemMainhand();
                if (bagStack.getItem() == null || !(bagStack.getItem() instanceof ItemRuneBag)) {
                    return null;
                }
                ItemRuneBag runebag = (ItemRuneBag) bagStack.getItem();
                return new GuiRuneBag(player.inventory, player.getHeldItemMainhand(), runebag.getInventory(bagStack));
            case GUI_FLICKER_HABITAT:
                return new GuiFlickerHabitat(player, (TileEntityFlickerHabitat) te);
            case GUI_ARCANE_DECONSTRUCTOR:
                return new GuiArcaneDeconstructor(player.inventory, (TileEntityArcaneDeconstructor) te);
            case GUI_ARCANE_RECONSTRUCTOR:
                return new GuiArcaneReconstructor(player.inventory, (TileEntityArcaneReconstructor) te);
            case GUI_ASTRAL_BARRIER:
                return new GuiAstralBarrier(player.inventory, (TileEntityAstralBarrier) te);
            case GUI_ESSENCE_REFINER:
                return new GuiEssenceRefiner(player.inventory, (TileEntityEssenceRefiner) te);
            case GUI_MAGICIANS_WORKBENCH:
                return new GuiMagiciansWorkbench(player.inventory, (TileEntityMagiciansWorkbench) te);
            case GUI_CALEFACTOR:
                return new GuiCalefactor(player, (TileEntityCalefactor) te);
            case GUI_SEER_STONE:
                return new GuiSeerStone(player.inventory, (TileEntitySeerStone) te);
            case GUI_INERT_SPAWNER:
                return new GuiInertSpawner(player, (TileEntityInertSpawner) te);
            case GUI_SUMMONER:
                return new GuiSummoner(player.inventory, (TileEntitySummoner) te);
            case GUI_ESSENCE_BAG:
                bagStack = player.getHeldItemMainhand();
                if (!(bagStack.getItem() instanceof ItemEssenceBag)) {
                    return null;
                }
                ItemEssenceBag essenceBag = (ItemEssenceBag) bagStack.getItem();
                return new GuiEssenceBag(player.inventory, player.getHeldItemMainhand(), ItemEssenceBag.getInventory(bagStack));
        }
        return super.getClientGuiElement(ID, player, world, x, y, z);
    }

    private void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCraftingAltar.class, new TileCraftingAltarRenderer());
        // Obelisk uses JSON model for base, TESR for animated runes overlay when active
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityObelisk.class, new TileObeliskRenderer());
        // Celestial Prism uses OBJ model rendered via TESR
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCelestialPrism.class, new TileCelestialPrismRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlackAurem.class, new TileBlackAuremRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLectern.class, new TileLecternRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeystoneReceptacle.class, new TileKeystoneReceptacleRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCrystalMarker.class, new TileCrystalMarkerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeystoneChest.class, new TileKeystoneChestRenderer());
        // Essence Conduit uses JSON model now, no TESR needed
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGroundRuneSpell.class, new TileRuneRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChalkArrow.class, new TileChalkArrowRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEverstone.class, new TileEverstoneRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneReconstructor.class, new TileArcaneReconstructorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneDeconstructor.class, new TileArcaneDeconstructorRenderer());
        // Illusion block now uses IExtendedBlockState + IBakedModel — no TESR needed
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySeerStone.class, new TileSeerStoneRenderer());
        // Calefactor uses JSON model for base, TESR for item/particles
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCalefactor.class, new TileCalefactorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySummoner.class, new TileSummonerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAstralBarrier.class, new TileAstralBarrierRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOtherworldAura.class, new TileOtherworldAuraRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityOcculus.class, new TileOcculusRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPhaseShift.class, new TilePhaseShiftRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityFlickerHabitat.class, new TileFlickerHabitatRenderer());


        ModelLoaderRegistry.registerLoader(new ArsMagicaModelLoader());
        ModelLoaderRegistry.registerLoader(new CullfaceModelLoader());
        ModelLoaderRegistry.registerLoader(new SpecialRenderModelLoader());

        MinecraftForge.EVENT_BUS.register(new ArsMagicaModelLoader());
        MinecraftForge.EVENT_BUS.register(clientTickHandler);
        MinecraftForge.EVENT_BUS.register(ItemRenderer.instance);
        MinecraftForge.EVENT_BUS.register(new BakingHandler());
        MinecraftForge.EVENT_BUS.register(new Keybindings());
        MinecraftForge.EVENT_BUS.register(new AMIngameGUI());

        ArsMagica.config.clientInit();
//		new AMSounds();
        AMEntities.instance.registerRenderers();
//		EntityManager.instance.registerRenderers();
//		blocks.preInitClient();

        ClientCommandHandler.instance.registerCommand(new ConfigureAMUICommand());
    }


    @Override
    public void preInit() {
        super.preInit();

        OBJLoader.INSTANCE.addDomain(ArsMagica.MODID);

        AMParticleIcons.instance.toString();
        SpellIconManager.INSTANCE.toString();

        ClientRegistry.registerKeyBinding(Keybindings.ICE_BRIDGE);
        ClientRegistry.registerKeyBinding(Keybindings.ENDER_TP);
        //ClientRegistry.registerKeyBinding(Keybindings.AURA_CUSTOMIZATION);
        ClientRegistry.registerKeyBinding(Keybindings.SHAPE_GROUP);
        ClientRegistry.registerKeyBinding(Keybindings.NIGHT_VISION);
        ClientRegistry.registerKeyBinding(Keybindings.SPELL_BOOK_NEXT);
        ClientRegistry.registerKeyBinding(Keybindings.SPELL_BOOK_PREV);
        ClientRegistry.registerKeyBinding(Keybindings.CHARM_CAST);

        registerRenderers();
    }

    @Override
    public void initHandlers() {
        particleManager = new ParticleManagerClient();
        clientTickHandler = new ClientTickHandler();
    }

    @Override
    public void postInit() {
        super.postInit();
        CompendiumRegistry.postInit();
    }

    @Override
    public void init() {
        super.init();
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        itemColors.registerItemColorHandler(new CrystalPhylacteryColorizer(), AMItems.crystal_phylactery);
        itemColors.registerItemColorHandler(new SpellBookColorizer(), AMItems.spellbook);
        itemColors.registerItemColorHandler(new FlickerJarColorizer(), AMItems.flicker_jar);
        itemColors.registerItemColorHandler(new ChalkArrowItemColorizer(), AMItems.colored_chalk);
        //Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new LostJournalColorizer(), AMItems.lost_journal);
        itemColors.registerItemColorHandler(new ManaBatteryItemColorizer(), AMBlocks.mana_battery);

        blockColors.registerBlockColorHandler(new ManaBatteryBlockColorizer(), AMBlocks.mana_battery);
        blockColors.registerBlockColorHandler(new CrystalMarkerColorizer(), AMBlocks.crystal_marker);
        blockColors.registerBlockColorHandler(new ChalkArrowBlockColorizer(), AMBlocks.chalk_arrow);
        blockColors.registerBlockColorHandler(new MonoColorizer(0xFFFFFF), AMBlocks.witchwood_leaves);
        // Delegate illusion block tinting to the mimic block so tinted blocks (grass, leaves, etc.) show correct biome colour
        blockColors.registerBlockColorHandler(
            (state, worldIn, pos, tintIndex) -> {
                if (worldIn != null && pos != null) {
                    net.minecraft.tileentity.TileEntity te = worldIn.getTileEntity(pos);
                    if (te instanceof am2.common.blocks.tileentity.TileEntityIllusionBlock) {
                        IBlockState mimic = ((am2.common.blocks.tileentity.TileEntityIllusionBlock) te).getMimicBlock();
                        if (mimic != null && mimic.getBlock() != net.minecraft.init.Blocks.AIR) {
                            return blockColors.colorMultiplier(mimic, worldIn, pos, tintIndex);
                        }
                    }
                }
                return -1;
            },
            AMBlocks.illusion_block
        );
        ArmorColorizer armorColorizer = new ArmorColorizer();
        itemColors.registerItemColorHandler(armorColorizer,
                AMItems.mage_hood, AMItems.mage_robe, AMItems.mage_leggings, AMItems.mage_boots,
                AMItems.battlemage_helmet, AMItems.battlemage_chestplate, AMItems.battlemage_leggings, AMItems.battlemage_boots);
    }

    @Override
    public void setTrackedLocation(AMVector3 location) {
        clientTickHandler.setTrackLocation(location.toVec3D());
    }

    @Override
    public void setTrackedPowerCompound(NBTTagCompound compound) {
        clientTickHandler.setTrackData(compound);
    }

    @Override
    public boolean hasTrackedLocationSynced() {
        return clientTickHandler.getHasSynced();
    }

    @Override
    public PowerNodeEntry getTrackedData() {
        return clientTickHandler.getTrackData();
    }

    @Override
    public boolean setMouseDWheel(int dwheel) {
        if (dwheel == 0) return false;

        ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if (stack == ItemStack.EMPTY) return false;

        boolean store = checkForTKMove(stack);
        if (!store && stack.getItem() instanceof ItemSpellBook) {
            store = Minecraft.getMinecraft().player.isSneaking();
        }

        if (store) {
            clientTickHandler.setDWheel(dwheel / 120, Minecraft.getMinecraft().player.inventory.currentItem, Minecraft.getMinecraft().player.isHandActive());
            return true;
        } else {
            clientTickHandler.setDWheel(0, -1, false);
        }
        return false;
    }

    private boolean checkForTKMove(ItemStack stack) {
        if (stack.getItem() instanceof ItemSpellBook) {
            ItemStack activeStack = ((ItemSpellBook) stack.getItem()).getActiveItemStack(stack);
            if (!activeStack.isEmpty())
                stack = activeStack;
        }
        if (stack.getItem() instanceof ItemSpellBase && stack.hasCapability(SpellCaster.INSTANCE, null) && Minecraft.getMinecraft().player.isHandActive()) {
            ISpellCaster caster = stack.getCapability(SpellCaster.INSTANCE, null);
            assert caster != null;
            for (List<SpellPart> components : caster.getSpellCommon()) {
                for (SpellPart component : components) {
                    if (component instanceof Telekinesis) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void drawPowerOnBlockHighlight(EntityPlayer player, RayTraceResult target, float partialTicks) {

        ItemStack headStack = Minecraft.getMinecraft().player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        boolean hasGoggles = !headStack.isEmpty() &&
                (headStack.getItem() == AMItems.magitech_goggles ||
                        ArmorHelper.isInfusionPreset(headStack, ImbuementRegistry.MAGITECH_GOGGLE_INTEGRATION));

        if (hasGoggles) {
            if (target == null || target.getBlockPos() == null)
                return;
            TileEntity te = player.world.getTileEntity(target.getBlockPos());
            if (te != null && te instanceof IPowerNode) {
                ArsMagica.proxy.setTrackedLocation(new AMVector3(target.getBlockPos()));
            } else {
                ArsMagica.proxy.setTrackedLocation(AMVector3.zero());
            }

            if (ArsMagica.proxy.hasTrackedLocationSynced()) {
                PowerNodeEntry data = ArsMagica.proxy.getTrackedData();
                IBlockState state = player.world.getBlockState(target.getBlockPos());
                Block block = state.getBlock();
                float yOff = 0.5f;
                if (data != null) {
                    GlStateManager.pushAttrib();
                    GlStateManager.disableLighting();
                    for (PowerTypes type : ((IPowerNode<?>) te).getValidPowerTypes()) {
                        float pwr = data.getPower(type);
                        float pct = pwr / ((IPowerNode<?>) te).getCapacity() * 100;
                        RenderUtils.drawTextInWorldAtOffset(String.format("%s%.2f (%.2f%%)", type.getChatColor(), pwr, pct),
                                target.getBlockPos().getX() - (player.prevPosX - (player.prevPosX - player.posX) * partialTicks) + 0.5f,
                                target.getBlockPos().getY() + yOff - (player.prevPosY - (player.prevPosY - player.posY) * partialTicks) + block.getBoundingBox(state, player.world, target.getBlockPos()).maxY * 0.8f,
                                target.getBlockPos().getZ() - (player.prevPosZ - (player.prevPosZ - player.posZ) * partialTicks) + 0.5f,
                                0xFFFFFF);
                        yOff += 0.12f;
                    }
                    GlStateManager.enableLighting();
                    GlStateManager.popAttrib();
                }
            }
        }
    }

    @Override
    public void requestPowerPathVisuals(IPowerNode<?> node, EntityPlayerMP player) {
        TileEntity te = (TileEntity) node;
        AMNetworkHandler.getNetwork().sendToServer(new PacketRequestPowerPaths((byte) 1, (float) te.getPos().getX(), (float) te.getPos().getY(), (float) te.getPos().getZ()));
    }

    @Override
    public void flashManaBar() {
        AMGuiHelper.instance.flashManaBar();
    }

    @Override
    public void receivePowerPathVisuals(HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> paths) {
        powerPathVisuals = paths;
    }

    @Override
    public HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> getPowerPathVisuals() {
        return powerPathVisuals;
    }

    @Override
    public EntityPlayer getLocalPlayer() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void openParticleBlockGUI(World world, EntityPlayer player, TileEntityParticleEmitter te) {
        if (world.isRemote) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiParticleEmitter(te));
        }
    }

    @Override
    public void addDigParticle(World world, BlockPos pos, IBlockState state) {
        Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(
                EnumParticleTypes.BLOCK_CRACK.getParticleID(),
                pos.getX() + world.rand.nextDouble(),
                pos.getY() + world.rand.nextDouble(),
                pos.getZ() + world.rand.nextDouble(),
                0, 0, 0,
                Block.getStateId(state)
        );
    }

    @Override
    public String translate(String key, Style style, Object... args) {
        return style.getFormattingCode() + I18n.format(key, args);
    }

    @Override
    public void showCompendiumToast(String entryId) {
        Minecraft mc = Minecraft.getMinecraft();
        // Look up the entry display name
        am2.api.compendium.CompendiumEntry entry = am2.api.compendium.CompendiumCategory.getEntryByID(entryId);
        if (entry == null) {
            // Try matching by bare id, normalizing away underscores so camelCase ("unlockingPowers")
            // matches snake_case XML ids ("unlocking_powers"), and vice versa.
            String simpleName = entryId.contains(".") ? entryId.substring(entryId.lastIndexOf('.') + 1) : entryId;
            String normalizedName = simpleName.replace("_", "").toLowerCase();
            for (am2.api.compendium.CompendiumEntry e : am2.api.compendium.CompendiumCategory.getAllEntries()) {
                String simpleEntry = e.getID().contains(".") ? e.getID().substring(e.getID().lastIndexOf('.') + 1) : e.getID();
                if (simpleEntry.replace("_", "").toLowerCase().equals(normalizedName)) {
                    entry = e;
                    break;
                }
            }
        }
        if (entry == null) return; // No displayable compendium entry for this ID — skip the toast
        SystemToast.addOrUpdate(
                mc.getToastGui(),
                SystemToast.Type.TUTORIAL_HINT,
                new net.minecraft.util.text.TextComponentTranslation("advancement.arsmagica2.compendium_data.title"),
                new net.minecraft.util.text.TextComponentString(entry.getName())
        );
    }
}
