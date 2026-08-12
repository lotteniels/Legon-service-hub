package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomMapTest {

    @Test
    public void testMapOperations() {
        CustomMap<String, String> map = new CustomMap<>();
        map.put("Key1", "Value1");
        map.put("Key2", "Value2");
        
        assertTrue(map.containsKey("Key1"));
        assertEquals("Value1", map.get("Key1"));
        assertEquals(2, map.size());
        
        map.remove("Key1");
        assertFalse(map.containsKey("Key1"));
        assertEquals(1, map.size());
    }

    @Test
    public void testUpdateMap() {
        CustomMap<Integer, String> map = new CustomMap<>();
        map.put(1, "A");
        map.put(1, "B");
        assertEquals("B", map.get(1));
    }
}
