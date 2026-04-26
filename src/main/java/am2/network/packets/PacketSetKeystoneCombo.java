package am2.network.packets;

import am2.common.container.ContainerKeystone;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to set the active keystone combination.
 * Sent from client to server when the player selects a saved combination.
 * <p>
 * Direction: Client -> Server
 */
public class PacketSetKeystoneCombo extends AMPacket<PacketSetKeystoneCombo> {

    private int comboIndex;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketSetKeystoneCombo() {
    }

    /**
     * Create a new set keystone combo packet.
     *
     * @param comboIndex The index of the combination to set
     */
    public PacketSetKeystoneCombo(int comboIndex) {
        this.comboIndex = comboIndex;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(comboIndex);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        comboIndex = buf.readInt();
    }

    @Override
    protected void handleServerSide(PacketSetKeystoneCombo message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && player.openContainer instanceof ContainerKeystone) {
            // Validate combo index
            if (message.comboIndex >= 0 && message.comboIndex < 100) {
                ((ContainerKeystone) player.openContainer).setInventoryToCombination(message.comboIndex);
            }
        }
    }
}
