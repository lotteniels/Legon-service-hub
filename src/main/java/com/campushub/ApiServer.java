package com.campushub;

import com.campushub.algorithms.graph.BFS;
import com.campushub.algorithms.graph.DFS;
import com.campushub.algorithms.graph.Kruskal;
import com.campushub.algorithms.graph.Prim;
import com.campushub.db.*;
import com.campushub.engine.*;
import com.campushub.model.*;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.priority.HashTable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;

/**
 * REST API server exposing all engine capabilities on port 8080.
 *
 * <p>All responses are JSON. CORS headers are set on every response so
 * the dashboard can call from any origin.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/locations</li>
 *   <li>GET /api/requests</li>
 *   <li>GET /api/resources</li>
 *   <li>GET /api/audit</li>
 *   <li>GET /api/route?from=ID&amp;to=ID[&amp;mode=shortest|hops|mst|summary]</li>
 *   <li>GET /api/schedule?mode=priority|fifo|circular|deque</li>
 *   <li>GET /api/undo</li>
 *   <li>GET /api/index[?type=location|location/rbt|location/btree|request|resource&amp;id=ID]</li>
 *   <li>GET /api/efficiency</li>
 *   <li>GET /api/export  — triggers CSV export of algorithm_runs</li>
 *   <li>GET /api/optimize[?depot=ID&amp;shift=MINUTES]</li>
 *   <li>GET /api/bfs?from=ID</li>
 *   <li>GET /api/dfs?from=ID</li>
 *   <li>GET /api/mst[?algo=prim|kruskal]</li>
 * </ul>
 */
public class ApiServer {

    private HttpServer server;
    private final RouteEngine routeEngine;
    private final RequestSchedulingEngine schedulingEngine;
    private final OptimizationEngine optimizationEngine;
    private final EfficiencyLabEngine efficiencyEngine;
    private final IndexingEngine indexingEngine;
    private final LocationRepository locationRepo;
    private final RequestRepository requestRepo;
    private final ResourceRepository resourceRepo;
    private final AuditEventRepository auditRepo;

    public ApiServer(RouteEngine routeEngine, RequestSchedulingEngine schedulingEngine,
                     OptimizationEngine optimizationEngine, EfficiencyLabEngine efficiencyEngine,
                     IndexingEngine indexingEngine) {
        this.routeEngine       = routeEngine;
        this.schedulingEngine  = schedulingEngine;
        this.optimizationEngine = optimizationEngine;
        this.efficiencyEngine  = efficiencyEngine;
        this.indexingEngine    = indexingEngine;
        this.locationRepo = new LocationRepository();
        this.requestRepo  = new RequestRepository();
        this.resourceRepo = new ResourceRepository();
        this.auditRepo    = new AuditEventRepository();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Data endpoints
        server.createContext("/api/locations", t -> handle(t, this::handleLocations));
        server.createContext("/api/requests",  t -> handle(t, this::handleRequests));
        server.createContext("/api/resources", t -> handle(t, this::handleResources));
        server.createContext("/api/audit",     t -> handle(t, this::handleAudit));

        // Scheduling
        server.createContext("/api/schedule", t -> handle(t, this::handleSchedule));
        server.createContext("/api/undo",     t -> handle(t, ex -> schedulingEngine.undoLastDispatch()));

        // Routing (Dijkstra, BFS fewest-hops, Prim/Kruskal MST)
        server.createContext("/api/route", t -> handle(t, this::handleRoute));
        server.createContext("/api/bfs",   t -> handle(t, this::handleBfs));
        server.createContext("/api/dfs",   t -> handle(t, this::handleDfs));
        server.createContext("/api/mst",   t -> handle(t, this::handleMst));

        // Indexing
        server.createContext("/api/index", t -> handle(t, this::handleIndex));

        // Efficiency lab + CSV export
        server.createContext("/api/efficiency",     t -> handle(t, this::handleEfficiency));
        server.createContext("/api/efficiency/run", t -> handle(t, this::handleEfficiency));
        server.createContext("/api/sort",            t -> handle(t, this::handleEfficiency));
        server.createContext("/api/export",          t -> handle(t, this::handleExport));

        // Optimisation (Greedy + DP)
        server.createContext("/api/optimize",     t -> handle(t, this::handleOptimize));
        server.createContext("/api/shift",         t -> handle(t, this::handleShift));
        server.createContext("/api/compare",       t -> handle(t, this::handleCompare));

        server.setExecutor(null);
        server.start();
        System.out.println("API Server started on port 8080");
    }

    // =========================================================================
    // Data handlers
    // =========================================================================

    private String handleLocations(HttpExchange t) throws SQLException {
        var list = locationRepo.getAllLocations();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Location l = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"locationId\":%d,\"name\":\"%s\",\"area\":\"%s\","
                    + "\"type\":\"%s\",\"coordinates\":\"%s\"}",
                l.getLocationId(), esc(l.getName()), esc(l.getArea()),
                esc(l.getType()), esc(l.getCoordinates())));
        }
        return sb.append("]").toString();
    }

    private String handleRequests(HttpExchange t) throws SQLException {
        var list = requestRepo.getAllRequests();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            ServiceRequest r = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"requestId\":%d,\"sourceLocationId\":%d,"
                    + "\"destinationLocationId\":%d,\"category\":\"%s\","
                    + "\"urgency\":\"%s\",\"timeSubmitted\":\"%s\","
                    + "\"deadline\":\"%s\",\"status\":\"%s\","
                    + "\"fineAmountGHS\":%.2f}",
                r.getRequestId(), r.getSourceLocationId(),
                r.getDestinationLocationId(), esc(r.getCategory()),
                esc(r.getUrgency()), r.getTimeSubmitted(),
                r.getDeadline(), esc(r.getStatus()), r.getFineAmountGHS()));
        }
        return sb.append("]").toString();
    }

    private String handleResources(HttpExchange t) throws SQLException {
        var list = resourceRepo.getAllResources();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Resource r = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"resourceId\":%d,\"type\":\"%s\",\"name\":\"%s\","
                    + "\"homeLocationId\":%d,\"capacity\":%d,"
                    + "\"availabilityStatus\":\"%s\"}",
                r.getResourceId(), esc(r.getType()), esc(r.getName()),
                r.getHomeLocationId(), r.getCapacity(),
                esc(r.getAvailabilityStatus())));
        }
        return sb.append("]").toString();
    }

    private String handleAudit(HttpExchange t) throws SQLException {
        var list = auditRepo.getAllEvents();
        StringBuilder sb = new StringBuilder("[");
        int limit = Math.min(list.size(), 20);
        for (int i = 0; i < limit; i++) {
            AuditEvent e = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format(
                "{\"eventId\":%d,\"eventType\":\"%s\","
                    + "\"description\":\"%s\",\"timestamp\":\"%s\"}",
                e.getEventId(), esc(e.getEventType()),
                esc(e.getDescription()), e.getTimestamp()));
        }
        return sb.append("]").toString();
    }

    // =========================================================================
    // Scheduling handler
    // =========================================================================

    private String handleSchedule(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String mode = params.get("mode");
        if (mode == null) mode = "priority";
        switch (mode.toLowerCase()) {
            case "fifo":     return schedulingEngine.scheduleRequestsFIFO();
            case "circular": return schedulingEngine.scheduleCircularZone();
            case "deque":    return schedulingEngine.scheduleDequeUrgent();
            default:         return schedulingEngine.scheduleRequests();
        }
    }

    // =========================================================================
    // Route / Graph handlers
    // =========================================================================

    /**
     * GET /api/route?from=ID&to=ID[&mode=shortest|hops|mst|summary]
     *
     * <ul>
     *   <li>{@code shortest} (default) — Dijkstra cheapest-cost path</li>
     *   <li>{@code hops} — BFS fewest-roads path</li>
     *   <li>{@code mst} — minimum spanning tree summary</li>
     *   <li>{@code summary} — network summary (node/edge counts)</li>
     * </ul>
     */
    private String handleRoute(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String modeRaw = params.get("mode");
        String mode = modeRaw != null ? modeRaw : "shortest";
        switch (mode.toLowerCase()) {
            case "hops": {
                int from = parseInt(params.get("from"), 1);
                int to   = parseInt(params.get("to"),  35);
                return jsonString(routeEngine.calculateFewestRoads(from, to));
            }
            case "mst":
                return jsonString(routeEngine.maintenanceNetwork());
            case "summary":
                return jsonString(routeEngine.networkSummary());
            default: {
                int from = parseInt(params.get("from"), 1);
                int to   = parseInt(params.get("to"),  35);
                return jsonString(routeEngine.calculateShortestPath(from, to));
            }
        }
    }

    /**
     * GET /api/bfs?from=ID
     * Runs BFS from the given location and returns reachability statistics.
     */
    private String handleBfs(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        int from = parseInt(params.get("from"), 1);
        Graph roads = routeEngine.graph();
        if (!roads.hasLocation(from)) {
            return "{\"error\":\"Unknown location: " + from + "\"}";
        }
        BFS.Result result = BFS.from(roads, from);
        int[] visited = result.visitOrder();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"source\":").append(from)
          .append(",\"reachedCount\":").append(result.reachedCount())
          .append(",\"maxHops\":").append(result.maxHops())
          .append(",\"roadsExamined\":").append(result.roadsExamined())
          .append(",\"elapsedNs\":").append(result.elapsedNanos())
          .append(",\"visitOrder\":[");
        for (int i = 0; i < visited.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(visited[i]);
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * GET /api/dfs?from=ID
     * Runs iterative DFS from the given location.
     */
    private String handleDfs(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        int from = parseInt(params.get("from"), 1);
        Graph roads = routeEngine.graph();
        if (!roads.hasLocation(from)) {
            return "{\"error\":\"Unknown location: " + from + "\"}";
        }
        DFS.Result result = DFS.from(roads, from);
        int[] preorder = result.preorder();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"source\":").append(from)
          .append(",\"reachedCount\":").append(result.reachedCount())
          .append(",\"maxDepth\":").append(result.maxDepth())
          .append(",\"roadsExamined\":").append(result.roadsExamined())
          .append(",\"elapsedNs\":").append(result.elapsedNanos())
          .append(",\"preorder\":[");
        for (int i = 0; i < preorder.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(preorder[i]);
        }
        sb.append("]}");
        return sb.toString();
    }

    /**
     * GET /api/mst[?algo=prim|kruskal]
     * Returns the minimum spanning tree via Prim (default) or Kruskal.
     */
    private String handleMst(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String algoRaw = params.get("algo");
        String algo = algoRaw != null ? algoRaw : "kruskal";
        Graph roads = routeEngine.graph();
        if (roads.order() == 0) {
            return "{\"error\":\"No road network loaded\"}";
        }

        if ("prim".equalsIgnoreCase(algo)) {
            Prim.Result result = Prim.of(roads);
            return String.format(
                "{\"algorithm\":\"Prim\",\"roadCount\":%d,\"totalCost\":%.4f,"
                    + "\"locationsSpanned\":%d,\"spansWholeGraph\":%b,"
                    + "\"heapComparisons\":%d,\"elapsedNs\":%d}",
                result.roadCount(), result.totalCost(),
                result.locationsSpanned(), result.spansWholeGraph(),
                result.heapComparisons(), result.elapsedNanos());
        } else {
            Kruskal.Result result = Kruskal.of(roads);
            return String.format(
                "{\"algorithm\":\"Kruskal\",\"roadCount\":%d,\"totalCost\":%.4f,"
                    + "\"componentCount\":%d,\"spansWholeGraph\":%b,"
                    + "\"findCalls\":%d,\"sortComparisons\":%d,\"elapsedNs\":%d}",
                result.roadCount(), result.totalCost(),
                result.componentCount(), result.spansWholeGraph(),
                result.findCalls(), result.comparisons(), result.elapsedNanos());
        }
    }

    // =========================================================================
    // Indexing handler
    // =========================================================================

    private String handleIndex(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String type  = params.get("type");
        String idStr = params.get("id");
        if (type == null || idStr == null) {
            return indexingEngine.buildIndex();
        }
        return indexingEngine.search(type, parseInt(idStr, -1));
    }

    // =========================================================================
    // Efficiency lab handlers
    // =========================================================================

    private String handleEfficiency(HttpExchange t) {
        return efficiencyEngine.analyzeEfficiency();
    }

    private String handleExport(HttpExchange t) {
        efficiencyEngine.exportToCsv();
        return "{\"status\":\"exported\","
             + "\"file\":\"database/algorithm_runs_export.csv\"}";
    }

    // =========================================================================
    // Optimisation handlers
    // =========================================================================

    private String handleOptimize(HttpExchange t) {
        return jsonString(optimizationEngine.optimizeResources());
    }

    /**
     * GET /api/shift?depot=ID[&shift=MINUTES]
     * DP shift planning from a given depot location.
     */
    private String handleShift(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        int depot = parseInt(params.get("depot"), 1);
        int shift = parseInt(params.get("shift"),
                OptimizationEngine.DEFAULT_SHIFT_MINUTES);
        return jsonString(optimizationEngine.planShift(depot, shift));
    }

    /**
     * GET /api/compare?depot=ID[&shift=MINUTES]
     * Side-by-side greedy vs DP comparison.
     */
    private String handleCompare(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        int depot = parseInt(params.get("depot"), 1);
        int shift = parseInt(params.get("shift"),
                OptimizationEngine.DEFAULT_SHIFT_MINUTES);
        return jsonString(optimizationEngine.compareGreedyWithDynamicProgram(depot, shift));
    }

    // =========================================================================
    // Infrastructure
    // =========================================================================

    @FunctionalInterface
    interface ApiHandler {
        String handle(HttpExchange t) throws Exception;
    }

    private void handle(HttpExchange t, ApiHandler handler) throws IOException {
        t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        t.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        t.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        t.getResponseHeaders().add("Content-Type", "application/json");

        if ("OPTIONS".equalsIgnoreCase(t.getRequestMethod())) {
            t.sendResponseHeaders(204, -1);
            return;
        }

        String response;
        try {
            response = handler.handle(t);
        } catch (Exception e) {
            response = "{\"error\":\"" + esc(e.getMessage()) + "\"}";
        }

        byte[] bytes = response.getBytes("UTF-8");
        t.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(bytes);
        }
    }

    private HashTable<String, String> parseQuery(URI uri) {
        HashTable<String, String> params = new HashTable<>();
        String query = uri.getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        return params;
    }

    private int parseInt(String s, int defaultVal) {
        if (s == null) return defaultVal;
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    /** Wraps a plain string result in a JSON object with a "result" key. */
    private String jsonString(String value) {
        return "{\"result\":\"" + esc(value) + "\"}";
    }
}
