package am2.api.items;

import am2.api.extensions.ISpellCaster;
import am2.common.spell.SpellCaster;
import am2.common.utils.InventoryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IBoundItem {

    public float maintainCost(EntityPlayer player, ItemStack stack);

    static final float diminishedMaintain = 0.1f;
    static final float normalMaintain = 0.4f;
    static final float augmentedMaintain = 1.0f;

    default boolean applySpellOnHitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (!stack.hasTagCompound())
            return true;
        ItemStack copiedStack = stack.copy();
        ISpellCaster caster = stack.copy().getCapability(SpellCaster.INSTANCE, null);
        if (caster != null)
            caster.createSpellData(copiedStack).skipFirstStage().execute(attacker.world, attacker, target, target.posX, target.posY, target.posZ, null);
        return true;
    }

    default boolean applySpellAtPosition(ItemStack stack, World world, EntityLivingBase caster, double x, double y, double z) {
        if (!stack.hasTagCompound())
            return true;
        ItemStack copiedStack = stack.copy();
        ISpellCaster spellCaster = stack.copy().getCapability(SpellCaster.INSTANCE, null);
        if (spellCaster != null)
            spellCaster.createSpellData(copiedStack).skipFirstStage().execute(world, caster, null, x, y, z, null);
        return true;
    }

    default boolean unbindOnDrop(ItemStack item, EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            if (player.inventory.getStackInSlot(i) == item) {
                player.inventory.setInventorySlotContents(i, InventoryUtilities.restoreSpellFromBoundItem(item));
                break;
            }
        }
        return false;
    }
}
