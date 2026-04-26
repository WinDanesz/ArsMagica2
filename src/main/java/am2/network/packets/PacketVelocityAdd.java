package am2.network.packets;

import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to add velocity to an entity on the client side.
 * Sent from server to clients when an entity's velocity needs to be modified.
 * <p>
 * Direction: Server -> Client
 */
public class PacketVelocityAdd extends AMPacket<PacketVelocityAdd> {

    private int entityId;
    private double velX;
    private double velY;
    private double velZ;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketVelocityAdd() {
    }

    /**
     * Create a new velocity add packet.
     *
     * @param entityId The entity ID to apply velocity to
     * @param velX     X component of velocity
     * @param velY     Y component of velocity
     * @param velZ     Z component of velocity
     */
    public PacketVelocityAdd(int entityId, double velX, double velY, double velZ) {
        this.entityId = entityId;
        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;
    }

    /**
     * Convenience constructor using an entity directly.
     */
    public PacketVelocityAdd(EntityLivingBase entity, double velX, double velY, double velZ) {
        this(entity.getEntityId(), velX, velY, velZ);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(velX);
        buf.writeDouble(velY);
        buf.writeDouble(velZ);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        velX = buf.readDouble();
        velY = buf.readDouble();
        velZ = buf.readDouble();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketVelocityAdd message, MessageContext ctx) {
        Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity != null) {
            // Validate velocity values are finite and within reasonable bounds to prevent client issues
            if (Double.isFinite(message.velX) && Double.isFinite(message.velY) && Double.isFinite(message.velZ)) {
                double maxVel = 100.0; // Maximum reasonable velocity
                if (Math.abs(message.velX) <= maxVel && Math.abs(message.velY) <= maxVel && Math.abs(message.velZ) <= maxVel) {
                    entity.motionX += message.velX;
                    entity.motionY += message.velY;
                    entity.motionZ += message.velZ;
                }
            }
        }
    }
}
