package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CircularQueueTest {

    private CircularQueue<Integer> cq;

    @BeforeEach
    public void setUp() {
        cq = new CircularQueue<>(4);
    }

    @Test
    public void testInitialization() {
        assertTrue(cq.isEmpty());
        assertFalse(cq.isFull());
        assertEquals(0, cq.size());
        assertEquals(4, cq.capacity());
    }

    @Test
    public void testEnqueueAndDequeue() {
        cq.enqueue(10);
        cq.enqueue(20);
        cq.enqueue(30);

        assertEquals(3, cq.size());
        assertEquals(10, cq.peek());
        assertEquals(10, cq.dequeue());
        assertEquals(20, cq.dequeue());
        assertEquals(30, cq.dequeue());
        assertTrue(cq.isEmpty());
    }

    @Test
    public void testWrapAroundBehavior() {
        cq.enqueue(1);
        cq.enqueue(2);
        cq.enqueue(3);
        cq.enqueue(4);
        assertEquals(4, cq.size());

        // Dequeue two elements so front advances
        assertEquals(1, cq.dequeue());
        assertEquals(2, cq.dequeue());

        // Enqueue two elements to trigger wrap-around to index 0 and 1
        cq.enqueue(5);
        cq.enqueue(6);

        assertEquals(4, cq.size());
        assertTrue(cq.getWrapCount() > 0);
        assertEquals(3, cq.dequeue());
        assertEquals(4, cq.dequeue());
        assertEquals(5, cq.dequeue());
        assertEquals(6, cq.dequeue());
    }

    @Test
    public void testDynamicResizeWhenFull() {
        cq.enqueue(100);
        cq.enqueue(200);
        cq.enqueue(300);
        cq.enqueue(400);
        assertTrue(cq.isFull());

        cq.enqueue(500); // Auto-grows to capacity 8
        assertEquals(5, cq.size());
        assertEquals(8, cq.capacity());
        assertTrue(cq.getResizeCount() > 0);
    }

    @Test
    public void testFixedCapacityMode() {
        CircularQueue<String> fixedQueue = new CircularQueue<>(2, true);
        fixedQueue.enqueue("A");
        fixedQueue.enqueue("B");
        assertTrue(fixedQueue.isFull());

        assertThrows(IllegalStateException.class, () -> fixedQueue.enqueue("C"));
    }

    @Test
    public void testClearAndIndices() {
        cq.enqueue(1);
        cq.enqueue(2);
        cq.clear();

        assertTrue(cq.isEmpty());
        assertEquals(0, cq.size());
        assertEquals(0, cq.getFrontIndex());
        assertEquals(-1, cq.getRearIndex());
    }

    @Test
    public void testEmptyQueueExceptions() {
        assertThrows(IllegalStateException.class, () -> cq.dequeue());
        assertThrows(IllegalStateException.class, () -> cq.peek());
    }

    @Test
    public void testToArrayAndMetrics() {
        cq.enqueue(10);
        cq.enqueue(20);
        cq.enqueue(30);

        Object[] arr = cq.toArray();
        assertEquals(3, arr.length);
        assertEquals(10, arr[0]);
        assertEquals(20, arr[1]);
        assertEquals(30, arr[2]);

        assertTrue(cq.getOperationCount() > 0);
        cq.resetOpCounters();
        assertEquals(0, cq.getWrapCount());
        assertEquals(0, cq.getResizeCount());
        assertEquals(0, cq.getOperationCount());
    }
}
