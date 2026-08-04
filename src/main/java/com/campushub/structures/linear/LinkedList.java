package com.campushub.structures.linear;

// Owner: Linear Structures
public class LinkedList<T> {

    public static class Node<T> {
        private T value;
        private Node<T> prev;
        private Node<T> next;

        public Node(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public Node<T> getNext() {
            return next;
        }

        public Node<T> getPrev() {
            return prev;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Performance metrics
    private long traversalSteps;
    private long nodeAllocations;
    private long operationCount;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
        this.traversalSteps = 0;
        this.nodeAllocations = 0;
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

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public void addFirst(T value) {
        Node<T> node = new Node<>(value);
        nodeAllocations++;
        if (head == null) {
            head = tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
        operationCount++;
    }

    public void addLast(T value) {
        Node<T> node = new Node<>(value);
        nodeAllocations++;
        if (tail == null) {
            head = tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        size++;
        operationCount++;
    }

    public void insertAfter(Node<T> node, T value) {
        if (node == null) {
            throw new IllegalArgumentException("Target node cannot be null");
        }
        Node<T> newNode = new Node<>(value);
        nodeAllocations++;
        newNode.prev = node;
        newNode.next = node.next;
        if (node.next != null) {
            node.next.prev = newNode;
        } else {
            tail = newNode;
        }
        node.next = newNode;
        size++;
        operationCount++;
    }

    public void insertAfter(int index, T value) {
        Node<T> node = nodeAt(index);
        insertAfter(node, value);
    }

    private Node<T> nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
        }
        Node<T> current;
        if (index <= size / 2) {
            current = head;
            for (int i = 0; i < index; i++) {
                current = current.next;
                traversalSteps++;
            }
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
                traversalSteps++;
            }
        }
        return current;
    }

    public T get(int index) {
        operationCount++;
        return nodeAt(index).value;
    }

    public void set(int index, T value) {
        Node<T> node = nodeAt(index);
        node.value = value;
        operationCount++;
    }

    public T removeFirst() {
        if (head == null) {
            throw new IllegalStateException("List is empty");
        }
        T value = head.value;
        head = head.next;
        if (head != null) {
            head.prev = null;
        } else {
            tail = null;
        }
        size--;
        operationCount++;
        return value;
    }

    public T removeLast() {
        if (tail == null) {
            throw new IllegalStateException("List is empty");
        }
        T value = tail.value;
        tail = tail.prev;
        if (tail != null) {
            tail.next = null;
        } else {
            head = null;
        }
        size--;
        operationCount++;
        return value;
    }

    public T removeAt(int index) {
        Node<T> node = nodeAt(index);
        T value = node.value;
        removeNode(node);
        return value;
    }

    public boolean remove(T value) {
        Node<T> current = head;
        while (current != null) {
            traversalSteps++;
            if (equalsValue(current.value, value)) {
                removeNode(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    private void removeNode(Node<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        size--;
        operationCount++;
    }

    public int indexOf(T value) {
        int index = 0;
        Node<T> current = head;
        while (current != null) {
            traversalSteps++;
            if (equalsValue(current.value, value)) {
                return index;
            }
            current = current.next;
            index++;
        }
        return -1;
    }

    public boolean contains(T value) {
        return indexOf(value) != -1;
    }

    private boolean equalsValue(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    public long getTraversalSteps() {
        return traversalSteps;
    }

    public long getNodeAllocations() {
        return nodeAllocations;
    }

    public long getOperationCount() {
        return operationCount;
    }

    public void resetOpCounters() {
        traversalSteps = 0;
        nodeAllocations = 0;
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
        StringBuilder sb = new StringBuilder("[");
        Node<T> current = head;
        while (current != null) {
            sb.append(current.value);
            if (current.next != null) {
                sb.append(", ");
            }
            current = current.next;
        }
        return sb.append("]").toString();
    }


    public LinkedListIterator getIterator() {
        return new LinkedListIterator();
    }

    public class LinkedListIterator {
        private Node<T> current = head;

        public boolean hasNext() {
            return current != null;
        }

        public T next() {
            if (!hasNext()) {
                throw new IllegalStateException("No more elements in the list");
            }
            T value = current.value;
            current = current.next;
            traversalSteps++;
            return value;
        }
    }
}
