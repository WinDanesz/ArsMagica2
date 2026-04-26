package am2.network.packets;

import am2.common.extensions.EntityExtension;
import am2.common.packet.AMDataReader;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to update mana link visualization on the client.
 * Sent from server to client when mana links change.
 * <p>
 * Direction: Server -> Client
 */
public class PacketManaLinkUpdate extends AMPacket<PacketManaLinkUpdate> {

    private int entityId;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketManaLinkUpdate() {
    }

    /**
     * Create a new mana link update packet.
     *
     * @param entityId The entity ID whose mana links to update
     * @param data     The mana link data
     */
    public PacketManaLinkUpdate(int entityId, byte[] data) {
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
        // Validate length
        if (length < 0 || length > 10240) { // Max 10KB
            throw new IllegalArgumentException("Invalid mana link data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketManaLinkUpdate message, MessageContext ctx) {
        EntityLivingBase entity = (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity != null) {
            AMDataReader reader = new AMDataReader(message.data, false);
            ((EntityExtension) EntityExtension.For(entity)).handleManaLinkUpdate(reader);
        }
    }
}
