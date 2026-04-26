package am2.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MathUtilitiesTest {

    private static final float DELTA = 1e-4f;

    // ---- color conversion -----------------------------------------------

    @Test
    public void testColorIntToFloatsWhite() {
        float[] rgb = MathUtilities.colorIntToFloats(0xFFFFFF);
        assertEquals(1.0f, rgb[0], DELTA);
        assertEquals(1.0f, rgb[1], DELTA);
        assertEquals(1.0f, rgb[2], DELTA);
    }

    @Test
    public void testColorIntToFloatsBlack() {
        float[] rgb = MathUtilities.colorIntToFloats(0x000000);
        assertEquals(0.0f, rgb[0], DELTA);
        assertEquals(0.0f, rgb[1], DELTA);
        assertEquals(0.0f, rgb[2], DELTA);
    }

    @Test
    public void testColorIntToFloatsPureRed() {
        float[] rgb = MathUtilities.colorIntToFloats(0xFF0000);
        assertEquals(1.0f, rgb[0], DELTA);
        assertEquals(0.0f, rgb[1], DELTA);
        assertEquals(0.0f, rgb[2], DELTA);
    }

    @Test
    public void testColorRoundTrip() {
        int original = 0x4080C0;
        float[] floats = MathUtilities.colorIntToFloats(original);
        int reconstructed = MathUtilities.colorFloatsToInt(floats[0], floats[1], floats[2]);
        // allow 1-unit rounding error per channel
        int rDiff = Math.abs(((original >> 16) & 0xFF) - ((reconstructed >> 16) & 0xFF));
        int gDiff = Math.abs(((original >> 8)  & 0xFF) - ((reconstructed >> 8)  & 0xFF));
        int bDiff = Math.abs(( original        & 0xFF) - ( reconstructed        & 0xFF));
        assertTrue(rDiff <= 1, "Red channel diff: " + rDiff);
        assertTrue(gDiff <= 1, "Green channel diff: " + gDiff);
        assertTrue(bDiff <= 1, "Blue channel diff: " + bDiff);
    }

    // ---- array helpers --------------------------------------------------

    @Test
    public void testPushAddsToEnd() {
        int[] result = MathUtilities.push(new int[]{1, 2, 3}, 4);
        assertArrayEquals(new int[]{1, 2, 3, 4}, result);
    }

    @Test
    public void testPushOnEmptyArray() {
        int[] result = MathUtilities.push(new int[]{}, 7);
        assertArrayEquals(new int[]{7}, result);
    }

    @Test
    public void testSpliceRemovesIndex() {
        int[] result = MathUtilities.splice(new int[]{10, 20, 30, 40}, 1);
        assertArrayEquals(new int[]{10, 30, 40}, result);
    }

    @Test
    public void testSpliceFirstElement() {
        int[] result = MathUtilities.splice(new int[]{10, 20, 30}, 0);
        assertArrayEquals(new int[]{20, 30}, result);
    }

    @Test
    public void testSpliceLastElement() {
        int[] result = MathUtilities.splice(new int[]{10, 20, 30}, 2);
        assertArrayEquals(new int[]{10, 20}, result);
    }

    @Test
    public void testSpliceSingleElementReturnsOriginal() {
        int[] arr = new int[]{5};
        int[] result = MathUtilities.splice(arr, 0);
        assertArrayEquals(arr, result);
    }

    // ---- rotation normalization -----------------------------------------

    @Test
    public void testNormalizeRotationInRange() {
        assertEquals(90.0,  MathUtilities.NormalizeRotation(90.0),  1e-9);
        assertEquals(0.0,   MathUtilities.NormalizeRotation(0.0),   1e-9);
        assertEquals(359.0, MathUtilities.NormalizeRotation(359.0), 1e-9);
    }

    @Test
    public void testNormalizeRotationNegative() {
        assertEquals(270.0, MathUtilities.NormalizeRotation(-90.0), 1e-9);
        assertEquals(359.0, MathUtilities.NormalizeRotation(-1.0),  1e-9);
    }

    @Test
    public void testNormalizeRotationOver360() {
        assertEquals(1.0,  MathUtilities.NormalizeRotation(361.0), 1e-9);
        assertEquals(90.0, MathUtilities.NormalizeRotation(450.0), 1e-9);
    }
}
