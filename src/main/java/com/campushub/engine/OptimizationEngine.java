package com.campushub.engine;

import com.campushub.algorithms.optimization.GreedyAssigner;
import com.campushub.algorithms.optimization.KnapsackDP;
import com.campushub.algorithms.optimization.ServiceData;
import com.campushub.model.Resource;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

import java.nio.file.Path;

/**
 * Dispatch and shift-planning for the CLI, backed by the real request and resource data.
 *
 * <p>Loads once and reuses, sharing the road network with a {@link RouteEngine} so the
 * graph is not built twice.
 */
public class OptimizationEngine {

    private static final Path SEED_DATA = Path.of("database", "seed-data");

    /** Minutes in one crew shift, used by {@link #planShift}. */
    public static final int DEFAULT_SHIFT_MINUTES = 240;

    private final Path seedDirectory;
    private final RouteEngine routes;
    private DynamicArray<ServiceRequest> requests;
    private DynamicArray<Resource> resources;

    public OptimizationEngine() {
        this(SEED_DATA, new RouteEngine());
    }

    public OptimizationEngine(Path seedDirectory, RouteEngine routes) {
        this.seedDirectory = seedDirectory;
        this.routes = routes;
    }

    private DynamicArray<ServiceRequest> requests() {
        if (requests == null) {
            requests = ServiceData.loadRequests(seedDirectory);
        }
        return requests;
    }

    private DynamicArray<Resource> resources() {
        if (resources == null) {
            resources = ServiceData.loadResources(seedDirectory);
        }
        return resources;
    }

    /**
     * Assigns the outstanding requests to resources and summarises the result for the
     * CLI.
     */
    public String optimizeResources() {
        Graph roads = routes.graph();
        DynamicArray<ServiceRequest> waiting = ServiceData.outstanding(requests());
        if (waiting.isEmpty()) {
            return "No outstanding requests to assign";
        }

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(roads, waiting, resources());
        return String.format(
                "Assigned %d of %d outstanding requests to %d resources | total travel %.2f min, "
                        + "average %.2f min | %d shortest-path searches, %d unassigned",
                dispatch.assignedCount(), waiting.size(), resources().size(),
                dispatch.totalTravelCost(), dispatch.averageTravelCost(),
                dispatch.shortestPathSearches(), dispatch.unassignedCount());
    }

    /**
     * Chooses which outstanding requests one crew based at {@code depotLocationId}
     * should take in a shift.
     */
    public String planShift(int depotLocationId, int shiftMinutes) {
        Graph roads = routes.graph();
        if (!roads.hasLocation(depotLocationId)) {
            return "Unknown depot location: " + depotLocationId;
        }
        DynamicArray<ServiceRequest> waiting = ServiceData.outstanding(requests());
        if (waiting.isEmpty()) {
            return "No outstanding requests to plan";
        }

        KnapsackDP.Result plan = KnapsackDP.forShift(roads, waiting, depotLocationId, shiftMinutes);
        return String.format(
                "From %s in %d min: fulfil %d of %d requests, value %.1f, using %d min",
                roads.nameOf(depotLocationId), shiftMinutes, plan.chosenCount(), waiting.size(),
                plan.bestValue(), plan.weightUsed());
    }

    /** Shift plan for the default shift length. */
    public String planShift(int depotLocationId) {
        return planShift(depotLocationId, DEFAULT_SHIFT_MINUTES);
    }

    /**
     * Greedy against the dynamic program on the same shift, which is the comparison the
     * report needs: greedy is fast but can be beaten.
     */
    public String compareGreedyWithDynamicProgram(int depotLocationId, int shiftMinutes) {
        Graph roads = routes.graph();
        if (!roads.hasLocation(depotLocationId)) {
            return "Unknown depot location: " + depotLocationId;
        }
        DynamicArray<ServiceRequest> waiting = ServiceData.outstanding(requests());
        KnapsackDP.Result optimal =
                KnapsackDP.forShift(roads, waiting, depotLocationId, shiftMinutes);
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(roads, waiting, resources());

        return String.format(
                "Shift of %d min from %s: DP fulfils %d requests worth %.1f. "
                        + "Greedy dispatch assigns %d requests across all resources at %.2f min "
                        + "total travel. DP optimises one crew's value; greedy spreads load.",
                shiftMinutes, roads.nameOf(depotLocationId), optimal.chosenCount(),
                optimal.bestValue(), dispatch.assignedCount(), dispatch.totalTravelCost());
    }
}
