package com.campushub.engine;

import com.campushub.algorithms.sort.MergeSort;
import com.campushub.algorithms.sort.QuickSort;
import com.campushub.algorithms.sort.InsertionSort;
import com.campushub.algorithms.sort.SelectionSort;
import com.campushub.db.AlgorithmRunRepository;
import com.campushub.model.AlgorithmRun;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

public class EfficiencyLabEngine {

    private final AlgorithmRunRepository runRepository;
    private final MergeSort mergeSort;
    private final QuickSort quickSort;
    private final InsertionSort insertionSort;
    private final SelectionSort selectionSort;
    private final Random random;

    public EfficiencyLabEngine() {
        this.runRepository = new AlgorithmRunRepository();
        this.mergeSort = new MergeSort();
        this.quickSort = new QuickSort();
        this.insertionSort = new InsertionSort();
        this.selectionSort = new SelectionSort();
        this.random = new Random(42);
    }

    // Run all 4 sorts at multiple input sizes and save timing results to DB
    public String analyzeEfficiency() {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        StringBuilder json = new StringBuilder("[");
        String today = LocalDate.now().toString();
        boolean first = true;

        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);

            String[][] algorithms = {
                {"MergeSort",     "mergeSort"},
                {"QuickSort",     "quickSort"},
                {"InsertionSort", "insertionSort"},
                {"SelectionSort", "selectionSort"}
            };

            for (String[] alg : algorithms) {
                String name = alg[0];
                long timeNs = timeAlgorithm(name, data.clone());

                AlgorithmRun run = new AlgorithmRun(0, name, size, (int) timeNs, 0, today);
                try {
                    runRepository.saveRun(run);
                } catch (SQLException e) {
                    System.err.println("Failed to save run: " + e.getMessage());
                }

                if (!first) json.append(",");
                json.append(String.format(
                    "{\"algorithm\": \"%s\", \"inputSize\": %d, \"timeNs\": %d}",
                    name, size, timeNs
                ));
                first = false;
            }
        }

        json.append("]");
        return json.toString();
    }

    private long timeAlgorithm(String name, int[] data) {
        long start = System.nanoTime();
        switch (name) {
            case "MergeSort":     mergeSort.mergeSort(data);         break;
            case "QuickSort":     quickSort.quickSort(data);         break;
            case "InsertionSort": insertionSort.insertionSort(data); break;
            case "SelectionSort": selectionSort.selectionSort(data); break;
        }
        return System.nanoTime() - start;
    }

    private int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(100000);
        }
        return arr;
    }
}

