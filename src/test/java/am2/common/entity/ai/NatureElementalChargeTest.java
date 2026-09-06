package am2.common.entity.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NatureElementalChargeTest {
    @Test
    void givesExactlyTwoSecondsOfWarning() {
        NatureElementalCharge charge = new NatureElementalCharge(6.0D, 8.0D, 100L);
        for (long tick = 100L; tick < 140L; tick++) {
            assertTrue(charge.isPreparing(tick));
            assertFalse(charge.isCharging(tick));
        }
        assertFalse(charge.isPreparing(140L));
        assertTrue(charge.isCharging(140L));
    }

    @Test
    void rushHasBoundedRangeAndARecoveryBeforeMeleeResumes() {
        NatureElementalCharge charge = new NatureElementalCharge(0.0D, 12.0D, 0L);
        int movementTicks = 0;
        for (long tick = 0; tick < 80L; tick++) {
            if (charge.isCharging(tick)) movementTicks++;
            assertFalse(charge.isFinished(tick));
        }
        assertEquals(20, movementTicks);
        assertEquals(18.0D, movementTicks * NatureElementalCharge.SPEED, 0.00001D);
        assertFalse(charge.isCharging(60L));
        assertTrue(charge.isFinished(80L));
    }

    @Test
    void diagonalChargesHaveTheSameSpeed() {
        NatureElementalCharge charge = new NatureElementalCharge(-3.0D, 4.0D, 0L);
        charge.updateAim(4.0D, -3.0D, 40L);
        assertEquals(0.8D, charge.getDirectionX(), 0.00001D);
        assertEquals(-0.6D, charge.getDirectionZ(), 0.00001D);
        assertEquals(NatureElementalCharge.SPEED, Math.hypot(charge.getDirectionX() * NatureElementalCharge.SPEED,
                charge.getDirectionZ() * NatureElementalCharge.SPEED), 0.00001D);
    }

    @Test
    void tracksUntilWindupEndsThenLocksTheTargetsLatestPosition() {
        NatureElementalCharge charge = new NatureElementalCharge(8.0D, 0.0D, 100L);
        charge.updateAim(0.0D, 8.0D, 139L);
        assertEquals(0.0D, charge.getDirectionX(), 0.00001D);
        assertEquals(1.0D, charge.getDirectionZ(), 0.00001D);
        // The target moves again on the launch tick: use this position, not the previous tick's.
        charge.updateAim(-8.0D, 0.0D, 140L);
        assertTrue(charge.isCharging(140L));
        assertEquals(-1.0D, charge.getDirectionX(), 0.00001D);
        assertEquals(0.0D, charge.getDirectionZ(), 0.00001D);
        // A sidestep after launch cannot steer the committed charge.
        charge.updateAim(0.0D, -8.0D, 141L);
        assertEquals(-1.0D, charge.getDirectionX(), 0.00001D);
        assertEquals(0.0D, charge.getDirectionZ(), 0.00001D);
    }

    @Test
    void overlappingTargetAtLaunchRetainsAndLocksLastUsableHeading() {
        NatureElementalCharge charge = new NatureElementalCharge(8.0D, 0.0D, 0L);
        charge.updateAim(0.0D, 8.0D, 39L);
        charge.updateAim(0.0D, 0.0D, 40L);
        charge.updateAim(-8.0D, 0.0D, 41L);
        assertEquals(0.0D, charge.getDirectionX(), 0.00001D);
        assertEquals(1.0D, charge.getDirectionZ(), 0.00001D);
    }

    @Test
    void collisionOrHitStopsDamageImmediatelyAndStartsRecovery() {
        NatureElementalCharge charge = new NatureElementalCharge(1.0D, 0.0D, 0L);
        charge.stop(43L);
        assertFalse(charge.isPreparing(43L));
        assertFalse(charge.isCharging(43L));
        assertFalse(charge.isFinished(62L));
        assertTrue(charge.isFinished(63L));
        charge.stop(50L);
        assertTrue(charge.isFinished(63L), "Repeated collisions must not extend recovery");
    }

    @Test
    void anInterruptedWindupCannotTurnIntoAChargeLater() {
        NatureElementalCharge charge = new NatureElementalCharge(1.0D, 0.0D, 0L);
        charge.stop(10L);
        charge.updateAim(0.0D, 1.0D, 40L);
        assertEquals(1.0D, charge.getDirectionX(), 0.00001D);
        assertFalse(charge.isPreparing(10L));
        assertFalse(charge.isCharging(40L));
        assertTrue(charge.isFinished(30L));
    }

    @Test
    void rejectsAZeroLengthDirection() {
        assertThrows(IllegalArgumentException.class, () -> new NatureElementalCharge(0.0D, 0.0D, 0L));
    }
}
