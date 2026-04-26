package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityLectern;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize lectern data to the client.
 * Sent from server to client when lectern contents change.
 * <p>
 * Direction: Server -> Client
 */
public class PacketLecternSync extends AMPacket<PacketLecternSync> {

    private BlockPos pos;
    private boolean hasStack;
    private ItemStack stack;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketLecternSync() {
    }

    /**
     * Create a new lectern sync packet.
     *
     * @param pos   The position of the lectern
     * @param stack The item stack to display (or null/empty)
     */
    public PacketLecternSync(BlockPos pos, ItemStack stack) {
        this.pos = pos;
        this.hasStack = !stack.isEmpty();
        this.stack = hasStack ? stack : ItemStack.EMPTY;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        buf.writeBoolean(hasStack);
        if (hasStack) {
            ByteBufUtils.writeItemStack(buf, stack);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        hasStack = buf.readBoolean();
        if (hasStack) {
            stack = ByteBufUtils.readItemStack(buf);
        } else {
            stack = ItemStack.EMPTY;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketLecternSync message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.pos);
        if (te instanceof TileEntityLectern) {
            if (message.hasStack) {
                ((TileEntityLectern) te).setStack(message.stack);
            } else {
                ((TileEntityLectern) te).setStack(ItemStack.EMPTY);
            }
        }
    }
}
