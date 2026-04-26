package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to update inscription table data.
 * Sent from client to server when the player modifies the inscription table.
 * <p>
 * Direction: Client -> Server
 */
public class PacketInscriptionTableUpdate extends AMPacket<PacketInscriptionTableUpdate> {

    private BlockPos pos;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketInscriptionTableUpdate() {
    }

    /**
     * Create a new inscription table update packet.
     *
     * @param pos  The position of the inscription table
     * @param data The update data
     */
    public PacketInscriptionTableUpdate(BlockPos pos, byte[] data) {
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
            throw new IllegalArgumentException("Invalid inscription table data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    protected void handleServerSide(PacketInscriptionTableUpdate message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            TileEntity te = player.world.getTileEntity(message.pos);
            if (te instanceof TileEntityInscriptionTable) {
                ((TileEntityInscriptionTable) te).handleUpdatePacket(message.data);
                player.world.markAndNotifyBlock(te.getPos(), te.getWorld().getChunk(te.getPos()),
                        te.getWorld().getBlockState(te.getPos()), te.getWorld().getBlockState(te.getPos()), 3);
            }
        }
    }
}
