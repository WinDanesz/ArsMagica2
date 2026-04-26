package am2.network.packets;

import am2.common.extensions.AffinityData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize affinity data to the client.
 * Direction: Server -> Client
 */
public class PacketSyncAffinityData extends AbstractSyncCapabilityPacket<PacketSyncAffinityData> {

    public PacketSyncAffinityData() {
    }

    public PacketSyncAffinityData(int entityId, byte[] data) {
        super(entityId, data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void applyData(EntityLivingBase entity, byte[] data) {
        AffinityData.For(entity).handleUpdatePacket(data);
    }
}
