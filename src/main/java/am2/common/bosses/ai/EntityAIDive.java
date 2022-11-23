package am2.common.bosses.ai;

import am2.common.bosses.BossActions;
import am2.common.bosses.EntityFireGuardian;
import am2.common.bosses.IArsMagicaBoss;
import net.minecraft.entity.ai.EntityAIBase;

public class EntityAIDive extends EntityAIBase{

	private final EntityFireGuardian host;
	private int cooldownTicks = 0;

	public EntityAIDive(EntityFireGuardian host){
		this.host = host;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){
		boolean execute = ((IArsMagicaBoss)host).getCurrentAction() == BossActions.IDLE && host.getAttackTarget() != null && cooldownTicks-- <= 0;
		if (execute)
			((IArsMagicaBoss)host).setCurrentAction(BossActions.SPINNING);
		return execute;
	}

	@Override
	public boolean shouldContinueExecuting(){
		if (host.getAttackTarget() == null || host.getAttackTarget().isDead || host.getNumHits() >= 3){
			this.cooldownTicks = 300;
			((IArsMagicaBoss)host).setCurrentAction(BossActions.IDLE);
			return false;
		}
		return true;
	}

	@Override
	public void updateTask(){
		host.getNavigator().tryMoveToEntityLiving(host.getAttackTarget(), 0.75f);
		if (((IArsMagicaBoss)host).getTicksInCurrentAction() > 40 && host.getDistanceSq(host.getAttackTarget()) < 64D){
			((IArsMagicaBoss)host).setCurrentAction(BossActions.SPINNING);
		}
	}

}
