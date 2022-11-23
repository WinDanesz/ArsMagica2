package am2.common.entity.ai.selectors;

import am2.common.utils.EntityUtils;
import com.google.common.base.Predicate;
import net.minecraft.entity.monster.EntityMob;

public class SummonEntitySelector implements Predicate<EntityMob>{

	public static final SummonEntitySelector instance = new SummonEntitySelector();

	private SummonEntitySelector(){
	}
	@Override
	public boolean apply(EntityMob entity) {
		if (entity != null){
			return !EntityUtils.isSummon(entity);
		}
		return false;
	}

}
