package com.campushub.structures.priority;

public class PriorityQueue<T> {
    private Heap<T> heap;

    // Constructor
    public PriorityQueue(int capacity) {
        this.heap = new Heap<>(capacity);
    }

    // Add an item with a priority value
    public void enqueue(T item, int priority) {
        heap.insert(item, priority);
    }

    // Remove and return the highest priority item (smallest value)
    public T dequeue() {
        return heap.removeMin();
    }

    // Look at the highest priority item without removing it
    public T peek() {
        return heap.peek();
    }

    // Check if the queue is empty
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    // Return the number of items in the queue
    public int size() {
        return heap.size();
    }
}