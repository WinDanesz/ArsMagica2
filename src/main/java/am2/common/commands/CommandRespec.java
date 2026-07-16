package am2.common.commands;

import am2.common.extensions.SkillData;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandRespec extends CommandBase {

    public CommandRespec() {
    }

    @Override
    public String getName() {
        return "respec";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/respec [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public List<String> getAliases() {
        return Lists.newArrayList("amrespec", "am2respec");
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) throw new WrongUsageException(getUsage(sender));
        EntityPlayer player;
        if (args.length == 1) {
            Entity ent = getEntity(server, sender, args[0]);
            if (!(ent instanceof EntityPlayer)) throw new CommandException("Target must be a player");
            player = (EntityPlayer) ent;
        } else {
            player = getCommandSenderAsPlayer(sender);
        }
        SkillData.For(player).respec();
        notifyCommandListener(sender, this, "Respec successful: returned all spent occulus points for %s", player.getDisplayNameString());
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }
}
