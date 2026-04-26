package am2.common.spell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContingencyTypeTest {

    @Test
    public void testFromNameExact() {
        assertEquals(ContingencyType.DEATH, ContingencyType.fromName("death"));
        assertEquals(ContingencyType.DAMAGE, ContingencyType.fromName("damage"));
        assertEquals(ContingencyType.FALL, ContingencyType.fromName("fall"));
        assertEquals(ContingencyType.HEALTH, ContingencyType.fromName("health"));
        assertEquals(ContingencyType.FIRE, ContingencyType.fromName("fire"));
        assertEquals(ContingencyType.NULL, ContingencyType.fromName("null"));
    }

    @Test
    public void testFromNameUppercase() {
        assertEquals(ContingencyType.DEATH, ContingencyType.fromName("DEATH"));
        assertEquals(ContingencyType.DAMAGE, ContingencyType.fromName("DAMAGE"));
    }

    @Test
    public void testFromNameMixedCase() {
        assertEquals(ContingencyType.FIRE, ContingencyType.fromName("Fire"));
        assertEquals(ContingencyType.FALL, ContingencyType.fromName("FALL"));
    }

    @Test
    public void testFromNameUnknownReturnsNull() {
        assertEquals(ContingencyType.NULL, ContingencyType.fromName("unknown"));
        assertEquals(ContingencyType.NULL, ContingencyType.fromName(""));
        assertEquals(ContingencyType.NULL, ContingencyType.fromName("explode"));
    }

    @Test
    public void testValuesCount() {
        // Ensures no contingency type is accidentally added or removed without notice.
        assertEquals(6, ContingencyType.values().length);
    }
}
