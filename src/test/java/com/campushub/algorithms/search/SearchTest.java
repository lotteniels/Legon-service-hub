package com.campushub.algorithms.search;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SearchTest {

    @Test
    public void testLinearSearch() {
        LinearSearch search = new LinearSearch();
        int[] arr = {4, 2, 9, 1, 5};
        
        assertEquals(2, search.linearSearch(arr, 9));
        assertEquals(0, search.linearSearch(arr, 4));
        assertEquals(-1, search.linearSearch(arr, 10)); // Not found
    }

    @Test
    public void testBinarySearch() {
        BinarySearch search = new BinarySearch();
        int[] arr = {1, 2, 4, 5, 9}; // Must be sorted
        
        assertEquals(2, search.binarySearch(arr, 4));
        assertEquals(4, search.binarySearch(arr, 9));
        assertEquals(-1, search.binarySearch(arr, 10)); // Not found
    }

    @Test
    public void testEmptyArray() {
        LinearSearch lSearch = new LinearSearch();
        BinarySearch bSearch = new BinarySearch();
        int[] arr = {};
        
        assertEquals(-1, lSearch.linearSearch(arr, 1));
        assertEquals(-1, bSearch.binarySearch(arr, 1));
    }
}
