package com.campushub.structures.graph;

import com.campushub.model.Road;
import com.campushub.structures.graph.Graph.WeightMode;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.testsupport.GraphFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphTest {

    /**
     * <pre>
     *   1 --100m/2min-- 2 --50m/1min-- 3
     *    \                            /
     *     ----- 400m/5min -- 4 -------
     * </pre>
     */
    private static Graph sample(WeightMode mode) {
        Graph graph = new Graph(mode);
        graph.addLocation(1, "Great Hall");
        graph.addLocation(2, "University Square");
        graph.addLocation(3, "Legon Hall");
        graph.addLocation(4, "Balme Library");
        graph.addRoad(1, 2, 100, 2, 1.0);
        graph.addRoad(2, 3, 50, 1, 1.0);
        graph.addRoad(1, 4, 400, 5, 1.0);
        graph.addRoad(4, 3, 400, 5, 1.0);
        return graph;
    }

    @Test
    public void reportsOrderAndSize() {
        Graph graph = sample(WeightMode.DISTANCE);

        assertEquals(4, graph.order());
        assertEquals(4, graph.size());
        assertArrayEquals(new int[] {1, 2, 3, 4}, graph.locationIds());
    }

    @Test
    public void roadsAreUndirected() {
        Graph graph = sample(WeightMode.DISTANCE);

        assertTrue(graph.hasRoad(1, 2));
        assertTrue(graph.hasRoad(2, 1));
        assertEquals(100.0, graph.cost(2, 1));
        assertSame(graph.road(1, 2), graph.road(2, 1));
    }

    @Test
    public void degreeCountsIncidentRoads() {
        Graph graph = sample(WeightMode.DISTANCE);

        assertEquals(2, graph.degree(1));
        assertEquals(2, graph.degree(2));
        assertEquals(0, graph.degree(99));
    }

    @Test
    public void roadsAreOrderedByNeighbourIdSoTraversalIsReproducible() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 9, 10, 1, 1.0);
        graph.addRoad(1, 3, 10, 1, 1.0);
        graph.addRoad(1, 5, 10, 1, 1.0);

        DynamicArray<Road> incident = graph.roadsFrom(1);
        int[] neighbours = new int[incident.size()];
        for (int index = 0; index < incident.size(); index++) {
            neighbours[index] = Graph.otherEndpoint(incident.get(index), 1);
        }
        assertArrayEquals(new int[] {3, 5, 9}, neighbours);
    }

    @Test
    public void slotsAreDenseAndStable() {
        Graph graph = sample(WeightMode.DISTANCE);

        boolean[] seen = new boolean[graph.order()];
        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index++) {
            int slot = graph.slotOf(ids[index]);
            assertTrue(slot >= 0 && slot < graph.order(), "slot out of range: " + slot);
            assertFalse(seen[slot], "slot " + slot + " used twice");
            seen[slot] = true;
            assertEquals(ids[index], graph.idAt(slot));
        }
    }

    @Test
    public void slotOfUnknownLocationIsNoSlot() {
        assertEquals(Graph.NO_SLOT, sample(WeightMode.DISTANCE).slotOf(99));
    }

    @Test
    public void costOfALocationToItselfIsZeroAndMissingRoadsAreInfinite() {
        Graph graph = sample(WeightMode.DISTANCE);

        assertEquals(0.0, graph.cost(1, 1));
        assertEquals(Graph.NO_EDGE, graph.cost(1, 3));
    }

    @Test
    public void weightModeSelectsWhichColumnIsCost() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 500, 4, 1.5);
        assertEquals(500.0, graph.cost(1, 2));

        assertEquals(4.0, graph.withWeightMode(WeightMode.TIME).cost(1, 2));
        assertEquals(6.0, graph.withWeightMode(WeightMode.TIME_ADJUSTED).cost(1, 2));
    }

    @Test
    public void rescoringPreservesTheNetworkAndMetadata() {
        Graph graph = sample(WeightMode.DISTANCE);
        Graph rescored = graph.withWeightMode(WeightMode.TIME);

        assertEquals(graph.order(), rescored.order());
        assertEquals(graph.size(), rescored.size());
        assertEquals(WeightMode.TIME, rescored.weightMode());
        assertEquals("Great Hall", rescored.location(1).getName());
    }

    @Test
    public void duplicateRoadWithIdenticalWeightsCollapsesWithoutConflict() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        assertTrue(graph.addRoad(1, 2, 100, 2, 1.0));
        assertFalse(graph.addRoad(1, 2, 100, 2, 1.0));

        assertEquals(1, graph.size());
        assertEquals(1, graph.duplicateRowsCollapsed());
        assertTrue(graph.conflicts().isEmpty());
    }

    @Test
    public void conflictingDuplicateKeepsTheFirstRowAndRecordsTheDisagreement() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(40, 42, 455, 8, 1.16);
        graph.addRoad(40, 42, 100, 2, 1.0);

        assertEquals(1, graph.size());
        assertEquals(455.0, graph.cost(40, 42), "the first row loaded should win");
        assertEquals(1, graph.conflicts().size());

        Graph.EdgeConflict conflict = graph.conflicts().get(0);
        assertEquals(455.0, conflict.kept().getDistance_m());
        assertEquals(100.0, conflict.discarded().getDistance_m());
        assertEquals(40, conflict.lowEndpoint());
        assertEquals(42, conflict.highEndpoint());
    }

    @Test
    public void duplicateIsDetectedRegardlessOfDirection() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(40, 42, 455, 8, 1.16);
        graph.addRoad(42, 40, 100, 2, 1.0);

        assertEquals(1, graph.size());
        assertEquals(455.0, graph.cost(40, 42));
        assertEquals(455.0, graph.cost(42, 40));
    }

    @Test
    public void matrixAgreesWithTheAdjacencyList() {
        Graph graph = sample(WeightMode.DISTANCE);
        double[][] matrix = graph.adjacencyMatrix();

        int[] ids = graph.locationIds();
        for (int from = 0; from < ids.length; from++) {
            for (int to = 0; to < ids.length; to++) {
                assertEquals(graph.cost(ids[from], ids[to]),
                        matrix[graph.slotOf(ids[from])][graph.slotOf(ids[to])],
                        "matrix disagrees for " + ids[from] + "->" + ids[to]);
            }
        }
    }

    @Test
    public void matrixIsSymmetricWithZeroDiagonal() {
        double[][] matrix = sample(WeightMode.TIME).adjacencyMatrix();

        for (int row = 0; row < matrix.length; row++) {
            assertEquals(0.0, matrix[row][row]);
            for (int column = 0; column < matrix.length; column++) {
                assertEquals(matrix[row][column], matrix[column][row]);
            }
        }
    }

    @Test
    public void matrixIsADefensiveCopy() {
        Graph graph = sample(WeightMode.DISTANCE);

        graph.adjacencyMatrix()[0][1] = -999;

        assertEquals(100.0, graph.cost(1, 2));
        assertEquals(100.0,
                graph.adjacencyMatrix()[graph.slotOf(1)][graph.slotOf(2)]);
    }

    @Test
    public void matrixIsRebuiltAfterTheGraphChanges() {
        Graph graph = sample(WeightMode.DISTANCE);
        assertEquals(4, graph.adjacencyMatrix().length);

        graph.addLocation(5, "Night Market");
        graph.addRoad(3, 5, 20, 1, 1.0);

        assertEquals(5, graph.adjacencyMatrix().length);
        assertEquals(20.0, graph.adjacencyMatrix()[graph.slotOf(3)][graph.slotOf(5)]);
    }

    @Test
    public void selfLoopsAreRejected() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad(1, 1, 10, 1, 1.0));
    }

    @Test
    public void nonPositiveWeightsAreRejectedBecauseDijkstraAssumesThem() {
        Graph graph = new Graph(WeightMode.DISTANCE);

        assertThrows(IllegalArgumentException.class, () -> graph.addRoad(1, 2, 0, 1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad(1, 2, 10, -1, 1.0));
        assertThrows(IllegalArgumentException.class, () -> graph.addRoad(1, 2, 10, 1, 0));
    }

    @Test
    public void weightModeIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> new Graph(null));
    }

    @Test
    public void addingARoadCreatesPlaceholdersForUnknownLocations() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(7, 8, 10, 1, 1.0);

        assertTrue(graph.hasLocation(7));
        assertNotNull(graph.location(8));
        assertEquals(2, graph.order());
    }

    @Test
    public void reAddingALocationKeepsItsRoadsButUpdatesItsName() {
        Graph graph = new Graph(WeightMode.DISTANCE);
        graph.addRoad(1, 2, 10, 1, 1.0);

        graph.addLocation(1, "Great Hall");

        assertEquals("Great Hall", graph.nameOf(1));
        assertEquals(1, graph.degree(1));
        assertEquals(2, graph.order());
    }

    @Test
    public void otherEndpointRejectsALocationNotOnTheRoad() {
        Road road = new Road(1, 2, 10, 1, 1.0);
        assertThrows(IllegalArgumentException.class, () -> Graph.otherEndpoint(road, 3));
    }

    @Test
    public void unknownLocationsReadAsEmptyRatherThanThrowing() {
        Graph graph = sample(WeightMode.DISTANCE);

        assertNull(graph.location(99));
        assertFalse(graph.hasLocation(99));
        assertTrue(graph.roadsFrom(99).isEmpty());
        assertEquals("99", graph.nameOf(99));
    }

    @Test
    public void loadsQuotedFieldsAndABomFromCsv(@TempDir Path directory) throws IOException {
        // Mirrors the real export: a UTF-8 BOM, names holding commas, CRLF lines.
        Files.write(directory.resolve("locations.csv"),
                ("﻿locationId,name,area,type,coordinates\r\n"
                        + "1,\"N Block (NB1, NB2, NB3)\",Central Campus,lecture_hall,C-9\r\n"
                        + "2,Legon Hill Junction,\"Legon Hill, near Halls\",connector,K-40\r\n")
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("roads.csv"),
                ("fromLocationId,toLocationId,distance_m,travelTime_min,roadConditionWeight\r\n"
                        + "1,2,427,2.6,1.22\r\n").getBytes(StandardCharsets.UTF_8));

        Graph graph = Graph.fromSeedData(directory, WeightMode.TIME_ADJUSTED);

        assertEquals(2, graph.order());
        assertEquals(1, graph.size());
        assertEquals("N Block (NB1, NB2, NB3)", graph.nameOf(1));
        assertEquals("Legon Hill, near Halls", graph.location(2).getArea());
        assertEquals("lecture_hall", graph.location(1).getType());
        assertEquals(2.6 * 1.22, graph.cost(1, 2));
    }

    @Test
    public void csvColumnsAreFoundByNameNotPosition(@TempDir Path directory) throws IOException {
        Files.write(directory.resolve("locations.csv"),
                "name,type,locationId\nGreat Hall,lecture_hall,1\n"
                        .getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("roads.csv"),
                ("roadConditionWeight,travelTime_min,distance_m,toLocationId,fromLocationId\n"
                        + "1.0,2,100,2,1\n").getBytes(StandardCharsets.UTF_8));

        Graph graph = Graph.fromSeedData(directory, WeightMode.DISTANCE);

        assertEquals("Great Hall", graph.nameOf(1));
        assertEquals(100.0, graph.cost(1, 2));
    }

    @Test
    public void missingRequiredColumnIsReportedWithTheFileName(@TempDir Path directory)
            throws IOException {
        Files.write(directory.resolve("locations.csv"),
                "locationId,name\n1,Great Hall\n".getBytes(StandardCharsets.UTF_8));
        Files.write(directory.resolve("roads.csv"),
                "fromLocationId,toLocationId\n1,2\n".getBytes(StandardCharsets.UTF_8));

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> Graph.fromSeedData(directory, WeightMode.DISTANCE));
        assertTrue(failure.getMessage().contains("distance_m"), failure.getMessage());
        assertTrue(failure.getMessage().contains("roads.csv"), failure.getMessage());
    }

    @Test
    public void emptyCsvFilesLoadAsAnEmptyGraph(@TempDir Path directory) throws IOException {
        Files.write(directory.resolve("locations.csv"), new byte[0]);
        Files.write(directory.resolve("roads.csv"), new byte[0]);

        Graph graph = Graph.fromSeedData(directory, WeightMode.DISTANCE);

        assertEquals(0, graph.order());
        assertEquals(0, graph.size());
    }

    // The corrected dataset: the Database pod removed the duplicate rows that used to
    // make road costs depend on load order.

    @Test
    public void realSeedDataLoadsCleanlyWithNoDuplicatesLeft() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        assertEquals(58, graph.order());
        assertEquals(117, graph.size());
        assertEquals(0, graph.duplicateRowsCollapsed(),
                "roads.csv should no longer contain duplicate pairs");
        assertTrue(graph.conflicts().isEmpty());
    }

    @Test
    public void realSeedDataHasNoDanglingRoadOrIsolatedLocation() {
        Graph graph = GraphFixtures.realGraphOrSkip();

        DynamicArray<Road> roads = graph.roads();
        for (int index = 0; index < roads.size(); index++) {
            Road road = roads.get(index);
            assertTrue(graph.hasLocation(road.getFromLocationId()));
            assertTrue(graph.hasLocation(road.getToLocationId()));
        }
        int[] ids = graph.locationIds();
        for (int index = 0; index < ids.length; index++) {
            assertTrue(graph.degree(ids[index]) > 0, "location " + ids[index] + " has no road");
        }
    }

    @Test
    public void loadingRealSeedDataTwiceGivesTheSameCosts() {
        Graph first = GraphFixtures.realGraphOrSkip();
        Graph second = Graph.fromSeedData(GraphFixtures.SEED_DATA, GraphFixtures.DEFAULT_MODE);

        assertEquals(first.size(), second.size());
        DynamicArray<Road> roads = first.roads();
        for (int index = 0; index < roads.size(); index++) {
            Road road = roads.get(index);
            assertEquals(first.cost(road.getFromLocationId(), road.getToLocationId()),
                    second.cost(road.getFromLocationId(), road.getToLocationId()));
        }
    }
}
