package com.campushub.structures.priority;


public class Heap<T> {
    private static class Node<T> {
        T item;
        int priority;

        Node(T item, int priority) {
            this.item = item;
            this.priority = priority;
        }
    }

    private Node<T>[] heap;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public Heap(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        this.capacity = capacity;
        this.heap = (Node<T>[]) new Node[capacity];
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public void insert(T item, int priority) {
        // Dynamically double the capacity if the heap is full
        if (size >= capacity) {
            capacity = capacity * 2;
            Node<T>[] newHeap = (Node<T>[]) new Node[capacity];
            for (int i = 0; i < size; i++) {
                newHeap[i] = heap[i];
            }
            heap = newHeap;
        }
        
        heap[size] = new Node<>(item, priority);
        size++;
        heapifyUp(size - 1);
    }


    public T removeMin() {
        if (size == 0) {
            System.out.println("Heap is empty!");
            return null;
        }
        T minItem = heap[0].item;
        heap[0] = heap[size - 1];
        heap[size - 1] = null; // Prevent memory leak (allow Garbage Collection)
        size--;
        heapifyDown(0);
        return minItem;
    }


    public T peek() {
        if (size == 0) {
            return null;
        }
        return heap[0].item;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void heapifyUp(int index) {
        int parentIndex = (index - 1) / 2;
        if (index > 0 && heap[index].priority < heap[parentIndex].priority) {
            Node<T> temp = heap[index];
            heap[index] = heap[parentIndex];
            heap[parentIndex] = temp;
            heapifyUp(parentIndex);
        }
    }

    private void heapifyDown(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int smallest = index;

        if (leftChild < size && heap[leftChild].priority < heap[smallest].priority) {
            smallest = leftChild;
        }
        if (rightChild < size && heap[rightChild].priority < heap[smallest].priority) {
            smallest = rightChild;
        }
        if (smallest != index) {
            Node<T> temp = heap[index];
            heap[index] = heap[smallest];
            heap[smallest] = temp;
            heapifyDown(smallest);
        }
    }
}
