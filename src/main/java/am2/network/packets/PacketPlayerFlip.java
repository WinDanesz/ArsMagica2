package am2.network.packets;

import am2.common.extensions.EntityExtension;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to toggle player flip/inversion state.
 * Sent from client to server when the player changes their inverted state.
 * <p>
 * Direction: Client -> Server
 */
public class PacketPlayerFlip extends AMPacket<PacketPlayerFlip> {

    private boolean inverted;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketPlayerFlip() {
    }

    /**
     * Create a new player flip packet.
     *
     * @param inverted Whether the player should be inverted
     */
    public PacketPlayerFlip(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        inverted = buf.readBoolean();
    }

    @Override
    protected void handleServerSide(PacketPlayerFlip message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            EntityExtension.For(player).setInverted(message.inverted);
        }
    }
}
