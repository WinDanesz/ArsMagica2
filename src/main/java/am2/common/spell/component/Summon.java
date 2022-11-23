package am2.common.spell.component;

import am2.api.affinity.Affinity;
import am2.api.extensions.ISpellCaster;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.common.defs.BlockDefs;
import am2.common.defs.ItemDefs;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.items.ItemOre;
import am2.common.power.PowerTypes;
import am2.common.utils.EntityUtils;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public class Summon extends SpellComponent{
	

	public EntityLiving summonCreature(SpellData spell, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z){
		Class<? extends Entity> clazz = getSummonType(spell);
		EntityLiving entity = null;
		try{
			entity = (EntityLiving)clazz.getConstructor(World.class).newInstance(world);
		}catch (Throwable t){
			t.printStackTrace();
			return null;
		}

		if (entity == null){
			return null;
		}
		if (entity instanceof EntitySkeleton){
//			((EntitySkeleton)entity).(SkeletonType.NORMAL);
			((EntitySkeleton)entity).setHeldItem(EnumHand.MAIN_HAND, new ItemStack(Items.BOW));
		}else if (entity instanceof EntityHorse && caster instanceof EntityPlayer){
			((EntityHorse)entity).setTamedBy(((EntityPlayer)caster));
		}
		entity.setPosition(x, y, z);
		world.spawnEntity(entity);
		if (caster instanceof EntityPlayer){
			EntityUtils.makeSummon_PlayerFaction((EntityCreature)entity, (EntityPlayer)caster, false);
		}else{
			EntityUtils.makeSummon_MonsterFaction((EntityCreature)entity, false);
		}
		EntityUtils.setOwner(entity, caster);

		int duration = (int) spell.getModifiedValue(4800, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);

		EntityUtils.setSummonDuration(entity, duration);

		spell.applyComponentsToEntity(world, caster, entity);

		return entity;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION);
	}

	@Override
	public Object[] getRecipe(){
		//Chimerite, purified vinteum, blue orchid, monster focus, any filled crystal phylactery, 1500 dark power
		return new Object[]{
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_CHIMERITE),
				new ItemStack(ItemDefs.itemOre, 1, ItemOre.META_PURIFIED_VINTEUM),
				BlockDefs.cerublossom,
				ItemDefs.mobFocus,
				new ItemStack(ItemDefs.crystalPhylactery, 1, ItemCrystalPhylactery.META_FULL),
				"E:" + PowerTypes.DARK.ID(), 1500
		};
	}

	public void setSummonType(NBTTagCompound stack, ItemStack phylacteryStack){
		if (phylacteryStack.getItemDamage() == ItemCrystalPhylactery.META_FULL && phylacteryStack.getItem() instanceof ItemCrystalPhylactery){
			setSummonType(stack, ItemDefs.crystalPhylactery.getSpawnClass(phylacteryStack));
		}
	}

	public Class<? extends Entity> getSummonType(SpellData spell){
		String s = spell.getStoredData().getString("SummonType");
		if (s == null || s == "")
			s = "Skeleton"; //default!  default!  default!
		Class<? extends Entity> clazz = (Class<? extends Entity>)EntityList.getClassFromName(s);
		return clazz;
	}
	
	public Class<? extends Entity> getSummonType(ISpellCaster spell){
		String s = spell.getCommonStoredData().getString("SummonType");
		if (s == null || s == "")
			s = "Skeleton"; //default!  default!  default!
		Class<? extends Entity> clazz = (Class<? extends Entity>)EntityList.getClassFromName(s);
		return clazz;
	}


	public void setSummonType(NBTTagCompound stack, String s){
		Class<? extends Entity> clazz = (Class<? extends Entity>)EntityList.getClassFromName(s);
		setSummonType(stack, clazz);
	}

	public void setSummonType(NBTTagCompound stack, Class<? extends Entity> clazz){
		clazz = checkForSpecialSpawns(stack, clazz);

		String s = (String)EntityList.getKey(clazz).toString();
		if (s == null)
			s = "";

		stack.setString("SpawnClassName", s);
		stack.setString("SummonType", s);
	}

	private Class<? extends Entity> checkForSpecialSpawns(NBTTagCompound tag, Class<? extends Entity> clazz){
//		if (clazz == EntityChicken.class){
//			if (SpellUtils.modifierIsPresent(SpellModifiers.DAMAGE, stack) && SpellUtils.componentIsPresent(stack, Haste.class)){
//				return EntityBattleChicken.class;
//			}
//		}else if (clazz == EntityCow.class){
//			if (SpellUtils.modifierIsPresent(SpellModifiers.DAMAGE, stack) && SpellUtils.componentIsPresent(stack, AstralDistortion.class)){
//				return EntityHellCow.class;
//			}
//		}
		return clazz;
	}

	@Override
	public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace, double impactX, double impactY, double impactZ, EntityLivingBase caster){
		if (!world.isRemote){
			if (EntityExtension.For(caster).getCanHaveMoreSummons()){
				if (summonCreature(spell, caster, caster, world, impactX, impactY, impactZ) == null){
					return false;
				}
			}else{
				if (caster instanceof EntityPlayer){
					((EntityPlayer)caster).sendStatusMessage(new TextComponentString(I18n.format("am2.tooltip.noMoreSummons")), false);
				}
			}
		}

		return true;
	}

	@Override
	public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target){

		if (target instanceof EntityLivingBase && EntityUtils.isSummon((EntityLivingBase)target))
			return false;

		if (!world.isRemote){
			if (EntityExtension.For(caster).getCanHaveMoreSummons()){
				if (summonCreature(spell, caster, caster, world, target.posX, target.posY, target.posZ) == null){
					return false;
				}
			}else{
				if (caster instanceof EntityPlayer){
					((EntityPlayer)caster).sendStatusMessage(new TextComponentString(I18n.format("am2.tooltip.noMoreSummons")), false);
				}
			}
		}

		return true;
	}

	@Override
	public float manaCost(){
		return 400;
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
		return Sets.newHashSet(Affinity.ENDER, Affinity.LIFE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.01f;
	}

	@Override
	public void encodeBasicData(NBTTagCompound tag, Object[] recipe) {
		for (Object obj : recipe) {
			if (obj instanceof ItemStack) {
				ItemStack is = (ItemStack) obj;
				if (is.getItem().equals(ItemDefs.crystalPhylactery))
					setSummonType(tag, is);
			}
		}
	}
}
