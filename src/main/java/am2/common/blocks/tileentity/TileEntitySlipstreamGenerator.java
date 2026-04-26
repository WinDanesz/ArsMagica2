package am2.common.blocks.tileentity;

import am2.ArsMagica;
import am2.api.math.AMVector3;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFloatUpward;
import am2.common.power.PowerNodeRegistry;
import am2.common.power.PowerTypes;
import am2.network.AMNetworkHandler;
import am2.network.packets.PacketRequestPowerPaths;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;

public class TileEntitySlipstreamGenerator extends TileEntityAMPower {

    private ArrayList<EntityPlayer> levitatingEntities;
    private int updateTicks = 1;

    private static final int EFFECT_HEIGHT = 25;

    public TileEntitySlipstreamGenerator() {
        super(ArsMagica.config.getCapacitySlipstreamGenerator());
        levitatingEntities = new ArrayList<EntityPlayer>();
    }

    @Override
    public boolean canProvidePower(PowerTypes type) {
        return false;
    }

    @Override
    public void update() {
        super.update();
        updateTicks++;
        if (updateTicks > 10) {
            refreshPlayerList();
            updateTicks = 0;
            if (world.isRemote && !levitatingEntities.isEmpty())
                AMNetworkHandler.getNetwork().sendToServer(new PacketRequestPowerPaths((byte) 1, new AMVector3(this)));
        }

        if (levitatingEntities.isEmpty())
            return;

        Iterator<EntityPlayer> it = levitatingEntities.iterator();
        while (it.hasNext()) {
            EntityPlayer player = it.next();
            if (!playerIsValid(player)) {
                it.remove();
                continue;
            }

            if (PowerNodeRegistry.For(this.world).getHighestPower(this) >= 0.25f) {

                if (world.isRemote) {
                    // Client-side movement handling
                    player.motionY *= 0.5999999;
                    if (Math.abs(player.motionY) < 0.2) {
                        player.motionY = 0;
                        player.fallDistance = 0f;
                    } else {
                        player.fallDistance--;
                    }
                    if (!player.isSneaking()) {
                        float pitch = player.rotationPitch;
                        float factor = (pitch > 0 ? (pitch - 10) : (pitch + 10)) / -360.0f;
                        if (Math.abs(pitch) > 10f) {
                            player.move(MoverType.SELF, 0, factor, 0);
                        }
                    }
                    spawnParticles(player);
                } else {
                    // Server-side: just consume power and reset fall distance
                    player.fallDistance = 0f;
                    PowerNodeRegistry.For(this.world).consumePower(this, PowerNodeRegistry.For(this.world).getHighestPowerType(this), 0.25f);
                }
            }
        }
    }

    private void spawnParticles(EntityPlayer player) {
        AMParticle wind = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "wind", player.posX, player.posY - player.height, player.posZ);
        float pitch = player.rotationPitch;
        float factor = (pitch > 0 ? (pitch - 10) : (pitch + 10)) / -180.0f;
        if (player.isSneaking())
            factor = 0.01f;
        if (wind != null) {
            wind.setMaxAge(10);
            wind.addRandomOffset(1, 1, 1);
            wind.setParticleScale(0.1f);
            wind.AddParticleController(new ParticleFloatUpward(wind, 0, Math.abs(factor) * 2, 1, false));
        }
    }

    private boolean playerIsValid(EntityPlayer player) {
        if (player == null || player.isDead)
            return false;
        float tolerance = 0.2f;
        AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - tolerance, pos.getY() + 1, pos.getZ() - tolerance, pos.getX() + 1 + tolerance, pos.getY() + 1 + EFFECT_HEIGHT, pos.getZ() + 1 + tolerance);
        Vec3d myLoc = new Vec3d(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        Vec3d playerLoc = new Vec3d(player.posX, player.posY, player.posZ);
        return bb.intersects(player.getEntityBoundingBox()) && world.rayTraceBlocks(myLoc, playerLoc, true) == null;
    }

    private void refreshPlayerList() {
        levitatingEntities.clear();

        for (int i = 0; i < world.playerEntities.size(); ++i) {
            EntityPlayer player = (EntityPlayer) world.playerEntities.get(i);
            if (playerIsValid(player) && !levitatingEntities.contains(player))
                levitatingEntities.add(player);
        }
    }

    @Override
    public boolean canRequestPower() {
        return true;
    }

    @Override
    public int getChargeRate() {
        return 12;
    }

    @Override
    public boolean canRelayPower(PowerTypes type) {
        return false;
    }
}
