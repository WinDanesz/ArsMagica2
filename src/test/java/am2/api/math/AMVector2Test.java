package am2.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AMVector2Test {

    @Test
    public void testConstruction() {
        AMVector2 v = new AMVector2(3.7, 2.2);
        assertEquals(3.7, v.x, 1e-9);
        assertEquals(2.2, v.y, 1e-9);
        assertEquals(3, v.iX);
        assertEquals(2, v.iY);
    }

    @Test
    public void testAdd() {
        AMVector2 v = new AMVector2(1.0, 2.0).add(new AMVector2(3.0, 4.0));
        assertEquals(4.0, v.x, 1e-9);
        assertEquals(6.0, v.y, 1e-9);
    }

    @Test
    public void testSubtract() {
        AMVector2 v = new AMVector2(5.0, 3.0).subtract(new AMVector2(2.0, 1.0));
        assertEquals(3.0, v.x, 1e-9);
        assertEquals(2.0, v.y, 1e-9);
    }

    @Test
    public void testMultiply() {
        AMVector2 v = new AMVector2(2.0, 3.0).multiply(new AMVector2(4.0, 5.0));
        assertEquals(8.0, v.x, 1e-9);
        assertEquals(15.0, v.y, 1e-9);
    }

    @Test
    public void testDivide() {
        AMVector2 v = new AMVector2(8.0, 9.0).divide(new AMVector2(2.0, 3.0));
        assertEquals(4.0, v.x, 1e-9);
        assertEquals(3.0, v.y, 1e-9);
    }

    @Test
    public void testImmutability() {
        AMVector2 a = new AMVector2(1.0, 2.0);
        AMVector2 b = new AMVector2(3.0, 4.0);
        a.add(b);
        // original vectors must be unchanged
        assertEquals(1.0, a.x, 1e-9);
        assertEquals(2.0, a.y, 1e-9);
    }
}
