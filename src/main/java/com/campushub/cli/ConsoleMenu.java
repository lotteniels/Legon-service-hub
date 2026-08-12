package com.campushub.cli;

import com.campushub.engine.*;
import java.util.Scanner;

public class ConsoleMenu {
    private final RouteEngine routeEngine;
    private final RequestSchedulingEngine schedulingEngine;
    private final OptimizationEngine optimizationEngine;
    private final EfficiencyLabEngine efficiencyEngine;
    private final IndexingEngine indexingEngine;
    private final Scanner scanner;

    public ConsoleMenu(RouteEngine routeEngine, RequestSchedulingEngine schedulingEngine,
                       OptimizationEngine optimizationEngine, EfficiencyLabEngine efficiencyEngine,
                       IndexingEngine indexingEngine) {
        this.routeEngine = routeEngine;
        this.schedulingEngine = schedulingEngine;
        this.optimizationEngine = optimizationEngine;
        this.efficiencyEngine = efficiencyEngine;
        this.indexingEngine = indexingEngine;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n  Legon Service Hub (CLI) ");
            System.out.println("1. Dispatch / Schedule Requests");
            System.out.println("2. Route Finder");
            System.out.println("3. Resource Optimization");
            System.out.println("4. Efficiency Lab");
            System.out.println("5. Search / Indexing");
            System.out.println("6. Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    new DispatchMenu(scanner, schedulingEngine).show();
                    break;
                case "2":
                    new RouteMenu(scanner, routeEngine).show();
                    break;
                case "3":
                    new OptimizationMenu(scanner, optimizationEngine).show();
                    break;
                case "4":
                    new EfficiencyLabMenu(scanner, efficiencyEngine).show();
                    break;
                case "5":
                    new SearchMenu(scanner, indexingEngine).show();
                    break;
                case "6":
                    System.out.println("Exiting Legon Service Hub...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}

