package com.campushub.structures.priority;

public class Heap {
    private int[] heap;
    private int size;
    private int capacity;

    // Constructor
    public Heap(int capacity) {
        this.capacity = capacity;
        this.heap = new int[capacity];
        this.size = 0;
    }

    // Add a value to the heap
    public void insert(int value) {
        if (size >= capacity) {
            System.out.println("Heap is full!");
            return;
        }
        heap[size] = value;
        size++;
        heapifyUp(size - 1);
    }

    // Remove and return the smallest value
    public int removeMin() {
        if (size == 0) {
            System.out.println("Heap is empty!");
            return -1;
        }
        int min = heap[0];
        heap[0] = heap[size - 1];
        size--;
        heapifyDown(0);
        return min;
    }

    // Look at the smallest value without removing it
    public int peek() {
        if (size == 0) {
            return -1;
        }
        return heap[0];
    }

    // Return the number of items in the heap
    public int size() {
        return size;
    }

    // Check if the heap is empty
    public boolean isEmpty() {
        return size == 0;
    }

    // Helper: move a value up to its correct position
    private void heapifyUp(int index) {
        int parentIndex = (index - 1) / 2;
        if (index > 0 && heap[index] < heap[parentIndex]) {
            // Swap
            int temp = heap[index];
            heap[index] = heap[parentIndex];
            heap[parentIndex] = temp;
            heapifyUp(parentIndex);
        }
    }

    // Helper: move a value down to its correct position
    private void heapifyDown(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int smallest = index;

        if (leftChild < size && heap[leftChild] < heap[smallest]) {
            smallest = leftChild;
        }
        if (rightChild < size && heap[rightChild] < heap[smallest]) {
            smallest = rightChild;
        }
        if (smallest != index) {
            // Swap
            int temp = heap[index];
            heap[index] = heap[smallest];
            heap[smallest] = temp;
            heapifyDown(smallest);
        }
    }
}