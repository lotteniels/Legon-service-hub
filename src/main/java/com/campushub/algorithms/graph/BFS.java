package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.structures.linear.Queue;

// Owner: Graphs and Optimization

/**
 * Breadth-first search over the campus road network.
 *
 * <p>Answers the fewest-roads question, which is not the question {@link Dijkstra}
 * answers. On this data the two genuinely disagree: one long shuttle road beats a
 * chain of short walkways on hop count while losing on travel time.
 *
 * <p>Uses the Linear Structures pod's {@link Queue} for the frontier and plain arrays
 * keyed by {@link Graph#slotOf} for per-location state. Neighbours come back in
 * ascending id order, so discovery order and every reconstructed path are reproducible.
 */
public final class BFS {

    private BFS() {
    }

    /** Distance recorded for a location the search never reached. */
    public static final int UNREACHED = -1;

    /** Outcome of one traversal. */
    public static final class Result {

        private final Graph graph;
        private final int source;
        private final int[] hops;
        private final int[] parent;
        private final int[] visitOrder;
        private final int reachedCount;
        private final int roadsExamined;
        private final int peakQueueSize;
        private final long elapsedNanos;

        Result(Graph graph, int source, int[] hops, int[] parent, int[] visitOrder,
               int reachedCount, int roadsExamined, int peakQueueSize, long elapsedNanos) {
            this.graph = graph;
            this.source = source;
            this.hops = hops;
            this.parent = parent;
            this.visitOrder = visitOrder;
            this.reachedCount = reachedCount;
            this.roadsExamined = roadsExamined;
            this.peakQueueSize = peakQueueSize;
            this.elapsedNanos = elapsedNanos;
        }

        public int source() {
            return source;
        }

        public boolean reached(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot != Graph.NO_SLOT && hops[slot] != UNREACHED;
        }

        /**
         * Roads on the shortest hop path to a location, 0 for the source, or
         * {@link #UNREACHED} if it was never reached.
         */
        public int hopsTo(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot == Graph.NO_SLOT ? UNREACHED : hops[slot];
        }

        /**
         * The location this one was first discovered from, or {@link #UNREACHED} for
         * the source and anything unreached.
         */
        public int parentOf(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot == Graph.NO_SLOT ? UNREACHED : parent[slot];
        }

        /** Locations in the order dequeued: level 0, then level 1, and so on. */
        public int[] visitOrder() {
            return visitOrder.clone();
        }

        /** Every location reached, ascending by id. */
        public int[] reachedLocations() {
            int[] found = new int[reachedCount];
            int next = 0;
            int[] ids = graph.locationIds();
            for (int index = 0; index < ids.length; index++) {
                if (reached(ids[index])) {
                    found[next++] = ids[index];
                }
            }
            return found;
        }

        public int reachedCount() {
            return reachedCount;
        }

        /** The largest hop count seen, the eccentricity of the source. */
        public int maxHops() {
            int furthest = 0;
            for (int slot = 0; slot < hops.length; slot++) {
                if (hops[slot] > furthest) {
                    furthest = hops[slot];
                }
            }
            return furthest;
        }

        /**
         * The fewest-roads route to a location, source first. Empty if unreached.
         */
        public int[] pathTo(int destination) {
            if (!reached(destination)) {
                return new int[0];
            }
            int length = hopsTo(destination) + 1;
            int[] route = new int[length];
            int step = destination;
            for (int position = length - 1; position >= 0; position--) {
                route[position] = step;
                step = parentOf(step);
            }
            return route;
        }

        /** Adjacency entries inspected. */
        public int roadsExamined() {
            return roadsExamined;
        }

        /** Peak frontier length, the traversal's dominant memory cost. */
        public int peakQueueSize() {
            return peakQueueSize;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "BFS from " + source + ": reached " + reachedCount + ", furthest "
                    + maxHops() + " hops, " + roadsExamined + " roads examined";
        }
    }

    /**
     * Traverses everything reachable from {@code source}.
     *
     * @throws IllegalArgumentException if the graph has no such location
     */
    public static Result from(Graph graph, int source) {
        int sourceSlot = graph.slotOf(source);
        if (sourceSlot == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown location: " + source);
        }

        long startedAt = System.nanoTime();
        int count = graph.order();
        int[] hops = new int[count];
        int[] parent = new int[count];
        for (int slot = 0; slot < count; slot++) {
            hops[slot] = UNREACHED;
            parent[slot] = UNREACHED;
        }

        int[] visitOrder = new int[count];
        int visited = 0;
        int roadsExamined = 0;
        int peakQueueSize = 1;

        Queue<Integer> frontier = new Queue<>();
        hops[sourceSlot] = 0;
        frontier.enqueue(source);

        while (!frontier.isEmpty()) {
            int current = frontier.dequeue();
            visitOrder[visited++] = current;

            DynamicArray<Road> incident = graph.roadsFrom(current);
            for (int index = 0; index < incident.size(); index++) {
                roadsExamined++;
                int neighbour = Graph.otherEndpoint(incident.get(index), current);
                int neighbourSlot = graph.slotOf(neighbour);
                if (hops[neighbourSlot] != UNREACHED) {
                    continue;
                }
                hops[neighbourSlot] = hops[graph.slotOf(current)] + 1;
                parent[neighbourSlot] = current;
                frontier.enqueue(neighbour);
                if (frontier.size() > peakQueueSize) {
                    peakQueueSize = frontier.size();
                }
            }
        }

        int[] trimmedOrder = new int[visited];
        for (int index = 0; index < visited; index++) {
            trimmedOrder[index] = visitOrder[index];
        }

        return new Result(graph, source, hops, parent, trimmedOrder, visited, roadsExamined,
                peakQueueSize, System.nanoTime() - startedAt);
    }

    /** The fewest-roads route between two locations, or an empty array if none exists. */
    public static int[] shortestHopPath(Graph graph, int source, int destination) {
        return from(graph, source).pathTo(destination);
    }

    /** True if any sequence of roads joins the two locations. */
    public static boolean isReachable(Graph graph, int source, int destination) {
        return from(graph, source).reached(destination);
    }

    /**
     * Connected components, each ascending by id and ordered by smallest id.
     *
     * <p>On the campus data this is a single component of all 58 locations, the
     * property Dijkstra, Prim and Kruskal all depend on.
     */
    public static int[][] components(Graph graph) {
        int[] ids = graph.locationIds();
        boolean[] assigned = new boolean[graph.order()];
        DynamicArray<int[]> found = new DynamicArray<>();

        for (int index = 0; index < ids.length; index++) {
            if (assigned[graph.slotOf(ids[index])]) {
                continue;
            }
            int[] component = from(graph, ids[index]).reachedLocations();
            for (int member = 0; member < component.length; member++) {
                assigned[graph.slotOf(component[member])] = true;
            }
            found.add(component);
        }

        int[][] components = new int[found.size()][];
        for (int index = 0; index < found.size(); index++) {
            components[index] = found.get(index);
        }
        return components;
    }

    /** True if every location can be reached from every other. */
    public static boolean isConnected(Graph graph) {
        if (graph.order() == 0) {
            return true;
        }
        return from(graph, graph.locationIds()[0]).reachedCount() == graph.order();
    }

    /**
     * Locations grouped by hop distance from {@code source}: index 0 the source, index
     * 1 its direct neighbours, and so on.
     */
    public static int[][] levels(Graph graph, int source) {
        Result traversal = from(graph, source);
        int depth = traversal.maxHops();
        int[] sizes = new int[depth + 1];
        int[] reached = traversal.reachedLocations();
        for (int index = 0; index < reached.length; index++) {
            sizes[traversal.hopsTo(reached[index])]++;
        }

        int[][] levels = new int[depth + 1][];
        for (int level = 0; level <= depth; level++) {
            levels[level] = new int[sizes[level]];
        }
        int[] filled = new int[depth + 1];
        for (int index = 0; index < reached.length; index++) {
            int level = traversal.hopsTo(reached[index]);
            levels[level][filled[level]++] = reached[index];
        }
        return levels;
    }
}
