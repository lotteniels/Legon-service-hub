package com.campushub.cli;

import com.campushub.engine.RouteEngine;
import java.util.Scanner;

public class RouteMenu {
    private final Scanner scanner;
    private final RouteEngine engine;

    public RouteMenu(Scanner scanner, RouteEngine engine) {
        this.scanner = scanner;
        this.engine  = engine;
    }

    public void show() {
        System.out.println("\n Route Finder ");
        System.out.println("1. Find Shortest Path (Dijkstra — cheapest cost)");
        System.out.println("2. Find Fewest Roads  (BFS — minimum hops)");
        System.out.println("3. Minimum Spanning Tree (Prim + Kruskal cross-check)");
        System.out.println("4. Network Summary");
        System.out.println("5. Back to Main Menu");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
            case "2": {
                System.out.print("Enter Source Location ID: ");
                String fromStr = scanner.nextLine();
                System.out.print("Enter Destination Location ID: ");
                String toStr = scanner.nextLine();
                try {
                    int from = Integer.parseInt(fromStr);
                    int to   = Integer.parseInt(toStr);
                    String result = "1".equals(choice)
                            ? engine.calculateShortestPath(from, to)
                            : engine.calculateFewestRoads(from, to);
                    System.out.println("Result: " + result);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid location ID format.");
                }
                break;
            }
            case "3":
                System.out.println("Result: " + engine.maintenanceNetwork());
                break;
            case "4":
                System.out.println("Result: " + engine.networkSummary());
                break;
            case "5":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
