package am2.common.affinity.abilities;

import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.extensions.AffinityData;
import am2.common.registry.Affinities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.WorldServer;

public class AbilityHealingTouch extends AbstractAffinityAbility {

    private static final float HEAL_AMOUNT = 4.0F;
    public static final int COOLDOWN_TICKS = 200; // 10 seconds

    public AbilityHealingTouch() {
        super(new ResourceLocation("arsmagica2", "healingtouch"));
    }

    @Override
    public float getMinimumDepth() {
        return 0.4f;
    }

    @Override
    public Affinity getAffinity() {
        return Affinities.life;
    }

    @Override
    public void applyInteractEntity(EntityPlayer player, PlayerInteractEvent.EntityInteract event) {
        if (player.world.isRemote) return;
        if (!player.getHeldItem(event.getHand()).isEmpty()) return;

        Entity target = event.getTarget();
        if (!(target instanceof EntityLivingBase) || target == player) return;

        if (AffinityData.For(player).getCooldown("HealingTouch") > 0) return;

        EntityLivingBase livingTarget = (EntityLivingBase) target;
        livingTarget.heal(HEAL_AMOUNT);
        spawnHealParticles((WorldServer) player.world, livingTarget);
        AffinityData.For(player).addCooldown("HealingTouch", COOLDOWN_TICKS);
    }

    // World#spawnParticle(type, x, y, z, xSpeed, ySpeed, zSpeed) only notifies client-side render
    // listeners and is a no-op on the logical server. WorldServer's numberOfParticles overload is
    // the one that actually builds and broadcasts an SPacketParticles to nearby clients.
    private void spawnHealParticles(WorldServer world, EntityLivingBase target) {
        world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY,
                target.posX, target.posY + target.height * 0.5D, target.posZ,
                12, target.width * 0.5D, target.height * 0.5D, target.width * 0.5D, 0.0D);
    }

}
