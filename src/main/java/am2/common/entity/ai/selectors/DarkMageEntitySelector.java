package am2.common.entity.ai.selectors;

import am2.common.entity.EntityDarkMage;
import com.google.common.base.Predicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;

public class DarkMageEntitySelector implements Predicate<EntityLivingBase>{

	public static final DarkMageEntitySelector instance = new DarkMageEntitySelector();

	private DarkMageEntitySelector(){
	}

	@Override
	public boolean apply(EntityLivingBase entity){
		return !(entity instanceof EntityDarkMage) && (entity == null || entity.getCreatureAttribute() != EnumCreatureAttribute.UNDEAD);
	}

}
