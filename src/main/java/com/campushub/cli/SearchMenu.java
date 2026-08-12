package com.campushub.cli;

import com.campushub.engine.IndexingEngine;
import java.util.Scanner;

public class SearchMenu {
    private final Scanner scanner;
    private final IndexingEngine engine;

    public SearchMenu(Scanner scanner, IndexingEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void show() {
        System.out.println("\n Search / Indexing ");
        System.out.println("1. Build Index (Load HashTables)");
        System.out.println("2. Search Location by ID");
        System.out.println("3. Search Request by ID");
        System.out.println("4. Search Resource by ID");
        System.out.println("5. Back to Main Menu");
        System.out.print("Choice: ");
        
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.println("Result: " + engine.buildIndex());
                break;
            case "2":
            case "3":
            case "4":
                System.out.print("Enter ID: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine());
                    String type = choice.equals("2") ? "location" : (choice.equals("3") ? "request" : "resource");
                    System.out.println("Result: " + engine.search(type, id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format.");
                }
                break;
            case "5":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}

