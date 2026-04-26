package am2.common.items;

import am2.common.utils.EntityUtils;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nullable;
import java.util.List;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemJournal extends Item implements IBauble {

    private static final String KEY_NBT_XP = "Stored_XP";
    private static final String KEY_NBT_OWNER = "Owner";

    public ItemJournal() {
        super();
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        String owner = getOwner(stack);
        if (owner == null) {
            tooltip.add(I18n.format("am2.tooltip.unowned"));
            tooltip.add(I18n.format("am2.tooltip.journal_use"));
            return;
        } else {
            tooltip.add(I18n.format("am2.tooltip.journal_owner"));
            tooltip.add(I18n.format("am2.tooltip.journal_owner_2", owner));
        }

        if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.getName().equals(owner)) {
            tooltip.add(I18n.format("am2.tooltip.containedXP", getXPInJournal(stack)));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.world.isRemote) {
            if (getOwner(stack) == null) {
                setOwner(stack, player);
            } else if (!getOwner(stack).equals(player.getName())) {
                player.sendMessage(new TextComponentString(I18n.format("am2.tooltip.notYourJournal")));
                return super.onItemRightClick(world, player, hand);
            }

            if (player.isSneaking()) {
                int removedXP = EntityUtils.deductXP(10, player);
                addXPToJournal(stack, removedXP);
            } else {
                int amt = Math.min(getXPInJournal(stack), 10);
                if (amt > 0) {
                    player.addExperience(amt);
                    deductXPFromJournal(stack, amt);
                }
            }
        }

        return super.onItemRightClick(world, player, hand);
    }

    private void addXPToJournal(ItemStack stack, int amount) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger(KEY_NBT_XP, stack.getTagCompound().getInteger(KEY_NBT_XP) + amount);
    }

    private void deductXPFromJournal(ItemStack stack, int amount) {
        addXPToJournal(stack, -amount);
    }

    private int getXPInJournal(ItemStack stack) {
        if (!stack.hasTagCompound())
            return 0;
        return stack.getTagCompound().getInteger(KEY_NBT_XP);
    }

    private String getOwner(ItemStack stack) {
        if (!stack.hasTagCompound())
            return null;
        return stack.getTagCompound().getString(KEY_NBT_OWNER);
    }

    private void setOwner(ItemStack stack, EntityPlayer player) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setString(KEY_NBT_OWNER, player.getName());
    }

    // ---------------------------------------------------------------
    // IBauble – Baubles charm-slot usage
    // ---------------------------------------------------------------

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack stack) {
        return BaubleType.CHARM;
    }

}
