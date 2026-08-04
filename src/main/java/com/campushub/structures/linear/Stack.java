package com.campushub.structures.linear;

// Owner: Linear Structures
public class Stack<T> {

    private static final int DEFAULT_CAPACITY = 8;

    private Object[] data;
    private int top;

    // Performance metrics
    private long pushCount;
    private long popCount;
    private long resizeCount;
    private long operationCount;

    public Stack() {
        this(DEFAULT_CAPACITY);
    }

    public Stack(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        this.data = new Object[initialCapacity];
        this.top = 0;
        this.pushCount = 0;
        this.popCount = 0;
        this.resizeCount = 0;
        this.operationCount = 0;
    }

    public int size() {
        return top;
    }

    public int capacity() {
        return data.length;
    }

    public boolean isEmpty() {
        return top == 0;
    }

    public void clear() {
        for (int i = 0; i < top; i++) {
            data[i] = null;
        }
        top = 0;
        operationCount++;
    }

    public void push(T value) {
        if (top == data.length) {
            grow();
        }
        data[top++] = value;
        pushCount++;
        operationCount++;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        T value = (T) data[--top];
        data[top] = null;
        popCount++;
        operationCount++;
        return value;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        operationCount++;
        return (T) data[top - 1];
    }

    private void grow() {
        Object[] newData = new Object[data.length * 2];
        for (int i = 0; i < top; i++) {
            newData[i] = data[i];
        }
        data = newData;
        resizeCount++;
    }

    public long getPushCount() {
        return pushCount;
    }

    public long getPopCount() {
        return popCount;
    }

    public long getResizeCount() {
        return resizeCount;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        pushCount = 0;
        popCount = 0;
        resizeCount = 0;
        operationCount = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] copy = new Object[top];
        for (int i = 0; i < top; i++) {
            copy[i] = data[i];
        }
        return (T[]) copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[top -> ");
        for (int i = top - 1; i >= 0; i--) {
            sb.append(data[i]);
            if (i > 0) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
