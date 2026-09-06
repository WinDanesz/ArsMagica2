package am2.common.entity.ai;

/** Tracks during windup, then commits to the target's position on the first charging tick. */
public final class NatureElementalCharge {
    public static final int WINDUP_TICKS = 40;
    public static final int CHARGE_TICKS = 20;
    public static final int RECOVERY_TICKS = 20;
    public static final int COOLDOWN_TICKS = 120;
    public static final double SPEED = 0.9D;

    private double directionX;
    private double directionZ;
    private boolean directionLocked;
    private final long startedAt;
    private long recoveryAt;

    public NatureElementalCharge(double deltaX, double deltaZ, long now) {
        double length = Math.hypot(deltaX, deltaZ);
        if (length < 0.001D) throw new IllegalArgumentException("Charge needs a horizontal direction");
        directionX = deltaX / length;
        directionZ = deltaZ / length;
        startedAt = now;
        recoveryAt = now + WINDUP_TICKS + CHARGE_TICKS;
    }

    public boolean isPreparing(long now) {
        return now < startedAt + WINDUP_TICKS && now < recoveryAt;
    }

    public double getDirectionX() { return directionX; }
    public double getDirectionZ() { return directionZ; }

    /** Call before movement, including on the tick that ends the windup. */
    public void updateAim(double deltaX, double deltaZ, long now) {
        if (directionLocked || now >= recoveryAt) return;
        double length = Math.hypot(deltaX, deltaZ);
        // If the target overlaps us, retain the last usable heading rather than divide by zero.
        if (length >= 0.001D) {
            directionX = deltaX / length;
            directionZ = deltaZ / length;
        }
        if (now >= startedAt + WINDUP_TICKS) directionLocked = true;
    }

    public boolean isCharging(long now) {
        return now >= startedAt + WINDUP_TICKS && now < recoveryAt;
    }

    public boolean isFinished(long now) {
        return now >= recoveryAt + RECOVERY_TICKS;
    }

    public void stop(long now) {
        recoveryAt = Math.min(recoveryAt, now);
    }
}
