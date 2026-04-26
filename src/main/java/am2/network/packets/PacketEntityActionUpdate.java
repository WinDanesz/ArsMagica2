package am2.network.packets;

import am2.common.bosses.BossActions;
import am2.common.bosses.IArsMagicaBoss;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet to update boss entity action on the client.
 * Sent from server to client when a boss changes its action/animation.
 * <p>
 * Direction: Server -> Client
 */
public class PacketEntityActionUpdate extends AMPacket<PacketEntityActionUpdate> {

    private int entityId;
    private int actionOrdinal;
    private int ticksInAction;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketEntityActionUpdate() {
    }

    /**
     * Create a new entity action update packet.
     *
     * @param entityId      The entity ID of the boss
     * @param actionOrdinal The ordinal of the BossActions enum
     * @param ticksInAction The number of ticks the boss has been in this action
     */
    public PacketEntityActionUpdate(int entityId, int actionOrdinal, int ticksInAction) {
        this.entityId = entityId;
        this.actionOrdinal = actionOrdinal;
        this.ticksInAction = ticksInAction;
    }

    /**
     * Convenience constructor with BossActions.
     */
    public PacketEntityActionUpdate(int entityId, BossActions action, int ticksInAction) {
        this(entityId, action.ordinal(), ticksInAction);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(actionOrdinal);
        buf.writeInt(ticksInAction);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        actionOrdinal = buf.readInt();
        ticksInAction = buf.readInt();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketEntityActionUpdate message, MessageContext ctx) {
        Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityId);
        if (entity != null && !entity.isDead && entity instanceof IArsMagicaBoss) {
            // Validate action ordinal
            if (message.actionOrdinal >= 0 && message.actionOrdinal < BossActions.values().length) {
                IArsMagicaBoss boss = (IArsMagicaBoss) entity;
                boss.setCurrentAction(BossActions.values()[message.actionOrdinal]);
                boss.setTicksInCurrentAction(message.ticksInAction);
            }
        }
    }
}
