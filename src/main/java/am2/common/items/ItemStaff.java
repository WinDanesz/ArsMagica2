package am2.common.items;

import am2.ArsMagica;
import am2.api.extensions.ISpellCaster;
import am2.common.spell.SpellCaster;
import am2.common.spell.StaffSpellCaster;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Magic Staff – casts the bound spell using item durability instead of mana/burnout.
 * Each successful cast (including channeled spells on release) consumes 1 durability.
 * The staff breaks when durability is depleted.
 */
public class ItemStaff extends ItemSpellBase {

    public ItemStaff() {
        super();
        this.setMaxDamage(ArsMagica.config.getStaffDurability());
        this.setMaxStackSize(1);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new StaffSpellCaster();
    }

    /**
     * Bootstrap capability data from the item's tagCompound. This allows /give commands
     * to populate the staff's spell by including a {@code SpellCasterCaps} tag in the NBT.
     * The tag is consumed on the first server tick and removed from the tagCompound.
     */
    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag.hasKey("SpellCasterCaps")) {
                ISpellCaster caster = SpellCaster.of(stack);
                if (caster instanceof SpellCaster) {
                    ((SpellCaster) caster).deserializeNBT(tag.getTag("SpellCasterCaps"));
                }
                tag.removeTag("SpellCasterCaps");
                if (tag.isEmpty()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
            }
        }
    }

    // --- Right-click always starts casting (never opens customization GUI) ---

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.hasTagCompound()) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    // --- Channeled spells cast per tick but do NOT cost durability per tick ---

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase caster, int count) {
        if (stack.hasCapability(SpellCaster.INSTANCE, null) && caster != null) {
            ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
            if (spell.createSpellData(stack).isChanneled()) {
                spell.cast(stack, caster.world, caster);
            }
        }
    }

    // --- On release: cast (non-channeled) or finish channeling, then damage ---

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {
        if (stack.hasCapability(SpellCaster.INSTANCE, null) && player != null) {
            ISpellCaster spell = stack.getCapability(SpellCaster.INSTANCE, null);
            boolean success = spell.cast(stack, world, player);
            if (success && player instanceof EntityPlayer) {
                stack.damageItem(1, player);
            }
        }
    }

    // --- Tooltip: show uses remaining instead of mana cost ---

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (!stack.hasTagCompound()) return;
        int remaining = getMaxDamage(stack) - getDamage(stack);
        tooltip.add(String.format("Uses remaining: %d", remaining));
        // Delegate advanced spell parts display to the parent (skipping its mana line).
        // The parent adds mana cost first, then the advanced breakdown. We call it and
        // strip the first "Mana Cost" line it added.
        int before = tooltip.size();
        super.addInformation(stack, worldIn, tooltip, flagIn);
        // Remove the "Mana Cost : ..." line that the parent added
        for (int i = before; i < tooltip.size(); i++) {
            if (tooltip.get(i).startsWith("Mana Cost")) {
                tooltip.remove(i);
                break;
            }
        }
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return getDamage(stack) > 0;
    }

    /**
     * Bypass {@link ItemSpellBase#getItemStackDisplayName} which returns "Unnamed Spell".
     * If the stack has a custom display name (e.g. from a loot function), use that.
     * Otherwise, resolve the lang-file name ("Magic Staff") via the standard Item translation key.
     */
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display", 10)) {
            NBTTagCompound display = stack.getTagCompound().getCompoundTag("display");
            if (display.hasKey("Name", 8)) {
                return display.getString("Name");
            }
        }
        return net.minecraft.util.text.translation.I18n.translateToLocal(this.getUnlocalizedNameInefficiently(stack) + ".name").trim();
    }
}
