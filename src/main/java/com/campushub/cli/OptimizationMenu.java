package com.campushub.cli;

import com.campushub.engine.OptimizationEngine;
import java.util.Scanner;

public class OptimizationMenu {
    private final Scanner scanner;
    private final OptimizationEngine engine;

    public OptimizationMenu(Scanner scanner, OptimizationEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void show() {
        System.out.println("\n  Resource Optimization  ");
        System.out.println("1. Run Optimization (Under Construction by Graphs Team)");
        System.out.println("2. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        if ("1".equals(choice)) {
            System.out.println("Result: " + engine.optimizeResources());
        }
    }
}

