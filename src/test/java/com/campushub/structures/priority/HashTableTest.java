package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashTableTest {

    @Test
    public void testPutAndGet() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put("Alice", 25);
        table.put("Bob", 30);

        assertEquals(25, table.get("Alice"));
        assertEquals(30, table.get("Bob"));
        assertEquals(2, table.size());
    }

    @Test
    public void testUpdateExistingKey() {
        HashTable<String, String> table = new HashTable<>();
        table.put("Role", "Student");
        table.put("Role", "Admin");
        
        assertEquals("Admin", table.get("Role"));
        assertEquals(1, table.size());
    }

    @Test
    public void testResizeAndCollisionStats() {
        HashTable<Integer, String> table = new HashTable<>(4); 
        
        for (int i = 0; i < 10; i++) {
            table.put(i, "Value " + i);
        }

        assertEquals(10, table.size());
        
        for (int i = 0; i < 10; i++) {
            assertEquals("Value " + i, table.get(i));
        }
        
        assertTrue(table.getCollisionStats() >= 0);
    }

    @Test
    public void testGetAndRemoveNonExistentKey() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put("Alice", 25);

        assertNull(table.get("Charlie")); 
        assertNull(table.remove("Charlie")); 
        assertEquals(1, table.size());
    }

    @Test
    public void testNullKeys() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put(null, 100); 
        
        assertEquals(0, table.size());
        assertNull(table.get(null));
        assertNull(table.remove(null));
    }
}
