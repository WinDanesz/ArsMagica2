package am2.common.affinity.abilities;

import am2.api.affinity.AbstractToggledAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilityNightVision extends AbstractToggledAffinityAbility {

    public AbilityNightVision() {
        super(new ResourceLocation("arsmagica2", "nightvision"));
    }

    @Override
    protected boolean isEnabled(EntityPlayer player) {
        return AffinityData.For(player).getAbilityBoolean(AffinityData.NIGHT_VISION);
    }

    @Override
    public float getMinimumDepth() {
        return 0.75f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.ender;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (!player.world.isRemote && (!player.isPotionActive(MobEffects.NIGHT_VISION) || player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration() <= 220)) {
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 300, 0, true, false));
        }
    }

}
