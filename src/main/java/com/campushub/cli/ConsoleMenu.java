package com.campushub.cli;

import com.campushub.algorithms.graph.BFS;
import com.campushub.algorithms.graph.DFS;
import com.campushub.engine.*;
import com.campushub.structures.graph.Graph;

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
        this.routeEngine       = routeEngine;
        this.schedulingEngine  = schedulingEngine;
        this.optimizationEngine = optimizationEngine;
        this.efficiencyEngine  = efficiencyEngine;
        this.indexingEngine    = indexingEngine;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n  Legon Service Hub (CLI) ");
            System.out.println("1. Dispatch / Schedule Requests");
            System.out.println("2. Route Finder (Dijkstra / BFS / MST)");
            System.out.println("3. Resource Optimisation (Greedy + DP)");
            System.out.println("4. Efficiency Lab");
            System.out.println("5. Search / Indexing");
            System.out.println("6. Graph Traversal (BFS / DFS reachability)");
            System.out.println("7. Exit");
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
                    showGraphTraversalMenu();
                    break;
                case "7":
                    System.out.println("Exiting Legon Service Hub...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void showGraphTraversalMenu() {
        System.out.println("\n Graph Traversal ");
        System.out.println("1. BFS — reachability from a location");
        System.out.println("2. DFS — depth-first traversal from a location");
        System.out.println("3. Back");
        System.out.print("Choice: ");
        String choice = scanner.nextLine();
        if ("3".equals(choice)) return;

        System.out.print("Enter Source Location ID: ");
        String src = scanner.nextLine();
        try {
            int source = Integer.parseInt(src.trim());
            Graph roads = routeEngine.graph();
            if (!roads.hasLocation(source)) {
                System.out.println("Unknown location: " + source);
                return;
            }
            if ("1".equals(choice)) {
                BFS.Result result = BFS.from(roads, source);
                System.out.printf("BFS from %s: reached %d locations, max depth %d hops, %d roads examined%n",
                        roads.nameOf(source), result.reachedCount(),
                        result.maxHops(), result.roadsExamined());
                System.out.print("Visit order: ");
                int[] order = result.visitOrder();
                for (int i = 0; i < order.length; i++) {
                    if (i > 0) System.out.print(" -> ");
                    System.out.print(roads.nameOf(order[i]));
                }
                System.out.println();
            } else if ("2".equals(choice)) {
                DFS.Result result = DFS.from(roads, source);
                System.out.printf("DFS from %s: reached %d locations, max depth %d, %d roads examined%n",
                        roads.nameOf(source), result.reachedCount(),
                        result.maxDepth(), result.roadsExamined());
                System.out.print("Preorder: ");
                int[] order = result.preorder();
                for (int i = 0; i < order.length; i++) {
                    if (i > 0) System.out.print(" -> ");
                    System.out.print(roads.nameOf(order[i]));
                }
                System.out.println();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid location ID.");
        }
    }
}
