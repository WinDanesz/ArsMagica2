package am2.common.blocks.tileentity;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Lists;

import am2.api.ArsMagicaAPI;
import am2.api.affinity.Affinity;
import am2.api.math.AMVector3;
import am2.common.LogHelper;
import am2.common.blocks.tileentity.flickers.TileEntityFlickerControllerBase;
import am2.common.defs.BlockDefs;
import am2.common.defs.ItemDefs;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class TileEntityFlickerHabitat extends TileEntityFlickerControllerBase implements IInventory{
	private static final float FULL_CIRCLE = 360.0f;
	private static final float ROATATION_RATE = 1.0f;
	private static final float FLOAT_RATE = 0.001f;
	private static final float MAX_FLOAT_UP = 0.1f;
	private static final float MAX_FLOAT_DOWN = -0.04f;

	public static final int PRIORITY_LEVELS = 10;
	public static final int PRIORITY_FINAL = PRIORITY_LEVELS + 1;

	private static final float MAX_SHIFT_TICKS = 20.0f;

	private int colorCounter = 0;
	private int fadeCounter = 0;

	private ArrayList<AMVector3> inList;
	private HashMap<Integer, ArrayList<AMVector3>> outList;
	private int inListPosition = 0;
	private HashMap<Integer, Integer> outListPositions = new HashMap<Integer, Integer>();
	private int defoutListPosition = 0;
	private ItemStack flickerJar;
	private float rotateOffset = 0;
	private float floatOffset = 0;
	private boolean floatUp = true;
	private boolean isUpgrade = false;
	private EnumFacing mainHabitatDirection = null;


	public boolean isUpgrade(){
		return this.isUpgrade;
	}

	public void setUpgrade(boolean isUpgrade, EnumFacing direction){
		this.isUpgrade = isUpgrade;
		this.mainHabitatDirection = direction;
	}

	public EnumFacing getMainHabitatDirection(){
		return this.mainHabitatDirection;
	}

	public TileEntityFlickerHabitat(){
		this.initLocationLists();

		if (this.world != null && this.world.isRemote){
			this.rotateOffset = this.world.rand.nextFloat() * FULL_CIRCLE - 1;
			this.floatOffset = MAX_FLOAT_DOWN + (this.world.rand.nextFloat() * (MAX_FLOAT_UP - MAX_FLOAT_DOWN) + 1);
		}
	}

	private void initLocationLists(){
		this.inList = new ArrayList<AMVector3>();
		this.outList = new HashMap<Integer, ArrayList<AMVector3>>();

		for (int i = 0; i < PRIORITY_LEVELS; ++i)
			this.outList.put(i, new ArrayList<AMVector3>());
	}

	public Affinity getSelectedAffinity(){
		if (this.flickerJar != null){
//			return ArsMagicaAPI.getAffinityRegistry().getObjectById(this.flickerJar.getItemDamage());
			// TODO
			return null;
		}else{
			return null;
		}
	}

	public boolean hasFlicker(){
		return this.flickerJar != null;
	}

	/**
	 * @return the inListPosition
	 */
	public int getInListPosition(){
		return this.inListPosition;
	}

	/**
	 * @param inListPosition the inListPosition to set
	 */
	public void setInListPosition(int inListPosition){
		this.inListPosition = inListPosition;
	}


	/**
	 * @return the outListPosition
	 */
	public int getOutListPosition(int priority){
		if (!this.outListPositions.containsKey(priority))
			this.outListPositions.put(priority, 0);

		return this.outListPositions.get(priority);
	}

	/**
	 * @param outListPosition the outListPosition to set
	 */
	public void setOutListPosition(int priority, int outListPosition){
		this.outListPositions.put(priority, outListPosition);
	}

	public int getDeferredOutListPosition(){
		return this.defoutListPosition;
	}

	public void setDeferredOutListPosition(int position){
		this.defoutListPosition = position;
	}

	/**
	 * @return the inList
	 */
	public AMVector3 getInListAt(int index){
		return this.inList.get(index);
	}
	
	public void setInListAt(int index, AMVector3 value){
		this.inList.set(index, value);
	}

	public void removeInListAt(int index){
		this.inList.remove(index);
	}

	public void removeInListAt(AMVector3 value){
		this.inList.remove(value);
	}

	public int getInListSize(){
		return this.inList.size();
	}

	public AMVector3 getOutListAt(int priority, int index){
		if (this.outList != null && this.outList.containsKey(priority) && this.outList.get(priority).size() > 0)
			return this.outList.get(priority).get(index);
		return new AMVector3(this);
	}

	public void setOutListAt(int priority, int index, AMVector3 value){
		if (this.outList.containsKey(priority))
			this.outList.get(priority).set(index, value);
	}

	public void removeOutListAt(int priority, int index){
		if (this.outList.containsKey(priority))
			this.outList.get(priority).remove(index);
	}

	public void removeOutListAt(int priority, AMVector3 value){
		if (!this.outList.containsKey(priority))
			return;
		this.outList.get(priority).remove(value);
	}

	public int getOutListSize(int priority){
		if (this.outList.containsKey(priority))
			return this.outList.get(priority).size();
		return 0;
	}

	/**
	 * @return the rotateOffset
	 */
	public float getRotateOffset(){
		return this.rotateOffset;
	}

	public float getFloatOffset(){
		return this.floatOffset;
	}

	public void AddMarkerLocationIn(AMVector3 markerLocation){
		if (!this.inList.contains(markerLocation)){
			this.inList.add(markerLocation);
			LogHelper.trace("In Link Created");
		}else{
			LogHelper.trace("Link Already Exists");
		}
	}

	public void AddMarkerLocationOut(AMVector3 markerLocation){

		Block out = this.world.getBlockState(markerLocation.toBlockPos()).getBlock();
		if (out != BlockDefs.crystalMarker)
			return;

		TileEntity te = this.world.getTileEntity(markerLocation.toBlockPos());
		if (te == null || te instanceof TileEntityCrystalMarker == false)
			return;

		int priority = ((TileEntityCrystalMarker)te).getPriority();

		if (!this.outList.containsKey(priority))
			this.outList.put(priority, new ArrayList<AMVector3>());

		if (!this.outList.get(priority).contains(markerLocation)){
			this.outList.get(priority).add(markerLocation);
			LogHelper.trace("Out Link Create");
		}else{
			LogHelper.trace("Link Already Exists");
		}
	}

	public void removeInMarkerLocation(BlockPos pos){
		this.inList.remove(new AMVector3(pos));
	}

	public void removeOutMarkerLocation(BlockPos pos) {
		for (ArrayList<AMVector3> ls : this.outList.values())
			ls.remove(new AMVector3(pos));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);

		//write in list
		NBTTagList inItems = new NBTTagList();
		for (AMVector3 inItem : this.inList){
			NBTTagCompound vectorItem = new NBTTagCompound();
			vectorItem.setFloat("x", inItem.x);
			vectorItem.setFloat("y", inItem.y);
			vectorItem.setFloat("z", inItem.z);
			inItems.appendTag(vectorItem);
		}
		nbttagcompound.setTag("InList", inItems);

		//write out list
		this.writeOutList(nbttagcompound);

		if (this.flickerJar != null){
			NBTTagCompound jar = new NBTTagCompound();
			this.flickerJar.writeToNBT(jar);
			nbttagcompound.setTag("flickerJar", jar);
		}

		//write upgrade status
		nbttagcompound.setBoolean("upgrade", this.isUpgrade);
		if (this.isUpgrade){
			nbttagcompound.setInteger("mainHabitatDirection", this.mainHabitatDirection.ordinal());
		}
		return nbttagcompound;
	}

	private void writeOutList(NBTTagCompound compound){
		NBTTagList outputList = new NBTTagList();

		for (int priority : this.outList.keySet()){
			//create a compound for the priority
			NBTTagCompound priorityCompound = new NBTTagCompound();
			//attach the priority to the compound
			priorityCompound.setInteger("priority", priority);

			//get the list of locations for this priority
			ArrayList<AMVector3> priorityList = this.outList.get(priority);
			if (priorityList == null)
				continue;

			//create a tag list to store the vectors in
			NBTTagList vectors = new NBTTagList();
			//spin through the list
			for (AMVector3 vec : priorityList){
				//create a compound to hold the individual vector
				NBTTagCompound vectorItem = new NBTTagCompound();
				//write the vector to the newly created compound
				vec.writeToNBT(vectorItem);
				//attach the vector tag to the vectors list
				vectors.appendTag(vectorItem);
			}
			//attach the vectors to the priority compound
			priorityCompound.setTag("vectors", vectors);

			//attach the priority compound to the final output list
			outputList.appendTag(priorityCompound);
		}

		//store the final output list in the parent compound
		compound.setTag("outList", outputList);
	}

	private void readOutList(NBTTagCompound compound){
		//valid compound?
		if (!compound.hasKey("outList"))
			return;
		//get the tag list for the output data
		NBTTagList outputList = compound.getTagList("outList", Constants.NBT.TAG_COMPOUND);
		//spin through em
		for (int i = 0; i < outputList.tagCount(); ++i){
			//get the current compound tag - this should contain a priority level and a list of vectors
			NBTTagCompound priorityCompound = outputList.getCompoundTagAt(i);
			//create the list to hold the output locations
			ArrayList<AMVector3> locationsInPriority = new ArrayList<AMVector3>();
			//does the current compound tag contain the values we're looking for?
			if (!priorityCompound.hasKey("priority") || !priorityCompound.hasKey("vectors")){
				LogHelper.warn("Malformed save data for flicker item transport controller - cannot process records.");
				continue;
			}
			//get the priority from the compound
			int priority = priorityCompound.getInteger("priority");
			//get the list of vectors from the compound
			NBTTagList vectors = priorityCompound.getTagList("vectors", Constants.NBT.TAG_COMPOUND);
			//spin through the vectors
			for (int x = 0; x < vectors.tagCount(); ++x){
				//get the current vector tag
				NBTTagCompound vectorItem = vectors.getCompoundTagAt(x);
				//read the vector from the NBT compound
				AMVector3 vec = AMVector3.readFromNBT(vectorItem);
				//add the vector location if it read correctly
				if (vec != null && vec != AMVector3.zero()){
					locationsInPriority.add(vec);
				}
			}

			//insert the list into the output list at the specified priority
			this.outList.put(priority, locationsInPriority);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);

		this.initLocationLists();

		//read in list
		if (nbttagcompound.hasKey("InList")){
			NBTTagList inItems = nbttagcompound.getTagList("InList", Constants.NBT.TAG_COMPOUND);

			for (int i = 0; i < inItems.tagCount(); ++i){
				NBTTagCompound inItem = inItems.getCompoundTagAt(i);
				if (inItem == null){
					continue;
				}

				float x = 0;
				float y = 0;
				float z = 0;
				boolean success = true;

				if (inItem.hasKey("x")){
					x = inItem.getFloat("x");
				}else{
					success = false;
				}

				if (success && inItem.hasKey("y")){
					y = inItem.getFloat("y");
				}else{
					success = false;
				}

				if (success && inItem.hasKey("z")){
					z = inItem.getFloat("z");
				}else{
					success = false;
				}

				if (success){
					this.inList.add(new AMVector3(x, y, z));
				}
			}//end for(int i = 0; i < inItems.tagCount(); ++i)
		}//end if(nbttagcompound.hasKey("InList"))

		this.readOutList(nbttagcompound);

		if (nbttagcompound.hasKey("upgrade")){
			this.isUpgrade = nbttagcompound.getBoolean("upgrade");
		}

		if (nbttagcompound.hasKey("flickerJar")){
			NBTTagCompound jar = nbttagcompound.getCompoundTag("flickerJar");
			this.flickerJar = new ItemStack((jar));

			if (!this.isUpgrade){
				this.setOperatorBasedOnFlicker();
			}
		}


		if (this.isUpgrade){
			int flag = nbttagcompound.getInteger("mainHabitatDirection");

			for (EnumFacing direction : EnumFacing.values()){
				if (direction.ordinal() == flag){
					this.mainHabitatDirection = direction;
					break;
				}
			}
		}
	}

	public int getCrystalColor(){
		if (this.flickerJar == null)
			return 0;

		if (this.flickerJar.getItem() == ItemDefs.flickerJar)
			// TODO registry
			return 0;
//			return ArsMagicaAPI.getAffinityRegistry().getObjectById(this.flickerJar.getItemDamage()).getColor();
		else if (this.flickerJar.getItem() == ItemDefs.flickerFocus){
			// TODO registry
			return 0;
//			ArrayList<Affinity> affinities = Lists.newArrayList(ArsMagicaAPI.getFlickerFocusRegistry().getObjectById(MathHelper.clamp(this.flickerJar.getItemDamage(), 0, ArsMagicaAPI.getFlickerFocusRegistry().getKeys().size())).getMask());
//
//			if (affinities.size() > 0){
//				int firstColor = affinities.get(this.colorCounter % affinities.size()).getColor();
//				int secondColor = affinities.get((this.colorCounter + 1) % affinities.size()).getColor();
//				if (firstColor == secondColor)
//					return firstColor;
//				if (this.fadeCounter > TileEntityFlickerHabitat.MAX_SHIFT_TICKS)
//					return secondColor;
//				return this.colorShift(firstColor, secondColor);
//			}
		}

		return 0;
	}

	private int colorShift(int f, int s){
		int fr = (f >> 16) & 0xFF;
		int fg = (f >> 8) & 0xFF;
		int fb = (f) & 0xFF;

		int sr = (s >> 16) & 0xFF;
		int sg = (s >> 8) & 0xFF;
		int sb = (s) & 0xFF;

		float dr = (sr - fr) / MAX_SHIFT_TICKS;
		float dg = (sg - fg) / MAX_SHIFT_TICKS;
		float db = (sb - fb) / MAX_SHIFT_TICKS;

		int combined =
				(((fr + (int)(dr * this.fadeCounter)) & 0xFF) << 16) |
						(((fg + (int)(dg * this.fadeCounter)) & 0xFF) << 8) |
						(((fb + (int)(db * this.fadeCounter)) & 0xFF));

		return combined;
	}

	@Override
	public int getSizeInventory(){
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int i){
		if (i <= this.getSizeInventory() && this.flickerJar != null){
			return this.flickerJar;
		}

		return null;
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (i <= this.getSizeInventory() && this.flickerJar != null){
			ItemStack jar = this.flickerJar;
			this.flickerJar = null;
			return jar;
		}
		return null;
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (i <= this.getSizeInventory() && this.flickerJar != null){
			ItemStack jar = this.flickerJar;
			this.flickerJar = null;
			return jar;
		}
		return null;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		this.flickerJar = itemstack;
		if (itemstack != null && itemstack.getCount() > this.getInventoryStackLimit()){
			itemstack.setCount(this.getInventoryStackLimit());
		}

	}

	@Override
	public String getName(){
		return "Flicker Habitat";
	}

	@Override
	public int getInventoryStackLimit(){
		return 1;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer entityplayer){
		if (this.world.getTileEntity(this.pos) != this){
			return false;
		}

		return entityplayer.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
		if (!this.isUpgrade){
			this.setOperatorBasedOnFlicker();
			this.scanForNearbyUpgrades();
		}else{
			this.setUpgradeOfMainHabitat();
		}
	}

	private void setUpgradeOfMainHabitat(){
		if (this.mainHabitatDirection != null){
			TileEntity te = this.world.getTileEntity(this.pos.offset(this.mainHabitatDirection));
			if (te != null && te instanceof TileEntityFlickerHabitat){
				((TileEntityFlickerHabitat)te).notifyOfNearbyUpgradeChange(this);
			}
		}
	}

	private void setOperatorBasedOnFlicker(){
		if (this.flickerJar != null && this.flickerJar.getItem() == ItemDefs.flickerFocus){
			// TODO registry
			//this.setOperator(ArsMagicaAPI.getFlickerFocusRegistry().getObjectById(this.flickerJar.getItemDamage()));
		}else{
			this.setOperator(null);
		}

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return false;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(this.pos, 1, this.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		this.readFromNBT(pkt.getNbtCompound());
		this.world.markAndNotifyBlock(this.pos, this.world.getChunk(this.pos), this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 0);
	}

	@Override
	public void update(){
		super.update();
		
		if (this.fadeCounter++ >= 30){
			this.colorCounter++;
			this.fadeCounter = 0;
		}

		if (this.world.isRemote && this.hasFlicker()){
			this.rotateOffset += ROATATION_RATE;

			if (this.rotateOffset >= FULL_CIRCLE){
				this.rotateOffset -= FULL_CIRCLE;
			}

			if (this.floatUp){
				this.floatOffset += FLOAT_RATE;

				if (this.floatOffset >= MAX_FLOAT_UP){
					this.invertDirection();
				}
			}else{
				this.floatOffset -= FLOAT_RATE;
				if (this.floatOffset <= MAX_FLOAT_DOWN){
					this.invertDirection();
				}
			}
		}
	}

	private void invertDirection(){
		this.floatUp = !this.floatUp;
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}


	public void switchMarkerPriority(AMVector3 vec, int oldPriority, int priority){
		if (this.outList.containsKey(oldPriority))
			this.removeOutListAt(oldPriority, vec);

		if (!this.outList.containsKey(priority))
			this.outList.put(priority, new ArrayList<AMVector3>());

		this.outList.get(priority).add(vec);
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
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {}
}


