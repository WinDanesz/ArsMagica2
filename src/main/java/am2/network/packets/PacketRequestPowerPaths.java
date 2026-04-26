package am2.network.packets;

import am2.api.math.AMVector3;
import am2.api.power.IPowerNode;
import am2.common.power.PowerNodeRegistry;
import am2.network.AMNetworkHandler;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to request power node path data from the server.
 * Sent from client to server when the player wants to view power paths.
 * <p>
 * Direction: Client -> Server
 */
public class PacketRequestPowerPaths extends AMPacket<PacketRequestPowerPaths> {

    private byte requestType;
    private float x;
    private float y;
    private float z;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketRequestPowerPaths() {
    }

    /**
     * Create a new power path request packet.
     *
     * @param requestType Request type (1 for specific node)
     * @param x           X coordinate of the node
     * @param y           Y coordinate of the node
     * @param z           Z coordinate of the node
     */
    public PacketRequestPowerPaths(byte requestType, float x, float y, float z) {
        this.requestType = requestType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Convenience constructor with AMVector3.
     */
    public PacketRequestPowerPaths(byte requestType, AMVector3 loc) {
        this(requestType, (float) loc.x, (float) loc.y, (float) loc.z);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(requestType);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        requestType = buf.readByte();
        x = buf.readFloat();
        y = buf.readFloat();
        z = buf.readFloat();
    }

    @Override
    protected void handleServerSide(PacketRequestPowerPaths message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && message.requestType == 1) {
            AMVector3 loc = new AMVector3(message.x, message.y, message.z);
            TileEntity te = player.world.getTileEntity(loc.toBlockPos());
            if (te != null && te instanceof IPowerNode) {
                AMNetworkHandler.getNetwork().sendTo(
                        new PacketPowerPathResponse((byte) 1,
                                PowerNodeRegistry.For(player.world).getDataCompoundForNode((IPowerNode<?>) te),
                                te.getPos().getX(), te.getPos().getY(), te.getPos().getZ()),
                        player);
            }
        }
    }
}
