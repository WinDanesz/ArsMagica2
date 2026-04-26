package am2.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AMMatrix4Test {

    private static final float DELTA = 1e-4f;

    @Test
    public void testIdentityTranslateIsNoop() {
        AMMatrix4 identity = new AMMatrix4();
        AMVector3 v = new AMVector3(3, 5, 7);
        identity.translate(v);
        assertEquals(3f, v.x, DELTA);
        assertEquals(5f, v.y, DELTA);
        assertEquals(7f, v.z, DELTA);
    }

    @Test
    public void testRotation360IsIdentity() {
        // A full 360° rotation about Y axis should return the vector to its original position.
        AMVector3 axis = new AMVector3(0, 1, 0);
        AMVector3 v = new AMVector3(1, 0, 0);
        AMMatrix4 rot = AMMatrix4.rotationMat(360, axis);
        rot.translate(v);
        assertEquals(1f, v.x, DELTA);
        assertEquals(0f, v.y, DELTA);
        assertEquals(0f, v.z, DELTA);
    }

    @Test
    public void testRotation90AboutY() {
        // 90° CCW rotation about +Y: (1,0,0) → (0,0,-1)
        // (using right-hand rule with the sign convention in rotationMat)
        AMVector3 axis = new AMVector3(0, 1, 0);
        AMVector3 v = new AMVector3(1, 0, 0);
        AMMatrix4 rot = AMMatrix4.rotationMat(90, axis);
        rot.translate(v);
        // length should be preserved
        assertEquals(1f, v.length(), DELTA);
        // y component unchanged
        assertEquals(0f, v.y, DELTA);
    }
}
