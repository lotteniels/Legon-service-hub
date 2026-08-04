package com.campushub.structures.linear;

// Owner: Linear Structures
public class CircularQueue<T> {

    private static final int DEFAULT_CAPACITY = 8;

    private Object[] data;
    private int front;
    private int rear;
    private int count;
    private final boolean fixedCapacity;

    // Performance metrics
    private long wrapCount;
    private long resizeCount;
    private long operationCount;

    public CircularQueue() {
        this(DEFAULT_CAPACITY, false);
    }

    public CircularQueue(int initialCapacity) {
        this(initialCapacity, false);
    }

    public CircularQueue(int initialCapacity, boolean fixedCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        this.data = new Object[initialCapacity];
        this.front = 0;
        this.rear = -1;
        this.count = 0;
        this.fixedCapacity = fixedCapacity;
        this.wrapCount = 0;
        this.resizeCount = 0;
        this.operationCount = 0;
    }

    public int size() {
        return count;
    }

    public int capacity() {
        return data.length;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public boolean isFull() {
        return count == data.length;
    }

    public void clear() {
        data = new Object[data.length];
        front = 0;
        rear = -1;
        count = 0;
        operationCount++;
    }

    public void enqueue(T value) {
        if (isFull()) {
            if (fixedCapacity) {
                throw new IllegalStateException("Circular queue is full");
            }
            grow();
        }
        int nextRear = (rear + 1) % data.length;
        if (nextRear == 0 && rear != -1) {
            wrapCount++;
        }
        rear = nextRear;
        data[rear] = value;
        count++;
        operationCount++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Circular queue is empty");
        }
        T value = (T) data[front];
        data[front] = null;
        int nextFront = (front + 1) % data.length;
        if (nextFront == 0 && front != 0) {
            wrapCount++;
        }
        front = nextFront;
        count--;
        operationCount++;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Circular queue is empty");
        }
        operationCount++;
        return (T) data[front];
    }

    private void grow() {
        int newCapacity = data.length * 2;
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < count; i++) {
            newData[i] = data[(front + i) % data.length];
        }
        data = newData;
        front = 0;
        rear = count - 1;
        resizeCount++;
    }

    public int getFrontIndex() {
        return front;
    }

    public int getRearIndex() {
        return rear;
    }

    public long getWrapCount() {
        return wrapCount;
    }

    public long getResizeCount() {
        return resizeCount;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        wrapCount = 0;
        resizeCount = 0;
        operationCount = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] copy = new Object[count];
        for (int i = 0; i < count; i++) {
            copy[i] = data[(front + i) % data.length];
        }
        return (T[]) copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("front[" + front + "] -> [");
        for (int i = 0; i < count; i++) {
            sb.append(data[(front + i) % data.length]);
            if (i < count - 1) {
                sb.append(", ");
            }
        }
        return sb.append("] <- rear[" + rear + "]").toString();
    }
}
