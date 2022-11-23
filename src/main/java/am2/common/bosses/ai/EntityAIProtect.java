package am2.common.bosses.ai;

import am2.api.extensions.ISpellCaster;
import am2.common.bosses.BossActions;
import am2.common.bosses.EntityEnderGuardian;
import am2.common.spell.SpellCaster;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.EntityLivingBase;
import thehippomaster.AnimationAPI.AIAnimation;
import thehippomaster.AnimationAPI.IAnimatedEntity;

public class EntityAIProtect extends AIAnimation{

	private int cooldownTicks = 0;

	public EntityAIProtect(IAnimatedEntity entity){
		super(entity);
	}

	@Override
	public int getAnimID(){
		return BossActions.SHIELD_BASH.ordinal();
	}

	@Override
	public boolean isAutomatic(){
		return false;
	}

	@Override
	public int getDuration(){
		return 35;
	}

	@Override
	public boolean shouldAnimate(){
		//accessor method in AIAnimation that gives access to the entity
		EntityEnderGuardian living = getEntity();

		//must have an attack target
		if (living.getAttackTarget() == null || living.getTicksSinceLastAttack() > 40)
			return false;

		return cooldownTicks-- <= 0;
	}

	@Override
	public void resetTask(){
		cooldownTicks = 20;
		EntityLivingBase ent = getEntity();
		ent.extinguish();
		ISpellCaster spell = NPCSpells.instance.dispel.getCapability(SpellCaster.INSTANCE, null);
		if (spell != null) {
			spell.cast(NPCSpells.instance.dispel, ent.world, ent);
		}
		super.resetTask();
	}

	@Override
	public void updateTask(){
		EntityEnderGuardian guardian = getEntity();
		if (guardian.getAttackTarget() != null){
			guardian.getLookHelper().setLookPositionWithEntity(guardian.getAttackTarget(), 30, 30);
		}
	}

}
