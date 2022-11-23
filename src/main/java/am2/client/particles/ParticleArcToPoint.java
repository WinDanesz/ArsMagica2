package am2.client.particles;

import am2.common.utils.MathUtilities;
import net.minecraft.util.math.Vec3d;

public class ParticleArcToPoint extends ParticleController{

	private final Vec3d start;
	private final Vec3d target;
	private Vec3d firstControl;
	private Vec3d secondControl;
	private float percent;
	private float speed;
	private final float offsetFactor;
	private final float halfOffsetFactor;

	public ParticleArcToPoint(AMParticle particleEffect, int priority, double startX, double startY, double startZ, double endX, double endY, double endZ, boolean exclusive){
		super(particleEffect, priority, exclusive);
		start = new Vec3d(startX, startY, startZ);
		target = new Vec3d(endX, endY, endZ);
		percent = 0.0f;
		speed = 0.03f;
		offsetFactor = 10;
		halfOffsetFactor = offsetFactor / 2;
		generateControlPoints();
	}

	public ParticleArcToPoint(AMParticle particleEffect, int priority, double endX, double endY, double endZ, boolean exclusive){
		this(particleEffect, priority, particleEffect.getPosX(), particleEffect.getPosY(), particleEffect.getPosZ(), endX, endY, endZ, exclusive);
	}

	public ParticleArcToPoint generateControlPoints(){
		firstControl = new Vec3d(
				start.x + ((target.x - start.x) / 3),
				start.y + ((target.y - start.y) / 3),
				start.z + ((target.z - start.z) / 3));

		secondControl = new Vec3d(
				start.x + ((target.x - start.x) / 3 * 2),
				start.y + ((target.y - start.y) / 3 * 2),
				start.z + ((target.z - start.z) / 3 * 2));

		double offsetX = (particle.getWorld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;
		double offsetZ = (particle.getWorld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;
		double offsetY = (particle.getWorld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;

		Vec3d offset = new Vec3d(offsetX, offsetY, offsetZ);

		firstControl = firstControl.add(offset);
		secondControl = secondControl.add(offset);

		return this;
	}

	public ParticleArcToPoint specifyControlPoints(Vec3d first, Vec3d second){
		this.firstControl = first;
		this.secondControl = second;
		return this;
	}

	public ParticleArcToPoint SetSpeed(float speed){
		this.speed = speed;
		return this;
	}

	@Override
	public void doUpdate(){
		percent += speed;
		if (percent >= 1.0f){
			this.finish();
			return;
		}
		Vec3d bez = MathUtilities.bezier(start, firstControl, secondControl, target, percent);
		particle.setPosition(bez.x, bez.y, bez.z);
	}

	@Override
	public ParticleController clone(){
		return new ParticleArcToPoint(particle, priority, target.x, target.y, target.z, exclusive).SetSpeed(speed).specifyControlPoints(firstControl, secondControl);
	}

}
