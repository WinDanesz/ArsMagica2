package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.api.event.SpellCastEvent.Pre;
import am2.common.registry.AMPotions;
import am2.common.registry.Affinities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

public class AbilityClearCaster extends AbstractAffinityAbility {

    public AbilityClearCaster() {
        super(new ResourceLocation("arsmagica2", "clearcaster"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.4f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.arcane;
    }

    @Override
    public void applyPreSpellCast(EntityPlayer player, Pre event) {
        if (event.entityLiving.world.rand.nextInt(100) < 5 && !event.entityLiving.world.isRemote) {
            event.entityLiving.addPotionEffect(new PotionEffect(AMPotions.clarity, 140, 0));
        }
    }
}
