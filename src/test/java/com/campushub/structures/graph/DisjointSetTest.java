package com.campushub.structures.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DisjointSetTest {

    @Test
    public void everyElementStartsInItsOwnSet() {
        DisjointSet sets = new DisjointSet(4);

        assertEquals(4, sets.elementCount());
        assertEquals(4, sets.setCount());
        assertFalse(sets.connected(0, 1));
        assertEquals(1, sets.sizeOfSet(0));
    }

    @Test
    public void unionMergesTwoSetsAndReportsIt() {
        DisjointSet sets = new DisjointSet(3);

        assertTrue(sets.union(0, 1));
        assertEquals(2, sets.setCount());
        assertTrue(sets.connected(0, 1));
        assertEquals(2, sets.sizeOfSet(0));
        assertFalse(sets.connected(0, 2));
    }

    @Test
    public void unionOfAlreadyJoinedElementsIsRejected() {
        DisjointSet sets = new DisjointSet(3);
        sets.union(0, 1);
        sets.union(1, 2);

        // This false is the signal Kruskal uses to skip a cycle-closing road.
        assertFalse(sets.union(0, 2));
        assertEquals(1, sets.setCount());
        assertEquals(2, sets.unionsPerformed());
    }

    @Test
    public void unionIsTransitiveAcrossAChain() {
        DisjointSet sets = new DisjointSet(5);
        sets.union(0, 1);
        sets.union(2, 3);
        sets.union(1, 2);

        assertTrue(sets.connected(0, 3));
        assertFalse(sets.connected(0, 4));
        assertEquals(2, sets.setCount());
        assertEquals(4, sets.sizeOfSet(3));
    }

    @Test
    public void setsAreReportedInDeterministicOrder() {
        DisjointSet sets = new DisjointSet(5);
        sets.union(4, 1);
        sets.union(3, 0);

        int[][] grouped = sets.sets();

        assertEquals(3, grouped.length);
        assertArrayEquals(new int[] {0, 3}, grouped[0]);
        assertArrayEquals(new int[] {1, 4}, grouped[1]);
        assertArrayEquals(new int[] {2}, grouped[2]);
    }

    @Test
    public void setsOfAnUntouchedStructureAreAllSingletons() {
        int[][] grouped = new DisjointSet(3).sets();

        assertEquals(3, grouped.length);
        assertArrayEquals(new int[] {0}, grouped[0]);
        assertArrayEquals(new int[] {1}, grouped[1]);
        assertArrayEquals(new int[] {2}, grouped[2]);
    }

    @Test
    public void pathCompressionFlattensARepeatedlyWalkedChain() {
        DisjointSet sets = new DisjointSet(200);
        for (int element = 0; element < 199; element++) {
            sets.union(element, element + 1);
        }
        for (int element = 0; element < 200; element++) {
            sets.find(element);
        }

        int hopsBefore = sets.pointerHops();
        for (int element = 0; element < 200; element++) {
            sets.find(element);
        }

        assertEquals(1, sets.setCount());
        assertTrue(sets.pointerHops() - hopsBefore <= 200,
                "a compressed forest should cost at most one hop per find, spent "
                        + (sets.pointerHops() - hopsBefore));
    }

    @Test
    public void unionByRankKeepsTreesShallow() {
        DisjointSet sets = new DisjointSet(1024);
        for (int step = 1; step < 1024; step *= 2) {
            for (int element = 0; element + step < 1024; element += step * 2) {
                sets.union(element, element + step);
            }
        }

        assertEquals(1, sets.setCount());
        assertEquals(1024, sets.sizeOfSet(0));

        int hopsBefore = sets.pointerHops();
        sets.find(1023);
        // log2(1024) is 10; anything near 1024 would mean the tree degenerated.
        assertTrue(sets.pointerHops() - hopsBefore <= 11,
                "depth should stay logarithmic, walked "
                        + (sets.pointerHops() - hopsBefore) + " pointers");
    }

    @Test
    public void outOfRangeElementsAreRejected() {
        DisjointSet sets = new DisjointSet(3);

        assertThrows(IndexOutOfBoundsException.class, () -> sets.find(3));
        assertThrows(IndexOutOfBoundsException.class, () -> sets.find(-1));
    }

    @Test
    public void negativeElementCountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new DisjointSet(-1));
    }

    @Test
    public void anEmptyStructureHasNoSets() {
        DisjointSet sets = new DisjointSet(0);

        assertEquals(0, sets.elementCount());
        assertEquals(0, sets.setCount());
        assertEquals(0, sets.sets().length);
    }
}
