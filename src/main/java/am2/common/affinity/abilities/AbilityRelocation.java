package am2.common.affinity.abilities;

import am2.ArsMagica;
import am2.api.affinity.AbstractAffinityAbility;
import am2.api.affinity.Affinity;
import am2.common.defs.Keybindings;
import am2.common.extensions.AffinityData;
import am2.common.extensions.EntityExtension;
import am2.common.registry.Affinities;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityRelocation extends AbstractAffinityAbility {

    public AbilityRelocation() {
        super(new ResourceLocation("arsmagica2", "relocation"));
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
    @SideOnly(Side.CLIENT)
    public KeyBinding getKey() {
        return Keybindings.ENDER_TP;
    }

    @Override
    public boolean canApply(EntityPlayer player) {
        return super.canApply(player);
    }

    @Override
    public void applyKeyPress(EntityPlayer player) {
        if (AffinityData.For(player).getCooldown("EnderTP") > 0) {
            if (!player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("am2.chat.relocation_cooldown"));
            return;
        }

        Vec3d playerPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        RayTraceResult result = player.world.rayTraceBlocks(playerPos, playerPos.add(new Vec3d(player.getLookVec().x * 32, player.getLookVec().y * 32, player.getLookVec().z * 32)));
        if (result == null)
            result = new RayTraceResult(playerPos.add(new Vec3d(player.getLookVec().x * 32, player.getLookVec().y * 32, player.getLookVec().z * 32)), null);
        EnderTeleportEvent event = new EnderTeleportEvent(player, result.hitVec.x, result.hitVec.y, result.hitVec.z, 0.0f);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            if (!player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("am2.chat.relocation_failed"));
            return;
        }
        double posY = event.getTargetY();
        while (!player.world.isAirBlock(new BlockPos(event.getTargetX(), posY, event.getTargetZ())) || !player.world.isAirBlock(new BlockPos(event.getTargetX(), posY + 1, event.getTargetZ())))
            posY++;
        if (player.getDistanceSq(event.getTargetX(), posY, event.getTargetZ()) > 1024) {
            if (!player.world.isRemote)
                player.sendMessage(new TextComponentTranslation("am2.chat.relocation_out_of_range"));
            return;
        }

        Vec3d oldVec = new Vec3d(player.posX, player.posY, player.posZ);
        Vec3d newVec = new Vec3d(event.getTargetX(), posY, event.getTargetZ());
        player.setPositionAndUpdate(newVec.x, newVec.y, newVec.z);
        if (!player.world.isRemote) {
            spawnTeleportParticles(player, oldVec, newVec);
            player.world.playSound(null, oldVec.x, oldVec.y, oldVec.z, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }

        player.fallDistance = 0;
        EntityExtension.For(player).setFallProtection(20000);
        AffinityData.For(player).addCooldown("EnderTP", ArsMagica.config.getEnderAffinityAbilityCooldown());
    }

    // Mirrors vanilla Entity#attemptTeleport's visual: a trail of portal particles interpolated
    // between the old and new position, like an Enderman or chorus fruit teleport.
    //
    // World#spawnParticle(type, x, y, z, xSpeed, ySpeed, zSpeed) only notifies client-side render
    // listeners and is a no-op on the logical server, so it never reaches other players. The
    // WorldServer#spawnParticle(type, x, y, z, count, xOffset, yOffset, zOffset, speed) overload is
    // the one that actually builds and broadcasts an SPacketParticles to nearby clients.
    private void spawnTeleportParticles(EntityPlayer player, Vec3d from, Vec3d to) {
        if (!(player.world instanceof WorldServer)) return;
        WorldServer world = (WorldServer) player.world;
        int count = 32;
        for (int i = 0; i < count; ++i) {
            double t = (double) i / (count - 1);
            double px = from.x + (to.x - from.x) * t + (world.rand.nextDouble() - 0.5D) * player.width * 2.0D;
            double py = from.y + (to.y - from.y) * t + world.rand.nextDouble() * player.height;
            double pz = from.z + (to.z - from.z) * t + (world.rand.nextDouble() - 0.5D) * player.width * 2.0D;
            world.spawnParticle(EnumParticleTypes.PORTAL, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.2D);
        }
    }

}
