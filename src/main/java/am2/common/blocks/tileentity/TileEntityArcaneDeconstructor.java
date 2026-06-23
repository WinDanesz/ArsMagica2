package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.ArsMagicaAPI;
import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.SpellPart;
import am2.client.particles.*;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMItems;
import am2.common.spell.SpellCaster;
import am2.common.spell.shape.Binding;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.RecipeUtils;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketArcaneDeconstructorSync;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityArcaneDeconstructor extends TileEntityAMPower implements IInventory, ITileEntityPacketSync, ISidedInventory, IKeystoneLockable<TileEntityArcaneDeconstructor> {

    private static final int SYNC_DECONSTRUCTION_TIME = 0x1;
    private static final int SYNC_DECONSTRUCTION_RECIPE = 0x2;
    private static final int SYNC_INVENTORY = 0x4;

    private int syncCode = -1;
    private int current_deconstruction_time = 0; //how long have we been deconstructing something?

    private static final ArrayList<PowerTypes> validPowerTypes = Lists.newArrayList(PowerTypes.DARK);

    private NonNullList<ItemStack> inventory;
    private ItemStack[] deconstructionRecipe;

    public TileEntityArcaneDeconstructor() {
        super(ArsMagica.config.getCapacityArcaneDeconstructor());
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public int getChargeRate() {
        return 250;
    }

    @Override
    public void update() {
        super.update();

        if (world.isRemote) {
            if (isActive()) {
                if (world.rand.nextInt(2) == 0) {
                    double cx = pos.getX() + 0.5;
                    double cy = pos.getY() + 0.5;
                    double cz = pos.getZ() + 0.5;
                    double spread = 0.5;
                    double sx = cx + (world.rand.nextDouble() - 0.5) * spread;
                    double sy = cy + (world.rand.nextDouble() - 0.5) * spread;
                    double sz = cz + (world.rand.nextDouble() - 0.5) * spread;
                    AMParticle swirl = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", sx, sy, sz);
                    if (swirl != null) {
                        swirl.setMaxAge(30 + world.rand.nextInt(20));
                        swirl.setRGBColorF(0.5f + world.rand.nextFloat() * 0.2f, 0.0f, 0.0f);
                        swirl.setParticleScale(0.05f + world.rand.nextFloat() * 0.05f);
                        swirl.AddParticleController(new ParticleOrbitPoint(swirl, cx, cy, cz, 1, false)
                                .SetTargetDistance(0.1 + world.rand.nextDouble() * 0.15)
                                .SetOrbitSpeed(0.08 + world.rand.nextDouble() * 0.04));
                        swirl.AddParticleController(new ParticleFadeOut(swirl, 2, false).setFadeSpeed(0.03f));
                    }
                }
                if (!inventory.get(0).isEmpty() && world.rand.nextInt(4) == 0) {
                    double ix = pos.getX() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.3;
                    double iy = pos.getY() + 0.55;
                    double iz = pos.getZ() + 0.5 + (world.rand.nextDouble() - 0.5) * 0.3;
                    AMParticle ember = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", ix, iy, iz);
                    if (ember != null) {
                        ember.setMaxAge(15 + world.rand.nextInt(10));
                        ember.setRGBColorF(0.6f + world.rand.nextFloat() * 0.3f, 0.0f, 0.0f);
                        ember.setParticleScale(0.03f + world.rand.nextFloat() * 0.03f);
                        ember.AddParticleController(new ParticleFloatUpward(ember, 0.06f, 0.02f, 1, false));
                        ember.AddParticleController(new ParticleFadeOut(ember, 2, false).setFadeSpeed(0.05f));
                    }
                }
            }
        } else {
            if (!isActive()) {
                if (!inventory.get(0).isEmpty()) {
                    setDeconstructionTime(1);
                }
            } else {
                if (inventory.get(0).isEmpty()) {
                    setDeconstructionTime(0);
                    deconstructionRecipe = null;
                    this.syncCode |= SYNC_DECONSTRUCTION_RECIPE;
                    this.markDirty();
                    //world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
                } else {
                    if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, ArsMagica.config.getDeconstructorPowerCost())) {
                        if (deconstructionRecipe == null) {
                            if (!getDeconstructionRecipe()) {
                                transferOrEjectItem(inventory.get(0));
                                setInventorySlotContents(0, ItemStack.EMPTY);
                            }
                        } else {
                            setDeconstructionTime(current_deconstruction_time + 1);
                            if (current_deconstruction_time >= ArsMagica.config.getDeconstructorTime()) {
                                if (getDeconstructionRecipe() == true) {
                                    for (ItemStack stack : deconstructionRecipe) {
                                        transferOrEjectItem(stack);
                                    }
                                }
                                deconstructionRecipe = null;
                                decrStackSize(0, 1);
                                setDeconstructionTime(0);
                            }
                            if (current_deconstruction_time % 10 == 0)
                                this.markDirty();
                            //world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
                        }
                        PowerNodeRegistry.For(world).consumePower(this, PowerTypes.DARK, ArsMagica.config.getDeconstructorPowerCost());
                    }
                }
            }
            if (this.shouldSync()) {
                AMNetworkHandler.getNetwork().sendToAllAround(new PacketArcaneDeconstructorSync(pos, this.createSyncPacket()), new net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64));
                this.confirm();
            }
        }
    }

    private boolean getDeconstructionRecipe() {
        ItemStack checkStack = getStackInSlot(0);
        ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
        if (checkStack.isEmpty())
            return false;
        if (checkStack.getItem() == AMItems.spell && checkStack.hasCapability(SpellCaster.INSTANCE, null)) {
            ISpellCaster spell = SpellCaster.of(checkStack);
            for (List<SpellPart> stage : spell.getSpellCommon()) {
                for (SpellPart part : stage) {
                    Object[] componentParts = part.getEffectiveRecipe();
                    if (componentParts != null) {
                        for (Object o : componentParts) {
                            ItemStack stack = objectToItemStack(o);
                            if (!stack.isEmpty()) {
                                if (stack.getItem() == AMItems.binding_catalyst) {
                                    stack.setItemDamage(((Binding) ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "binding"))).getBindingType(spell));
                                } else if (stack.getItem() == AMItems.crystal_phylactery) {
                                    // todo ItemDefs.crystalPhylactery.setSpawnClass(stack,((Summon)ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "summon"))).getSummonType(spell));
                                    ((ItemCrystalPhylactery) AMItems.crystal_phylactery).addFill(stack, 100);
                                }

                                recipeItems.add(stack.copy());
                            }
                        }
                    }
                }
            }

            for (List<List<SpellPart>> shapeGroup : spell.getShapeGroups()) {
                for (List<SpellPart> stage : shapeGroup) {
                    for (SpellPart part : stage) {
                        Object[] componentParts = part.getEffectiveRecipe();
                        if (componentParts != null) {
                            for (Object o : componentParts) {
                                ItemStack stack = objectToItemStack(o);
                                if (!stack.isEmpty()) {
                                    if (stack.getItem() == AMItems.binding_catalyst) {
                                        stack.setItemDamage(((Binding) ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "binding"))).getBindingType(spell));
                                    }
                                    recipeItems.add(stack.copy());
                                }
                            }
                        }
                    }
                }
            }

            ItemStack[] arr = recipeItems.toArray(new ItemStack[recipeItems.size()]);
            if (arr != deconstructionRecipe) {
                this.syncCode |= SYNC_DECONSTRUCTION_RECIPE;
                deconstructionRecipe = arr;
            }
            return true;
        } else {
            IRecipe recipe = RecipeUtils.getRecipeFor(checkStack);
            if (recipe == null)
                return false;
            Object[] recipeParts = (Object[]) RecipeUtils.getRecipeItems(recipe);
            if (recipeParts != null && !checkStack.isEmpty() && recipe.getRecipeOutput() != null) {
                if (recipe.getRecipeOutput().getItem() == checkStack.getItem() && recipe.getRecipeOutput().getItemDamage() == checkStack.getItemDamage() && recipe.getRecipeOutput().getCount() > 1)
                    return false;

                for (Object o : recipeParts) {
                    ItemStack stack = objectToItemStack(o);
                    if (!stack.isEmpty() && !stack.getItem().hasContainerItem(stack)) {
                        stack.setCount(1);
                        recipeItems.add(stack.copy());
                    }
                }
            }
            ItemStack[] arr = recipeItems.toArray(new ItemStack[recipeItems.size()]);
            if (arr != deconstructionRecipe) {
                this.syncCode |= SYNC_DECONSTRUCTION_RECIPE;
                deconstructionRecipe = arr;
            }
            return true;
        }
    }

    private ItemStack objectToItemStack(Object o) {
        ItemStack output = ItemStack.EMPTY;
        if (o instanceof ItemStack)
            output = ((ItemStack) o).copy();
        else if (o instanceof Ingredient) {
            Ingredient ingredient = (Ingredient) o;
            ItemStack[] matchingStacks = ingredient.getMatchingStacks();
            if (matchingStacks.length > 0 && !matchingStacks[0].isEmpty())
                output = matchingStacks[0].copy();
        } else if (o instanceof Item)
            output = new ItemStack((Item) o);
        else if (o instanceof Block)
            output = new ItemStack((Block) o);
        else if (o instanceof List)
            output = objectToItemStack(((List<?>) o).get(0));

        if (output.isEmpty()) {
            output = ItemStack.EMPTY;
        } else if (output.getCount() == 0) {
            output.setCount(1);
        }

        return output;
    }

    private void transferOrEjectItem(ItemStack stack) {
        if (world.isRemote)
            return;

        // First, try to place items into internal output slots (1-9)
        for (int slot = 1; slot <= 9; slot++) {
            if (stack.isEmpty()) return;
            ItemStack existing = inventory.get(slot);
            if (existing.isEmpty()) {
                setInventorySlotContents(slot, stack.copy());
                stack.setCount(0);
                return;
            } else if (ItemStack.areItemsEqual(existing, stack) && ItemStack.areItemStackTagsEqual(existing, stack)) {
                int space = existing.getMaxStackSize() - existing.getCount();
                if (space > 0) {
                    int toTransfer = Math.min(space, stack.getCount());
                    existing.grow(toTransfer);
                    stack.shrink(toTransfer);
                    if (stack.isEmpty()) return;
                }
            }
        }

        // Then try adjacent inventories
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                for (int k = -1; k <= 1; ++k) {
                    if (i == 0 && j == 0 && k == 0)
                        continue;
                    TileEntity te = world.getTileEntity(pos.add(i, j, k));
                    if (te != null && te instanceof IInventory) {
                        for (EnumFacing side : EnumFacing.values()) {
                            if (InventoryUtilities.mergeIntoInventory((IInventory) te, stack, stack.getCount(), side))
                                return;
                        }
                    }
                }
            }
        }

        //eject the remainder
        EntityItem item = new EntityItem(world);
        item.setPosition(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
        item.setItem(stack);
        world.spawnEntity(item);
    }

    private void setDeconstructionTime(int time) {
        if (this.current_deconstruction_time != time) {
            this.current_deconstruction_time = time;
            this.syncCode |= SYNC_DECONSTRUCTION_TIME;
        }
    }

    public boolean isActive() {
        return current_deconstruction_time > 0;
    }

    public ItemStack getInputItem() {
        return inventory.get(0);
    }

    @Override
    public int getSizeInventory() {
        return 16;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
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
        return "ArcaneDeconstructor";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return player.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return i <= 9;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing var1) {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j) {
        return i == 0;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j) {
        return i >= 1 && i <= 9;
    }

    @Override
    public ItemStack[] getRunesInKey() {
        return new ItemStack[]{
                inventory.get(13),
                inventory.get(14),
                inventory.get(15)
        };
    }

    @Override
    public boolean keystoneMustBeHeld() {
        return false;
    }

    @Override
    public boolean keystoneMustBeInActionBar() {
        return false;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return validPowerTypes;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        ItemStackHelper.loadAllItems(nbttagcompound, this.inventory);
        this.current_deconstruction_time = nbttagcompound.getInteger("DeconstructionTime");

        if (current_deconstruction_time > 0)
            getDeconstructionRecipe();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        ItemStackHelper.saveAllItems(nbttagcompound, this.inventory);
        nbttagcompound.setInteger("DeconstructionTime", current_deconstruction_time);
        return nbttagcompound;
    }

    public int getProgressScaled(int i) {
        return current_deconstruction_time * i / ArsMagica.config.getDeconstructorTime();
    }

    @Override
    public ITextComponent getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return current_deconstruction_time;
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: current_deconstruction_time = value; break;
        }
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public byte[] createSyncPacket() {
        AMDataWriter writer = new AMDataWriter();
        writer.add(syncCode);
        if ((syncCode & SYNC_DECONSTRUCTION_TIME) == SYNC_DECONSTRUCTION_TIME)
            writer.add(current_deconstruction_time);
        if ((syncCode & SYNC_INVENTORY) == SYNC_INVENTORY) {
            for (int i = 0; i < getSizeInventory(); i++) {
                ItemStack stack = inventory.get(i);
                if (!stack.isEmpty()) {
                    writer.add(true);
                    writer.add(stack.writeToNBT(new NBTTagCompound()));
                } else
                    writer.add(false);
            }
        }
        if ((syncCode & SYNC_DECONSTRUCTION_RECIPE) == SYNC_DECONSTRUCTION_RECIPE) {
            if (deconstructionRecipe != null) {
                writer.add(true);
                writer.add(deconstructionRecipe.length);
                for (int i = 0; i < deconstructionRecipe.length; i++) {
                    ItemStack stack = deconstructionRecipe[i];
                    if (!stack.isEmpty()) {
                        writer.add(true);
                        writer.add(stack.writeToNBT(new NBTTagCompound()));
                    } else
                        writer.add(false);
                }
            } else
                writer.add(false);
        }
        return writer.generate();
    }

    @Override
    public boolean handleSyncPacket(byte[] packet) {
        AMDataReader reader = new AMDataReader(packet, false);
        int syncCode = reader.getInt();
        if ((syncCode & SYNC_DECONSTRUCTION_TIME) == SYNC_DECONSTRUCTION_TIME)
            this.current_deconstruction_time = reader.getInt();
        if ((syncCode & SYNC_INVENTORY) == SYNC_INVENTORY) {
            for (int i = 0; i < getSizeInventory(); i++) {
                if (reader.getBoolean()) {
                    setInventorySlotContents(i, new ItemStack((reader.getNBTTagCompound())));
                }
            }
        }
        if ((syncCode & SYNC_DECONSTRUCTION_RECIPE) == SYNC_DECONSTRUCTION_RECIPE) {
            if (reader.getBoolean()) {
                deconstructionRecipe = new ItemStack[reader.getInt()];
                for (int i = 0; i < deconstructionRecipe.length; i++) {
                    if (reader.getBoolean()) {
                        deconstructionRecipe[i] = new ItemStack((reader.getNBTTagCompound()));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldSync() {
        return syncCode != 0;
    }

    @Override
    public void confirm() {
        this.syncCode = 0;
    }
}
