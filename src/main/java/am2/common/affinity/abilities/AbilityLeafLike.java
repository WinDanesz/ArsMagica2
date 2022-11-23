package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.EntityExtension;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class AbilityLeafLike extends AbstractAffinityAbility {

	public AbilityLeafLike() {
		super(new ResourceLocation("arsmagica2", "leaflike"));
	}

	@Override
	public float getMinimumDepth() {
		return 1f;
	}

	@Override
	public Affinity getAffinity() {
		return Affinity.NATURE;
	}
	
	@Override
	public void applyTick(EntityPlayer player) {
		if (player.collidedHorizontally){
			if (!player.isSneaking()){
				float movement = EntityExtension.For(player).getIsFlipped() ? -0.25f : 0.25f;
				player.move(MoverType.PLAYER, 0, movement, 0);
				player.motionY = 0;
			}else{
				player.motionY *= 0.79999999;
			}
			player.fallDistance = 0;
		}
	}

}
