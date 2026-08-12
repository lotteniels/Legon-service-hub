package com.campushub.cli;

import com.campushub.engine.RouteEngine;
import java.util.Scanner;

public class RouteMenu {
    private final Scanner scanner;
    private final RouteEngine engine;

    public RouteMenu(Scanner scanner, RouteEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void show() {
        System.out.println("\n Route Finder ");
        System.out.println("1. Find Shortest Path (Under Construction by Graphs Team)");
        System.out.println("2. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        if ("1".equals(choice)) {
            System.out.print("Enter Source Location ID: ");
            String fromStr = scanner.nextLine();
            System.out.print("Enter Destination Location ID: ");
            String toStr = scanner.nextLine();
            try {
                int from = Integer.parseInt(fromStr);
                int to = Integer.parseInt(toStr);
                System.out.println("Result: " + engine.calculateShortestPath(from, to));
            } catch (NumberFormatException e) {
                System.out.println("Invalid location ID format.");
            }
        }
    }
}

