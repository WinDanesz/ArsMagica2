package am2.network.packets;

import am2.ArsMagica;
import am2.common.LogHelper;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Packet for synchronizing server config data on login/respawn.
 * Sends skill tree cap, disabled skills, mana cap, and XP calculation mode.
 * <p>
 * Direction: Server -> Client
 */
public class PacketPlayerLogin extends AMPacket<PacketPlayerLogin> {

    private int skillTreeSecondaryTierCap;
    private int[] disabledSkills;
    private double manaCap;
    private boolean oldXpCalculations;

    public PacketPlayerLogin() {
    }

    public PacketPlayerLogin(int skillTreeSecondaryTierCap, int[] disabledSkills, double manaCap, boolean oldXpCalculations) {
        this.skillTreeSecondaryTierCap = skillTreeSecondaryTierCap;
        this.disabledSkills = disabledSkills;
        this.manaCap = manaCap;
        this.oldXpCalculations = oldXpCalculations;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(skillTreeSecondaryTierCap);
        buf.writeInt(disabledSkills.length);
        for (int id : disabledSkills) {
            buf.writeInt(id);
        }
        buf.writeDouble(manaCap);
        buf.writeBoolean(oldXpCalculations);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        skillTreeSecondaryTierCap = buf.readInt();
        int length = buf.readInt();
        if (length < 0 || length > 10000) {
            throw new IllegalArgumentException("Invalid disabled skills array length: " + length);
        }
        disabledSkills = new int[length];
        for (int i = 0; i < length; i++) {
            disabledSkills[i] = buf.readInt();
        }
        manaCap = buf.readDouble();
        oldXpCalculations = buf.readBoolean();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected void handleClientSide(PacketPlayerLogin message, MessageContext ctx) {
        ArsMagica.config.setSkillTreeSecondaryTierCap(message.skillTreeSecondaryTierCap);
        ArsMagica.config.setManaCap(message.manaCap);
        ArsMagica.config.setOldXpCalculations(message.oldXpCalculations);

        LogHelper.info("Received player login packet.");
        LogHelper.debug("Secondary tree cap: %d", message.skillTreeSecondaryTierCap);
        LogHelper.debug("Disabled skills: %d", message.disabledSkills.length);
        LogHelper.debug("Mana cap: %.2f", message.manaCap);
        LogHelper.debug("Old XP System: {}", message.oldXpCalculations);

        ArsMagica.disabledSkills.disableAllSkillsIn(message.disabledSkills);
    }
}
