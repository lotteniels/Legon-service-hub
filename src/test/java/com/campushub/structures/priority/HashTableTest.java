package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashTableTest {

    @Test
    public void testPutAndGet() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put("A", 1);
        table.put("B", 2);
        assertEquals(1, table.get("A"));
        assertEquals(2, table.get("B"));
        assertEquals(2, table.size());
    }

    @Test
    public void testUpdateExistingKey() {
        HashTable<String, String> table = new HashTable<>();
        table.put("Key", "Value1");
        table.put("Key", "Value2");
        assertEquals("Value2", table.get("Key"));
        assertEquals(1, table.size());
    }

    @Test
    public void testRemove() {
        HashTable<String, Integer> table = new HashTable<>();
        table.put("A", 1);
        table.put("B", 2);
        assertEquals(1, table.remove("A"));
        assertNull(table.get("A"));
        assertEquals(1, table.size());
    }

    @Test
    public void testResizeAndCollisions() {
        HashTable<Integer, String> table = new HashTable<>(2);
        for (int i = 0; i < 20; i++) {
            table.put(i, "Val" + i);
        }
        assertEquals(20, table.size());
        assertEquals("Val15", table.get(15));
    }
}
