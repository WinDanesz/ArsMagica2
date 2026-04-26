package am2.network.packets;

import am2.common.extensions.EntityExtension;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to sync telekinesis distance from client to server.
 * Sent when the player adjusts their TK distance on the client.
 * <p>
 * Direction: Client -> Server
 */
public class PacketTKDistanceSync extends AMPacket<PacketTKDistanceSync> {

    private float distance;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketTKDistanceSync() {
    }

    /**
     * Create a new TK distance sync packet.
     *
     * @param distance The TK distance to sync
     */
    public PacketTKDistanceSync(float distance) {
        this.distance = distance;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(distance);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        distance = buf.readFloat();
    }

    @Override
    protected void handleServerSide(PacketTKDistanceSync message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate distance is within reasonable bounds to prevent exploits
            if (Float.isFinite(message.distance) && message.distance >= 0.0F && message.distance <= 1000.0F) {
                EntityExtension.For(player).setTKDistance(message.distance);
            }
        }
    }
}
