package com.campushub.structures.linear;

// Owner: Linear Structures
public class DynamicArray<T> {

    private Object[] data;
    private int size;
    private static final int DEFAULT_CAPACITY = 8;

    // Performance metrics
    private long elementWrites;
    private long resizeCount;
    private long operationCount;

    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("initialCapacity must be >= 1");
        }
        this.data = new Object[initialCapacity];
        this.size = 0;
        this.elementWrites = 0;
        this.resizeCount = 0;
        this.operationCount = 0;
    }

    public int size() {
        return size;
    }

    public int capacity() {
        return data.length;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
            elementWrites++;
        }
        size = 0;
        operationCount++;
    }

    private void checkIndexForAccess(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
    }

    private void checkIndexForInsert(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for insertion at size " + size);
        }
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndexForAccess(index);
        operationCount++;
        return (T) data[index];
    }

    public void set(int index, T value) {
        checkIndexForAccess(index);
        data[index] = value;
        elementWrites++;
        operationCount++;
    }

    public void add(T value) {
        insert(size, value);
    }

    public void insert(int index, T value) {
        checkIndexForInsert(index);
        ensureCapacity(size + 1);
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            elementWrites++;
        }
        data[index] = value;
        elementWrites++;
        size++;
        operationCount++;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkIndexForAccess(index);
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            elementWrites++;
        }
        data[size - 1] = null;
        size--;
        operationCount++;
        return removed;
    }

    public boolean removeValue(T value) {
        int index = indexOf(value);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    public int indexOf(T value) {
        operationCount++;
        for (int i = 0; i < size; i++) {
            if (equalsValue(data[i], value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) != -1;
    }

    private boolean equalsValue(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }
        int newCapacity = data.length;
        while (newCapacity < minCapacity) {
            newCapacity = newCapacity * 2;
        }
        resize(newCapacity);
    }

    public void resize(int newCapacity) {
        if (newCapacity < size) {
            throw new IllegalArgumentException("Target capacity " + newCapacity + " cannot be less than current size " + size);
        }
        Object[] newData = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            elementWrites++;
        }
        data = newData;
        resizeCount++;
    }

    public void trimToSize() {
        if (data.length > size) {
            resize(Math.max(size, 1));
        }
    }

    public long getElementWrites() {
        return elementWrites;
    }

    public long getResizeCount() {
        return resizeCount;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        elementWrites = 0;
        resizeCount = 0;
        operationCount = 0;
    }

    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] copy = new Object[size];
        for (int i = 0; i < size; i++) {
            copy[i] = data[i];
        }
        return (T[]) copy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }
}
