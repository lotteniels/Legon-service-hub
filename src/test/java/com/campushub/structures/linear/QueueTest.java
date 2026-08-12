package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueueTest {

    @Test
    public void testEnqueueAndDequeue() {
        Queue<Integer> queue = new Queue<>();
        queue.enqueue(1);
        queue.enqueue(2);
        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testPeek() {
        Queue<String> queue = new Queue<>();
        queue.enqueue("First");
        queue.enqueue("Second");
        assertEquals("First", queue.peek());
        assertEquals(2, queue.size());
    }

    @Test
    public void testEmptyQueueExceptions() {
        Queue<Double> queue = new Queue<>();
        assertThrows(IllegalStateException.class, () -> queue.dequeue());
        assertThrows(IllegalStateException.class, () -> queue.peek());
    }
}
