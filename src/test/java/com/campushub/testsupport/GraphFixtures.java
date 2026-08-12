package com.campushub.testsupport;

import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Shared fixtures for the Graphs and Optimization tests.
 *
 * <p>Correctness is pinned by the hand-built graphs here. The real campus network is
 * used only for checks that are about the dataset itself, and those go through
 * {@link #realGraphOrSkip()} so they skip rather than fail if the Database pod's CSVs
 * are ever empty on a branch.
 */
public final class GraphFixtures {

    public static final Path SEED_DATA = Path.of("database", "seed-data");

    /** Default cost function for routing: travel time scaled by road condition. */
    public static final WeightMode DEFAULT_MODE = WeightMode.TIME_ADJUSTED;

    private GraphFixtures() {
    }

    public static boolean seedDataAvailable() {
        try {
            return Files.size(SEED_DATA.resolve("roads.csv")) > 0
                    && Files.size(SEED_DATA.resolve("locations.csv")) > 0;
        } catch (IOException absent) {
            return false;
        }
    }

    public static Graph realGraphOrSkip() {
        return realGraphOrSkip(DEFAULT_MODE);
    }

    public static Graph realGraphOrSkip(WeightMode mode) {
        Assumptions.assumeTrue(seedDataAvailable(),
                "seed data not populated on this branch - skipping real-data check");
        return Graph.fromSeedData(SEED_DATA, mode);
    }

    /** A path of {@code locations} locations, ids 1..n, each road 10 m and 1 minute. */
    public static Graph path(int locations) {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addLocation(1, "Location 1");
        for (int id = 1; id < locations; id++) {
            graph.addRoad(id, id + 1, 10, 1, 1.0);
        }
        return graph;
    }

    /**
     * Two routes from 1 to 4, so hop count and cost disagree:
     *
     * <pre>
     *   1 --1000m-- 4                     1 hop,  1000 m
     *   1 --10m-- 2 --10m-- 3 --10m-- 4   3 hops,   30 m
     * </pre>
     */
    public static Graph twoRoutes() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 4, 1000, 20, 1.0);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addRoad(2, 3, 10, 1, 1.0);
        graph.addRoad(3, 4, 10, 1, 1.0);
        return graph;
    }

    /**
     * A branching tree, so preorder and postorder differ visibly:
     *
     * <pre>
     *        1
     *      /   \
     *     2     5
     *    / \
     *   3   4
     * </pre>
     */
    public static Graph tree() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addRoad(1, 5, 10, 1, 1.0);
        graph.addRoad(2, 3, 10, 1, 1.0);
        graph.addRoad(2, 4, 10, 1, 1.0);
        return graph;
    }
}
