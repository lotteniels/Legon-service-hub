package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

// Owner: Graphs and Optimization

/**
 * Dijkstra's shortest-path search over the campus road network.
 *
 * <p>Answers the cheapest-route question under the graph's weight mode, which on this
 * data differs from {@link BFS}'s fewest-roads answer. Every road weight is strictly
 * positive - {@link Graph#addRoad} enforces it - so Dijkstra's assumption holds and no
 * Bellman-Ford fallback is needed.
 *
 * <p>Uses {@link CostHeap}, an indexed double-keyed heap, so tentative costs are
 * lowered in place rather than pushed as duplicates. Locations therefore settle in
 * exactly the textbook order, which is what makes the trace table reproducible.
 */
public final class Dijkstra {

    private Dijkstra() {
    }

    /** Cost recorded for a location no route reaches. */
    public static final double UNREACHABLE = Double.POSITIVE_INFINITY;

    /** Parent value for the source and for unreachable locations. */
    public static final int NONE = -1;

    /** Outcome of one search. */
    public static final class Result {

        private final Graph graph;
        private final int source;
        private final double[] cost;
        private final int[] parent;
        private final int[] settleOrder;
        private final int roadsExamined;
        private final int improvements;
        private final int heapComparisons;
        private final long elapsedNanos;

        Result(Graph graph, int source, double[] cost, int[] parent, int[] settleOrder,
               int roadsExamined, int improvements, int heapComparisons, long elapsedNanos) {
            this.graph = graph;
            this.source = source;
            this.cost = cost;
            this.parent = parent;
            this.settleOrder = settleOrder;
            this.roadsExamined = roadsExamined;
            this.improvements = improvements;
            this.heapComparisons = heapComparisons;
            this.elapsedNanos = elapsedNanos;
        }

        public int source() {
            return source;
        }

        /**
         * Cheapest cost from the source, 0 for the source itself, or
         * {@link #UNREACHABLE} if no route exists.
         */
        public double costTo(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot == Graph.NO_SLOT ? UNREACHABLE : cost[slot];
        }

        public boolean isReachable(int locationId) {
            return costTo(locationId) != UNREACHABLE;
        }

        /**
         * The location preceding this one on its cheapest route, or {@link #NONE} for
         * the source and anything unreachable.
         */
        public int parentOf(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot == Graph.NO_SLOT ? NONE : parent[slot];
        }

        /**
         * The cheapest route to a location, source first. Empty if unreachable.
         */
        public int[] pathTo(int destination) {
            if (!isReachable(destination)) {
                return new int[0];
            }
            int length = 0;
            for (int step = destination; step != NONE; step = parentOf(step)) {
                length++;
                if (step == source) {
                    break;
                }
            }
            int[] route = new int[length];
            int step = destination;
            for (int position = length - 1; position >= 0; position--) {
                route[position] = step;
                step = parentOf(step);
            }
            return route;
        }

        /**
         * Locations in the order they were settled - the column the trace table walks
         * down.
         */
        public int[] settleOrder() {
            return settleOrder.clone();
        }

        /** How many locations were reachable, including the source. */
        public int reachedCount() {
            int reached = 0;
            for (int slot = 0; slot < cost.length; slot++) {
                if (cost[slot] != UNREACHABLE) {
                    reached++;
                }
            }
            return reached;
        }

        /** The costliest reachable location's cost. */
        public double maxCost() {
            double furthest = 0;
            for (int slot = 0; slot < cost.length; slot++) {
                if (cost[slot] != UNREACHABLE && cost[slot] > furthest) {
                    furthest = cost[slot];
                }
            }
            return furthest;
        }

        /** Adjacency entries inspected. */
        public int roadsExamined() {
            return roadsExamined;
        }

        /** How many times a tentative cost was lowered. */
        public int improvements() {
            return improvements;
        }

        /** Cost comparisons inside the heap. */
        public int heapComparisons() {
            return heapComparisons;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "Dijkstra from " + source + ": reached " + reachedCount() + ", costliest "
                    + maxCost() + ", " + roadsExamined + " roads examined";
        }
    }

    /**
     * Cheapest routes from {@code source} to every reachable location.
     *
     * @throws IllegalArgumentException if the graph has no such location
     */
    public static Result from(Graph graph, int source) {
        return search(graph, source, NONE);
    }

    /**
     * As {@link #from}, stopping as soon as {@code destination} is settled.
     *
     * <p>Costs for locations settled before it are final and correct; anything settled
     * later is left unexplored. Useful for a single route query, where finishing the
     * whole network is wasted work.
     *
     * @throws IllegalArgumentException if either location is unknown
     */
    public static Result to(Graph graph, int source, int destination) {
        if (graph.slotOf(destination) == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown location: " + destination);
        }
        return search(graph, source, destination);
    }

    private static Result search(Graph graph, int source, int stopAt) {
        int sourceSlot = graph.slotOf(source);
        if (sourceSlot == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown location: " + source);
        }

        long startedAt = System.nanoTime();
        int count = graph.order();
        double[] cost = new double[count];
        int[] parent = new int[count];
        boolean[] settled = new boolean[count];
        int[] settleOrder = new int[count];
        int settledCount = 0;
        int roadsExamined = 0;
        int improvements = 0;

        for (int slot = 0; slot < count; slot++) {
            cost[slot] = UNREACHABLE;
            parent[slot] = NONE;
        }

        CostHeap frontier = new CostHeap(count);
        cost[sourceSlot] = 0;
        frontier.insert(sourceSlot, 0);

        while (!frontier.isEmpty()) {
            int slot = frontier.removeMin();
            settled[slot] = true;
            int current = graph.idAt(slot);
            settleOrder[settledCount++] = current;

            if (current == stopAt) {
                break;
            }

            DynamicArray<Road> incident = graph.roadsFrom(current);
            for (int index = 0; index < incident.size(); index++) {
                roadsExamined++;
                Road road = incident.get(index);
                int neighbour = Graph.otherEndpoint(road, current);
                int neighbourSlot = graph.slotOf(neighbour);
                if (settled[neighbourSlot]) {
                    continue;
                }
                double throughCurrent = cost[slot] + graph.costOf(road);
                if (throughCurrent < cost[neighbourSlot]) {
                    cost[neighbourSlot] = throughCurrent;
                    parent[neighbourSlot] = current;
                    frontier.decreaseCost(neighbourSlot, throughCurrent);
                    improvements++;
                }
            }
        }

        int[] trimmedOrder = new int[settledCount];
        for (int index = 0; index < settledCount; index++) {
            trimmedOrder[index] = settleOrder[index];
        }

        return new Result(graph, source, cost, parent, trimmedOrder, roadsExamined, improvements,
                frontier.comparisons(), System.nanoTime() - startedAt);
    }

    /**
     * Cheapest cost from every location to every other, as a matrix keyed by
     * {@link Graph#slotOf}. Runs one search per location.
     */
    public static double[][] allPairsCosts(Graph graph) {
        int count = graph.order();
        double[][] costs = new double[count][];
        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index++) {
            Result search = from(graph, ids[index]);
            int row = graph.slotOf(ids[index]);
            costs[row] = new double[count];
            for (int column = 0; column < count; column++) {
                costs[row][column] = search.costTo(graph.idAt(column));
            }
        }
        return costs;
    }
}
