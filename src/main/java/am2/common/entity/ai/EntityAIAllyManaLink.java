package am2.common.entity.ai;

import am2.api.extensions.ISpellCaster;
import am2.common.defs.SkillDefs;
import am2.common.extensions.EntityExtension;
import am2.common.extensions.SkillData;
import am2.common.spell.SpellCaster;
import am2.common.utils.EntityUtils;
import am2.common.utils.NPCSpells;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class EntityAIAllyManaLink extends EntityAIBase {

	private EntityCreature host;
	private static final ItemStack spellStack = NPCSpells.instance.manaLink.copy();
	
	public EntityAIAllyManaLink(EntityCreature host){
		this.host = host;
	}

	@Override
	public boolean shouldExecute(){
		boolean isSummon = EntityUtils.isSummon(host);
		if (!isSummon)
			return false;
		EntityPlayer owner = getHostOwner();
		if (owner == null || !SkillData.For(owner).hasSkill(SkillDefs.MAGE_POSSE_2.getID()) || host.getDistanceSq(host) > 64D || EntityExtension.For(owner).isManaLinkedTo(host))
			return false;
		return true;
	}

	private EntityPlayer getHostOwner(){
		int ownerID = EntityUtils.getOwner(host);
		Entity owner = host.world.getEntityByID(ownerID);
		if (owner == null || !(owner instanceof EntityPlayer))
			return null;
		return (EntityPlayer)owner;
	}

	@Override
	public void updateTask(){
		EntityPlayer owner = getHostOwner();
		if (owner == null)
			return;
		if (host.getDistance(owner) < 1)
			host.getNavigator().tryMoveToXYZ(host.posX, host.posY, host.posZ, 0.5f);
		else {
			ISpellCaster caster = spellStack.getCapability(SpellCaster.INSTANCE, null);
			if (caster != null) {
				caster.cast(spellStack, host.world, host);
			}
		}
	}

}
