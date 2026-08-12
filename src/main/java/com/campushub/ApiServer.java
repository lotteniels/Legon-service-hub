package com.campushub;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.sql.SQLException;
import com.campushub.structures.priority.HashTable;
import com.campushub.db.*;
import com.campushub.model.*;
import com.campushub.engine.*;

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
        this.routeEngine = routeEngine;
        this.schedulingEngine = schedulingEngine;
        this.optimizationEngine = optimizationEngine;
        this.efficiencyEngine = efficiencyEngine;
        this.indexingEngine = indexingEngine;
        this.locationRepo = new LocationRepository();
        this.requestRepo = new RequestRepository();
        this.resourceRepo = new ResourceRepository();
        this.auditRepo = new AuditEventRepository();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Data endpoints
        server.createContext("/api/locations", t -> handle(t, this::handleLocations));
        server.createContext("/api/requests",  t -> handle(t, this::handleRequests));
        server.createContext("/api/resources", t -> handle(t, this::handleResources));
        server.createContext("/api/audit",     t -> handle(t, this::handleAudit));

        // Engine endpoints
        server.createContext("/api/route",      t -> handle(t, this::handleRoute));
        server.createContext("/api/schedule",   t -> handle(t, this::handleSchedule));
        server.createContext("/api/undo",       t -> handle(t, ex -> schedulingEngine.undoLastDispatch()));
        server.createContext("/api/index",      t -> handle(t, this::handleIndex));
        server.createContext("/api/efficiency", t -> handle(t, this::handleEfficiency));
        server.createContext("/api/efficiency/run", t -> handle(t, this::handleEfficiency));
        server.createContext("/api/sort",       t -> handle(t, this::handleEfficiency)); // For now, /api/sort runs the whole efficiency lab
        server.createContext("/api/optimize",   t -> handle(t, this::handleOptimize));

        // Graph endpoints (waiting on Graphs team — hmmm someway oo)
        server.createContext("/api/bfs",  t -> handle(t, ex -> "{\"status\": \"not_yet_available\", \"message\": \"BFS not yet implemented by Graphs team\"}"));
        server.createContext("/api/dfs",  t -> handle(t, ex -> "{\"status\": \"not_yet_available\", \"message\": \"DFS not yet implemented by Graphs team\"}"));
        server.createContext("/api/mst",  t -> handle(t, ex -> "{\"status\": \"not_yet_available\", \"message\": \"MST not yet implemented by Graphs team\"}"));


        server.setExecutor(null);
        server.start();
        System.out.println("API Server started on port 8080");
    }

    // Data Handlers

    private String handleLocations(HttpExchange t) throws SQLException {
        var list = locationRepo.getAllLocations();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Location l = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"locationId\":%d,\"name\":\"%s\",\"area\":\"%s\",\"type\":\"%s\",\"coordinates\":\"%s\"}",
                l.getLocationId(), esc(l.getName()), esc(l.getArea()), esc(l.getType()), esc(l.getCoordinates())));
        }
        return sb.append("]").toString();
    }

    private String handleRequests(HttpExchange t) throws SQLException {
        var list = requestRepo.getAllRequests();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            ServiceRequest r = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"requestId\":%d,\"sourceLocationId\":%d,\"destinationLocationId\":%d," +
                "\"category\":\"%s\",\"urgency\":\"%s\",\"timeSubmitted\":\"%s\",\"deadline\":\"%s\",\"status\":\"%s\",\"fineAmountGHS\":%.2f}",
                r.getRequestId(), r.getSourceLocationId(), r.getDestinationLocationId(),
                esc(r.getCategory()), esc(r.getUrgency()), r.getTimeSubmitted(), r.getDeadline(),
                esc(r.getStatus()), r.getFineAmountGHS()));
        }
        return sb.append("]").toString();
    }

    private String handleResources(HttpExchange t) throws SQLException {
        var list = resourceRepo.getAllResources();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Resource r = list.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"resourceId\":%d,\"type\":\"%s\",\"name\":\"%s\",\"homeLocationId\":%d,\"capacity\":%d,\"availabilityStatus\":\"%s\"}",
                r.getResourceId(), esc(r.getType()), esc(r.getName()), r.getHomeLocationId(), r.getCapacity(), esc(r.getAvailabilityStatus())));
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
            sb.append(String.format("{\"eventId\":%d,\"eventType\":\"%s\",\"description\":\"%s\",\"timestamp\":\"%s\"}",
                e.getEventId(), esc(e.getEventType()), esc(e.getDescription()), e.getTimestamp()));
        }
        return sb.append("]").toString();
    }

    // Engine Handlers 

    private String handleRoute(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        int from = parseInt(params.get("from"), 1);
        int to   = parseInt(params.get("to"), 35);
        return routeEngine.calculateShortestPath(from, to);
    }

    private String handleSchedule(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String mode = params.get("mode");
        if (mode == null) mode = "priority";
        return "fifo".equalsIgnoreCase(mode)
            ? schedulingEngine.scheduleRequestsFIFO()
            : schedulingEngine.scheduleRequests();
    }

    private String handleIndex(HttpExchange t) {
        HashTable<String, String> params = parseQuery(t.getRequestURI());
        String type = params.get("type");
        String idStr = params.get("id");
        if (type == null || idStr == null) {
            return indexingEngine.buildIndex();
        }
        return indexingEngine.search(type, parseInt(idStr, -1));
    }

    private String handleEfficiency(HttpExchange t) {
        return efficiencyEngine.analyzeEfficiency();
    }

    private String handleOptimize(HttpExchange t) {
        return optimizationEngine.optimizeResources();
    }

    //  Utilities 

    @FunctionalInterface
    interface ApiHandler {
        String handle(HttpExchange t) throws Exception;
    }

    private void handle(HttpExchange t, ApiHandler handler) throws IOException {
        // CORS headers so any  browser can call us from any origin
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
            response = "{\"error\": \"" + e.getMessage() + "\"}";
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
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return defaultVal; }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}

