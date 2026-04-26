package am2.common.bosses.ai;

import am2.common.bosses.EntityWaterGuardian;
import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;

public class WaterGuardianTargetSelector implements Predicate<Entity> {

    @Override
    public boolean apply(Entity entity) {
        return !(entity instanceof EntityWaterGuardian);
    }

}
