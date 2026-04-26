package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.SpellRegistryHelper;
import am2.api.affinity.Affinity;
import am2.api.event.SpellRecipeItemsEvent;
import am2.api.extensions.ISpellCaster;
import am2.api.skill.Skill;
import am2.api.spell.*;
import am2.common.LogHelper;
import am2.common.blocks.BlockInscriptionTable;
import am2.common.compat.electroblob.EBWizardryCompatBootstrap;
import am2.common.compat.electroblob.item.ItemEBWizSpellBinding;
import am2.common.container.ContainerInscriptionTable;
import am2.common.lore.Story;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCaster;
import am2.common.spell.SpellValidator;
import am2.common.spell.modifier.IEBWizExclusive;
import am2.common.utils.KeyValuePair;
import am2.common.utils.NBTUtils;
import am2.common.utils.RecipeUtils;
import am2.common.utils.SpellUtils;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketInscriptionTableUpdate;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;

public class TileEntityInscriptionTable extends TileEntity implements IInventory, ITickable, ITileEntityAMBase {

    private NonNullList<ItemStack> inventory;
    private final ArrayList<SpellPart> currentRecipe;
    private final ArrayList<ArrayList<SpellPart>> shapeGroups;
    private int numStageGroups = 2;
    private boolean dirty = false;
    public static int getMaxStageGroups() {
        return ArsMagica.config.getMaxStageGroups();
    }
    public static int bookIndex = 0;
    public static int paperIndex = 1;
    public static int featherIndex = 2;
    public static int inkIndex = 3;
    /** Slot index for the writable book in EBWiz-preserve mode (config: EBWiz_Preserve_Spell_Book). */
    public static int ebwizWritableBookIndex = 4;
    private EntityPlayer currentPlayerUsing;
    private final HashMap<SpellModifiers, Integer> modifierCount;
    private String currentSpellName;
    private boolean currentSpellIsReadOnly;

    private static final byte FULL_UPDATE = 0x1;
    private static final byte MAKE_SPELL = 0x2;
    private static final byte RESET_NAME = 0x4;

    public TileEntityInscriptionTable() {
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        this.currentPlayerUsing = null;
        this.currentSpellName = "";
        this.currentRecipe = new ArrayList<>();
        this.shapeGroups = new ArrayList<>();

        for (int i = 0; i < getMaxStageGroups(); ++i) {
            this.shapeGroups.add(new ArrayList<>());
        }

        this.modifierCount = new HashMap<>();
        this.resetModifierCount();
    }

    public ArrayList<SpellPart> getCurrentRecipe() {
        return this.currentRecipe;
    }

    @Override
    public int getSizeInventory() {
        // Always allocate 5 slots; slot 4 is the writable-book slot used in EBWiz preserve mode.
        return 5;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory.get(slot);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.inventory, slot, amount);
        if (!itemstack.isEmpty()) {
            this.markDirty();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {

        ItemStack stack = getStackInSlot(slot);

        if (!stack.isEmpty()) {
            setInventorySlotContents(slot, ItemStack.EMPTY);
        }

        return stack;
    }

    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        if (stack.isEmpty()) {
            stack = ItemStack.EMPTY;
        }
        this.inventory.set(index, stack);
        if (stack.getCount() > this.getInventoryStackLimit()) {
            stack.setCount(this.getInventoryStackLimit());
        }

        this.markDirty();
    }


    @Override
    public String getName() {
        return "Inscription Table";
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSqToCenter(this.pos) <= 64D;
    }

    public boolean isInUse(EntityPlayer player) {
        return this.currentPlayerUsing != null && this.currentPlayerUsing.getEntityId() != player.getEntityId();
    }

    public void setInUse(EntityPlayer player) {
        this.currentPlayerUsing = player;
        if (!this.world.isRemote) {
            this.markDirty();
            //world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
        }
    }

    public EntityPlayer getCurrentPlayerUsing() {
        return this.currentPlayerUsing;
    }

    @Override
    public void update() {
        if (this.world.getBlockState(this.pos).getBlock() != AMBlocks.inscription_table) {
            this.invalidate();
            return;
        }
        if (this.numStageGroups > getMaxStageGroups())
            this.numStageGroups = getMaxStageGroups();
        //if (!this.world.isRemote) {
        boolean shouldSet = false;
        IBlockState state = this.world.getBlockState(this.pos);
        if (this.getUpgradeState() >= 1 && !state.getValue(BlockInscriptionTable.TIER_1)) {
            shouldSet = true;
        } else if (this.getUpgradeState() >= 2 && !state.getValue(BlockInscriptionTable.TIER_2)) {
            shouldSet = true;
        } else if (this.getUpgradeState() >= 3 && !state.getValue(BlockInscriptionTable.TIER_3)) {
            shouldSet = true;
        }
        if (shouldSet)
            this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).withProperty(BlockInscriptionTable.TIER_1, this.getUpgradeState() >= 1).withProperty(BlockInscriptionTable.TIER_2, this.getUpgradeState() >= 2).withProperty(BlockInscriptionTable.TIER_3, this.getUpgradeState() >= 3), 2);
        //}
        this.markDirty();
        //world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 3);
    }

    public int getUpgradeState() {
        return this.numStageGroups - 2;
    }



    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
        super.readFromNBT(par1NBTTagCompound);
        this.clearCurrentRecipe();
        this.parseTagCompound(par1NBTTagCompound);
    }

    private void parseTagCompound(NBTTagCompound nbttagcompound) {
        ItemStackHelper.loadAllItems(nbttagcompound, this.inventory);

        this.shapeGroups.clear();
        NBTTagList shapeGroups = nbttagcompound.getTagList("ShapeGroups", Constants.NBT.TAG_LIST);
        for (int i = 0; i < shapeGroups.tagCount(); i++) {
            NBTTagList tmplist = (NBTTagList) shapeGroups.get(i);
            ArrayList<SpellPart> parts = new ArrayList<>();
            for (int j = 0; j < tmplist.tagCount(); j++) {
                NBTTagCompound tmp = tmplist.getCompoundTagAt(j);
                parts.add(tmp.getInteger("Slot"), ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tmp.getString("ID"))));
            }
            this.shapeGroups.add(parts);
        }
        this.currentRecipe.clear();
        NBTTagList recipe = nbttagcompound.getTagList("CurrentRecipe", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < recipe.tagCount(); i++) {
            NBTTagCompound tmp = recipe.getCompoundTagAt(i);
            this.currentRecipe.add(tmp.getInteger("Slot"), ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation(tmp.getString("ID"))));
        }
        this.numStageGroups = Math.max(nbttagcompound.getInteger("numShapeGroupSlots"), 2);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        ItemStackHelper.saveAllItems(nbttagcompound, this.inventory);

        NBTTagList recipe = new NBTTagList();
        for (int i = 0; i < this.currentRecipe.size(); i++) {
            NBTTagCompound tmp = new NBTTagCompound();
            tmp.setInteger("Slot", i);
            tmp.setString("ID", SpellRegistryHelper.getSkillFromPart(this.currentRecipe.get(i)).getID());
            recipe.appendTag(tmp);
        }
        NBTTagList shapeGroups = new NBTTagList();
        for (int j = 0; j < this.shapeGroups.size(); j++) {
            ArrayList<SpellPart> parts = this.shapeGroups.get(j);
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < parts.size(); i++) {
                NBTTagCompound tmp = new NBTTagCompound();
                tmp.setInteger("Slot", i);
                tmp.setString("ID", SpellRegistryHelper.getSkillFromPart(parts.get(i)).getID());
                list.appendTag(tmp);
            }
            shapeGroups.appendTag(list);
        }
        nbttagcompound.setTag("ShapeGroups", shapeGroups);
        nbttagcompound.setTag("CurrentRecipe", recipe);
        nbttagcompound.setInteger("numShapeGroupSlots", this.numStageGroups);
        return nbttagcompound;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    public void handleUpdatePacket(byte[] data) {
        if (this.world == null)
            return;
        AMDataReader rdr = new AMDataReader(data);
        switch (rdr.ID) {
            case FULL_UPDATE:
                if (!rdr.getBoolean()) {
                    Entity e = this.world.getEntityByID(rdr.getInt());
                    if (e instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) e;
                        this.setInUse(player);
                    } else {
                        this.setInUse(null);
                    }
                } else {
                    this.setInUse(null);
                }

                this.currentRecipe.clear();
                int partLength = rdr.getInt();
                for (int i = 0; i < partLength; ++i) {
                    Skill part = Skill.byNetworkID(rdr.getInt());
                    SpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName());
                    if (spellPart != null)
                        this.currentRecipe.add(spellPart);
                }

                this.shapeGroups.clear();
                int numGroups = rdr.getInt();
                for (int i = 0; i < numGroups; ++i) {
                    ArrayList<SpellPart> group = new ArrayList<>();
                    int[] partData = rdr.getIntArray();
                    for (int n : partData) {

                        Skill part = Skill.byNetworkID(n);
                        SpellPart spellPart = ArsMagicaAPI.getSpellRegistry().getValue(part.getRegistryName());
                        if (spellPart != null)
                            group.add(spellPart);
                    }
                    this.shapeGroups.add(group);
                }

                this.countModifiers();
                this.currentSpellName = rdr.getString();
                this.currentSpellIsReadOnly = rdr.getBoolean();
                this.numStageGroups = rdr.getInt();
                break;
            case MAKE_SPELL:
                int entityID = rdr.getInt();
                EntityPlayer player = (EntityPlayer) this.world.getEntityByID(entityID);
                if (player != null) {
                    this.createSpellForPlayer(player);
                }
                break;
            case RESET_NAME:
                entityID = rdr.getInt();
                player = (EntityPlayer) this.world.getEntityByID(entityID);
                if (player != null) {
                    ((ContainerInscriptionTable) player.openContainer).resetSpellNameAndIcon();
                }
                break;
        }
    }

    private byte[] GetUpdatePacketForServer() {
        AMDataWriter writer = new AMDataWriter();
        writer.add(FULL_UPDATE);
        writer.add(this.currentPlayerUsing == null);
        if (this.currentPlayerUsing != null) writer.add(this.currentPlayerUsing.getEntityId());

        writer.add(this.currentRecipe.size());
        for (int i = 0; i < this.currentRecipe.size(); ++i) {
            writer.add(this.currentRecipe.get(i).getSkill().networkID());
        }


        writer.add(this.shapeGroups.size());
        for (ArrayList<SpellPart> shapeGroup : this.shapeGroups) {
            int[] groupData = new int[shapeGroup.size()];
            for (int i = 0; i < shapeGroup.size(); ++i) {
                groupData[i] = shapeGroup.get(i).getSkill().networkID();
            }
            writer.add(groupData);
        }

        writer.add(this.currentSpellName);
        writer.add(this.currentSpellIsReadOnly);
        writer.add(this.numStageGroups);

        return writer.generate();
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return new SPacketUpdateTileEntity(this.getPos(), this.getBlockMetadata(), compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.parseTagCompound(pkt.getNbtCompound());
    }

    private void sendDataToServer() {
        AMNetworkHandler.getNetwork().sendToServer(new PacketInscriptionTableUpdate(this.getPos(), this.GetUpdatePacketForServer()));
    }

    public void addSpellPartToStageGroup(int groupIndex, SpellPart part) {
        ArrayList<SpellPart> group = this.shapeGroups.get(groupIndex);
        if (!this.currentSpellIsReadOnly && group.size() < 4 && !(part instanceof SpellComponent)) {
            group.add(part);
            if (this.world.isRemote)
                this.sendDataToServer();
            this.countModifiers();
        }
    }

    public void removeSpellPartFromStageGroup(int index, int groupIndex) {
        ArrayList<SpellPart> group = this.shapeGroups.get(groupIndex);
        if (!this.currentSpellIsReadOnly) {
            group.remove(index);
            if (this.world.isRemote)
                this.sendDataToServer();
            this.countModifiers();
        }
    }

    public void removeMultipleSpellPartsFromStageGroup(int startIndex, int length, int groupIndex) {
        ArrayList<SpellPart> group = this.shapeGroups.get(groupIndex);
        if (!this.currentSpellIsReadOnly) {
            for (int i = 0; i <= length; ++i) {
                if (startIndex < group.size())
                    group.remove(startIndex);
            }
            this.countModifiers();
            if (this.world.isRemote)
                this.sendDataToServer();
        }
    }

    public void addSpellPart(SpellPart part) {
        if (!this.currentSpellIsReadOnly && this.currentRecipe.size() < ArsMagica.config.getMaxRecipeSize()) {
            this.currentRecipe.add(part);
            if (this.world.isRemote)
                this.sendDataToServer();
            this.countModifiers();
        }
    }

    public void removeSpellPart(int index) {
        if (!this.currentSpellIsReadOnly) {
            this.currentRecipe.remove(index);
            if (this.world.isRemote)
                this.sendDataToServer();
            this.countModifiers();
        }
    }

    public void removeMultipleSpellParts(int startIndex, int length) {
        if (!this.currentSpellIsReadOnly) {
            for (int i = 0; i <= length; ++i) {
                if (startIndex < this.currentRecipe.size())
                    this.currentRecipe.remove(startIndex);
            }
            this.countModifiers();
            if (this.world.isRemote)
                this.sendDataToServer();
        }
    }

    public int getNumStageGroups() {
        return this.numStageGroups;
    }

    private void countModifiers() {

        this.resetModifierCount();

        for (ArrayList<SpellPart> shapeGroup : this.shapeGroups) {
            this.countModifiersInList(shapeGroup);
        }

        ArrayList<ArrayList<SpellPart>> stages = SpellValidator.splitToStages(this.currentRecipe);
        if (stages.isEmpty()) return;

        for (ArrayList<SpellPart> currentStage : stages) {
            this.countModifiersInList(currentStage);
        }
        //ArrayList<SpellPart> currentStage = stages.get(stages.size() - 1);
        //countModifiersInList(currentStage);
    }

    private void countModifiersInList(ArrayList<SpellPart> currentStage) {
        for (SpellPart part : currentStage) {
            if (part instanceof SpellModifier) {
                EnumSet<SpellModifiers> modifiers = ((SpellModifier) part).getAspectsModified();
                for (SpellModifiers modifier : modifiers) {
                    int count = this.modifierCount.get(modifier) + 1;
                    this.modifierCount.put(modifier, count);
                }
            }
        }
    }

    private void resetModifierCount() {
        this.modifierCount.clear();
        for (SpellModifiers modifier : SpellModifiers.values()) {
            this.modifierCount.put(modifier, 0);
        }
    }

    public int getModifierCount(SpellModifiers modifier) {
        return this.modifierCount.get(modifier);
    }

    public void createSpellForPlayer(EntityPlayer player) {
        if (this.world.isRemote) {
            AMDataWriter writer = new AMDataWriter();
            writer.add(MAKE_SPELL);
            writer.add(player.getEntityId());
            AMNetworkHandler.getNetwork().sendToServer(new PacketInscriptionTableUpdate(this.getPos(), writer.generate()));
        } else {

            // EBWiz mode: convert the binding in slot 0 into a Written Book that encodes
            // the spell + AM2 modifier counts. The player then takes this book to the Crafting
            // Altar and throws a blank rune to finalize it into the ItemEBWizSpellBinding.
            if (isEBWizMode()) {
                ItemStack binding = this.getStackInSlot(0);
                if (!binding.isEmpty()) {
                    // Creative mode shortcut: skip the Written Book intermediate and produce the
                    // final ItemEBWizSpellBinding immediately without visiting the Crafting Altar.
                    if (player.capabilities.isCreativeMode) {
                        ItemStack finalBinding = EBWizardryCompatBootstrap.convertEBWizSpellBook(binding);
                        if (!finalBinding.isEmpty()) {
                            this.setInventorySlotContents(0, finalBinding);
                            this.markDirty();
                            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                                    "In creative mode the spell item is created instantly, without the recipe book"));
                            LogHelper.info("Creative mode: directly created EBWiz spell binding from book");
                        } else {
                            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                                    "\u00a7c[Creative] Failed to convert EBWiz spell book into a binding item."));
                            LogHelper.warn("Creative mode: failed to convert EBWiz spell book to binding item");
                        }
                        return;
                    }
                    if (isEBWizPreserveMode()) {
                        // Preserve-mode: write the binding book into the secondary slot (4)
                        // and keep the EBWiz spell book in slot 0 untouched.
                        ItemStack writableSlot = this.getStackInSlot(ebwizWritableBookIndex);
                        // Only proceed when the slot holds the un-finalised written-book placeholder.
                        boolean isPlaceholder = !writableSlot.isEmpty()
                                && writableSlot.getItem() == Items.WRITTEN_BOOK
                                && (!writableSlot.hasTagCompound()
                                    || !writableSlot.getTagCompound().getBoolean("spellFinalized"));
                        if (!isPlaceholder) {
                            LogHelper.warn("EBWiz preserve mode: secondary slot does not have a writable book placeholder – cannot create binding book");
                            return;
                        }
                        ItemStack book = createEBWizBindingBook(binding);
                        this.setInventorySlotContents(ebwizWritableBookIndex, book);
                        // Slot 0 is intentionally left unchanged.
                        this.markDirty();
                        LogHelper.info("EBWiz binding book written to secondary slot; original spell book preserved.");
                    } else {
                        ItemStack book = createEBWizBindingBook(binding);
                        this.setInventorySlotContents(0, book);
                        this.markDirty();
                        LogHelper.info("EBWiz binding converted to written book – take it from the desk and finalize at the Crafting Altar");
                    }
                }
                return;
            }

            // Fix inscription table: Add validation before spell creation
            if (this.currentRecipe == null || this.currentRecipe.isEmpty()) {
                LogHelper.warn("Cannot create spell: recipe is empty");
                return;
            }

            if (!this.currentRecipeIsValid().valid) {
                LogHelper.warn("Cannot create spell: recipe is invalid - %s", this.currentRecipeIsValid().message);
                return;
            }

            // Fix for creative mode: Check if there's a book in the slot (or allow empty slot in creative mode)
            ItemStack bookStack = this.getStackInSlot(0);
            boolean hasValidBook = !bookStack.isEmpty() &&
                    (bookStack.getItem() == Items.WRITABLE_BOOK || bookStack.getItem() == Items.WRITTEN_BOOK);

            // In creative mode, we allow crafting without a book (it will just place the spell in the slot)
            // In survival mode, a book is required
            if (!player.capabilities.isCreativeMode && !hasValidBook) {
                LogHelper.warn("Cannot create spell: no valid book in inscription table");
                return;
            }

            LogHelper.info("Creating spell for player in creative mode");

            ArrayList<KeyValuePair<ArrayList<SpellPart>, NBTTagCompound>> shapeGroupSetup = new ArrayList<>();
            KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> curRecipeSetup = new KeyValuePair<>(this.currentRecipe, new NBTTagCompound());

            for (ArrayList<SpellPart> arr : this.shapeGroups) {
                if (arr != null) { // Fix inscription table: Add null check
                    shapeGroupSetup.add(new KeyValuePair<>(arr, new NBTTagCompound()));
                }
            }
            ItemStack stack = new ItemStack(AMItems.spell);
            if (!stack.hasTagCompound())
                stack.setTagCompound(new NBTTagCompound());
            ISpellCaster caster = SpellCaster.of(stack);
            if (caster != null) {
                caster.setSpellCommon(SpellUtils.transformParts(curRecipeSetup.key));
                caster.setCommonStoredData(curRecipeSetup.value);
                List<List<List<SpellPart>>> shapeGroups = Lists.newArrayList();
                for (int i = 0; i < shapeGroupSetup.size(); i++) {
                    KeyValuePair<ArrayList<SpellPart>, NBTTagCompound> entry = shapeGroupSetup.get(i);
                    shapeGroups.add(SpellUtils.transformParts(entry.key));
                    caster.setStoredData(i, entry.value);
                }
                caster.setShapeGroups(shapeGroups);
                // Write mana cost into the regular tagCompound so it is always synced
                // to the client automatically (capabilities are not synced by default).
                stack.getTagCompound().setFloat(am2.common.items.ItemSpellBase.KEY_MANA_COST_CACHED,
                        caster.getBaseManaCost(caster.getCurrentShapeGroup()));
            }

            stack.getTagCompound().setString("suggestedName", this.currentSpellName);
            stack.getTagCompound().setString("creatorName", player.getName());

            // Fix for creative mode: Replace the book in the slot with the spell item
            this.setInventorySlotContents(0, stack);
            this.markDirty();
            LogHelper.info("Successfully created spell and placed in slot");
        }
    }

    public ItemStack writeRecipeAndDataToBook(ItemStack bookstack, EntityPlayer player, String title) {
        if (bookstack.getItem() == Items.WRITTEN_BOOK && this.currentRecipe != null) {
            if (!this.currentRecipeIsValid().valid)
                return bookstack;

            if (!bookstack.hasTagCompound())
                bookstack.setTagCompound(new NBTTagCompound());
            else if (bookstack.getTagCompound().getBoolean("spellFinalized")) //don't overwrite a completed spell
                return bookstack;

            LinkedHashMap<String, Integer> materialsList = new LinkedHashMap<String, Integer>();

            materialsList.put(AMItems.blank_rune.getItemStackDisplayName(new ItemStack(AMItems.blank_rune)), 1);

            ArrayList<ItemStack> componentRecipeList = new ArrayList<ItemStack>();
            ArrayList<SpellPart> allRecipeItems = new ArrayList<SpellPart>();

            for (ArrayList<SpellPart> shapeGroup : this.shapeGroups) {
                if (shapeGroup == null || shapeGroup.isEmpty())
                    continue;
                allRecipeItems.addAll(shapeGroup);
            }

            allRecipeItems.addAll(this.currentRecipe);
            for (SpellPart part : allRecipeItems) {

                if (part == null) {
                    LogHelper.error("Unable to write recipe to book.  Recipe part is null!");
                    return bookstack;
                }

                Object[] recipeItems = part.getEffectiveRecipe();
                SpellRecipeItemsEvent event = new SpellRecipeItemsEvent(SpellRegistryHelper.getSkillFromPart(part).getID(), recipeItems);
                MinecraftForge.EVENT_BUS.post(event);
                recipeItems = event.recipeItems;

                if (recipeItems == null) {
                    LogHelper.error("Unable to write recipe to book.  Recipe items are null for part %s!", SpellRegistryHelper.getSkillFromPart(part).getName());
                    return bookstack;
                }
                for (int i = 0; i < recipeItems.length; ++i) {
                    Object o = recipeItems[i];
                    String materialkey = "";
                    int qty = 1;
                    ItemStack recipeStack = ItemStack.EMPTY;
                    if (o instanceof ItemStack) {
                        materialkey = ((ItemStack) o).getDisplayName();
                        recipeStack = (ItemStack) o;
                    } else if (o instanceof Item) {
                        recipeStack = new ItemStack((Item) o);
                        materialkey = ((Item) o).getItemStackDisplayName(new ItemStack((Item) o));
                    } else if (o instanceof Block) {
                        recipeStack = new ItemStack((Block) o);
                        materialkey = ((Block) o).getLocalizedName();
                    } else if (o instanceof String) {
                        if (((String) o).startsWith("E:")) {
                            int[] ids = RecipeUtils.ParseEssenceIDs((String) o);
                            materialkey = "Essence (";
                            for (int powerID : ids) {
                                PowerTypes type = PowerTypes.getByID(powerID);
                                materialkey += type.name() + "/";
                            }

                            if (materialkey.equals("Essence (")) {
                                ++i;
                                continue;
                            }

                            o = recipeItems[++i];
                            if (materialkey.startsWith("Essence (")) {
                                materialkey = materialkey.substring(0, materialkey.lastIndexOf("/")) + ")";
                                qty = (Integer) o;
                                int flag = 0;
                                for (int f : ids) {
                                    flag |= f;
                                }

                                recipeStack = new ItemStack(AMItems.etherium, qty, flag);
                            }

                        } else {
                            List<ItemStack> ores = OreDictionary.getOres((String) o);
                            recipeStack = !ores.isEmpty() ? ores.get(1) : null;
                            materialkey = (String) o;
                        }
                    }

                    if (materialsList.containsKey(materialkey)) {
                        int old = materialsList.get(materialkey);
                        old += qty;
                        materialsList.put(materialkey, old);
                    } else {
                        materialsList.put(materialkey, qty);
                    }

                    if (!recipeStack.isEmpty())
                        componentRecipeList.add(recipeStack);
                }
            }

            materialsList.put(AMItems.spell_parchment.getItemStackDisplayName(new ItemStack(AMItems.spell_parchment)), 1);

            StringBuilder sb = new StringBuilder();
            int sgCount = 0;
            int[][] shapeGroupCombos = new int[this.shapeGroups.size()][];
            for (ArrayList<SpellPart> shapeGroup : this.shapeGroups) {
                sb.append("Shape Group " + ++sgCount + "\n\n");
                Iterator<SpellPart> it = shapeGroup.iterator();
                shapeGroupCombos[sgCount - 1] = this.SpellPartListToStringBuilder(it, sb, " -");
                sb.append("\n");
            }

            sb.append("Combination:\n\n");
            Iterator<SpellPart> it = this.currentRecipe.iterator();
            int[] outputData = this.SpellPartListToStringBuilder(it, sb, null);
            LogHelper.info(sb.toString());

            ArrayList<NBTTagString> pages = Story.splitStoryPartIntoPages(sb.toString());

            sb = new StringBuilder();
            sb.append("\n\nMaterials List:\n\n");
            for (String s : materialsList.keySet()) {
                sb.append(materialsList.get(s) + " x " + s + "\n");
            }

            pages.addAll(Story.splitStoryPartIntoPages(sb.toString()));

            sb = new StringBuilder();
            sb.append("Affinity Breakdown:\n\n");
            it = this.currentRecipe.iterator();
            HashMap<Affinity, Integer> affinityData = new HashMap<Affinity, Integer>();
            int cpCount = 0;
            while (it.hasNext()) {
                SpellPart part = it.next();
                if (part instanceof SpellComponent) {
                    Set<Affinity> aff = ((SpellComponent) part).getAffinity();
                    for (Affinity affinity : aff) {
                        int qty = 1;
                        if (affinityData.containsKey(affinity)) {
                            qty = 1 + affinityData.get(affinity);
                        }
                        affinityData.put(affinity, qty);
                    }
                    cpCount++;
                }
            }
            ValueComparator vc = new ValueComparator(affinityData);
            TreeMap<Affinity, Integer> sorted = new TreeMap<Affinity, Integer>(vc);
            sorted.putAll(affinityData);
            for (Affinity aff : sorted.keySet()) {
                float pct = (float) sorted.get(aff) / (float) cpCount * 100f;
                sb.append(String.format("%s: %.2f%%", aff.getLocalizedName(), pct));
                sb.append("\n");
            }
            pages.addAll(Story.splitStoryPartIntoPages(sb.toString()));
            Story.WritePartToNBT(bookstack.getTagCompound(), pages);

            bookstack = Story.finalizeStory(bookstack, title, player.getName());

            ItemStack[] recipeData = new ItemStack[componentRecipeList.size()];
            int idx = 0;
            for (ItemStack stack : componentRecipeList) {
                recipeData[idx++] = stack;
            }

            NBTUtils.setItemStackArray(bookstack.getTagCompound(), "spell_combo", recipeData);
            bookstack.getTagCompound().setIntArray("output_combo", outputData);
            bookstack.getTagCompound().setInteger("numShapeGroups", shapeGroupCombos.length);
            int index = 0;
            for (int[] sgArray : shapeGroupCombos) {
                bookstack.getTagCompound().setIntArray("shapeGroupCombo_" + index++, sgArray);
            }
            bookstack.getTagCompound().setString("spell_mod_version", ArsMagica.instance.getVersion());

            if (this.currentSpellName.equals(""))
                this.currentSpellName = "Spell Recipe";
            bookstack.setStackDisplayName(this.currentSpellName);

            this.currentRecipe.clear();
            for (ArrayList<SpellPart> list : this.shapeGroups)
                list.clear();
            this.currentSpellName = "";

            bookstack.getTagCompound().setBoolean("spellFinalized", true);

            //world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), "arsmagica2:misc.inscriptiontable.takebook", 1.0f, 1.0f, true);
            this.markDirty();
            //world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
        }
        return bookstack;
    }

    private int[] SpellPartListToStringBuilder(Iterator<SpellPart> it, StringBuilder sb, String prefix) {
        ArrayList<Integer> outputCombo = new ArrayList<Integer>();
        while (it.hasNext()) {
            SpellPart part = it.next();
            String displayName = SpellRegistryHelper.getSkillFromPart(part).getName();

            if (prefix != null) {
                sb.append(prefix + displayName + "\n");
            } else {
                if (part instanceof SpellShape) {
                    sb.append(displayName + "\n");
                } else {
                    sb.append("-" + displayName + "\n");
                }
            }

            outputCombo.add(part.networkID());
        }

        int[] outputData = new int[outputCombo.size()];
        int idx = 0;
        for (Integer I : outputCombo) {
            outputData[idx++] = I;
        }

        return outputData;
    }

    public void clearCurrentRecipe() {
        this.currentRecipe.clear();
        for (ArrayList<SpellPart> group : this.shapeGroups)
            group.clear();
        this.currentSpellName = "";
        this.currentSpellIsReadOnly = false;
    }

    public SpellValidator.ValidationResult currentRecipeIsValid() {
        ArrayList<ArrayList<SpellPart>> segmented = SpellValidator.splitToStages(this.currentRecipe);
        return SpellValidator.instance.spellDefIsValid(this.shapeGroups, segmented);
    }

    public boolean modifierCanBeAdded(SpellModifier modifier) {
        // Fix inscription table unusable spells: properly validate modifier limits
        EnumSet<SpellModifiers> modifiers = modifier.getAspectsModified();
        for (SpellModifiers mod : modifiers) {
            if (this.getModifierCount(mod) > 2) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void markDirty() {
        this.markForUpdate();
        super.markDirty();
    }

    @Override
    public void markForUpdate() {
        this.dirty = true;
    }

    @Override
    public boolean needsUpdate() {
        return this.dirty;
    }

    @Override
    public void clean() {
        this.dirty = false;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    static class ValueComparator implements Comparator<Affinity> {

        Map<Affinity, Integer> base;

        ValueComparator(Map<Affinity, Integer> base) {
            this.base = base;
        }

        @Override
        public int compare(Affinity a, Affinity b) {
            Integer x = this.base.get(a);
            Integer y = this.base.get(b);
            if (x.equals(y)) {
                return a.compareTo(b);
            }
            return x.compareTo(y);
        }
    }

    public void setSpellName(String name) {
        this.currentSpellName = name;
        this.sendDataToServer();
    }

    public String getSpellName() {
        return this.currentSpellName != null ? this.currentSpellName : "";
    }

    public void reverseEngineerSpell(ItemStack stack) {
        this.currentRecipe.clear();
        for (ArrayList<SpellPart> group : this.shapeGroups) {
            group.clear();
        }
        this.currentSpellName = "";
        ISpellCaster caster = SpellCaster.of(stack);
        if (caster != null) {
            this.currentSpellName = stack.getDisplayName();
            for (int i = 0; i < caster.getShapeGroupCount(); i++) {
                List<List<SpellPart>> shapeGroup = caster.getShapeGroups().get(i);
                for (List<SpellPart> stage : shapeGroup) {
                    for (SpellPart part : stage) {
                        this.shapeGroups.get(i).add(part);
                    }
                }
            }
            for (List<SpellPart> stage : caster.getSpellCommon()) {
                for (SpellPart part : stage) {
                    this.currentRecipe.add(part);
                }
            }
            this.currentSpellIsReadOnly = true;
        }
    }

    public boolean currentSpellDefIsReadOnly() {
        return this.currentSpellIsReadOnly;
    }

    /**
     * Returns {@code true} when the desk's book slot holds an
     * {@link ItemEBWizSpellBinding}.
     * In this mode, only Range, Duration, and EBWizBlast modifiers may be
     * applied – no shapes or components.
     */
    public boolean isEBWizMode() {
        ItemStack slot = this.getStackInSlot(0);
        return !slot.isEmpty() && EBWizardryCompatBootstrap.isEBWizSpellBook(slot);
    }

    /**
     * Returns {@code true} when EBWiz mode is active <em>and</em> the
     * {@code EBWiz_Preserve_Spell_Book} config option is enabled.
     *
     * <p>In this sub-mode a second slot (index {@link #ebwizWritableBookIndex})
     * accepts the writable book; the original EBWiz spell book in slot 0 is
     * preserved when the binding is created.
     */
    public boolean isEBWizPreserveMode() {
        return isEBWizMode() && ArsMagica.config.getEBWizPreserveSpellBook();
    }

    /**
     * Converts the EBWiz binding from {@code bindingStack} together with the
     * current recipe's AM2 modifier counts into a Written Book.
     *
     * <p>The resulting book is placed on the Crafting Altar lectern; throwing a
     * {@code blank_rune} starts the normal ingredient-based crafting flow where
     * each modifier's recipe items must be thrown in, followed by a
     * {@code spell_parchment}. The altar then spawns the final
     * {@link ItemEBWizSpellBinding}.
     *
     * <p>Also calls {@link #clearCurrentRecipe()}.
     */
    public ItemStack createEBWizBindingBook(ItemStack bindingStack) {
        int rangeCount = 0, durationCount = 0, blastCount = 0;
        ArrayList<SpellPart> recipe = new ArrayList<>(this.currentRecipe);
        for (SpellPart part : recipe) {
            if (!(part instanceof SpellModifier)) continue;
            SpellModifier mod = (SpellModifier) part;
            EnumSet<SpellModifiers> aspects = mod.getAspectsModified();
            if (aspects.contains(SpellModifiers.RANGE))                                           rangeCount++;
            else if (aspects.contains(SpellModifiers.DURATION))                                   durationCount++;
            else if (aspects.contains(SpellModifiers.RADIUS) && mod instanceof IEBWizExclusive)  blastCount++;
        }

        // Build ingredient list from modifier recipes (same logic as writeRecipeAndDataToBook).
        // These are stored as spell_combo so the Crafting Altar's item guide tracks them.
        ArrayList<ItemStack> componentRecipeList = new ArrayList<>();
        for (SpellPart part : recipe) {
            Object[] recipeItems = part.getEffectiveRecipe();
            SpellRecipeItemsEvent event = new SpellRecipeItemsEvent(
                    SpellRegistryHelper.getSkillFromPart(part).getID(), recipeItems);
            MinecraftForge.EVENT_BUS.post(event);
            recipeItems = event.recipeItems;
            if (recipeItems == null) continue;
            for (int i = 0; i < recipeItems.length; ++i) {
                Object o = recipeItems[i];
                ItemStack recipeStack = ItemStack.EMPTY;
                if (o instanceof ItemStack) {
                    recipeStack = (ItemStack) o;
                } else if (o instanceof Item) {
                    recipeStack = new ItemStack((Item) o);
                } else if (o instanceof Block) {
                    recipeStack = new ItemStack((Block) o);
                } else if (o instanceof String && ((String) o).startsWith("E:")) {
                    int[] ids = RecipeUtils.ParseEssenceIDs((String) o);
                    if (ids.length == 0) { ++i; continue; }
                    Object qty = recipeItems[++i];
                    int amount = (qty instanceof Integer) ? (Integer) qty : 1;
                    int flag = 0;
                    for (int f : ids) flag |= f;
                    recipeStack = new ItemStack(AMItems.etherium, amount, flag);
                }
                if (!recipeStack.isEmpty()) componentRecipeList.add(recipeStack);
            }
        }

        // Vanilla Written Book NBT structure
        NBTTagCompound tag = new NBTTagCompound();
        // Read the EBWiz spell registry name from the spell book's metadata
        String spellKey = EBWizardryCompatBootstrap.getEBWizSpellBookRegistryName(bindingStack);
        if (!spellKey.isEmpty()) tag.setString("EBWizSpell", spellKey);
        // AM2 modifier counts
        NBTTagCompound mods = new NBTTagCompound();
        mods.setInteger("range",    rangeCount);
        mods.setInteger("duration", durationCount);
        mods.setInteger("blast",    blastCount);
        tag.setTag("AM2Modifiers", mods);
        // Required vanilla fields for a written book (prevents display crashes)
        String titleStr = (this.currentSpellName != null && !this.currentSpellName.isEmpty())
                ? this.currentSpellName : EBWizardryCompatBootstrap.getEBWizSpellBookDisplayName(bindingStack);
        if (titleStr == null || titleStr.isEmpty()) titleStr = bindingStack.getDisplayName();
        tag.setString("title",  titleStr);
        tag.setString("author", currentPlayerUsing != null ? currentPlayerUsing.getName() : "");

        // Build book pages: spell info, applied modifiers, required ingredients
        StringBuilder sb = new StringBuilder();
        sb.append(titleStr).append("\n\n");
        sb.append("Applied Modifiers:\n");
        if (rangeCount == 0 && durationCount == 0 && blastCount == 0) {
            sb.append("(none)\n");
        } else {
            if (rangeCount > 0)    sb.append("Range x").append(rangeCount).append("\n");
            if (durationCount > 0) sb.append("Duration x").append(durationCount).append("\n");
            if (blastCount > 0)    sb.append("Blast x").append(blastCount).append("\n");
        }
        sb.append("\nTake this book to the Crafting Altar and throw a Blank Rune nearby to begin.\n");

        ArrayList<NBTTagString> pages = Story.splitStoryPartIntoPages(sb.toString());

        if (!componentRecipeList.isEmpty()) {
            StringBuilder matSb = new StringBuilder();
            matSb.append("Required at Altar:\n\n");
            // Aggregate by display name
            java.util.LinkedHashMap<String, Integer> matMap = new java.util.LinkedHashMap<>();
            for (ItemStack s : componentRecipeList) {
                String key = s.getDisplayName();
                matMap.put(key, matMap.getOrDefault(key, 0) + s.getCount());
            }
            for (java.util.Map.Entry<String, Integer> entry : matMap.entrySet()) {
                matSb.append(entry.getValue()).append(" x ").append(entry.getKey()).append("\n");
            }
            matSb.append("\n1 x Blank Rune");
            pages.addAll(Story.splitStoryPartIntoPages(matSb.toString()));
        }

        Story.WritePartToNBT(tag, pages);
        // Ingredient list for the Crafting Altar's multi-step ingredient flow
        ItemStack[] recipeData = componentRecipeList.toArray(new ItemStack[0]);
        NBTUtils.setItemStackArray(tag, "spell_combo", recipeData);
        tag.setIntArray("output_combo", new int[0]);
        tag.setInteger("numShapeGroups", 0);
        // Mark finalized so the Scribing Desk won't re-accept this book
        tag.setBoolean("spellFinalized", true);
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        book.setTagCompound(tag);
        book.setStackDisplayName(titleStr);
        clearCurrentRecipe();
        return book;
    }

    public void resetSpellNameAndIcon(ItemStack stack, EntityPlayer player) {
        if (this.world.isRemote) {
            AMDataWriter writer = new AMDataWriter();
            writer.add(RESET_NAME);
            writer.add(player.getEntityId());
            AMNetworkHandler.getNetwork().sendToServer(new PacketInscriptionTableUpdate(this.getPos(), writer.generate()));
        }
        stack.setItemDamage(0);
        stack.clearCustomName();
    }

    public int getShapeGroupSize(int groupIndex) {
        if (groupIndex > this.shapeGroups.size() || groupIndex < 0)
            return 0;
        return this.shapeGroups.get(groupIndex).size();
    }

    public SpellPart getShapeGroupPartAt(int groupIndex, int index) {

        return this.shapeGroups.get(groupIndex).get(index);
    }


    public void incrementUpgradeState() {
        this.numStageGroups++;
        if (!this.world.isRemote) {
            List<EntityPlayerMP> players = this.world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(this.pos).expand(256, 256, 256));
            for (EntityPlayerMP player : players) {
                player.connection.sendPacket(this.getUpdatePacket());
            }
        }
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
    }
}
