package am2.common.container.slot;

import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.items.ItemSpellBase;
import am2.common.registry.AMItems;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotInscriptionTable extends Slot {

    public SlotInscriptionTable(TileEntityInscriptionTable par1iInventory, int par2, int par3, int par4) {
        super(par1iInventory, par2, par3, par4);
    }

    @Override
    public boolean isItemValid(ItemStack par1ItemStack) {
        if (par1ItemStack.isEmpty()) {
            return false;
        }
        if (par1ItemStack.getItem() == Items.WRITTEN_BOOK && (par1ItemStack.getTagCompound() == null || !par1ItemStack.getTagCompound().getBoolean("spellFinalized")))
            return true;
        else if (par1ItemStack.getItem() == Items.WRITABLE_BOOK)
            return true;
        else if (par1ItemStack.getItem() == AMItems.spell)
            return true;
        else if (EBWizardryCompatBootstrap.isEBWizSpellBook(par1ItemStack))
            return true;
        return false;
    }

    @Override
    public ItemStack onTake(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack) {
        if (EBWizardryCompatBootstrap.isEBWizBindingBook(par2ItemStack)) {
            // Player is taking the written book output of EBWiz binding workflow – recipe already cleared by createEBWizBindingBook.
            // Nothing more to do; recipe was cleared when the book was created.
        } else if (EBWizardryCompatBootstrap.isEBWizSpellBinding(par2ItemStack)) {
            // Fallback: player took the binding directly without pressing "create" – just clear the recipe.
            ((TileEntityInscriptionTable) this.inventory).clearCurrentRecipe();
        } else if (par2ItemStack.getItem() == Items.WRITTEN_BOOK) {
            par2ItemStack = ((TileEntityInscriptionTable) this.inventory).writeRecipeAndDataToBook(par2ItemStack, par1EntityPlayer, "Spell Recipe");
        } else {
            ((TileEntityInscriptionTable) this.inventory).clearCurrentRecipe();
        }
        super.onTake(par1EntityPlayer, par2ItemStack);
        return par2ItemStack;
    }

    @Override
    public void onSlotChanged() {
        if (this.getStack() != null) {
            Class<? extends Item> clazz = this.getStack().getItem().getClass();
            if (ItemSpellBase.class.isAssignableFrom(clazz)) {
                ((TileEntityInscriptionTable) this.inventory).reverseEngineerSpell(this.getStack());
            }
        }
        super.onSlotChanged();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void putStack(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == Items.WRITABLE_BOOK) {
            ItemStack stack2 = new ItemStack(Items.WRITTEN_BOOK);
            if (stack.hasTagCompound())
                stack2.setTagCompound(stack.getTagCompound());
            stack2.setStackDisplayName(I18n.translateToLocalFormatted("am2.tooltip.unfinishedSpellRecipe"));
            super.putStack(stack2);
        } else {
            super.putStack(stack);
        }
    }
}
