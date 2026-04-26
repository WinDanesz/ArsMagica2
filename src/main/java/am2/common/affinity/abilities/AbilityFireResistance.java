package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityFireResistance extends AbstractAffinityAbility {

    public AbilityFireResistance() {
        super(new ResourceLocation("arsmagica2", "fireresistance"));
    }

    @Override
    public float getMinimumDepth() {
        return 0f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.fire;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker && event.getSource().isFireDamage()) {
            double fireDepth = AffinityData.For(player).getAffinityDepth(Affinities.fire);
            double reduction = 1 - (0.6f * fireDepth);
            event.setAmount((float) (event.getAmount() * reduction));
        }
    }
}
