package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CircularQueueTest {

    @Test
    public void testEnqueueAndDequeue() {
        CircularQueue<Integer> queue = new CircularQueue<>(3);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        assertEquals(3, queue.size());
        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.size());
    }

    @Test
    public void testWrapAround() {
        CircularQueue<Integer> queue = new CircularQueue<>(3, true);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        
        queue.dequeue(); // remove 1
        queue.enqueue(4); // should wrap around
        
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());
        assertEquals(4, queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testDynamicGrowth() {
        CircularQueue<Integer> queue = new CircularQueue<>(2, false);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3); // Should trigger resize
        assertEquals(3, queue.size());
        assertEquals(1, queue.dequeue());
    }

    @Test
    public void testFixedCapacityException() {
        CircularQueue<Integer> queue = new CircularQueue<>(2, true);
        queue.enqueue(1);
        queue.enqueue(2);
        assertThrows(IllegalStateException.class, () -> queue.enqueue(3));
    }
}
