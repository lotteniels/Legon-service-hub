package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.DisjointSet;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

// Owner: Graphs and Optimization

/**
 * Kruskal's minimum spanning tree, built by taking cheap roads globally.
 *
 * <p>Sorts every road by cost and accepts each one whose endpoints are not already
 * connected, using {@link DisjointSet} to answer that in near-constant time. Where
 * {@link Prim} grows one tree outward, Kruskal merges many fragments, so on a
 * disconnected graph it naturally produces a spanning <em>forest</em> rather than
 * stopping at one component.
 *
 * <p>The sort is a merge sort over an index array, written here because the Sorting
 * pod's {@code MergeSort} only accepts {@code int[]} and these keys are doubles.
 * Sorting indices rather than the roads themselves keeps it stable, so equal-cost roads
 * stay in load order and the accept/reject sequence in the trace table is reproducible.
 */
public final class Kruskal {

    private Kruskal() {
    }

    /** One decision the algorithm made, in order - the rows of the trace table. */
    public static final class Step {

        private final Road road;
        private final double cost;
        private final boolean accepted;
        private final int setsRemaining;

        Step(Road road, double cost, boolean accepted, int setsRemaining) {
            this.road = road;
            this.cost = cost;
            this.accepted = accepted;
            this.setsRemaining = setsRemaining;
        }

        public Road road() {
            return road;
        }

        public double cost() {
            return cost;
        }

        /** True if the road joined two fragments, false if it would have closed a cycle. */
        public boolean accepted() {
            return accepted;
        }

        /** Fragments still separate after this decision. */
        public int setsRemaining() {
            return setsRemaining;
        }

        @Override
        public String toString() {
            return road.getFromLocationId() + "-" + road.getToLocationId() + " cost " + cost
                    + (accepted ? " accepted" : " rejected (cycle)") + ", " + setsRemaining
                    + " fragments left";
        }
    }

    /** Outcome of one run. */
    public static final class Result {

        private final Graph graph;
        private final DynamicArray<Road> roads;
        private final DynamicArray<Step> steps;
        private final double totalCost;
        private final int componentCount;
        private final int findCalls;
        private final int comparisons;
        private final long elapsedNanos;

        Result(Graph graph, DynamicArray<Road> roads, DynamicArray<Step> steps, double totalCost,
               int componentCount, int findCalls, int comparisons, long elapsedNanos) {
            this.graph = graph;
            this.roads = roads;
            this.steps = steps;
            this.totalCost = totalCost;
            this.componentCount = componentCount;
            this.findCalls = findCalls;
            this.comparisons = comparisons;
            this.elapsedNanos = elapsedNanos;
        }

        /** The chosen roads, cheapest first. */
        public DynamicArray<Road> roads() {
            return roads;
        }

        /** Every accept and reject decision, in order. */
        public DynamicArray<Step> steps() {
            return steps;
        }

        public double totalCost() {
            return totalCost;
        }

        public int roadCount() {
            return roads.size();
        }

        /**
         * How many separate components the result spans. One means a spanning tree; more
         * means a forest, because the graph itself was disconnected.
         */
        public int componentCount() {
            return componentCount;
        }

        public boolean spansWholeGraph() {
            return componentCount == 1 && roads.size() == graph.order() - 1;
        }

        /** Union-find lookups performed. */
        public int findCalls() {
            return findCalls;
        }

        /** Cost comparisons performed while sorting. */
        public int comparisons() {
            return comparisons;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "Kruskal: " + roadCount() + " roads, total " + totalCost + ", "
                    + componentCount + " component(s)";
        }
    }

    /** Minimum spanning tree, or spanning forest if the graph is disconnected. */
    public static Result of(Graph graph) {
        long startedAt = System.nanoTime();

        DynamicArray<Road> allRoads = graph.roads();
        int roadCount = allRoads.size();
        double[] costs = new double[roadCount];
        int[] order = new int[roadCount];
        for (int index = 0; index < roadCount; index++) {
            costs[index] = graph.costOf(allRoads.get(index));
            order[index] = index;
        }

        int[] comparisons = new int[1];
        mergeSortByCost(order, new int[roadCount], costs, 0, roadCount - 1, comparisons);

        DisjointSet fragments = new DisjointSet(graph.order());
        DynamicArray<Road> chosen = new DynamicArray<>();
        DynamicArray<Step> steps = new DynamicArray<>();
        double totalCost = 0;

        for (int position = 0; position < roadCount; position++) {
            Road road = allRoads.get(order[position]);
            int fromSlot = graph.slotOf(road.getFromLocationId());
            int toSlot = graph.slotOf(road.getToLocationId());
            double cost = costs[order[position]];

            boolean accepted = fragments.union(fromSlot, toSlot);
            if (accepted) {
                chosen.add(road);
                totalCost += cost;
            }
            steps.add(new Step(road, cost, accepted, fragments.setCount()));

            if (fragments.setCount() == 1) {
                // Every location is connected; remaining roads can only close cycles.
                break;
            }
        }

        return new Result(graph, chosen, steps, totalCost, fragments.setCount(),
                fragments.findCalls(), comparisons[0], System.nanoTime() - startedAt);
    }

    /**
     * Stable bottom-up-recursive merge sort of {@code order} by the cost each index
     * refers to. Ties keep their earlier position, which is what makes the trace
     * reproducible.
     */
    private static void mergeSortByCost(int[] order, int[] scratch, double[] costs, int low,
                                        int high, int[] comparisons) {
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        mergeSortByCost(order, scratch, costs, low, middle, comparisons);
        mergeSortByCost(order, scratch, costs, middle + 1, high, comparisons);

        for (int index = low; index <= high; index++) {
            scratch[index] = order[index];
        }

        int left = low;
        int right = middle + 1;
        for (int target = low; target <= high; target++) {
            if (left > middle) {
                order[target] = scratch[right++];
            } else if (right > high) {
                order[target] = scratch[left++];
            } else {
                comparisons[0]++;
                if (costs[scratch[right]] < costs[scratch[left]]) {
                    order[target] = scratch[right++];
                } else {
                    order[target] = scratch[left++];
                }
            }
        }
    }
}
