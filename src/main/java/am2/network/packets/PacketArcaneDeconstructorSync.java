package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityArcaneDeconstructor;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize arcane deconstructor data to the client.
 * Sent from server to client when deconstructor state changes.
 * <p>
 * Direction: Server -> Client
 */
public class PacketArcaneDeconstructorSync extends AMPacket<PacketArcaneDeconstructorSync> {

    private BlockPos pos;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketArcaneDeconstructorSync() {
    }

    /**
     * Create a new arcane deconstructor sync packet.
     *
     * @param pos  The position of the arcane deconstructor
     * @param data The sync data
     */
    public PacketArcaneDeconstructorSync(BlockPos pos, byte[] data) {
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
        if (length < 0 || length > 10240) {
            throw new IllegalArgumentException("Invalid arcane deconstructor data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketArcaneDeconstructorSync message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
        if (te instanceof TileEntityArcaneDeconstructor) {
            ((TileEntityArcaneDeconstructor) te).handleSyncPacket(message.data);
        }
    }
}
