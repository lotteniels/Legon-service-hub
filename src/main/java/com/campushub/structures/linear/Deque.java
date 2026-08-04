package com.campushub.structures.linear;

// Owner: Linear Structures
public class Deque<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Performance metrics
    private long frontOps;
    private long rearOps;
    private long operationCount;

    public Deque() {
        this.head = null;
        this.tail = null;
        this.size = 0;
        this.frontOps = 0;
        this.rearOps = 0;
        this.operationCount = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
        operationCount++;
    }

    public void addFront(T value) {
        Node<T> node = new Node<>(value);
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
        frontOps++;
        operationCount++;
    }

    public void addRear(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        size++;
        rearOps++;
        operationCount++;
    }

    public T removeFront() {
        if (head == null) {
            throw new IllegalStateException("Deque is empty");
        }
        T value = head.value;
        head = head.next;
        if (head != null) {
            head.prev = null;
        } else {
            tail = null;
        }
        size--;
        frontOps++;
        operationCount++;
        return value;
    }

    public T removeRear() {
        if (tail == null) {
            throw new IllegalStateException("Deque is empty");
        }
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) {
            tail.next = null;
        } else {
            head = null;
        }
        size--;
        rearOps++;
        operationCount++;
        return value;
    }

    public T peekFront() {
        if (head == null) {
            throw new IllegalStateException("Deque is empty");
        }
        operationCount++;
        return head.value;
    }

    public T peekRear() {
        if (tail == null) {
            throw new IllegalStateException("Deque is empty");
        }
        operationCount++;
        return tail.value;
    }

    public long getFrontOps() {
        return frontOps;
    }

    public long getRearOps() {
        return rearOps;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        frontOps = 0;
        rearOps = 0;
        operationCount = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] copy = new Object[size];
        Node<T> current = head;
        int i = 0;
        while (current != null) {
            copy[i++] = current.value;
            current = current.next;
        }
        return (T[]) copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("front[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        return sb.append("]rear").toString();
    }
}
