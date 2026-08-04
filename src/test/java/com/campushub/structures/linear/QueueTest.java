package com.campushub.structures.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    private Queue<String> queue;

    @BeforeEach
    public void setUp() {
        queue = new Queue<>();
    }

    @Test
    public void testInitialization() {
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testEnqueueAndDequeue() {
        queue.enqueue("Req1");
        queue.enqueue("Req2");
        queue.enqueue("Req3");

        assertEquals(3, queue.size());
        assertEquals("Req1", queue.dequeue());
        assertEquals("Req2", queue.dequeue());
        assertEquals("Req3", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testPeek() {
        queue.enqueue("TaskA");
        queue.enqueue("TaskB");

        assertEquals("TaskA", queue.peek());
        assertEquals(2, queue.size()); // Size should remain unchanged
    }

    @Test
    public void testMultipleEnqueueDequeueInterleaved() {
        queue.enqueue("1");
        queue.enqueue("2");
        assertEquals("1", queue.dequeue());

        queue.enqueue("3");
        assertEquals("2", queue.dequeue());
        assertEquals("3", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testClear() {
        queue.enqueue("X");
        queue.enqueue("Y");
        queue.clear();

        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    @Test
    public void testEmptyQueueExceptions() {
        assertThrows(IllegalStateException.class, () -> queue.dequeue());
        assertThrows(IllegalStateException.class, () -> queue.peek());
    }

    @Test
    public void testToArrayAndMetrics() {
        queue.enqueue("First");
        queue.enqueue("Second");

        Object[] arr = queue.toArray();
        assertEquals(2, arr.length);
        assertEquals("First", arr[0]);
        assertEquals("Second", arr[1]);

        assertEquals(2, queue.getEnqueueCount());
        assertEquals(0, queue.getDequeueCount());
        assertTrue(queue.getPointerMoves() > 0);

        queue.dequeue();
        assertEquals(1, queue.getDequeueCount());

        queue.resetOpCounters();
        assertEquals(0, queue.getEnqueueCount());
        assertEquals(0, queue.getDequeueCount());
        assertEquals(0, queue.getPointerMoves());
    }
}
