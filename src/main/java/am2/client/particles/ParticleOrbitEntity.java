package am2.client.particles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public final class ParticleOrbitEntity extends ParticleController{

	private final Entity target;
//	private double distance;
	private final boolean rotateClockwise;
	private double targetY;
	private double curYOffset;
	private double targetDistance;
	private final double orbitSpeed;
	private double orbitAngle;
	private double orbitY = -512;
	private boolean ignoreYCoordinate = false;

	public ParticleOrbitEntity(AMParticle particleEffect, Entity orbitTarget, double orbitSpeed, int priority, boolean exclusive){
		super(particleEffect, priority, exclusive);
		this.target = orbitTarget;
		this.orbitAngle = this.particle.getWorld().rand.nextInt(360);
		this.rotateClockwise = this.particle.getWorld().rand.nextBoolean();
		this.generateNewTargetY();
		this.targetDistance = 1 + (this.particle.getWorld().rand.nextDouble() * 0.5);
		this.orbitSpeed = orbitSpeed;
	}

	public ParticleOrbitEntity setOrbitY(double orbitY){
		this.orbitY = orbitY;
		return this;
	}

	public ParticleOrbitEntity SetTargetDistance(double targetDistance){
		this.targetDistance = targetDistance;
		return this;
	}

	private void generateNewTargetY(){
		if (this.target != null){
			this.targetY = this.particle.getWorld().rand.nextDouble() * this.target.height;
		}else{
			this.targetY = 0;
		}
	}

//	private void generateNewDistance(){
//		if (target != null){
//			targetDistance = particle.getWorld().rand.nextDouble() * 2;
//		}else{
//			targetDistance = 0;
//		}
//	}

	@Override
	public void doUpdate(){

		if (this.target == null || this.target.isDead){
			this.finish();
			return;
		}

		if (this.firstTick){
			this.curYOffset = this.particle.getPosY() - (this.target.posY + this.target.getEyeHeight());
		}

		double posX;
		double posZ;
		double posY = this.particle.getPosY();

		if (Math.abs(this.targetY - this.curYOffset) < 0.1){
			this.generateNewTargetY();
		}

		posX = this.target.posX + (Math.cos(this.orbitAngle) * this.targetDistance);
		posZ = this.target.posZ + (Math.sin(this.orbitAngle) * this.targetDistance);

		if (this.targetY < this.curYOffset){
			this.curYOffset -= this.orbitSpeed / 4;
		}else if (this.targetY > this.curYOffset){
			this.curYOffset += this.orbitSpeed / 4;
		}

		if (this.rotateClockwise){
			this.orbitAngle += this.orbitSpeed;
		}else{
			this.orbitAngle -= this.orbitSpeed;
		}
		if (this.orbitAngle > 360){
			this.orbitAngle -= 360;
		}else if (this.orbitAngle < 0){
			this.orbitAngle += 360;
		}

		if (!this.ignoreYCoordinate){
			if (this.orbitY != -512){
				posY = (this.target.posY + this.target.getEyeHeight()) + this.orbitY;
			}else{
				int offset = 0;
				if (this.target instanceof EntityPlayer && !(this.target instanceof EntityPlayerMP))
					offset += 2 * this.target.height;
				posY = this.target.posY - this.target.getEyeHeight() + this.curYOffset + offset;
			}
		}

		this.particle.setPosition(posX, posY, posZ);
		if (this.firstTick){
			this.particle.setPrevPos(posX, posY, posZ);
		}
	}

	@Override
	public ParticleController clone(){
		ParticleOrbitEntity clone = new ParticleOrbitEntity(this.particle, this.target, this.orbitSpeed, this.priority, this.rotateClockwise).SetTargetDistance(this.targetDistance);
		if (this.orbitY != -512){
			clone.setOrbitY(this.orbitY);
		}
		clone.setIgnoreYCoordinate(this.ignoreYCoordinate);
		return clone;
	}

	public ParticleOrbitEntity setIgnoreYCoordinate(boolean b){
		this.ignoreYCoordinate = b;
		return this;
	}

}
