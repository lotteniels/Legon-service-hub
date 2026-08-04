package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomSetTest {

    @Test
    public void testAddAndContains() {
        CustomSet<String> set = new CustomSet<>();
        set.add("Resource A");
        set.add("Resource B");

        assertTrue(set.contains("Resource A"));
        assertTrue(set.contains("Resource B"));
        assertEquals(2, set.size());
    }

    @Test
    public void testAddDuplicates() {
        CustomSet<Integer> set = new CustomSet<>();
        set.add(100);
        set.add(100); 
        set.add(100); 

        assertEquals(1, set.size()); 
        assertTrue(set.contains(100));
    }

    @Test
    public void testRemoveNonExistentAndEmpty() {
        CustomSet<String> set = new CustomSet<>();
        
        assertEquals(0, set.size());
        assertFalse(set.contains("Unknown"));
        
        set.remove("Unknown"); 
        assertEquals(0, set.size());
    }
}
