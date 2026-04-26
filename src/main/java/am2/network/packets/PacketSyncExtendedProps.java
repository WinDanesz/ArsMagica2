package am2.network.packets;

import am2.common.extensions.EntityExtension;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize extended properties to the client.
 * Direction: Server -> Client
 */
public class PacketSyncExtendedProps extends AbstractSyncCapabilityPacket<PacketSyncExtendedProps> {

    public PacketSyncExtendedProps() {
    }

    public PacketSyncExtendedProps(int entityId, byte[] data) {
        super(entityId, data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void applyData(EntityLivingBase entity, byte[] data) {
        EntityExtension.For(entity).handleUpdatePacket(data);
    }
}
