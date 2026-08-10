package com.campushub.structures.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BSTTest {

    // Normal Case
    // Insert 4 valid items into the tree and verify that each one can be
    // found using the search method. This confirms that insert places
    // nodes correctly and search retrieves the right value for each key.
    @Test
    public void testInsertAndSearchNormalCase() {
        BST tree = new BST();

        tree.insert(10, "Balme Library");
        tree.insert(5,  "Great Hall");
        tree.insert(15, "N Block");
        tree.insert(3,  "Legon Hall");

        // Each search should return the value that was paired with the key.
        assertEquals("Balme Library", tree.search(10));
        assertEquals("Great Hall",   tree.search(5));
        assertEquals("N Block",      tree.search(15));
        assertEquals("Legon Hall",   tree.search(3));
    }

    // Boundary Case 1: Empty Tree
    // Search on an empty tree (nothing inserted yet). The search method
    // should return null because there are no nodes to find.
    @Test
    public void testSearchOnEmptyTree() {
        BST tree = new BST();

        // No items have been inserted, so any search should return null.
        assertNull(tree.search(1));
        assertNull(tree.search(100));
        assertNull(tree.search(0));
    }

    // Boundary Case 2: Single Element
    // Insert exactly one item and verify it can be found. Also check that
    // searching for a different key returns null, confirming the tree
    // works correctly even with just one node.
    @Test
    public void testSingleElement() {
        BST tree = new BST();

        tree.insert(42, "JQB");

        // The one inserted key should be found.
        assertEquals("JQB", tree.search(42));

        // Any other key should not be found.
        assertNull(tree.search(1));
        assertNull(tree.search(99));
    }

    // Invalid Input: Duplicate Key
    // Insert the same key twice with different values. The tree should
    // NOT create a duplicate node — instead it should update the value
    // associated with that key. This confirms the tree handles duplicates
    // safely without breaking.
    @Test
    public void testDuplicateKeyUpdatesValue() {
        BST tree = new BST();

        tree.insert(20, "Evandy Hostel");
        tree.insert(20, "Evandy Hostel (Renovated)");

        // The value should be updated to the second insert's value.
        assertEquals("Evandy Hostel (Renovated)", tree.search(20));
    }

    // Duplicate Key With Other Nodes
    // Insert several items, then insert a duplicate key. Verify that
    // only the duplicate's value is updated and all other entries remain
    // unchanged — proving that the duplicate handling does not corrupt
    // the rest of the tree.
    @Test
    public void testDuplicateKeyDoesNotAffectOtherNodes() {
        BST tree = new BST();

        tree.insert(10, "Balme Library");
        tree.insert(5,  "Great Hall");
        tree.insert(15, "N Block");

        // Update key 5 with a new value.
        tree.insert(5, "Updated Great Hall");

        // Key 5 should have the new value.
        assertEquals("Updated Great Hall", tree.search(5));

        // Keys 10 and 15 should be completely unaffected.
        assertEquals("Balme Library", tree.search(10));
        assertEquals("N Block",      tree.search(15));
    }

    // Search For Non-Existent Key
    // Insert a few items, then search for a key that was never inserted.
    // The search should return null, not crash or return a wrong value.
    @Test
    public void testSearchForNonExistentKey() {
        BST tree = new BST();

        tree.insert(10, "Balme Library");
        tree.insert(20, "Athletic Oval");

        // Key 99 was never inserted — search should return null.
        assertNull(tree.search(99));
        assertNull(tree.search(1));
    }
}

