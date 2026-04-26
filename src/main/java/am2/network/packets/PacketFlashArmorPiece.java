package am2.network.packets;

import am2.client.gui.AMGuiHelper;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to flash/blackout an armor piece on the client.
 * Sent from server to client to trigger a visual flash effect on armor.
 * <p>
 * Direction: Server -> Client
 */
public class PacketFlashArmorPiece extends AMPacket<PacketFlashArmorPiece> {

    private int entityId;
    private int armorSlot;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketFlashArmorPiece() {
    }

    /**
     * Create a new flash armor piece packet.
     *
     * @param entityId  The entity ID whose armor to flash
     * @param armorSlot The armor slot to flash
     */
    public PacketFlashArmorPiece(int entityId, int armorSlot) {
        this.entityId = entityId;
        this.armorSlot = armorSlot;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(armorSlot);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        armorSlot = buf.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketFlashArmorPiece message, MessageContext ctx) {
        // Validate armor slot (0-3 for boots, leggings, chestplate, helmet)
        if (message.armorSlot >= 0 && message.armorSlot <= 3) {
            AMGuiHelper.instance.blackoutArmorPiece(message.entityId, message.armorSlot);
        }
    }
}
