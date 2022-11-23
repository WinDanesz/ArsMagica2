package am2.common.items;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class ItemCrystalPhylactery extends ItemArsMagica{

	public final HashMap<String, Integer> spawnableEntities;

	public static final int META_EMPTY = 0;
	public static final int META_QUARTER = 1;
	public static final int META_HALF = 2;
	public static final int META_FULL = 3;


	public ItemCrystalPhylactery(){
		super();
		this.spawnableEntities = new HashMap<>();
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.hasTagCompound()){
			String className = stack.getTagCompound().getString("SpawnClassName");
			tooltip.add(I18n.format("am2.tooltip.phyEss", I18n.format("entity." + className + ".name")));
			float pct = stack.getTagCompound().getFloat("PercentFilled");
			tooltip.add(I18n.format("am2.tooltip.pctFull", pct));
		}else{
			tooltip.add(I18n.format("am2.tooltip.empty"));
		}
	}
	
	public void addFill(ItemStack stack){
		if (stack.hasTagCompound()){
			float pct = stack.getTagCompound().getFloat("PercentFilled");
			pct += itemRand.nextFloat() * 5;
			if (pct > 100) pct = 100;
			stack.getTagCompound().setFloat("PercentFilled", pct);
			if (pct == 100)
				stack.setItemDamage(META_FULL);
			else if (pct > 50)
				stack.setItemDamage(META_HALF);
			else if (pct > 25)
				stack.setItemDamage(META_QUARTER);
			else
				stack.setItemDamage(META_EMPTY);

		}
	}

	public void addFill(ItemStack stack, float amt){
		if (stack.hasTagCompound()){
			float pct = stack.getTagCompound().getFloat("PercentFilled");
			pct += amt;
			if (pct > 100) pct = 100;
			stack.getTagCompound().setFloat("PercentFilled", pct);
			if (pct == 100)
				stack.setItemDamage(META_FULL);
			else if (pct > 50)
				stack.setItemDamage(META_HALF);
			else if (pct > 25)
				stack.setItemDamage(META_QUARTER);
			else
				stack.setItemDamage(META_EMPTY);

		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack par1ItemStack){
		return par1ItemStack.getItemDamage() == META_FULL;
	}

	// TODO
	public void setSpawnClass(ItemStack stack, Class<? extends Entity> clazz){
//
//		if (!stack.hasTagCompound())
//			stack.setTagCompound(new NBTTagCompound());
//
//		String s = EntityList.CLASS_TO_NAME.get(clazz);
//		if (s != null)
//			stack.getTagCompound().setString("SpawnClassName", s);
	}

		public boolean canStore(ItemStack stack, EntityLiving entity){
		return false;
	//		if (!entity.isNonBoss()) return false;
	//		if (stack.getItemDamage() == META_FULL)
	//			return false;
	//		if (!stack.hasTagCompound())
	//			return true;
	//
	//		String e = stack.getTagCompound().getString("SpawnClassName");
	//		String s = EntityList.CLASS_TO_NAME.get(entity.getClass());
	//
	//		return (e != null && s != null) && e.equals(s);
	//	}
	//
	//	public boolean isFull(ItemStack stack){
	//		return stack.getItemDamage() == META_FULL;
		}

	public boolean isFull(ItemStack stack) {
		return false;
	}

	public String getSpawnClass(ItemStack stack){
		if (!stack.hasTagCompound())
			return null;
		return stack.getTagCompound().getString("SpawnClassName");
	}
//
	public void getSpawnableEntities(World world){
//		for (Class<? extends Entity> clazz : EntityList.CLASS_TO_NAME.keySet()){
//			if (EntityCreature.class.isAssignableFrom(clazz)){
//				try{
//					EntityCreature temp = (EntityCreature)clazz.getConstructor(World.class).newInstance(world);
//					if (EntityUtils.isAIEnabled(temp) && temp.isNonBoss()){
//						int color = 0;
//						boolean found = false;
//						//look for entity egg
//						for (Object info : EntityList.ENTITY_EGGS.values()){
//							EntityEggInfo eei = (EntityEggInfo)info;
//							Class<? extends Entity> spawnClass = EntityList.getClassFromID(EntityList.getIDFromString(eei.spawnedID));
//							if (spawnClass == clazz){
//								color = eei.primaryColor;
//								found = true;
//								break;
//							}
//						}
//						if (!found){
//							//no spawn egg...pick random color?
//							color = world.rand.nextInt();
//						}
//						this.spawnableEntities.put(EntityList.CLASS_TO_NAME.get(clazz), color);
//					}
//				}catch (Throwable e){
//					//e.printStackTrace();
//				}
//			}
//		}
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.add(new ItemStack(this));
		for (String s : this.spawnableEntities.keySet()){
			if (s == null)
				continue;
			ItemStack stack = new ItemStack(this, 1, META_FULL);
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setString("SpawnClassName", s);
			stack.getTagCompound().setFloat("PercentFilled", 100);
			items.add(stack);
		}
	}
}
