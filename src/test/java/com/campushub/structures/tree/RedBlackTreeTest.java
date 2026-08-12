package com.campushub.structures.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RedBlackTreeTest {

    @Test
    public void testInsertAndSearch() {
        RedBlackTree rbt = new RedBlackTree();
        rbt.insert(10, "A");
        rbt.insert(20, "B");
        rbt.insert(30, "C"); // Should trigger rotations
        
        assertEquals("A", rbt.search(10));
        assertEquals("B", rbt.search(20));
        assertEquals("C", rbt.search(30));
    }

    @Test
    public void testUpdateDuplicateKey() {
        RedBlackTree rbt = new RedBlackTree();
        rbt.insert(10, "A");
        rbt.insert(10, "B");
        assertEquals("B", rbt.search(10));
    }

    @Test
    public void testSearchNotFound() {
        RedBlackTree rbt = new RedBlackTree();
        rbt.insert(10, "A");
        assertNull(rbt.search(99));
    }
}
