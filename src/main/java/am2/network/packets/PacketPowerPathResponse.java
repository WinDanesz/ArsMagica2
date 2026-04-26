package am2.network.packets;

import am2.ArsMagica;
import am2.api.power.IPowerNode;
import am2.common.power.PowerNodeEntry;
import am2.common.power.PowerNodeRegistry;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to send power node path data to the client.
 * Sent from server to client in response to power path requests.
 * <p>
 * Direction: Server -> Client
 */
public class PacketPowerPathResponse extends AMPacket<PacketPowerPathResponse> {

    private byte responseType;
    private NBTTagCompound compound;
    private int x;
    private int y;
    private int z;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketPowerPathResponse() {
    }

    /**
     * Create a new power path response packet.
     *
     * @param responseType Response type (0 for paths only, 1 for node data)
     * @param compound     The NBT compound with power data
     * @param x            X coordinate (for type 1)
     * @param y            Y coordinate (for type 1)
     * @param z            Z coordinate (for type 1)
     */
    public PacketPowerPathResponse(byte responseType, NBTTagCompound compound, int x, int y, int z) {
        this.responseType = responseType;
        this.compound = compound;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Convenience constructor for type 0 (paths only).
     */
    public PacketPowerPathResponse(NBTTagCompound compound) {
        this((byte) 0, compound, 0, 0, 0);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(responseType);
        ByteBufUtils.writeTag(buf, compound);
        if (responseType == 1) {
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        responseType = buf.readByte();
        compound = ByteBufUtils.readTag(buf);
        if (responseType == 1) {
            x = buf.readInt();
            y = buf.readInt();
            z = buf.readInt();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketPowerPathResponse message, MessageContext ctx) {
        if (message.compound != null) {
            if (message.responseType == 0) {
                // Just display the paths
                PowerNodeEntry pnd = PowerNodeRegistry.For(Minecraft.getMinecraft().world)
                        .parseFromNBT(message.compound);
                ArsMagica.proxy.receivePowerPathVisuals(pnd.getNodePaths());
            } else if (message.responseType == 1) {
                // Update tracked node data
                ArsMagica.proxy.setTrackedPowerCompound((NBTTagCompound) message.compound.copy());
                TileEntity te = Minecraft.getMinecraft().world.getTileEntity(new BlockPos(message.x, message.y, message.z));
                if (te != null && te instanceof IPowerNode) {
                    PowerNodeRegistry.For(Minecraft.getMinecraft().world)
                            .setDataCompoundForNode((IPowerNode<?>) te, message.compound);
                }
            }
        }
    }
}
