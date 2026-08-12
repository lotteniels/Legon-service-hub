package com.campushub.algorithms.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.testsupport.GraphFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Prim and Kruskal are tested together because their agreement is the strongest check. */
public class MinimumSpanningTreeTest {

    /**
     * A square with one diagonal, where the cheapest tree is unambiguous:
     *
     * <pre>
     *   1 -1- 2      1-3 costs 10, so the tree is 1-2, 2-3, 3-4 at total 3
     *   |     |
     *   4 -1- 3
     * </pre>
     */
    private static Graph square() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 1, 1, 1.0);
        graph.addRoad(2, 3, 1, 1, 1.0);
        graph.addRoad(3, 4, 1, 1, 1.0);
        graph.addRoad(4, 1, 5, 1, 1.0);
        graph.addRoad(1, 3, 10, 1, 1.0);
        return graph;
    }

    private static double totalOf(Graph graph, DynamicArray<Road> roads) {
        double total = 0;
        for (int index = 0; index < roads.size(); index++) {
            total += graph.costOf(roads.get(index));
        }
        return total;
    }

    @Test
    public void bothFindTheSameCheapestTreeOnASmallGraph() {
        Graph graph = square();

        Prim.Result prim = Prim.of(graph);
        Kruskal.Result kruskal = Kruskal.of(graph);

        assertEquals(3.0, prim.totalCost());
        assertEquals(3.0, kruskal.totalCost());
        assertEquals(3, prim.roadCount());
        assertEquals(3, kruskal.roadCount());
    }

    @Test
    public void aSpanningTreeHasOneFewerRoadThanLocations() {
        Graph graph = GraphFixtures.path(6);

        assertEquals(5, Prim.of(graph).roadCount());
        assertEquals(5, Kruskal.of(graph).roadCount());
    }

    @Test
    public void reportedTotalMatchesTheChosenRoads() {
        Graph graph = square();

        Prim.Result prim = Prim.of(graph);
        Kruskal.Result kruskal = Kruskal.of(graph);

        assertEquals(prim.totalCost(), totalOf(graph, prim.roads()), 1e-9);
        assertEquals(kruskal.totalCost(), totalOf(graph, kruskal.roads()), 1e-9);
    }

    @Test
    public void theChosenRoadsFormATreeWithNoCycle() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        Kruskal.Result kruskal = Kruskal.of(graph);

        Graph tree = new Graph(graph.weightMode());
        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index++) {
            tree.addLocation(graph.location(ids[index]));
        }
        DynamicArray<Road> chosen = kruskal.roads();
        for (int index = 0; index < chosen.size(); index++) {
            tree.addRoad(chosen.get(index));
        }

        assertFalse(DFS.hasCycle(tree), "a spanning tree must not contain a cycle");
        assertTrue(BFS.isConnected(tree), "a spanning tree must connect every location");
        assertEquals(graph.order() - 1, tree.size());
    }

    @Test
    public void primAndKruskalAgreeOnTheRealCampusNetwork() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        Prim.Result prim = Prim.of(graph);
        Kruskal.Result kruskal = Kruskal.of(graph);

        assertEquals(57, prim.roadCount(), "58 locations need 57 roads");
        assertEquals(57, kruskal.roadCount());
        assertEquals(prim.totalCost(), kruskal.totalCost(), 1e-9);
        assertTrue(prim.spansWholeGraph());
        assertTrue(kruskal.spansWholeGraph());
    }

    @Test
    public void theRootDoesNotChangeTheTotalCost() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        double fromFirst = Prim.of(graph).totalCost();

        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index += 11) {
            assertEquals(fromFirst, Prim.from(graph, ids[index]).totalCost(), 1e-9,
                    "root " + ids[index] + " gave a different total");
        }
    }

    @Test
    public void allThreeWeightModesProduceAgreeingTrees() {
        Graph base = GraphFixtures.realGraphOrSkip();

        WeightMode[] modes = {WeightMode.DISTANCE, WeightMode.TIME, WeightMode.TIME_ADJUSTED};
        for (int index = 0; index < modes.length; index++) {
            Graph graph = base.withWeightMode(modes[index]);
            assertEquals(Prim.of(graph).totalCost(), Kruskal.of(graph).totalCost(), 1e-9,
                    "Prim and Kruskal disagree under " + modes[index]);
        }
    }

    @Test
    public void primSpansOnlyItsOwnComponent() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 1, 1, 1.0);
        graph.addRoad(7, 8, 1, 1, 1.0);

        Prim.Result prim = Prim.from(graph, 1);

        assertEquals(2, prim.locationsSpanned());
        assertEquals(1, prim.roadCount());
        assertFalse(prim.spansWholeGraph());
    }

    @Test
    public void kruskalProducesAForestOnADisconnectedGraph() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 1, 1, 1.0);
        graph.addRoad(7, 8, 1, 1, 1.0);

        Kruskal.Result kruskal = Kruskal.of(graph);

        assertEquals(2, kruskal.roadCount());
        assertEquals(2, kruskal.componentCount());
        assertFalse(kruskal.spansWholeGraph());
    }

    @Test
    public void kruskalRejectsExactlyTheCycleClosingRoads() {
        Kruskal.Result kruskal = Kruskal.of(square());

        int accepted = 0;
        int rejected = 0;
        DynamicArray<Kruskal.Step> steps = kruskal.steps();
        for (int index = 0; index < steps.size(); index++) {
            if (steps.get(index).accepted()) {
                accepted++;
            } else {
                rejected++;
            }
        }

        // The three 1-cost roads connect all four locations, and the loop stops the
        // moment the last fragment merges - so the 5-cost road and the 10-cost diagonal
        // are never examined at all.
        assertEquals(3, accepted);
        assertEquals(0, rejected);
        assertEquals(3, steps.size());
    }

    @Test
    public void kruskalRejectsARoadThatWouldCloseACycle() {
        // A triangle plus a tail, so a cycle-closing road is reached before the tree is
        // complete and has to be rejected rather than skipped.
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 1, 1, 1.0);
        graph.addRoad(2, 3, 2, 1, 1.0);
        graph.addRoad(1, 3, 3, 1, 1.0);
        graph.addRoad(3, 4, 9, 1, 1.0);

        Kruskal.Result kruskal = Kruskal.of(graph);

        int rejected = 0;
        DynamicArray<Kruskal.Step> steps = kruskal.steps();
        for (int index = 0; index < steps.size(); index++) {
            if (!steps.get(index).accepted()) {
                rejected++;
            }
        }

        assertEquals(3, kruskal.roadCount());
        assertEquals(12.0, kruskal.totalCost());
        assertEquals(1, rejected, "the 1-3 road closes a cycle and must be rejected");
    }

    @Test
    public void kruskalStepsAreOrderedByNonDecreasingCost() {
        Kruskal.Result kruskal = Kruskal.of(GraphFixtures.realGraphOrSkip());

        DynamicArray<Kruskal.Step> steps = kruskal.steps();
        double previous = -1;
        for (int index = 0; index < steps.size(); index++) {
            assertTrue(steps.get(index).cost() >= previous,
                    "step " + index + " cost " + steps.get(index).cost() + " after " + previous);
            previous = steps.get(index).cost();
        }
    }

    @Test
    public void repeatedRunsGiveIdenticalTrees() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        Kruskal.Result first = Kruskal.of(graph);
        Kruskal.Result second = Kruskal.of(graph);

        assertEquals(first.totalCost(), second.totalCost());
        assertEquals(first.roadCount(), second.roadCount());
        for (int index = 0; index < first.roads().size(); index++) {
            assertEquals(first.roads().get(index).toString(),
                    second.roads().get(index).toString(), "road " + index + " differs");
        }
    }

    @Test
    public void emptyAndSingleLocationGraphsAreHandled() {
        Graph empty = new Graph(WeightMode.DISTANCE);
        assertThrows(IllegalArgumentException.class, () -> Prim.of(empty));
        assertEquals(0, Kruskal.of(empty).roadCount());

        Graph alone = new Graph(WeightMode.DISTANCE);
        alone.addLocation(1, "Alone");
        assertEquals(0, Prim.of(alone).roadCount());
        assertEquals(1, Prim.of(alone).locationsSpanned());
        assertEquals(0, Kruskal.of(alone).roadCount());
    }

    @Test
    public void unknownRootIsRejected() {
        Graph graph = GraphFixtures.path(3);
        assertThrows(IllegalArgumentException.class, () -> Prim.from(graph, 99));
    }
}
