package com.campushub.structures.linear;

// Owner: Linear Structures
public class Queue<T> {

    private static class Node<T> {
        T value;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> front;
    private Node<T> rear;
    private int size;

    // Performance metrics
    private long enqueueCount;
    private long dequeueCount;
    private long pointerMoves;
    private long operationCount;

    public Queue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
        this.enqueueCount = 0;
        this.dequeueCount = 0;
        this.pointerMoves = 0;
        this.operationCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        front = null;
        rear = null;
        size = 0;
        operationCount++;
    }

    public void enqueue(T value) {
        Node<T> node = new Node<>(value);
        if (rear == null) {
            front = rear = node;
        } else {
            rear.next = node;
            rear = node;
        }
        pointerMoves++;
        enqueueCount++;
        size++;
        operationCount++;
    }

    public T dequeue() {
        if (front == null) {
            throw new IllegalStateException("Queue is empty");
        }
        T value = front.value;
        front = front.next;
        if (front == null) {
            rear = null;
        }
        pointerMoves++;
        dequeueCount++;
        size--;
        operationCount++;
        return value;
    }

    public T peek() {
        if (front == null) {
            throw new IllegalStateException("Queue is empty");
        }
        operationCount++;
        return front.value;
    }

    public long getEnqueueCount() {
        return enqueueCount;
    }

    public long getDequeueCount() {
        return dequeueCount;
    }

    public long getPointerMoves() {
        return pointerMoves;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        enqueueCount = 0;
        dequeueCount = 0;
        pointerMoves = 0;
        operationCount = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] copy = new Object[size];
        Node<T> current = front;
        int i = 0;
        while (current != null) {
            copy[i++] = current.value;
            current = current.next;
        }
        return (T[]) copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("front -> [");
        Node<T> current = front;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        return sb.append("] <- rear").toString();
    }
}