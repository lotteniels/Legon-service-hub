package com.campushub.structures.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * A B-Tree that maps keys to values (used by IndexingEngine / M6).
 *
 * <p>THE ONE-SENTENCE IDEA: a B-Tree is a search tree where every node is a "box"
 * that holds several keys at once instead of just one. When a box gets too full we
 * split it in half and push the middle key up to the parent. Because growth happens
 * upwards from the middle, every leaf always sits at exactly the same depth, so the
 * tree can never go lopsided the way a plain BST can.
 *
 * <p>SIZE RULES (t = minimum degree, set in the constructor):
 * <ul>
 *   <li>Every node holds at most 2t - 1 keys. That is when it is "full".</li>
 *   <li>Every node except the root holds at least t - 1 keys.</li>
 *   <li>An internal node with n keys has exactly n + 1 children.</li>
 *   <li>All leaves are at the same depth.</li>
 * </ul>
 * With the default t = 3: max 5 keys per node, min 2 keys per node, up to 6 children.
 *
 * <p>COST: search, insert and delete are all O(log n) comparisons, and the height is
 * only about log_t(n) - far shorter than a BST, which is why real databases index
 * with B-Trees.
 *
 * @param <K> key type, must be comparable (e.g. Integer request id, String call number)
 * @param <V> value type (e.g. ServiceRequest, Location)
 */
public class BTree<K extends Comparable<K>, V> {

    /** Default minimum degree: max 5 keys per node. */
    public static final int DEFAULT_MIN_DEGREE = 3;

    /**
     * One "box" in the tree.
     *
     * <p>Kept as a plain static class with Object[] storage so that we never have to
     * create a generic array (Java does not allow {@code new K[10]}). The typed
     * helpers key(), value() and child() below do the casting in one place.
     */
    private static final class Node {
        /** How many keys are actually in use right now (0 .. 2t-1). */
        int n;
        /** True if this node has no children. */
        boolean leaf;
        final Object[] keys;
        final Object[] values;
        final Node[] children;

        Node(int minDegree, boolean leaf) {
            this.leaf = leaf;
            this.n = 0;
            this.keys = new Object[2 * minDegree - 1];
            this.values = new Object[2 * minDegree - 1];
            this.children = new Node[2 * minDegree];
        }
    }

    private final int t;
    private Node root;
    private int size;

    /** Creates a B-Tree with the default minimum degree (max 5 keys per node). */
    public BTree() {
        this(DEFAULT_MIN_DEGREE);
    }

    /**
     * Derives the minimum degree from team member index numbers.
     *
     * <p>The brief (section 2, AI-resistance) requires at least three algorithm parameters
     * to be derived from member index numbers. This is the tree pod's contribution: the
     * B-Tree's node capacity is not a number somebody picked, it is a function of who is on
     * the team, so no two teams get the same tree shape and the trace tables in the report
     * cannot match anyone else's.
     *
     * <p>Formula: {@code t = 2 + (sum of index numbers mod 4)}, giving t in the range 2..5,
     * so a node holds between 3 and 9 keys. The modulus is 4 rather than something larger
     * because t below 2 is not a legal B-Tree and t above 5 makes hand-drawn trace tables
     * unreadable in the report.
     *
     * <p>Be ready to compute this by hand at the oral defence, and to say what happens to
     * the height if the team changes: larger t means more keys per node, so a shorter tree
     * but more comparisons inside each node.
     *
     * @param indexNumbers the numeric part of each member's student index number
     */
    public static int minDegreeFromIndexNumbers(int... indexNumbers) {
        if (indexNumbers == null || indexNumbers.length == 0) {
            throw new IllegalArgumentException("at least one index number is required");
        }
        long sum = 0;
        for (int indexNumber : indexNumbers) {
            sum += Math.abs((long) indexNumber);
        }
        return 2 + (int) (sum % 4);
    }

    /** Builds a tree whose node capacity is derived from the team's index numbers. */
    public static <K extends Comparable<K>, V> BTree<K, V> fromIndexNumbers(int... indexNumbers) {
        return new BTree<K, V>(minDegreeFromIndexNumbers(indexNumbers));
    }

    /**
     * @param minDegree the t value; must be at least 2 (t = 2 gives max 3 keys per node,
     *                  which is the easiest size to demo a split by hand)
     */
    public BTree(int minDegree) {
        if (minDegree < 2) {
            throw new IllegalArgumentException("minimum degree must be >= 2, got " + minDegree);
        }
        this.t = minDegree;
        this.root = new Node(minDegree, true);
        this.size = 0;
    }

    // ---------------------------------------------------------------- casting helpers

    @SuppressWarnings("unchecked")
    private K key(Node x, int i) {
        return (K) x.keys[i];
    }

    @SuppressWarnings("unchecked")
    private V value(Node x, int i) {
        return (V) x.values[i];
    }

    private Node child(Node x, int i) {
        return x.children[i];
    }

    private int compare(K a, Node x, int i) {
        return a.compareTo(key(x, i));
    }

    // ---------------------------------------------------------------- simple queries

    /** Number of keys stored in the whole tree. */
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** The t value this tree was built with. */
    public int minDegree() {
        return t;
    }

    /** Maximum keys one node can hold before it must split (2t - 1). */
    public int maxKeysPerNode() {
        return 2 * t - 1;
    }

    /** Height in edges: a tree with only a root leaf has height 0. */
    public int height() {
        int h = 0;
        Node x = root;
        while (!x.leaf) {
            x = child(x, 0);
            h++;
        }
        return h;
    }

    // ---------------------------------------------------------------- SEARCH

    /**
     * Looks a key up.
     *
     * <p>Inside a node we walk left to right until we find a key that is not smaller
     * than what we want. If it is equal we are done. If not, we drop into the child
     * sitting just before it - that child holds every key between the two keys it sits
     * between. Repeat until we match or fall off a leaf.
     *
     * @return the stored value, or null if the key is absent
     */
    public V get(K searchKey) {
        requireKey(searchKey);
        Node x = root;
        while (true) {
            int i = 0;
            while (i < x.n && compare(searchKey, x, i) > 0) {
                i++;
            }
            if (i < x.n && compare(searchKey, x, i) == 0) {
                return value(x, i);   // found it inside this box
            }
            if (x.leaf) {
                return null;          // nowhere left to go
            }
            x = child(x, i);          // drop one level
        }
    }

    /** True if the key is in the tree (works even if the stored value is null). */
    public boolean contains(K searchKey) {
        return locate(searchKey) != null;
    }

    /** Finds the node holding a key, or null. Used by contains() and put(). */
    private Object[] locate(K searchKey) {
        requireKey(searchKey);
        Node x = root;
        while (true) {
            int i = 0;
            while (i < x.n && compare(searchKey, x, i) > 0) {
                i++;
            }
            if (i < x.n && compare(searchKey, x, i) == 0) {
                return new Object[] { x, i };
            }
            if (x.leaf) {
                return null;
            }
            x = child(x, i);
        }
    }

    /** Result of a traced search - handy for the evidence document and for the CLI. */
    public static final class SearchResult {
        public final boolean found;
        public final int nodesVisited;
        public final int comparisons;
        public final List<String> steps;

        SearchResult(boolean found, int nodesVisited, int comparisons, List<String> steps) {
            this.found = found;
            this.nodesVisited = nodesVisited;
            this.comparisons = comparisons;
            this.steps = steps;
        }

        @Override
        public String toString() {
            return (found ? "FOUND" : "NOT FOUND")
                    + " after visiting " + nodesVisited + " node(s) and "
                    + comparisons + " comparison(s)";
        }
    }

    /**
     * Same algorithm as {@link #get}, but it writes down every step it takes.
     * Run this to produce the step-by-step search trace the evidence folder asks for.
     */
    public SearchResult searchWithTrace(K searchKey) {
        requireKey(searchKey);
        List<String> steps = new ArrayList<>();
        int nodes = 0;
        int comparisons = 0;
        int level = 0;
        Node x = root;

        while (true) {
            nodes++;
            steps.add("Level " + level + ": arrive at node " + nodeKeysAsText(x));
            int i = 0;
            while (i < x.n && compare(searchKey, x, i) > 0) {
                comparisons++;
                steps.add("  compare " + searchKey + " > " + key(x, i) + "  -> keep moving right");
                i++;
            }
            if (i < x.n) {
                comparisons++;
                int c = compare(searchKey, x, i);
                if (c == 0) {
                    steps.add("  compare " + searchKey + " == " + key(x, i) + "  -> FOUND at slot " + i);
                    return new SearchResult(true, nodes, comparisons, steps);
                }
                steps.add("  compare " + searchKey + " < " + key(x, i) + "  -> stop at slot " + i);
            }
            if (x.leaf) {
                steps.add("  this node is a leaf, so there is no child to follow -> NOT FOUND");
                return new SearchResult(false, nodes, comparisons, steps);
            }
            steps.add("  follow child " + i + " (holds keys between the neighbours above)");
            x = child(x, i);
            level++;
        }
    }

    // ---------------------------------------------------------------- INSERT

    /**
     * Inserts a key, or overwrites the value if the key is already there.
     *
     * <p>Duplicate policy: a B-Tree used as an index must not hold the same key twice,
     * so a repeat insert updates the value in place and size() does not change.
     *
     * @return the previous value for that key, or null if the key is new
     */
    public V put(K newKey, V newValue) {
        requireKey(newKey);

        // 1. Already present? Just overwrite. This keeps the tree duplicate-free.
        Object[] hit = locate(newKey);
        if (hit != null) {
            Node holder = (Node) hit[0];
            int slot = (Integer) hit[1];
            V old = value(holder, slot);
            holder.values[slot] = newValue;
            return old;
        }

        // 2. If the root is full, split it FIRST. This is the only moment the tree
        //    gets taller, and it grows from the top, which is why it stays balanced.
        if (root.n == maxKeysPerNode()) {
            Node newRoot = new Node(t, false);
            newRoot.children[0] = root;
            splitChild(newRoot, 0);
            root = newRoot;
        }

        insertNonFull(root, newKey, newValue);
        size++;
        return null;
    }

    /** Convenience for sets/tests: store the key with itself as the value. */
    public void insert(K newKey) {
        @SuppressWarnings("unchecked")
        V asValue = (V) newKey;
        put(newKey, asValue);
    }

    /**
     * Walks down from a node that is guaranteed NOT full, splitting any full child
     * on the way. Because of that guarantee a split never cascades upwards.
     */
    private void insertNonFull(Node x, K newKey, V newValue) {
        int i = x.n - 1;

        if (x.leaf) {
            // Shuffle bigger keys one slot right, then drop the new key into the gap.
            while (i >= 0 && newKey.compareTo(key(x, i)) < 0) {
                x.keys[i + 1] = x.keys[i];
                x.values[i + 1] = x.values[i];
                i--;
            }
            x.keys[i + 1] = newKey;
            x.values[i + 1] = newValue;
            x.n++;
            return;
        }

        while (i >= 0 && newKey.compareTo(key(x, i)) < 0) {
            i--;
        }
        i++;                                  // the child we want to descend into

        if (child(x, i).n == maxKeysPerNode()) {
            splitChild(x, i);                 // pre-emptive split
            if (newKey.compareTo(key(x, i)) > 0) {
                i++;                          // the median moved up; go right of it
            }
        }
        insertNonFull(child(x, i), newKey, newValue);
    }

    /**
     * THE SPLIT - the heart of the B-Tree, and the thing you will be asked to explain.
     *
     * <p>parent.children[i] is full: it holds 2t-1 keys. We cut it into three pieces:
     * the smallest t-1 keys stay in the left node, the largest t-1 keys move to a brand
     * new right node, and the single middle key is promoted into the parent, sitting
     * between the two halves. Total keys are unchanged, but the over-full box is now
     * two comfortable boxes.
     */
    private void splitChild(Node parent, int i) {
        Node left = child(parent, i);
        Node right = new Node(t, left.leaf);

        int mid = t - 1;                      // index of the median key
        Object medianKey = left.keys[mid];
        Object medianValue = left.values[mid];

        // Top half of the keys moves into the new right node.
        for (int j = 0; j < t - 1; j++) {
            right.keys[j] = left.keys[j + t];
            right.values[j] = left.values[j + t];
            left.keys[j + t] = null;
            left.values[j + t] = null;
        }
        // If we are splitting an internal node, its children must follow their keys.
        if (!left.leaf) {
            for (int j = 0; j < t; j++) {
                right.children[j] = left.children[j + t];
                left.children[j + t] = null;
            }
        }
        right.n = t - 1;

        left.keys[mid] = null;
        left.values[mid] = null;
        left.n = t - 1;

        // Make room in the parent for one extra child pointer...
        for (int j = parent.n; j >= i + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[i + 1] = right;

        // ...and for the median key itself.
        for (int j = parent.n - 1; j >= i; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.values[j + 1] = parent.values[j];
        }
        parent.keys[i] = medianKey;
        parent.values[i] = medianValue;
        parent.n++;
    }

    // ---------------------------------------------------------------- DELETE

    /**
     * Removes a key if present.
     *
     * <p>Mirror image of insert: on the way down we make sure the child we are about to
     * enter has at least t keys, by borrowing a key from a sibling or merging with one.
     * That way a leaf never ends up under-full after the removal.
     *
     * @return true if something was actually removed
     */
    public boolean delete(K keyToRemove) {
        requireKey(keyToRemove);
        if (isEmpty()) {
            return false;
        }
        boolean removed = deleteFrom(root, keyToRemove);
        if (removed) {
            size--;
        }
        // The root may have been emptied by a merge; then the tree gets shorter.
        if (root.n == 0 && !root.leaf) {
            root = child(root, 0);
        }
        return removed;
    }

    private boolean deleteFrom(Node x, K target) {
        int idx = firstIndexAtLeast(x, target);

        if (idx < x.n && compare(target, x, idx) == 0) {
            if (x.leaf) {
                removeFromLeaf(x, idx);
            } else {
                removeFromInternal(x, idx);
            }
            return true;
        }

        if (x.leaf) {
            return false;                     // ran out of tree
        }

        boolean wasLastChild = (idx == x.n);
        if (child(x, idx).n < t) {
            refill(x, idx);                   // guarantee at least t keys before descending
        }
        if (wasLastChild && idx > x.n) {
            return deleteFrom(child(x, idx - 1), target);   // a merge shifted things left
        }
        return deleteFrom(child(x, idx), target);
    }

    /** First slot whose key is >= target (== x.n if every key is smaller). */
    private int firstIndexAtLeast(Node x, K target) {
        int i = 0;
        while (i < x.n && compare(target, x, i) > 0) {
            i++;
        }
        return i;
    }

    private void removeFromLeaf(Node x, int idx) {
        for (int j = idx + 1; j < x.n; j++) {
            x.keys[j - 1] = x.keys[j];
            x.values[j - 1] = x.values[j];
        }
        x.keys[x.n - 1] = null;
        x.values[x.n - 1] = null;
        x.n--;
    }

    private void removeFromInternal(Node x, int idx) {
        K target = key(x, idx);
        Node before = child(x, idx);
        Node after = child(x, idx + 1);

        if (before.n >= t) {
            // Replace with the largest key on the left (the in-order predecessor).
            Node p = before;
            while (!p.leaf) {
                p = child(p, p.n);
            }
            x.keys[idx] = p.keys[p.n - 1];
            x.values[idx] = p.values[p.n - 1];
            deleteFrom(before, key(x, idx));
        } else if (after.n >= t) {
            // Or the smallest key on the right (the in-order successor).
            Node s = after;
            while (!s.leaf) {
                s = child(s, 0);
            }
            x.keys[idx] = s.keys[0];
            x.values[idx] = s.values[0];
            deleteFrom(after, key(x, idx));
        } else {
            // Both neighbours are minimal: fuse them into one node and delete from there.
            merge(x, idx);
            deleteFrom(child(x, idx), target);
        }
    }

    /** Give children[idx] at least t keys, by borrowing or merging. */
    private void refill(Node x, int idx) {
        if (idx > 0 && child(x, idx - 1).n >= t) {
            borrowFromLeft(x, idx);
        } else if (idx < x.n && child(x, idx + 1).n >= t) {
            borrowFromRight(x, idx);
        } else if (idx < x.n) {
            merge(x, idx);
        } else {
            merge(x, idx - 1);
        }
    }

    private void borrowFromLeft(Node x, int idx) {
        Node target = child(x, idx);
        Node left = child(x, idx - 1);

        for (int j = target.n - 1; j >= 0; j--) {
            target.keys[j + 1] = target.keys[j];
            target.values[j + 1] = target.values[j];
        }
        if (!target.leaf) {
            for (int j = target.n; j >= 0; j--) {
                target.children[j + 1] = target.children[j];
            }
            target.children[0] = left.children[left.n];
            left.children[left.n] = null;
        }
        // Parent key drops down into the target, left sibling's last key moves up.
        target.keys[0] = x.keys[idx - 1];
        target.values[0] = x.values[idx - 1];
        x.keys[idx - 1] = left.keys[left.n - 1];
        x.values[idx - 1] = left.values[left.n - 1];
        left.keys[left.n - 1] = null;
        left.values[left.n - 1] = null;

        target.n++;
        left.n--;
    }

    private void borrowFromRight(Node x, int idx) {
        Node target = child(x, idx);
        Node right = child(x, idx + 1);

        target.keys[target.n] = x.keys[idx];
        target.values[target.n] = x.values[idx];
        if (!target.leaf) {
            target.children[target.n + 1] = right.children[0];
        }
        x.keys[idx] = right.keys[0];
        x.values[idx] = right.values[0];

        for (int j = 1; j < right.n; j++) {
            right.keys[j - 1] = right.keys[j];
            right.values[j - 1] = right.values[j];
        }
        right.keys[right.n - 1] = null;
        right.values[right.n - 1] = null;
        if (!right.leaf) {
            for (int j = 1; j <= right.n; j++) {
                right.children[j - 1] = right.children[j];
            }
            right.children[right.n] = null;
        }

        target.n++;
        right.n--;
    }

    /** Fuse children[idx], the parent key at idx, and children[idx+1] into one node. */
    private void merge(Node x, int idx) {
        Node into = child(x, idx);
        Node from = child(x, idx + 1);

        into.keys[t - 1] = x.keys[idx];          // parent key sinks into the middle
        into.values[t - 1] = x.values[idx];

        for (int j = 0; j < from.n; j++) {
            into.keys[j + t] = from.keys[j];
            into.values[j + t] = from.values[j];
        }
        if (!into.leaf) {
            for (int j = 0; j <= from.n; j++) {
                into.children[j + t] = from.children[j];
            }
        }
        into.n += from.n + 1;

        for (int j = idx + 1; j < x.n; j++) {
            x.keys[j - 1] = x.keys[j];
            x.values[j - 1] = x.values[j];
        }
        for (int j = idx + 2; j <= x.n; j++) {
            x.children[j - 1] = x.children[j];
        }
        x.keys[x.n - 1] = null;
        x.values[x.n - 1] = null;
        x.children[x.n] = null;
        x.n--;
    }

    // ---------------------------------------------------------------- TRAVERSAL / DEBUG

    /** All keys in sorted order. An in-order walk of a B-Tree is a sorted read of the index. */
    public List<K> keysInOrder() {
        List<K> out = new ArrayList<>(size);
        collect(root, out);
        return out;
    }

    private void collect(Node x, List<K> out) {
        for (int i = 0; i < x.n; i++) {
            if (!x.leaf) {
                collect(child(x, i), out);
            }
            out.add(key(x, i));
        }
        if (!x.leaf) {
            collect(child(x, x.n), out);
        }
    }

    /** Printable picture of the tree - use this in your demo and in the evidence doc. */
    public String structureAsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("B-Tree (t=").append(t)
          .append(", max ").append(maxKeysPerNode()).append(" keys/node, size=")
          .append(size).append(", height=").append(height()).append(")\n");
        draw(root, 0, sb);
        return sb.toString();
    }

    private void draw(Node x, int depth, StringBuilder sb) {
        sb.append("  ".repeat(depth)).append(nodeKeysAsText(x)).append('\n');
        if (!x.leaf) {
            for (int i = 0; i <= x.n; i++) {
                draw(child(x, i), depth + 1, sb);
            }
        }
    }

    private String nodeKeysAsText(Node x) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < x.n; i++) {
            if (i > 0) {
                sb.append(" | ");
            }
            sb.append(key(x, i));
        }
        return sb.append(']').toString();
    }

    /**
     * Checks every B-Tree rule and throws if one is broken. The tests call this after
     * every batch of operations, which is the cheapest possible proof that the splits
     * and merges are correct.
     */
    public void validate() {
        if (!root.leaf && root.n == 0) {
            throw new IllegalStateException("non-leaf root with no keys");
        }
        int leafDepth = check(root, 0, null, null, true);
        int counted = keysInOrder().size();
        if (counted != size) {
            throw new IllegalStateException("size says " + size + " but tree holds " + counted);
        }
        if (leafDepth < 0) {
            throw new IllegalStateException("leaf depth mismatch");
        }
    }

    /** Returns the depth at which leaves were found, verifying it is the same everywhere. */
    private int check(Node x, int depth, K lowerBound, K upperBound, boolean isRoot) {
        if (x.n > maxKeysPerNode()) {
            throw new IllegalStateException("node " + nodeKeysAsText(x) + " has too many keys");
        }
        if (!isRoot && x.n < t - 1) {
            throw new IllegalStateException("node " + nodeKeysAsText(x) + " is under-full");
        }
        for (int i = 1; i < x.n; i++) {
            if (key(x, i - 1).compareTo(key(x, i)) >= 0) {
                throw new IllegalStateException("keys out of order in " + nodeKeysAsText(x));
            }
        }
        for (int i = 0; i < x.n; i++) {
            K k = key(x, i);
            if (lowerBound != null && k.compareTo(lowerBound) <= 0) {
                throw new IllegalStateException("key " + k + " is in the wrong subtree");
            }
            if (upperBound != null && k.compareTo(upperBound) >= 0) {
                throw new IllegalStateException("key " + k + " is in the wrong subtree");
            }
        }
        if (x.leaf) {
            return depth;
        }
        int seen = -1;
        for (int i = 0; i <= x.n; i++) {
            Node c = child(x, i);
            if (c == null) {
                throw new IllegalStateException("missing child " + i + " under " + nodeKeysAsText(x));
            }
            K low = (i == 0) ? lowerBound : key(x, i - 1);
            K high = (i == x.n) ? upperBound : key(x, i);
            int d = check(c, depth + 1, low, high, false);
            if (seen == -1) {
                seen = d;
            } else if (seen != d) {
                throw new IllegalStateException("leaves are at different depths (" + seen + " vs " + d + ")");
            }
        }
        return seen;
    }

    private void requireKey(K k) {
        if (k == null) {
            throw new IllegalArgumentException("B-Tree keys cannot be null");
        }
    }
}