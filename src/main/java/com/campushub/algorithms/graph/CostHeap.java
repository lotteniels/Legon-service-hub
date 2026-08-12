package com.campushub.algorithms.graph;

// Owner: Graphs and Optimization

/**
 * Indexed binary min-heap keyed by a {@code double} cost, used by {@link Dijkstra} and
 * {@link Prim}.
 *
 * <p>The Priority Structures pod's {@code Heap}/{@code PriorityQueue} take an
 * {@code int} priority. Graph costs are real-valued, and no single scale factor
 * converts them safely for every weight mode: distances sum to roughly 129 000 metres
 * across the network, while condition-adjusted times need three or more decimal places.
 * Rounding to an int would let two nearly-equal tentative distances swap order, and
 * Dijkstra settles a location permanently the first time it is removed, so that
 * swap becomes a wrong answer rather than a rounding blemish. This heap therefore keys
 * on the double directly. It is written from scratch, not taken from
 * {@code java.util}.
 *
 * <p>Elements are dense slots from {@code 0} to {@code capacity-1}, matching
 * {@link com.campushub.structures.graph.Graph#slotOf}. Tracking each slot's position
 * gives {@link #decreaseCost} in logarithmic time, so no stale duplicate entries build
 * up and the settle order matches the textbook algorithm exactly.
 */
public class CostHeap {

    private final int[] heap;
    private final int[] positionOf;
    private final double[] cost;
    private int size;

    private int comparisons;
    private int swaps;

    /**
     * A heap able to hold slots {@code 0..capacity-1}.
     *
     * @throws IllegalArgumentException if {@code capacity} is negative
     */
    public CostHeap(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
        heap = new int[capacity];
        positionOf = new int[capacity];
        cost = new double[capacity];
        for (int slot = 0; slot < capacity; slot++) {
            positionOf[slot] = -1;
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** True if {@code slot} is currently in the heap. */
    public boolean contains(int slot) {
        return positionOf[slot] >= 0;
    }

    /** The cost recorded for {@code slot}. Meaningful only while it is in the heap. */
    public double costOf(int slot) {
        return cost[slot];
    }

    /**
     * Adds {@code slot} at {@code newCost}.
     *
     * @throws IllegalStateException if it is already present
     */
    public void insert(int slot, double newCost) {
        if (contains(slot)) {
            throw new IllegalStateException("slot " + slot + " is already in the heap");
        }
        heap[size] = slot;
        positionOf[slot] = size;
        cost[slot] = newCost;
        size++;
        siftUp(size - 1);
    }

    /**
     * Lowers the cost of {@code slot}, or inserts it if absent. Raising a cost is
     * rejected: Dijkstra and Prim only ever improve an estimate, so an increase means a
     * caller bug.
     *
     * @throws IllegalArgumentException if {@code newCost} is higher than the current one
     */
    public void decreaseCost(int slot, double newCost) {
        if (!contains(slot)) {
            insert(slot, newCost);
            return;
        }
        if (newCost > cost[slot]) {
            throw new IllegalArgumentException("cost of slot " + slot + " would rise from "
                    + cost[slot] + " to " + newCost);
        }
        cost[slot] = newCost;
        siftUp(positionOf[slot]);
    }

    /** The slot with the lowest cost, without removing it, or -1 if empty. */
    public int peek() {
        return size == 0 ? -1 : heap[0];
    }

    /** Removes and returns the lowest-cost slot, or -1 if empty. */
    public int removeMin() {
        if (size == 0) {
            return -1;
        }
        int smallest = heap[0];
        positionOf[smallest] = -1;
        size--;
        if (size > 0) {
            heap[0] = heap[size];
            positionOf[heap[0]] = 0;
            siftDown(0);
        }
        return smallest;
    }

    private void siftUp(int position) {
        while (position > 0) {
            int parent = (position - 1) / 2;
            comparisons++;
            if (cost[heap[position]] >= cost[heap[parent]]) {
                break;
            }
            swap(position, parent);
            position = parent;
        }
    }

    private void siftDown(int position) {
        while (true) {
            int left = position * 2 + 1;
            int right = left + 1;
            int smallest = position;

            if (left < size) {
                comparisons++;
                if (cost[heap[left]] < cost[heap[smallest]]) {
                    smallest = left;
                }
            }
            if (right < size) {
                comparisons++;
                if (cost[heap[right]] < cost[heap[smallest]]) {
                    smallest = right;
                }
            }
            if (smallest == position) {
                return;
            }
            swap(position, smallest);
            position = smallest;
        }
    }

    private void swap(int left, int right) {
        int temporary = heap[left];
        heap[left] = heap[right];
        heap[right] = temporary;
        positionOf[heap[left]] = left;
        positionOf[heap[right]] = right;
        swaps++;
    }

    /** Cost comparisons performed, for the efficiency study. */
    public int comparisons() {
        return comparisons;
    }

    /** Element swaps performed. */
    public int swaps() {
        return swaps;
    }

    @Override
    public String toString() {
        return "CostHeap[" + size + " of " + heap.length + "]";
    }
}
