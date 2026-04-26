package am2.network.packets;

import am2.common.extensions.SkillData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to synchronize skill data to the client.
 * Direction: Server -> Client
 */
public class PacketSyncSkillData extends AbstractSyncCapabilityPacket<PacketSyncSkillData> {

    public PacketSyncSkillData() {
    }

    public PacketSyncSkillData(int entityId, byte[] data) {
        super(entityId, data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void applyData(EntityLivingBase entity, byte[] data) {
        SkillData.For(entity).handleUpdatePacket(data);
    }
}
