package am2.network.packets;

import am2.ArsMagica;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet for spawning special particle effects (arcs, beams, bolts, ribbons).
 * Carries raw byte[] data that is dispatched by the particle manager's
 * {@code handleClientPacketData()} method based on a sub-type byte.
 * <p>
 * Direction: Server -> Client
 */
public class PacketParticleSpawnSpecial extends AMPacket<PacketParticleSpawnSpecial> {

    private byte[] data;

    public PacketParticleSpawnSpecial() {
    }

    public PacketParticleSpawnSpecial(byte[] data) {
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int length = buf.readInt();
        if (length < 0 || length > 1048576) {
            throw new IllegalArgumentException("Invalid particle data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketParticleSpawnSpecial message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            ArsMagica.proxy.particleManager.handleClientPacketData(world, message.data);
        }
    }
}
