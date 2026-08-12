package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTest {

    @Test
    public void testAddAndGet() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("A");
        list.addLast("B");
        list.addFirst("C");
        
        assertEquals(3, list.size());
        assertEquals("C", list.get(0));
        assertEquals("A", list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test
    public void testRemoveFirstAndLast() {
        LinkedList<Integer> list = new LinkedList<>();
        list.addLast(1);
        list.addLast(2);
        list.addLast(3);
        
        assertEquals(1, list.removeFirst());
        assertEquals(3, list.removeLast());
        assertEquals(1, list.size());
        assertEquals(2, list.get(0));
    }

    @Test
    public void testRemoveValue() {
        LinkedList<String> list = new LinkedList<>();
        list.addLast("X");
        list.addLast("Y");
        list.addLast("Z");
        
        assertTrue(list.remove("Y"));
        assertFalse(list.remove("W"));
        assertEquals(2, list.size());
        assertEquals("Z", list.get(1));
    }

    @Test
    public void testIterator() {
        LinkedList<Integer> list = new LinkedList<>();
        list.addLast(10);
        list.addLast(20);
        
        LinkedList<Integer>.LinkedListIterator it = list.getIterator();
        assertTrue(it.hasNext());
        assertEquals(10, it.next());
        assertTrue(it.hasNext());
        assertEquals(20, it.next());
        assertFalse(it.hasNext());
        assertThrows(IllegalStateException.class, () -> it.next());
    }

    @Test
    public void testEmptyListExceptions() {
        LinkedList<String> list = new LinkedList<>();
        assertThrows(IllegalStateException.class, () -> list.removeFirst());
        assertThrows(IllegalStateException.class, () -> list.removeLast());
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
    }
}
