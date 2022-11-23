package am2.common.power;

import am2.api.power.IPowerNode;
import am2.common.utils.NBTUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class PowerNodeEntry{
	private HashMap<PowerTypes, Float> powerAmounts;
	private HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> nodePaths;

	public PowerNodeEntry(){
		this.powerAmounts = new HashMap<>();
		this.nodePaths = new HashMap<>();
	}

	public void clearNodePaths(){
		for (PowerTypes type : this.nodePaths.keySet()){
			this.nodePaths.get(type).clear();
		}
	}

	public void registerNodePath(PowerTypes type, LinkedList<BlockPos> path){
		ArrayList<LinkedList<BlockPos>> paths = this.nodePaths.computeIfAbsent(type, k -> new ArrayList<>());

		//do we already have a path that ends here?
		Iterator<LinkedList<BlockPos>> it = paths.iterator();
		while (it.hasNext()){
			if (it.next().getLast().equals(path.getLast())){
				it.remove();
				break;
			}
		}

		paths.add(path);
	}

	public float requestPower(World world, PowerTypes type, float amount, float capacity){
		if (this.getPower(type) >= capacity)
			return 0f;
		ArrayList<LinkedList<BlockPos>> paths = this.nodePaths.get(type);
		if (paths == null || paths.size() == 0){
			//AMCore.log.info("No Paths!");
			return 0;
		}

		//AMCore.log.info("Path Exists");

		if (this.powerAmounts.containsKey(type) && this.powerAmounts.get(type) + amount > capacity){
			amount = capacity - this.powerAmounts.get(type);
		}

		float requested = 0f;
		for (LinkedList<BlockPos> path : paths){
			requested += this.requestPowerFrom(world, path, type, amount - requested);
			if (requested >= amount)
				break;
		}
		return requested;
	}

	private boolean validatePath(World world, LinkedList<BlockPos> path){
		for (BlockPos vec : path){
			//power can't transfer through unloaded chunks!
			Chunk chunk = world.getChunk(vec);
			if (!chunk.isLoaded())
				return false;
			TileEntity te = world.getTileEntity(vec);
			//if valid, continue the loop, otherwise return false.
			if (te != null && te instanceof IPowerNode)
				continue;

			//set a marker block to say that a conduit or other power relay of some sort was here and is now not
//			if (!world.isRemote && world.isAirBlock(vec)){
//				world.setBlockState(vec, BlockDefs.brokenPowerLink.getDefaultState());
//			}

			return false;
		}
		//if we're here, then all locations checked out
		return true;
	}

	private float requestPowerFrom(World world, LinkedList<BlockPos> path, PowerTypes type, float amount){
		if (!this.validatePath(world, path))
			return 0f;
		BlockPos end = path.getLast();
		TileEntity te = world.getTileEntity(end);
		if (te != null && te instanceof IPowerNode){
			if (((IPowerNode<?>)te).canProvidePower(type)){
				return PowerNodeRegistry.For(world).consumePower(((IPowerNode<?>)te), type, amount);
			}
		}
		return 0f;
	}

	public PowerTypes getHighestPowerType(){
		float highest = 0;
		PowerTypes hType = PowerTypes.NONE;
		for (PowerTypes type : this.powerAmounts.keySet()){
			if (this.powerAmounts.get(type) > highest){
				highest = this.powerAmounts.get(type);
				hType = type;
			}
		}
		return hType;
	}

	public float getHighestPower(){
		float highest = 0;
		for (PowerTypes type : this.powerAmounts.keySet()){
			if (this.powerAmounts.get(type) > highest){
				highest = this.powerAmounts.get(type);
			}
		}
		return highest;
	}

	public float getPower(PowerTypes type){
		Float f = this.powerAmounts.get(type);
		return f == null ? 0 : f;
	}

	public void setPower(PowerTypes type, float amt){
		if (type != null)
			this.powerAmounts.put(type, amt);
	}

	public NBTTagCompound saveToNBT(){
		NBTTagCompound compound = new NBTTagCompound();

		//power amounts
		//list of entries containing power type IDs and the associated amount
		NBTTagList powerAmountStore = new NBTTagList();
		for (PowerTypes type : this.powerAmounts.keySet()){
			if (type == null) //sanity check
				continue;
			//individual power type/amount entry
			NBTTagCompound powerType = new NBTTagCompound();
			//set power type ID
			powerType.setInteger("powerType", type.ID());
			//set power amount
			powerType.setFloat("powerAmount", this.powerAmounts.get(type));
			//attach the power node to the list
			powerAmountStore.appendTag(powerType);
		}
		//append list to output compound
		compound.setTag("powerAmounts", powerAmountStore);

		//power paths
		NBTTagList powerPathList = new NBTTagList();

		for (PowerTypes type : this.nodePaths.keySet()){

			//This is the actual entry in the power path list
			NBTTagCompound powerPathEntry = new NBTTagCompound();

			ArrayList<LinkedList<BlockPos>> paths = this.nodePaths.get(type);
			//This stores each path individually for a given power type
			NBTTagList pathsForType = new NBTTagList();
			for (LinkedList<BlockPos> path : paths){
				//This stores each individual node in the given path
				NBTTagList pathNodes = new NBTTagList();
				for (BlockPos pathNode : path){
					//This stores one individual node in the given path
					NBTTagCompound node = new NBTTagCompound();
					NBTUtils.writeBlockPosToNBT(pathNode, node);
					//Append individual node to path
					pathNodes.appendTag(node);
				}
				//Append path to list of paths for the power type
				pathsForType.appendTag(pathNodes);
			}

			//set the power type that this list of paths is for
			powerPathEntry.setInteger("powerType", type.ID());
			//append the list of paths to the entry in the power path list
			powerPathEntry.setTag("nodePaths", pathsForType);

			//AMCore.log.info("Saved %d node paths for %s etherium.", nodePaths.get(type).size(), type.name());

			//append this entry in the power path list to the list of power path entries
			powerPathList.appendTag(powerPathEntry);
		}
		//append the entire power path list to the saved compound
		compound.setTag("powerPathList", powerPathList);

		return compound;
	}

	public void readFromNBT(NBTTagCompound compound){
		//power amounts
		//locate the list of power amounts
		NBTTagList powerAmountStore = compound.getTagList("powerAmounts", Constants.NBT.TAG_COMPOUND);
		//sanity check
		//spin through nodes
		for (int i = 0; i < powerAmountStore.tagCount(); ++i){
			//reference current node
			NBTTagCompound powerType = powerAmountStore.getCompoundTagAt(i);
			//resolve power type
			PowerTypes type = PowerTypes.getByID(powerType.getInteger("powerType"));
			//resolve power amount
			float powerAmount = powerType.getFloat("powerAmount");
			//register entry
			this.powerAmounts.put(type, powerAmount);
		}

		//power paths
		//locate list of power paths
		NBTTagList powerPathList = compound.getTagList("powerPathList", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < powerPathList.tagCount(); ++i){
			NBTTagCompound powerPathEntry = powerPathList.getCompoundTagAt(i);
			PowerTypes type = PowerTypes.getByID(powerPathEntry.getInteger("powerType"));
			NBTTagList pathNodes = powerPathEntry.getTagList("nodePaths", Constants.NBT.TAG_LIST);
			ArrayList<LinkedList<BlockPos>> pathsList = new ArrayList<>();
			for (int j = 0; j < pathNodes.tagCount(); j++){
				NBTTagList nodeList = (NBTTagList)pathNodes.get(j);
				LinkedList<BlockPos> powerPath = new LinkedList<>();
				for (int b = 0; b < nodeList.tagCount(); ++b){
					NBTTagCompound node = nodeList.getCompoundTagAt(b);
					BlockPos nodeLocation = NBTUtils.readBlockPosFromNBT(node);
					powerPath.add(nodeLocation);
				}
				pathsList.add(powerPath);
			}
			this.nodePaths.put(type, pathsList);
			//ArsMagica2.LOGGER.info(String.format("Loaded %d node paths for %s etherium.", pathsList.size(), type.name()));
		}
	}

	@SuppressWarnings("unchecked")
	public HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>> getNodePaths(){
		return (HashMap<PowerTypes, ArrayList<LinkedList<BlockPos>>>) this.nodePaths.clone();
	}
}