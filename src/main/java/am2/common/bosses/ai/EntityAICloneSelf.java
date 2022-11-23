package am2.common.bosses.ai;

import am2.common.bosses.BossActions;
import am2.common.bosses.EntityWaterGuardian;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAICloneSelf extends EntityAIBase{
	private final EntityWaterGuardian host;
	private int cooldownTicks = 0;

	public EntityAICloneSelf(EntityWaterGuardian host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		if (cooldownTicks-- > 0 || host.getCurrentAction() != BossActions.IDLE || !host.isActionValid(BossActions.CLONE) || host.isClone())
			return false;
		EntityLivingBase AITarget = host.getAttackTarget();
		return AITarget != null && !AITarget.isDead;
	}

	@Override
	public boolean shouldContinueExecuting(){
		if (host.isClone())
			return false;
		if (host.getCurrentAction() == BossActions.CLONE && host.getTicksInCurrentAction() > host.getCurrentAction().getMaxActionTime()){
			host.setCurrentAction(BossActions.IDLE);
			cooldownTicks = 200;
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		if (host.isClone())
			return;

		if (host.getCurrentAction() != BossActions.CLONE)
			host.setCurrentAction(BossActions.CLONE);

		if (!host.world.isRemote && host.getCurrentAction() == BossActions.CLONE && host.getTicksInCurrentAction() == 30){
			EntityWaterGuardian clone1 = spawnClone();
			EntityWaterGuardian clone2 = spawnClone();
			host.setClones(clone1, clone2);
			this.cooldownTicks = 200;
		}
	}

	private EntityWaterGuardian spawnClone(){
		EntityWaterGuardian clone = new EntityWaterGuardian(host.world);
		clone.setMaster(host);
		clone.setPosition(host.posX, host.posY, host.posZ);
		host.world.spawnEntity(clone);
		return clone;
	}
}
