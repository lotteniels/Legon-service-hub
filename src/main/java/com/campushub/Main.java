package com.campushub;

import com.campushub.db.DatabaseConnection;
import com.campushub.engine.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args){
        System.out.println("Starting Balme Service Optimizer...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("Successfully connected to the database!");
            } else {
                System.out.println("Failed to connect!");    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        RouteEngine routeEngine = new RouteEngine();
        RequestSchedulingEngine schedulingEngine = new RequestSchedulingEngine();
        OptimizationEngine optimizationEngine = new OptimizationEngine();
        EfficiencyLabEngine efficiencyEngine = new EfficiencyLabEngine();
        IndexingEngine indexingEngine = new IndexingEngine();

        try {
            ApiServer apiServer = new ApiServer(routeEngine, schedulingEngine, optimizationEngine, efficiencyEngine, indexingEngine);
            apiServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
