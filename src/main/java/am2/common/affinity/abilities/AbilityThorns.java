package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class AbilityThorns extends AbstractAffinityAbility {

    public AbilityThorns() {
        super(new ResourceLocation("arsmagica2", "thorns"));
    }

    @Override
    public float getMinimumDepth() {
        return ArsMagica.config.getAffinityThornsMinDepth();
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.nature;
    }

    @Override
    public void applyHurt(EntityPlayer player, LivingHurtEvent event, boolean isAttacker) {
        if (!isAttacker && event.getSource().getTrueSource() instanceof EntityLivingBase) {
            double natureDepth = AffinityData.For(player).getAffinityDepth(Affinities.nature);
            if (natureDepth == 1.0f) {
                ((EntityLivingBase) event.getSource().getTrueSource()).attackEntityFrom(DamageSource.CACTUS, ArsMagica.config.getAffinityThornsDamage3());
            } else if (natureDepth >= 0.75f) {
                ((EntityLivingBase) event.getSource().getTrueSource()).attackEntityFrom(DamageSource.CACTUS, ArsMagica.config.getAffinityThornsDamage2());
            } else if (natureDepth >= getMinimumDepth()) {
                ((EntityLivingBase) event.getSource().getTrueSource()).attackEntityFrom(DamageSource.CACTUS, ArsMagica.config.getAffinityThornsDamage1());
            }
        }
    }

}
