package am2.common.bosses.ai;

import am2.api.extensions.ISpellCaster;
import am2.api.math.AMVector3;
import am2.common.bosses.BossActions;
import am2.common.bosses.EntityEnderGuardian;
import am2.common.bosses.IArsMagicaBoss;
import am2.common.spell.SpellCaster;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.SoundCategory;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

public class EntityAIEnderwave extends AIAnimation{

	private int cooldownTicks = 0;

	public EntityAIEnderwave(IAnimatedEntity entity){
		super(entity);
	}

	@Override
	public int getAnimID(){
		return BossActions.STRIKE.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 30;
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityLiving living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null || new AMVector3(living).distanceSqTo(new AMVector3(living.getAttackTarget())) > 100)
			return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 20;
	}

	@Override
	public void updateTask(){
		EntityEnderGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 30, 30);
			if (guardian.getTicksInCurrentAction() == 7){
				guardian.faceEntity(guardian.getAttackTarget(), 180, 180);
				guardian.world.playSound(guardian.posX, guardian.posY, guardian.posZ, ((IArsMagicaBoss)guardian).getAttackSound(), SoundCategory.HOSTILE, 1.0f, (float)(0.5 + guardian.getRNG().nextDouble() * 0.5f), false);
				ISpellCaster spell = NPCSpells.instance.enderGuardian_enderWave.getCapability(SpellCaster.INSTANCE, null);
				if (spell != null) {
					spell.cast(NPCSpells.instance.enderGuardian_enderWave, guardian.world, guardian);
				}
			}else{
				guardian.faceEntity(guardian.getAttackTarget(), 180, 180);
			}
		}
	}


}
