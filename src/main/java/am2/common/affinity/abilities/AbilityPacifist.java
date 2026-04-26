package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.registry.Affinities;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class AbilityPacifist extends AbstractAffinityAbility {

    public AbilityPacifist() {
        super(new ResourceLocation("arsmagica2", "pacifist"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.6f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.life;
    }

    @Override
    public void applyKill(EntityPlayer player, LivingDeathEvent event) {
        if (event.getEntityLiving().getCreatureAttribute() != EnumCreatureAttribute.UNDEAD) {
            assert MobEffects.NAUSEA != null;
            player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 40, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 1));
            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 1));
        }
    }

}
