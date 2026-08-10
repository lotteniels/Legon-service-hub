package com.campushub;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.campushub.engine.*;

public class ApiServer {
    private HttpServer server;
    private RouteEngine routeEngine;
    private RequestSchedulingEngine schedulingEngine;
    private OptimizationEngine optimizationEngine;
    private EfficiencyLabEngine efficiencyEngine;
    private IndexingEngine indexingEngine;

    public ApiServer(RouteEngine routeEngine, RequestSchedulingEngine schedulingEngine, OptimizationEngine optimizationEngine, EfficiencyLabEngine efficiencyEngine, IndexingEngine indexingEngine) {
        this.routeEngine = routeEngine;
        this.schedulingEngine = schedulingEngine;
        this.optimizationEngine = optimizationEngine;
        this.efficiencyEngine = efficiencyEngine;
        this.indexingEngine = indexingEngine;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/route", new RouteHandler());
        server.createContext("/api/schedule", new ScheduleHandler());
        server.createContext("/api/optimize", new OptimizeHandler());
        server.createContext("/api/efficiency", new EfficiencyHandler());
        server.createContext("/api/index", new IndexHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("API Server started on port 8080");
    }

    class RouteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = routeEngine.calculateShortestPath(1, 35);
            sendResponse(t, response);
        }
    }

    class ScheduleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = schedulingEngine.scheduleRequests();
            sendResponse(t, response);
        }
    }

    class OptimizeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = optimizationEngine.optimizeResources();
            sendResponse(t, response);
        }
    }

    class EfficiencyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = efficiencyEngine.analyzeEfficiency();
            sendResponse(t, response);
        }
    }

    class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = indexingEngine.buildIndex();
            sendResponse(t, response);
        }
    }

    private void sendResponse(HttpExchange t, String response) throws IOException {
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
