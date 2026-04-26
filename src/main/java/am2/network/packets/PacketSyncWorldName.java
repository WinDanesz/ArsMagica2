package am2.network.packets;

import am2.client.handlers.ClientTickHandler;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize the world name to the client.
 * Sent from server to client to update the display name of the current world.
 * <p>
 * Direction: Server -> Client
 */
public class PacketSyncWorldName extends AMPacket<PacketSyncWorldName> {

    private String worldName;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSyncWorldName() {
    }

    /**
     * Create a new world name sync packet.
     *
     * @param worldName The name of the world
     */
    public PacketSyncWorldName(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, worldName);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        worldName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketSyncWorldName message, MessageContext ctx) {
        // Validate world name to prevent issues
        if (message.worldName != null && message.worldName.length() <= 100) {
            ClientTickHandler.worldName = message.worldName;
        }
    }
}
