package com.campushub.cli;

import com.campushub.engine.RequestSchedulingEngine;
import java.util.Scanner;

public class DispatchMenu {
    private final Scanner scanner;
    private final RequestSchedulingEngine engine;

    public DispatchMenu(Scanner scanner, RequestSchedulingEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void show() {
        System.out.println("\n--- Dispatch / Schedule Requests ---");
        System.out.println("1. Dispatch Next Urgent Request (Priority)");
        System.out.println("2. Dispatch Next Oldest Request (FIFO)");
        System.out.println("3. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.println("Result: " + engine.scheduleRequests());
                break;
            case "2":
                System.out.println("Result: " + engine.scheduleRequestsFIFO());
                break;
            case "3":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}

