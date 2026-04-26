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
 * Base class for capability sync packets that share the same entityId + byte[] data structure.
 * Subclasses only need to implement {@link #applyData(EntityLivingBase, byte[])} to route
 * the deserialized data to the correct capability.
 * <p>
 * Direction: Server -> Client
 */
public abstract class AbstractSyncCapabilityPacket<T extends AbstractSyncCapabilityPacket<T>> extends AMPacket<T> {

    protected int entityId;
    protected byte[] data;

    public AbstractSyncCapabilityPacket() {
    }

    public AbstractSyncCapabilityPacket(int entityId, byte[] data) {
        this.entityId = entityId;
        this.data = data;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(data.length);
        buf.writeBytes(data);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        int length = buf.readInt();
        if (length < 0 || length > 1048576) { // Max 1MB
            throw new IllegalArgumentException("Invalid capability data length: " + length);
        }
        data = new byte[length];
        buf.readBytes(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(T message, MessageContext ctx) {
        Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity instanceof EntityLivingBase) {
            applyData((EntityLivingBase) entity, message.data);
        }
    }

    /**
     * Apply the deserialized data to the appropriate capability on the given entity.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void applyData(EntityLivingBase entity, byte[] data);
}
