package am2.common.items;

import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellPart;
import am2.api.spell.SpellShape;
import am2.common.spell.SpellCaster;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.IRarity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDebugRune extends Item {

    public ItemDebugRune() {
        super();
        setMaxStackSize(1);
    }

    @Override
    public IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("§cCreative only");
        tooltip.add("§7Right-click: print NBT and capability data");
        tooltip.add("§7of the item held in your offhand");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!player.capabilities.isCreativeMode) return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));

        msg(player, "§e=== AM2 Debug Rune ===");

        // --- Spell in other hand ---
        EnumHand other = (hand == EnumHand.MAIN_HAND) ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
        ItemStack spell = player.getHeldItem(other);
        if (spell.isEmpty() || !spell.hasCapability(SpellCaster.INSTANCE, null)) {
            msg(player, "§7(No spell item in other hand)");
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        ISpellCaster caster = SpellCaster.of(spell);
        if (caster == null) {
            msg(player, "§cSpell capability is null!");
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        msg(player, "§e--- Spell: " + spell.getDisplayName() + " ---");
        msg(player, "§7ShapeGroups=" + caster.getShapeGroups().size()
                + "  currentGroup=" + caster.getCurrentShapeGroup()
                + "  common=" + caster.getSpellCommon().size() + " stages");

        int g = 0;
        for (List<List<SpellPart>> group : caster.getShapeGroups()) {
            msg(player, "§6  ShapeGroup[" + g + "] cost=" + String.format("%.1f", caster.getBaseManaCost(g)));
            printStages(player, group, "    ");
            g++;
        }

        List<List<SpellPart>> common = caster.getSpellCommon();
        if (!common.isEmpty()) {
            msg(player, "§6  Common:");
            printStages(player, common, "    ");
        }

        // --- Raw NBT ---
        msg(player, "§e--- Raw item NBT ---");
        NBTTagCompound itemTag = spell.getTagCompound();
        msg(player, itemTag != null ? itemTag.toString() : "(no tag compound)");

        msg(player, "§e--- Raw capability NBT ---");
        NBTBase capNbt = SpellCaster.INSTANCE.getStorage().writeNBT(SpellCaster.INSTANCE, caster, null);
        msg(player, capNbt != null ? capNbt.toString() : "(null)");

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private static void printStages(EntityPlayer player, List<List<SpellPart>> stages, String indent) {
        for (int i = 0; i < stages.size(); i++) {
            List<SpellPart> stage = stages.get(i);
            if (stage == null || stage.isEmpty()) {
                msg(player, indent + "§8Stage[" + i + "]: (empty)");
                continue;
            }
            msg(player, indent + "§8Stage[" + i + "]:");
            for (SpellPart part : stage) {
                if (part == null) { msg(player, indent + "  §cnull part"); continue; }
                ResourceLocation reg = part.getRegistryName();
                String kind = (part instanceof SpellShape) ? "§6[Shape]"
                        : (part instanceof SpellModifier) ? "§b[Mod]"
                        : (part instanceof SpellComponent) ? "§a[Comp]"
                        : "§f[?]";
                msg(player, indent + "  " + kind + " §f" + (reg != null ? reg : "null"));
            }
        }
    }

    private static void msg(EntityPlayer player, String text) {
        player.sendMessage(new TextComponentString(text));
    }
}
