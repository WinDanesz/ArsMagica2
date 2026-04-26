package am2.network.packets;

import am2.ArsMagica;
import am2.common.blocks.tileentity.TileEntityArmorImbuer;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to imbue armor with special effects.
 * Sent from client to server when the player imbues armor at an armor imbuer.
 * <p>
 * Direction: Client -> Server
 */
public class PacketImbueArmor extends AMPacket<PacketImbueArmor> {

    private BlockPos pos;
    private String imbuementId;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketImbueArmor() {
    }

    /**
     * Create a new imbue armor packet.
     *
     * @param pos         The position of the armor imbuer
     * @param imbuementId The ResourceLocation string of the imbuement
     */
    public PacketImbueArmor(BlockPos pos, String imbuementId) {
        this.pos = pos;
        this.imbuementId = imbuementId;
    }

    /**
     * Convenience constructor with ResourceLocation.
     */
    public PacketImbueArmor(BlockPos pos, ResourceLocation imbuementId) {
        this(pos, imbuementId.toString());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(pos.getX());
        buf.writeInt(pos.getY());
        buf.writeInt(pos.getZ());
        ByteBufUtils.writeUTF8String(buf, imbuementId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        int z = buf.readInt();
        pos = new BlockPos(x, y, z);
        imbuementId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketImbueArmor message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null && ArsMagica.config.getIsImbueEnchantEnabled()) {
            // Validate imbuement ID
            if (message.imbuementId != null && message.imbuementId.length() > 0 && message.imbuementId.length() <= 200) {
                TileEntity te = player.world.getTileEntity(message.pos);
                if (te instanceof TileEntityArmorImbuer) {
                    ((TileEntityArmorImbuer) te).imbueCurrentArmor(new ResourceLocation(message.imbuementId));
                }
            }
        }
    }
}
