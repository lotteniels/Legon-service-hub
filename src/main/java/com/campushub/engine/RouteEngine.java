package com.campushub.engine;

import com.campushub.algorithms.graph.BFS;
import com.campushub.algorithms.graph.Dijkstra;
import com.campushub.algorithms.graph.Kruskal;
import com.campushub.algorithms.graph.Prim;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;

import java.nio.file.Path;

/**
 * Routing questions for the CLI, backed by the real road network.
 *
 * <p>The graph is loaded once on first use and reused, because loading it is far more
 * expensive than the searches run over it.
 */
public class RouteEngine {

    private static final Path SEED_DATA = Path.of("database", "seed-data");

    private final Path seedDirectory;
    private final WeightMode weightMode;
    private Graph graph;

    /** Routes over the seed data, costed by condition-adjusted travel time. */
    public RouteEngine() {
        this(SEED_DATA, WeightMode.TIME_ADJUSTED);
    }

    public RouteEngine(Path seedDirectory, WeightMode weightMode) {
        this.seedDirectory = seedDirectory;
        this.weightMode = weightMode;
    }

    /** The loaded road network. */
    public Graph graph() {
        if (graph == null) {
            graph = Graph.fromSeedData(seedDirectory, weightMode);
        }
        return graph;
    }

    /**
     * The cheapest route between two locations, as a line for the CLI. Reports plainly
     * when a location is unknown or no route exists rather than inventing a path.
     */
    public String calculateShortestPath(int sourceId, int destinationId) {
        Graph roads = graph();
        if (!roads.hasLocation(sourceId)) {
            return "Unknown location: " + sourceId;
        }
        if (!roads.hasLocation(destinationId)) {
            return "Unknown location: " + destinationId;
        }

        Dijkstra.Result search = Dijkstra.to(roads, sourceId, destinationId);
        if (!search.isReachable(destinationId)) {
            return "No route from " + roads.nameOf(sourceId) + " to "
                    + roads.nameOf(destinationId);
        }

        int[] route = search.pathTo(destinationId);
        StringBuilder line = new StringBuilder();
        for (int step = 0; step < route.length; step++) {
            if (step > 0) {
                line.append(" -> ");
            }
            line.append(roads.nameOf(route[step]));
        }
        line.append(String.format(" | %.2f %s over %d road(s)", search.costTo(destinationId),
                unitOf(weightMode), route.length - 1));
        return line.toString();
    }

    /** The fewest-roads route, which often differs from the cheapest one. */
    public String calculateFewestRoads(int sourceId, int destinationId) {
        Graph roads = graph();
        if (!roads.hasLocation(sourceId) || !roads.hasLocation(destinationId)) {
            return "Unknown location";
        }
        int[] route = BFS.shortestHopPath(roads, sourceId, destinationId);
        if (route.length == 0) {
            return "No route from " + roads.nameOf(sourceId) + " to "
                    + roads.nameOf(destinationId);
        }
        StringBuilder line = new StringBuilder();
        for (int step = 0; step < route.length; step++) {
            if (step > 0) {
                line.append(" -> ");
            }
            line.append(roads.nameOf(route[step]));
        }
        line.append(" | ").append(route.length - 1).append(" road(s)");
        return line.toString();
    }

    /**
     * The cheapest set of roads keeping every location connected, computed both ways as
     * a cross-check.
     */
    public String maintenanceNetwork() {
        Graph roads = graph();
        if (roads.order() == 0) {
            return "No road network loaded";
        }
        Prim.Result prim = Prim.of(roads);
        Kruskal.Result kruskal = Kruskal.of(roads);
        return String.format(
                "Minimum spanning tree: %d roads, %.2f %s total (Prim and Kruskal %s)",
                kruskal.roadCount(), kruskal.totalCost(), unitOf(weightMode),
                Math.abs(prim.totalCost() - kruskal.totalCost()) < 1e-9 ? "agree" : "DISAGREE");
    }

    /** One-line description of the loaded network. */
    public String networkSummary() {
        return graph().summary();
    }

    private static String unitOf(WeightMode mode) {
        return mode == WeightMode.DISTANCE ? "m" : "min";
    }
}
