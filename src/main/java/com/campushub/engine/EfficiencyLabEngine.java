package com.campushub.engine;

import com.campushub.algorithms.sort.MergeSort;
import com.campushub.algorithms.sort.QuickSort;
import com.campushub.algorithms.sort.InsertionSort;
import com.campushub.algorithms.sort.SelectionSort;
import com.campushub.algorithms.search.LinearSearch;
import com.campushub.algorithms.search.BinarySearch;
import com.campushub.structures.priority.HashTable;
import com.campushub.structures.priority.PriorityQueue;
import com.campushub.structures.tree.BST;
import com.campushub.structures.tree.RedBlackTree;
import com.campushub.db.AlgorithmRunRepository;
import com.campushub.model.AlgorithmRun;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

public class EfficiencyLabEngine {

    private final AlgorithmRunRepository runRepository;
    private final MergeSort mergeSort = new MergeSort();
    private final QuickSort quickSort = new QuickSort();
    private final InsertionSort insertionSort = new InsertionSort();
    private final SelectionSort selectionSort = new SelectionSort();
    private final LinearSearch linearSearch = new LinearSearch();
    private final BinarySearch binarySearch = new BinarySearch();
    private final Random random = new Random(42);

    public EfficiencyLabEngine() {
        this.runRepository = new AlgorithmRunRepository();
    }

    public String analyzeEfficiency() {
        StringBuilder json = new StringBuilder("[");
        boolean[] first = {true};

        runSortingExperiments(json, first);
        runSearchExperiments(json, first);
        runHashTableExperiments(json, first);
        runTreeExperiments(json, first);
        runHeapExperiments(json, first);

        json.append("]");
        return json.toString();
    }

    private void runSortingExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);
            recordRun(json, first, "MergeSort", size, () -> mergeSort.mergeSort(data.clone()));
            recordRun(json, first, "QuickSort", size, () -> quickSort.quickSort(data.clone()));
            recordRun(json, first, "InsertionSort", size, () -> insertionSort.insertionSort(data.clone()));
            recordRun(json, first, "SelectionSort", size, () -> selectionSort.selectionSort(data.clone()));
        }
    }

    private void runSearchExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);
            int target = data[random.nextInt(size)]; // Pick a random existing element

            recordRun(json, first, "LinearSearch", size, () -> linearSearch.linearSearch(data, target));

            // Binary search needs sorted data
            int[] sortedData = mergeSort.mergeSort(data);
            recordRun(json, first, "BinarySearch", size, () -> binarySearch.binarySearch(sortedData, target));
        }
    }

    private void runHashTableExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000, 20000};
        for (int size : inputSizes) {
            HashTable<Integer, String> table = new HashTable<>(size / 2); // Force load factor to increase
            recordRun(json, first, "HashTable_Insert", size, () -> {
                for (int i = 0; i < size; i++) {
                    table.put(i, "V");
                }
            });
            recordRun(json, first, "HashTable_Search", size, () -> {
                for (int i = 0; i < size; i++) {
                    table.get(i);
                }
            });
        }
    }

    private void runTreeExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);

            recordRun(json, first, "BST_Insert", size, () -> {
                BST bst = new BST();
                for (int key : data) bst.insert(key, "V");
            });

            recordRun(json, first, "RedBlackTree_Insert", size, () -> {
                RedBlackTree rbt = new RedBlackTree();
                for (int key : data) rbt.insert(key, "V");
            });
            
            BST bst = new BST();
            RedBlackTree rbt = new RedBlackTree();
            for (int key : data) {
                bst.insert(key, "V");
                rbt.insert(key, "V");
            }
            
            recordRun(json, first, "BST_Search", size, () -> {
                for (int key : data) bst.search(key);
            });
            
            recordRun(json, first, "RedBlackTree_Search", size, () -> {
                for (int key : data) rbt.search(key);
            });
        }
    }

    private void runHeapExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000, 20000};
        for (int size : inputSizes) {
            int[] priorities = generateRandomArray(size);

            recordRun(json, first, "PriorityQueue_Enqueue", size, () -> {
                PriorityQueue<String> pq = new PriorityQueue<>(size);
                for (int p : priorities) pq.enqueue("Req", p);
            });
            
            PriorityQueue<String> pq = new PriorityQueue<>(size);
            for (int p : priorities) pq.enqueue("Req", p);

            recordRun(json, first, "PriorityQueue_Dequeue", size, () -> {
                for (int i = 0; i < size; i++) pq.dequeue();
            });
        }
    }

    private void recordRun(StringBuilder json, boolean[] first, String name, int size, Runnable logic) {
        long start = System.nanoTime();
        logic.run();
        long timeNs = System.nanoTime() - start;

        String today = LocalDate.now().toString();
        AlgorithmRun run = new AlgorithmRun(0, name, size, (int) timeNs, 0, today);
        try {
            runRepository.saveRun(run);
        } catch (SQLException e) {
            System.err.println("Failed to save run: " + e.getMessage());
        }

        if (!first[0]) json.append(",");
        json.append(String.format(
            "{\"algorithm\": \"%s\", \"inputSize\": %d, \"timeNs\": %d}",
            name, size, timeNs
        ));
        first[0] = false;
    }

    private int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(100000);
        }
        return arr;
    }
}
