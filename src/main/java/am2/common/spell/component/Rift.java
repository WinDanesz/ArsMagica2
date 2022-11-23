package am2.common.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.defs.BlockDefs;
import am2.common.defs.ItemDefs;
import am2.common.entity.EntityRiftStorage;
import am2.common.utils.AffinityShiftUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Rift extends SpellComponent implements IRitualInteraction{

	@Override
	public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){

		if (world.getBlockState(blockPos).getBlock().equals(Blocks.MOB_SPAWNER)){
			
			if (RitualShapeHelper.instance.matchesRitual(this, world, blockPos)){
				if (!world.isRemote){
					world.setBlockToAir(blockPos);
					RitualShapeHelper.instance.consumeReagents(this, world, blockPos);
					RitualShapeHelper.instance.consumeShape(this, world, blockPos);
					
					EntityItem item = new EntityItem(world);
					item.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
					item.setItem(new ItemStack(BlockDefs.inertSpawner));
					world.spawnEntity(item);
				}else{

				}

				return true;
			}
		}

		if (world.isRemote)
			return true;
		EntityRiftStorage storage = new EntityRiftStorage(world);
		int storageLevel = Math.min(1 + spell.getModifierCount(SpellModifiers.BUFF_POWER), 3);
		storage.setStorageLevel(storageLevel);
		switch (blockFace) {
		case UP:
			storage.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5);
			break;
		case NORTH:
			storage.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() - 1.5);
			break;
		case SOUTH:
			storage.setPosition(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 1.5);
			break;
		case WEST:
			storage.setPosition(blockPos.getX() - 1.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			break;
		case EAST:
			storage.setPosition(blockPos.getX() + 1.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
			break;
		default :
			storage.setPosition(blockPos.getX() + 0.5, blockPos.getY() - 1.5, blockPos.getZ() + 0.5);
			break;
		}
		world.spawnEntity(storage);
		return true;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.noneOf(SpellModifiers.class);
	}
	
	@Override
	public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target){
		return false;
	}

	@Override
	public float manaCost(){
		return 90;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.NONE);
	}

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.WHITE.getDyeDamage()),
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.PURPLE.getDyeDamage()),
				Blocks.CHEST,
				Items.ENDER_EYE
		};
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0;
	}

	@Override
	public IMultiblock getRitualShape(){
		return RitualShapeHelper.instance.corruption;
	}

	@Override
	public ItemStack[] getRitualReagents(){
		return new ItemStack[]{
				new ItemStack(ItemDefs.mobFocus),
				AffinityShiftUtils.getEssenceForAffinity(Affinity.ENDER)
		};
	}

	@Override
	public int getRitualReagentSearchRadius(){
		return 3;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return new ItemStack(BlockDefs.inertSpawner);
	}
}
