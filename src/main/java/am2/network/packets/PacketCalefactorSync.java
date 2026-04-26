package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityCalefactor;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize calefactor data to the client.
 * Sent from server to client when calefactor state changes.
 * <p>
 * Direction: Server -> Client
 */
public class PacketCalefactorSync extends AMPacket<PacketCalefactorSync> {

    private BlockPos pos;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketCalefactorSync() {
    }

    /**
     * Create a new calefactor sync packet.
     *
     * @param pos  The position of the calefactor
     * @param data The sync data
     */
    public PacketCalefactorSync(BlockPos pos, byte[] data) {
        this.pos = pos;
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        int length = buf.readInt();
        // Validate length
        if (length < 0 || length > 10240) { // Max 10KB for TileEntity data
            throw new IllegalArgumentException("Invalid calefactor data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketCalefactorSync message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
        if (te instanceof TileEntityCalefactor) {
            ((TileEntityCalefactor) te).handlePacket(message.data);
        }
    }
}
