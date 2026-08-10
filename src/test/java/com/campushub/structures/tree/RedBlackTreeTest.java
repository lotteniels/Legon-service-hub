package com.campushub.structures.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RedBlackTreeTest {

    // Normal Case
    // Also uses realistic local Ghanaian campus data.
    @Test
    public void testInsertAndSearchNormalCase() {
        RedBlackTree tree = new RedBlackTree();

        tree.insert(100, "Pentagon Hostel");
        tree.insert(105, "Balme Library");
        tree.insert(202, "JQB Building");
        tree.insert(310, "Commonwealth Hall");

        // Assert that all inserted valid items can be successfully found
        assertEquals("Pentagon Hostel", tree.search(100));
        assertEquals("Balme Library", tree.search(105));
        assertEquals("JQB Building", tree.search(202));
        assertEquals("Commonwealth Hall", tree.search(310));
    }

    // Boundary Case 1: Empty Tree
    @Test
    public void testSearchOnEmptyTree() {
        RedBlackTree tree = new RedBlackTree();

        // Searching an empty tree should safely return null
        assertNull(tree.search(100));
        assertNull(tree.search(999));
    }

    // Boundary Case 2: Single Element
    @Test
    public void testSingleElement() {
        RedBlackTree tree = new RedBlackTree();

        tree.insert(50, "Night Market");

        // Should find the only element in the tree
        assertEquals("Night Market", tree.search(50));

        // Should return null for anything else
        assertNull(tree.search(100));
    }

    // Invalid Input: Duplicate Key
    @Test
    public void testDuplicateKeyUpdatesValue() {
        RedBlackTree tree = new RedBlackTree();

        tree.insert(150, "Akuafo Hall (Old)");
        
        // Attempting to insert a duplicate ID
        tree.insert(150, "Akuafo Hall (Renovated)");

        // Assert that the tree didn't crash and the original value is updated
        assertEquals("Akuafo Hall (Renovated)", tree.search(150));
    }

    // Robustness / Boundary Case 3: Non-Existent Key
    @Test
    public void testSearchForNonExistentKeyInPopulatedTree() {
        RedBlackTree tree = new RedBlackTree();

        tree.insert(101, "Legon Hall");
        tree.insert(102, "Volta Hall");

        // The tree has data, but this specific ID doesn't exist
        assertNull(tree.search(999));
    }

    // Robustness: Duplicate Does Not Break Others
    @Test
    public void testDuplicateDoesNotCorruptOtherNodes() {
        RedBlackTree tree = new RedBlackTree();

        tree.insert(1, "Mensah Sarbah Hall");
        tree.insert(2, "Jean Nelson Aka Hall");
        tree.insert(3, "Alexander Adum Kwapong Hall");

        // Insert duplicate for ID 2
        tree.insert(2, "Jean Nelson (Updated)");

        // ID 2 is updated
        assertEquals("Jean Nelson (Updated)", tree.search(2));
        
        // IDs 1 and 3 remain perfectly intact
        assertEquals("Mensah Sarbah Hall", tree.search(1));
        assertEquals("Alexander Adum Kwapong Hall", tree.search(3));
    }
}

