package com.campushub.structures.priority;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PriorityQueueTest {

    @Test
    public void testEnqueueDequeue() {
        PriorityQueue<String> pq = new PriorityQueue<>(5);
        pq.enqueue("LowPriority", 10);
        pq.enqueue("HighPriority", 1);
        pq.enqueue("MedPriority", 5);

        assertEquals(3, pq.size());
        assertEquals("HighPriority", pq.dequeue(), "Lowest number is highest priority (Min-Heap)");
        assertEquals("MedPriority", pq.dequeue());
        assertEquals("LowPriority", pq.dequeue());
        assertTrue(pq.isEmpty());
    }

    @Test
    public void testPeek() {
        PriorityQueue<String> pq = new PriorityQueue<>(5);
        pq.enqueue("Test", 2);
        assertEquals("Test", pq.peek());
        assertEquals(1, pq.size(), "Peek should not remove the item");
    }

    @Test
    public void testDynamicResize() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(2);
        pq.enqueue(1, 10);
        pq.enqueue(2, 20);
        pq.enqueue(3, 5); // Should trigger resize in Heap
        assertEquals(3, pq.size());
        assertEquals(3, pq.dequeue());
    }

    @Test
    public void testEmptyDequeue() {
        PriorityQueue<Integer> pq = new PriorityQueue<>(2);
        assertNull(pq.dequeue(), "Should return null when empty as per Heap implementation");
    }
}