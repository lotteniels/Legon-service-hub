package com.campushub.structures.priority;

public class PriorityQueueTest {
    public static void main(String[] args) {
        PriorityQueue pq = new PriorityQueue(10);

        System.out.println("=== Testing Priority Queue ===\n");

        System.out.println("Adding items: 5, 2, 8, 1, 3");
        pq.enqueue(5);
        pq.enqueue(2);
        pq.enqueue(8);
        pq.enqueue(1);
        pq.enqueue(3);

        System.out.println("Size: " + pq.size() + "\n");

        System.out.println("Removing items in priority order (smallest first):");
        while (!pq.isEmpty()) {
            int value = pq.dequeue();
            System.out.println("  Removed: " + value);
        }

        System.out.println("\n=== Test Complete ===");
    }
}