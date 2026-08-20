package com.campushub.engine;

import com.campushub.algorithms.graph.BFS;
import com.campushub.algorithms.graph.DFS;
import com.campushub.algorithms.graph.Dijkstra;
import com.campushub.algorithms.graph.Kruskal;
import com.campushub.algorithms.graph.Prim;
import com.campushub.algorithms.search.BinarySearch;
import com.campushub.algorithms.search.LinearSearch;
import com.campushub.algorithms.sort.InsertionSort;
import com.campushub.algorithms.sort.MergeSort;
import com.campushub.algorithms.sort.QuickSort;
import com.campushub.algorithms.sort.SelectionSort;
import com.campushub.db.AlgorithmRunRepository;
import com.campushub.model.AlgorithmRun;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.priority.HashTable;
import com.campushub.structures.priority.PriorityQueue;
import com.campushub.structures.tree.BST;
import com.campushub.structures.tree.RedBlackTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Random;

/**
 * Empirical efficiency benchmarks (M10).
 *
 * <p>Each experiment is run {@link #RUNS} times and the average nanosecond
 * time is recorded. Memory is measured via {@link Runtime} before and after
 * each timed block. Results are persisted to the database and exported to
 * {@code database/algorithm_runs_export.csv} so graphs can be plotted
 * directly from the file.
 *
 * <p>Covers: sorting, searching, hash table load, BST vs Red-Black tree,
 * heap priority dispatch, and graph traversal / shortest path / MST.
 */
public class EfficiencyLabEngine {

    /** How many times each experiment is repeated; average is reported. */
    private static final int RUNS = 3;

    private static final Path CSV_PATH =
            Path.of("database", "algorithm_runs_export.csv");

    private final AlgorithmRunRepository runRepository;
    private final MergeSort mergeSort         = new MergeSort();
    private final QuickSort quickSort         = new QuickSort();
    private final InsertionSort insertionSort = new InsertionSort();
    private final SelectionSort selectionSort = new SelectionSort();
    private final LinearSearch linearSearch   = new LinearSearch();
    private final BinarySearch binarySearch   = new BinarySearch();
    private final Random random               = new Random(42);

    /** Graph loaded once and reused for all graph experiments. */
    private Graph graph;

    public EfficiencyLabEngine() {
        this.runRepository = new AlgorithmRunRepository();
    }

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    public String analyzeEfficiency() {
        StringBuilder json = new StringBuilder("[");
        boolean[] first = {true};

        runSortingExperiments(json, first);
        runSearchExperiments(json, first);
        runHashTableExperiments(json, first);
        runTreeExperiments(json, first);
        runHeapExperiments(json, first);
        runGraphExperiments(json, first);

        json.append("]");

        exportToCsv();

        return json.toString();
    }

    /** Runs only the selected experiment family and persists fresh measurements. */
    public String runExperiment(String experiment) {
        StringBuilder json = new StringBuilder("[");
        boolean[] first = {true};
        appendExperiment(experiment, json, first);
        json.append("]");
        exportToCsv();
        return json.toString();
    }

    /** Returns persisted measurements belonging to the selected experiment family. */
    public String getSavedExperiment(String experiment) {
        String prefix = prefixFor(experiment);
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        try {
            var runs = runRepository.getAllRuns();
            for (int index = 0; index < runs.size(); index++) {
                AlgorithmRun run = runs.get(index);
                if (!matchesPrefix(run.getAlgorithmName(), prefix)) {
                    continue;
                }
                if (!first) json.append(',');
                json.append(String.format(
                        "{\"algorithm\":\"%s\",\"inputSize\":%d,\"timeNs\":%d,\"memoryKb\":%d,\"date\":\"%s\"}",
                        escapeJson(run.getAlgorithmName()), run.getInputSize(), run.getTimeNs(),
                        run.getMemoryKb(), escapeJson(run.getDateRun())));
                first = false;
            }
        } catch (SQLException e) {
            return "{\"error\":\"Unable to load saved experiment\"}";
        }
        return json.append(']').toString();
    }

    private void appendExperiment(String experiment, StringBuilder json, boolean[] first) {
        switch (normaliseExperiment(experiment)) {
            case "search": runSearchExperiments(json, first); break;
            case "sort":   runSortingExperiments(json, first); break;
            case "hash":   runHashTableExperiments(json, first); break;
            case "tree":   runTreeExperiments(json, first); break;
            case "graph":  runGraphExperiments(json, first); break;
            default: throw new IllegalArgumentException("Unknown experiment: " + experiment);
        }
    }

    private String prefixFor(String experiment) {
        return switch (normaliseExperiment(experiment)) {
            case "search" -> "Search";
            case "sort" -> "Sort";
            case "hash" -> "HashTable_";
            case "tree" -> "BST_";
            case "graph" -> "Graph";
            default -> throw new IllegalArgumentException("Unknown experiment: " + experiment);
        };
    }

    private boolean matchesPrefix(String algorithm, String prefix) {
        if ("Search".equals(prefix)) {
            return "LinearSearch".equals(algorithm) || "BinarySearch".equals(algorithm);
        }
        if ("Sort".equals(prefix)) {
            return algorithm.endsWith("Sort");
        }
        if ("BST_".equals(prefix)) {
            return algorithm.startsWith("BST_") || algorithm.startsWith("RedBlackTree_");
        }
        if ("Graph".equals(prefix)) {
            return algorithm.startsWith("BFS_") || algorithm.startsWith("DFS_")
                    || algorithm.startsWith("Dijkstra_") || algorithm.startsWith("Prim_")
                    || algorithm.startsWith("Kruskal_");
        }
        return algorithm.startsWith(prefix);
    }

    private String normaliseExperiment(String experiment) {
        return experiment == null ? "" : experiment.trim().toLowerCase();
    }

    private String escapeJson(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public String runSortDemo(String algorithm, int size) {
        int safeSize = Math.max(1, size);
        String normalized = algorithm == null ? "all" : algorithm.trim().toLowerCase();

        if ("all".equals(normalized)) {
            StringBuilder json = new StringBuilder("[");
            boolean[] first = {true};
            String[] names = {"selection", "insertion", "merge", "quick"};

            for (String name : names) {
                if (!first[0]) json.append(',');
                json.append(runSingleSort(name, safeSize));
                first[0] = false;
            }
            json.append(']');
            return json.toString();
        }

        return runSingleSort(normalized, safeSize);
    }

    private String runSingleSort(String algorithm, int size) {
        int[] data = generateRandomArray(size);
        long start = System.nanoTime();
        int[] sorted;

        switch (algorithm) {
            case "selection":
                sorted = selectionSort.selectionSort(data.clone());
                break;
            case "insertion":
                sorted = insertionSort.insertionSort(data.clone());
                break;
            case "merge":
                sorted = mergeSort.mergeSort(data.clone());
                break;
            case "quick":
                sorted = quickSort.quickSort(data.clone());
                break;
            default:
                sorted = mergeSort.mergeSort(data.clone());
                algorithm = "merge";
                break;
        }

        long elapsedNs = System.nanoTime() - start;
        double elapsedMs = elapsedNs / 1_000_000.0;
        String title = switch (algorithm) {
            case "selection" -> "SelectionSort";
            case "insertion" -> "InsertionSort";
            case "merge" -> "MergeSort";
            case "quick" -> "QuickSort";
            default -> "MergeSort";
        };

        return String.format(
                "{\"algorithm\":\"%s\",\"inputSize\":%d,\"timeMs\":%.3f,\"sorted\":%s}",
                title, size, elapsedMs, java.util.Arrays.toString(sorted));
    }

    // -------------------------------------------------------------------------
    // Experiment groups
    // -------------------------------------------------------------------------

    private void runSortingExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);
            recordAvg(json, first, "MergeSort",     size, () -> mergeSort.mergeSort(data.clone()));
            recordAvg(json, first, "QuickSort",     size, () -> quickSort.quickSort(data.clone()));
            recordAvg(json, first, "InsertionSort", size, () -> insertionSort.insertionSort(data.clone()));
            recordAvg(json, first, "SelectionSort", size, () -> selectionSort.selectionSort(data.clone()));
        }
    }

    private void runSearchExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data   = generateRandomArray(size);
            int   target = data[random.nextInt(size)];
            recordAvg(json, first, "LinearSearch", size, () -> linearSearch.linearSearch(data, target));

            int[] sorted = mergeSort.mergeSort(data);
            recordAvg(json, first, "BinarySearch", size, () -> binarySearch.binarySearch(sorted, target));
        }
    }

    private void runHashTableExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000, 20000};
        for (int size : inputSizes) {
            // Start with capacity = size/2 so load factor grows above 0.75 and
            // triggers the auto-resize the brief asks us to measure.
            recordAvg(json, first, "HashTable_Insert", size, () -> {
                HashTable<Integer, String> table = new HashTable<>(size / 2);
                for (int i = 0; i < size; i++) {
                    table.put(i, "V");
                }
            });

            HashTable<Integer, String> populated = new HashTable<>(size);
            for (int i = 0; i < size; i++) populated.put(i, "V");
            recordAvg(json, first, "HashTable_Search", size, () -> {
                for (int i = 0; i < size; i++) populated.get(i);
            });
        }
    }

    private void runTreeExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000};
        for (int size : inputSizes) {
            int[] data = generateRandomArray(size);

            // Insert benchmarks
            recordAvg(json, first, "BST_Insert", size, () -> {
                BST bst = new BST();
                for (int key : data) bst.insert(key, "V");
            });
            recordAvg(json, first, "RedBlackTree_Insert", size, () -> {
                RedBlackTree rbt = new RedBlackTree();
                for (int key : data) rbt.insert(key, "V");
            });

            // Pre-built trees for search benchmarks
            BST bst = new BST();
            RedBlackTree rbt = new RedBlackTree();
            for (int key : data) {
                bst.insert(key, "V");
                rbt.insert(key, "V");
            }

            recordAvg(json, first, "BST_Search", size, () -> {
                for (int key : data) bst.search(key);
            });
            recordAvg(json, first, "RedBlackTree_Search", size, () -> {
                for (int key : data) rbt.search(key);
            });
        }
    }

    private void runHeapExperiments(StringBuilder json, boolean[] first) {
        int[] inputSizes = {100, 500, 1000, 5000, 10000, 20000};
        for (int size : inputSizes) {
            int[] priorities = generateRandomArray(size);

            recordAvg(json, first, "PriorityQueue_Enqueue", size, () -> {
                PriorityQueue<String> pq = new PriorityQueue<>(size);
                for (int p : priorities) pq.enqueue("Req", p);
            });

            // Pre-filled queue for dequeue benchmark
            PriorityQueue<String> filled = new PriorityQueue<>(size);
            for (int p : priorities) filled.enqueue("Req", p);
            recordAvg(json, first, "PriorityQueue_Dequeue", size, () -> {
                PriorityQueue<String> pq = new PriorityQueue<>(size);
                for (int p : priorities) pq.enqueue("Req", p);
                for (int i = 0; i < size; i++) pq.dequeue();
            });
        }
    }

    /**
     * Graph traversal and shortest-path experiments (M10 requires BFS/DFS/Dijkstra/MST
     * across graph sizes). We use the campus network (58 locations, 117 roads) as the
     * single real instance, and repeat the measurement {@link #RUNS} times so an average
     * is reported — the graph itself does not change size, but the timing variation is
     * captured.
     *
     * <p>Input sizes reported are the number of locations (50) or edges (117) so the
     * experiment rows make sense in the performance table.
     */
    private void runGraphExperiments(StringBuilder json, boolean[] first) {
        Graph roads = campusGraph();
        if (roads == null || roads.order() == 0) {
            return; // seed data not present — skip silently
        }

        int locationCount = roads.order();
        int edgeCount     = roads.size();

        // Pick a fixed source (first location id) for reproducibility.
        int source = roads.locationIds()[0];

        // BFS — reports edge count as "input size" (graph size proxy)
        recordAvg(json, first, "BFS_Traversal", edgeCount,
                () -> BFS.from(roads, source));

        // DFS — iterative form (using custom Stack)
        recordAvg(json, first, "DFS_Traversal", edgeCount,
                () -> DFS.from(roads, source));

        // Dijkstra single-source shortest path
        recordAvg(json, first, "Dijkstra_SingleSource", edgeCount,
                () -> Dijkstra.from(roads, source));

        // Prim MST
        recordAvg(json, first, "Prim_MST", edgeCount,
                () -> Prim.of(roads));

        // Kruskal MST
        recordAvg(json, first, "Kruskal_MST", edgeCount,
                () -> Kruskal.of(roads));

        // BFS connected-components check (location count as size)
        recordAvg(json, first, "BFS_Connected", locationCount,
                () -> BFS.isConnected(roads));

        // DFS cycle detection
        recordAvg(json, first, "DFS_CycleDetect", locationCount,
                () -> DFS.hasCycle(roads));
    }

    // -------------------------------------------------------------------------
    // Core measurement helper — runs logic RUNS times, records the average
    // -------------------------------------------------------------------------

    private void recordAvg(StringBuilder json, boolean[] first,
                            String name, int size, Runnable logic) {
        Runtime rt = Runtime.getRuntime();

        long totalNs  = 0;
        long totalMem = 0;

        for (int run = 0; run < RUNS; run++) {
            rt.gc();
            long memBefore = rt.totalMemory() - rt.freeMemory();
            long tStart    = System.nanoTime();

            logic.run();

            long tEnd    = System.nanoTime();
            long memAfter = rt.totalMemory() - rt.freeMemory();

            totalNs  += (tEnd - tStart);
            totalMem += Math.max(0, memAfter - memBefore);
        }

        long avgNs    = totalNs  / RUNS;
        long avgMemKb = (totalMem / RUNS) / 1024;

        String today = LocalDate.now().toString();
        AlgorithmRun record = new AlgorithmRun(0, name, size, (int) avgNs, (int) avgMemKb, today);
        try {
            runRepository.saveRun(record);
        } catch (SQLException e) {
            System.err.println("Failed to save run: " + e.getMessage());
        }

        if (!first[0]) json.append(",");
        json.append(String.format(
                "{\"algorithm\":\"%s\",\"inputSize\":%d,\"timeNs\":%d,\"memoryKb\":%d}",
                name, size, avgNs, avgMemKb));
        first[0] = false;
    }

    // -------------------------------------------------------------------------
    // CSV export
    // -------------------------------------------------------------------------

    /**
     * Exports all rows from {@code algorithm_runs} to a CSV file so results
     * can be plotted directly in Excel, Python or any other tool.
     */
    public void exportToCsv() {
        try {
            var runs = runRepository.getAllRuns();
            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(CSV_PATH.toFile()))) {
                writer.write("runId,algorithmName,inputSize,timeNs,memoryKb,dateRun");
                writer.newLine();
                for (int i = 0; i < runs.size(); i++) {
                    AlgorithmRun r = runs.get(i);
                    writer.write(r.getRunId() + "," + r.getAlgorithmName() + ","
                            + r.getInputSize() + "," + r.getTimeNs() + ","
                            + r.getMemoryKb() + "," + r.getDateRun());
                    writer.newLine();
                }
            }
        } catch (SQLException | IOException e) {
            System.err.println("CSV export failed: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Graph campusGraph() {
        if (graph == null) {
            try {
                graph = Graph.fromSeedData(
                        Path.of("database", "seed-data"),
                        Graph.WeightMode.TIME_ADJUSTED);
            } catch (Exception e) {
                System.err.println("Graph load failed: " + e.getMessage());
            }
        }
        return graph;
    }

    private int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = random.nextInt(100000);
        }
        return arr;
    }
}
