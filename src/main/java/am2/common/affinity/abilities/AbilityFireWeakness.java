package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;

public class AbilityFireWeakness extends AbstractAffinityAbility {

    // How often (in ticks) this deals its periodic fire-exposure damage.
    private static final int DAMAGE_INTERVAL_TICKS = 20;
    // Won't push the player below this fraction of their max health, mirroring fire's weakness to water.
    private static final float DAMAGE_HEALTH_FLOOR = 0.75f;

    public AbilityFireWeakness() {
        super(new ResourceLocation("arsmagica2", "fireweakness"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.5f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.water;
    }

    @Override
    public void applyTick(EntityPlayer player) {
        if (player.world.isRemote) return;
        if (!player.isBurning() && player.world.provider.getDimension() != -1) return;
        if (player.ticksExisted % DAMAGE_INTERVAL_TICKS != 0) return;
        if (player.getHealth() <= player.getMaxHealth() * DAMAGE_HEALTH_FLOOR) return;
        player.attackEntityFrom(DamageSource.ON_FIRE, 1.0f);
    }

}
