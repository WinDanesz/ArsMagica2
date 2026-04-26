package am2.network.packets;

import am2.common.container.ContainerKeystone;
import am2.common.items.ItemKeystone;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to save or remove a keystone combination.
 * Sent from client to server when the player saves or removes a keystone combo.
 * <p>
 * Direction: Client -> Server
 */
public class PacketSaveKeystoneCombo extends AMPacket<PacketSaveKeystoneCombo> {

    private boolean add;
    private String name;
    private int[] metas;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSaveKeystoneCombo() {
    }

    /**
     * Create a new save keystone combo packet.
     *
     * @param add   True to add, false to remove
     * @param name  The name of the combination
     * @param metas The three metadata values for the combination
     */
    public PacketSaveKeystoneCombo(boolean add, String name, int[] metas) {
        this.add = add;
        this.name = name;
        this.metas = metas;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(add);
        ByteBufUtils.writeUTF8String(buf, name);
        if (metas != null && metas.length == 3) {
            buf.writeInt(metas[0]);
            buf.writeInt(metas[1]);
            buf.writeInt(metas[2]);
        } else {
            // Write zeros if invalid
            buf.writeInt(0);
            buf.writeInt(0);
            buf.writeInt(0);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        add = buf.readBoolean();
        name = ByteBufUtils.readUTF8String(buf);
        metas = new int[3];
        metas[0] = buf.readInt();
        metas[1] = buf.readInt();
        metas[2] = buf.readInt();
    }

    @Override
    protected void handleServerSide(PacketSaveKeystoneCombo message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && player.openContainer instanceof ContainerKeystone) {
            // Validate input
            if (message.name != null && message.name.length() > 0 && message.name.length() <= 50) {
                ContainerKeystone container = (ContainerKeystone) player.openContainer;
                if (message.add) {
                    ItemKeystone.addCombination(container.getKeystoneStack(), message.name, message.metas);
                } else {
                    ItemKeystone.removeCombination(container.getKeystoneStack(), message.name);
                }
            }
        }
    }
}
