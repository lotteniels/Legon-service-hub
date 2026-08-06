package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicArrayTest {

    private DynamicArray<String> array;

    @BeforeEach
    public void setUp() {
        array = new DynamicArray<>(4);
    }

    @Test
    public void testInitializationAndDefaults() {
        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
        assertEquals(4, array.capacity());
    }

    @Test
    public void testAddAndGet() {
        array.add("JQB");
        array.add("Balme");
        array.add("Night Market");

        assertEquals(3, array.size());
        assertEquals("JQB", array.get(0));
        assertEquals("Balme", array.get(1));
        assertEquals("Night Market", array.get(2));
        assertFalse(array.isEmpty());
    }

    @Test
    public void testInsertMiddleAndStart() {
        array.add("A");
        array.add("C");
        array.insert(1, "B"); // Insert in middle
        array.insert(0, "START"); // Insert at start

        assertEquals(4, array.size());
        assertEquals("START", array.get(0));
        assertEquals("A", array.get(1));
        assertEquals("B", array.get(2));
        assertEquals("C", array.get(3));
    }

    @Test
    public void testResizeAndCapacity() {
        array.add("1");
        array.add("2");
        array.add("3");
        array.add("4");
        assertEquals(4, array.capacity());

        array.add("5"); // Triggers auto-doubling to capacity 8
        assertEquals(5, array.size());
        assertEquals(8, array.capacity());
        assertTrue(array.getResizeCount() > 0);
    }

    @Test
    public void testSetAndRemove() {
        array.add("First");
        array.add("Second");
        array.set(1, "UpdatedSecond");

        assertEquals("UpdatedSecond", array.get(1));

        String removed = array.remove(0);
        assertEquals("First", removed);
        assertEquals(1, array.size());
        assertEquals("UpdatedSecond", array.get(0));
    }

    @Test
    public void testRemoveValueAndIndexOf() {
        array.add("Alpha");
        array.add("Beta");
        array.add("Gamma");

        assertEquals(1, array.indexOf("Beta"));
        assertTrue(array.contains("Gamma"));
        assertTrue(array.removeValue("Beta"));
        assertFalse(array.contains("Beta"));
        assertEquals(2, array.size());
        assertFalse(array.removeValue("NonExistent"));
    }

    @Test
    public void testClearAndTrimToSize() {
        array.add("X");
        array.add("Y");
        array.add("Z");
        array.add("W");
        array.add("V"); // Capacity 8

        array.trimToSize();
        assertEquals(5, array.capacity());

        array.clear();
        assertTrue(array.isEmpty());
        assertEquals(0, array.size());
    }

    @Test
    public void testIndexOutOfBoundsExceptions() {
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> array.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> array.set(0, "Val"));
        assertThrows(IndexOutOfBoundsException.class, () -> array.insert(5, "Val"));
    }

    @Test
    public void testToArrayAndMetrics() {
        array.add("One");
        array.add("Two");

        Object[] arr = array.toArray();
        assertEquals(2, arr.length);
        assertEquals("One", arr[0]);
        assertEquals("Two", arr[1]);

        assertTrue(array.getElementWrites() > 0);
        assertTrue(array.getOperationCount() > 0);

        array.resetOpCounters();
        assertEquals(0, array.getElementWrites());
        assertEquals(0, array.getOperationCount());
    }
}
