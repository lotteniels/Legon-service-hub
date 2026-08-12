package com.campushub.structures.linear;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicArrayTest {

    @Test
    public void testInsertNormal() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(10);
        arr.add(20);
        assertEquals(2, arr.size(), "Size should be 2 after inserting two elements");
        assertEquals(10, arr.get(0), "First element should be 10");
        assertEquals(20, arr.get(1), "Second element should be 20");
    }

    @Test
    public void testInsertResize() {
        DynamicArray<Integer> arr = new DynamicArray<>(2);
        arr.add(1);
        arr.add(2);
        arr.add(3); // This should trigger a resize
        assertEquals(3, arr.size(), "Size should be 3 after resizing");
        assertEquals(3, arr.get(2), "Third element should be 3");
    }

    @Test
    public void testGetInvalidIndex() {
        DynamicArray<String> arr = new DynamicArray<>();
        arr.add("hello");
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(1), "Should throw exception for out of bounds index");
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(-1), "Should throw exception for negative index");
    }

    @Test
    public void testRemoveNormal() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(10);
        arr.add(20);
        arr.add(30);
        Integer removed = arr.remove(1);
        assertEquals(20, removed, "Removed element should be 20");
        assertEquals(2, arr.size(), "Size should be 2 after removal");
        assertEquals(30, arr.get(1), "Element at index 1 should now be 30");
    }

    @Test
    public void testRemoveBoundary() {
        DynamicArray<Integer> arr = new DynamicArray<>();
        arr.add(10);
        Integer removed = arr.remove(0);
        assertEquals(10, removed, "Removed element should be 10");
        assertEquals(0, arr.size(), "Size should be 0 after removing the only element");
        assertThrows(IndexOutOfBoundsException.class, () -> arr.remove(0), "Should throw exception when removing from empty array");
    }
}
