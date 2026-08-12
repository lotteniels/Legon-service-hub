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

public class DijkstraTest {

    @Test
    public void sourceCostsNothingToReach() {
        Dijkstra.Result search = Dijkstra.from(GraphFixtures.path(3), 1);

        assertEquals(0.0, search.costTo(1));
        assertEquals(Dijkstra.NONE, search.parentOf(1));
        assertArrayEquals(new int[] {1}, search.pathTo(1));
    }

    @Test
    public void costsAccumulateAlongAPath() {
        Dijkstra.Result search = Dijkstra.from(GraphFixtures.path(5), 1);

        assertEquals(10.0, search.costTo(2));
        assertEquals(20.0, search.costTo(3));
        assertEquals(40.0, search.costTo(5));
        assertEquals(40.0, search.maxCost());
    }

    @Test
    public void prefersTheCheapRouteWhereBfsWouldTakeTheShortOne() {
        Graph graph = GraphFixtures.twoRoutes();

        Dijkstra.Result search = Dijkstra.from(graph, 1);

        assertEquals(30.0, search.costTo(4), "three 10m roads beat one 1000m road");
        assertArrayEquals(new int[] {1, 2, 3, 4}, search.pathTo(4));
        // BFS disagrees, and that disagreement is the point of having both.
        assertArrayEquals(new int[] {1, 4}, BFS.shortestHopPath(graph, 1, 4));
    }

    @Test
    public void weightModeChangesWhichRouteWins() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 100, 30, 1.0);
        graph.addRoad(1, 3, 500, 2, 1.0);
        graph.addRoad(3, 2, 500, 2, 1.0);

        // By distance the direct road wins; by time the detour does.
        assertEquals(100.0, Dijkstra.from(graph, 1).costTo(2));
        assertEquals(4.0, Dijkstra.from(graph.withWeightMode(WeightMode.TIME), 1).costTo(2));
    }

    @Test
    public void unreachableLocationsAreInfiniteWithNoPath() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addLocation(9, "Detached");

        Dijkstra.Result search = Dijkstra.from(graph, 1);

        assertEquals(Dijkstra.UNREACHABLE, search.costTo(9));
        assertFalse(search.isReachable(9));
        assertEquals(0, search.pathTo(9).length);
        assertEquals(2, search.reachedCount());
    }

    @Test
    public void settleOrderIsNonDecreasingInCost() {
        Dijkstra.Result search = Dijkstra.from(GraphFixtures.realGraphOrSkip(), 1);

        int[] order = search.settleOrder();
        double previous = -1;
        for (int index = 0; index < order.length; index++) {
            double cost = search.costTo(order[index]);
            assertTrue(cost >= previous,
                    "settled " + order[index] + " at " + cost + " after " + previous);
            previous = cost;
        }
        assertEquals(58, order.length);
    }

    @Test
    public void earlyExitAgreesWithTheFullSearchOnTheTargetCost() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        Dijkstra.Result full = Dijkstra.from(graph, 1);
        Dijkstra.Result stopped = Dijkstra.to(graph, 1, 58);

        assertEquals(full.costTo(58), stopped.costTo(58));
        assertArrayEquals(full.pathTo(58), stopped.pathTo(58));
        assertTrue(stopped.settleOrder().length <= full.settleOrder().length);
    }

    @Test
    public void pathsFollowActualRoadsAndSumToTheReportedCost() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        Dijkstra.Result search = Dijkstra.from(graph, 1);

        int[] route = search.pathTo(58);
        double summed = 0;
        for (int step = 0; step < route.length - 1; step++) {
            assertTrue(graph.hasRoad(route[step], route[step + 1]),
                    "no road between " + route[step] + " and " + route[step + 1]);
            summed += graph.cost(route[step], route[step + 1]);
        }
        assertEquals(search.costTo(58), summed, 1e-9);
    }

    @Test
    public void costsAreSymmetricOnAnUndirectedGraph() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        assertEquals(Dijkstra.from(graph, 8).costTo(58),
                Dijkstra.from(graph, 58).costTo(8), 1e-9);
    }

    @Test
    public void everyCampusLocationIsReachable() {
        Dijkstra.Result search = Dijkstra.from(GraphFixtures.realGraphOrSkip(), 35);

        assertEquals(58, search.reachedCount());
    }

    @Test
    public void singleLocationGraphIsHandled() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addLocation(1, "Alone");

        Dijkstra.Result search = Dijkstra.from(graph, 1);

        assertEquals(0.0, search.costTo(1));
        assertEquals(1, search.reachedCount());
        assertEquals(0, search.roadsExamined());
    }

    @Test
    public void unknownLocationsAreRejected() {
        Graph graph = GraphFixtures.path(3);

        assertThrows(IllegalArgumentException.class, () -> Dijkstra.from(graph, 99));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.to(graph, 1, 99));
        assertThrows(IllegalArgumentException.class, () -> Dijkstra.to(graph, 99, 1));
    }

    @Test
    public void allPairsCostsMatchIndividualSearches() {
        Graph graph = GraphFixtures.path(5);
        double[][] costs = Dijkstra.allPairsCosts(graph);

        int[] ids = graph.locationIds();
        for (int from = 0; from < ids.length; from++) {
            Dijkstra.Result search = Dijkstra.from(graph, ids[from]);
            for (int to = 0; to < ids.length; to++) {
                assertEquals(search.costTo(ids[to]),
                        costs[graph.slotOf(ids[from])][graph.slotOf(ids[to])]);
            }
        }
    }

    @Test
    public void instrumentationRecordsWork() {
        Dijkstra.Result search = Dijkstra.from(GraphFixtures.path(5), 1);

        assertEquals(8, search.roadsExamined());
        assertEquals(4, search.improvements());
        assertTrue(search.elapsedNanos() > 0);
    }

    @Test
    public void repeatedSearchesGiveIdenticalResults() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        Dijkstra.Result first = Dijkstra.from(graph, 1);
        Dijkstra.Result second = Dijkstra.from(graph, 1);

        assertArrayEquals(first.settleOrder(), second.settleOrder());
        assertArrayEquals(first.pathTo(58), second.pathTo(58));
        assertEquals(first.costTo(58), second.costTo(58));
    }
}
