package am2.common.entity;

import am2.ArsMagica;
import am2.common.entity.ai.EntityAIManaDrainBolt;
import am2.common.extensions.EntityExtension;
import am2.common.registry.AMLoot;
import am2.common.registry.AMSounds;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityManaElemental extends EntityMob {

    private final float hostileSpeed;

    public EntityManaElemental(World par1World) {
        super(par1World);
        this.setAIMoveSpeed(0.2f);
        this.hostileSpeed = 0.25F;
        //this.attackStrength = 4;
        this.setSize(0.8f, 2.5f);
        EntityExtension.For(this).setMagicLevelWithMana(15);
        initAI();
    }

    public void setOnGroudFloat(float onGround) {
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(ArsMagica.config.getManaElementalMaxHealth());
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public int getTotalArmorValue() {
        return (int) ArsMagica.config.getManaElementalArmor();
    }

    @Override
    public void onUpdate() {
        if (this.world != null) {
            if (this.world.isRemote) {
                if (!ArsMagica.config.getDisableManaElementalParticles()) {
                    spawnConnectingParticles();
                }
            } else {
                if (EntityExtension.For(this).getCurrentMana() <= 0) {
                    this.attackEntityFrom(DamageSource.GENERIC, 500);
                }
            }
        }
        super.onUpdate();
    }

    /**
     * Spawns arcane/sparkle particles that visually connect the Mana Elemental's
     * disconnected floating body segments (head, chest, core, pelvis, arms, legs).
     *
     * Model coordinate mapping (16 model units = 1 block, y=24 = entity feet):
     *  - Chest↔Core gap:  world y ≈ 1.72–1.875 above feet
     *  - Core↔Pelvis gap: world y ≈ 1.375–1.53 above feet
     *  - Pelvis↔Legs:     world y ≈ 0.19–1.25  above feet  (biggest gap, ~1 block)
     */
    private void spawnConnectingParticles() {
        // Magenta-violet mana color (123, 3, 252)
        final float r = 0.482f, g = 0.012f, b = 0.988f;


        // Beam particles filling the gaps between the 3 floating arm segments.
        // Gap local y values (model units from arm rotation point): upper=4, lower=8
        // Arm rotation points: right model x=−8 → −0.5 blocks, left model x=+8 → +0.5 blocks
        // Arm swing and entity yaw are both applied in spawnArmBeam().
        if (ticksExisted % 5 == 0) {
            if (rand.nextInt(10) == 0)spawnArmBeam(r, g, b, true);    // right arm gaps
            if (rand.nextInt(10) == 0)spawnArmBeam(r, g, b, false);   // left arm gaps
            if (rand.nextInt(10) == 0) spawnShoulderBolt(true);        // core → right arm top
            if (rand.nextInt(10) == 0) spawnShoulderBolt(false);       // core → left arm top
            if (rand.nextInt(10) == 0) spawnLegBolt(true);             // pelvis → right leg
            if (rand.nextInt(10) == 0) spawnLegBolt(false);            // pelvis → left leg
        }
    }

    /**
     * Spawns lightning bolts in the two gaps between the arm's three floating
     * segments, accounting for entity renderYawOffset and arm swing rotation.
     */
    private void spawnArmBeam(float r, float g, float b, boolean rightArm) {
        double yawRad = Math.toRadians(this.renderYawOffset);

        double sideSign = rightArm ? 1.0 : -1.0;
        double armX = posX - sideSign * Math.cos(yawRad) * 0.5;
        double armZ = posZ - sideSign * Math.sin(yawRad) * 0.5;
        double armY = posY + 1.9375;

        double fwdX = -Math.sin(yawRad);
        double fwdZ =  Math.cos(yawRad);

        float swingAngle = rightArm
                ? MathHelper.cos(this.limbSwing * 0.6662F + (float) Math.PI) * 2.0F * this.limbSwingAmount * 0.5F
                : MathHelper.cos(this.limbSwing * 0.6662F)                   * 2.0F * this.limbSwingAmount * 0.5F;

        int color = 0x7C03FC;
        if (rand.nextInt(10) == 0) {
            spawnGapBolt(armX, armY, armZ, fwdX, fwdZ, swingAngle, 7, 9, color);
        }
        if (rand.nextInt(10) == 0) {
            spawnGapBolt(armX, armY, armZ, fwdX, fwdZ, swingAngle, 3, 5, color);
        }
    }

    private void spawnGapBolt(double armX, double armY, double armZ,
                              double fwdX, double fwdZ, float swingAngle,
                              double lyStart, double lyEnd, int color) {
        double[] start = armSegmentWorldPos(armX, armY, armZ, fwdX, fwdZ, lyStart, swingAngle);
        double[] end   = armSegmentWorldPos(armX, armY, armZ, fwdX, fwdZ, lyEnd,   swingAngle);
        ArsMagica.proxy.particleManager.BoltFromPointToPoint(
                world, start[0], start[1], start[2], end[0], end[1], end[2], 1, color, 6);
    }

    /** Converts a local arm y offset (model units from arm rotation point) to world xyz. */
    private double[] armSegmentWorldPos(double armX, double armY, double armZ,
                                        double fwdX, double fwdZ,
                                        double ly, float swingAngle) {
        double lyBlocks  = ly / 16.0;
        double worldY    = armY - lyBlocks * Math.cos(swingAngle);
        double fwdOffset = lyBlocks * Math.sin(swingAngle);
        return new double[]{ armX + fwdX * fwdOffset, worldY, armZ + fwdZ * fwdOffset };
    }

    /**
     * Bolt from Core center (posY+1.625) to the top of the arm's highest segment,
     * connecting the floating core/body to the floating shoulder piece.
     */
    private void spawnShoulderBolt(boolean rightArm) {
        double yawRad = Math.toRadians(this.renderYawOffset);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double fwdX = -sinYaw;
        double fwdZ =  cosYaw;

        // Core center: RP (0,-7,0), box y 3.5–6.5 → center model y=-2 → world posY+(24+2)/16
        double coreY = posY + 1.625;

        // Arm rotation point (same as spawnArmBeam)
        double sideSign = rightArm ? 1.0 : -1.0;
        double armX = posX - sideSign * cosYaw * 0.5;
        double armZ = posZ - sideSign * sinYaw * 0.5;
        double armY = posY + 1.9375;

        float swingAngle = rightArm
                ? MathHelper.cos(this.limbSwing * 0.6662F + (float) Math.PI) * 2.0F * this.limbSwingAmount * 0.5F
                : MathHelper.cos(this.limbSwing * 0.6662F)                   * 2.0F * this.limbSwingAmount * 0.5F;

        // Top edge of topmost arm segment: ly=1 from rotation point
        double[] armTop = armSegmentWorldPos(armX, armY, armZ, fwdX, fwdZ, 1.0, swingAngle);

        ArsMagica.proxy.particleManager.BoltFromPointToPoint(
                world, posX, coreY, posZ, armTop[0], armTop[1], armTop[2], 1, 0x7C03FC, 6);
    }

    /**
     * Bolt from Pelvis bottom center (posY+1.25) to the top of the leg,
     * connecting the floating pelvis to each floating leg.
     *
     * Leg rotation points: model (±4, 7, 0) → world y posY+1.0625.
     * Leg swing formula mirrors ModelBiped.setRotationAngles().
     */
    private void spawnLegBolt(boolean rightLeg) {
        double yawRad = Math.toRadians(this.renderYawOffset);
        double cosYaw = Math.cos(yawRad);
        double sinYaw = Math.sin(yawRad);
        double fwdX = -sinYaw;
        double fwdZ =  cosYaw;

        // Pelvis bottom: RP (0,-7,0), box y 9–11 → bottom model y=4 → world posY+(24-4)/16
        double pelvisY = posY + 1.25;

        // Leg RP: rightLeg model x=+4, leftLeg model x=−4 (±4/16 = ±0.25 blocks)
        // worldX = posX + legModelX*cos(yaw), worldZ = posZ − legModelX*sin(yaw)
        double legModelX = rightLeg ? 0.25 : -0.25;
        double legRotX = posX + legModelX * cosYaw;
        double legRotZ = posZ - legModelX * sinYaw;
        // Leg RP world y: model y=7 → posY + (24-7)/16
        double legRotY = posY + 1.0625;

        // Leg swing (ModelBiped.setRotationAngles equivalents)
        float legSwing = rightLeg
                ? MathHelper.cos(this.limbSwing * 0.6662F)                   * 1.4F * this.limbSwingAmount
                : MathHelper.cos(this.limbSwing * 0.6662F + (float) Math.PI) * 1.4F * this.limbSwingAmount;

        // Top edge of leg box: ly=14 from leg rotation point
        double[] legTop = armSegmentWorldPos(legRotX, legRotY, legRotZ, fwdX, fwdZ, 14.0, legSwing);

        ArsMagica.proxy.particleManager.BoltFromPointToPoint(
                world, posX, pelvisY, posZ, legTop[0], legTop[1], legTop[2], 1, 0x7C03FC, 6);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return AMSounds.MANA_ELEMENTAL_IDLE;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return AMSounds.MANA_ELEMENTAL_DEATH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return AMSounds.MANA_ELEMENTAL_DEATH;
    }

    private void initAI() {
        this.setPathPriority(PathNodeType.WATER, -1F);
        this.tasks.addTask(3, new EntityAIManaDrainBolt(this, this.hostileSpeed, 35, 1, 10));
        this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, this.getAIMoveSpeed()));
        this.tasks.addTask(7, new EntityAIWander(this, this.getAIMoveSpeed()));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget<EntityPlayer>(this, EntityPlayer.class, 0, true, false, null));
    }

    @Override
    public boolean getCanSpawnHere() {
        if (!SpawnBlacklists.entityCanSpawnHere(this.getPosition(), world, this))
            return false;
        return super.getCanSpawnHere();
    }

    @Override
    protected ResourceLocation getLootTable() {
        return AMLoot.MANA_ELEMENTAL_LOOT;
    }
}
