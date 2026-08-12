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

public class DFSTest {

    private static Graph triangle() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addRoad(2, 3, 10, 1, 1.0);
        graph.addRoad(1, 3, 10, 1, 1.0);
        return graph;
    }

    @Test
    public void goesDeepBeforeWide() {
        // From 1 the whole 2-subtree is taken before 5 is touched.
        assertArrayEquals(new int[] {1, 2, 3, 4, 5}, DFS.from(GraphFixtures.tree(), 1).preorder());
    }

    @Test
    public void postorderFinishesChildrenBeforeParents() {
        assertArrayEquals(new int[] {3, 4, 2, 5, 1},
                DFS.from(GraphFixtures.tree(), 1).postorder());
    }

    @Test
    public void iterativeAndRecursiveFormsAgree() {
        Graph graph = GraphFixtures.tree();

        DFS.Result iterative = DFS.from(graph, 1);
        DFS.Result recursive = DFS.recursiveFrom(graph, 1);

        assertArrayEquals(iterative.preorder(), recursive.preorder());
        assertArrayEquals(iterative.postorder(), recursive.postorder());
        assertEquals(iterative.maxDepth(), recursive.maxDepth());
        assertEquals(iterative.roadsExamined(), recursive.roadsExamined());

        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index++) {
            assertEquals(iterative.parentOf(ids[index]), recursive.parentOf(ids[index]),
                    "parents disagree for location " + ids[index]);
        }
    }

    @Test
    public void bothFormsAgreeOnTheRealCampusNetwork() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        DFS.Result iterative = DFS.from(graph, 1);
        DFS.Result recursive = DFS.recursiveFrom(graph, 1);

        assertEquals(58, iterative.reachedCount());
        assertArrayEquals(iterative.preorder(), recursive.preorder());
        assertArrayEquals(iterative.postorder(), recursive.postorder());
    }

    @Test
    public void parentTreeRecordsHowEachLocationWasEntered() {
        DFS.Result traversal = DFS.from(GraphFixtures.tree(), 1);

        assertEquals(DFS.NONE, traversal.parentOf(1));
        assertEquals(1, traversal.parentOf(2));
        assertEquals(2, traversal.parentOf(3));
        assertEquals(1, traversal.parentOf(5));
    }

    @Test
    public void maxDepthMatchesTheDeepestBranch() {
        assertEquals(2, DFS.from(GraphFixtures.tree(), 1).maxDepth());
        assertEquals(5, DFS.from(GraphFixtures.path(6), 1).maxDepth());
    }

    @Test
    public void pathToGivesAValidRouteEvenIfNotTheShortest() {
        Graph graph = GraphFixtures.tree();
        int[] route = DFS.from(graph, 1).pathTo(4);

        assertEquals(1, route[0]);
        assertEquals(4, route[route.length - 1]);
        for (int step = 0; step < route.length - 1; step++) {
            assertTrue(graph.hasRoad(route[step], route[step + 1]));
        }
    }

    @Test
    public void unreachableLocationsAreReported() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addLocation(9, "Detached");

        DFS.Result traversal = DFS.from(graph, 1);

        assertFalse(traversal.reached(9));
        assertEquals(DFS.NONE, traversal.parentOf(9));
        assertEquals(0, traversal.pathTo(9).length);
    }

    @Test
    public void unknownSourceIsRejectedByBothForms() {
        Graph graph = GraphFixtures.tree();

        assertThrows(IllegalArgumentException.class, () -> DFS.from(graph, 99));
        assertThrows(IllegalArgumentException.class, () -> DFS.recursiveFrom(graph, 99));
    }

    @Test
    public void isolatedSourceReachesOnlyItself() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addLocation(1, "Alone");

        DFS.Result traversal = DFS.from(graph, 1);

        assertArrayEquals(new int[] {1}, traversal.preorder());
        assertArrayEquals(new int[] {1}, traversal.postorder());
        assertEquals(0, traversal.maxDepth());
        assertEquals(0, traversal.roadsExamined());
    }

    @Test
    public void aTreeHasNoCycle() {
        assertFalse(DFS.hasCycle(GraphFixtures.tree()));
    }

    @Test
    public void aPathHasNoCycle() {
        assertFalse(DFS.hasCycle(GraphFixtures.path(8)));
    }

    @Test
    public void aStarHasNoCycle() {
        Graph star = new Graph(WeightMode.DISTANCE);
        for (int leaf = 2; leaf <= 9; leaf++) {
            star.addRoad(1, leaf, 10, 1, 1.0);
        }
        assertFalse(DFS.hasCycle(star));
    }

    @Test
    public void aTriangleHasACycle() {
        assertTrue(DFS.hasCycle(triangle()));
    }

    @Test
    public void aSquareHasACycle() {
        Graph square = new Graph(WeightMode.DISTANCE);
        square.addRoad(1, 2, 10, 1, 1.0);
        square.addRoad(2, 3, 10, 1, 1.0);
        square.addRoad(3, 4, 10, 1, 1.0);
        square.addRoad(4, 1, 10, 1, 1.0);

        assertTrue(DFS.hasCycle(square));
    }

    @Test
    public void aCycleIsFoundEvenInASecondComponent() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addRoad(7, 8, 10, 1, 1.0);
        graph.addRoad(8, 9, 10, 1, 1.0);
        graph.addRoad(9, 7, 10, 1, 1.0);

        assertTrue(DFS.hasCycle(graph));
    }

    @Test
    public void anEmptyGraphHasNoCycle() {
        assertFalse(DFS.hasCycle(new Graph(WeightMode.DISTANCE)));
    }

    @Test
    public void theCampusNetworkIsNotATreeWhichIsWhyAnMstIsMeaningful() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        assertTrue(DFS.hasCycle(graph));
        // 117 roads over 58 locations: 60 more than a spanning tree needs.
        assertTrue(graph.size() > graph.order() - 1);
    }

    @Test
    public void componentsMatchWhatBfsFinds() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(5, 6, 10, 1, 1.0);
        graph.addRoad(1, 2, 10, 1, 1.0);
        graph.addLocation(9, "Alone");

        int[][] byDfs = DFS.components(graph);
        int[][] byBfs = BFS.components(graph);

        assertEquals(byBfs.length, byDfs.length);
        for (int index = 0; index < byDfs.length; index++) {
            assertArrayEquals(byBfs[index], byDfs[index]);
        }
        assertArrayEquals(new int[] {1, 2}, byDfs[0]);
        assertArrayEquals(new int[] {5, 6}, byDfs[1]);
        assertArrayEquals(new int[] {9}, byDfs[2]);
    }

    @Test
    public void componentsMatchBfsOnTheRealNetwork() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        int[][] byDfs = DFS.components(graph);
        int[][] byBfs = BFS.components(graph);

        assertEquals(1, byDfs.length);
        assertArrayEquals(byBfs[0], byDfs[0]);
    }

    @Test
    public void reachabilityMatchesBfs() {
        Graph graph = GraphFixtures.path(6);

        assertEquals(BFS.isReachable(graph, 1, 6), DFS.isReachable(graph, 1, 6));
        assertTrue(DFS.isReachable(graph, 1, 6));
    }
}
