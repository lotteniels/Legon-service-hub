package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomSetTest {

    @Test
    public void testSetOperations() {
        CustomSet<String> set = new CustomSet<>();
        set.add("A");
        set.add("B");
        set.add("A"); // Duplicate shouldn't increase size
        
        assertEquals(2, set.size());
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertFalse(set.contains("C"));
        
        set.remove("A");
        assertFalse(set.contains("A"));
        assertEquals(1, set.size());
    }

    @Test
    public void testEmptySet() {
        CustomSet<Integer> set = new CustomSet<>();
        assertFalse(set.contains(1));
        assertEquals(0, set.size());
    }
}
