package am2.common.spell.component;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import am2.ArsMagica2;
import am2.api.affinity.Affinity;
import am2.api.blocks.IMultiblock;
import am2.api.rituals.IRitualInteraction;
import am2.api.rituals.RitualShapeHelper;
import am2.api.spell.Operation;
import am2.api.spell.SpellComponent;
import am2.api.spell.SpellData;
import am2.api.spell.SpellModifiers;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.common.buffs.BuffEffectCharmed;
import am2.common.defs.ItemDefs;
import am2.common.defs.PotionEffectsDefs;
import am2.common.extensions.EntityExtension;
import am2.common.items.ItemCrystalPhylactery;
import am2.common.utils.AffinityShiftUtils;
import am2.common.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Charm extends SpellComponent implements IRitualInteraction{

	@Override
	public Object[] getRecipe(){
		return new Object[]{
				new ItemStack(ItemDefs.rune, 1, EnumDyeColor.RED.getDyeDamage()),
				AffinityShiftUtils.getEssenceForAffinity(Affinity.LIFE),
				new ItemStack(ItemDefs.crystalPhylactery, 1, ItemCrystalPhylactery.META_EMPTY)
		};
	}

	@Override
	public boolean applyEffectEntity(SpellData spell, World world, EntityLivingBase caster, Entity target){
		if (!(target instanceof EntityCreature) || ((EntityCreature)target).isPotionActive(PotionEffectsDefs.CHARME) || EntityUtils.isSummon((EntityCreature)target)){
			return false;
		}

		int duration = (int) spell.getModifiedValue(PotionEffectsDefs.DEFAULT_BUFF_DURATION, SpellModifiers.DURATION, Operation.MULTIPLY, world, caster, target);
		//duration = SpellUtils.modifyDurationBasedOnArmor(caster, duration);

		if (RitualShapeHelper.instance.matchesRitual(this, world, target.getPosition())){
			duration += (3600 * (spell.getModifierCount(SpellModifiers.BUFF_POWER) + 1));
			RitualShapeHelper.instance.consumeReagents(this, world, target.getPosition());
		}

		if (target instanceof EntityAnimal){
			((EntityAnimal)target).setInLove(null);
			return true;
		}

		if (EntityExtension.For(caster).getCanHaveMoreSummons()){
			if (caster instanceof EntityPlayer){
				if (target instanceof EntityCreature){
					BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, BuffEffectCharmed.CHARM_TO_PLAYER);
					charmBuff.setCharmer(caster);
					((EntityCreature)target).addPotionEffect(charmBuff);
				}
				return true;
			}else if (caster instanceof EntityLiving){
				if (target instanceof EntityCreature){
					BuffEffectCharmed charmBuff = new BuffEffectCharmed(duration, BuffEffectCharmed.CHARM_TO_MONSTER);
					charmBuff.setCharmer(caster);
					((EntityCreature)target).addPotionEffect(charmBuff);
				}
				return true;
			}
		}else{
			if (caster instanceof EntityPlayer){
				((EntityPlayer)caster).sendMessage(new TextComponentString("You cannot have any more summons."));
			}
			return true;
		}
		return false;
	}
	
	@Override
	public EnumSet<SpellModifiers> getModifiers() {
		return EnumSet.of(SpellModifiers.DURATION);
	}


	@Override
	public float manaCost(){
		return 300;
	}

	@Override
	public ItemStack[] reagents(EntityLivingBase caster){
		return null;
	}

	@Override
	public void spawnParticles(World world, double x, double y, double z, EntityLivingBase caster, Entity target, Random rand, int colorModifier){
		for (int i = 0; i < 10; ++i){
			AMParticle particle = (AMParticle)ArsMagica2.proxy.particleManager.spawn(world, "heart", x, y, z);
			if (particle != null){
				particle.addRandomOffset(1, 2, 1);
				particle.AddParticleController(new ParticleFloatUpward(particle, 0, 0.05f + rand.nextFloat() * 0.1f, 1, false));
				particle.setMaxAge(20);
				if (colorModifier > -1){
					particle.setRGBColorF(((colorModifier >> 16) & 0xFF) / 255.0f, ((colorModifier >> 8) & 0xFF) / 255.0f, (colorModifier & 0xFF) / 255.0f);
				}
			}
		}
	}

	@Override
	public Set<Affinity> getAffinity(){
		return Sets.newHashSet(Affinity.LIFE);
	}

	@Override
	public float getAffinityShift(Affinity affinity){
		return 0.1f;
	}

	@Override
	public IMultiblock getRitualShape(){
		return RitualShapeHelper.instance.hourglass;
	}

	@Override
	public ItemStack[] getRitualReagents(){
		return new ItemStack[]{
				new ItemStack(Items.WHEAT),
				new ItemStack(Items.WHEAT_SEEDS),
				new ItemStack(Items.CARROT)
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
	public boolean applyEffectBlock(SpellData spell, World world, BlockPos blockPos, EnumFacing blockFace,
			double impactX, double impactY, double impactZ, EntityLivingBase caster) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getResult() {
		return null;
	}
}
