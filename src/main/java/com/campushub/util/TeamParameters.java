package com.campushub.util;

/**
 * Derives required algorithm parameters from the team's student index numbers.
 * Fulfills the AI-resistance and localization requirements (Rubric Section 2).
 */
public final class TeamParameters {

    // The student index numbers for the team members (excluding #6)
    private static final int[] INDEX_NUMBERS = {
        22098316, 22056801, 22128981, 22233989, 22069268,
        22309657, 22388561, 22304655, 22396017, 22406063,
        22375144, 22382281, 22412662, 22299460
    };

    private TeamParameters() {
    }

    /**
     * Parameter 1: Minimum degree for the B-Tree page simulation.
     * Derived using: 2 + (sum of index numbers mod 4) -> 2..5 range.
     */
    public static int treeMinDegree() {
        long sum = sumIndexNumbers();
        return (int) (2 + (sum % 4));
    }

    /**
     * Parameter 2: Random seed for the Empirical Efficiency Lab benchmarks.
     * Derived using the sum of all index numbers.
     */
    public static long randomSeed() {
        return sumIndexNumbers();
    }

    /**
     * Parameter 3: Base urgency value (priority weight) for Knapsack DP.
     * Derived using: 10 + (sum mod 20), giving a value between 10.0 and 29.0.
     */
    public static double urgencyValue() {
        long sum = sumIndexNumbers();
        return 10.0 + (sum % 20);
    }

    private static long sumIndexNumbers() {
        long sum = 0;
        for (int index : INDEX_NUMBERS) {
            sum += Math.abs((long) index);
        }
        return sum;
    }
}
