package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.blocks.IKeystoneLockable;
import am2.api.items.ISpellFocus;
import am2.api.items.ItemFilterFocus;
import am2.client.models.SpriteRenderInfo;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.client.particles.ParticleMoveOnHeading;
import am2.common.blocks.BlockSeerStone;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.registry.AMBlocks;
import am2.common.utils.KeystoneUtilities;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntitySeerStone extends TileEntityAMPower implements IInventory, IKeystoneLockable<TileEntitySeerStone> {

    private boolean hasSight;
    private ArrayList<SpriteRenderInfo> animations;
    private ArrayList<Integer> animationWeighting;
    private SpriteRenderInfo currentAnimation;
    private int ticksToNextCheck;
    private int maxTicksToCheck = 20;
    private NonNullList<ItemStack> inventory;
    int tickCounter;
    public static int keystoneSlot = 1;

    private List<PowerTypes> validTypes = Lists.newArrayList(
            PowerTypes.LIGHT
    );

    boolean swapDetectionMode = false;

    public TileEntitySeerStone() {
        super(ArsMagica.config.getCapacitySeerStone());
        hasSight = false;
        tickCounter = 0;
        animations = new ArrayList<SpriteRenderInfo>();
        animationWeighting = new ArrayList<Integer>();

        animations.add(new SpriteRenderInfo(50, 59, 2)); //blink
        animationWeighting.add(0);

        animations.add(new SpriteRenderInfo(20, 29, 2)); //idle
        animationWeighting.add(60);

        animations.add(new SpriteRenderInfo(30, 39, 2)); //look left
        animationWeighting.add(11);

        animations.add(new SpriteRenderInfo(40, 49, 2)); //look right
        animationWeighting.add(11);

        animations.add(new SpriteRenderInfo(0, 14, 2)); //flare 1
        animationWeighting.add(6);

        animations.add(new SpriteRenderInfo(60, 74, 2)); //flare 2
        animationWeighting.add(6);

        animations.add(new SpriteRenderInfo(80, 94, 2)); //flare 3
        animationWeighting.add(6);

        currentAnimation = animations.get(0);
        currentAnimation.isDone = true;

        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ticksToNextCheck = maxTicksToCheck;
    }

    @Override
    public float particleOffset(int axis) {
        EnumFacing meta = world.getBlockState(pos).getValue(BlockSeerStone.FACING);

        if (axis == 0) {
            switch (meta) {
                case UP:
                    return 0.15f;
                case DOWN:
                    return 0.85f;
                default:
                    return 0.5f;
            }
        } else if (axis == 1) {
            switch (meta) {
                case EAST:
                    return 0.85f;
                case WEST:
                    return 0.2f;
                default:
                    return 0.5f;
            }
        } else if (axis == 2) {
            switch (meta) {
                case NORTH:
                    return 0.15f;
                case SOUTH:
                    return 0.85f;
                default:
                    return 0.5f;
            }
        }

        return 0.5f;
    }

    public void invertDetection() {
        this.swapDetectionMode = !this.swapDetectionMode;
    }

    public boolean isInvertingDetection() {
        return this.swapDetectionMode;
    }

    private SpriteRenderInfo GetWeightedRandomAnimation() {
        currentAnimation.reset(false);

        int randomNumber = world.rand.nextInt(100);
        int index = 0;

        SpriteRenderInfo current = animations.get(0);

        for (Integer i : animationWeighting) {
            if (randomNumber < i) {
                current = animations.get(index);
                break;
            }
            index++;
            randomNumber -= i;
        }

        return current == animations.get(0) ? animations.get(1) : current;
    }

    public boolean isActive() {
        if (this.world == null)
            return false;
        return PowerNodeRegistry.For(this.world).checkPower(this, PowerTypes.LIGHT, this.hasSight ? 2 : 1) && GetSearchRadius() > 0;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToNBT(compound);
        return new SPacketUpdateTileEntity(pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void update() {
        super.update();

        if (!world.isRemote && isActive()) {
            if (hasSight)
                PowerNodeRegistry.For(world).consumePower(this, PowerTypes.LIGHT, 0.25f);
            else
                PowerNodeRegistry.For(world).consumePower(this, PowerTypes.LIGHT, 0.125f);
        }

        ticksToNextCheck--;
        if (ticksToNextCheck <= 0 && isActive()) {
            ticksToNextCheck = maxTicksToCheck;

            long key = KeystoneUtilities.instance.getKeyFromRunes(getRunesInKey());

            int radius = GetSearchRadius();
            Class<? extends Entity> searchClass = GetSearchClass();
            ArrayList<Entity> nearbyMobs = new ArrayList<Entity>();
            if (searchClass != null) {
                nearbyMobs = (ArrayList<Entity>) this.world.getEntitiesWithinAABB(searchClass, new AxisAlignedBB(pos.add(-radius, -radius, -radius), pos.add(1 + radius, 1 + radius, 1 + radius)));

                if (key > 0) {
                    ArrayList<Entity> mobsToIgnore = new ArrayList<Entity>();
                    for (Entity e : nearbyMobs) {
                        if (swapDetectionMode) {
                            if (!(e instanceof EntityPlayer)) {
                                mobsToIgnore.add(e);
                                continue;
                            }
                            if (!KeystoneUtilities.instance.GetKeysInInvenory((EntityLivingBase) e).contains(key)) {
                                mobsToIgnore.add(e);
                            }
                        } else {
                            if (!(e instanceof EntityPlayer)) continue;
                            if (KeystoneUtilities.instance.GetKeysInInvenory((EntityLivingBase) e).contains(key)) {
                                mobsToIgnore.add(e);
                            }
                        }
                    }
                    for (Entity e : mobsToIgnore) nearbyMobs.remove(e);
                }
            }

            if (!nearbyMobs.isEmpty()) {
                if (!hasSight) {
                    hasSight = true;
                    notifyNeighborsOfPowerChange();

                    if (world.isRemote) {
                        currentAnimation.reset(false);
                        currentAnimation = animations.get(0);
                        currentAnimation.reset(true);
                    }
                }
            } else {
                if (hasSight) {
                    hasSight = false;
                    notifyNeighborsOfPowerChange();

                    if (world.isRemote) {
                        currentAnimation.reset(false);
                        currentAnimation = animations.get(0);
                        currentAnimation.reset(false);
                    }
                }
            }
        } else {
            if (hasSight && !isActive()) {
                hasSight = false;
                notifyNeighborsOfPowerChange();

                if (world.isRemote) {
                    currentAnimation.reset(false);
                    currentAnimation = animations.get(0);
                    currentAnimation.reset(false);
                }
            }
        }

        //animations
        if (world.isRemote) {
            if (!currentAnimation.isDone) {
                tickCounter++;
                if (tickCounter == currentAnimation.speed) {
                    tickCounter = 0;
                    currentAnimation.incrementIndex();
                }
            } else {
                if (isActive() && hasSight) {
                    currentAnimation = GetWeightedRandomAnimation();
                }
            }

            if (isActive() && hasSight) {

                EnumFacing meta = world.getBlockState(pos).getValue(BlockSeerStone.FACING);

                double yaw = 0;
                double y = pos.getX() + 0.5;
                double x = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;

                switch (meta) {
                    case UP:
                        y += 0.3;
                        break;
                    case DOWN:
                        y -= 0.3;
                        break;
                    case EAST:
                        yaw = 270;
                        z += 0.3;
                        break;
                    case WEST:
                        yaw = 90;
                        z -= 0.3;
                        break;
                    case NORTH:
                        yaw = 180;
                        x += 0.3;
                        break;
                    case SOUTH:
                        yaw = 0;
                        x -= 0.3;
                        break;
                }

                AMParticle effect = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", x, y, z);
                if (effect != null) {
                    effect.setIgnoreMaxAge(false);
                    effect.setMaxAge(35);
                    //effect.setRGBColorF(0.9f, 0.7f, 0.0f);

                    switch (meta) {
                        case UP:
                            effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, -0.01f, 1, false));
                            break;
                        case DOWN:
                            effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, 0.01f, 1, false));
                            break;
                        default:
                            effect.AddParticleController(new ParticleMoveOnHeading(effect, yaw, 0, 0.01f, 1, false));
                            effect.AddParticleController(new ParticleFloatUpward(effect, 0.1f, 0, 1, false));
                    }
                }
            }
        }
    }

    private void notifyNeighborsOfPowerChange() {
        // TODO this will be a problem
        this.world.notifyNeighborsOfStateExcept(pos, AMBlocks.seer_stone, null);
        BlockPos otherPos = pos.offset(world.getBlockState(pos).getValue(BlockSeerStone.FACING));
        this.world.notifyNeighborsOfStateExcept(otherPos, AMBlocks.seer_stone, null);
        this.world.markAndNotifyBlock(otherPos, world.getChunk(otherPos), world.getBlockState(otherPos), world.getBlockState(otherPos), 3);
        // Sync to clients
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

    public boolean ShouldAnimate() {
        return (isActive() && hasSight) || !currentAnimation.isDone;
    }

    public int getAnimationIndex() {
        return currentAnimation.curFrame;
    }

    private int GetSearchRadius() {
        int focusLevel = -1;
        int inventoryIndex = 0;

        if (!inventory.get(inventoryIndex).isEmpty() && inventory.get(inventoryIndex).getItem() instanceof ISpellFocus) {
            int tempFocusLevel = ((ISpellFocus) inventory.get(inventoryIndex).getItem()).getFocusLevel();
            if (tempFocusLevel > focusLevel) {
                focusLevel = tempFocusLevel;
            }
        }
        int radius = (focusLevel + 1) * 5;
        return radius;
    }

    private Class<? extends Entity> GetSearchClass() {
        if (inventory.get(1) != null && inventory.get(1).getItem() instanceof ItemFilterFocus) {
            return ((ItemFilterFocus) inventory.get(1).getItem()).getFilterClass();
        }
        return null;
    }

    public boolean HasSight() {
        return isActive() && this.hasSight;
    }

    @Override
    public int getSizeInventory() {
        return 5;
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

    @Override
    public ItemStack[] getRunesInKey() {
        ItemStack[] runes = new ItemStack[3];
        runes[0] = inventory.get(2);
        runes[1] = inventory.get(3);
        runes[2] = inventory.get(4);
        return runes;
    }

    @Override
    public boolean keystoneMustBeHeld() {
        return false;
    }

    @Override
    public boolean keystoneMustBeInActionBar() {
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
        return "Seer Stone";
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (world.getTileEntity(pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSqToCenter(pos) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.swapDetectionMode = nbttagcompound.getBoolean("seerStoneIsInverting");
        this.hasSight = nbttagcompound.getBoolean("hasSight");
        ItemStackHelper.loadAllItems(nbttagcompound, this.inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        ItemStackHelper.saveAllItems(nbttagcompound, this.inventory);
        nbttagcompound.setBoolean("seerStoneIsInverting", this.isInvertingDetection());
        nbttagcompound.setBoolean("hasSight", this.hasSight);
        return nbttagcompound;
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public int getChargeRate() {
        return 20;
    }

    @Override
    public List<PowerTypes> getValidPowerTypes() {
        return validTypes;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getField(int id) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        // TODO Auto-generated method stub

    }

    @Override
    public int getFieldCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }
}
