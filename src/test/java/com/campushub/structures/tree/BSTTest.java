package com.campushub.structures.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BSTTest {

    @Test
    public void testInsertAndSearch() {
        BST bst = new BST();
        bst.insert(10, "A");
        bst.insert(5, "B");
        bst.insert(15, "C");
        
        assertEquals("A", bst.search(10));
        assertEquals("B", bst.search(5));
        assertEquals("C", bst.search(15));
    }

    @Test
    public void testUpdateDuplicateKey() {
        BST bst = new BST();
        bst.insert(10, "A");
        bst.insert(10, "B");
        assertEquals("B", bst.search(10), "Duplicate insert should update value");
    }

    @Test
    public void testSearchNotFound() {
        BST bst = new BST();
        bst.insert(10, "A");
        assertNull(bst.search(5), "Should return null for missing key");
    }
}
