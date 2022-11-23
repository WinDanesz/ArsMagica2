package am2.client.particles;

import am2.ArsMagica2;
import am2.common.utils.MathUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public class ParticleArcToEntity extends ParticleController{

	private Vec3d start;
	private Entity target;
	private Vec3d firstControl;
	private Vec3d secondControl;
	private float percent;
	private float speed;
	private float offsetFactor;
	private float halfOffsetFactor;

	public ParticleArcToEntity(AMParticle particleEffect, int priority, double startX, double startY, double startZ, Entity target, boolean exclusive){
		super(particleEffect, priority, exclusive);
		start = new Vec3d(startX, startY, startZ);
		percent = 0.0f;
		speed = 0.03f;
		offsetFactor = 10;
		halfOffsetFactor = offsetFactor / 2;
		this.target = target;

		generateControlPoints();
	}

	public ParticleArcToEntity(AMParticle particleEffect, int priority, Entity target, boolean exclusive){
		this(particleEffect, priority, particleEffect.getPosX(), particleEffect.getPosY(), particleEffect.getPosZ(), target, exclusive);
	}

	public ParticleArcToEntity generateControlPoints(){
		firstControl = new Vec3d(
				start.x + ((target.posX - start.x) / 3),
				start.y + ((target.posY - start.y) / 3),
				start.z + ((target.posZ - start.z) / 3));

		secondControl = new Vec3d(
				start.x + ((target.posX - start.x) / 3 * 2),
				start.y + ((target.posY - start.y) / 3 * 2),
				start.z + ((target.posZ - start.z) / 3 * 2));

		double offsetX = (particle.getWorld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;
		double offsetZ = (particle.getWorld().rand.nextFloat() * offsetFactor) - halfOffsetFactor;

		Vec3d offset = new Vec3d(offsetX, 0, offsetZ);

		firstControl = firstControl.add(offset);
		secondControl = secondControl.add(offset);

		addParticleAtPoint(start);
		addParticleAtPoint(firstControl);
		addParticleAtPoint(secondControl);
		addParticleAtPoint(new Vec3d(target.posX, target.posY, target.posZ));

		return this;
	}

	private void addParticleAtPoint(Vec3d point){
		AMParticle p = (AMParticle)ArsMagica2.proxy.particleManager.spawn(particle.getWorld(), "smoke", point.x, point.y, point.z);
		if (p != null){
			p.setIgnoreMaxAge(false);
			p.setMaxAge(200);
			p.setParticleScale(1.5f);
			p.AddParticleController(new ParticleColorShift(p, 1, false));
		}
	}

	public ParticleArcToEntity specifyControlPoints(Vec3d first, Vec3d second){
		this.firstControl = first;
		this.secondControl = second;
		return this;
	}

	public ParticleArcToEntity SetSpeed(float speed){
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
		Vec3d bez = MathUtilities.bezier(start, firstControl, secondControl, new Vec3d(target.posX, target.posY, target.posZ).add(new Vec3d(0.0, target.getEyeHeight(), 0.0)), percent);
		particle.setPosition(bez.x, bez.y, bez.z);
	}

	@Override
	public ParticleController clone(){
		return new ParticleArcToEntity(particle, priority, target, exclusive).SetSpeed(speed).specifyControlPoints(firstControl, secondControl);
	}

}
