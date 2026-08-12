package com.campushub.structures.graph;

import com.campushub.algorithms.search.BinarySearch;
import com.campushub.model.Location;
import com.campushub.model.Road;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.util.Csv;

import java.io.UncheckedIOException;
import java.nio.file.Path;

// Owner: Graphs and Optimization

/**
 * Undirected weighted graph of campus locations and the roads between them.
 *
 * <p>Holds both representations the project structure calls for. The adjacency list
 * is the primary store and is what the algorithms walk; the adjacency matrix is
 * derived on demand and cached, so the efficiency study can compare the two.
 *
 * <p>Each road carries three weights, and a {@link WeightMode} fixed at construction
 * decides which counts as traversal cost. It is fixed rather than switchable so a
 * given graph always answers cost questions the same way; {@link #withWeightMode}
 * rebuilds the same network under a different one.
 *
 * <p>No {@code java.util} collections are used. Locations live in the team's
 * {@link DynamicArray} and are addressed internally by <em>slot</em> - a dense index
 * from 0 to {@code order()-1} - which lets all per-node state sit in plain arrays.
 * {@link #slotOf} maps an id to its slot with the Searching pod's {@link BinarySearch}
 * over a sorted id array.
 */
public class Graph {

    /** Which of a road's three weights counts as traversal cost. */
    public enum WeightMode {
        DISTANCE,
        TIME,
        TIME_ADJUSTED
    }

    /** Matrix entry for a pair of locations with no road between them. */
    public static final double NO_EDGE = Double.POSITIVE_INFINITY;

    /** Returned by {@link #slotOf} for a location the graph does not hold. */
    public static final int NO_SLOT = -1;

    /**
     * A duplicate road row that disagreed with the row already loaded for the same
     * pair of locations.
     */
    public static final class EdgeConflict {

        private final Road kept;
        private final Road discarded;

        EdgeConflict(Road kept, Road discarded) {
            this.kept = kept;
            this.discarded = discarded;
        }

        public Road kept() {
            return kept;
        }

        public Road discarded() {
            return discarded;
        }

        public int lowEndpoint() {
            return Math.min(kept.getFromLocationId(), kept.getToLocationId());
        }

        public int highEndpoint() {
            return Math.max(kept.getFromLocationId(), kept.getToLocationId());
        }

        @Override
        public String toString() {
            return lowEndpoint() + "<->" + highEndpoint()
                    + ": kept " + kept + ", discarded " + discarded;
        }
    }

    private static final BinarySearch SEARCH = new BinarySearch();

    private final WeightMode weightMode;
    private final DynamicArray<Location> nodes = new DynamicArray<>();
    private final DynamicArray<DynamicArray<Road>> adjacency = new DynamicArray<>();
    private final DynamicArray<Road> edges = new DynamicArray<>();
    private final DynamicArray<Road> rejectedRows = new DynamicArray<>();
    private final DynamicArray<EdgeConflict> conflicts = new DynamicArray<>();
    private int duplicateRowsCollapsed;

    // Sorted location ids alongside the slot each occupies, rebuilt lazily whenever a
    // location is added. Null means it needs rebuilding.
    private int[] sortedIds;
    private int[] slotOfSortedId;
    private double[][] matrix;

    public Graph(WeightMode weightMode) {
        if (weightMode == null) {
            throw new IllegalArgumentException("weightMode is required");
        }
        this.weightMode = weightMode;
    }

    /**
     * Builds a graph from the Database pod's loaded rows, so it works equally with
     * {@code DataLoader}/{@code RoadRepository} output and with {@link #fromSeedData}.
     */
    public static Graph of(DynamicArray<Location> locations, DynamicArray<Road> roads,
                           WeightMode mode) {
        Graph graph = new Graph(mode);
        if (locations != null) {
            for (int index = 0; index < locations.size(); index++) {
                graph.addLocation(locations.get(index));
            }
        }
        if (roads != null) {
            for (int index = 0; index < roads.size(); index++) {
                graph.addRoad(roads.get(index));
            }
        }
        return graph;
    }

    public WeightMode weightMode() {
        return weightMode;
    }

    /**
     * Adds a location, replacing the metadata of an existing id while keeping its
     * roads.
     */
    public void addLocation(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("location is required");
        }
        int existing = slotOf(location.getLocationId());
        if (existing != NO_SLOT) {
            nodes.set(existing, location);
            return;
        }
        nodes.add(location);
        adjacency.add(new DynamicArray<Road>());
        sortedIds = null;
        matrix = null;
    }

    /** Convenience for tests and hand-built graphs. */
    public void addLocation(int id, String name) {
        addLocation(new Location(id, name, null, null, null));
    }

    /**
     * Adds an undirected road, creating placeholder locations for unknown endpoints.
     *
     * <p>A road already joining this pair is not duplicated: the first row loaded
     * wins, matching how the Database pod resolved the duplicates that used to sit in
     * {@code roads.csv}. A discarded row whose weights disagreed is recorded in
     * {@link #conflicts()} rather than dropped silently.
     *
     * @return true if a new road was stored, false if it collapsed into an existing one
     * @throws IllegalArgumentException on self-loops or non-positive weights
     */
    public boolean addRoad(Road road) {
        if (road == null) {
            throw new IllegalArgumentException("road is required");
        }
        int from = road.getFromLocationId();
        int to = road.getToLocationId();
        if (from == to) {
            throw new IllegalArgumentException("self-loop at location " + from);
        }
        if (road.getDistance_m() <= 0 || road.getTravelTime_min() <= 0
                || road.getRoadConditionWeight() <= 0) {
            throw new IllegalArgumentException("weights must be positive for road " + from
                    + "-" + to + "; Dijkstra and Prim assume non-negative costs");
        }

        Road existing = road(from, to);
        if (existing != null) {
            duplicateRowsCollapsed++;
            rejectedRows.add(road);
            if (!sameWeights(existing, road)) {
                conflicts.add(new EdgeConflict(existing, road));
            }
            return false;
        }

        ensureLocation(from);
        ensureLocation(to);
        edges.add(road);
        insertSorted(adjacency.get(slotOf(from)), road, from);
        insertSorted(adjacency.get(slotOf(to)), road, to);
        matrix = null;
        return true;
    }

    /** Convenience for tests and hand-built graphs. */
    public boolean addRoad(int from, int to, double distanceMetres, double travelTimeMinutes,
                           double conditionWeight) {
        return addRoad(new Road(from, to, distanceMetres, travelTimeMinutes, conditionWeight));
    }

    /**
     * Inserts a road into one endpoint's adjacency list at the position keeping it
     * ordered by the other endpoint's id, so traversals are reproducible without
     * sorting on every read.
     */
    private void insertSorted(DynamicArray<Road> incident, Road road, int endpoint) {
        int neighbour = otherEndpoint(road, endpoint);
        int position = 0;
        while (position < incident.size()
                && otherEndpoint(incident.get(position), endpoint) < neighbour) {
            position++;
        }
        incident.insert(position, road);
    }

    private void ensureLocation(int id) {
        if (slotOf(id) == NO_SLOT) {
            addLocation(new Location(id, "Location " + id, null, null, null));
        }
    }

    /** Number of locations. */
    public int order() {
        return nodes.size();
    }

    /** Number of distinct roads. */
    public int size() {
        return edges.size();
    }

    /**
     * Dense index of a location, or {@link #NO_SLOT} if the graph does not hold it.
     * Slots are stable once assigned, so callers may key arrays on them.
     */
    public int slotOf(int locationId) {
        rebuildIndexIfNeeded();
        int position = SEARCH.binarySearch(sortedIds, locationId);
        return position < 0 ? NO_SLOT : slotOfSortedId[position];
    }

    /** The location id occupying {@code slot}. */
    public int idAt(int slot) {
        return nodes.get(slot).getLocationId();
    }

    /** Location ids in ascending order. */
    public int[] locationIds() {
        rebuildIndexIfNeeded();
        return sortedIds.clone();
    }

    public Location location(int locationId) {
        int slot = slotOf(locationId);
        return slot == NO_SLOT ? null : nodes.get(slot);
    }

    public boolean hasLocation(int locationId) {
        return slotOf(locationId) != NO_SLOT;
    }

    /** Display name for a location, falling back to its id. */
    public String nameOf(int locationId) {
        Location location = location(locationId);
        return location == null || location.getName() == null
                ? String.valueOf(locationId)
                : location.getName();
    }

    /**
     * Roads touching a location, ordered by the neighbour's id. Empty for an unknown
     * or isolated location.
     */
    public DynamicArray<Road> roadsFrom(int locationId) {
        int slot = slotOf(locationId);
        return slot == NO_SLOT ? new DynamicArray<Road>() : adjacency.get(slot);
    }

    public int degree(int locationId) {
        return roadsFrom(locationId).size();
    }

    /** Every distinct road, in the order loaded. */
    public DynamicArray<Road> roads() {
        return edges;
    }

    /** The road joining two locations, or null if none does. */
    public Road road(int from, int to) {
        DynamicArray<Road> incident = roadsFrom(from);
        for (int index = 0; index < incident.size(); index++) {
            Road candidate = incident.get(index);
            if (otherEndpoint(candidate, from) == to) {
                return candidate;
            }
        }
        return null;
    }

    public boolean hasRoad(int from, int to) {
        return road(from, to) != null;
    }

    /** Cost of {@code road} under this graph's weight mode. */
    public double costOf(Road road) {
        switch (weightMode) {
            case DISTANCE:
                return road.getDistance_m();
            case TIME:
                return road.getTravelTime_min();
            case TIME_ADJUSTED:
                return road.getTravelTime_min() * road.getRoadConditionWeight();
            default:
                throw new IllegalArgumentException("unknown weight mode: " + weightMode);
        }
    }

    /**
     * Cost of travelling directly between two locations: 0 for a location to itself,
     * {@link #NO_EDGE} when no road joins them.
     */
    public double cost(int from, int to) {
        if (from == to) {
            return 0;
        }
        Road road = road(from, to);
        return road == null ? NO_EDGE : costOf(road);
    }

    /** The endpoint of {@code road} that is not {@code endpoint}. */
    public static int otherEndpoint(Road road, int endpoint) {
        if (endpoint == road.getFromLocationId()) {
            return road.getToLocationId();
        }
        if (endpoint == road.getToLocationId()) {
            return road.getFromLocationId();
        }
        throw new IllegalArgumentException(
                "location " + endpoint + " is not an endpoint of " + road);
    }

    /**
     * Costs as an adjacency matrix: 0 on the diagonal, {@link #NO_EDGE} where no road
     * exists. Rows and columns are slots. Built on first use, cached until the graph
     * changes, and returned as a copy.
     */
    public double[][] adjacencyMatrix() {
        buildMatrixIfNeeded();
        double[][] copy = new double[matrix.length][];
        for (int row = 0; row < matrix.length; row++) {
            copy[row] = matrix[row].clone();
        }
        return copy;
    }

    private void buildMatrixIfNeeded() {
        if (matrix != null) {
            return;
        }
        int count = order();
        double[][] built = new double[count][count];
        for (int row = 0; row < count; row++) {
            for (int column = 0; column < count; column++) {
                built[row][column] = row == column ? 0 : NO_EDGE;
            }
        }
        for (int index = 0; index < edges.size(); index++) {
            Road road = edges.get(index);
            int row = slotOf(road.getFromLocationId());
            int column = slotOf(road.getToLocationId());
            double cost = costOf(road);
            built[row][column] = cost;
            built[column][row] = cost;
        }
        matrix = built;
    }

    private void rebuildIndexIfNeeded() {
        if (sortedIds != null) {
            return;
        }
        int count = nodes.size();
        int[] ids = new int[count];
        int[] slots = new int[count];
        for (int slot = 0; slot < count; slot++) {
            ids[slot] = nodes.get(slot).getLocationId();
            slots[slot] = slot;
        }
        // Insertion sort: runs only when locations change, and the campus has 58.
        for (int index = 1; index < count; index++) {
            int id = ids[index];
            int slot = slots[index];
            int scan = index - 1;
            while (scan >= 0 && ids[scan] > id) {
                ids[scan + 1] = ids[scan];
                slots[scan + 1] = slots[scan];
                scan--;
            }
            ids[scan + 1] = id;
            slots[scan + 1] = slot;
        }
        sortedIds = ids;
        slotOfSortedId = slots;
    }

    private static boolean sameWeights(Road left, Road right) {
        return left.getDistance_m() == right.getDistance_m()
                && left.getTravelTime_min() == right.getTravelTime_min()
                && left.getRoadConditionWeight() == right.getRoadConditionWeight();
    }

    /** Duplicate road rows whose weights disagreed with the row kept. */
    public DynamicArray<EdgeConflict> conflicts() {
        return conflicts;
    }

    /** How many road rows collapsed into an already-known pair. */
    public int duplicateRowsCollapsed() {
        return duplicateRowsCollapsed;
    }

    /** The road rows discarded by duplicate collapsing. */
    public DynamicArray<Road> rejectedRows() {
        return rejectedRows;
    }

    public String summary() {
        return order() + " locations, " + size() + " roads, cost = " + weightMode
                + (duplicateRowsCollapsed == 0
                        ? ""
                        : " (" + duplicateRowsCollapsed + " duplicate rows collapsed, "
                                + conflicts.size() + " with disagreeing weights)");
    }

    @Override
    public String toString() {
        return "Graph[" + summary() + "]";
    }

    /**
     * The same network scored by a different weight mode. Duplicate collapsing is
     * redone from the original rows, so the result matches loading the raw data under
     * {@code mode}.
     */
    public Graph withWeightMode(WeightMode mode) {
        Graph rescored = new Graph(mode);
        for (int slot = 0; slot < nodes.size(); slot++) {
            rescored.addLocation(nodes.get(slot));
        }
        for (int index = 0; index < edges.size(); index++) {
            rescored.addRoad(edges.get(index));
        }
        for (int index = 0; index < rejectedRows.size(); index++) {
            rescored.addRoad(rejectedRows.get(index));
        }
        return rescored;
    }

    /**
     * Loads from the seed data directory, expecting {@code locations.csv} and
     * {@code roads.csv} inside it.
     *
     * @throws UncheckedIOException if either file cannot be read
     */
    public static Graph fromSeedData(Path seedDirectory, WeightMode mode) {
        return of(readLocations(seedDirectory.resolve("locations.csv")),
                readRoads(seedDirectory.resolve("roads.csv")), mode);
    }

    /** Reads {@code locations.csv} into the Database pod's {@link Location} type. */
    public static DynamicArray<Location> readLocations(Path file) {
        DynamicArray<String[]> rows = Csv.rows(file);
        DynamicArray<Location> locations = new DynamicArray<>();
        if (rows.isEmpty()) {
            return locations;
        }

        String[] header = rows.get(0);
        int idColumn = Csv.requiredColumn(header, file, "locationId");
        int nameColumn = Csv.column(header, "name");
        int areaColumn = Csv.column(header, "area");
        int typeColumn = Csv.column(header, "type");
        int coordinatesColumn = Csv.column(header, "coordinates");

        for (int index = 1; index < rows.size(); index++) {
            String[] row = rows.get(index);
            if (Csv.isBlankRow(row)) {
                continue;
            }
            locations.add(new Location(
                    Integer.parseInt(Csv.requiredField(row, idColumn, file, "locationId")),
                    Csv.field(row, nameColumn),
                    Csv.field(row, areaColumn),
                    Csv.field(row, typeColumn),
                    Csv.field(row, coordinatesColumn)));
        }
        return locations;
    }

    /** Reads {@code roads.csv} into the Database pod's {@link Road} type. */
    public static DynamicArray<Road> readRoads(Path file) {
        DynamicArray<String[]> rows = Csv.rows(file);
        DynamicArray<Road> roads = new DynamicArray<>();
        if (rows.isEmpty()) {
            return roads;
        }

        String[] header = rows.get(0);
        int fromColumn = Csv.requiredColumn(header, file, "fromLocationId");
        int toColumn = Csv.requiredColumn(header, file, "toLocationId");
        int distanceColumn = Csv.requiredColumn(header, file, "distance_m");
        int timeColumn = Csv.requiredColumn(header, file, "travelTime_min");
        int conditionColumn = Csv.requiredColumn(header, file, "roadConditionWeight");

        for (int index = 1; index < rows.size(); index++) {
            String[] row = rows.get(index);
            if (Csv.isBlankRow(row)) {
                continue;
            }
            roads.add(new Road(
                    Integer.parseInt(Csv.requiredField(row, fromColumn, file, "fromLocationId")),
                    Integer.parseInt(Csv.requiredField(row, toColumn, file, "toLocationId")),
                    Double.parseDouble(Csv.requiredField(row, distanceColumn, file, "distance_m")),
                    Double.parseDouble(Csv.requiredField(row, timeColumn, file, "travelTime_min")),
                    Double.parseDouble(
                            Csv.requiredField(row, conditionColumn, file, "roadConditionWeight"))));
        }
        return roads;
    }
}
