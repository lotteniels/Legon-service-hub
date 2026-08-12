package com.campushub.structures.graph;

// Owner: Graphs and Optimization

/**
 * Union-find over a dense range of elements, the structure Kruskal uses to decide
 * whether an edge would close a cycle.
 *
 * <p>Elements are {@code 0} to {@code elementCount-1}. Callers holding sparse keys map
 * them first - Kruskal passes {@link Graph#slotOf} values, which is why the graph
 * exposes slots at all.
 *
 * <p>Both standard optimisations are applied. Union by rank attaches the shallower
 * tree beneath the deeper one, and path compression flattens a branch every time it is
 * walked, giving an amortised cost per operation that is effectively constant.
 *
 * <p>Backed by two {@code int[]} arrays, so no {@code java.util} collection is
 * involved.
 */
public class DisjointSet {

    private final int[] parent;
    private final int[] rank;
    private int setCount;

    private int findCalls;
    private int pointerHops;
    private int unionsPerformed;

    /**
     * A structure of {@code elementCount} single-member sets.
     *
     * @throws IllegalArgumentException if {@code elementCount} is negative
     */
    public DisjointSet(int elementCount) {
        if (elementCount < 0) {
            throw new IllegalArgumentException("elementCount must not be negative");
        }
        parent = new int[elementCount];
        rank = new int[elementCount];
        for (int element = 0; element < elementCount; element++) {
            parent[element] = element;
        }
        setCount = elementCount;
    }

    /**
     * The representative of {@code element}'s set, compressing the path walked so
     * later lookups are shorter.
     *
     * @throws IndexOutOfBoundsException if {@code element} is out of range
     */
    public int find(int element) {
        checkRange(element);
        findCalls++;

        int root = element;
        while (parent[root] != root) {
            root = parent[root];
            pointerHops++;
        }

        int current = element;
        while (current != root) {
            int next = parent[current];
            parent[current] = root;
            current = next;
        }
        return root;
    }

    /**
     * Merges the sets holding {@code a} and {@code b}.
     *
     * @return true if they were separate, false if already joined - which is exactly
     *         the signal Kruskal needs to reject a cycle-closing edge
     */
    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);
        if (rootA == rootB) {
            return false;
        }

        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }

        setCount--;
        unionsPerformed++;
        return true;
    }

    /** True if both elements are in the same set. */
    public boolean connected(int a, int b) {
        return find(a) == find(b);
    }

    /** Number of disjoint sets. */
    public int setCount() {
        return setCount;
    }

    /** Number of elements the structure covers. */
    public int elementCount() {
        return parent.length;
    }

    /** Size of the set holding {@code element}. */
    public int sizeOfSet(int element) {
        int root = find(element);
        int members = 0;
        for (int candidate = 0; candidate < parent.length; candidate++) {
            if (find(candidate) == root) {
                members++;
            }
        }
        return members;
    }

    /**
     * The sets as an array of member arrays, each ascending and ordered by its
     * smallest element, so output is reproducible.
     */
    public int[][] sets() {
        int[] rootOf = new int[parent.length];
        int[] groupOfRoot = new int[parent.length];
        int[] sizes = new int[parent.length];
        for (int element = 0; element < parent.length; element++) {
            groupOfRoot[element] = -1;
        }

        // Scanning ascending means the first element seen for a root is its smallest,
        // so groups come out ordered by smallest member and members ascending within.
        int groups = 0;
        for (int element = 0; element < parent.length; element++) {
            int root = find(element);
            rootOf[element] = root;
            if (groupOfRoot[root] < 0) {
                groupOfRoot[root] = groups;
                groups++;
            }
            sizes[groupOfRoot[root]]++;
        }

        int[][] grouped = new int[groups][];
        for (int group = 0; group < groups; group++) {
            grouped[group] = new int[sizes[group]];
        }

        int[] filled = new int[groups];
        for (int element = 0; element < parent.length; element++) {
            int group = groupOfRoot[rootOf[element]];
            grouped[group][filled[group]++] = element;
        }
        return grouped;
    }

    /** How many times {@link #find} was called. */
    public int findCalls() {
        return findCalls;
    }

    /** Parent pointers followed across all finds - the cost path compression cuts. */
    public int pointerHops() {
        return pointerHops;
    }

    /** How many unions actually merged two distinct sets. */
    public int unionsPerformed() {
        return unionsPerformed;
    }

    private void checkRange(int element) {
        if (element < 0 || element >= parent.length) {
            throw new IndexOutOfBoundsException(
                    "element " + element + " outside 0.." + (parent.length - 1));
        }
    }

    @Override
    public String toString() {
        return "DisjointSet[" + elementCount() + " elements in " + setCount + " sets]";
    }
}
