package am2.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Base class for all ArsMagica2 packets.
 * Follows the clean packet pattern from Electroblob77/Wizardry.
 * <p>
 * Each packet implementation should:
 * 1. Extend AMPacket
 * 2. Implement toBytes() and fromBytes() for serialization
 * 3. Implement handleClientSide() or handleServerSide() depending on the side it's received on
 *
 * @param <REQ> The message type (typically the implementing class itself)
 */
public abstract class AMPacket<REQ extends IMessage> implements IMessage, IMessageHandler<REQ, IMessage> {

    /**
     * Write packet data to the ByteBuf for network transmission.
     * Called on the sending side.
     */
    @Override
    public abstract void toBytes(ByteBuf buf);

    /**
     * Read packet data from the ByteBuf after network transmission.
     * Called on the receiving side.
     */
    @Override
    public abstract void fromBytes(ByteBuf buf);

    /**
     * Handle the packet on the client side.
     * Override this method if the packet is sent to the client.
     *
     * @param message The received message
     * @param ctx     The message context
     */
    protected void handleClientSide(REQ message, MessageContext ctx) {
        // Default: do nothing
    }

    /**
     * Handle the packet on the server side.
     * Override this method if the packet is sent to the server.
     *
     * @param message The received message
     * @param ctx     The message context
     */
    protected void handleServerSide(REQ message, MessageContext ctx) {
        // Default: do nothing
    }

    /**
     * IMessageHandler implementation. Routes the packet to the appropriate side handler.
     * This method schedules the handling on the main thread to avoid concurrency issues.
     */
    @Override
    public IMessage onMessage(REQ message, MessageContext ctx) {
        if (ctx.side.isClient()) {
            // Schedule on client main thread
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> handleClientSide(message, ctx));
        } else {
            // Schedule on server main thread
            // Null checks to handle disconnect edge cases
            if (ctx.getServerHandler() != null && ctx.getServerHandler().player != null) {
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> handleServerSide(message, ctx));
            }
        }
        return null;
    }
}
