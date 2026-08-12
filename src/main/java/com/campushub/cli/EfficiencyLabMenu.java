package com.campushub.cli;

import com.campushub.engine.EfficiencyLabEngine;
import java.util.Scanner;

public class EfficiencyLabMenu {
    private final Scanner scanner;
    private final EfficiencyLabEngine engine;

    public EfficiencyLabMenu(Scanner scanner, EfficiencyLabEngine engine) {
        this.scanner = scanner;
        this.engine = engine;
    }

    public void show() {
        System.out.println("\n--- Efficiency Lab ---");
        System.out.println("Running performance analysis on sorting algorithms...");
        System.out.println("This might take a few seconds.");
        
        String result = engine.analyzeEfficiency();
        System.out.println("Result:\n" + result);
    }
}

