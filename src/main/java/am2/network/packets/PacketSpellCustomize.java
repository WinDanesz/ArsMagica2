package am2.network.packets;

import am2.common.container.ContainerSpellCustomization;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to customize spell icon and name.
 * Sent from client to server when the player customizes a spell in the spell customization GUI.
 * <p>
 * Direction: Client -> Server
 */
public class PacketSpellCustomize extends AMPacket<PacketSpellCustomize> {

    private int iconIndex;
    private String name;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSpellCustomize() {
    }

    /**
     * Create a new spell customize packet.
     *
     * @param iconIndex The icon index for the spell
     * @param name      The custom name for the spell
     */
    public PacketSpellCustomize(int iconIndex, String name) {
        this.iconIndex = iconIndex;
        this.name = name;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(iconIndex);
        ByteBufUtils.writeUTF8String(buf, name);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        iconIndex = buf.readInt();
        name = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketSpellCustomize message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate input
            if (message.name != null && message.name.length() <= 50 &&
                    message.iconIndex >= 0 && message.iconIndex < 256) {
                if (player.openContainer instanceof ContainerSpellCustomization) {
                    ((ContainerSpellCustomization) player.openContainer)
                            .setNameAndIndex(message.name, message.iconIndex);
                }
            }
        }
    }
}
