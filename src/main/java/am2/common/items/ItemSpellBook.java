package am2.common.items;

import am2.ArsMagica;
import am2.common.container.InventorySpellBook;
import am2.common.defs.IDDefs;
import am2.common.enchantments.AMEnchantmentHelper;
import am2.common.extensions.SkillData;
import am2.common.registry.AMEnchantments;
import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import am2.api.spell.SpellData;
import am2.common.spell.SpellCaster;
import am2.common.spell.component.Appropriation;
import am2.common.spell.component.PlaceBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@Optional.Interface(iface = "baubles.api.IBauble", modid = "baubles")
public class ItemSpellBook extends Item implements IBauble {

    public static final byte ID_NEXT_SPELL = 0;
    public static final byte ID_PREV_SPELL = 1;

    /**
     * Maps spell book metadata (color variant) to the closest Minecraft chat formatting color.
     */
    private static final TextFormatting[] META_TO_COLOR = {
        TextFormatting.GOLD,         // 0  brown
        TextFormatting.DARK_AQUA,    // 1  cyan
        TextFormatting.GRAY,         // 2  gray
        TextFormatting.BLUE,         // 3  light blue
        TextFormatting.WHITE,        // 4  white
        TextFormatting.DARK_GRAY,    // 5  black  (pure black is invisible on most backgrounds)
        TextFormatting.GOLD,         // 6  orange
        TextFormatting.DARK_PURPLE,  // 7  purple
        TextFormatting.DARK_BLUE,    // 8  blue
        TextFormatting.DARK_GREEN,   // 9  green
        TextFormatting.YELLOW,       // 10 yellow
        TextFormatting.RED,          // 11 red
        TextFormatting.GREEN,        // 12 lime
        TextFormatting.LIGHT_PURPLE, // 13 pink
        TextFormatting.LIGHT_PURPLE, // 14 magenta
        TextFormatting.GRAY,         // 15 light gray
    };

//	private final String[] npc_textureFiles = {"affinity_tome_general", "affinity_tome_ice", "affinity_tome_life", "affinity_tome_fire", "affinity_tome_lightning", "affinity_tome_ender"};
//	private final String[] player_textureFiles = {"spell_book_cover", "spell_book_decoration"};

    public ItemSpellBook() {
        super();
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return super.getItemStackDisplayName(stack);
        }

        String colorPrefix = "\u00a77"; // default gray
        if (ArsMagica.config.getColoredSpellBookNames()) {
            int meta = stack.getItemDamage();
            if (meta >= 0 && meta < META_TO_COLOR.length) {
                colorPrefix = META_TO_COLOR[meta].toString();
            }
        }

        ItemStack activeSpell = getActiveItemStack(stack);
        if (!activeSpell.isEmpty()) {
            return String.format("%s%s (%s%s)", colorPrefix, I18n.translateToLocalFormatted("item.arsmagica2:spellbook.name"), activeSpell.getDisplayName(), colorPrefix);
        }
        return colorPrefix + I18n.translateToLocalFormatted("item.arsmagica2:spellbook.name");
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        if (getMaxItemUseDuration(itemstack) == 0) {
            return EnumAction.NONE;
        }
        return EnumAction.BOW;
    }

    @Override
    public final int getMaxItemUseDuration(ItemStack itemstack) {
        ItemSpellBase scroll = GetActiveScroll(itemstack);
        if (scroll != null) {
            return scroll.getMaxItemUseDuration(itemstack);
        }
        return 0;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.openGui(ArsMagica.instance, IDDefs.GUI_SPELL_BOOK, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            }
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        player.setActiveHand(hand);

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private static ItemStack[] getMyInventory(ItemStack itemStack) {
        return readFromStackTagCompound(itemStack);
    }

    public ItemStack[] getActiveScrollInventory(ItemStack bookStack) {
        ItemStack[] inventoryItems = getMyInventory(bookStack);
        ItemStack[] returnArray = new ItemStack[8];
        for (int i = 0; i < 8; ++i) {
            returnArray[i] = inventoryItems[i];
        }
        return returnArray;
    }

    public ItemSpellBase GetActiveScroll(ItemStack bookStack) {
        ItemStack[] inventoryItems = getMyInventory(bookStack);
        if (inventoryItems[getActiveSlot(bookStack)].isEmpty()) {
            return null;
        }
        return (ItemSpellBase) inventoryItems[getActiveSlot(bookStack)].getItem();
    }

    public ItemStack getActiveItemStack(ItemStack bookStack) {
        ItemStack[] inventoryItems = getMyInventory(bookStack);
        if (inventoryItems[getActiveSlot(bookStack)].isEmpty()) {
            return ItemStack.EMPTY;
        }
        return inventoryItems[getActiveSlot(bookStack)].copy();
    }

    public void replaceActiveItemStack(ItemStack bookStack, ItemStack newstack) {
        ItemStack[] inventoryItems = getMyInventory(bookStack);
        int index = getActiveSlot(bookStack);
        inventoryItems[index] = newstack;
        updateStackTagCompound(bookStack, inventoryItems);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving.isSneaking() && entityLiving instanceof EntityPlayer) {
            if (!world.isRemote) {
                ((EntityPlayer) entityLiving).openGui(ArsMagica.instance, IDDefs.GUI_SPELL_BOOK, world, (int) entityLiving.posX, (int) entityLiving.posY, (int) entityLiving.posZ);
            }
        } else {
            ItemStack currentSpellStack = getActiveItemStack(stack);
            if (!currentSpellStack.isEmpty()) {
                // Dispatch to the active scroll's own item so that subclasses
                // (e.g. ItemEBWizSpellBinding) can handle casting differently.
                currentSpellStack.getItem().onPlayerStoppedUsing(currentSpellStack, world, entityLiving, timeLeft);
                persistAppropriationData(stack, currentSpellStack);
            }
        }
    }

    /** Spell stack tags that components modify during a cast and that must survive in the book. */
    private static final String[] CAST_PERSISTED_KEYS = {Appropriation.storageKey, PlaceBlock.KEY_STATE, "Lore"};

    /**
     * Spells are cast from a copy of the book's active stack, so anything Appropriation
     * stores on (or removes from) that copy has to be written back to the book,
     * otherwise the captured block/entity is lost and can never be placed again.
     */
    public void persistAppropriationData(ItemStack bookStack, ItemStack castStack) {
        if (bookStack.isEmpty() || castStack.isEmpty()) return;
        persistToSlot(bookStack, getActiveSlot(bookStack), castStack);
    }

    /**
     * For spells that apply their components later than the cast (projectiles, orbs, runes...),
     * the book's stack is long gone. Finds the spell in the caster's book by UUID and writes the
     * changed tags back to it. Does nothing if the spell did not come from a spell book.
     */
    public static void persistFromSpell(EntityLivingBase caster, SpellData spell) {
        if (!(caster instanceof EntityPlayer) || caster.world.isRemote) return;
        ItemStack source = spell.getSource();
        if (source.isEmpty()) return;

        ItemStack bookStack = findSpellBook((EntityPlayer) caster);
        if (bookStack.isEmpty() || !(bookStack.getItem() instanceof ItemSpellBook)) return;

        ItemStack[] slots = getMyInventory(bookStack);
        for (int i = 0; i < slots.length; i++) {
            ItemStack slot = slots[i];
            if (slot.isEmpty() || !slot.hasCapability(SpellCaster.INSTANCE, null)) continue;
            if (spell.getUUID().equals(SpellCaster.of(slot).getSpellUUID())) {
                ((ItemSpellBook) bookStack.getItem()).persistToSlot(bookStack, i, source);
                return;
            }
        }
    }

    private void persistToSlot(ItemStack bookStack, int slotIndex, ItemStack castStack) {
        ItemStack[] slots = getMyInventory(bookStack);
        if (slotIndex < 0 || slotIndex >= slots.length) return;
        ItemStack stored = slots[slotIndex].copy();
        if (stored.isEmpty()) return;

        NBTTagCompound castTag = castStack.getTagCompound();
        boolean changed = false;

        for (String key : CAST_PERSISTED_KEYS) {
            NBTTagCompound storedTag = stored.getTagCompound();
            boolean castHas = castTag != null && castTag.hasKey(key);
            boolean storedHas = storedTag != null && storedTag.hasKey(key);

            if (castHas) {
                if (storedHas && castTag.getTag(key).equals(storedTag.getTag(key))) continue;
                if (storedTag == null) {
                    storedTag = new NBTTagCompound();
                    stored.setTagCompound(storedTag);
                }
                storedTag.setTag(key, castTag.getTag(key).copy());
                changed = true;
            } else if (storedHas) {
                storedTag.removeTag(key);
                changed = true;
            }
        }

        if (changed) {
            slots[slotIndex] = stored;
            updateStackTagCompound(bookStack, slots);
        }
    }

    public void updateStackTagCompound(ItemStack itemStack, ItemStack[] values) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        NonNullList<ItemStack> nonNullList = NonNullList.<ItemStack>withSize(values.length, ItemStack.EMPTY);
        for (int i = 0; i < values.length; i++) {
            nonNullList.set(i, values[i]);
        }
        ItemStackHelper.saveAllItems(itemStack.getTagCompound(), nonNullList);

        mirrorActiveSpellEnchantments(itemStack);
    }

    /** Persistent flag so soulbound survives anything that rewrites the book's enchantment list. */
    private static final String KEY_SOULBOUND = "SpellbookSoulbound";

    private static boolean isSoulbound(ItemStack book) {
        if (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, book) > 0) return true;
        return book.hasTagCompound() && book.getTagCompound().getBoolean(KEY_SOULBOUND);
    }

    /**
     * Makes the book carry the active spell's enchantments (as in 1.7.10) while never losing
     * its own soulbound enchantment, which is remembered in {@link #KEY_SOULBOUND}.
     */
    private void mirrorActiveSpellEnchantments(ItemStack book) {
        boolean soulbound = isSoulbound(book);
        ItemStack active = getActiveItemStack(book);
        if (!active.isEmpty())
            AMEnchantmentHelper.copyEnchantments(active, book);
        if (soulbound)
            ensureSoulbound(book);
    }

    private static void ensureSoulbound(ItemStack book) {
        if (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, book) <= 0)
            AMEnchantmentHelper.soulbindStack(book);
        if (!book.hasTagCompound()) book.setTagCompound(new NBTTagCompound());
        book.getTagCompound().setBoolean(KEY_SOULBOUND, true);
    }

    /** Restores soulbound if the enchantment went missing from a book that was soulbound before. */
    private static void healSoulbound(ItemStack book) {
        if (book.hasTagCompound() && book.getTagCompound().getBoolean(KEY_SOULBOUND)
                && EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, book) <= 0) {
            AMEnchantmentHelper.soulbindStack(book);
        }
    }

    public void setActiveSlot(ItemStack itemStack, int slot) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (slot < 0) slot = 0;
        if (slot > 7) slot = 7;
        itemStack.getTagCompound().setInteger("SpellbookActiveSlot", slot);

        mirrorActiveSpellEnchantments(itemStack);
    }

    public int setNextSlot(ItemStack itemStack) {
        int slot = getActiveSlot(itemStack);
        int newSlot = slot;

        do {
            newSlot++;
            if (newSlot > 7) newSlot = 0;
            setActiveSlot(itemStack, newSlot);
        } while (GetActiveScroll(itemStack) == null && newSlot != slot);
        return slot;
    }

    public int setPrevSlot(ItemStack itemStack) {
        int slot = getActiveSlot(itemStack);
        int newSlot = slot;

        do {
            newSlot--;
            if (newSlot < 0) newSlot = 7;
            setActiveSlot(itemStack, newSlot);
        } while (GetActiveScroll(itemStack) == null && newSlot != slot);
        return slot;
    }

    public int getActiveSlot(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            setActiveSlot(itemStack, 0);
            return 0;
        }
        return itemStack.getTagCompound().getInteger("SpellbookActiveSlot");
    }

    public static ItemStack[] readFromStackTagCompound(ItemStack itemStack) {
        NonNullList<ItemStack> items = NonNullList.withSize(InventorySpellBook.inventorySize, ItemStack.EMPTY);
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("Items")) {
            ItemStackHelper.loadAllItems(itemStack.getTagCompound(), items);
        }
        ItemStack[] list = new ItemStack[InventorySpellBook.inventorySize];
        for (int i = 0; i < items.size(); i++) {
            list[i] = items.get(i);
        }

        return list;
    }

    public static InventorySpellBook getInventory(ItemStack bookStack) {
        InventorySpellBook isb = new InventorySpellBook();
        isb.setBagStack(bookStack);
        isb.SetInventoryContents(getMyInventory(bookStack));
        return isb;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    public String GetActiveSpellName(ItemStack bookStack) {
        ItemStack stack = getActiveItemStack(bookStack);
        if (stack.isEmpty()) {
            return I18n.translateToLocalFormatted("am2.tooltip.none");
        }
        return stack.getDisplayName();
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (world == null) {
            return;
        }
        ItemSpellBase activeScroll = GetActiveScroll(stack);

        String s = I18n.translateToLocalFormatted("am2.tooltip.open");
        String s2 = I18n.translateToLocalFormatted("am2.tooltip.scroll");
        tooltip.add((new StringBuilder()).append("\2477").append(s).toString());
        tooltip.add((new StringBuilder()).append("\2477").append(s2).toString());
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        ItemStack scrollStack = getActiveItemStack(stack);
        if (!scrollStack.isEmpty()) {
            // Dispatch to the active scroll's own item (same pattern as onPlayerStoppedUsing).
            scrollStack.getItem().onUsingTick(scrollStack, player, count);
            persistAppropriationData(stack, scrollStack);
        }
    }

    @Override
    public boolean isBookEnchantable(ItemStack bookStack, ItemStack enchantBook) {
        Map<Enchantment, Integer> enchantMap = EnchantmentHelper.getEnchantments(enchantBook);
        for (Enchantment o : enchantMap.keySet()) {
            if (o == AMEnchantments.soulbound) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    public boolean isItemTool(ItemStack par1ItemStack) {
        return true;
    }


    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);
        if (!world.isRemote) {
            // Remember soulbound on the book itself and put it back if the enchantment list got rewritten.
            if (EnchantmentHelper.getEnchantmentLevel(AMEnchantments.soulbound, stack) > 0)
                ensureSoulbound(stack);
            else
                healSoulbound(stack);
        } else {
            clientUpdate(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    private void clientUpdate(Entity entity) {
        if (entity instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) entity;
            ItemStack usingItem = player.getActiveItemStack();
            if (!usingItem.isEmpty() && usingItem.getItem() == this) {
                if (SkillData.For(player).hasSkill("spell_motion")) {
                    player.movementInput.moveForward *= 2.5F;
                    player.movementInput.moveStrafe *= 2.5F;
                }
            }
        }
    }

    // ---- Baubles integration ----

    @Override
    @Optional.Method(modid = "baubles")
    public BaubleType getBaubleType(ItemStack itemstack) {
        return BaubleType.CHARM;
    }

    /**
     * Finds a spell book: checks main hand first, then baubles charm slot.
     * Returns ItemStack.EMPTY if no spell book is found.
     */
    public static ItemStack findSpellBook(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ItemSpellBook) {
            return mainHand;
        }
        if (Loader.isModLoaded("baubles")) {
            return findSpellBookInBaubles(player);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns true if the player's spell book (as found by {@link #findSpellBook}) is
     * in the baubles slot rather than in the main hand.
     */
    public static boolean isSpellBookInBaubles(EntityPlayer player) {
        ItemStack mainHand = player.getHeldItemMainhand();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof ItemSpellBook) {
            return false;
        }
        if (!Loader.isModLoaded("baubles")) return false;
        return !findSpellBookInBaubles(player).isEmpty();
    }

    /**
     * Writes the spell book ItemStack back to the baubles slot.
     * Used by ContainerSpellBook on close when the book was opened from baubles.
     */
    public static void writeSpellBookToBaubles(EntityPlayer player, ItemStack bookStack) {
        if (!Loader.isModLoaded("baubles")) return;
        writeSpellBookToBaublesImpl(player, bookStack);
    }

    @Optional.Method(modid = "baubles")
    private static ItemStack findSpellBookInBaubles(EntityPlayer player) {
        IItemHandler handler = BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSpellBook) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Optional.Method(modid = "baubles")
    private static void writeSpellBookToBaublesImpl(EntityPlayer player, ItemStack bookStack) {
        IItemHandlerModifiable handler =
                (IItemHandlerModifiable) BaublesApi.getBaublesHandler(player);
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemSpellBook) {
                handler.setStackInSlot(i, bookStack);
                return;
            }
        }
    }
}









