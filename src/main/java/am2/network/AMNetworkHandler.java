package am2.network;

import am2.common.network.packets.MessageTEUpdate;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Centralized packet handler for ArsMagica2.
 * Uses Forge's SimpleNetworkWrapper for clean, type-safe packet handling.
 * <p>
 * Replaces the old AMNetHandler and AMTileEntityNetHandler classes.
 */
public class AMNetworkHandler {

    private static SimpleNetworkWrapper INSTANCE;
    private static int packetId = 0;

    /**
     * Initialize the network handler and register all packets.
     * Should be called during mod initialization.
     * IMPORTANT: This method should only be called once!
     */
    public static void init() {
        if (INSTANCE != null) {
            throw new IllegalStateException("AMNetworkHandler has already been initialized!");
        }

        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("am2net");

        // Register packets here as they are migrated
        // Format: registerPacket(HandlerClass.class, MessageClass.class, Side.CLIENT/SERVER);

        // Server-bound packets (Client -> Server)
        // Player actions
        registerPacket(am2.network.packets.PacketTKDistanceSync.class,
                am2.network.packets.PacketTKDistanceSync.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketAbilityToggle.class,
                am2.network.packets.PacketAbilityToggle.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketPlayerFlip.class,
                am2.network.packets.PacketPlayerFlip.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketOcculusUnlock.class,
                am2.network.packets.PacketOcculusUnlock.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketDisciplineLevelUp.class,
                am2.network.packets.PacketDisciplineLevelUp.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketKeyAbilityPress.class,
                am2.network.packets.PacketKeyAbilityPress.class, Side.SERVER);

        // Spell system
        registerPacket(am2.network.packets.PacketSpellCustomize.class,
                am2.network.packets.PacketSpellCustomize.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketSpellbookSlotChange.class,
                am2.network.packets.PacketSpellbookSlotChange.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketSpellShapeGroupChange.class,
                am2.network.packets.PacketSpellShapeGroupChange.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketCharmCast.class,
                am2.network.packets.PacketCharmCast.class, Side.SERVER);

        // Keystone and armor
        registerPacket(am2.network.packets.PacketSaveKeystoneCombo.class,
                am2.network.packets.PacketSaveKeystoneCombo.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketSetKeystoneCombo.class,
                am2.network.packets.PacketSetKeystoneCombo.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketImbueArmor.class,
                am2.network.packets.PacketImbueArmor.class, Side.SERVER);

        // Workbench
        registerPacket(am2.network.packets.PacketWorkbenchLockRecipe.class,
                am2.network.packets.PacketWorkbenchLockRecipe.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketWorkbenchSetRecipe.class,
                am2.network.packets.PacketWorkbenchSetRecipe.class, Side.SERVER);

        // TileEntity updates (server-bound)
        registerPacket(am2.network.packets.PacketInscriptionTableUpdate.class,
                am2.network.packets.PacketInscriptionTableUpdate.class, Side.SERVER);
        registerPacket(am2.network.packets.PacketParticleEmitterUpdate.class,
                am2.network.packets.PacketParticleEmitterUpdate.class, Side.SERVER);

        // Power paths (server-bound)
        registerPacket(am2.network.packets.PacketRequestPowerPaths.class,
                am2.network.packets.PacketRequestPowerPaths.class, Side.SERVER);

        // Client-bound packets (Server -> Client)
        // Player sync
        registerPacket(am2.network.packets.PacketVelocityAdd.class,
                am2.network.packets.PacketVelocityAdd.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketPlayerLogin.class,
                am2.network.packets.PacketPlayerLogin.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketSyncWorldName.class,
                am2.network.packets.PacketSyncWorldName.class, Side.CLIENT);

        // Data sync
        registerPacket(am2.network.packets.PacketSyncAffinityData.class,
                am2.network.packets.PacketSyncAffinityData.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketSyncSkillData.class,
                am2.network.packets.PacketSyncSkillData.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketSyncCompendium.class,
                am2.network.packets.PacketSyncCompendium.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketSyncExtendedProps.class,
                am2.network.packets.PacketSyncExtendedProps.class, Side.CLIENT);

        // Visual effects
        registerPacket(am2.network.packets.PacketFlashArmorPiece.class,
                am2.network.packets.PacketFlashArmorPiece.class, Side.CLIENT);

        // TileEntity sync (client-bound)
        registerPacket(am2.network.packets.PacketCraftingAltarSync.class,
                am2.network.packets.PacketCraftingAltarSync.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketLecternSync.class,
                am2.network.packets.PacketLecternSync.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketCalefactorSync.class,
                am2.network.packets.PacketCalefactorSync.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketObeliskSync.class,
                am2.network.packets.PacketObeliskSync.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketArcaneDeconstructorSync.class,
                am2.network.packets.PacketArcaneDeconstructorSync.class, Side.CLIENT);

        // Power and mana (client-bound)
        registerPacket(am2.network.packets.PacketPowerPathResponse.class,
                am2.network.packets.PacketPowerPathResponse.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketManaLinkUpdate.class,
                am2.network.packets.PacketManaLinkUpdate.class, Side.CLIENT);

        // Boss and visual effects (client-bound)
        registerPacket(am2.network.packets.PacketEntityActionUpdate.class,
                am2.network.packets.PacketEntityActionUpdate.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketHecateDeath.class,
                am2.network.packets.PacketHecateDeath.class, Side.CLIENT);
        registerPacket(am2.network.packets.PacketStarFall.class,
                am2.network.packets.PacketStarFall.class, Side.CLIENT);

        // Particle effects (client-bound)
        registerPacket(am2.network.packets.PacketParticleSpawnSpecial.class,
                am2.network.packets.PacketParticleSpawnSpecial.class, Side.CLIENT);

        // Legacy packets (previously on separate channels)
        registerPacket(MessageTEUpdate.MessageHolder.class, MessageTEUpdate.class, Side.CLIENT);
    }

    /**
     * Register a packet to be handled on the specified side.
     *
     * @param packet  The packet handler class (must extend AMPacket)
     * @param message The packet message class
     * @param side    The side to register on (CLIENT or SERVER)
     */
    private static <REQ extends IMessage, REPLY extends IMessage> void registerPacket(
            Class<? extends IMessageHandler<REQ, REPLY>> packet, Class<REQ> message, Side side) {
        if (side == Side.CLIENT) {
            INSTANCE.registerMessage(packet, message, packetId, Side.CLIENT);
        } else if (side == Side.SERVER) {
            INSTANCE.registerMessage(packet, message, packetId, Side.SERVER);
        } else {
            throw new IllegalArgumentException("Side must be either CLIENT or SERVER, got: " + side);
        }
        packetId++;
    }

    /**
     * Get the network instance for sending packets.
     *
     * @throws IllegalStateException if init() has not been called yet
     */
    public static SimpleNetworkWrapper getNetwork() {
        if (INSTANCE == null) {
            throw new IllegalStateException("AMNetworkHandler has not been initialized! Call init() first.");
        }
        return INSTANCE;
    }
}
