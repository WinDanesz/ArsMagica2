package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityMagicWeakness extends AbstractAffinityAbility {

    public AbilityMagicWeakness() {
        super(new ResourceLocation("arsmagica2", "magicweakness"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.25f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.arcane;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker) {
            event.setAmount(event.getAmount() * 1.1F);
        }
    }
}
