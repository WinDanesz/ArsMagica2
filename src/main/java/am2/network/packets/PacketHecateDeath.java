package am2.network.packets;

import am2.ArsMagica;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to spawn Hecate death particle effect on the client.
 * Sent from server to client when Hecate boss dies.
 * <p>
 * Direction: Server -> Client
 */
public class PacketHecateDeath extends AMPacket<PacketHecateDeath> {

    private double x;
    private double y;
    private double z;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketHecateDeath() {
    }

    /**
     * Create a new Hecate death packet.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     */
    public PacketHecateDeath(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketHecateDeath message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().world;
        if (world != null) {
            for (int i = 0; i < 10 * ArsMagica.config.getGFXLevel(); ++i) {
                world.spawnParticle(EnumParticleTypes.FLAME,
                        message.x, message.y + 1, message.z,
                        world.rand.nextDouble() - 0.5,
                        world.rand.nextDouble() - 0.5,
                        world.rand.nextDouble() - 0.5);
            }
        }
    }
}
