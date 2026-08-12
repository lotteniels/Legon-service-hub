package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.structures.linear.Stack;

// Owner: Graphs and Optimization

/**
 * Depth-first search over the campus road network.
 *
 * <p>Given in two forms. The recursive version is the textbook shape and the one for
 * the report; the iterative version does the same work on the Linear Structures pod's
 * {@link Stack}, which is what makes it safe on inputs deep enough to overflow the call
 * stack. Both produce the same preorder, so they can be compared directly.
 *
 * <p>DFS is not used for routing - it finds a route, not a short one. Its jobs here are
 * reachability, component labelling, and cycle detection, the last of which establishes
 * that the road network is not a tree and therefore that a spanning tree is worth
 * computing.
 */
public final class DFS {

    private DFS() {
    }

    /** Parent value for the source and for unreached locations. */
    public static final int NONE = -1;

    /** Outcome of one traversal. */
    public static final class Result {

        private final Graph graph;
        private final int source;
        private final int[] preorder;
        private final int[] postorder;
        private final int[] parent;
        private final boolean[] reached;
        private final int roadsExamined;
        private final int maxDepth;
        private final long elapsedNanos;

        Result(Graph graph, int source, int[] preorder, int[] postorder, int[] parent,
               boolean[] reached, int roadsExamined, int maxDepth, long elapsedNanos) {
            this.graph = graph;
            this.source = source;
            this.preorder = preorder;
            this.postorder = postorder;
            this.parent = parent;
            this.reached = reached;
            this.roadsExamined = roadsExamined;
            this.maxDepth = maxDepth;
            this.elapsedNanos = elapsedNanos;
        }

        public int source() {
            return source;
        }

        /** Locations in the order first entered. */
        public int[] preorder() {
            return preorder.clone();
        }

        /** Locations in the order finished, after all their descendants. */
        public int[] postorder() {
            return postorder.clone();
        }

        public boolean reached(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot != Graph.NO_SLOT && reached[slot];
        }

        /**
         * The location this one was first entered from, or {@link #NONE} for the source
         * and anything unreached.
         */
        public int parentOf(int locationId) {
            int slot = graph.slotOf(locationId);
            return slot == Graph.NO_SLOT ? NONE : parent[slot];
        }

        /** Every location reached, ascending by id. */
        public int[] reachedLocations() {
            int[] ids = graph.locationIds();
            int found = 0;
            for (int index = 0; index < ids.length; index++) {
                if (reached(ids[index])) {
                    found++;
                }
            }
            int[] result = new int[found];
            int next = 0;
            for (int index = 0; index < ids.length; index++) {
                if (reached(ids[index])) {
                    result[next++] = ids[index];
                }
            }
            return result;
        }

        public int reachedCount() {
            return preorder.length;
        }

        /**
         * The route the traversal took to a location, source first - valid, but not
         * necessarily short. Empty if unreached.
         */
        public int[] pathTo(int destination) {
            if (!reached(destination)) {
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

        /** Adjacency entries inspected. */
        public int roadsExamined() {
            return roadsExamined;
        }

        /** Deepest point of the traversal tree, the recursion depth the recursive form needs. */
        public int maxDepth() {
            return maxDepth;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "DFS from " + source + ": reached " + reachedCount() + ", max depth "
                    + maxDepth + ", " + roadsExamined + " roads examined";
        }
    }

    /**
     * Depth-first traversal using an explicit stack. Preferred over
     * {@link #recursiveFrom} because it cannot overflow.
     *
     * @throws IllegalArgumentException if the graph has no such location
     */
    public static Result from(Graph graph, int source) {
        int sourceSlot = requireLocation(graph, source);
        long startedAt = System.nanoTime();

        int count = graph.order();
        int[] parent = new int[count];
        int[] depth = new int[count];
        boolean[] reached = new boolean[count];
        int[] preorder = new int[count];
        int[] postorder = new int[count];
        int entered = 0;
        int finished = 0;
        int roadsExamined = 0;
        int maxDepth = 0;

        for (int slot = 0; slot < count; slot++) {
            parent[slot] = NONE;
        }

        // Each frame is a location plus how far through its road list we have gone,
        // which is what lets postorder be recorded without recursion.
        Stack<int[]> stack = new Stack<>();
        reached[sourceSlot] = true;
        preorder[entered++] = source;
        stack.push(new int[] {source, 0});

        while (!stack.isEmpty()) {
            int[] frame = stack.peek();
            int current = frame[0];
            DynamicArray<Road> incident = graph.roadsFrom(current);

            if (frame[1] >= incident.size()) {
                stack.pop();
                postorder[finished++] = current;
                continue;
            }

            int neighbour = Graph.otherEndpoint(incident.get(frame[1]), current);
            frame[1]++;
            roadsExamined++;

            int neighbourSlot = graph.slotOf(neighbour);
            if (reached[neighbourSlot]) {
                continue;
            }

            int nextDepth = depth[graph.slotOf(current)] + 1;
            reached[neighbourSlot] = true;
            parent[neighbourSlot] = current;
            depth[neighbourSlot] = nextDepth;
            if (nextDepth > maxDepth) {
                maxDepth = nextDepth;
            }
            preorder[entered++] = neighbour;
            stack.push(new int[] {neighbour, 0});
        }

        return new Result(graph, source, trim(preorder, entered), trim(postorder, finished),
                parent, reached, roadsExamined, maxDepth, System.nanoTime() - startedAt);
    }

    /**
     * The same traversal expressed recursively, producing identical preorder and
     * postorder to {@link #from}.
     *
     * <p>Recursion depth is bounded by the number of locations, so this is safe on the
     * campus dataset but not on arbitrarily large graphs.
     *
     * @throws IllegalArgumentException if the graph has no such location
     */
    public static Result recursiveFrom(Graph graph, int source) {
        int sourceSlot = requireLocation(graph, source);
        long startedAt = System.nanoTime();

        int count = graph.order();
        int[] parent = new int[count];
        boolean[] reached = new boolean[count];
        int[] preorder = new int[count];
        int[] postorder = new int[count];
        for (int slot = 0; slot < count; slot++) {
            parent[slot] = NONE;
        }

        // counters: 0 entered, 1 finished, 2 roads examined, 3 max depth
        int[] counters = new int[4];
        reached[sourceSlot] = true;
        preorder[counters[0]++] = source;
        visit(graph, source, 1, parent, reached, preorder, postorder, counters);

        return new Result(graph, source, trim(preorder, counters[0]),
                trim(postorder, counters[1]), parent, reached, counters[2], counters[3],
                System.nanoTime() - startedAt);
    }

    private static void visit(Graph graph, int current, int depth, int[] parent,
                              boolean[] reached, int[] preorder, int[] postorder,
                              int[] counters) {
        DynamicArray<Road> incident = graph.roadsFrom(current);
        for (int index = 0; index < incident.size(); index++) {
            counters[2]++;
            int neighbour = Graph.otherEndpoint(incident.get(index), current);
            int neighbourSlot = graph.slotOf(neighbour);
            if (reached[neighbourSlot]) {
                continue;
            }
            reached[neighbourSlot] = true;
            parent[neighbourSlot] = current;
            preorder[counters[0]++] = neighbour;
            if (depth > counters[3]) {
                counters[3] = depth;
            }
            visit(graph, neighbour, depth + 1, parent, reached, preorder, postorder, counters);
        }
        postorder[counters[1]++] = current;
    }

    /** Connected components, each ascending by id and ordered by smallest id. */
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

    /**
     * True if the graph contains a cycle.
     *
     * <p>In an undirected graph a cycle exists exactly when a traversal meets an
     * already-visited location that is not the one it just came from. The road back to
     * the parent is skipped once rather than by identity, so a genuine second road
     * between the same pair would still register - though {@link Graph} collapses those
     * on load, so it cannot arise here.
     */
    public static boolean hasCycle(Graph graph) {
        int[] ids = graph.locationIds();
        boolean[] visited = new boolean[graph.order()];

        for (int index = 0; index < ids.length; index++) {
            int start = ids[index];
            if (visited[graph.slotOf(start)]) {
                continue;
            }
            Stack<int[]> stack = new Stack<>();
            stack.push(new int[] {start, NONE});
            visited[graph.slotOf(start)] = true;

            while (!stack.isEmpty()) {
                int[] frame = stack.pop();
                int current = frame[0];
                int cameFrom = frame[1];
                boolean skippedParentOnce = false;

                DynamicArray<Road> incident = graph.roadsFrom(current);
                for (int road = 0; road < incident.size(); road++) {
                    int neighbour = Graph.otherEndpoint(incident.get(road), current);
                    if (neighbour == cameFrom && !skippedParentOnce) {
                        skippedParentOnce = true;
                        continue;
                    }
                    int neighbourSlot = graph.slotOf(neighbour);
                    if (visited[neighbourSlot]) {
                        return true;
                    }
                    visited[neighbourSlot] = true;
                    stack.push(new int[] {neighbour, current});
                }
            }
        }
        return false;
    }

    /** True if any sequence of roads joins the two locations. */
    public static boolean isReachable(Graph graph, int source, int destination) {
        return from(graph, source).reached(destination);
    }

    private static int requireLocation(Graph graph, int locationId) {
        int slot = graph.slotOf(locationId);
        if (slot == Graph.NO_SLOT) {
            throw new IllegalArgumentException("unknown location: " + locationId);
        }
        return slot;
    }

    private static int[] trim(int[] values, int length) {
        int[] trimmed = new int[length];
        for (int index = 0; index < length; index++) {
            trimmed[index] = values[index];
        }
        return trimmed;
    }
}
