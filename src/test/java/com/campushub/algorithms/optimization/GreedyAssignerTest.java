package com.campushub.algorithms.optimization;

import com.campushub.model.Resource;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;
import com.campushub.structures.linear.DynamicArray;
import com.campushub.testsupport.GraphFixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GreedyAssignerTest {

    private static ServiceRequest request(int id, int source, String urgency, String deadline,
                                         double fine) {
        return new ServiceRequest(id, source, source, "Maintenance - Electrical Fault", urgency,
                "2026-08-01", deadline, "pending", fine);
    }

    private static Resource resource(int id, int home, int capacity) {
        return new Resource(id, "staff", "Technician " + id, home, capacity, "available");
    }

    private static DynamicArray<ServiceRequest> requests(ServiceRequest... items) {
        DynamicArray<ServiceRequest> list = new DynamicArray<>();
        for (int index = 0; index < items.length; index++) {
            list.add(items[index]);
        }
        return list;
    }

    private static DynamicArray<Resource> resources(Resource... items) {
        DynamicArray<Resource> list = new DynamicArray<>();
        for (int index = 0; index < items.length; index++) {
            list.add(items[index]);
        }
        return list;
    }

    /** A line of four locations, each road one minute apart. */
    private static Graph line() {
        Graph graph = new Graph(WeightMode.TIME);
        graph.addLocation(1, "One");
        for (int id = 1; id < 4; id++) {
            graph.addRoad(id, id + 1, 100, 1, 1.0);
        }
        return graph;
    }

    @Test
    public void sendsTheNearestResource() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 4, "high", "2026-08-02", 0)),
                resources(resource(10, 1, 1), resource(11, 3, 1)));

        assertEquals(1, dispatch.assignedCount());
        assertEquals(11, dispatch.assignments().get(0).resource().getResourceId());
        assertEquals(1.0, dispatch.assignments().get(0).travelCost());
    }

    @Test
    public void urgencyDecidesWhoIsServedFirst() {
        // One resource, one unit of capacity: only the most urgent request gets it.
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "low", "2026-08-02", 0),
                        request(2, 2, "high", "2026-08-09", 0)),
                resources(resource(10, 1, 1)));

        assertEquals(1, dispatch.assignedCount());
        assertEquals(2, dispatch.assignments().get(0).request().getRequestId());
        assertEquals(1, dispatch.unassignedCount());
    }

    @Test
    public void deadlineBreaksAnUrgencyTie() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "high", "2026-08-20", 0),
                        request(2, 2, "high", "2026-08-03", 0)),
                resources(resource(10, 1, 1)));

        assertEquals(2, dispatch.assignments().get(0).request().getRequestId());
    }

    @Test
    public void accruedFineBreaksADeadlineTie() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "high", "2026-08-03", 10),
                        request(2, 2, "high", "2026-08-03", 50)),
                resources(resource(10, 1, 1)));

        assertEquals(2, dispatch.assignments().get(0).request().getRequestId());
    }

    @Test
    public void requestIdBreaksEveryRemainingTieSoOrderIsTotal() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(7, 2, "high", "2026-08-03", 10),
                        request(3, 2, "high", "2026-08-03", 10)),
                resources(resource(10, 1, 2)));

        assertEquals(3, dispatch.assignments().get(0).request().getRequestId());
        assertEquals(7, dispatch.assignments().get(1).request().getRequestId());
    }

    @Test
    public void capacityIsRespected() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "high", "2026-08-02", 0),
                        request(2, 2, "high", "2026-08-03", 0),
                        request(3, 2, "high", "2026-08-04", 0)),
                resources(resource(10, 1, 2)));

        assertEquals(2, dispatch.assignedCount());
        assertEquals(1, dispatch.unassignedCount());
        assertEquals(2, dispatch.loadOf(10));
    }

    @Test
    public void unavailableResourcesAreSkipped() {
        DynamicArray<Resource> pool = resources(
                new Resource(10, "staff", "Off duty", 1, 5, "unavailable"),
                resource(11, 4, 1));

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "high", "2026-08-02", 0)), pool);

        assertEquals(11, dispatch.assignments().get(0).resource().getResourceId());
        assertEquals(0, dispatch.loadOf(10));
    }

    @Test
    public void unreachableRequestsAreReportedWithAReason() {
        Graph graph = line();
        graph.addLocation(99, "Island");

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(graph,
                requests(request(1, 99, "high", "2026-08-02", 0)),
                resources(resource(10, 1, 1)));

        assertEquals(0, dispatch.assignedCount());
        assertEquals(1, dispatch.unassignedCount());
        assertTrue(dispatch.unassigned().get(0).reason().contains("reach"),
                dispatch.unassigned().get(0).reason());
    }

    @Test
    public void greedyIsBeatenByTheBestPossiblePairing() {
        // Resource 10 sits next to both requests; resource 11 is far from both. Greedy
        // serves the urgent request with the near resource, forcing the far one onto the
        // second request. Pairing them the other way costs less in total.
        Graph graph = new Graph(WeightMode.TIME);
        graph.addLocation(1, "Depot near");
        graph.addRoad(1, 2, 100, 1, 1.0);
        graph.addRoad(2, 3, 100, 1, 1.0);
        graph.addRoad(3, 4, 100, 1, 1.0);

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(graph,
                requests(request(1, 2, "high", "2026-08-02", 0),
                        request(2, 3, "low", "2026-08-02", 0)),
                resources(resource(10, 1, 1), resource(11, 4, 1)));

        // Greedy: request 1 -> resource 10 (1 min), request 2 -> resource 11 (1 min) = 2.
        // The alternative pairing costs 2 + 2 = 4, so greedy happens to win here.
        assertEquals(2, dispatch.assignedCount());
        assertEquals(2.0, dispatch.totalTravelCost(), 1e-9);

        // The genuine failure: one resource, and greedy spends it on the urgent request
        // even though the other is far cheaper to serve.
        GreedyAssigner.Result single = GreedyAssigner.assign(graph,
                requests(request(1, 4, "high", "2026-08-02", 0),
                        request(2, 2, "low", "2026-08-02", 0)),
                resources(resource(10, 1, 1)));

        assertEquals(1, single.assignedCount());
        assertEquals(3.0, single.totalTravelCost(), 1e-9);
        assertTrue(single.totalTravelCost() > 1.0,
                "serving the low-urgency request would have cost only 1 minute");
    }

    @Test
    public void oneShortestPathSearchPerDistinctResourceHome() {
        GreedyAssigner.Result dispatch = GreedyAssigner.assign(line(),
                requests(request(1, 2, "high", "2026-08-02", 0),
                        request(2, 3, "high", "2026-08-03", 0)),
                resources(resource(10, 1, 1), resource(11, 1, 1), resource(12, 4, 1)));

        assertEquals(2, dispatch.shortestPathSearches(),
                "three resources share two homes, so two searches suffice");
    }

    @Test
    public void matchingTypeRuleRestrictsWhoCanTakeAShuttleBooking() {
        DynamicArray<ServiceRequest> shuttle = requests(new ServiceRequest(1, 2, 3,
                "Shuttle Booking - Special Event Transport", "high", "2026-08-01", "2026-08-02",
                "pending", 0));
        DynamicArray<Resource> pool = resources(
                new Resource(10, "staff", "Technician", 1, 1, "available"),
                new Resource(11, "vehicle", "Shuttle bus", 1, 1, "available"));

        GreedyAssigner.Result loose = GreedyAssigner.assign(line(), shuttle, pool);
        assertEquals(10, loose.assignments().get(0).resource().getResourceId());

        GreedyAssigner.Result strict =
                GreedyAssigner.assign(line(), shuttle, pool, GreedyAssigner.MATCHING_TYPE);
        assertEquals(11, strict.assignments().get(0).resource().getResourceId());
    }

    @Test
    public void nullArgumentsAreRejected() {
        Graph graph = line();
        DynamicArray<ServiceRequest> empty = new DynamicArray<>();
        DynamicArray<Resource> pool = resources(resource(10, 1, 1));

        assertThrows(IllegalArgumentException.class,
                () -> GreedyAssigner.assign(null, empty, pool));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAssigner.assign(graph, null, pool));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAssigner.assign(graph, empty, null));
        assertThrows(IllegalArgumentException.class,
                () -> GreedyAssigner.assign(graph, empty, pool, null));
    }

    @Test
    public void totalTravelCostMatchesTheSumOfAssignments() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting =
                ServiceData.outstanding(ServiceData.loadRequests(GraphFixtures.SEED_DATA));
        DynamicArray<Resource> pool = ServiceData.loadResources(GraphFixtures.SEED_DATA);

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(graph, waiting, pool);

        double summed = 0;
        for (int index = 0; index < dispatch.assignments().size(); index++) {
            summed += dispatch.assignments().get(index).travelCost();
        }
        assertEquals(dispatch.totalTravelCost(), summed, 1e-9);
        assertEquals(dispatch.assignedCount() + dispatch.unassignedCount(), waiting.size());
    }

    @Test
    public void everyOutstandingRequestIsServedOnTheRealData() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting =
                ServiceData.outstanding(ServiceData.loadRequests(GraphFixtures.SEED_DATA));
        DynamicArray<Resource> pool = ServiceData.loadResources(GraphFixtures.SEED_DATA);

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(graph, waiting, pool);

        assertEquals(157, waiting.size(), "pending plus overdue requests");
        assertEquals(0, dispatch.unassignedCount());
        assertEquals(15, dispatch.shortestPathSearches(), "49 resources share 15 homes");
    }

    @Test
    public void dispatchOrderNeverPutsALowerUrgencyFirst() {
        Graph graph = GraphFixtures.realGraphOrSkip();
        DynamicArray<ServiceRequest> waiting =
                ServiceData.outstanding(ServiceData.loadRequests(GraphFixtures.SEED_DATA));
        DynamicArray<Resource> pool = ServiceData.loadResources(GraphFixtures.SEED_DATA);

        GreedyAssigner.Result dispatch = GreedyAssigner.assign(graph, waiting, pool);

        int previousRank = Integer.MAX_VALUE;
        for (int index = 0; index < dispatch.assignments().size(); index++) {
            int rank = ServiceData.urgencyRank(
                    dispatch.assignments().get(index).request().getUrgency());
            assertTrue(rank <= previousRank,
                    "urgency rose from " + previousRank + " to " + rank + " at " + index);
            previousRank = rank;
        }
    }
}
