package com.campushub.performance;

import com.campushub.algorithms.graph.BFS;
import com.campushub.algorithms.graph.DFS;
import com.campushub.algorithms.graph.Dijkstra;
import com.campushub.algorithms.graph.Kruskal;
import com.campushub.algorithms.graph.Prim;
import com.campushub.model.Road;
import com.campushub.structures.graph.Graph;
import com.campushub.structures.graph.Graph.WeightMode;
import com.campushub.structures.linear.DynamicArray;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

// Owner: Graphs and Optimization

/**
 * Times the graph algorithms over growing slices of the campus network and writes the
 * results to {@code performance/}.
 *
 * <p>Run with {@code mvn compile exec:java -Dexec.mainClass=com.campushub.performance.GraphBenchmark}.
 * Produces a CSV of raw timings, a line chart, and a short notes file for the report.
 *
 * <p>Each measurement is the median of several runs after a warm-up, because the JIT
 * makes a single cold timing meaningless. Fifty-eight locations is small enough that
 * absolute numbers are microseconds; the shape of the curves is the interesting part.
 */
public final class GraphBenchmark {

    private static final Path SEED_DATA = Path.of("database", "seed-data");
    private static final Path RESULTS = Path.of("performance", "results");
    private static final Path GRAPHS = Path.of("performance", "graphs");
    private static final Path NOTES = Path.of("performance", "graph-efficiency-notes.md");

    private static final int WARMUP_RUNS = 3000;

    // A single run here takes well under a microsecond, which is the same order as the
    // clock's own noise, so timing one at a time produces garbage - curves that go down
    // as the input grows. Each sample therefore times a batch and divides.
    private static final int BATCH_SIZE = 500;
    private static final int TIMED_BATCHES = 21;

    private static final String[] ALGORITHMS = {"BFS", "DFS", "Dijkstra", "Prim", "Kruskal"};

    /** University Square, the highest-degree location, used as the growth seed. */
    private static final int HUB_LOCATION = 35;

    private GraphBenchmark() {
    }

    public static void main(String[] args) throws IOException {
        Graph full = Graph.fromSeedData(SEED_DATA, WeightMode.TIME_ADJUSTED);
        System.out.println("Benchmarking against " + full.summary());

        int[] sizes = sizesUpTo(full.order());
        long[][] medians = new long[ALGORITHMS.length][sizes.length];
        int[] roadCounts = new int[sizes.length];
        int[] componentCounts = new int[sizes.length];
        int[] reachedFromSource = new int[sizes.length];

        // Slices grow outward from the busiest location in BFS order, so every one is
        // connected. Slicing by lowest id instead leaves the small slices in several
        // pieces, and then the timings measure array allocation rather than traversal.
        int[] growthOrder = BFS.from(full, HUB_LOCATION).visitOrder();

        for (int column = 0; column < sizes.length; column++) {
            Graph slice = inducedSubgraph(full, growthOrder, sizes[column]);
            roadCounts[column] = slice.size();
            componentCounts[column] = BFS.components(slice).length;
            int source = slice.locationIds()[0];
            // How much of the slice the source can actually see. Below 40 locations the
            // slice is disconnected, so BFS, Dijkstra and Prim all touch only part of it
            // while Kruskal still sorts every road - which is what the curves show.
            reachedFromSource[column] = BFS.from(slice, source).reachedCount();

            medians[0][column] = time(slice, source, 0);
            medians[1][column] = time(slice, source, 1);
            medians[2][column] = time(slice, source, 2);
            medians[3][column] = time(slice, source, 3);
            medians[4][column] = time(slice, source, 4);

            System.out.printf("  %2d locations, %3d roads, %d component(s), %2d reachable "
                            + "-> BFS %5d ns, DFS %5d ns, Dijkstra %5d ns, Prim %5d ns, "
                            + "Kruskal %5d ns%n",
                    sizes[column], roadCounts[column], componentCounts[column],
                    reachedFromSource[column], medians[0][column], medians[1][column],
                    medians[2][column], medians[3][column], medians[4][column]);
        }

        writeCsv(sizes, roadCounts, medians);
        writeChart(sizes, medians);
        writeNotes(full, sizes, roadCounts, componentCounts, reachedFromSource, medians);
        System.out.println("Wrote " + RESULTS.resolve("graph_algorithm_runs.csv") + ", "
                + GRAPHS.resolve("graph-algorithms.png") + " and " + NOTES);
    }

    /** Median nanoseconds per run for one algorithm, chosen by index to keep the loop flat. */
    private static long time(Graph graph, int source, int algorithm) {
        for (int run = 0; run < WARMUP_RUNS; run++) {
            runOnce(graph, source, algorithm);
        }
        long[] samples = new long[TIMED_BATCHES];
        for (int batch = 0; batch < TIMED_BATCHES; batch++) {
            long startedAt = System.nanoTime();
            for (int run = 0; run < BATCH_SIZE; run++) {
                runOnce(graph, source, algorithm);
            }
            samples[batch] = (System.nanoTime() - startedAt) / BATCH_SIZE;
        }
        sort(samples);
        return samples[samples.length / 2];
    }

    private static void runOnce(Graph graph, int source, int algorithm) {
        switch (algorithm) {
            case 0:
                BFS.from(graph, source);
                break;
            case 1:
                DFS.from(graph, source);
                break;
            case 2:
                Dijkstra.from(graph, source);
                break;
            case 3:
                Prim.from(graph, source);
                break;
            default:
                Kruskal.of(graph);
        }
    }

    /**
     * The subgraph on the first {@code locationCount} entries of {@code order}, keeping
     * only roads with both endpoints inside it. Passing a BFS visit order keeps every
     * slice connected.
     */
    static Graph inducedSubgraph(Graph full, int[] order, int locationCount) {
        Graph slice = new Graph(full.weightMode());
        int limit = Math.min(locationCount, order.length);
        for (int index = 0; index < limit; index++) {
            slice.addLocation(full.location(order[index]));
        }
        DynamicArray<Road> roads = full.roads();
        for (int index = 0; index < roads.size(); index++) {
            Road road = roads.get(index);
            if (slice.hasLocation(road.getFromLocationId())
                    && slice.hasLocation(road.getToLocationId())) {
                slice.addRoad(road);
            }
        }
        return slice;
    }

    private static int[] sizesUpTo(int order) {
        int steps = order / 10;
        boolean includeFull = order % 10 != 0;
        int[] sizes = new int[steps + (includeFull ? 1 : 0)];
        for (int index = 0; index < steps; index++) {
            sizes[index] = (index + 1) * 10;
        }
        if (includeFull) {
            sizes[sizes.length - 1] = order;
        }
        return sizes;
    }

    private static void writeCsv(int[] sizes, int[] roadCounts, long[][] medians)
            throws IOException {
        StringBuilder out = new StringBuilder("algorithm,locations,roads,medianTimeNs\n");
        for (int row = 0; row < ALGORITHMS.length; row++) {
            for (int column = 0; column < sizes.length; column++) {
                out.append(ALGORITHMS[row]).append(',').append(sizes[column]).append(',')
                        .append(roadCounts[column]).append(',').append(medians[row][column])
                        .append('\n');
            }
        }
        Files.createDirectories(RESULTS);
        Files.write(RESULTS.resolve("graph_algorithm_runs.csv"),
                out.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static void writeChart(int[] sizes, long[][] medians) throws IOException {
        int width = 900;
        int height = 560;
        int left = 90;
        int right = width - 210;
        int top = 84;
        int bottom = height - 70;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D canvas = image.createGraphics();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        canvas.setColor(Color.WHITE);
        canvas.fillRect(0, 0, width, height);

        long peak = 1;
        for (int row = 0; row < medians.length; row++) {
            for (int column = 0; column < sizes.length; column++) {
                if (medians[row][column] > peak) {
                    peak = medians[row][column];
                }
            }
        }
        double ceiling = niceCeiling(peak);

        canvas.setColor(new Color(0x22, 0x22, 0x22));
        canvas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 17));
        canvas.drawString("Graph algorithms on the campus network", left, 32);
        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        canvas.setColor(new Color(0x66, 0x66, 0x66));
        canvas.drawString("ns per run, median of " + TIMED_BATCHES + " batches of " + BATCH_SIZE
                + ", connected slices grown from location " + HUB_LOCATION, left, 50);

        // Horizontal gridlines and y labels.
        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        for (int step = 0; step <= 5; step++) {
            int y = bottom - (bottom - top) * step / 5;
            canvas.setColor(new Color(0xE8, 0xE8, 0xE8));
            canvas.drawLine(left, y, right, y);
            canvas.setColor(new Color(0x88, 0x88, 0x88));
            String label = String.format("%.0f", ceiling * step / 5 / 1000.0);
            canvas.drawString(label, left - 12 - canvas.getFontMetrics().stringWidth(label),
                    y + 4);
        }

        canvas.setColor(new Color(0x44, 0x44, 0x44));
        canvas.drawLine(left, top, left, bottom);
        canvas.drawLine(left, bottom, right, bottom);
        canvas.drawString("microseconds", 14, top - 12);
        canvas.drawString("locations in the subgraph", (left + right) / 2 - 60, height - 24);

        for (int column = 0; column < sizes.length; column++) {
            int x = xFor(column, sizes.length, left, right);
            canvas.setColor(new Color(0x88, 0x88, 0x88));
            String label = String.valueOf(sizes[column]);
            canvas.drawString(label, x - canvas.getFontMetrics().stringWidth(label) / 2,
                    bottom + 18);
        }

        Color[] palette = {
            new Color(0x1F, 0x77, 0xB4),
            new Color(0xD6, 0x27, 0x28),
            new Color(0x2C, 0xA0, 0x2C),
            new Color(0xFF, 0x7F, 0x0E),
            new Color(0x94, 0x67, 0xBD),
        };

        canvas.setStroke(new BasicStroke(2.2f));
        for (int row = 0; row < medians.length; row++) {
            canvas.setColor(palette[row % palette.length]);
            for (int column = 0; column < sizes.length - 1; column++) {
                int x1 = xFor(column, sizes.length, left, right);
                int x2 = xFor(column + 1, sizes.length, left, right);
                int y1 = yFor(medians[row][column], ceiling, top, bottom);
                int y2 = yFor(medians[row][column + 1], ceiling, top, bottom);
                canvas.drawLine(x1, y1, x2, y2);
            }
            for (int column = 0; column < sizes.length; column++) {
                int x = xFor(column, sizes.length, left, right);
                int y = yFor(medians[row][column], ceiling, top, bottom);
                canvas.fillOval(x - 3, y - 3, 6, 6);
            }
        }

        canvas.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        for (int row = 0; row < ALGORITHMS.length; row++) {
            int y = top + 8 + row * 24;
            canvas.setColor(palette[row % palette.length]);
            canvas.fillRect(right + 26, y - 8, 18, 4);
            canvas.setColor(new Color(0x33, 0x33, 0x33));
            canvas.drawString(ALGORITHMS[row], right + 52, y);
        }

        canvas.dispose();
        Files.createDirectories(GRAPHS);
        ImageIO.write(image, "png", GRAPHS.resolve("graph-algorithms.png").toFile());
    }

    private static int xFor(int column, int columns, int left, int right) {
        if (columns <= 1) {
            return left;
        }
        return left + (right - left) * column / (columns - 1);
    }

    private static int yFor(long value, double ceiling, int top, int bottom) {
        return bottom - (int) Math.round((bottom - top) * (value / ceiling));
    }

    private static double niceCeiling(long peak) {
        double magnitude = Math.pow(10, Math.floor(Math.log10(peak)));
        double normalised = peak / magnitude;
        double rounded = normalised <= 1 ? 1 : normalised <= 2 ? 2 : normalised <= 5 ? 5 : 10;
        return rounded * magnitude;
    }

    private static void writeNotes(Graph graph, int[] sizes, int[] roadCounts,
                                   int[] componentCounts, int[] reachedFromSource,
                                   long[][] medians) throws IOException {
        int last = sizes.length - 1;
        StringBuilder out = new StringBuilder();
        out.append("<!-- Owner: Graphs and Optimization -->\n\n");
        out.append("# Graph algorithm efficiency\n\n");
        out.append("Generated by `GraphBenchmark`. Each figure is nanoseconds per run, taken as\n");
        out.append("the median of ").append(TIMED_BATCHES).append(" batches of ")
                .append(BATCH_SIZE).append(" runs, after ").append(WARMUP_RUNS)
                .append(" warm-up runs. Batching is\nnecessary because a single run costs less"
                        + " than a microsecond, which is the same\norder as the clock's own"
                        + " noise.\n\n");
        out.append("**Graph:** ").append(graph.summary()).append("\n\n");

        out.append("## Median time in nanoseconds\n\n| Algorithm ");
        for (int column = 0; column < sizes.length; column++) {
            out.append("| ").append(sizes[column]).append(" loc ");
        }
        out.append("|\n|---");
        for (int column = 0; column < sizes.length; column++) {
            out.append("|---");
        }
        out.append("|\n");
        for (int row = 0; row < ALGORITHMS.length; row++) {
            out.append("| ").append(ALGORITHMS[row]).append(" ");
            for (int column = 0; column < sizes.length; column++) {
                out.append("| ").append(medians[row][column]).append(" ");
            }
            out.append("|\n");
        }

        out.append("\n## Shape of each slice\n\n");
        out.append("Slices are induced subgraphs grown outward from location ").append(HUB_LOCATION)
                .append(", ").append(graph.nameOf(HUB_LOCATION)).append(",\nin BFS order. Growing"
                        + " from the lowest ids instead leaves the smaller slices in\nseveral"
                        + " disconnected pieces, and the timings then measure array allocation\n"
                        + "rather than traversal.\n\n");
        out.append("| Locations | Roads | Components | Reachable from source |\n|---|---|---|---|\n");
        for (int column = 0; column < sizes.length; column++) {
            out.append("| ").append(sizes[column]).append(" | ").append(roadCounts[column])
                    .append(" | ").append(componentCounts[column]).append(" | ")
                    .append(reachedFromSource[column]).append(" |\n");
        }
        out.append("\nEvery slice is a single component, so all five algorithms cover the whole of\n");
        out.append("each one and the comparison is like-for-like. Kruskal still tracks road count\n");
        out.append("rather than location count, because sorting the roads is its dominant cost.\n\n");

        out.append("## Complexity, and what was measured\n\n");
        out.append("| Algorithm | Complexity | Dominant cost | At ").append(graph.order())
                .append(" locations |\n|---|---|---|---|\n");
        String[] dominant = {
            "one queue pass, no cost comparisons",
            "one stack pass, no cost comparisons",
            "heap operations on tentative costs",
            "heap operations on cheapest incident road",
            "sorting all roads, then union-find",
        };
        String[] complexity = {
            "O(V + E)", "O(V + E)", "O((V + E) log V)", "O((V + E) log V)", "O(E log E)",
        };
        for (int row = 0; row < ALGORITHMS.length; row++) {
            out.append("| ").append(ALGORITHMS[row]).append(" | ").append(complexity[row])
                    .append(" | ").append(dominant[row]).append(" | ").append(medians[row][last])
                    .append(" ns |\n");
        }

        out.append("\nDijkstra costs ")
                .append(String.format("%.1f", (double) medians[2][last] / medians[0][last]))
                .append("x what BFS does at full size, which is the price of\n");
        out.append("answering the cheapest-route question rather than the fewest-roads one.\n\n");
        out.append("## Caveat on these numbers\n\n");
        out.append("Fifty-eight locations is far too small for asymptotic behaviour to dominate.\n");
        out.append("Constant factors, allocation, and cache behaviour matter more here than the\n");
        out.append("log factor, so the ordering between two algorithms of the same complexity\n");
        out.append("class should not be read as meaningful, and neither should a curve that dips\n");
        out.append("between two adjacent sizes. Combined with the uneven connectivity above, these\n");
        out.append("are not a clean function of V. They are included to give the efficiency lab\n");
        out.append("real figures and to show the expected ordering at full size, not as a claim\n");
        out.append("about operational performance.\n");

        Files.write(NOTES, out.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** Insertion sort over the timing samples, to pick a median without java.util. */
    private static void sort(long[] samples) {
        for (int index = 1; index < samples.length; index++) {
            long value = samples[index];
            int scan = index - 1;
            while (scan >= 0 && samples[scan] > value) {
                samples[scan + 1] = samples[scan];
                scan--;
            }
            samples[scan + 1] = value;
        }
    }
}
