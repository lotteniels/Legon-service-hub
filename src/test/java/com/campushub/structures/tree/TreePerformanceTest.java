package com.campushub.structures.tree;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Performance experiment for the Tree Structures pod (Step 5 / performance folder).
 *
 * <p>WHAT IT MEASURES: how long it takes to insert and then search n items in
 * {@code BST}, {@code RedBlackTree} and {@code BTree}, for n = 100, 500, 1,000, 5,000 and
 * 10,000, three runs each, averaged.
 *
 * <p>IT RUNS THE WHOLE EXPERIMENT TWICE, once with the keys shuffled and once with them in
 * ascending order, because those two cases give opposite answers and only reporting the
 * first would hide the real finding. In random order an unbalanced BST is roughly balanced
 * by luck and looks fine. In ascending order it degenerates into a 10,000-long chain and
 * collapses to O(n) per operation, while the Red-Black Tree and B-Tree do not move. That
 * contrast is the point of the experiment.
 *
 * <p>HOW IT FINDS YOUR PARTNER'S TREES: by name, using reflection, so this file compiles
 * and runs today even if their files are not merged yet - they show up as SKIPPED. It
 * accepts an insert method named put/insert/add (one or two arguments) and a search method
 * named get/search/contains/containsKey/find, so it should pick their code up unedited.
 * Because all three trees are called through the same reflective path, that small overhead
 * is identical for each and the comparison stays fair.
 *
 * <p>Run it with:
 * {@code java -cp target/classes com.campushub.structures.tree.TreePerformanceTest}
 * <br>Optional args: {@code [outputDir] [keysCsv] [csvColumnIndex]}
 */
public final class TreePerformanceTest {

    /** Input sizes required by the brief. */
    private static final int[] SIZES = {100, 500, 1_000, 5_000, 10_000};

    /** Repeat each measurement this many times and average, to smooth out noise. */
    private static final int RUNS = 3;

    /** Classes to test, in the order they appear in the CSV and the graph. */
    private static final String[] STRUCTURES = {"BST", "RedBlackTree", "BTree"};

    private static final String PACKAGE = "com.campushub.structures.tree";

    private static final String[] INSERT_NAMES = {"put", "insert", "add"};
    private static final String[] SEARCH_NAMES = {"get", "search", "contains", "containsKey", "find"};
    private static final String[] HEIGHT_NAMES = {"height", "getHeight", "depth", "getDepth"};

    /**
     * TEAM INDEX NUMBERS - REPLACE THESE WITH YOUR REAL ONES BEFORE SUBMITTING.
     *
     * <p>Section 2 of the brief requires at least three algorithm parameters to be derived
     * from member index numbers. Three of them come from this array: the B-Tree's minimum
     * degree, the random seed that fixes the key order, and the offset used to build
     * guaranteed-miss lookups. That makes every number in your results table team-specific.
     */
    private static final int[] INDEX_NUMBERS = {11111111, 22222222, 33333333};

    /** Derived parameter 1: random seed, so the key order is team-specific but repeatable. */
    private static final long SEED = derivedSeed();

    /** Derived parameter 2: the offset that turns a stored key into a guaranteed miss. */
    private static final int MISS_OFFSET = derivedMissOffset();

    private static long derivedSeed() {
        long seed = 0;
        for (int i = 0; i < INDEX_NUMBERS.length; i++) {
            seed = seed * 31 + Math.abs((long) INDEX_NUMBERS[i]);
        }
        return seed;
    }

    private static int derivedMissOffset() {
        long sum = 0;
        for (int i = 0; i < INDEX_NUMBERS.length; i++) {
            sum += Math.abs((long) INDEX_NUMBERS[i]);
        }
        return 100_000_000 + (int) (sum % 100_000_000L);
    }

    /** Stop repeating a measurement once it has taken this long in total. */
    private static final long TIME_BUDGET_NANOS = 300_000_000L;   // 0.3 seconds

    /** Marker for a measurement that could not be completed (e.g. stack overflow). */
    private static final double FAILED = Double.NaN;

    public static void main(String[] args) throws Exception {
        Path outputDir = Paths.get(args.length > 0 ? args[0] : "performance/results");
        Path keysCsv = args.length > 1 ? Paths.get(args[1]) : null;
        int csvColumn = args.length > 2 ? Integer.parseInt(args[2]) : 0;

        Files.createDirectories(outputDir);
        printAndSaveMachineSpec(outputDir);

        System.out.println("Derived parameters (from index numbers "
                + java.util.Arrays.toString(INDEX_NUMBERS) + "):");
        System.out.println("  B-Tree minimum degree t = "
                + BTree.minDegreeFromIndexNumbers(INDEX_NUMBERS)
                + "  (max " + (2 * BTree.minDegreeFromIndexNumbers(INDEX_NUMBERS) - 1)
                + " keys per node)");
        System.out.println("  random seed             = " + SEED);
        System.out.println("  miss offset            = " + MISS_OFFSET);
        System.out.println();

        List<Integer> sourceKeys = loadKeys(keysCsv, csvColumn);
        System.out.println("Key source: "
                + (keysCsv == null ? "generated (no CSV given)" : keysCsv + " column " + csvColumn)
                + " -> " + sourceKeys.size() + " distinct keys available");

        // Load whichever tree classes exist right now.
        Map<String, TreeUnderTest> trees = new LinkedHashMap<>();
        for (String simpleName : STRUCTURES) {
            try {
                TreeUnderTest tree = TreeUnderTest.forClass(PACKAGE + "." + simpleName);
                trees.put(simpleName, tree);
                System.out.println("Found " + simpleName + "  (insert via " + tree.insertName()
                        + ", search via " + tree.searchName() + ")");
            } catch (ReflectiveOperationException | RuntimeException e) {
                System.out.println("SKIPPED " + simpleName + ": " + e.getMessage());
            }
        }
        if (trees.isEmpty()) {
            System.out.println("No tree classes could be loaded - nothing to measure.");
            return;
        }

        List<String> tested = new ArrayList<>(trees.keySet());
        List<String[]> everyRun = new ArrayList<>();
        List<String[]> algorithmRuns = new ArrayList<>();
        Map<String, Map<Integer, long[]>> profiles = new LinkedHashMap<>();

        for (boolean sorted : new boolean[] {false, true}) {
            String orderName = sorted ? "sorted" : "random";
            System.out.println();
            System.out.println("=== insertion order: " + orderName.toUpperCase() + " ===");

            Map<String, Map<Integer, double[]>> averages = new LinkedHashMap<>();
            for (Map.Entry<String, TreeUnderTest> entry : trees.entrySet()) {
                String name = entry.getKey();
                TreeUnderTest tree = entry.getValue();
                averages.put(name, new LinkedHashMap<Integer, double[]>());
                System.out.println("Testing " + name);

                warmUp(tree, sourceKeys);

                for (int size : SIZES) {
                    List<Integer> keys = keysOfSize(sourceKeys, size, sorted);
                    List<Integer> lookups = lookupsFor(keys);

                    // One extra un-timed build, to record tree height and memory used.
                    long[] profile = profileOnce(tree, keys);
                    profiles.computeIfAbsent(orderName + "/" + name,
                            new java.util.function.Function<String, Map<Integer, long[]>>() {
                                public Map<Integer, long[]> apply(String ignored) {
                                    return new LinkedHashMap<Integer, long[]>();
                                }
                            }).put(size, profile);

                    double insertSum = 0;
                    double searchSum = 0;
                    int good = 0;
                    String note = "";

                    for (int run = 1; run <= RUNS; run++) {
                        double[] result;
                        try {
                            result = measureOnce(tree, keys, lookups, size);
                        } catch (StackOverflowError e) {
                            // Not a bug in the harness. This IS the unbalanced-BST failure
                            // mode: sorted input makes a recursive BST recurse n levels deep.
                            result = new double[] {FAILED, FAILED};
                            note = "stack overflow (degenerate tree)";
                        } catch (Throwable t) {
                            result = new double[] {FAILED, FAILED};
                            note = t.getClass().getSimpleName();
                        }
                        if (!Double.isNaN(result[0])) {
                            insertSum += result[0];
                            searchSum += result[1];
                            good++;
                        }
                        everyRun.add(new String[] {
                                orderName, name, String.valueOf(size), String.valueOf(run),
                                format(result[0]), format(result[1]),
                                profile[0] < 0 ? "" : String.valueOf(profile[0]),
                                toKb(profile[1]), note
                        });
                        // Rows shaped for the algorithm_runs database table (brief section 4).
                        addAlgorithmRun(algorithmRuns, name + ".insert", orderName, size,
                                result[0], profile[1]);
                        addAlgorithmRun(algorithmRuns, name + ".search", orderName, size,
                                result[1], profile[1]);
                    }

                    double[] average = good == 0
                            ? new double[] {FAILED, FAILED}
                            : new double[] {insertSum / good, searchSum / good};
                    averages.get(name).put(size, average);

                    if (good == 0) {
                        System.out.printf("  n=%-6d FAILED: %s%n", size, note);
                    } else {
                        System.out.printf("  n=%-6d insert avg %10s ms   search avg %10s ms"
                                        + "   height %s   memory %s KB%n",
                                size, format(average[0]), format(average[1]),
                                profile[0] < 0 ? "n/a" : String.valueOf(profile[0]),
                                profile[1] < 0 ? "n/a" : toKb(profile[1]));
                    }
                }
            }

            Path summary = outputDir.resolve(sorted
                    ? "tree_performance_sorted.csv" : "tree_performance.csv");
            writeSummary(summary, tested, averages);
            System.out.println("Wrote " + summary.toAbsolutePath());
        }

        Path detail = outputDir.resolve("tree_performance_runs.csv");
        writeDetail(detail, everyRun);
        System.out.println("Wrote " + detail.toAbsolutePath());

        Path heights = outputDir.resolve("tree_height.csv");
        writeHeights(heights, tested, profiles);
        System.out.println("Wrote " + heights.toAbsolutePath());

        Path runsTable = outputDir.resolve("algorithm_runs_export.csv");
        writeAlgorithmRuns(runsTable, algorithmRuns);
        System.out.println("Wrote " + runsTable.toAbsolutePath()
                + "  (load this into the algorithm_runs table)");
        System.out.println();
        System.out.println("Next: chart tree_performance.csv and tree_performance_sorted.csv,");
        System.out.println("and save the pictures into performance/graphs/.");
    }

    // ------------------------------------------------------------------ measuring

    /**
     * Times one build-from-empty pass and one search pass.
     *
     * <p>Inserting 100 items takes microseconds, which is near the resolution of the system
     * clock, so a single measurement is mostly noise. We therefore repeat the whole pass
     * until either the repetition cap or a 0.3-second time budget is reached, then divide by
     * the number of passes actually completed. The number reported is still "time for one
     * pass of n items", just measured reliably. The time budget is what stops a degenerate
     * BST on sorted input from running for minutes.
     */
    private static double[] measureOnce(TreeUnderTest tree, List<Integer> keys,
                                        List<Integer> lookups, int size)
            throws ReflectiveOperationException {
        int maxReps = Math.max(1, 100_000 / size);
        Object instance = null;

        int insertReps = 0;
        long t0 = System.nanoTime();
        while (insertReps < maxReps) {
            instance = tree.newInstance();
            for (int i = 0; i < keys.size(); i++) {
                tree.insert(instance, keys.get(i));
            }
            insertReps++;
            if (System.nanoTime() - t0 > TIME_BUDGET_NANOS) {
                break;
            }
        }
        double insertMs = (System.nanoTime() - t0) / 1_000_000.0 / insertReps;

        int searchReps = 0;
        long t1 = System.nanoTime();
        while (searchReps < maxReps) {
            for (int i = 0; i < lookups.size(); i++) {
                tree.search(instance, lookups.get(i));
            }
            searchReps++;
            if (System.nanoTime() - t1 > TIME_BUDGET_NANOS) {
                break;
            }
        }
        double searchMs = (System.nanoTime() - t1) / 1_000_000.0 / searchReps;

        return new double[] {insertMs, searchMs};
    }

    /**
     * Builds the tree once, untimed, and records its height and how much heap it occupies.
     *
     * <p>Section 9 of the brief asks for a height comparison as well as a runtime one, and
     * the algorithm_runs table has a memoryKb column. Height is the honest way to show why
     * a balanced tree wins: it is a property of the structure, not of the machine, so unlike
     * a timing it cannot be blamed on background processes.
     *
     * <p>The memory figure is a heap-usage delta around the build, with garbage collection
     * requested either side. Treat it as indicative, not exact - the JVM is free to ignore
     * a gc request, and other threads allocate too. Say that when you present it.
     *
     * @return {height, memoryKb}, with -1 for anything that could not be measured
     */
    private static long[] profileOnce(TreeUnderTest tree, List<Integer> keys) {
        try {
            // Height first, from a single tree. It is deterministic, so one reading is enough.
            Object single = tree.newInstance();
            for (int i = 0; i < keys.size(); i++) {
                tree.insert(single, keys.get(i));
            }
            int height = tree.height(single);
            single = null;

            // Memory: measuring one tree does not work. A single 10,000-node tree is a few
            // hundred KB, which is the same order as the noise from garbage collection and
            // other threads, so readings come out wildly wrong or as zero. Instead we hold
            // MANY trees at once until the total is around 200,000 nodes - tens of megabytes,
            // far above the noise - and divide. This trades memory for a reading you can
            // actually defend.
            int copies = Math.max(1, 200_000 / keys.size());
            settle();
            long before = usedMemory();

            Object[] instances = new Object[copies];
            for (int c = 0; c < copies; c++) {
                instances[c] = tree.newInstance();
                for (int i = 0; i < keys.size(); i++) {
                    tree.insert(instances[c], keys.get(i));
                }
            }

            settle();
            long after = usedMemory();
            long bytesPerTree = Math.max(0L, (after - before) / copies);

            // Keep the array reachable until after the measurement.
            if (instances.length == Integer.MIN_VALUE) {
                System.out.print("");
            }
            return new long[] {height, bytesPerTree};
        } catch (Throwable t) {
            return new long[] {-1L, -1L};
        }
    }

    /** Bytes to kilobytes, with one decimal, since small trees are well under 1 KB. */
    private static String toKb(long bytes) {
        return bytes < 0 ? "" : String.format("%.1f", bytes / 1024.0);
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /** Asks for garbage collection twice with a pause, to make the memory delta less noisy. */
    private static void settle() {
        System.gc();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.gc();
    }

    /**
     * Section 9 requires the machine specification to be stated alongside the results,
     * because timings from different laptops are not comparable.
     */
    private static void printAndSaveMachineSpec(Path outputDir) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder spec = new StringBuilder();
        spec.append("Machine specification for the tree performance experiment\n");
        spec.append("Date run          : ").append(LocalDate.now()).append('\n');
        spec.append("Operating system  : ").append(System.getProperty("os.name"))
            .append(' ').append(System.getProperty("os.version"))
            .append(" (").append(System.getProperty("os.arch")).append(")\n");
        spec.append("Java version      : ").append(System.getProperty("java.version"))
            .append(" - ").append(System.getProperty("java.vm.name")).append('\n');
        spec.append("Logical CPUs      : ").append(runtime.availableProcessors()).append('\n');
        spec.append("JVM max heap      : ").append(runtime.maxMemory() / (1024 * 1024))
            .append(" MB\n");
        spec.append("CPU model         : TODO - fill in from Task Manager > Performance > CPU\n");
        spec.append("Installed RAM     : TODO - fill in from Task Manager > Performance > Memory\n");
        spec.append("\nAll experiments in this folder were run on this one machine.\n");

        System.out.print(spec);
        Files.write(outputDir.resolve("machine-spec.txt"),
                spec.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + outputDir.resolve("machine-spec.txt").toAbsolutePath());
        System.out.println();
    }

    /** Builds one row in the shape of the algorithm_runs table from section 4 of the brief. */
    private static void addAlgorithmRun(List<String[]> rows, String algorithmName,
                                        String orderName, int inputSize,
                                        double millis, long memoryBytes) {
        String timeNs = Double.isNaN(millis)
                ? "" : String.valueOf(Math.round(millis * 1_000_000.0));
        rows.add(new String[] {
                String.valueOf(rows.size() + 1),                 // runId
                algorithmName + " (" + orderName + " order)",     // algorithmName
                String.valueOf(inputSize),                        // inputSize
                timeNs,                                           // timeNs
                toKb(memoryBytes),                                // memoryKb
                LocalDate.now().toString()                        // dateRun
        });
    }

    /**
     * Runs a few thousand throwaway operations and discards the timings. The JVM compiles
     * hot code while it runs, so without this the first size measured looks artificially
     * slow and the graph is misleading. Always shuffled, so it cannot itself overflow.
     */
    private static void warmUp(TreeUnderTest tree, List<Integer> source) {
        try {
            for (int i = 0; i < 3; i++) {
                Object instance = tree.newInstance();
                List<Integer> keys = keysOfSize(source, 2_000, false);
                for (int j = 0; j < keys.size(); j++) {
                    tree.insert(instance, keys.get(j));
                }
                for (int j = 0; j < keys.size(); j++) {
                    tree.search(instance, keys.get(j));
                }
            }
        } catch (Throwable ignored) {
            // A warm-up failure is not interesting; the real runs will report it.
        }
    }

    // ------------------------------------------------------------------ key preparation

    /**
     * Reads keys from one column of a CSV, keeping the digits of each value. Anything
     * non-numeric becomes a stable number via its hash, so real request ids, location ids
     * and call numbers all work.
     */
    private static List<Integer> loadKeys(Path csv, int column) {
        List<Integer> keys = new ArrayList<>();
        if (csv != null && Files.exists(csv)) {
            try {
                List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
                for (int i = 1; i < lines.size(); i++) {          // skip the header row
                    String[] cells = lines.get(i).split(",");
                    if (column >= cells.length) {
                        continue;
                    }
                    String raw = cells[column].trim();
                    String digits = raw.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty() && digits.length() <= 9) {
                        keys.add(Integer.parseInt(digits));
                    } else if (!raw.isEmpty()) {
                        keys.add(Math.abs(raw.hashCode() % 1_000_000));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Could not read " + csv + " (" + e.getMessage()
                        + ") - falling back to generated keys.");
            }
        }
        if (keys.isEmpty()) {
            Random random = new Random(SEED);
            for (int i = 0; i < 10_000; i++) {
                keys.add(random.nextInt(1_000_000));
            }
        }
        // Duplicates must go: a duplicate insert is an update, not an insert, which would
        // quietly make n smaller than we claim it is.
        List<Integer> distinct = new ArrayList<>(new LinkedHashSet<Integer>(keys));
        Collections.shuffle(distinct, new Random(SEED));
        return distinct;
    }

    /**
     * Produces exactly {@code size} distinct keys, either shuffled or in ascending order.
     *
     * <p>If the dataset holds fewer rows than we need (service_requests.csv has 300), the
     * keys are cycled and each cycle offset by 1,000,000 so they stay distinct. State this
     * in the report: the shape of the data is real, the volume is padded.
     */
    private static List<Integer> keysOfSize(List<Integer> source, int size, boolean sorted) {
        List<Integer> keys = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int cycle = i / source.size();
            keys.add(source.get(i % source.size()) + cycle * 1_000_000);
        }
        if (sorted) {
            Collections.sort(keys);
        } else {
            Collections.shuffle(keys, new Random(SEED + size));
        }
        return keys;
    }

    /** Half hits, half misses - a search benchmark that always hits is not realistic. */
    private static List<Integer> lookupsFor(List<Integer> keys) {
        List<Integer> lookups = new ArrayList<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            lookups.add(i % 2 == 0 ? keys.get(i) : keys.get(i) + MISS_OFFSET);
        }
        Collections.shuffle(lookups, new Random(SEED + 1));
        return lookups;
    }

    // ------------------------------------------------------------------ CSV output

    private static void writeSummary(Path file, List<String> tested,
                                     Map<String, Map<Integer, double[]>> averages)
            throws IOException {
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
        try {
            StringBuilder header = new StringBuilder("Size");
            for (int i = 0; i < tested.size(); i++) {
                header.append(',').append(tested.get(i)).append("_Insert_ms");
            }
            for (int i = 0; i < tested.size(); i++) {
                header.append(',').append(tested.get(i)).append("_Search_ms");
            }
            out.println(header);

            for (int s = 0; s < SIZES.length; s++) {
                int size = SIZES[s];
                StringBuilder row = new StringBuilder(String.valueOf(size));
                for (int i = 0; i < tested.size(); i++) {
                    row.append(',').append(format(averages.get(tested.get(i)).get(size)[0]));
                }
                for (int i = 0; i < tested.size(); i++) {
                    row.append(',').append(format(averages.get(tested.get(i)).get(size)[1]));
                }
                out.println(row);
            }
        } finally {
            out.close();
        }
    }

    private static void writeDetail(Path file, List<String[]> rows) throws IOException {
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
        try {
            out.println("Order,Structure,Size,Run,Insert_ms,Search_ms,Height,MemoryKb,Note");
            for (int i = 0; i < rows.size(); i++) {
                out.println(String.join(",", rows.get(i)));
            }
        } finally {
            out.close();
        }
    }

    /**
     * Height comparison, one column per structure. This is the "height and search time
     * comparison" graph section 9 asks for; chart it next to the search-time graph.
     */
    private static void writeHeights(Path file, List<String> tested,
                                     Map<String, Map<Integer, long[]>> profiles)
            throws IOException {
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
        try {
            StringBuilder header = new StringBuilder("Order,Size");
            for (int i = 0; i < tested.size(); i++) {
                header.append(',').append(tested.get(i)).append("_Height");
            }
            for (int i = 0; i < tested.size(); i++) {
                header.append(',').append(tested.get(i)).append("_MemoryKb");
            }
            out.println(header);

            String[] orders = {"random", "sorted"};
            for (int o = 0; o < orders.length; o++) {
                for (int s = 0; s < SIZES.length; s++) {
                    int size = SIZES[s];
                    StringBuilder row = new StringBuilder(orders[o] + "," + size);
                    for (int part = 0; part < 2; part++) {
                        for (int i = 0; i < tested.size(); i++) {
                            Map<Integer, long[]> bySize = profiles.get(orders[o] + "/" + tested.get(i));
                            long[] profile = bySize == null ? null : bySize.get(size);
                            long value = profile == null ? -1L : profile[part];
                            if (part == 0) {
                                row.append(',').append(value < 0 ? "" : String.valueOf(value));
                            } else {
                                row.append(',').append(toKb(value));
                            }
                        }
                    }
                    out.println(row);
                }
            }
        } finally {
            out.close();
        }
    }

    /** Exactly the columns of the algorithm_runs table, ready to import. */
    private static void writeAlgorithmRuns(Path file, List<String[]> rows) throws IOException {
        PrintWriter out = new PrintWriter(Files.newBufferedWriter(file, StandardCharsets.UTF_8));
        try {
            out.println("runId,algorithmName,inputSize,timeNs,memoryKb,dateRun");
            for (int i = 0; i < rows.size(); i++) {
                out.println(String.join(",", rows.get(i)));
            }
        } finally {
            out.close();
        }
    }

    /** A failed measurement becomes an empty cell, which spreadsheets draw as a gap. */
    private static String format(double millis) {
        return Double.isNaN(millis) ? "" : String.format("%.4f", millis);
    }

    // ------------------------------------------------------------------ the adapter

    /** Wraps any tree class that has an insert-like and a search-like method. */
    private static final class TreeUnderTest {
        private final Constructor<?> constructor;
        private final Object[] constructorArgs;
        private final Method insertMethod;
        private final Method searchMethod;
        private final Method heightMethod;

        private TreeUnderTest(Constructor<?> constructor, Object[] constructorArgs,
                              Method insertMethod, Method searchMethod, Method heightMethod) {
            this.constructor = constructor;
            this.constructorArgs = constructorArgs;
            this.insertMethod = insertMethod;
            this.searchMethod = searchMethod;
            this.heightMethod = heightMethod;
        }

        static TreeUnderTest forClass(String className) throws ReflectiveOperationException {
            Class<?> type;
            try {
                type = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("class not found - has it been merged yet?");
            }

            Constructor<?> constructor = null;
            Object[] constructorArgs = new Object[0];
            try {
                constructor = type.getDeclaredConstructor();
            } catch (NoSuchMethodException ignored) {
                Constructor<?>[] candidates = type.getDeclaredConstructors();
                for (int i = 0; i < candidates.length; i++) {
                    Class<?>[] params = candidates[i].getParameterTypes();
                    if (params.length == 1 && (params[0] == int.class || params[0] == Integer.class)) {
                        constructor = candidates[i];
                        constructorArgs = new Object[] {Integer.valueOf(
                                BTree.minDegreeFromIndexNumbers(INDEX_NUMBERS))};
                        break;
                    }
                }
            }
            if (constructor == null) {
                throw new IllegalStateException("no no-arg or single-int constructor found");
            }
            constructor.setAccessible(true);

            Method insert = pick(type, INSERT_NAMES, 2);
            if (insert == null) {
                insert = pick(type, INSERT_NAMES, 1);
            }
            if (insert == null) {
                throw new IllegalStateException("no insert method found (looked for "
                        + String.join("/", INSERT_NAMES) + ")");
            }
            Method search = pick(type, SEARCH_NAMES, 1);
            if (search == null) {
                throw new IllegalStateException("no search method found (looked for "
                        + String.join("/", SEARCH_NAMES) + ")");
            }
            insert.setAccessible(true);
            search.setAccessible(true);
            // Height is optional: if the partner's class does not expose it, the height
            // column is simply left blank rather than failing the whole experiment.
            Method height = pick(type, HEIGHT_NAMES, 0);
            if (height != null) {
                height.setAccessible(true);
            }
            return new TreeUnderTest(constructor, constructorArgs, insert, search, height);
        }

        private static Method pick(Class<?> type, String[] names, int parameterCount) {
            for (int n = 0; n < names.length; n++) {
                Method[] methods = type.getMethods();
                for (int m = 0; m < methods.length; m++) {
                    if (methods[m].getName().equals(names[n])
                            && methods[m].getParameterCount() == parameterCount) {
                        return methods[m];
                    }
                }
            }
            return null;
        }

        Object newInstance() throws ReflectiveOperationException {
            return constructor.newInstance(constructorArgs);
        }

        void insert(Object instance, Integer key) throws ReflectiveOperationException {
            if (insertMethod.getParameterCount() == 2) {
                insertMethod.invoke(instance, key, key);
            } else {
                insertMethod.invoke(instance, key);
            }
        }

        void search(Object instance, Integer key) throws ReflectiveOperationException {
            searchMethod.invoke(instance, key);
        }

        /** Height of the tree, or -1 if this class does not report one. */
        int height(Object instance) {
            if (heightMethod == null) {
                return -1;
            }
            try {
                Object result = heightMethod.invoke(instance);
                return result instanceof Number ? ((Number) result).intValue() : -1;
            } catch (Throwable t) {
                return -1;
            }
        }

        boolean reportsHeight() {
            return heightMethod != null;
        }

        String insertName() {
            return insertMethod.getName() + "/" + insertMethod.getParameterCount() + " arg(s)";
        }

        String searchName() {
            return searchMethod.getName() + "/1 arg";
        }
    }

    private TreePerformanceTest() {
    }
}