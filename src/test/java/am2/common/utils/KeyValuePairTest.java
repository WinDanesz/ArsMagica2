package am2.common.utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class KeyValuePairTest {

    @Test
    public void testConstruction() {
        KeyValuePair<String, Integer> pair = new KeyValuePair<>("hello", 42);
        assertEquals("hello", pair.key);
        assertEquals(42, pair.value);
    }

    @Test
    public void testMergeFlattensInOrder() {
        ArrayList<KeyValuePair<String, String>> pairs = new ArrayList<>();
        pairs.add(new KeyValuePair<>("a", "b"));
        pairs.add(new KeyValuePair<>("c", "d"));
        ArrayList<String> merged = KeyValuePair.merge(pairs);
        assertEquals(4, merged.size());
        assertEquals("a", merged.get(0));
        assertEquals("b", merged.get(1));
        assertEquals("c", merged.get(2));
        assertEquals("d", merged.get(3));
    }

    @Test
    public void testMergeEmptyList() {
        ArrayList<String> merged = KeyValuePair.merge(new ArrayList<>());
        assertTrue(merged.isEmpty());
    }
}
