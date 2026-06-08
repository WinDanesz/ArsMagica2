package am2.client.particles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

public class ParticleApproachEntity extends ParticleController {

    private final Entity target;
    private final double height;
    private final double approachSpeed;
    private final double targetDistance;

    public ParticleApproachEntity(AMParticle particleEffect, Entity approachEntity, double approachSpeed, double targetDistance, int priority, boolean exclusive) {
        super(particleEffect, priority, exclusive);
        this.target = approachEntity;
        this.approachSpeed = approachSpeed;
        this.targetDistance = targetDistance;
        if (target instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving) target;
            height = entityliving.getEyeHeight();
        }
        else if (target instanceof EntityItem) {
            height = 0.5;
        }
        else {
            height = target.height / 2.0;
        }
    }

    @Override
    public void doUpdate() {
        if (target == null) {
            this.finish();
            return;
        }

        double posX = particle.getPosX();
        double posZ = particle.getPosZ();
        double posY = particle.getPosY();

        double dx = target.posX - posX;
        double dz = target.posZ - posZ;
        double dy = target.posY + height - posY;

        double d = dx * dx + dz * dz + dy * dy;

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
        return new ParticleApproachEntity(particle, target, approachSpeed, targetDistance, priority, exclusive);
    }

}
