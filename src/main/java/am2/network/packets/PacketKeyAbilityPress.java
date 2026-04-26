package am2.network.packets;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.AbstractAffinityAbility;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to trigger a key press for an affinity ability.
 * Sent from client to server when the player presses a key-bound ability.
 * <p>
 * Direction: Client -> Server
 */
public class PacketKeyAbilityPress extends AMPacket<PacketKeyAbilityPress> {

    private int targetEntityId;
    private String abilityId;

    /**
     * Required empty constructor for packet registration.
     */
    public PacketKeyAbilityPress() {
    }

    /**
     * Create a new key ability press packet.
     *
     * @param targetEntityId The entity ID to apply the ability to
     * @param abilityId      The ResourceLocation string of the ability
     */
    public PacketKeyAbilityPress(int targetEntityId, String abilityId) {
        this.targetEntityId = targetEntityId;
        this.abilityId = abilityId;
    }

    /**
     * Convenience constructor using ResourceLocation.
     */
    public PacketKeyAbilityPress(int targetEntityId, ResourceLocation abilityId) {
        this(targetEntityId, abilityId.toString());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(targetEntityId);
        ByteBufUtils.writeUTF8String(buf, abilityId);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        targetEntityId = buf.readInt();
        abilityId = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketKeyAbilityPress message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player != null) {
            // Validate ability ID to prevent exploits
            if (message.abilityId != null && message.abilityId.length() > 0 && message.abilityId.length() <= 200) {
                Entity target = player.getEntityWorld().getEntityByID(message.targetEntityId);
                if (target != null && target instanceof EntityPlayer) {
                    AbstractAffinityAbility ability = ArsMagicaAPI.getAffinityAbilityRegistry()
                            .getValue(new ResourceLocation(message.abilityId));
                    if (ability != null && ability.canApply((EntityPlayer) target)) {
                        ability.applyKeyPress((EntityPlayer) target);
                    }
                }
            }
        }
    }
}
