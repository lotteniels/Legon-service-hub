package com.campushub.algorithms.optimization;

import com.campushub.model.ServiceRequest;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.testsupport.GraphFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnapsackDPTest {

    @Test
    public void takesNothingWhenThereIsNoCapacity() {
        KnapsackDP.Result solved = KnapsackDP.solve(new int[] {5}, new double[] {100}, 0);

        assertEquals(0, solved.chosenCount());
        assertEquals(0.0, solved.bestValue());
        assertEquals(0, solved.weightUsed());
    }

    @Test
    public void takesTheOnlyItemThatFits() {
        KnapsackDP.Result solved = KnapsackDP.solve(new int[] {10, 3}, new double[] {50, 5}, 5);

        assertArrayEquals(new int[] {1}, solved.chosenIndices());
        assertEquals(5.0, solved.bestValue());
    }

    @Test
    public void skipsAHighValueItemToFitTwoWorthMoreTogether() {
        // Greedy on value takes item 0 for 60 and stops. The optimum is items 1 and 2
        // together for 70.
        KnapsackDP.Result solved =
                KnapsackDP.solve(new int[] {10, 5, 5}, new double[] {60, 35, 35}, 10);

        assertEquals(70.0, solved.bestValue());
        assertArrayEquals(new int[] {1, 2}, solved.chosenIndices());
    }

    @Test
    public void beatsGreedyByValuePerWeightToo() {
        // Best value-per-weight is item 0 at 6.0, but taking it wastes 4 capacity.
        // Items 1 and 2 fill the sack exactly and win.
        KnapsackDP.Result solved =
                KnapsackDP.solve(new int[] {6, 5, 5}, new double[] {36, 30, 30}, 10);

        assertEquals(60.0, solved.bestValue());
        assertArrayEquals(new int[] {1, 2}, solved.chosenIndices());
    }

    @Test
    public void takesEverythingWhenCapacityAllows() {
        KnapsackDP.Result solved =
                KnapsackDP.solve(new int[] {2, 3, 4}, new double[] {1, 1, 1}, 100);

        assertEquals(3, solved.chosenCount());
        assertEquals(3.0, solved.bestValue());
        assertEquals(9, solved.weightUsed());
    }

    @Test
    public void chosenWeightNeverExceedsCapacity() {
        int[] weights = {7, 11, 4, 9, 3, 6};
        double[] values = {13, 21, 6, 17, 4, 10};

        for (int capacity = 0; capacity <= 40; capacity++) {
            KnapsackDP.Result solved = KnapsackDP.solve(weights, values, capacity);
            assertTrue(solved.weightUsed() <= capacity,
                    "capacity " + capacity + " overfilled to " + solved.weightUsed());
        }
    }

    @Test
    public void chosenItemsSumToTheReportedValue() {
        int[] weights = {7, 11, 4, 9, 3, 6};
        double[] values = {13, 21, 6, 17, 4, 10};

        KnapsackDP.Result solved = KnapsackDP.solve(weights, values, 25);

        double summedValue = 0;
        int summedWeight = 0;
        int[] chosen = solved.chosenIndices();
        for (int index = 0; index < chosen.length; index++) {
            summedValue += values[chosen[index]];
            summedWeight += weights[chosen[index]];
        }
        assertEquals(solved.bestValue(), summedValue, 1e-9);
        assertEquals(solved.weightUsed(), summedWeight);
    }

    @Test
    public void matchesBruteForceOnEverySubsetOfASmallInstance() {
        int[] weights = {7, 11, 4, 9, 3, 6};
        double[] values = {13, 21, 6, 17, 4, 10};

        for (int capacity = 0; capacity <= 30; capacity++) {
            double best = 0;
            for (int mask = 0; mask < (1 << weights.length); mask++) {
                int weight = 0;
                double value = 0;
                for (int item = 0; item < weights.length; item++) {
                    if ((mask & (1 << item)) != 0) {
                        weight += weights[item];
                        value += values[item];
                    }
                }
                if (weight <= capacity && value > best) {
                    best = value;
                }
            }
            assertEquals(best, KnapsackDP.solve(weights, values, capacity).bestValue(), 1e-9,
                    "DP disagreed with brute force at capacity " + capacity);
        }
    }

    @Test
    public void tableHasOneRowPerItemPlusTheEmptyRow() {
        KnapsackDP.Result solved = KnapsackDP.solve(new int[] {2, 3}, new double[] {1, 1}, 5);

        double[][] table = solved.table();
        assertEquals(3, table.length);
        assertEquals(6, table[0].length);
        for (int budget = 0; budget < table[0].length; budget++) {
            assertEquals(0.0, table[0][budget], "the empty row must be all zero");
        }
    }

    @Test
    public void tableIsADefensiveCopy() {
        KnapsackDP.Result solved = KnapsackDP.solve(new int[] {2}, new double[] {9}, 5);

        solved.table()[1][5] = -1;

        assertEquals(9.0, solved.table()[1][5]);
    }

    @Test
    public void malformedInputIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> KnapsackDP.solve(new int[] {1}, new double[] {1, 2}, 5));
        assertThrows(IllegalArgumentException.class,
                () -> KnapsackDP.solve(new int[] {1}, new double[] {1}, -1));
        assertThrows(IllegalArgumentException.class,
                () -> KnapsackDP.solve(new int[] {-1}, new double[] {1}, 5));
        assertThrows(IllegalArgumentException.class,
                () -> KnapsackDP.solve(null, new double[] {1}, 5));
    }

    @Test
    public void shiftPlanStaysWithinBudgetAndReturnsCallerIndices() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting =
                ServiceData.outstanding(ServiceData.loadRequests(GraphFixtures.SEED_DATA));

        KnapsackDP.Result plan = KnapsackDP.forShift(graph, waiting, 33, 240);

        assertTrue(plan.chosenCount() > 0);
        assertTrue(plan.weightUsed() <= 240);
        int[] chosen = plan.chosenIndices();
        for (int index = 0; index < chosen.length; index++) {
            assertTrue(chosen[index] >= 0 && chosen[index] < waiting.size(),
                    "index " + chosen[index] + " is outside the request list");
        }
    }

    @Test
    public void aLongerShiftIsNeverWorseThanAShorterOne() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting =
                ServiceData.outstanding(ServiceData.loadRequests(GraphFixtures.SEED_DATA));

        double previous = -1;
        for (int minutes = 60; minutes <= 480; minutes += 60) {
            double value = KnapsackDP.forShift(graph, waiting, 33, minutes).bestValue();
            assertTrue(value >= previous,
                    "value fell from " + previous + " to " + value + " at " + minutes + " min");
            previous = value;
        }
    }

    @Test
    public void unknownDepotIsRejected() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting = ServiceData.loadRequests(GraphFixtures.SEED_DATA);

        assertThrows(IllegalArgumentException.class,
                () -> KnapsackDP.forShift(graph, waiting, 999, 240));
    }
}
