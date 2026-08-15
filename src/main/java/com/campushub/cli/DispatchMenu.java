package com.campushub.cli;

import com.campushub.engine.RequestSchedulingEngine;
import java.util.Scanner;

public class DispatchMenu {
    private final Scanner scanner;
    private final RequestSchedulingEngine engine;

    public DispatchMenu(Scanner scanner, RequestSchedulingEngine engine) {
        this.scanner = scanner;
        this.engine  = engine;
    }

    public void show() {
        System.out.println("\n Dispatch / Schedule Requests ");
        System.out.println("1. Dispatch Next Urgent Request (PriorityQueue — HIGH first)");
        System.out.println("2. Dispatch Next Oldest Request (FIFO Queue)");
        System.out.println("3. Dispatch by Zone Rotation    (CircularQueue)");
        System.out.println("4. Dispatch with Urgent Front   (Deque)");
        System.out.println("5. Undo Last Dispatch           (Stack)");
        System.out.println("6. Back to Main Menu");
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
                System.out.println("Result: " + engine.scheduleCircularZone());
                break;
            case "4":
                System.out.println("Result: " + engine.scheduleDequeUrgent());
                break;
            case "5":
                System.out.println("Result: " + engine.undoLastDispatch());
                break;
            case "6":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
