package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTest {

    private LinkedList<Integer> list;

    @BeforeEach
    public void setUp() {
        list = new LinkedList<>();
    }

    @Test
    public void testInitialization() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertNull(list.getHead());
        assertNull(list.getTail());
    }

    @Test
    public void testAddFirstAndAddLast() {
        list.addFirst(20);
        list.addFirst(10);
        list.addLast(30);

        assertEquals(3, list.size());
        assertEquals(10, list.getHead().getValue());
        assertEquals(30, list.getTail().getValue());
        assertEquals(10, list.get(0));
        assertEquals(20, list.get(1));
        assertEquals(30, list.get(2));
    }

    @Test
    public void testInsertAfterNodeAndIndex() {
        list.addLast(100);
        list.addLast(300);
        list.insertAfter(0, 200); // Insert 200 after index 0

        assertEquals(3, list.size());
        assertEquals(100, list.get(0));
        assertEquals(200, list.get(1));
        assertEquals(300, list.get(2));
    }

    @Test
    public void testGetAndSet() {
        list.addLast(5);
        list.addLast(10);
        list.set(1, 15);

        assertEquals(5, list.get(0));
        assertEquals(15, list.get(1));
    }

    @Test
    public void testRemoveFirstAndRemoveLast() {
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);

        assertEquals(1, list.removeFirst());
        assertEquals(3, list.removeLast());
        assertEquals(1, list.size());
        assertEquals(2, list.get(0));
    }

    @Test
    public void testRemoveByValueAndIndex() {
        list.addLast(10);
        list.addLast(20);
        list.addLast(30);

        assertTrue(list.remove(Integer.valueOf(20)));
        assertEquals(2, list.size());
        assertEquals(30, list.get(1));

        assertEquals(30, list.removeAt(1));
        assertEquals(1, list.size());
        assertFalse(list.remove(Integer.valueOf(999)));
    }

    @Test
    public void testIndexOfAndContains() {
        list.addLast(100);
        list.addLast(200);

        assertEquals(0, list.indexOf(100));
        assertEquals(1, list.indexOf(200));
        assertEquals(-1, list.indexOf(500));
        assertTrue(list.contains(100));
        assertFalse(list.contains(500));
    }

    @Test
    public void testClear() {
        list.addLast(10);
        list.addLast(20);
        list.clear();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertNull(list.getHead());
        assertNull(list.getTail());
    }

    @Test
    public void testEmptyExceptions() {
        assertThrows(IllegalStateException.class, () -> list.removeFirst());
        assertThrows(IllegalStateException.class, () -> list.removeLast());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> list.removeAt(0));
    }

    @Test
    public void testMetricsAndToArray() {
        list.addLast(5);
        list.addLast(15);
        list.get(1); // Triggers traversal

        assertTrue(list.getTraversalSteps() > 0);
        assertEquals(2, list.getNodeAllocations());

        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals(5, arr[0]);
        assertEquals(15, arr[1]);

        list.resetOpCounters();
        assertEquals(0, list.getTraversalSteps());
        assertEquals(0, list.getNodeAllocations());
    }
}
