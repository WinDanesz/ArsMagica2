package am2.common.blocks.tileentity;

import am2.api.math.AMVector3;
import am2.common.blocks.BlockCrystalMarker;
import am2.common.utils.InventoryUtilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public class TileEntityCrystalMarker extends TileEntity implements IInventory, ISidedInventory, ITickable {
    private static final int SEARCH_RADIUS = 400;
    public static final int FILTER_SIZE = 9;


    private EnumFacing facing = EnumFacing.UP;
    private int priority = 0;
    private AMVector3 elementalAttuner = null;
    protected ItemStack[] filterItems;
    private int markerType;
    private AxisAlignedBB connectedBoundingBox;
    private int tickCount;

    public void setFacing(EnumFacing face) {
        this.facing = face;
    }

    public EnumFacing getFacing() {
        return this.facing;
    }

    public int getPriority() {
        return this.priority;
    }

    public void cyclePriority() {
        this.priority++;
        this.priority %= TileEntityFlickerHabitat.PRIORITY_LEVELS;
        if (!this.world.isRemote) {
            for (EntityPlayerMP player : this.world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(this.pos).expand(64, 64, 64))) {
                player.connection.sendPacket(this.getUpdatePacket());
            }
        }
    }

    public AxisAlignedBB GetConnectedBoundingBox() {
        if (this.connectedBoundingBox != null) {
            return this.connectedBoundingBox;
        } else {
            return new AxisAlignedBB(BlockPos.ORIGIN);
        }
    }

    public void SetConnectedBoundingBox(AxisAlignedBB boundingBox) {
        this.connectedBoundingBox = boundingBox;
    }

    public void SetConnectedBoundingBox(double minx, double miny, double minz, double maxx, double maxy, double maxz) {
        this.connectedBoundingBox = new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz);
    }

    public int getMarkerType() {
        return this.markerType;
    }

    public void setElementalAttuner(AMVector3 vector) {
        this.elementalAttuner = new AMVector3(vector.x, vector.y, vector.z);
    }

    public AMVector3 getElementalAttuner() {
        return this.elementalAttuner;
    }

    public void removeElementalAttuner() {
        this.elementalAttuner = null;
    }

    public boolean hasFilterItems() {
        boolean retVar = false;

        if (this.filterItems != null) {
            for (int i = 0; i < this.filterItems.length; i++) {
                if (this.filterItems[i] != null) {
                    retVar = true;
                    break;
                }
            }
        }

        return retVar;
    }

    /**
     * Checks if the crystal markers filter contains the passed item
     *
     * @param stack the item to check for
     * @return Returns true if the filter contains the item, false otherwise
     */
    public boolean filterHasItem(ItemStack stack) {
        boolean retVal = false;

        if (!stack.isEmpty() && this.hasFilterItems()) {
            for (int i = 0; i < this.filterItems.length; i++) {
                if (this.filterItems[i] != null && InventoryUtilities.compareItemStacks(this.filterItems[i], stack, true, false, true, true)) {
                    retVal = true;
                    break;
                }
            }
        }

        return retVal;
    }

    public int getFilterCount(ItemStack stack) {
        int totalCount = 0;

        if (this.hasFilterItems()) {
            for (int i = 0; i < this.filterItems.length; i++) {
                if (this.filterItems[i] != null && InventoryUtilities.compareItemStacks(this.filterItems[i], stack, true, false, true, true)) {
                    totalCount += this.filterItems[i].getCount();
                }
            }
        }

        return totalCount;
    }

    public TileEntityCrystalMarker() {
        this(0);
    }

    public TileEntityCrystalMarker(int markerType) {
        this.filterItems = new ItemStack[FILTER_SIZE];
        this.markerType = markerType;
        if (this.markerType == BlockCrystalMarker.META_FINAL_DEST)
            this.priority = TileEntityFlickerHabitat.PRIORITY_FINAL;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setInteger("facing", this.facing.ordinal());
        nbttagcompound.setInteger("priority", this.priority);
        nbttagcompound.setInteger("markerType", this.markerType);

        if (this.elementalAttuner != null) {
            NBTTagCompound elementalAttunerLocation = new NBTTagCompound();
            elementalAttunerLocation.setFloat("x", this.elementalAttuner.x);
            elementalAttunerLocation.setFloat("y", this.elementalAttuner.y);
            elementalAttunerLocation.setFloat("z", this.elementalAttuner.z);
            nbttagcompound.setTag("elementalAttuner", elementalAttunerLocation);
        }

        if (this.hasFilterItems()) {
            NBTTagList filterItemsList = new NBTTagList();

            for (int i = 0; i < this.getSizeInventory(); i++) {
                if (this.filterItems[i] != null) {
                    String tag = String.format("ArrayIndex", i);
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setByte(tag, (byte) i);
                    this.filterItems[i].writeToNBT(nbttagcompound1);
                    filterItemsList.appendTag(nbttagcompound1);
                }
            }

            nbttagcompound.setTag("filterItems", filterItemsList);
        }

        if (this.connectedBoundingBox != null) {
            NBTTagCompound connectedBoundingBoxDimensions = new NBTTagCompound();
            connectedBoundingBoxDimensions.setDouble("minx", this.connectedBoundingBox.minX);
            connectedBoundingBoxDimensions.setDouble("miny", this.connectedBoundingBox.minY);
            connectedBoundingBoxDimensions.setDouble("minz", this.connectedBoundingBox.minZ);
            connectedBoundingBoxDimensions.setDouble("maxx", this.connectedBoundingBox.maxX);
            connectedBoundingBoxDimensions.setDouble("maxy", this.connectedBoundingBox.maxY);
            connectedBoundingBoxDimensions.setDouble("maxz", this.connectedBoundingBox.maxZ);
            nbttagcompound.setTag("connectedBoundingBox", connectedBoundingBoxDimensions);
        }
        return nbttagcompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound par1) {
        super.readFromNBT(par1);
        this.filterItems = new ItemStack[FILTER_SIZE];
        if (par1.hasKey("facing")) {
            this.facing = EnumFacing.values()[par1.getInteger("facing")];
        }

        if (par1.hasKey("priority")) {
            this.priority = par1.getInteger("priority");
        }

        if (par1.hasKey("markerType")) {
            this.markerType = par1.getInteger("markerType");
            if (this.markerType == BlockCrystalMarker.META_FINAL_DEST)
                this.priority = TileEntityFlickerHabitat.PRIORITY_FINAL;
        }

        //Get elemental attuner location
        if (par1.hasKey("elementalAttuner")) {
            float x = 0;
            float y = 0;
            float z = 0;
            boolean success = true;
            NBTTagCompound elementalAttunerLocation = par1.getCompoundTag("elementalAttuner");

            if (elementalAttunerLocation.hasKey("x")) {
                x = elementalAttunerLocation.getFloat("x");
            } else {
                success = false;
            }

            if (success && elementalAttunerLocation.hasKey("y")) {
                y = elementalAttunerLocation.getFloat("y");
            } else {
                success = false;
            }

            if (success && elementalAttunerLocation.hasKey("z")) {
                z = elementalAttunerLocation.getFloat("z");
            } else {
                success = false;
            }

            if (success) {
                this.elementalAttuner = new AMVector3(x, y, z);
            }
        }

        //Load filter items
        if (par1.hasKey("filterItems")) {
            NBTTagList filterItemsList = par1.getTagList("filterItems", Constants.NBT.TAG_COMPOUND);
            this.filterItems = new ItemStack[this.getSizeInventory()];
            for (int i = 0; i < filterItemsList.tagCount(); i++) {
                String tag = String.format("ArrayIndex", i);
                NBTTagCompound nbttagcompound1 = filterItemsList.getCompoundTagAt(i);
                byte byte0 = nbttagcompound1.getByte(tag);
                if (byte0 >= 0 && byte0 < this.filterItems.length) {
                    this.filterItems[byte0] = new ItemStack((nbttagcompound1));
                }
            }
        }//end if(par1.hasKey("filterItems")){

        //Get connected block bounding box
        if (par1.hasKey("connectedBoundingBox")) {
            NBTTagCompound connectedBoundingBoxDimensions = par1.getCompoundTag("connectedBoundingBox");
            double minx = connectedBoundingBoxDimensions.getDouble("minx");
            double miny = connectedBoundingBoxDimensions.getDouble("miny");
            double minz = connectedBoundingBoxDimensions.getDouble("minz");
            double maxx = connectedBoundingBoxDimensions.getDouble("maxx");
            double maxy = connectedBoundingBoxDimensions.getDouble("maxy");
            double maxz = connectedBoundingBoxDimensions.getDouble("maxz");
            this.connectedBoundingBox = new AxisAlignedBB(minx, miny, minz, maxx, maxy, maxz);
        }
    }

    @Override
    public int getSizeInventory() {
        if (this.markerType == BlockCrystalMarker.META_SET_EXPORT || this.markerType == BlockCrystalMarker.META_REGULATE_EXPORT || this.markerType == BlockCrystalMarker.META_REGULATE_MULTI || this.markerType == BlockCrystalMarker.META_SET_IMPORT) {
            return FILTER_SIZE;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        if (i >= FILTER_SIZE) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = this.filterItems[i];
        return (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        if (this.filterItems[i] != null && !this.filterItems[i].isEmpty()) {
            if (this.filterItems[i].getCount() <= j) {
                ItemStack itemstack = this.filterItems[i];
                this.filterItems[i] = null;
                return itemstack;
            }
            ItemStack itemstack1 = this.filterItems[i].splitStack(j);
            if (this.filterItems[i].getCount() == 0) {
                this.filterItems[i] = null;
            }
            return itemstack1;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int i) {
        if (this.filterItems[i] != null && !this.filterItems[i].isEmpty()) {
            ItemStack itemstack = this.filterItems[i];
            this.filterItems[i] = null;
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        this.filterItems[i] = itemstack;
        if (!itemstack.isEmpty() && itemstack.getCount() > this.getInventoryStackLimit()) {
            itemstack.setCount(this.getInventoryStackLimit());
        }
    }

    @Override
    public String getName() {
        return "Crystal Marker";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        if (this.markerType == BlockCrystalMarker.META_REGULATE_EXPORT || this.markerType == BlockCrystalMarker.META_REGULATE_MULTI) {
            return 128;
        } else {
            return 1;
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityplayer) {
        if (this.world.getTileEntity(this.pos) != this) {
            return false;
        }
        return entityplayer.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64D;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return false;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new SPacketUpdateTileEntity(this.pos, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos);
    }

    public void linkToHabitat(AMVector3 habLocation, EntityPlayer player) {
        TileEntity te = this.world.getTileEntity(habLocation.toBlockPos());

        if (te instanceof TileEntityFlickerHabitat) {
            AMVector3 myLocation = new AMVector3(this.pos);
            boolean setElementalAttuner = false;

            if (myLocation.distanceSqTo(habLocation) <= SEARCH_RADIUS) {
                if (BlockCrystalMarker.isInputMarker(this.getBlockMetadata())) {
                    ((TileEntityFlickerHabitat) te).AddMarkerLocationIn(myLocation);
                    setElementalAttuner = true;
                }

                if (BlockCrystalMarker.isOutputMarker(this.getBlockMetadata())) {
                    ((TileEntityFlickerHabitat) te).AddMarkerLocationOut(myLocation);
                    setElementalAttuner = true;
                }

                if (setElementalAttuner)
                    this.setElementalAttuner(habLocation);
            } else {
                player.sendMessage(new TextComponentTranslation("am2.tooltip.habitatToFar"));
            }
        }
    }

    @Override
    public int[] getSlotsForFace(EnumFacing var1) {
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j) {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j) {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
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

    @Override
    public void update() {
        if (!this.world.isRemote) {
            IBlockState state = this.world.getBlockState(this.pos);
            if (!state.getValue(BlockCrystalMarker.FACING).equals(facing))
                this.world.setBlockState(this.pos, state.withProperty(BlockCrystalMarker.FACING, this.facing), 3);
            //This is probably the fastest I can get it to go.
            //If you know of any better way, please feel free to suggest it.
            if (++tickCount % 20 == 0)
                this.world.notifyBlockUpdate(this.pos, state, state, 2);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
