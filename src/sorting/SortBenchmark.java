package sorting;

import java.util.Arrays;

/**
 * Benchmark driver for <b>Question 2 Part C</b> — Empirical Sorting Algorithm Analysis.
 *
 * <p>Tests Insertion Sort, Merge Sort, Quick Sort, and Heap Sort across:
 * <ul>
 *   <li>Four input sizes: 100, 1 000, 10 000, and 100 000 elements.</li>
 *   <li>Five distribution types: random, sorted, reverse-sorted, nearly-sorted,
 *       and many-duplicates.</li>
 * </ul>
 *
 * <p>For each (algorithm, size, distribution) triple, the benchmark:
 * <ol>
 *   <li>Runs the algorithm {@value #MEASURED_RUNS} times on identical input.</li>
 *   <li>Records execution time in nanoseconds using {@link System#nanoTime()}.</li>
 *   <li>Counts comparisons and swaps via {@link SortStats}.</li>
 *   <li>Computes mean and standard deviation of the runtimes.</li>
 * </ol>
 *
 * <h2>How to run</h2>
 * <pre>
 *   javac -d out src/main/java/com/csc483/assignment/sorting/*.java
 *   java  -cp out com.csc483.assignment.sorting.SortBenchmark
 * </pre>
 *
 * @author  Bethel Messiah
 * @version 1.0
 */
public class SortBenchmark {

    // -------------------------------------------------------------------------
    // Configuration constants
    // -------------------------------------------------------------------------

    /** Input sizes tested for the full comparison table. */
    private static final int[]     SIZES         = { 100, 1_000, 10_000, 100_000 };

    /** Number of JVM warm-up iterations (results discarded). */
    private static final int       WARMUP_RUNS   = 5;

    /** Number of measured iterations whose times are averaged and std-deviated. */
    private static final int       MEASURED_RUNS = 5;

    /** Distribution types passed to {@link DataGenerator}. */
    private static final String[]  DATA_TYPES    =
            { "random", "sorted", "reverse", "nearly", "duplicates" };

    /** Algorithm names used for printing and dispatch. */
    private static final String[]  ALGORITHMS    =
            { "Insertion", "Merge", "Quick", "Heap" };

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Application entry point. Coordinates all benchmark phases and prints
     * results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // Warm up the JVM before taking any measurements
        warmUp();

        // ── Section 1: Random data across all sizes ───────────────────────────
        printHeader("SORTING ALGORITHMS COMPARISON — RANDOM DATA");
        System.out.printf("%-10s %-12s %12s %16s %14s%n",
                "Size", "Algorithm", "Time (ms)", "Comparisons", "Swaps");
        printDivider();

        for (int size : SIZES) {
            if (size == 100_000) {
                // Insertion Sort at n=100,000 is prohibitively slow on random data;
                // skip it to keep the benchmark from stalling
                for (String algo : new String[]{ "Merge", "Quick", "Heap" }) {
                    printBenchmarkRow(algo, size, "random");
                }
            } else {
                // Run all four algorithms for smaller sizes
                for (String algo : ALGORITHMS) {
                    printBenchmarkRow(algo, size, "random");
                }
            }
            System.out.println(); // Blank line between size groups
        }

        // ── Section 2: All data types at n = 10,000 ───────────────────────────
        printHeader("ALL DATA TYPES COMPARISON (n = 10,000)");

        for (String dataType : DATA_TYPES) {
            System.out.printf("%nData Type: %s%n", DataGenerator.label(dataType));
            System.out.printf("%-12s %12s %16s %14s%n",
                    "Algorithm", "Time (ms)", "Comparisons", "Swaps");
            printDivider();

            for (String algo : ALGORITHMS) {
                printBenchmarkRow(algo, 10_000, dataType);
            }
        }

        // ── Section 3: Statistical analysis (n = 10,000, random) ─────────────
        printHeader("STATISTICAL ANALYSIS (n = 10,000, Random Data, 5 runs)");
        printStatisticalAnalysis(10_000, "random");

        // ── Section 4: Conclusions ────────────────────────────────────────────
        printConclusions();
    }

    // -------------------------------------------------------------------------
    // Benchmark core
    // -------------------------------------------------------------------------

    /**
     * Runs one (algorithm, size, dataType) benchmark and prints a single result row.
     *
     * <p>The algorithm is run {@value #MEASURED_RUNS} times on identical input.
     * The mean time and the comparison / swap counts from the final run are reported.
     *
     * @param algo     one of "Insertion", "Merge", "Quick", "Heap"
     * @param size     number of elements in the test array
     * @param dataType one of "random", "sorted", "reverse", "nearly", "duplicates"
     */
    private static void printBenchmarkRow(String algo, int size, String dataType) {

        long[]    nanoTimes = new long[MEASURED_RUNS];
        SortStats stats     = new SortStats();

        for (int run = 0; run < MEASURED_RUNS; run++) {
            // Generate a fresh copy of the input for each run so results are independent
            int[] data = getDataset(dataType, size);
            stats.reset();

            long t0 = System.nanoTime();
            runAlgorithm(algo, data, stats);
            nanoTimes[run] = System.nanoTime() - t0;
        }

        // Report the mean time and the counts from the last run
        double meanMs  = meanNanos(nanoTimes) / 1_000_000.0;
        String swapStr = algo.equals("Merge") ? "N/A"
                : String.format("%,d", stats.getSwaps());

        System.out.printf("%-10s %-12s %12.4f %16s %14s%n",
                size > 999 ? String.format("%,d", size) : String.valueOf(size),
                algo,
                meanMs,
                String.format("%,d", stats.getComparisons()),
                swapStr);
    }

    /**
     * Runs a per-run statistical analysis for the given (size, dataType) combination.
     *
     * <p>Prints individual run times, mean, and standard deviation for each algorithm.
     * Uses the same {@value #MEASURED_RUNS} runs to keep the table consistent with
     * the main benchmark.
     *
     * @param size     number of elements
     * @param dataType distribution type key
     */
    private static void printStatisticalAnalysis(int size, String dataType) {

        System.out.printf("%-12s %10s %10s %10s %10s %10s %12s %10s%n",
                "Algorithm",
                "Run1(ms)", "Run2(ms)", "Run3(ms)", "Run4(ms)", "Run5(ms)",
                "Mean(ms)", "StdDev");
        printDivider();

        for (String algo : ALGORITHMS) {
            double[] runMs = new double[MEASURED_RUNS];
            SortStats stats = new SortStats();

            for (int r = 0; r < MEASURED_RUNS; r++) {
                int[] data = getDataset(dataType, size);
                stats.reset();

                long t0 = System.nanoTime();
                runAlgorithm(algo, data, stats);
                runMs[r] = (System.nanoTime() - t0) / 1_000_000.0;
            }

            double mean = meanDouble(runMs);
            double sd   = stdDev(runMs, mean);

            System.out.printf("%-12s %10.4f %10.4f %10.4f %10.4f %10.4f %12.4f %10.4f%n",
                    algo,
                    runMs[0], runMs[1], runMs[2], runMs[3], runMs[4],
                    mean, sd);
        }
    }

    // -------------------------------------------------------------------------
    // Algorithm dispatch
    // -------------------------------------------------------------------------

    /**
     * Dispatches execution to the appropriate sorting algorithm.
     *
     * @param algo  algorithm name; must be one of "Insertion", "Merge", "Quick", "Heap"
     * @param data  array to sort (modified in place)
     * @param stats comparison and swap counter
     * @throws IllegalArgumentException if {@code algo} is not recognised
     */
    private static void runAlgorithm(String algo, int[] data, SortStats stats) {
        switch (algo) {
            case "Insertion" -> SortingAlgorithms.insertionSort(data, stats);
            case "Merge"     -> SortingAlgorithms.mergeSort(data, stats);
            case "Quick"     -> SortingAlgorithms.quickSort(data, stats);
            case "Heap"      -> SortingAlgorithms.heapSort(data, stats);
            default          -> throw new IllegalArgumentException(
                    "Unknown algorithm: " + algo);
        }
    }

    // -------------------------------------------------------------------------
    // Dataset dispatch
    // -------------------------------------------------------------------------

    /**
     * Returns a freshly generated dataset of the requested type and size.
     *
     * @param type one of "random", "sorted", "reverse", "nearly", "duplicates"
     * @param size number of elements
     * @return new integer array; falls back to random if {@code type} is unknown
     */
    private static int[] getDataset(String type, int size) {
        return switch (type) {
            case "random"     -> DataGenerator.randomArray(size);
            case "sorted"     -> DataGenerator.sortedArray(size);
            case "reverse"    -> DataGenerator.reverseSortedArray(size);
            case "nearly"     -> DataGenerator.nearlySortedArray(size);
            case "duplicates" -> DataGenerator.manyDuplicatesArray(size);
            default           -> DataGenerator.randomArray(size);
        };
    }

    // -------------------------------------------------------------------------
    // JVM warm-up
    // -------------------------------------------------------------------------

    /**
     * Runs every algorithm several times on a moderately sized dataset and
     * discards the results.
     *
     * <p>This allows the JVM's Just-In-Time compiler to compile the hot paths
     * before measurements begin, reducing noise in the first few measured runs.
     */
    private static void warmUp() {
        SortStats s = new SortStats();

        for (String algo : ALGORITHMS) {
            for (int i = 0; i < WARMUP_RUNS; i++) {
                // Use n=1,000 random data — large enough to trigger JIT compilation
                runAlgorithm(algo, DataGenerator.randomArray(1_000), s);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Statistical helpers
    // -------------------------------------------------------------------------

    /**
     * Computes the arithmetic mean of an array of {@code long} nanosecond times.
     *
     * @param nanos array of nanosecond measurements
     * @return mean value in nanoseconds (as a {@code double})
     */
    private static double meanNanos(long[] nanos) {
        long sum = 0;
        for (long v : nanos) sum += v;
        return (double) sum / nanos.length;
    }

    /**
     * Computes the arithmetic mean of an array of {@code double} millisecond times.
     *
     * @param values array of millisecond measurements
     * @return mean value in milliseconds
     */
    private static double meanDouble(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    /**
     * Computes the population standard deviation of an array of {@code double} values.
     *
     * @param values array of measurements
     * @param mean   pre-computed arithmetic mean of {@code values}
     * @return standard deviation (same unit as {@code values})
     */
    private static double stdDev(double[] values, double mean) {
        double variance = 0;

        for (double v : values) {
            double diff = v - mean;
            variance += diff * diff; // Sum of squared deviations
        }

        return Math.sqrt(variance / values.length); // Population std dev
    }

    // -------------------------------------------------------------------------
    // Output helpers
    // -------------------------------------------------------------------------

    /**
     * Prints a bold section header surrounded by separator lines.
     *
     * @param title header text
     */
    private static void printHeader(String title) {
        System.out.println("\n================================================================");
        System.out.println("  " + title);
        System.out.println("================================================================");
    }

    /** Prints a horizontal rule to visually separate table rows from the header. */
    private static void printDivider() {
        System.out.println("────────────────────────────────────────────────────────────────");
    }

    /** Prints the empirical conclusions derived from the benchmark results. */
    private static void printConclusions() {
        printHeader("CONCLUSIONS");
        System.out.println("  - Quick Sort is fastest on average for random data (best cache locality).");
        System.out.println("  - Insertion Sort is competitive only for n < 1,000 or nearly-sorted data.");
        System.out.println("  - Merge Sort delivers the most consistent performance across all data types.");
        System.out.println("  - Heap Sort uses O(1) auxiliary space but has higher constant factors than Quick Sort.");
        System.out.println("  - Quick Sort with median-of-three pivot avoids the O(n²) worst case on sorted inputs.");
        System.out.println("================================================================");
    }
}
