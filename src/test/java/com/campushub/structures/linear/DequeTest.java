package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DequeTest {

    private Deque<String> deque;

    @BeforeEach
    public void setUp() {
        deque = new Deque<>();
    }

    @Test
    public void testInitialization() {
        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    public void testAddFrontAndRemoveFront() {
        deque.addFront("B");
        deque.addFront("A");

        assertEquals(2, deque.size());
        assertEquals("A", deque.peekFront());
        assertEquals("A", deque.removeFront());
        assertEquals("B", deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testAddRearAndRemoveRear() {
        deque.addRear("X");
        deque.addRear("Y");

        assertEquals(2, deque.size());
        assertEquals("Y", deque.peekRear());
        assertEquals("Y", deque.removeRear());
        assertEquals("X", deque.removeRear());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testAddFrontAndRemoveRear() {
        deque.addFront("1");
        deque.addFront("2");
        deque.addFront("3");

        assertEquals("1", deque.removeRear());
        assertEquals("2", deque.removeRear());
        assertEquals("3", deque.removeRear());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testAddRearAndRemoveFront() {
        deque.addRear("Alpha");
        deque.addRear("Beta");

        assertEquals("Alpha", deque.removeFront());
        assertEquals("Beta", deque.removeFront());
        assertTrue(deque.isEmpty());
    }

    @Test
    public void testClear() {
        deque.addFront("Node1");
        deque.addRear("Node2");
        deque.clear();

        assertTrue(deque.isEmpty());
        assertEquals(0, deque.size());
    }

    @Test
    public void testEmptyDequeExceptions() {
        assertThrows(IllegalStateException.class, () -> deque.removeFront());
        assertThrows(IllegalStateException.class, () -> deque.removeRear());
        assertThrows(IllegalStateException.class, () -> deque.peekFront());
        assertThrows(IllegalStateException.class, () -> deque.peekRear());
    }

    @Test
    public void testToArrayAndMetrics() {
        deque.addFront("Mid");
        deque.addFront("Head");
        deque.addRear("Tail");

        Object[] arr = deque.toArray();
        assertEquals(3, arr.length);
        assertEquals("Head", arr[0]);
        assertEquals("Mid", arr[1]);
        assertEquals("Tail", arr[2]);

        assertTrue(deque.getFrontOps() > 0);
        assertTrue(deque.getRearOps() > 0);
        assertTrue(deque.getOperationCount() > 0);

        deque.resetOpCounters();
        assertEquals(0, deque.getFrontOps());
        assertEquals(0, deque.getRearOps());
        assertEquals(0, deque.getOperationCount());
    }
}
