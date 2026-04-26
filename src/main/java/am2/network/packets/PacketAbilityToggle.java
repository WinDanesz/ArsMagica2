package am2.network.packets;

import am2.common.extensions.AffinityData;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to toggle an affinity ability on/off.
 * Sent from client to server when the player toggles an ability.
 * <p>
 * Direction: Client -> Server
 */
public class PacketAbilityToggle extends AMPacket<PacketAbilityToggle> {

    private String abilityId;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketAbilityToggle() {
    }

    /**
     * Create a new ability toggle packet.
     *
     * @param abilityId The ID of the ability to toggle
     */
    public PacketAbilityToggle(String abilityId) {
        this.abilityId = abilityId;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, abilityId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        abilityId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketAbilityToggle message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate ability ID to prevent exploits
            if (message.abilityId != null && message.abilityId.length() > 0 && message.abilityId.length() <= 100) {
                AffinityData affinityData = AffinityData.For(player);
                boolean newState = !affinityData.getAbilityBoolean(message.abilityId);

                // Update the ability state
                affinityData.addAbilityBoolean(message.abilityId, newState);

                // Send feedback message to player
                // Note: Using simple strings here, proper localization should be used
                String statusText = newState ? "enabled" : "disabled";
                String messageText = String.format("Ability '%s' %s", message.abilityId, statusText);
                player.sendMessage(new TextComponentString(messageText));
            }
        }
    }
}
