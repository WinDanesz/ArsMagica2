package am2.common.entity.ai;

import am2.common.entity.EntityNatureElemental;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/** Sniffs dry ground between walks; combat and swimming immediately interrupt it. */
public class EntityAINatureElementalSniff extends EntityAIBase {
    private final EntityNatureElemental host;
    private long nextSniffTick;
    private int sniffTicks;
    private int duration;

    public EntityAINatureElementalSniff(EntityNatureElemental host) {
        this.host = host;
        setMutexBits(7);
        scheduleNextSniff();
    }

    private void scheduleNextSniff() {
        nextSniffTick = host.world.getTotalWorldTime() + 120 + host.getRNG().nextInt(121);
    }

    private boolean canSniff() {
        if (!host.canPerformIdleActions() || !host.onGround || host.isInWater() || host.isInLava()) return false;
        float yaw = host.rotationYaw * 0.017453292F;
        BlockPos ground = new BlockPos(host.posX - MathHelper.sin(yaw), host.posY - 0.1D,
                host.posZ + MathHelper.cos(yaw));
        return host.world.getBlockState(ground).isTopSolid();
    }

    @Override
    public boolean shouldExecute() {
        return host.world.getTotalWorldTime() >= nextSniffTick && host.getNavigator().noPath() && canSniff();
    }

    @Override
    public void startExecuting() {
        sniffTicks = 0;
        duration = 50 + host.getRNG().nextInt(31);
        host.getNavigator().clearPath();
        host.setSniffing(true);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return sniffTicks < duration && canSniff();
    }

    @Override
    public void updateTask() {
        sniffTicks++;
        host.getMoveHelper().setMoveTo(host.posX, host.posY, host.posZ, 0.0D);
        float yaw = host.rotationYaw * 0.017453292F;
        host.getLookHelper().setLookPosition(host.posX - MathHelper.sin(yaw), host.posY + 0.1D,
                host.posZ + MathHelper.cos(yaw), 10.0F, 10.0F);
        if (sniffTicks == 15 || sniffTicks == 35) {
            host.playSound(SoundEvents.ENTITY_PIG_AMBIENT, 0.25F, 0.7F + host.getRNG().nextFloat() * 0.15F);
        }
    }

    @Override
    public void resetTask() {
        sniffTicks = 0;
        host.setSniffing(false);
        scheduleNextSniff();
    }
}
