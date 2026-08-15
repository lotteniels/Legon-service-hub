package com.campushub.cli;

import com.campushub.engine.IndexingEngine;
import java.util.Scanner;

public class SearchMenu {
    private final Scanner scanner;
    private final IndexingEngine engine;

    public SearchMenu(Scanner scanner, IndexingEngine engine) {
        this.scanner = scanner;
        this.engine  = engine;
    }

    public void show() {
        System.out.println("\n Search / Indexing ");
        System.out.println("1. Build Index (BST + RedBlackTree + BTree + HashTables)");
        System.out.println("2. Search Location by ID (BST)");
        System.out.println("3. Search Location by ID (Red-Black Tree)");
        System.out.println("4. Search Location by ID (B-Tree)");
        System.out.println("5. Search Request by ID  (HashTable)");
        System.out.println("6. Search Resource by ID (HashTable)");
        System.out.println("7. Back to Main Menu");
        System.out.print("Choice: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.println("Result: " + engine.buildIndex());
                break;
            case "2":
            case "3":
            case "4":
            case "5":
            case "6": {
                System.out.print("Enter ID: ");
                try {
                    int id = Integer.parseInt(scanner.nextLine());
                    String type;
                    switch (choice) {
                        case "2":  type = "location";        break;
                        case "3":  type = "location/rbt";    break;
                        case "4":  type = "location/btree";  break;
                        case "5":  type = "request";         break;
                        default:   type = "resource";        break;
                    }
                    System.out.println("Result: " + engine.search(type, id));
                } catch (NumberFormatException e) {
                    System.out.println("Invalid ID format.");
                }
                break;
            }
            case "7":
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }
}
