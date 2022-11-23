package am2.common.utils;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NBTUtils {
	
	public static NBTTagCompound getAM2Tag (NBTTagCompound baseTag) {
		return addTag(baseTag, "AM2");
	}
	
	public static NBTTagCompound getEssenceTag (NBTTagCompound baseTag) {
		return addTag(getAM2Tag(baseTag), "Essence");
	}
	
	public static void writeVecToNBT(Vec3d vec, NBTTagCompound nbt) {
		nbt.setDouble("X", vec.x);
		nbt.setDouble("Y", vec.y);
		nbt.setDouble("Z", vec.z);
	}
	
	public static void writeBlockPosToNBT(BlockPos pos, NBTTagCompound nbt) {
		nbt.setInteger("X", pos.getX());
		nbt.setInteger("Y", pos.getY());
		nbt.setInteger("Z", pos.getZ());
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends List<?>> void ensureSize(ArrayList<T> list, int size) {
	    list.ensureCapacity(size);
	    while (list.size() < size) {
	        list.add((T) Lists.newArrayList());
	    }
	}
	
	public static Vec3d readVecFromNBT(NBTTagCompound nbt) {
		Vec3d vec = new Vec3d(nbt.getDouble("X"), nbt.getDouble("Y"), nbt.getDouble("Z"));
		return vec;
	}
	
	public static BlockPos readBlockPosFromNBT(NBTTagCompound nbt) {
		BlockPos pos = new BlockPos(nbt.getInteger("X"), nbt.getInteger("Y"), nbt.getInteger("Z"));
		return pos;
	}
	
	public static Object getValueAt (NBTTagCompound baseTag, String tagName) {
		NBTBase base = baseTag.getTag(tagName);
		switch (base.getId()) {
		case 0: return null;
		case 1: return ((NBTTagByte)base).getByte();
		case 2: return ((NBTTagShort)base).getShort();
		case 3: return ((NBTTagInt)base).getInt();
		case 4: return ((NBTTagLong)base).getLong();
		case 5: return ((NBTTagFloat)base).getFloat();
		case 6: return ((NBTTagDouble)base).getDouble();
		case 7: return ((NBTTagByteArray)base).getByteArray();
		case 8: return ((NBTTagString)base).getString();
		case 9: return ((NBTTagCompound)base);
		case 10: return ((NBTTagIntArray)base).getIntArray();
		default:
			return null;
		}
	}
	
	public static NBTTagCompound addTag (NBTTagCompound upper, String name) {
		if (upper == null) throw new IllegalStateException("Base Tag must exist");
		NBTTagCompound newTag = new NBTTagCompound();
		if (upper.getCompoundTag(name) != null) {
			newTag = upper.getCompoundTag(name);
		}
		upper.setTag(name, newTag);
		return newTag;		
	}
	
	public static NBTTagCompound addTag (NBTTagCompound upper, NBTTagCompound compound, String name) {
		if (upper == null) throw new IllegalStateException("Base Tag must exist");
		upper.setTag(name, compound);
		return upper;		
	}
	
	public static NBTTagList addList (NBTTagCompound upper, int type, String name) {
		if (upper == null) throw new IllegalStateException("Base Tag must exist");
		NBTTagList newTag = new NBTTagList();
		if (upper.getTagList(name, type) != null) {
			newTag = upper.getTagList(name, type);
		}
		upper.setTag(name, newTag);
		return newTag;
	}
	
	public static NBTTagList addCompoundList(NBTTagCompound upper, String name) {
		return addList(upper, 10, name);
	}
	
	public static boolean contains(NBTTagCompound container, NBTTagCompound check) {
		if (container == null) return true;
		if (check == null) return false;
		boolean match = true;
		for (String key : container.getKeySet()) {
			NBTBase tag = container.getTag(key);
			NBTBase checkTag = check.getTag(key);
			if (tag == null)
				continue;
			if (checkTag == null) return false;
			if (tag instanceof NBTTagCompound && checkTag instanceof NBTTagCompound)
				match &= contains((NBTTagCompound)tag, (NBTTagCompound) checkTag);
			else
				match &= tag.equals(checkTag);
			if (!match)
				break;
		}
		return match;
	}

	public static ItemStack[] getItemStackArray(NBTTagCompound tagCompound, String string) {
		NBTTagList list = addCompoundList(tagCompound, string);
		ItemStack[] array = new ItemStack[list.tagCount()];
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tmp = list.getCompoundTagAt(i);
			ItemStack is = new ItemStack((tmp));
			is.setCount( tmp.getInteger("ActualStackSize"));
			array[tmp.getInteger("ID")] = is;
		}
		return array;
	}

	public static void setItemStackArray(NBTTagCompound tagCompound, String string, ItemStack[] recipeData) {
		NBTTagList list = addCompoundList(tagCompound, string);
		for (int i = 0; i < recipeData.length; i++) {
			NBTTagCompound tmp = new NBTTagCompound();
			tmp.setInteger("ID", i);
			tmp.setInteger("ActualStackSize", recipeData[i].getCount());
			recipeData[i].writeToNBT(tmp);
			list.appendTag(tmp);
		}
		tagCompound.setTag(string, list);
	}

}
