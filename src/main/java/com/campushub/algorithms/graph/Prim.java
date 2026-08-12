package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

// Owner: Graphs and Optimization

/**
 * Prim's minimum spanning tree, grown outward from one location.
 *
 * <p>The cheapest set of roads keeping every location connected - the answer to "which
 * roads must stay maintained", as opposed to Dijkstra's "how do I get from A to B".
 *
 * <p>Grows a single tree, repeatedly taking the cheapest road leaving it. Uses
 * {@link CostHeap} keyed on each outside location's cheapest road into the tree, so the
 * per-location best is lowered in place rather than re-pushed.
 *
 * <p>On a disconnected graph this spans only the component holding the root. Use
 * {@link Kruskal} for a spanning forest of the whole graph, or check
 * {@link Result#spansWholeGraph}.
 */
public final class Prim {

    private Prim() {
    }

    /** Outcome of one run. */
    public static final class Result {

        private final Graph graph;
        private final int root;
        private final DynamicArray<Road> roads;
        private final double totalCost;
        private final int locationsSpanned;
        private final int roadsExamined;
        private final int heapComparisons;
        private final long elapsedNanos;

        Result(Graph graph, int root, DynamicArray<Road> roads, double totalCost,
               int locationsSpanned, int roadsExamined, int heapComparisons, long elapsedNanos) {
            this.graph = graph;
            this.root = root;
            this.roads = roads;
            this.totalCost = totalCost;
            this.locationsSpanned = locationsSpanned;
            this.roadsExamined = roadsExamined;
            this.heapComparisons = heapComparisons;
            this.elapsedNanos = elapsedNanos;
        }

        public int root() {
            return root;
        }

        /** The chosen roads, in the order the tree took them. */
        public DynamicArray<Road> roads() {
            return roads;
        }

        /** Summed cost of the chosen roads under the graph's weight mode. */
        public double totalCost() {
            return totalCost;
        }

        public int roadCount() {
            return roads.size();
        }

        /** How many locations the tree reaches, including the root. */
        public int locationsSpanned() {
            return locationsSpanned;
        }

        /** True if the tree reaches every location, so it is a spanning tree. */
        public boolean spansWholeGraph() {
            return locationsSpanned == graph.order();
        }

        public int roadsExamined() {
            return roadsExamined;
        }

        public int heapComparisons() {
            return heapComparisons;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "Prim from " + root + ": " + roadCount() + " roads, total " + totalCost
                    + ", spanning " + locationsSpanned + " of " + graph.order() + " locations";
        }
    }

    /**
     * Minimum spanning tree of the component holding {@code root}.
     *
     * @throws IllegalArgumentException if the graph has no such location
     */
    public static Result from(Graph graph, int root) {
        int rootSlot = graph.slotOf(root);
        if (rootSlot == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown location: " + root);
        }

        long startedAt = System.nanoTime();
        int count = graph.order();
        boolean[] inTree = new boolean[count];
        Road[] cheapestRoad = new Road[count];
        int roadsExamined = 0;

        DynamicArray<Road> chosen = new DynamicArray<>();
        double totalCost = 0;
        int spanned = 0;

        CostHeap frontier = new CostHeap(count);
        frontier.insert(rootSlot, 0);

        while (!frontier.isEmpty()) {
            int slot = frontier.removeMin();
            inTree[slot] = true;
            spanned++;

            Road arrivedBy = cheapestRoad[slot];
            if (arrivedBy != null) {
                chosen.add(arrivedBy);
                totalCost += graph.costOf(arrivedBy);
            }

            int current = graph.idAt(slot);
            DynamicArray<Road> incident = graph.roadsFrom(current);
            for (int index = 0; index < incident.size(); index++) {
                roadsExamined++;
                Road road = incident.get(index);
                int neighbourSlot = graph.slotOf(Graph.otherEndpoint(road, current));
                if (inTree[neighbourSlot]) {
                    continue;
                }
                double roadCost = graph.costOf(road);
                if (!frontier.contains(neighbourSlot) || roadCost < frontier.costOf(neighbourSlot)) {
                    cheapestRoad[neighbourSlot] = road;
                    frontier.decreaseCost(neighbourSlot, roadCost);
                }
            }
        }

        return new Result(graph, root, chosen, totalCost, spanned, roadsExamined,
                frontier.comparisons(), System.nanoTime() - startedAt);
    }

    /**
     * Minimum spanning tree rooted at the lowest location id.
     *
     * @throws IllegalArgumentException if the graph has no locations
     */
    public static Result of(Graph graph) {
        if (graph.order() == 0) {
            throw new IllegalArgumentException("graph has no locations");
        }
        return from(graph, graph.locationIds()[0]);
    }
}
