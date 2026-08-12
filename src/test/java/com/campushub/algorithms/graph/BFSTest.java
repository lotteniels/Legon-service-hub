package com.campushub.algorithms.graph;

import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;
import com.campushub.testsupport.GraphFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BFSTest {

    @Test
    public void sourceIsZeroHopsFromItself() {
        BFS.Result traversal = BFS.from(GraphFixtures.path(3), 1);

        assertEquals(0, traversal.hopsTo(1));
        assertEquals(BFS.UNREACHED, traversal.parentOf(1));
        assertArrayEquals(new int[] {1}, traversal.pathTo(1));
    }

    @Test
    public void hopCountsGrowByOnePerRoad() {
        BFS.Result traversal = BFS.from(GraphFixtures.path(5), 1);

        assertEquals(1, traversal.hopsTo(2));
        assertEquals(2, traversal.hopsTo(3));
        assertEquals(4, traversal.hopsTo(5));
        assertEquals(4, traversal.maxHops());
    }

    @Test
    public void choosesTheFewestRoadsEvenWhenThatRouteIsLonger() {
        BFS.Result traversal = BFS.from(GraphFixtures.twoRoutes(), 1);

        assertEquals(1, traversal.hopsTo(4));
        assertArrayEquals(new int[] {1, 4}, traversal.pathTo(4));
    }

    @Test
    public void visitOrderIsBreadthFirstAndAscendingWithinALevel() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 5, 10, 1, 1.0);
        graph.addRoad(1, 3, 10, 1, 1.0);
        graph.addRoad(3, 9, 10, 1, 1.0);
        graph.addRoad(5, 7, 10, 1, 1.0);

        assertArrayEquals(new int[] {1, 3, 5, 9, 7}, BFS.from(graph, 1).visitOrder());
    }

    @Test
    public void levelsGroupLocationsByDistanceInRoads() {
        int[][] levels = BFS.levels(GraphFixtures.path(4), 1);

        assertEquals(4, levels.length);
        assertArrayEquals(new int[] {1}, levels[0]);
        assertArrayEquals(new int[] {2}, levels[1]);
        assertArrayEquals(new int[] {3}, levels[2]);
        assertArrayEquals(new int[] {4}, levels[3]);
    }

    @Test
    public void unreachableLocationsAreReportedNotGuessed() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addLocation(9, "Detached");

        BFS.Result traversal = BFS.from(graph, 1);

        assertFalse(traversal.reached(9));
        assertEquals(BFS.UNREACHED, traversal.hopsTo(9));
        assertEquals(0, traversal.pathTo(9).length);
        assertEquals(2, traversal.reachedCount());
    }

    @Test
    public void pathsAreSymmetricOnAnUndirectedGraph() {
        Graph graph = GraphFixtures.path(5);

        assertArrayEquals(new int[] {1, 2, 3, 4, 5}, BFS.shortestHopPath(graph, 1, 5));
        assertArrayEquals(new int[] {5, 4, 3, 2, 1}, BFS.shortestHopPath(graph, 5, 1));
    }

    @Test
    public void reachabilityIsMutual() {
        Graph graph = GraphFixtures.path(4);

        assertTrue(BFS.isReachable(graph, 1, 4));
        assertTrue(BFS.isReachable(graph, 4, 1));
    }

    @Test
    public void componentsSplitADisconnectedGraphDeterministically() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(5, 6, 10, 1, 1.0);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addRoad(2, 3, 10, 1, 1.0);
        graph.addLocation(9, "Alone");

        int[][] components = BFS.components(graph);

        assertEquals(3, components.length);
        assertArrayEquals(new int[] {1, 2, 3}, components[0]);
        assertArrayEquals(new int[] {5, 6}, components[1]);
        assertArrayEquals(new int[] {9}, components[2]);
        assertFalse(BFS.isConnected(graph));
    }

    @Test
    public void aSingleComponentIsReportedAsConnected() {
        assertTrue(BFS.isConnected(GraphFixtures.path(6)));
        assertEquals(1, BFS.components(GraphFixtures.path(6)).length);
    }

    @Test
    public void anEmptyGraphIsTriviallyConnected() {
        Graph empty = new Graph(WeightMode.DISTANCE);

        assertTrue(BFS.isConnected(empty));
        assertEquals(0, BFS.components(empty).length);
    }

    @Test
    public void unknownSourceIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> BFS.from(GraphFixtures.path(3), 99));
    }

    @Test
    public void instrumentationCountsWorkDone() {
        BFS.Result traversal = BFS.from(GraphFixtures.path(5), 1);

        // Each of the 4 roads is examined from both endpoints.
        assertEquals(8, traversal.roadsExamined());
        assertTrue(traversal.peakQueueSize() >= 1);
        assertTrue(traversal.elapsedNanos() > 0);
    }

    @Test
    public void everyCampusLocationIsReachableFromAnyOther() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        assertTrue(BFS.isConnected(graph));
        assertEquals(1, BFS.components(graph).length);
        assertEquals(58, BFS.from(graph, 35).reachedCount());
    }

    @Test
    public void theCampusNetworkIsShallowInHopTerms() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        // University Square touches 33 of the 58 locations directly.
        assertEquals(33, graph.degree(35));
        assertTrue(BFS.from(graph, 35).maxHops() <= 6,
                "expected a shallow network, furthest was " + BFS.from(graph, 35).maxHops());
    }

    @Test
    public void hopPathsOnRealDataFollowActualRoads() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        int[] route = BFS.shortestHopPath(graph, 8, 58);

        assertEquals(8, route[0]);
        assertEquals(58, route[route.length - 1]);
        for (int step = 0; step < route.length - 1; step++) {
            assertTrue(graph.hasRoad(route[step], route[step + 1]),
                    "no road between " + route[step] + " and " + route[step + 1]);
        }
    }
}
