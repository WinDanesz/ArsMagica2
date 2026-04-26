package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityPoisonResistance extends AbstractAffinityAbility {

    public AbilityPoisonResistance() {
        super(new ResourceLocation("arsmagica2", "poisonresistance"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.25f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.ender;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker) {
            if (event.getSource() == DamageSource.MAGIC || event.getSource() == DamageSource.WITHER) {
                double enderDepth = AffinityData.For(player).getAffinityDepth(Affinities.ender);
                double reduction = 1 - (0.75f * enderDepth);
                event.setAmount((float) (event.getAmount() * reduction));
            }
        }
    }
}
