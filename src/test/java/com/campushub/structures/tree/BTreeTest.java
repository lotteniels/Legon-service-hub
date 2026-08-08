package com.campushub.structures.tree;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BTree}.
 *
 * <p>The four cases the brief asks for are the first four tests below. The rest exist
 * because a B-Tree can look fine on small inputs and still be broken once nodes start
 * splitting, so we also check the structural rules directly via {@code validate()} and
 * compare against java.util.TreeMap on thousands of random operations.
 *
 * <p>Small t values are used on purpose: t = 2 means a node fills up after 3 keys, so
 * splits happen almost immediately and the interesting code paths get exercised.
 */
class BTreeTest {

    // ============================================================ required cases

    @Test
    @DisplayName("adding and finding a normal item")
    void addAndFindNormalItem() {
        BTree<Integer, String> tree = new BTree<>(3);

        tree.put(101, "Reference Hall");
        tree.put(102, "Braille Library");
        tree.put(103, "Research Commons");

        assertAll(
                () -> assertEquals("Braille Library", tree.get(102), "should return the stored value"),
                () -> assertTrue(tree.contains(101), "contains should see an inserted key"),
                () -> assertEquals(3, tree.size(), "three inserts means size three"),
                () -> assertNull(tree.get(999), "a key that was never inserted returns null"),
                () -> assertFalse(tree.contains(999), "contains is false for a missing key")
        );
        tree.validate();
    }

    @Test
    @DisplayName("an empty tree behaves safely")
    void emptyTree() {
        BTree<Integer, String> tree = new BTree<>(3);

        assertAll(
                () -> assertTrue(tree.isEmpty()),
                () -> assertEquals(0, tree.size()),
                () -> assertEquals(0, tree.height(), "a tree with just an empty root has height 0"),
                () -> assertNull(tree.get(1), "searching an empty tree returns null, it must not crash"),
                () -> assertFalse(tree.contains(1)),
                () -> assertFalse(tree.delete(1), "deleting from an empty tree returns false"),
                () -> assertTrue(tree.keysInOrder().isEmpty()),
                () -> assertNotNull(tree.structureAsText(), "printing an empty tree must not crash")
        );
        tree.validate();
    }

    @Test
    @DisplayName("a tree holding exactly one item")
    void singleItem() {
        BTree<Integer, String> tree = new BTree<>(3);
        tree.put(7, "Z Room");

        assertAll(
                () -> assertFalse(tree.isEmpty()),
                () -> assertEquals(1, tree.size()),
                () -> assertEquals(0, tree.height(), "one item still fits in the root, so no split yet"),
                () -> assertEquals("Z Room", tree.get(7)),
                () -> assertNull(tree.get(6), "a smaller missing key returns null"),
                () -> assertNull(tree.get(8), "a bigger missing key returns null"),
                () -> assertEquals(List.of(7), tree.keysInOrder())
        );
        tree.validate();

        assertTrue(tree.delete(7), "removing the only item works");
        assertTrue(tree.isEmpty(), "tree is empty again afterwards");
        tree.validate();
    }

    @Test
    @DisplayName("adding a duplicate updates the value instead of storing the key twice")
    void duplicateItem() {
        BTree<Integer, String> tree = new BTree<>(2);
        tree.put(50, "first");

        String previous = tree.put(50, "second");

        assertAll(
                () -> assertEquals("first", previous, "put returns the value it replaced"),
                () -> assertEquals("second", tree.get(50), "the new value wins"),
                () -> assertEquals(1, tree.size(), "size must NOT grow on a duplicate"),
                () -> assertEquals(List.of(50), tree.keysInOrder(), "the key appears exactly once")
        );
        tree.validate();

        // Same thing, but on a tree big enough that 50 lives in an internal node.
        BTree<Integer, Integer> bigger = new BTree<>(2);
        for (int i = 1; i <= 40; i++) {
            bigger.insert(i);
        }
        int sizeBefore = bigger.size();
        for (int i = 1; i <= 40; i++) {
            bigger.insert(i);
        }
        assertEquals(sizeBefore, bigger.size(), "re-inserting every key must not change the size");
        bigger.validate();
    }

    // ============================================================ splitting / shape

    @Test
    @DisplayName("a node splits once it passes 2t-1 keys, and the tree gets taller from the root")
    void nodeSplitsWhenFull() {
        BTree<Integer, Integer> tree = new BTree<>(2);   // max 3 keys per node
        assertEquals(3, tree.maxKeysPerNode());

        tree.insert(10);
        tree.insert(20);
        tree.insert(5);
        assertEquals(0, tree.height(), "3 keys still fit in the root, so it is one node");

        tree.insert(6);   // 4th key -> the root is full, so it must split first
        assertEquals(1, tree.height(), "after the split the tree is one level taller");
        tree.validate();

        // Median 10 was pushed up; 5 and 6 went left, 20 went right.
        assertEquals(List.of(5, 6, 10, 20), tree.keysInOrder());
        assertTrue(tree.structureAsText().contains("[10]"), "10 should now be the root key");
    }

    @Test
    @DisplayName("inserting already-sorted data stays balanced (a plain BST would not)")
    void sortedInputStaysBalanced() {
        BTree<Integer, Integer> tree = new BTree<>(3);
        int count = 10_000;
        for (int i = 0; i < count; i++) {
            tree.insert(i);          // worst case for an unbalanced BST: a 10,000-deep chain
        }
        tree.validate();

        assertEquals(count, tree.size());
        assertTrue(tree.height() <= 8,
                "height should stay tiny, was " + tree.height() + " for " + count + " keys");
        for (int i = 0; i < count; i += 500) {
            assertTrue(tree.contains(i), "should still find " + i);
        }
    }

    @Test
    @DisplayName("in-order traversal returns every key in sorted order")
    void traversalIsSorted() {
        BTree<Integer, Integer> tree = new BTree<>(2);
        List<Integer> input = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            input.add(i);
        }
        Collections.shuffle(input, new Random(11));
        input.forEach(tree::insert);
        tree.validate();

        List<Integer> sorted = new ArrayList<>(input);
        Collections.sort(sorted);
        assertEquals(sorted, tree.keysInOrder());
    }

    @Test
    @DisplayName("string keys work too (e.g. library call numbers)")
    void stringKeys() {
        BTree<String, String> tree = new BTree<>(2);
        tree.put("PL480.B3", "Arabic Library");
        tree.put("HA29.S6", "Arts & Social Science Stacks");
        tree.put("Z711.2", "Z Room");

        assertEquals("Z Room", tree.get("Z711.2"));
        assertEquals(List.of("HA29.S6", "PL480.B3", "Z711.2"), tree.keysInOrder(),
                "keys come back in alphabetical order");
        tree.validate();
    }

    // ============================================================ deleting

    @Test
    @DisplayName("deleting keys keeps every B-Tree rule intact")
    void deleteKeepsTreeValid() {
        BTree<Integer, Integer> tree = new BTree<>(2);
        for (int i = 1; i <= 200; i++) {
            tree.insert(i);
        }

        assertFalse(tree.delete(999), "deleting a key that is not there returns false");

        List<Integer> order = new ArrayList<>();
        for (int i = 1; i <= 200; i++) {
            order.add(i);
        }
        Collections.shuffle(order, new Random(3));
        for (int k : order) {
            assertTrue(tree.delete(k), "should remove " + k);
            tree.validate();                       // checks depth, fill levels and ordering
            assertFalse(tree.contains(k), k + " should be gone");
        }
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.height(), "the tree shrinks back down as merges empty the root");
    }

    // ============================================================ tracing / evidence

    @Test
    @DisplayName("the search trace records one step per level")
    void searchTraceIsRecorded() {
        BTree<Integer, Integer> tree = new BTree<>(2);
        for (int k : new int[] {10, 20, 5, 6, 12, 30, 7, 17}) {
            tree.insert(k);
        }

        BTree.SearchResult hit = tree.searchWithTrace(17);
        assertAll(
                () -> assertTrue(hit.found),
                () -> assertEquals(2, hit.nodesVisited, "root then one leaf"),
                () -> assertTrue(hit.comparisons > 0),
                () -> assertFalse(hit.steps.isEmpty())
        );

        BTree.SearchResult miss = tree.searchWithTrace(8);
        assertFalse(miss.found, "8 was never inserted");
        assertTrue(miss.nodesVisited <= tree.height() + 1,
                "a search never visits more nodes than the tree is deep");
    }

    // ============================================================ guards

    @Test
    @DisplayName("bad input is rejected loudly")
    void badInputRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, Integer>(1),
                "t must be at least 2");
        assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, Integer>().put(null, 1),
                "null keys cannot be ordered, so they are rejected");
        assertThrows(IllegalArgumentException.class, () -> BTree.minDegreeFromIndexNumbers(),
                "deriving t needs at least one index number");
    }

    @Test
    @DisplayName("the minimum degree is derived from the team's index numbers")
    void minDegreeDerivedFromIndexNumbers() {
        // t = 2 + (sum mod 4).  11111111 + 22222222 + 33333333 = 66666666; 66666666 % 4 = 2
        assertEquals(4, BTree.minDegreeFromIndexNumbers(11111111, 22222222, 33333333));

        // Always a legal B-Tree, whatever the index numbers happen to be.
        for (int a = 0; a < 40; a++) {
            int t = BTree.minDegreeFromIndexNumbers(10000000 + a, 20000000 + a * 7);
            assertTrue(t >= 2 && t <= 5, "derived t must stay in 2..5, got " + t);
        }

        // A tree built this way is a working tree, not just a number.
        BTree<Integer, Integer> tree = BTree.fromIndexNumbers(11111111, 22222222, 33333333);
        assertEquals(4, tree.minDegree());
        assertEquals(7, tree.maxKeysPerNode(), "2t-1 keys per node");
        for (int i = 0; i < 1000; i++) {
            tree.insert(i);
        }
        tree.validate();
        assertEquals(1000, tree.size());
    }

    // ============================================================ the big one

    @Test
    @DisplayName("thousands of random operations agree with java.util.TreeMap")
    void matchesTreeMapUnderRandomOperations() {
        for (int t = 2; t <= 4; t++) {
            BTree<Integer, Integer> tree = new BTree<>(t);
            TreeMap<Integer, Integer> reference = new TreeMap<>();
            Random random = new Random(42L + t);

            for (int op = 0; op < 5000; op++) {
                int key = random.nextInt(400);
                if (random.nextInt(100) < 60) {
                    assertEquals(reference.put(key, op), tree.put(key, op),
                            "put should return the same old value as TreeMap");
                } else {
                    assertEquals(reference.remove(key) != null, tree.delete(key),
                            "delete should agree with TreeMap");
                }
                if (op % 250 == 0) {
                    tree.validate();
                    assertEquals(reference.size(), tree.size());
                }
            }
            tree.validate();
            assertEquals(new ArrayList<>(reference.keySet()), tree.keysInOrder(),
                    "t=" + t + ": the whole index should match, in order");
        }
    }
}