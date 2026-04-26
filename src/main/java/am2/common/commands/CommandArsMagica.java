package am2.common.commands;

import am2.common.armor.ArmorHelper;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.lore.ArcaneCompendium;
import am2.common.utils.SpellUtils;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandArsMagica extends CommandBase {

    public CommandArsMagica() {
    }

    @Override
    public String getName() {
        return "am";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.am2.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException("Wrong usage");
        else if (args[0].equalsIgnoreCase("magiclevel"))
            handleMagicLevelUp(server, sender, args);
        else if (args[0].equalsIgnoreCase("forcesync")) {
            EntityExtension.For(getCommandSenderAsPlayer(sender)).forceUpdate();
            AffinityData.For(getCommandSenderAsPlayer(sender)).forceUpdate();
            SkillData.For(getCommandSenderAsPlayer(sender)).forceUpdate();
            ArcaneCompendium.For(getCommandSenderAsPlayer(sender)).forceUpdate();
            notifyCommandListener(sender, this, "commands.am2.sync.successful", new Object[]{});
        } else if (args[0].equalsIgnoreCase("updatespells")) {
            EntityPlayer player = getCommandSenderAsPlayer(sender);
            try {
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack stack = player.inventory.getStackInSlot(i);
                    SpellUtils.updateSpell(stack);
                }
            } catch (Throwable t) {
                throw new CommandException("Error updating item.");
            }
        } else if (args[0].equalsIgnoreCase("setmana")) {
            handleSetMana(server, sender, args);
        } else if (args[0].equalsIgnoreCase("infusexp")) {
            handleInfuseXP(server, sender, args);
        }
    }

    private void handleMagicLevelUp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("Magic Level requires at least one argument");
        else if (args.length == 2) {
            int level = parseInt(args[1]);
            if (level < 1) level = 1;
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            EntityExtension.For(entityplayer).setMagicLevelWithMana(level);
            // Force sync to client to fix the sync issue
            EntityExtension.For(entityplayer).forceUpdate();
            notifyCommandListener(sender, this, "commands.am2.levelup.successful", new Object[]{entityplayer.getDisplayName(), level});
        } else if (args.length == 3) {
            int level = parseInt(args[1]);
            if (level < 1) level = 1;
            Entity ent = getEntity(server, sender, args[2]);
            if (ent instanceof EntityLivingBase) {
                EntityExtension.For((EntityLivingBase) ent).setMagicLevelWithMana(level);
                // Force sync to client to fix the sync issue
                EntityExtension.For((EntityLivingBase) ent).forceUpdate();
            }
            notifyCommandListener(sender, this, "commands.am2.levelup.successful", new Object[]{ent.getDisplayName(), level});
        } else throw new WrongUsageException("Magic Level has too much arguments");
    }

    private void handleSetMana(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("Set Mana requires at least one argument");
        else if (args.length == 2) {
            float manaAmount = (float) parseDouble(args[1]);
            if (manaAmount < 0) manaAmount = 0;
            EntityPlayer entityplayer = getCommandSenderAsPlayer(sender);
            EntityExtension ext = EntityExtension.For(entityplayer);
            float maxMana = ext.getMaxMana();
            if (manaAmount > maxMana) manaAmount = maxMana;
            ext.setCurrentMana(manaAmount);
            // Force sync to client to update the mana bar
            ext.forceUpdate();
            notifyCommandListener(sender, this, "commands.am2.setmana.successful", new Object[]{entityplayer.getDisplayName(), manaAmount});
        } else if (args.length == 3) {
            float manaAmount = (float) parseDouble(args[1]);
            if (manaAmount < 0) manaAmount = 0;
            Entity ent = getEntity(server, sender, args[2]);
            if (ent instanceof EntityLivingBase) {
                EntityExtension ext = EntityExtension.For((EntityLivingBase) ent);
                float maxMana = ext.getMaxMana();
                if (manaAmount > maxMana) manaAmount = maxMana;
                ext.setCurrentMana(manaAmount);
                // Force sync to client
                ext.forceUpdate();
            }
            notifyCommandListener(sender, this, "commands.am2.setmana.successful", new Object[]{ent.getDisplayName(), manaAmount});
        } else throw new WrongUsageException("Set Mana has too many arguments");
    }

    private void handleInfuseXP(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException("/am2 infusexp <amount> - Adds infused XP to the held armor item");
        EntityPlayer player = getCommandSenderAsPlayer(sender);
        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty()) throw new CommandException("You must be holding an item");
        if (!(heldItem.getItem() instanceof net.minecraft.item.ItemArmor)) throw new CommandException("The held item must be an armor piece");
        float amount = (float) parseDouble(args[1]);
        if (amount <= 0) throw new CommandException("Amount must be positive");
        ArmorHelper.addXPToArmor(amount, heldItem);
        notifyCommandListener(sender, this, "Added %.0f infused XP to held armor (level now %d)", amount, ArmorHelper.getArmorLevel(heldItem));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, Lists.newArrayList("magiclevel", "forcesync", "updatespells", "setmana", "infusexp"));
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("magiclevel")) return Collections.emptyList();
            else if (args[0].equalsIgnoreCase("setmana")) return Collections.emptyList();
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("magiclevel"))
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            else if (args[0].equalsIgnoreCase("setmana"))
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

}
