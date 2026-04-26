package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityCraftingAltar;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize crafting altar data to the client.
 * Sent from server to client when crafting altar state changes.
 * <p>
 * Direction: Server -> Client
 */
public class PacketCraftingAltarSync extends AMPacket<PacketCraftingAltarSync> {

    private BlockPos pos;
    private byte[] data;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketCraftingAltarSync() {
    }

    /**
     * Create a new crafting altar sync packet.
     *
     * @param pos  The position of the crafting altar
     * @param data The sync data
     */
    public PacketCraftingAltarSync(BlockPos pos, byte[] data) {
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
            throw new IllegalArgumentException("Invalid crafting altar data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketCraftingAltarSync message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
        if (te instanceof TileEntityCraftingAltar) {
            ((TileEntityCraftingAltar) te).HandleUpdatePacket(message.data);
        }
    }
}
