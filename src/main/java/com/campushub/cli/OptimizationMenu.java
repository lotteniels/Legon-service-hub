package com.campushub.cli;

import com.campushub.engine.OptimizationEngine;
import java.util.Scanner;

public class OptimizationMenu {
    private final Scanner scanner;
    private final OptimizationEngine engine;

    public OptimizationMenu(Scanner scanner, OptimizationEngine engine) {
        this.scanner = scanner;
        this.engine  = engine;
    }

    public void show() {
        System.out.println("\n  Resource Optimisation  ");
        System.out.println("1. Greedy Resource Assignment (assign all outstanding requests)");
        System.out.println("2. DP Shift Planning (knapsack — best requests for one crew)");
        System.out.println("3. Compare Greedy vs Dynamic Programming");
        System.out.println("4. Back to Main Menu");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.println("Result: " + engine.optimizeResources());
                break;
            case "2": {
                System.out.print("Enter Depot Location ID: ");
                String depotStr = scanner.nextLine();
                System.out.print("Enter Shift Length in Minutes [default 240]: ");
                String shiftStr = scanner.nextLine();
                try {
                    int depot = Integer.parseInt(depotStr);
                    int shift = shiftStr.trim().isEmpty()
                            ? OptimizationEngine.DEFAULT_SHIFT_MINUTES
                            : Integer.parseInt(shiftStr.trim());
                    System.out.println("Result: " + engine.planShift(depot, shift));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
                break;
            }
            case "3": {
                System.out.print("Enter Depot Location ID: ");
                String depotStr = scanner.nextLine();
                System.out.print("Enter Shift Length in Minutes [default 240]: ");
                String shiftStr = scanner.nextLine();
                try {
                    int depot = Integer.parseInt(depotStr);
                    int shift = shiftStr.trim().isEmpty()
                            ? OptimizationEngine.DEFAULT_SHIFT_MINUTES
                            : Integer.parseInt(shiftStr.trim());
                    System.out.println("Result: "
                            + engine.compareGreedyWithDynamicProgram(depot, shift));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
                break;
            }
            case "4":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
