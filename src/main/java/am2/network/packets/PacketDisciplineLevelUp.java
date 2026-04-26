package am2.network.packets;

import am2.common.extensions.SkillData;
import am2.common.skill.Discipline;
import am2.network.AMPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Packet to level up a discipline for a player.
 * Sent from client to server when the player clicks the + button on the discipline tab.
 * <p>
 * Direction: Client -> Server
 */
public class PacketDisciplineLevelUp extends AMPacket<PacketDisciplineLevelUp> {

    private String disciplineName;

    public PacketDisciplineLevelUp() {
    }

    public PacketDisciplineLevelUp(Discipline discipline) {
        this.disciplineName = discipline.getName();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, disciplineName);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        disciplineName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    protected void handleServerSide(PacketDisciplineLevelUp message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player == null || message.disciplineName == null) return;
        Discipline discipline = Discipline.fromName(message.disciplineName);
        if (discipline == null) return;
        SkillData data = (SkillData) SkillData.For(player);
        if (data.canLevelUpDiscipline(discipline)) {
            data.levelUpDiscipline(discipline);
        }
    }
}
