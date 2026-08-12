package com.campushub.algorithms.optimization;

import com.campushub.algorithms.graph.Dijkstra;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

// Owner: Graphs and Optimization

/**
 * 0/1 knapsack by dynamic programming: which requests to fulfil in one shift.
 *
 * <p>A crew has a fixed number of minutes. Each request costs the travel time to reach
 * it plus its service time, and is worth the penalty it avoids plus a bonus for urgency.
 * Every request is taken whole or not at all, which is what makes this 0/1 rather than
 * fractional.
 *
 * <p>This is the deliberate counterpoint to {@link GreedyAssigner}. Greedy commits to
 * the most urgent request immediately; the DP examines every subset implicitly and can
 * refuse an urgent-but-expensive request to fit three cheaper ones worth more together.
 * {@code KnapsackDPTest} pins an instance where greedy provably loses.
 *
 * <p>Runs in O(n x capacity) time and space. The full table is kept rather than the
 * usual single-row optimisation, because reconstructing the chosen set needs it and
 * {@code evidence/trace-tables/dp-trace.md} reads rows straight off it.
 */
public final class KnapsackDP {

    private KnapsackDP() {
    }

    /** Minutes of service assumed per request, on top of travel time. */
    public static final int DEFAULT_SERVICE_MINUTES = 15;

    /** Value added per step of urgency, so a high request outranks a small fine. */
    public static final double URGENCY_VALUE = 20.0;

    /** Outcome of one solve. */
    public static final class Result {

        private final int[] chosenIndices;
        private final double bestValue;
        private final int weightUsed;
        private final int capacity;
        private final double[][] table;
        private final long elapsedNanos;

        Result(int[] chosenIndices, double bestValue, int weightUsed, int capacity,
               double[][] table, long elapsedNanos) {
            this.chosenIndices = chosenIndices;
            this.bestValue = bestValue;
            this.weightUsed = weightUsed;
            this.capacity = capacity;
            this.table = table;
            this.elapsedNanos = elapsedNanos;
        }

        /** Indices of the chosen items, ascending. */
        public int[] chosenIndices() {
            return chosenIndices.clone();
        }

        /** Total value of the chosen set - the optimum for this capacity. */
        public double bestValue() {
            return bestValue;
        }

        /** Total weight of the chosen set, never above {@link #capacity()}. */
        public int weightUsed() {
            return weightUsed;
        }

        public int capacity() {
            return capacity;
        }

        public int chosenCount() {
            return chosenIndices.length;
        }

        /**
         * The DP table: {@code table[item][weight]} is the best value using the first
         * {@code item} items within {@code weight}. Returned as a copy.
         */
        public double[][] table() {
            double[][] copy = new double[table.length][];
            for (int row = 0; row < table.length; row++) {
                copy[row] = table[row].clone();
            }
            return copy;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "KnapsackDP: " + chosenCount() + " items, value " + bestValue + ", using "
                    + weightUsed + " of " + capacity;
        }
    }

    /**
     * Solves the 0/1 knapsack for the given weights and values.
     *
     * @throws IllegalArgumentException if the arrays differ in length, the capacity is
     *         negative, or any weight is negative
     */
    public static Result solve(int[] weights, double[] values, int capacity) {
        if (weights == null || values == null) {
            throw new IllegalArgumentException("weights and values are required");
        }
        if (weights.length != values.length) {
            throw new IllegalArgumentException("weights and values must be the same length");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity must not be negative");
        }
        for (int index = 0; index < weights.length; index++) {
            if (weights[index] < 0) {
                throw new IllegalArgumentException("weight of item " + index + " is negative");
            }
        }

        long startedAt = System.nanoTime();
        int count = weights.length;
        double[][] best = new double[count + 1][capacity + 1];

        for (int item = 1; item <= count; item++) {
            int weight = weights[item - 1];
            double value = values[item - 1];
            for (int budget = 0; budget <= capacity; budget++) {
                double withoutItem = best[item - 1][budget];
                if (weight > budget) {
                    best[item][budget] = withoutItem;
                    continue;
                }
                double withItem = best[item - 1][budget - weight] + value;
                best[item][budget] = withItem > withoutItem ? withItem : withoutItem;
            }
        }

        // Walk back down the table: an item was taken exactly when its row improved on
        // the row above at the same budget.
        int taken = 0;
        int budget = capacity;
        for (int item = count; item > 0; item--) {
            if (best[item][budget] != best[item - 1][budget]) {
                taken++;
                budget -= weights[item - 1];
            }
        }

        int[] chosen = new int[taken];
        int next = taken - 1;
        int weightUsed = 0;
        budget = capacity;
        for (int item = count; item > 0; item--) {
            if (best[item][budget] != best[item - 1][budget]) {
                chosen[next--] = item - 1;
                weightUsed += weights[item - 1];
                budget -= weights[item - 1];
            }
        }

        return new Result(chosen, best[count][capacity], weightUsed, capacity, best,
                System.nanoTime() - startedAt);
    }

    /**
     * Chooses which requests one crew should fulfil in a shift.
     *
     * <p>Weight is the travel time from {@code depotLocationId} to each request plus
     * {@link #DEFAULT_SERVICE_MINUTES}, rounded up to whole minutes because the table is
     * indexed by them. Value is the avoided fine plus {@link #URGENCY_VALUE} per step of
     * urgency. Requests the depot cannot reach are dropped, and the returned indices
     * point back into {@code requests}.
     *
     * @throws IllegalArgumentException if the depot is not on the graph
     */
    public static Result forShift(Graph graph, DynamicArray<ServiceRequest> requests,
                                  int depotLocationId, int shiftMinutes) {
        if (graph.slotOf(depotLocationId) == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown depot location: " + depotLocationId);
        }
        Dijkstra.Result travel = Dijkstra.from(graph, depotLocationId);

        int reachable = 0;
        for (int index = 0; index < requests.size(); index++) {
            if (travel.isReachable(requests.get(index).getSourceLocationId())) {
                reachable++;
            }
        }

        int[] weights = new int[reachable];
        double[] values = new double[reachable];
        int[] sourceIndex = new int[reachable];
        int next = 0;
        for (int index = 0; index < requests.size(); index++) {
            ServiceRequest request = requests.get(index);
            if (!travel.isReachable(request.getSourceLocationId())) {
                continue;
            }
            double minutes = travel.costTo(request.getSourceLocationId());
            weights[next] = (int) Math.ceil(minutes) + DEFAULT_SERVICE_MINUTES;
            values[next] = request.getFineAmountGHS()
                    + URGENCY_VALUE * ServiceData.urgencyRank(request.getUrgency());
            sourceIndex[next] = index;
            next++;
        }

        Result solved = solve(weights, values, shiftMinutes);

        int[] chosen = solved.chosenIndices();
        int[] mapped = new int[chosen.length];
        for (int index = 0; index < chosen.length; index++) {
            mapped[index] = sourceIndex[chosen[index]];
        }
        return new Result(mapped, solved.bestValue(), solved.weightUsed(), solved.capacity(),
                solved.table, solved.elapsedNanos());
    }
}
