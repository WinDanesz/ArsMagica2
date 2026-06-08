package am2.client.particles;

import net.minecraft.util.math.MathHelper;

public class ParticleApproachPoint extends ParticleController {

    private final double targetX, targetY, targetZ;
    private final double approachSpeed;
    private final double targetDistance;
    private boolean ignoreYCoord;

    public ParticleApproachPoint(AMParticle particleEffect, double targetX, double targetY, double targetZ, double approachSpeed, double targetDistance, int priority, boolean exclusive) {
        super(particleEffect, priority, exclusive);
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;
        this.approachSpeed = approachSpeed;
        this.targetDistance = targetDistance;
    }

    private double getDistanceSqToPoint(double x, double y, double z) {
//        double dx = particle.getPosX() - x;
//        double dz = particle.getPosZ() - z;
//        if (ignoreYCoord) {
//            return dx * dx + dz * dz;
//        }
//        double dy = particle.getPosY() - y;
//        return dx * dx + dz * dz + dy * dy;
        return 0.0D;
    }

    public ParticleApproachPoint setIgnoreYCoordinate(boolean ignore) {
        this.ignoreYCoord = ignore;
        return this;
    }

    @Override
    public void doUpdate() {
        double posX = particle.getPosX();
        double posZ = particle.getPosZ();
        double posY = particle.getPosY();

        double dx = targetX - posX;
        double dz = targetZ - posZ;
        double dy = targetY - posY;

        double d = dx * dx + dz * dz;
        if (!ignoreYCoord) {
            d += dy * dy;
        }

        if (d < targetDistance) {
            this.finish();
            return;
        }

        double angleRad = Math.atan2(dz, dx);
        posX += approachSpeed * Math.cos(angleRad);
        posZ += approachSpeed * Math.sin(angleRad);
        double dxz = MathHelper.sqrt(dx * dx + dz * dz);
        double pitchRad = Math.atan2(dy, dxz);
        posY = particle.getPosY() + (approachSpeed * Math.sin(pitchRad));
        particle.setPosition(posX, posY, posZ);
    }

    @Override
    public ParticleController clone() {
        return new ParticleApproachPoint(particle, targetX, targetY, targetZ, approachSpeed, targetDistance, priority, exclusive).setIgnoreYCoordinate(ignoreYCoord);
    }

}
