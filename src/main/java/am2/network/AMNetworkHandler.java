package am2.network;

import am2.common.network.packets.MessageTEUpdate;
import am2.network.packets.*;
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
        registerPacket(PacketTKDistanceSync.class, PacketTKDistanceSync.class, Side.SERVER);
        registerPacket(PacketAbilityToggle.class, PacketAbilityToggle.class, Side.SERVER);
        registerPacket(PacketPlayerFlip.class, PacketPlayerFlip.class, Side.SERVER);
        registerPacket(PacketOcculusUnlock.class, PacketOcculusUnlock.class, Side.SERVER);
        registerPacket(PacketDisciplineLevelUp.class, PacketDisciplineLevelUp.class, Side.SERVER);
        registerPacket(PacketKeyAbilityPress.class, PacketKeyAbilityPress.class, Side.SERVER);

        // Spell system
        registerPacket(PacketSpellCustomize.class, PacketSpellCustomize.class, Side.SERVER);
        registerPacket(PacketSpellbookSlotChange.class, PacketSpellbookSlotChange.class, Side.SERVER);
        registerPacket(PacketSpellShapeGroupChange.class, PacketSpellShapeGroupChange.class, Side.SERVER);
        registerPacket(PacketCharmCast.class, PacketCharmCast.class, Side.SERVER);

        // Keystone and armor
        registerPacket(PacketSaveKeystoneCombo.class, PacketSaveKeystoneCombo.class, Side.SERVER);
        registerPacket(PacketSetKeystoneCombo.class, PacketSetKeystoneCombo.class, Side.SERVER);
        registerPacket(PacketImbueArmor.class, PacketImbueArmor.class, Side.SERVER);

        // Workbench
        registerPacket(PacketWorkbenchLockRecipe.class, PacketWorkbenchLockRecipe.class, Side.SERVER);
        registerPacket(PacketWorkbenchSetRecipe.class, PacketWorkbenchSetRecipe.class, Side.SERVER);

        // TileEntity updates (server-bound)
        registerPacket(PacketInscriptionTableUpdate.class, PacketInscriptionTableUpdate.class, Side.SERVER);
        registerPacket(PacketParticleEmitterUpdate.class, PacketParticleEmitterUpdate.class, Side.SERVER);

        // Power paths (server-bound)
        registerPacket(PacketRequestPowerPaths.class, PacketRequestPowerPaths.class, Side.SERVER);

        // Client-bound packets (Server -> Client)
        // Player sync
        registerPacket(PacketVelocityAdd.class, PacketVelocityAdd.class, Side.CLIENT);
        registerPacket(PacketPlayerLogin.class, PacketPlayerLogin.class, Side.CLIENT);
        registerPacket(PacketSyncWorldName.class, PacketSyncWorldName.class, Side.CLIENT);

        // Data sync
        registerPacket(PacketSyncAffinityData.class, PacketSyncAffinityData.class, Side.CLIENT);
        registerPacket(PacketSyncSkillData.class, PacketSyncSkillData.class, Side.CLIENT);
        registerPacket(PacketSyncCompendium.class, PacketSyncCompendium.class, Side.CLIENT);
        registerPacket(PacketSyncExtendedProps.class, PacketSyncExtendedProps.class, Side.CLIENT);

        // Visual effects
        registerPacket(PacketFlashArmorPiece.class, PacketFlashArmorPiece.class, Side.CLIENT);

        // TileEntity sync (client-bound)
        registerPacket(PacketCraftingAltarSync.class, PacketCraftingAltarSync.class, Side.CLIENT);
        registerPacket(PacketLecternSync.class, PacketLecternSync.class, Side.CLIENT);
        registerPacket(PacketCalefactorSync.class, PacketCalefactorSync.class, Side.CLIENT);
        registerPacket(PacketObeliskSync.class, PacketObeliskSync.class, Side.CLIENT);
        registerPacket(PacketArcaneDeconstructorSync.class, PacketArcaneDeconstructorSync.class, Side.CLIENT);

        // Power and mana (client-bound)
        registerPacket(PacketPowerPathResponse.class, PacketPowerPathResponse.class, Side.CLIENT);
        registerPacket(PacketManaLinkUpdate.class, PacketManaLinkUpdate.class, Side.CLIENT);

        // Boss and visual effects (client-bound)
        registerPacket(PacketEntityActionUpdate.class, PacketEntityActionUpdate.class, Side.CLIENT);
        registerPacket(PacketHecateDeath.class, PacketHecateDeath.class, Side.CLIENT);
        registerPacket(PacketStarFall.class, PacketStarFall.class, Side.CLIENT);

        // Particle effects (client-bound)
        registerPacket(PacketParticleSpawnSpecial.class, PacketParticleSpawnSpecial.class, Side.CLIENT);

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
    private static <REQ extends IMessage, REPLY extends IMessage> void registerPacket(Class<? extends IMessageHandler<REQ, REPLY>> packet, Class<REQ> message, Side side) {
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
