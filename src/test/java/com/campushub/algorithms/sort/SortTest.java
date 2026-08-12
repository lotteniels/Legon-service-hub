package com.campushub.algorithms.sort;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SortTest {

    @Test
    public void testMergeSort() {
        MergeSort sorter = new MergeSort();
        int[] arr = {5, 2, 9, 1, 5, 6};
        int[] expected = {1, 2, 5, 5, 6, 9};
        
        int[] sorted = sorter.mergeSort(arr);
        assertArrayEquals(expected, sorted);
        // Ensure original array is not mutated
        assertEquals(5, arr[0]);
    }

    @Test
    public void testQuickSort() {
        QuickSort sorter = new QuickSort();
        int[] arr = {5, 2, 9, 1, 5, 6};
        int[] expected = {1, 2, 5, 5, 6, 9};
        
        int[] sorted = sorter.quickSort(arr);
        assertArrayEquals(expected, sorted);
    }

    @Test
    public void testInsertionSort() {
        InsertionSort sorter = new InsertionSort();
        int[] arr = {5, 2, 9, 1, 5, 6};
        int[] expected = {1, 2, 5, 5, 6, 9};
        
        int[] sorted = sorter.insertionSort(arr);
        assertArrayEquals(expected, sorted);
    }

    @Test
    public void testSelectionSort() {
        SelectionSort sorter = new SelectionSort();
        int[] arr = {5, 2, 9, 1, 5, 6};
        int[] expected = {1, 2, 5, 5, 6, 9};
        
        int[] sorted = sorter.selectionSort(arr);
        assertArrayEquals(expected, sorted);
    }

    @Test
    public void testEmptyAndSingleElementArray() {
        MergeSort sorter = new MergeSort();
        int[] empty = {};
        int[] single = {1};
        
        assertArrayEquals(new int[]{}, sorter.mergeSort(empty));
        assertArrayEquals(new int[]{1}, sorter.mergeSort(single));
    }
}
