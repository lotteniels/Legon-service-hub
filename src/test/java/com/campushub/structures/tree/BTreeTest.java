package com.campushub.structures.tree;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BTreeTest {

    @Test
    public void testInsertAndGet() {
        BTree<Integer, String> tree = new BTree<>(3);
        tree.put(10, "A");
        tree.put(20, "B");
        tree.put(5, "C");
        
        assertEquals("A", tree.get(10));
        assertEquals("B", tree.get(20));
        assertEquals("C", tree.get(5));
        assertEquals(3, tree.size());
    }

    @Test
    public void testUpdateKey() {
        BTree<Integer, String> tree = new BTree<>(3);
        tree.put(1, "Old");
        tree.put(1, "New");
        assertEquals("New", tree.get(1));
        assertEquals(1, tree.size());
    }

    @Test
    public void testDelete() {
        BTree<Integer, String> tree = new BTree<>(3);
        tree.put(10, "A");
        tree.put(20, "B");
        
        assertTrue(tree.delete(10));
        assertNull(tree.get(10));
        assertFalse(tree.delete(10));
        assertEquals(1, tree.size());
    }

    @Test
    public void testBTreeSplitAndValidate() {
        BTree<Integer, String> tree = new BTree<>(2);
        for (int i = 0; i < 20; i++) {
            tree.put(i, "V" + i);
        }
        assertEquals(20, tree.size());
        assertEquals("V15", tree.get(15));
        
        // This validates the B-Tree properties (splits, ordering, depths)
        assertDoesNotThrow(() -> tree.validate());
    }
}