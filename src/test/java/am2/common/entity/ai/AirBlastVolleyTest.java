package am2.common.entity.ai;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AirBlastVolleyTest {
    private static class Member implements AirBlastVolley.Member {
        boolean valid = true, positioned = true, hit = true, visible;
        long now, readyAt;
        int starts, finishes, pulses, damagePulses, contributors;
        double pushX, pushZ;
        float inflation;
        boolean fired;

        @Override public boolean isValid() { return valid; }
        @Override public boolean isReady() { return now >= readyAt; }
        @Override public boolean isInPosition() { return positioned; }
        @Override public boolean canHit() { return hit; }
        @Override public void setInflation(float inflation) { this.inflation = inflation; }
        @Override public void begin() { starts++; readyAt = now + AirBlastVolley.COOLDOWN_TICKS; }
        @Override public void showTorrent(boolean visible) { this.visible = visible; }
        @Override public void finish(boolean fired) { finishes++; this.fired = fired; visible = false; inflation = 0.0F; }
        @Override public void applyBlast(int contributors, double pushX, double pushZ, boolean damage) {
            pulses++;
            if (damage) damagePulses++;
            this.contributors = contributors;
            this.pushX = pushX;
            this.pushZ = pushZ;
        }
    }

    private AirBlastVolley volley(Member... members) {
        return new AirBlastVolley(Arrays.asList(members), 1.0D, 0.0D, 0L);
    }

    private void tick(AirBlastVolley volley, long now, Member... members) {
        for (Member member : members) member.now = now;
        volley.update(now);
    }

    @Test
    void waitsForApproachThenTelegraphsBeforeFiring() {
        Member solo = new Member();
        solo.positioned = false;
        AirBlastVolley volley = volley(solo);
        tick(volley, 0, solo);
        tick(volley, 20, solo);
        assertEquals(0, solo.starts);
        solo.positioned = true;
        tick(volley, 21, solo);
        tick(volley, 40, solo);
        assertEquals(0, solo.starts);
        tick(volley, 41, solo);
        assertEquals(1, solo.starts);
        assertEquals(1, solo.pulses);
    }

    @Test
    void teammatesWaitForCooldownAndStartOnTheSameTick() {
        Member first = new Member(), second = new Member();
        second.readyAt = 25;
        AirBlastVolley volley = volley(first, second);
        tick(volley, 0, first, second);
        tick(volley, 24, first, second);
        assertEquals(0, first.starts);
        tick(volley, 25, first, second);
        tick(volley, 45, first, second);
        assertEquals(1, first.starts);
        assertEquals(1, second.starts);
        assertEquals(205, first.readyAt);
        assertEquals(first.readyAt, second.readyAt);
        assertEquals(2, first.contributors);
    }

    @Test
    void eachWorldTickAppliesOneCombinedPushRegardlessOfMemberUpdateOrder() {
        Member first = new Member(), second = new Member(), third = new Member();
        AirBlastVolley volley = volley(first, second, third);
        tick(volley, 0, first, second, third);
        for (int i = 0; i < 3; i++) tick(volley, 20, first, second, third);
        assertEquals(1, first.pulses + second.pulses + third.pulses);
        assertEquals(3, first.contributors);
        assertEquals(-1.0D, first.pushX);
        assertEquals(0.0D, first.pushZ, 1.0e-8);
    }

    @Test
    void blockedAllyTimesOutWithoutPreventingAReadyAllyFromFiring() {
        Member ready = new Member(), blocked = new Member();
        blocked.positioned = false;
        AirBlastVolley volley = volley(ready, blocked);
        tick(volley, 0, ready, blocked);
        tick(volley, 79, ready, blocked);
        assertEquals(0, ready.starts);
        tick(volley, 80, ready, blocked);
        assertEquals(1, blocked.finishes);
        assertFalse(blocked.fired);
        tick(volley, 100, ready, blocked);
        assertEquals(1, ready.starts);
        assertEquals(0, blocked.starts);
        assertEquals(1, ready.contributors);
    }

    @Test
    void unreachableSoloAttackEndsInsteadOfKeepingTheCombatAiForever() {
        Member solo = new Member();
        solo.positioned = false;
        AirBlastVolley volley = volley(solo);
        tick(volley, 0, solo);
        tick(volley, 80, solo);
        assertTrue(volley.isFinished());
        assertEquals(0, solo.starts);
        assertEquals(1, solo.finishes);
    }

    @Test
    void targetMovementDuringWindupDoesNotCancelAReachableAttack() {
        Member first = new Member(), second = new Member();
        AirBlastVolley volley = volley(first, second);
        tick(volley, 0, first, second);
        first.positioned = false;
        second.positioned = false;
        tick(volley, 20, first, second);
        assertTrue(volley.isFiring());
        assertEquals(1, first.starts);
        assertEquals(1, second.starts);
        assertEquals(2, first.contributors);
    }

    @Test
    void losingReachOrLineOfSightDuringWindupStillPreventsFiring() {
        Member solo = new Member();
        AirBlastVolley volley = volley(solo);
        tick(volley, 0, solo);
        tick(volley, 10, solo);
        assertTrue(solo.inflation > 0.0F);
        solo.hit = false;
        tick(volley, 20, solo);
        assertTrue(volley.isFinished());
        assertEquals(0, solo.starts);
        assertEquals(0.0F, solo.inflation);
    }

    @Test
    void targetLossOrDeathStopsDamageAndVisualsImmediately() {
        Member solo = new Member();
        AirBlastVolley volley = volley(solo);
        tick(volley, 0, solo);
        tick(volley, 20, solo);
        assertTrue(solo.visible);
        solo.valid = false;
        tick(volley, 21, solo);
        assertTrue(volley.isFinished());
        assertFalse(solo.visible);
        assertEquals(0.0F, solo.inflation);
        assertEquals(1, solo.pulses);
    }

    @Test
    void losingOneAllyDoesNotCancelTheRemainingTorrent() {
        Member first = new Member(), second = new Member();
        AirBlastVolley volley = volley(first, second);
        tick(volley, 0, first, second);
        tick(volley, 20, first, second);
        first.valid = false;
        tick(volley, 22, first, second);
        assertFalse(volley.isFinished());
        assertEquals(1, second.pulses);
        assertEquals(1, second.contributors);
    }

    @Test
    void wallsAndOutOfRangeTargetsStopForceButAllowTheBreathToFinish() {
        Member first = new Member(), second = new Member();
        AirBlastVolley volley = volley(first, second);
        tick(volley, 0, first, second);
        tick(volley, 20, first, second);
        second.hit = false;
        tick(volley, 22, first, second);
        assertTrue(second.visible);
        assertEquals(1, first.contributors);
        first.hit = false;
        tick(volley, 24, first, second);
        assertEquals(2, first.pulses);
        assertTrue(first.visible);
    }

    @Test
    void channelDealsOnlyThreeSmallDamagePulsesAndEndsAfterFortyTicks() {
        Member solo = new Member();
        AirBlastVolley volley = volley(solo);
        for (int now = 0; now <= 60; now++) tick(volley, now, solo);
        assertEquals(20, solo.pulses);
        assertEquals(3, solo.damagePulses);
        assertEquals(1, solo.finishes);
        assertTrue(solo.fired);
        assertTrue(volley.isFinished());
        assertFalse(solo.visible);
        assertEquals(180, solo.readyAt);
        assertEquals(0.0F, solo.inflation);
    }

    @Test
    void interruptedMemberCannotBeUsedByItsOldVolleyAfterJoiningAnother() {
        Member first = new Member(), second = new Member();
        AirBlastVolley old = volley(first, second);
        tick(old, 0, first, second);
        old.withdraw(first);
        AirBlastVolley replacement = volley(first);
        tick(replacement, 1, first);
        tick(old, 20, first, second);
        assertEquals(0, first.starts);
        assertEquals(1, second.starts);
        tick(replacement, 21, first);
        assertEquals(1, first.starts);
    }

    @Test
    void allyAcquiringTheTargetLaterJoinsTheGatheringVolley() {
        Member first = new Member(), second = new Member();
        AirBlastVolley volley = volley(first);
        tick(volley, 0, first);
        second.positioned = false;
        assertTrue(volley.recruit(second, 1));
        tick(volley, 10, first, second);
        assertEquals(0, first.starts);
        second.positioned = true;
        tick(volley, 15, first, second);
        tick(volley, 35, first, second);
        assertEquals(1, first.starts);
        assertEquals(1, second.starts);
        assertEquals(2, first.contributors);
        assertFalse(volley.recruit(new Member(), 36));
    }

    @Test
    void departingAllyDoesNotMoveTheRemainingFormationSlots() {
        Member first = new Member(), second = new Member(), third = new Member();
        AirBlastVolley volley = volley(first, second, third);
        volley.withdraw(first);
        assertEquals(1, volley.slotOf(second));
        assertEquals(2, volley.slotOf(third));
        assertEquals(3, volley.formationSize());
    }

    @Test
    void formationKeepsEveryMemberThreeAndAHalfBlocksAwayOnTheSameSide() {
        for (int count = 1; count <= 8; count++) {
            for (int slot = 0; slot < count; slot++) {
                double angle = AirBlastVolley.formationAngle(slot, count);
                double x = Math.cos(angle) * AirBlastVolley.FORMATION_RADIUS;
                double z = Math.sin(angle) * AirBlastVolley.FORMATION_RADIUS;
                assertEquals(3.5D, Math.sqrt(x * x + z * z), 1.0e-8);
                assertTrue(x / 3.5D > 0.92D);
            }
        }
    }

    @Test
    void teamworkAddsForceWhileRespectingResistanceAndLimitingSpeed() {
        double solo = AirBlastVolley.pushStrength(1, 0, 0);
        assertTrue(AirBlastVolley.pushStrength(2, 0, 0) > solo);
        assertEquals(solo * 0.5D, AirBlastVolley.pushStrength(1, 0.5D, 0), 1.0e-8);
        assertEquals(0, AirBlastVolley.pushStrength(4, 1, 0));
        assertEquals(0, AirBlastVolley.pushStrength(4, 0, 3));
        assertTrue(AirBlastVolley.pushStrength(4, 0, 2.35D) <= 0.05D + 1.0e-8);
        assertEquals(AirBlastVolley.pushStrength(4, 0, 0), AirBlastVolley.pushStrength(40, 0, 0));
        assertEquals(0, AirBlastVolley.pushStrength(0, 0, 0));
    }

    @Test
    void soloGustHasSubstantialForceEvenWithPartialResistance() {
        // The original solo impulse was 0.24 before any resistance.
        assertTrue(AirBlastVolley.pushStrength(1, 0.25D, 0) > 0.24D);
        assertTrue(AirBlastVolley.pushStrength(1, 0, 1.25D) > 0.0D);
        assertEquals(0.0D, AirBlastVolley.pushStrength(1, 0, 1.5D));
    }

    @Test
    void settledFormationToleratesTargetStrafingAndJumpingWithoutOscillating() {
        boolean settled = AirBlastVolley.inFormation(3.5D * 3.5D, 0.0D, false);
        assertTrue(settled);
        // A two-block lateral step and a jump move the desired slot away,
        // but this elemental remains on the correct side in firing range.
        settled = AirBlastVolley.inFormation(16.25D, 5.0D, settled);
        assertTrue(settled);
        assertTrue(AirBlastVolley.inFormation(25.0D, 6.0D, settled));
        assertFalse(AirBlastVolley.inFormation(25.0D, 6.0D, false));
        assertFalse(AirBlastVolley.inFormation(37.0D, 8.0D, settled));
        assertFalse(AirBlastVolley.inFormation(3.0D, 8.0D, settled));
    }

    @Test
    void alliesInflateTogetherDuringWindupAndDeflateThroughoutTheChannel() {
        Member first = new Member(), second = new Member();
        AirBlastVolley volley = volley(first, second);
        tick(volley, 0, first, second);
        assertTrue(volley.isWindingUp());
        assertEquals(0.0F, first.inflation);
        tick(volley, 10, first, second);
        assertEquals(0.5F, first.inflation);
        assertEquals(first.inflation, second.inflation);
        assertEquals(0, first.starts);
        tick(volley, 20, first, second);
        assertEquals(1.0F, first.inflation);
        assertEquals(first.inflation, second.inflation);
        assertFalse(volley.isWindingUp());
        tick(volley, 40, first, second);
        assertEquals(0.5F, first.inflation);
        tick(volley, 60, first, second);
        assertEquals(0.0F, first.inflation);
        assertEquals(first.inflation, second.inflation);
    }
}
