package am2.common.container;

import am2.ArsMagica;
import am2.api.spell.SpellModifier;
import am2.api.spell.SpellModifiers;
import am2.api.spell.SpellPart;
import am2.common.blocks.tileentity.TileEntityInscriptionTable;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.container.slot.SlotEBWizWritableBook;
import am2.common.container.slot.SlotInscriptionTable;
import am2.common.spell.SpellValidator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumSet;

public class ContainerInscriptionTable extends Container {

    // ---- Slot layout indices in inventorySlots list -------------------------
    // When ebwizPreserveSlotEnabled == false:
    //   [0]      book slot
    //   [1..27]  player inventory  (3×9)
    //   [28..36] hotbar            (9)
    //
    // When ebwizPreserveSlotEnabled == true:
    //   [0]      book slot           (shifted left to x=91)
    //   [1]      writable book slot  (x=113)
    //   [2..28]  player inventory    (3×9)
    //   [29..37] hotbar              (9)
    // ------------------------------------------------------------------------

    private final int PLAYER_INVENTORY_START;
    private final int PLAYER_ACTION_BAR_START;
    private final int PLAYER_ACTION_BAR_END;

    /** {@code true} when config EBWiz_Preserve_Spell_Book is enabled. */
    private final boolean ebwizPreserveSlotEnabled;

    private final TileEntityInscriptionTable table;
    private final InventoryPlayer inventoryPlayer;

    public ContainerInscriptionTable(TileEntityInscriptionTable table, InventoryPlayer inventoryplayer) {
        this.table = table;
        this.inventoryPlayer = inventoryplayer;
        this.ebwizPreserveSlotEnabled = ArsMagica.config.getEBWizPreserveSpellBook();

        if (ebwizPreserveSlotEnabled) {
            // Book slot starts centred; the GUI shifts it left when the second slot appears.
            addSlotToContainer(new SlotInscriptionTable(table, 0, 102, 74));
            // Secondary slot for the writable book in EBWiz preserve mode.
            addSlotToContainer(new SlotEBWizWritableBook(
                    table, TileEntityInscriptionTable.ebwizWritableBookIndex, 113, 74));
            PLAYER_INVENTORY_START = 2;
            PLAYER_ACTION_BAR_START = 29;
            PLAYER_ACTION_BAR_END   = 38;
        } else {
            addSlotToContainer(new SlotInscriptionTable(table, 0, 102, 74));
            PLAYER_INVENTORY_START = 1;
            PLAYER_ACTION_BAR_START = 28;
            PLAYER_ACTION_BAR_END   = 37;
        }

        //display player inventory
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
                addSlotToContainer(new Slot(inventoryplayer, k + i * 9 + 9, 30 + k * 18, 170 + i * 18));
            }
        }

        //display player action bar
        for (int j1 = 0; j1 < 9; j1++) {
            addSlotToContainer(new Slot(inventoryplayer, j1, 30 + j1 * 18, 228));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer) {
        return table.isUsableByPlayer(entityplayer);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
        ItemStack itemstack = ItemStack.EMPTY;
        // Fix inscription table: Add bounds checking for slot access
        if (i < 0 || i >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = (Slot) inventorySlots.get(i);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (i < PLAYER_INVENTORY_START) {
                if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (i >= PLAYER_INVENTORY_START && i < PLAYER_ACTION_BAR_START) //from player inventory
            {
                if (!mergeSpecialItems(itemstack1, slot)) {
                    if (!mergeItemStack(itemstack1, PLAYER_ACTION_BAR_START, PLAYER_ACTION_BAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (i >= PLAYER_ACTION_BAR_START && i < PLAYER_ACTION_BAR_END) {
                if (!mergeSpecialItems(itemstack1, slot)) {
                    if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_START - 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else if (!mergeItemStack(itemstack1, PLAYER_INVENTORY_START, PLAYER_ACTION_BAR_END, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() != itemstack.getCount()) {
                slot.onSlotChange(itemstack1, itemstack);
            } else {
                return ItemStack.EMPTY;
            }
        }
        return itemstack;
    }

    private boolean mergeSpecialItems(ItemStack stack, Slot slot) {
        // EBWiz spell book always goes to slot 0.
        if (EBWizardryCompatBootstrap.isEBWizSpellBook(stack)) {
            Slot bookSlot = (Slot) inventorySlots.get(0);
            if (bookSlot.getHasStack()) return false;
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            bookSlot.putStack(newStack);
            bookSlot.onSlotChanged();
            stack.shrink(1);
            if (stack.getCount() == 0) { slot.putStack(ItemStack.EMPTY); slot.onSlotChanged(); }
            return true;
        }

        if (stack.getItem() instanceof ItemWritableBook) {
            // When preserve mode is active and an EBWiz book is in slot 0, route the
            // writable book to the secondary slot (inventorySlots[1]).
            if (ebwizPreserveSlotEnabled && inventorySlots.size() > 1) {
                Slot bookSlot = (Slot) inventorySlots.get(0);
                if (bookSlot.getHasStack() && EBWizardryCompatBootstrap.isEBWizSpellBook(bookSlot.getStack())) {
                    Slot writableSlot = (Slot) inventorySlots.get(1);
                    if (writableSlot.getHasStack()) return false;
                    ItemStack newStack = stack.copy();
                    newStack.setCount(1);
                    writableSlot.putStack(newStack);
                    writableSlot.onSlotChanged();
                    stack.shrink(1);
                    if (stack.getCount() == 0) { slot.putStack(ItemStack.EMPTY); slot.onSlotChanged(); }
                    return true;
                }
            }
            // Otherwise route to slot 0.
            Slot bookSlot = (Slot) inventorySlots.get(0);
            if (bookSlot.getHasStack()) return false;
            ItemStack newStack = stack.copy();
            newStack.setCount(1);
            bookSlot.putStack(newStack);
            bookSlot.onSlotChanged();
            stack.shrink(1);
            if (stack.getCount() == 0) { slot.putStack(ItemStack.EMPTY); slot.onSlotChanged(); }
            return true;
        }

        return false;
    }

    public int getCurrentRecipeSize() {
        return table.getCurrentRecipe().size();
    }

    public boolean currentRecipeContains(SpellPart part) {
        return table.getCurrentRecipe().contains(part);
    }

    public boolean shapeIsAlreadyUsed(SpellPart part) {
        return table.shapeIsAlreadyUsed(part);
    }

    public SpellPart getRecipeItemAt(int index) {
        return table.getCurrentRecipe().get(index);
    }

    public void removeMultipleRecipeParts(int startIndex, int length) {
        table.removeMultipleSpellParts(startIndex, length);
    }

    public void removeSingleRecipePart(int index) {
        table.removeSpellPart(index);
    }

    public void addRecipePart(SpellPart part) {
        table.addSpellPart(part);
    }

    public void addRecipePartToGroup(int groupIndex, SpellPart part) {
        table.addSpellPartToStageGroup(groupIndex, part);
    }

    public void removeSingleRecipePartFromGroup(int groupIndex, int index) {
        table.removeSpellPartFromStageGroup(index, groupIndex);
    }

    public void removeMultipleRecipePartsFromGroup(int groupIndex, int startIndex, int length) {
        table.removeMultipleSpellPartsFromStageGroup(startIndex, length, groupIndex);
    }

    public int getNumStageGroups() {
        return table.getNumStageGroups();
    }

    public int getShapeGroupSize(int groupIndex) {
        return table.getShapeGroupSize(groupIndex);
    }

    public SpellPart getShapeGroupPartAt(int groupIndex, int index) {
        return table.getShapeGroupPartAt(groupIndex, index);
    }

    public String getSpellName() {
        return table.getSpellName();
    }

    public void setSpellName(String name) {
        table.setSpellName(name);
    }

    public void giveSpellToPlayer(EntityPlayer player) {
        table.createSpellForPlayer(player);
    }

    public boolean slotHasStack(int slot) {
        return ((Slot) this.inventorySlots.get(slot)).getHasStack();
    }

    public boolean slotIsBook(int slot) {
        // Fix inscription table: Add null safety to book validation
        if (!slotHasStack(slot) || slot >= inventorySlots.size()) {
            return false;
        }

        ItemStack stack = this.inventorySlots.get(slot).getStack();
        if (stack.isEmpty() || stack.getItem() != Items.WRITTEN_BOOK) {
            return false;
        }

        if (!stack.hasTagCompound()) {
            return false;
        }

        NBTTagCompound nbt = stack.getTagCompound();
        return nbt != null &&
                nbt.hasKey("spellFinalized") &&
                !nbt.getBoolean("spellFinalized");
    }

    public SpellValidator.ValidationResult validateCurrentDefinition() {
        if (isEBWizMode()) {
            if (isEBWizPreserveMode() && inventorySlots.size() >= 2) {
                // In preserve mode the secondary slot must hold a writable-book placeholder
                // (written book with no spellFinalized tag or spellFinalized=false).
                // After "Make Spell" the slot holds the finalized output - the button should
                // be disabled at that point so the player knows to take the book.
                ItemStack secondary = inventorySlots.get(1).getStack();
                boolean hasPlaceholder = !secondary.isEmpty()
                        && secondary.getItem() == Items.WRITTEN_BOOK
                        && (!secondary.hasTagCompound()
                            || !secondary.getTagCompound().getBoolean("spellFinalized"));
                if (!hasPlaceholder) {
                    String msg = secondary.isEmpty()
                            ? "Place a writable book in the second slot"
                            : "Take the binding book and bring it to the Crafting Altar";
                    return SpellValidator.instance.new ValidationResult(null, msg);
                }
            }
            return SpellValidator.instance.new ValidationResult(); // always valid when an EBWiz spell is in the slot
        }
        return table.currentRecipeIsValid();
    }

    public boolean modifierCanBeAdded(SpellModifier modifier) {
        EnumSet<SpellModifiers> modifiers = modifier.getAspectsModified();
        for (SpellModifiers mod : modifiers) {
            if (table.getModifierCount(mod) > 2) {
                return false;
            }
        }
        return true;
    }

    public boolean currentSpellDefIsReadOnly() {
        return table.currentSpellDefIsReadOnly();
    }

    /** Returns {@code true} when the desk holds an EBWiz spell binding. */
    public boolean isEBWizMode() {
        return table.isEBWizMode();
    }

    /** Returns {@code true} when EBWiz preserve-book mode is active. */
    public boolean isEBWizPreserveMode() {
        return table.isEBWizPreserveMode();
    }

    public void resetSpellNameAndIcon() {
        ItemStack stack = this.inventorySlots.get(0).getStack();
        if (!stack.isEmpty()) {
            table.resetSpellNameAndIcon(stack, inventoryPlayer.player);
        }
        this.inventorySlots.get(0).onSlotChanged();
        detectAndSendChanges();
    }
}
