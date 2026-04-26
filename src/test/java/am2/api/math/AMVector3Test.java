package am2.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AMVector3Test {

    private static final float DELTA = 1e-4f;

    @Test
    public void testConstruction() {
        AMVector3 v = new AMVector3(1.0, 2.0, 3.0);
        assertEquals(1.0f, v.x, DELTA);
        assertEquals(2.0f, v.y, DELTA);
        assertEquals(3.0f, v.z, DELTA);
    }

    @Test
    public void testAdd() {
        AMVector3 v = new AMVector3(1, 2, 3);
        v.add(new AMVector3(4, 5, 6));
        assertEquals(5f, v.x, DELTA);
        assertEquals(7f, v.y, DELTA);
        assertEquals(9f, v.z, DELTA);
    }

    @Test
    public void testSub() {
        AMVector3 v = new AMVector3(5, 7, 9);
        v.sub(new AMVector3(1, 2, 3));
        assertEquals(4f, v.x, DELTA);
        assertEquals(5f, v.y, DELTA);
        assertEquals(6f, v.z, DELTA);
    }

    @Test
    public void testScale() {
        AMVector3 v = new AMVector3(2, 3, 4);
        v.scale(2.0f);
        assertEquals(4f, v.x, DELTA);
        assertEquals(6f, v.y, DELTA);
        assertEquals(8f, v.z, DELTA);
    }

    @Test
    public void testLength() {
        // 3-4-5 right triangle in XY, Z=0
        AMVector3 v = new AMVector3(3, 4, 0);
        assertEquals(5.0f, v.length(), DELTA);
    }

    @Test
    public void testLengthPow2() {
        AMVector3 v = new AMVector3(3, 4, 0);
        assertEquals(25.0f, v.lengthPow2(), DELTA);
    }

    @Test
    public void testNormalize() {
        AMVector3 v = new AMVector3(3, 4, 0);
        v.normalize();
        assertEquals(1.0f, v.length(), DELTA);
    }

    @Test
    public void testCopy() {
        AMVector3 original = new AMVector3(1, 2, 3);
        AMVector3 copy = original.copy();
        assertEquals(original.x, copy.x, DELTA);
        assertEquals(original.y, copy.y, DELTA);
        assertEquals(original.z, copy.z, DELTA);
        // modifying copy must not affect original
        copy.x = 99;
        assertEquals(1f, original.x, DELTA);
    }

    @Test
    public void testIsZero() {
        assertTrue(AMVector3.zero().isZero());
        assertFalse(new AMVector3(0, 0, 1).isZero());
    }

    @Test
    public void testDistanceTo() {
        AMVector3 a = new AMVector3(0, 0, 0);
        AMVector3 b = new AMVector3(3, 4, 0);
        assertEquals(5.0, a.distanceTo(b), 1e-3);
    }

    @Test
    public void testDistanceSqTo() {
        AMVector3 a = new AMVector3(0, 0, 0);
        AMVector3 b = new AMVector3(3, 4, 0);
        assertEquals(25.0, a.distanceSqTo(b), 1e-3);
    }

    @Test
    public void testCrossProduct() {
        // X cross Y = Z
        AMVector3 x = new AMVector3(1, 0, 0);
        AMVector3 y = new AMVector3(0, 1, 0);
        AMVector3 z = AMVector3.crossProduct(x, y);
        assertEquals(0f, z.x, DELTA);
        assertEquals(0f, z.y, DELTA);
        assertEquals(1f, z.z, DELTA);
    }

    @Test
    public void testDotProduct() {
        // Parallel unit vectors → dot = 1
        AMVector3 a = new AMVector3(1, 0, 0);
        assertEquals(1.0f, AMVector3.dotProduct(a, a), DELTA);
        // Perpendicular → dot = 0
        AMVector3 b = new AMVector3(0, 1, 0);
        assertEquals(0.0f, AMVector3.dotProduct(a, b), DELTA);
    }

    @Test
    public void testAngle() {
        AMVector3 a = new AMVector3(1, 0, 0);
        AMVector3 b = new AMVector3(0, 1, 0);
        // angle between X and Y axes is 90° = π/2
        assertEquals(Math.PI / 2, AMVector3.angle(a, b), 1e-3);
    }

    @Test
    public void testDifferenceConstructor() {
        AMVector3 a = new AMVector3(5, 7, 9);
        AMVector3 b = new AMVector3(1, 2, 3);
        AMVector3 diff = new AMVector3(a, b);
        assertEquals(4f, diff.x, DELTA);
        assertEquals(5f, diff.y, DELTA);
        assertEquals(6f, diff.z, DELTA);
    }

    @Test
    public void testRounding() {
        AMVector3 v = new AMVector3(1.7, 2.3, 3.5);
        v.floorToI();
        assertEquals(1f, v.x, DELTA);
        assertEquals(2f, v.y, DELTA);
        assertEquals(3f, v.z, DELTA);
    }
}
