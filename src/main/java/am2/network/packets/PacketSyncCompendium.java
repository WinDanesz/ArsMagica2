package am2.network.packets;

import am2.common.lore.ArcaneCompendium;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize compendium data to the client.
 * Sent from server to client when compendium unlocks change.
 * <p>
 * Direction: Server -> Client
 */
public class PacketSyncCompendium extends AMPacket<PacketSyncCompendium> {

    private int entityId;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSyncCompendium() {
    }

    /**
     * Create a new compendium sync packet.
     *
     * @param entityId The entity ID (player) whose compendium to sync
     * @param data     The serialized compendium data
     */
    public PacketSyncCompendium(int entityId, byte[] data) {
        this.entityId = entityId;
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        int length = buf.readInt();
        // Validate length to prevent memory issues
        if (length < 0 || length > 1048576) { // Max 1MB
            throw new IllegalArgumentException("Invalid compendium data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketSyncCompendium message, MessageContext ctx) {
        Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity != null && entity instanceof EntityPlayer) {
            ArcaneCompendium.For((EntityPlayer) entity).handleUpdatePacket(message.data);
        }
    }
}
