package am2.common.blocks.tileentity;

public interface ITileEntityPacketSync {
    /**
     * Create the synchronization packet
     */
    byte[] createSyncPacket();

    /**
     * Handles the synchronization data
     */
    boolean handleSyncPacket(byte[] packet);

    /**
     * Should the packet be sent ?
     */
    boolean shouldSync();

    /**
     * Confirms that the packet has been received client-side
     */
    void confirm();
}
