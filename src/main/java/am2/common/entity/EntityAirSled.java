package am2.common.entity;

import am2.ArsMagica;
import am2.client.particles.AMParticle;
import am2.client.particles.ParticleFadeOut;
import am2.common.enchantments.AMEnchantmentHelper;
import am2.common.registry.AMItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityAirSled extends EntityLiving {

    private float rotation;

    public EntityAirSled(World par1World) {
        super(par1World);
        this.setSize(0.5f, 1);
        this.stepHeight = 1.02f;
    }

    @Override
    public void onUpdate() {
        this.stepHeight = 1.02f;

        if (world.isRemote) {
            rotation += 1f;
            if (this.world.isAirBlock(getPosition().down())) {
                for (int i = 0; i < ArsMagica.config.getGFXLevel(); ++i) {
                    AMParticle cloud = (AMParticle) ArsMagica.proxy.particleManager.spawn(world, "sparkle2", posX, posY + 0.5, posZ);
                    if (cloud != null) {
                        cloud.addRandomOffset(1, 1, 1);
                        cloud.AddParticleController(new ParticleFadeOut(cloud, 1, false).setFadeSpeed(0.01f));
                    }
                }
            }
        }
        super.onUpdate();
    }

    public float getRotation() {
        return rotation;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (this.getControllingPassenger() != null && this.getControllingPassenger() instanceof EntityPlayer && this.getControllingPassenger() != player) {
            return EnumActionResult.SUCCESS;
        } else {
            if (!this.world.isRemote) {
                if (player.isSneaking()) {
                    ItemStack sledStack = AMEnchantmentHelper.soulbindStack(new ItemStack(AMItems.air_sled));
                    // Try active hand first, then general inventory, then drop
                    if (player.getHeldItem(hand).isEmpty()) {
                        player.setHeldItem(hand, sledStack);
                    } else if (!player.inventory.addItemStackToInventory(sledStack)) {
                        this.entityDropItem(sledStack, 0.0f);
                    }
                    this.setDead();
                } else {
                    player.startRiding(this);
                }
            }

            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public void updateRidden() {
        if (this.isBeingRidden()) {
            this.getControllingPassenger().setPosition(this.posX, this.posY + this.getMountedYOffset() + this.getControllingPassenger().getYOffset(), this.posZ);
        }
    }

    @Override
    public Entity getControllingPassenger() {
        return getPassengers().isEmpty() ? null : getPassengers().get(0);
    }

    @Override
    public void setMoveForward(float amount) {
        super.setMoveForward(amount);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (this.getControllingPassenger() != null) {
            this.prevRotationYaw = this.rotationYaw = this.getControllingPassenger().rotationYaw;
            this.rotationPitch = this.getControllingPassenger().rotationPitch * 0.5F;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.rotationYawHead = this.renderYawOffset = this.rotationYaw;
            strafe = ((EntityLivingBase) this.getControllingPassenger()).moveStrafing * 0.5F;
            forward = ((EntityLivingBase) this.getControllingPassenger()).moveForward;

            if (forward <= 0.0F) {
                forward *= 0.25F;
            }

            this.stepHeight = 1.0F;
            this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;

            if (!this.world.isRemote) {
                forward *= 0.06f;
                if (strafe != 0) {
                    float f4 = MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F);
                    float f5 = MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F);
                    this.motionX += (strafe * f5 - forward * f4) * 0.06f;
                    this.motionZ += (forward * f5 + strafe * f4) * 0.06f;
                }

                this.motionX += -Math.sin(Math.toRadians(this.rotationYaw)) * forward;
                this.motionY += -Math.sin(Math.toRadians(this.rotationPitch)) * forward;
                this.motionZ += Math.cos(Math.toRadians(this.rotationYaw)) * forward;
            }
        } else {
            if (!this.onGround && !this.isInWater())
                this.motionY = -0.1f;
            else
                this.motionY = 0f;

            this.motionX *= 0.7f;
            this.motionZ *= 0.7f;

        }

        if (this.getControllingPassenger() != null)
            this.setSize(0.5f, 3);
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        if (this.getControllingPassenger() != null)
            this.setSize(0.5f, 1);

        float f2 = 0.91F;

        this.motionY *= f2;
        this.motionX *= f2;
        this.motionZ *= f2;
    }

    @Override
    public double getMountedYOffset() {
        return 1.6f;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void playSound(SoundEvent soundIn, float volume, float pitch) {
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity) {
        return null;
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
        if (this.getControllingPassenger() != null && !this.getControllingPassenger().isEntityEqual(par1DamageSource.getTrueSource()))
            this.getControllingPassenger().dismountRidingEntity();
        return false;
    }
}
