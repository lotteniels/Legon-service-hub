package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DequeTest {

    @Test
    public void testAddAndRemoveFront() {
        Deque<Integer> deque = new Deque<>();
        deque.addFront(1);
        deque.addFront(2);
        assertEquals(2, deque.peekFront());
        assertEquals(2, deque.removeFront());
        assertEquals(1, deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testAddAndRemoveRear() {
        Deque<String> deque = new Deque<>();
        deque.addRear("A");
        deque.addRear("B");
        assertEquals("B", deque.peekRear());
        assertEquals("B", deque.removeRear());
        assertEquals("A", deque.removeRear());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testMixedOperations() {
        Deque<Integer> deque = new Deque<>();
        deque.addFront(1);
        deque.addRear(2);
        deque.addFront(0);
        // Deque: 0, 1, 2
        assertEquals(3, deque.size());
        assertEquals(0, deque.removeFront());
        assertEquals(2, deque.removeRear());
        assertEquals(1, deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testEmptyExceptions() {
        Deque<Double> deque = new Deque<>();
        assertThrows(IllegalStateException.class, () -> deque.removeFront());
        assertThrows(IllegalStateException.class, () -> deque.removeRear());
    }
}
