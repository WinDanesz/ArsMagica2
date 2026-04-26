package am2.api.spell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OperationTest {

    private static final double DELTA = 1e-9;

    @Test
    public void testAdd() {
        assertEquals(7.0, Operation.ADD.apply(3.0, 4.0), DELTA);
        assertEquals(0.0, Operation.ADD.apply(0.0, 0.0), DELTA);
        assertEquals(-1.0, Operation.ADD.apply(-3.0, 2.0), DELTA);
    }

    @Test
    public void testSubtract() {
        assertEquals(1.0, Operation.SUBTRACT.apply(3.0, 2.0), DELTA);
        assertEquals(-5.0, Operation.SUBTRACT.apply(0.0, 5.0), DELTA);
    }

    @Test
    public void testMultiply() {
        assertEquals(12.0, Operation.MULTIPLY.apply(3.0, 4.0), DELTA);
        assertEquals(0.0, Operation.MULTIPLY.apply(0.0, 99.0), DELTA);
        assertEquals(-6.0, Operation.MULTIPLY.apply(-2.0, 3.0), DELTA);
    }

    @Test
    public void testDivide() {
        assertEquals(2.5, Operation.DIVIDE.apply(5.0, 2.0), DELTA);
        assertEquals(1.0, Operation.DIVIDE.apply(4.0, 4.0), DELTA);
    }

    @Test
    public void testPow() {
        assertEquals(8.0, Operation.POW.apply(2.0, 3.0), DELTA);
        assertEquals(1.0, Operation.POW.apply(5.0, 0.0), DELTA);
    }

    @Test
    public void testReplace() {
        assertEquals(99.0, Operation.REPLACE.apply(1.0, 99.0), DELTA);
        assertEquals(0.0, Operation.REPLACE.apply(42.0, 0.0), DELTA);
    }
}
