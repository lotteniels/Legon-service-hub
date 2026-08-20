package com.campushub.algorithms.optimization;

import com.campushub.algorithms.graph.Dijkstra;
import com.campushub.model.Resource;
import com.campushub.model.ServiceRequest;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.linear.DynamicArray;

// Owner: Graphs and Optimization

/**
 * Greedy dispatch: assigns service requests to the resources that will serve them.
 *
 * <p>Handle the most pressing request first and, for each, send the nearest resource
 * that can take it. Requests are ordered by urgency, then earliest deadline, then
 * largest accrued fine - money already being lost - then lowest id so the order is
 * total and reproducible. The eligible resource with the cheapest travel cost from its
 * home to the request's source wins, ties going to the lower resource id, and it loses
 * one unit of capacity.
 *
 * <p>Travel costs come from {@link Dijkstra} on the road network, not straight lines.
 * Running a search per request-resource pair would mean hundreds of thousands of
 * searches, so one runs per <em>distinct resource home</em> and the cost tables are
 * cached.
 *
 * <p><strong>Greedy is not optimal, which is the point of pairing it with
 * {@link KnapsackDP}.</strong> Serving the most urgent request first can consume the
 * one resource that was also the only cheap option for two later requests.
 * {@code GreedyAssignerTest} pins a small case where the greedy total is provably worse
 * than the best possible pairing.
 */
public final class GreedyAssigner {

    private GreedyAssigner() {
    }

    /** Decides whether a resource may take a request. */
    public interface Eligibility {
        boolean allows(ServiceRequest request, Resource resource);
    }

    /** Any available resource with spare capacity may take any request. */
    public static final Eligibility ANY_AVAILABLE = (request, resource) -> true;

    /**
     * Matches request categories to resource types: shuttle bookings need a vehicle,
     * lab-resource movements need equipment, everything else needs staff.
     *
     * <p>Offered rather than imposed. Whether dispatch is type-constrained is a policy
     * question for the Integration pod, so {@link #ANY_AVAILABLE} stays the default and
     * no rule is silently assumed.
     */
    public static final Eligibility MATCHING_TYPE = (request, resource) -> {
        String category = request.getCategory() == null
                ? "" : request.getCategory().toLowerCase();
        String type = resource.getType() == null ? "" : resource.getType().toLowerCase();
        if (category.startsWith("shuttle")) {
            return type.equals("vehicle");
        }
        if (category.startsWith("lab-resource")) {
            return type.equals("equipment");
        }
        return type.equals("staff");
    };

    /** One request paired with the resource sent to it. */
    public static final class Assignment {

        private final ServiceRequest request;
        private final Resource resource;
        private final double travelCost;

        Assignment(ServiceRequest request, Resource resource, double travelCost) {
            this.request = request;
            this.resource = resource;
            this.travelCost = travelCost;
        }

        public ServiceRequest request() {
            return request;
        }

        public Resource resource() {
            return resource;
        }

        /** Cost of getting the resource from its home to the request's source. */
        public double travelCost() {
            return travelCost;
        }

        @Override
        public String toString() {
            return "request " + request.getRequestId() + " -> resource " + resource.getResourceId()
                    + " (" + resource.getName() + ") at cost " + travelCost;
        }
    }

    /** A request no resource could take, and why. */
    public static final class Unassigned {

        private final ServiceRequest request;
        private final String reason;

        Unassigned(ServiceRequest request, String reason) {
            this.request = request;
            this.reason = reason;
        }

        public ServiceRequest request() {
            return request;
        }

        public String reason() {
            return reason;
        }

        @Override
        public String toString() {
            return "request " + request.getRequestId() + " unassigned: " + reason;
        }
    }

    /** Outcome of one dispatch run. */
    public static final class Result {

        private final DynamicArray<Assignment> assignments;
        private final DynamicArray<Unassigned> unassigned;
        private final int[] resourceIds;
        private final int[] loadPerResource;
        private final double totalTravelCost;
        private final int shortestPathSearches;
        private final int candidatesEvaluated;
        private final long elapsedNanos;

        Result(DynamicArray<Assignment> assignments, DynamicArray<Unassigned> unassigned,
               int[] resourceIds, int[] loadPerResource, double totalTravelCost,
               int shortestPathSearches, int candidatesEvaluated, long elapsedNanos) {
            this.assignments = assignments;
            this.unassigned = unassigned;
            this.resourceIds = resourceIds;
            this.loadPerResource = loadPerResource;
            this.totalTravelCost = totalTravelCost;
            this.shortestPathSearches = shortestPathSearches;
            this.candidatesEvaluated = candidatesEvaluated;
            this.elapsedNanos = elapsedNanos;
        }

        /** Assignments in the order the greedy rule made them. */
        public DynamicArray<Assignment> assignments() {
            return assignments;
        }

        /** Requests left unserved. */
        public DynamicArray<Unassigned> unassigned() {
            return unassigned;
        }

        /** Resource ids, aligned with {@link #loadPerResource()}. */
        public int[] resourceIds() {
            return resourceIds.clone();
        }

        /** How many requests each resource took on, aligned with {@link #resourceIds()}. */
        public int[] loadPerResource() {
            return loadPerResource.clone();
        }

        /** How many requests the given resource took on. */
        public int loadOf(int resourceId) {
            for (int index = 0; index < resourceIds.length; index++) {
                if (resourceIds[index] == resourceId) {
                    return loadPerResource[index];
                }
            }
            return 0;
        }

        public double totalTravelCost() {
            return totalTravelCost;
        }

        /** Mean travel cost per assignment, or 0 if nothing was assigned. */
        public double averageTravelCost() {
            return assignments.isEmpty() ? 0 : totalTravelCost / assignments.size();
        }

        public int assignedCount() {
            return assignments.size();
        }

        public int unassignedCount() {
            return unassigned.size();
        }

        /** Dijkstra runs performed - one per distinct resource home, not per pair. */
        public int shortestPathSearches() {
            return shortestPathSearches;
        }

        /** Request-resource pairs considered. */
        public int candidatesEvaluated() {
            return candidatesEvaluated;
        }

        public long elapsedNanos() {
            return elapsedNanos;
        }

        @Override
        public String toString() {
            return "GreedyAssigner: " + assignedCount() + " assigned, " + unassignedCount()
                    + " unassigned, total travel " + totalTravelCost + " over "
                    + shortestPathSearches + " shortest-path searches";
        }
    }

    /** Assigns requests using {@link #ANY_AVAILABLE} eligibility. */
    public static Result assign(Graph graph, DynamicArray<ServiceRequest> requests,
                                DynamicArray<Resource> resources) {
        return assign(graph, requests, resources, ANY_AVAILABLE);
    }

    /**
     * Assigns requests under a custom eligibility rule. Availability and remaining
     * capacity are enforced regardless of what {@code eligible} says.
     *
     * @throws IllegalArgumentException if any argument is null
     */
    public static Result assign(Graph graph, DynamicArray<ServiceRequest> requests,
                                DynamicArray<Resource> resources, Eligibility eligible) {
        return assign(graph, requests, resources, eligible, true, Double.POSITIVE_INFINITY);
    }

    /** Assigns with explicit availability and maximum travel-cost policies. */
    public static Result assign(Graph graph, DynamicArray<ServiceRequest> requests,
                                DynamicArray<Resource> resources, Eligibility eligible,
                                boolean requireAvailable, double maxTravelCost) {
        if (graph == null || requests == null || resources == null || eligible == null) {
            throw new IllegalArgumentException("graph, requests, resources and rule are required");
        }
        long startedAt = System.nanoTime();

        int resourceCount = resources.size();
        int[] resourceIds = new int[resourceCount];
        int[] remainingCapacity = new int[resourceCount];
        int[] homeSlot = new int[resourceCount];
        for (int index = 0; index < resourceCount; index++) {
            Resource resource = resources.get(index);
            resourceIds[index] = resource.getResourceId();
            remainingCapacity[index] = Math.max(0, resource.getCapacity());
            homeSlot[index] = graph.slotOf(resource.getHomeLocationId());
        }

        // One search per distinct home, reused across every request.
        int slotCount = graph.order();
        double[][] costFromSlot = new double[slotCount][];
        int searches = 0;
        for (int index = 0; index < resourceCount; index++) {
            int slot = homeSlot[index];
            if (slot == Graph.NO_SLOT || costFromSlot[slot] != null) {
                continue;
            }
            Dijkstra.Result search = Dijkstra.from(graph, graph.idAt(slot));
            double[] table = new double[slotCount];
            for (int target = 0; target < slotCount; target++) {
                table[target] = search.costTo(graph.idAt(target));
            }
            costFromSlot[slot] = table;
            searches++;
        }

        int[] order = priorityOrder(requests);
        DynamicArray<Assignment> assignments = new DynamicArray<>();
        DynamicArray<Unassigned> unassigned = new DynamicArray<>();
        int[] load = new int[resourceCount];
        double totalTravelCost = 0;
        int candidatesEvaluated = 0;

        for (int position = 0; position < order.length; position++) {
            ServiceRequest request = requests.get(order[position]);
            int destinationSlot = graph.slotOf(request.getSourceLocationId());

            int best = -1;
            double bestCost = Double.POSITIVE_INFINITY;
            boolean sawEligible = false;

            for (int index = 0; index < resourceCount; index++) {
                Resource resource = resources.get(index);
                if (remainingCapacity[index] <= 0
                    || (requireAvailable && !ServiceData.isAvailable(resource))) {
                    continue;
                }
                if (!eligible.allows(request, resource)) {
                    continue;
                }
                sawEligible = true;
                candidatesEvaluated++;

                if (destinationSlot == Graph.NO_SLOT || homeSlot[index] == Graph.NO_SLOT) {
                    continue;
                }
                double[] table = costFromSlot[homeSlot[index]];
                if (table == null) {
                    continue;
                }
                double cost = table[destinationSlot];
                if (cost == Dijkstra.UNREACHABLE) {
                    continue;
                }
                if (cost > maxTravelCost) {
                    continue;
                }
                // Resources are scanned in list order, so a strict comparison keeps the
                // first-seen winner and ties fall to the lower id.
                if (best < 0 || cost < bestCost) {
                    best = index;
                    bestCost = cost;
                }
            }

            if (best < 0) {
                unassigned.add(new Unassigned(request, sawEligible
                        ? "no eligible resource could reach the request's location"
                        : "no eligible resource with remaining capacity"));
                continue;
            }

            assignments.add(new Assignment(request, resources.get(best), bestCost));
            totalTravelCost += bestCost;
            remainingCapacity[best]--;
            load[best]++;
        }

        return new Result(assignments, unassigned, resourceIds, load, totalTravelCost, searches,
                candidatesEvaluated, System.nanoTime() - startedAt);
    }

    /**
     * Request indices in dispatch order. Merge sort over an index array, so equal
     * requests keep their load order and the whole ordering is reproducible.
     */
    static int[] priorityOrder(DynamicArray<ServiceRequest> requests) {
        int count = requests.size();
        int[] order = new int[count];
        for (int index = 0; index < count; index++) {
            order[index] = index;
        }
        mergeSort(order, new int[count], requests, 0, count - 1);
        return order;
    }

    private static void mergeSort(int[] order, int[] scratch,
                                 DynamicArray<ServiceRequest> requests, int low, int high) {
        if (low >= high) {
            return;
        }
        int middle = low + (high - low) / 2;
        mergeSort(order, scratch, requests, low, middle);
        mergeSort(order, scratch, requests, middle + 1, high);

        for (int index = low; index <= high; index++) {
            scratch[index] = order[index];
        }

        int left = low;
        int right = middle + 1;
        for (int target = low; target <= high; target++) {
            if (left > middle) {
                order[target] = scratch[right++];
            } else if (right > high) {
                order[target] = scratch[left++];
            } else if (comesFirst(requests.get(scratch[right]), requests.get(scratch[left]))) {
                order[target] = scratch[right++];
            } else {
                order[target] = scratch[left++];
            }
        }
    }

    /** True if {@code candidate} should be dispatched before {@code incumbent}. */
    private static boolean comesFirst(ServiceRequest candidate, ServiceRequest incumbent) {
        int byUrgency = ServiceData.urgencyRank(candidate.getUrgency())
                - ServiceData.urgencyRank(incumbent.getUrgency());
        if (byUrgency != 0) {
            return byUrgency > 0;
        }
        // Deadlines are ISO dates, so string order is date order. Blank sorts last.
        int byDeadline = compareDeadlines(candidate.getDeadline(), incumbent.getDeadline());
        if (byDeadline != 0) {
            return byDeadline < 0;
        }
        if (candidate.getFineAmountGHS() != incumbent.getFineAmountGHS()) {
            return candidate.getFineAmountGHS() > incumbent.getFineAmountGHS();
        }
        return candidate.getRequestId() < incumbent.getRequestId();
    }

    private static int compareDeadlines(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return left.compareTo(right);
    }
}
