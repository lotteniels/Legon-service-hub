package com.campushub.structures.priority;

public class PriorityQueueTest {
    public static void main(String[] args) {
        PriorityQueue<String> pq = new PriorityQueue<>(10);

        System.out.println("=== Testing Priority Queue ===\n");

        System.out.println("Adding items with priorities: (Task A, 5), (Task B, 2), (Task C, 8), (Task D, 1), (Task E, 3)");
        pq.enqueue("Task A", 5);
        pq.enqueue("Task B", 2);
        pq.enqueue("Task C", 8);
        pq.enqueue("Task D", 1);
        pq.enqueue("Task E", 3);

        System.out.println("\nSize: " + pq.size() + "\n");

        System.out.println("Removing items in priority order (smallest first):");
        while (!pq.isEmpty()) {
            String value = pq.dequeue();
            System.out.println("  Removed: " + value);
        }

        System.out.println("\n=== Test Complete ===");
    }
}