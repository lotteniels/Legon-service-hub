package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomMapTest {

    @Test
    public void testPutAndGet() {
        CustomMap<Integer, String> map = new CustomMap<>();
        map.put(1, "Location A");
        map.put(2, "Location B");

        assertEquals("Location A", map.get(1));
        assertEquals("Location B", map.get(2));
        assertEquals(2, map.size());
        assertTrue(map.containsKey(1));
    }

    @Test
    public void testOverwriteExistingKey() {
        CustomMap<String, String> map = new CustomMap<>();
        map.put("R1", "Pending");
        map.put("R1", "Completed"); 

        assertEquals("Completed", map.get("R1"));
        assertEquals(1, map.size()); 
    }

    @Test
    public void testOperationsOnEmptyMap() {
        CustomMap<Integer, String> map = new CustomMap<>();
        
        assertEquals(0, map.size());
        assertFalse(map.containsKey(99)); 
        assertNull(map.get(99));
        assertNull(map.remove(99)); 
    }
}
