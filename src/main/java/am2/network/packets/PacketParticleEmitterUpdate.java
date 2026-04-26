package am2.network.packets;

import am2.common.blocks.tileentity.TileEntityParticleEmitter;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to update particle emitter (deco block) data.
 * Sent from client to server when the player configures a particle emitter.
 * <p>
 * Direction: Client -> Server
 */
public class PacketParticleEmitterUpdate extends AMPacket<PacketParticleEmitterUpdate> {

    private BlockPos pos;
    private NBTTagCompound nbt;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketParticleEmitterUpdate() {
    }

    /**
     * Create a new particle emitter update packet.
     *
     * @param pos The position of the particle emitter
     * @param nbt The NBT data to update
     */
    public PacketParticleEmitterUpdate(BlockPos pos, NBTTagCompound nbt) {
        this.pos = pos;
        this.nbt = nbt;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeTag(buf, nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        nbt = ByteBufUtils.readTag(buf);
    }

    @Override
    protected void handleServerSide(PacketParticleEmitterUpdate message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && message.nbt != null) {
            TileEntity te = player.world.getTileEntity(message.pos);
            if (te instanceof TileEntityParticleEmitter) {
                ((TileEntityParticleEmitter) te).readFromNBT(message.nbt);
                player.world.markAndNotifyBlock(te.getPos(), player.world.getChunk(te.getPos()),
                        player.world.getBlockState(te.getPos()), player.world.getBlockState(te.getPos()), 2);
            }
        }
    }
}
