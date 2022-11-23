package am2.common.blocks.tileentity;

import am2.ArsMagica2;
import am2.api.ArsMagicaAPI;
import am2.api.blocks.IKeystoneLockable;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.AbstractSpellPart;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleHoldPosition;
import am2.common.defs.ItemDefs;
import am2.common.packet.AMDataReader;
import am2.common.packet.AMDataWriter;
import am2.common.packet.AMNetHandler;
import am2.common.packet.AMTileEntityPacketIDs;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.common.spell.SpellCaster;
import am2.common.spell.component.Summon;
import am2.common.spell.shape.Binding;
import am2.common.utils.InventoryUtilities;
import am2.common.utils.RecipeUtils;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class TileEntityArcaneDeconstructor extends TileEntityAMPower implements IInventory, ITileEntityPacketSync, ISidedInventory, IKeystoneLockable<TileEntityArcaneDeconstructor>{
	
	private static final int SYNC_DECONSTRUCTION_TIME = 0x1;
	private static final int SYNC_DECONSTRUCTION_RECIPE = 0x2;
	private static final int SYNC_INVENTORY = 0x4;

	private int particleCounter;
	private int syncCode = -1;
	private static final float DECONSTRUCTION_POWER_COST = 1.25f; //power cost per tick
	private static final int DECONSTRUCTION_TIME = 200; //how long does it take to deconstruct something?
	private int current_deconstruction_time = 0; //how long have we been deconstructing something?

	private static final ArrayList<PowerTypes> validPowerTypes = Lists.newArrayList(PowerTypes.DARK);

	@SideOnly(Side.CLIENT)
	AMParticle radiant;

	private ItemStack[] inventory;

	private ItemStack[] deconstructionRecipe;

	public TileEntityArcaneDeconstructor(){
		super(500);
		inventory = new ItemStack[getSizeInventory()];
	}

	@Override
	public boolean canRelayPower(PowerTypes type){
		return false;
	}

	@Override
	public int getChargeRate(){
		return 250;
	}

	@Override
	public void update(){
		super.update();

		if (world.isRemote){
			if (particleCounter == 0 || particleCounter++ > 1000){
				particleCounter = 1;
				radiant = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "radiant", pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
				if (radiant != null){
					radiant.setMaxAge(1000);
					radiant.setRGBColorF(0.1f, 0.1f, 0.1f);
					radiant.setParticleScale(0.1f);
					radiant.AddParticleController(new ParticleHoldPosition(radiant, 1000, 1, false));
				}
			}
		}else{
			if (!isActive()){
				if (inventory[0] != null){
					setDeconstructionTime(1);
				}
			}else{
				if (inventory[0] == null){
					setDeconstructionTime(0);
					deconstructionRecipe = null;
					this.syncCode |= SYNC_DECONSTRUCTION_RECIPE;
					this.markDirty();
					//world.markAndNotifyBlock(pos, world.getChunk(pos), world.getBlockState(pos), world.getBlockState(pos), 2);
				}else{
					if (PowerNodeRegistry.For(world).checkPower(this, PowerTypes.DARK, DECONSTRUCTION_POWER_COST)){
						if (deconstructionRecipe == null){
							if (!getDeconstructionRecipe()){
								transferOrEjectItem(inventory[0]);
								setInventorySlotContents(0, null);
							}
						}else{
							setDeconstructionTime(current_deconstruction_time + 1);
							if (current_deconstruction_time >= DECONSTRUCTION_TIME){
							        if(getDeconstructionRecipe() == true){
									for (ItemStack stack : deconstructionRecipe){
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
						PowerNodeRegistry.For(world).consumePower(this, PowerTypes.DARK, DECONSTRUCTION_POWER_COST);
					}
				}
			}
			if (this.shouldSync())
				AMNetHandler.INSTANCE.sendPacketToAllClientsNear(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 64F, AMTileEntityPacketIDs.ARCANE_DECONSTRUCTOR, this.createSyncPacket());
		}
	}

	private boolean getDeconstructionRecipe(){
		ItemStack checkStack = getStackInSlot(0);
		ArrayList<ItemStack> recipeItems = new ArrayList<ItemStack>();
		if (checkStack == null)
			return false;
		if (checkStack.getItem() == ItemDefs.spell && checkStack.hasCapability(SpellCaster.INSTANCE, null)){
			ISpellCaster spell = checkStack.getCapability(SpellCaster.INSTANCE, null);
			for (List<AbstractSpellPart> stage : spell.getSpellCommon()) {
				for (AbstractSpellPart part : stage) {
					Object[] componentParts = part.getRecipe();
					if (componentParts != null){
						for (Object o : componentParts){
							ItemStack stack = objectToItemStack(o);
							if (stack != null){
								if (stack.getItem() == ItemDefs.bindingCatalyst){
									stack.setItemDamage(((Binding)ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "binding"))).getBindingType(spell));
								} else if (stack.getItem() == ItemDefs.crystalPhylactery){
									// todo ItemDefs.crystalPhylactery.setSpawnClass(stack,((Summon)ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "summon"))).getSummonType(spell));
									ItemDefs.crystalPhylactery.addFill(stack, 100);
								}

								recipeItems.add(stack.copy());
							}
						}
					}					
				}
			}

			for (List<List<AbstractSpellPart>> shapeGroup : spell.getShapeGroups()){
				for (List<AbstractSpellPart> stage : shapeGroup) {
					for (AbstractSpellPart part : stage) {
						Object[] componentParts = part.getRecipe();
						if (componentParts != null){
							for (Object o : componentParts){
								ItemStack stack = objectToItemStack(o);
								if (stack != null){
									if (stack.getItem() == ItemDefs.bindingCatalyst){
										stack.setItemDamage(((Binding)ArsMagicaAPI.getSpellRegistry().getValue(new ResourceLocation("arsmagica2", "binding"))).getBindingType(spell));
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
		}else{
			IRecipe recipe = RecipeUtils.getRecipeFor(checkStack);
			if (recipe == null)
				return false;
			Object[] recipeParts = (Object[]) RecipeUtils.getRecipeItems(recipe);
			if (recipeParts != null && checkStack != null && recipe.getRecipeOutput() != null){
				if (recipe.getRecipeOutput().getItem() == checkStack.getItem() && recipe.getRecipeOutput().getItemDamage() == checkStack.getItemDamage() && recipe.getRecipeOutput().getCount() > 1)
					return false;

				for (Object o : recipeParts){
					ItemStack stack = objectToItemStack(o);
					if (stack != null && !stack.getItem().hasContainerItem(stack)){
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

	private ItemStack objectToItemStack(Object o){
		ItemStack output = null;
		if (o instanceof ItemStack)
			output = (ItemStack)o;
		else if (o instanceof Item)
			output = new ItemStack((Item)o);
		else if (o instanceof Block)
			output = new ItemStack((Block)o);
		else if (o instanceof List)
			output = objectToItemStack(((List<?>) o).get(0));

		if (output != null){
			if (output.getCount() == 0)
				output.setCount(1);
		}

		return output;
	}

	private void transferOrEjectItem(ItemStack stack){
		if (world.isRemote)
			return;

		for (int i = -1; i <= 1; ++i){
			for (int j = -1; j <= 1; ++j){
				for (int k = -1; k <= 1; ++k){
					if (i == 0 && j == 0 && k == 0)
						continue;
					TileEntity te = world.getTileEntity(pos.add(i, j, k));
					if (te != null && te instanceof IInventory){
						for (EnumFacing side : EnumFacing.values()){
							if (InventoryUtilities.mergeIntoInventory((IInventory)te, stack, stack.getCount(), side))
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
	
	public boolean isActive(){
		return current_deconstruction_time > 0;
	}

	@Override
	public int getSizeInventory(){
		return 16;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int var1){
		if (var1 >= inventory.length){
			return null;
		}
		return inventory[var1];
	}

	@Override
	public ItemStack decrStackSize(int i, int j){
		if (inventory[i] != null){
			if (inventory[i].getCount() <= j){
				ItemStack itemstack = inventory[i];
				inventory[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = inventory[i].splitStack(j);
			if (inventory[i].getCount() == 0){
				inventory[i] = null;
			}
			this.syncCode |= SYNC_INVENTORY;
			return itemstack1;
		}else{
			return null;
		}
	}

	@Override
	public ItemStack removeStackFromSlot(int i){
		if (inventory[i] != null){
			ItemStack itemstack = inventory[i];
			inventory[i] = null;
			this.syncCode |= SYNC_INVENTORY;
			return itemstack;
		}else{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack){
		inventory[i] = itemstack;
		this.syncCode |= SYNC_INVENTORY;
		if (itemstack != null && itemstack.getCount() > getInventoryStackLimit()){
			itemstack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName(){
		return "ArcaneDeconstructor";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return player.getDistanceSqToCenter(pos) <= 64D;
	}

	@Override
	public void openInventory(EntityPlayer player){
	}

	@Override
	public void closeInventory(EntityPlayer player){
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack){
		return i <= 9;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing var1){
		return new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing j){
		return i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing j){
		return i >= 1 && i <= 9;
	}

	@Override
	public ItemStack[] getRunesInKey(){
		return new ItemStack[]{
				inventory[13],
				inventory[14],
				inventory[15]
		};
	}

	@Override
	public boolean keystoneMustBeHeld(){
		return false;
	}

	@Override
	public boolean keystoneMustBeInActionBar(){
		return false;
	}

	@Override
	public List<PowerTypes> getValidPowerTypes(){
		return validPowerTypes;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound){
		super.readFromNBT(nbttagcompound);
		NBTTagList nbttaglist = nbttagcompound.getTagList("DeconstructorInventory", Constants.NBT.TAG_COMPOUND);
		inventory = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++){
			String tag = String.format("ArrayIndex", i);
			NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbttaglist.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte(tag);
			if (byte0 >= 0 && byte0 < inventory.length){
				inventory[byte0] = new ItemStack((nbttagcompound1));
			}
		}

		this.current_deconstruction_time = nbttagcompound.getInteger("DeconstructionTime");

		if (current_deconstruction_time > 0)
			getDeconstructionRecipe();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound){
		super.writeToNBT(nbttagcompound);
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventory.length; i++){
			if (inventory[i] != null){
				String tag = String.format("ArrayIndex", i);
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte(tag, (byte)i);
				inventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("DeconstructorInventory", nbttaglist);

		nbttagcompound.setInteger("DeconstructionTime", current_deconstruction_time);
		return nbttagcompound;
	}

	public int getProgressScaled(int i){
		return current_deconstruction_time * i / DECONSTRUCTION_TIME;
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

	@Override
	public byte[] createSyncPacket() {
		AMDataWriter writer = new AMDataWriter();
		writer.add(syncCode);
		if ((syncCode & SYNC_DECONSTRUCTION_TIME) == SYNC_DECONSTRUCTION_TIME)
			writer.add(current_deconstruction_time);
		if ((syncCode & SYNC_INVENTORY) == SYNC_INVENTORY) {
			for (int i = 0; i < inventory.length; i++) {
				ItemStack stack = inventory[i];
				if (stack != null) {
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
					if (stack != null) {
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
			for (int i = 0; i < inventory.length; i++) {
				if (reader.getBoolean()) {
					inventory[i] = new ItemStack((reader.getNBTTagCompound()));
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
