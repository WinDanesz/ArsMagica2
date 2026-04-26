package am2.network.packets;

import am2.common.extensions.SkillData;
import am2.common.lore.ArcaneCompendium;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to unlock an Occulus entry for a player.
 * Sent from client to server when the player unlocks a skill/compendium entry.
 * <p>
 * Direction: Client -> Server
 */
public class PacketOcculusUnlock extends AMPacket<PacketOcculusUnlock> {

    private String entryId;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketOcculusUnlock() {
    }

    /**
     * Create a new occulus unlock packet.
     *
     * @param entryId The ID of the entry to unlock
     */
    public PacketOcculusUnlock(String entryId) {
        this.entryId = entryId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, entryId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        entryId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketOcculusUnlock message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate entry ID to prevent exploits
            if (message.entryId != null && message.entryId.length() > 0 && message.entryId.length() <= 200) {
                SkillData.For(player).unlockSkill(message.entryId);
                ArcaneCompendium.For(player).unlockEntry(message.entryId);
            }
        }
    }
}
