package am2.common.entity.ai;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Server-tick coordination shared by a group attacking the same target. */
final class AirBlastVolley {
    static final int COOLDOWN_TICKS = 160;
    static final int PREPARE_TICKS = 80;
    static final int WINDUP_TICKS = 20;
    static final int DURATION_TICKS = 40;
    static final double FORMATION_RADIUS = 3.5D;

    interface Member {
        boolean isValid();
        boolean isReady();
        boolean isInPosition();
        boolean canHit();
        void setInflation(float inflation);
        void begin();
        void showTorrent(boolean visible);
        void applyBlast(int contributors, double pushX, double pushZ, boolean damage);
        void finish(boolean fired);
    }

    final double sideX, sideZ;
    private final List<Member> members;
    private final List<Member> formation;
    private final long createdAt;
    private long lastUpdate = Long.MIN_VALUE;
    private long startAt = Long.MAX_VALUE;
    private boolean fired;
    private boolean finished;

    AirBlastVolley(List<? extends Member> members, double sideX, double sideZ, long now) {
        this.members = new ArrayList<>(members);
        this.formation = new ArrayList<>(members);
        this.sideX = sideX;
        this.sideZ = sideZ;
        this.createdAt = now;
    }

    boolean isFinished() {
        return finished;
    }

    boolean isFiring() {
        return fired && !finished;
    }

    boolean isWindingUp() {
        return startAt != Long.MAX_VALUE && !fired && !finished;
    }

    void withdraw(Member member) {
        if (members.remove(member)) member.finish(fired);
    }

    boolean recruit(Member member, long now) {
        if (finished || fired || now - createdAt >= PREPARE_TICKS || members.contains(member)) return false;
        members.add(member);
        formation.add(member);
        startAt = Long.MAX_VALUE;
        for (Member ally : members) ally.setInflation(0.0F);
        return true;
    }

    int slotOf(Member member) {
        return formation.indexOf(member);
    }

    int formationSize() {
        // Keep gaps when an ally leaves, so the others aren't pulled out of
        // position just as the synchronized windup ends.
        return formation.size();
    }

    void update(long now) {
        // Each elemental ticks this shared volley; damage and force run only once.
        if (finished || lastUpdate == now) return;
        lastUpdate = now;
        for (Iterator<Member> it = members.iterator(); it.hasNext();) {
            Member member = it.next();
            if (!member.isValid()) {
                member.finish(fired);
                it.remove();
            }
        }
        if (members.isEmpty()) {
            finished = true;
            return;
        }

        if (startAt == Long.MAX_VALUE) {
            boolean allReady = members.stream().allMatch(m -> m.isReady() && m.isInPosition());
            if (!allReady && now - createdAt < PREPARE_TICKS) return;
            // A blocked or cooling ally cannot stall the rest of the group forever.
            dropUnreadyMembers(true);
            if (members.isEmpty()) {
                finished = true;
                return;
            }
            startAt = now + WINDUP_TICKS;
        }
        if (now < startAt) {
            float inflation = 1.0F - (startAt - now) / (float) WINDUP_TICKS;
            for (Member member : members) member.setInflation(inflation);
            return;
        }

        if (!fired) {
            // Once committed, follow through if the target is still in the
            // torrent's reach. A small step must not cancel the shared windup.
            dropUnreadyMembers(false);
            if (members.isEmpty()) {
                finished = true;
                return;
            }
            fired = true;
            for (Member member : members) member.begin();
        }
        long elapsed = now - startAt;
        if (elapsed >= DURATION_TICKS) {
            finished = true;
            for (Member member : members) member.finish(true);
            return;
        }

        Member attacker = null;
        int contributors = 0;
        for (Member member : members) {
            member.setInflation(1.0F - elapsed / (float) DURATION_TICKS);
            boolean hit = member.canHit();
            // Keep blowing for the whole channel, even after the target is
            // pushed beyond reach. The client clips the visible stream at walls.
            member.showTorrent(true);
            if (hit) {
                if (attacker == null) attacker = member;
                contributors++;
            }
        }
        if (attacker != null && elapsed % 2 == 0) {
            // All currents push the same way, rather than cancelling one another.
            // Three small damage pulses across the two-second channel.
            attacker.applyBlast(contributors, -sideX, -sideZ, elapsed % 16 == 0);
        }
    }

    private void dropUnreadyMembers(boolean requireFormation) {
        for (Iterator<Member> it = members.iterator(); it.hasNext();) {
            Member member = it.next();
            if (!member.isReady() || !(requireFormation ? member.isInPosition() : member.canHit())) {
                member.finish(false);
                it.remove();
            }
        }
    }

    static double formationAngle(int slot, int count) {
        return count <= 1 ? 0.0D : -0.4D + 0.8D * slot / (count - 1);
    }

    static boolean inFormation(double targetDistanceSq, double slotDistanceSq, boolean settled) {
        // Approach the 3.5-block arc, then allow room for strafing/jumping while
        // allies catch up. Separate arrival/leave distances prevent jitter.
        return settled ? targetDistanceSq >= 4.0D && targetDistanceSq <= 36.0D
                : targetDistanceSq >= 6.25D && targetDistanceSq <= 20.25D && slotDistanceSq <= 2.25D;
    }

    static double pushStrength(int contributors, double resistance, double forwardSpeed) {
        int count = Math.max(0, Math.min(4, contributors));
        if (count == 0) return 0.0D;
        double strength = (0.44D + 0.24D * (count - 1)) * (1.0D - Math.max(0.0D, Math.min(1.0D, resistance)));
        return Math.max(0.0D, Math.min(strength, 1.2D + 0.3D * count - forwardSpeed));
    }
}
