package am2.bosses.ai;

import am2.bosses.BossActions;
import am2.bosses.EntityEnderGuardian;
import am2.bosses.IArsMagicaBoss;
import am2.utils.NPCSpells;
import am2.utils.SpellUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.SoundCategory;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

public class EntityAIEndertorrent extends AIAnimation{

	private int cooldownTicks = 0;

	public EntityAIEndertorrent(IAnimatedEntity entity){
		super(entity);
	}

	@Override
	public int getAnimID(){
		return BossActions.SMASH.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 155;
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityLiving living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null) return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 100;
		super.resetTask();
	}

	@Override
	public void updateTask(){
		EntityEnderGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 30, 30);
			if (guardian.getTicksInCurrentAction() > 15){
				if ((guardian.getTicksInCurrentAction() - 15) % 10 == 0)
					SpellUtils.applyStackStage(NPCSpells.instance.enderGuardian_enderTorrent, guardian, null, guardian.posX, guardian.posY, guardian.posZ, null, guardian.world, false, false, guardian.getTicksInCurrentAction());
				guardian.faceEntity(guardian.getAttackTarget(), 15, 180);
			}else if (guardian.getTicksInCurrentAction() == 15){
				guardian.world.playSound(guardian.posX, guardian.posY, guardian.posZ, ((IArsMagicaBoss)guardian).getAttackSound(), SoundCategory.HOSTILE, 1.0f, (float)(0.5 + guardian.getRNG().nextDouble() * 0.5f), false);
			}else{
				guardian.faceEntity(guardian.getAttackTarget(), 180, 180);
			}
		}
	}


}
