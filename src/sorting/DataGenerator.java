package sorting;

import java.util.Random;

/**
 * Factory class that generates integer test datasets with five distinct
 * distribution characteristics for the empirical sorting analysis in
 * Question 2 Part C.
 *
 * <h2>Distribution types</h2>
 * <ul>
 *   <li><b>Random</b>        – uniformly random integers in [0, size)</li>
 *   <li><b>Sorted</b>        – ascending sequence [0, 1, 2, &hellip;, size−1]</li>
 *   <li><b>Reverse Sorted</b>– descending sequence [size−1, &hellip;, 1, 0]</li>
 *   <li><b>Nearly Sorted</b> – sorted, then ~10 % of positions randomly displaced</li>
 *   <li><b>Many Duplicates</b>– random values from only 10 distinct integers</li>
 * </ul>
 *
 * <p>All generators use the same fixed {@link #SEED} so that experiments are
 * fully reproducible across machines and runs.
 *
 * @author  Bethel Messiah
 * @version 1.0
 * @see     SortBenchmark
 */
public class DataGenerator {

    /** Fixed random seed ensuring reproducible dataset generation. */
    private static final long SEED = 42L;

    // -------------------------------------------------------------------------
    // Dataset generators
    // -------------------------------------------------------------------------

    /**
     * Generates an array of {@code size} uniformly random integers in [0, size).
     *
     * <p>This is the "average case" distribution for comparison-based sorting:
     * no special structure exists that any algorithm can exploit.
     *
     * @param size number of elements; must be &gt; 0
     * @return newly allocated array with random contents
     */
    public static int[] randomArray(int size) {
        Random rng = new Random(SEED);
        int[]  arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = rng.nextInt(size); // value in [0, size)
        }

        return arr;
    }

    /**
     * Generates a sorted (ascending) array containing the integers
     * [0, 1, 2, &hellip;, size−1].
     *
     * <p>Used to evaluate best-case behaviour for Insertion Sort and
     * worst-case (naive pivot) behaviour for Quick Sort.
     *
     * @param size number of elements; must be &gt; 0
     * @return newly allocated ascending-sorted array
     */
    public static int[] sortedArray(int size) {
        int[] arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = i; // Simply assign index value — already in order
        }

        return arr;
    }

    /**
     * Generates a reverse-sorted (descending) array containing
     * [size−1, size−2, &hellip;, 1, 0].
     *
     * <p>This is the worst case for both Insertion Sort and naive Quick Sort
     * (with first-element pivot), and exercises the full O(n²) behaviour.
     *
     * @param size number of elements; must be &gt; 0
     * @return newly allocated descending-sorted array
     */
    public static int[] reverseSortedArray(int size) {
        int[] arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = size - 1 - i; // Descending: size-1, size-2, …, 0
        }

        return arr;
    }

    /**
     * Generates a nearly-sorted array: starts with the ascending sequence
     * [0, 1, &hellip;, size−1] and then randomly displaces approximately
     * 10 % of elements by swapping random pairs.
     *
     * <p>Models real-world data that is "almost" in order — a common scenario
     * in log processing, timestamped records, and incremental updates.
     * Insertion Sort is near O(n) on such data.
     *
     * @param size number of elements; must be &gt; 0
     * @return newly allocated nearly-sorted array
     */
    public static int[] nearlySortedArray(int size) {

        // Begin with a perfectly sorted array
        int[]  arr       = sortedArray(size);
        Random rng       = new Random(SEED);

        // Displace ~10 % of elements via random swaps
        int swapCount = Math.max(1, size / 10);

        for (int i = 0; i < swapCount; i++) {
            int a = rng.nextInt(size);
            int b = rng.nextInt(size);

            // Swap arr[a] and arr[b]
            int tmp = arr[a];
            arr[a]  = arr[b];
            arr[b]  = tmp;
        }

        return arr;
    }

    /**
     * Generates an array of {@code size} elements drawn from only 10 distinct
     * integer values [0, 9].
     *
     * <p>Tests algorithm behaviour in the presence of heavy duplicate density —
     * a case where stable sort algorithms are especially useful and where
     * Quick Sort's three-way partition variant would shine.
     *
     * @param size number of elements; must be &gt; 0
     * @return newly allocated array with high duplicate density
     */
    public static int[] manyDuplicatesArray(int size) {
        Random rng = new Random(SEED);
        int[]  arr = new int[size];

        for (int i = 0; i < size; i++) {
            arr[i] = rng.nextInt(10); // Only 10 distinct values: 0–9
        }

        return arr;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Returns a human-readable label for a given distribution type key.
     *
     * <p>Used by {@link SortBenchmark} to annotate printed output tables.
     *
     * @param type one of {@code "random"}, {@code "sorted"}, {@code "reverse"},
     *             {@code "nearly"}, or {@code "duplicates"} (case-insensitive)
     * @return descriptive label string; returns {@code type} unchanged if not recognised
     */
    public static String label(String type) {
        if (type == null) return "Unknown";

        return switch (type.toLowerCase()) {
            case "random"     -> "Random";
            case "sorted"     -> "Sorted (Ascending)";
            case "reverse"    -> "Reverse Sorted";
            case "nearly"     -> "Nearly Sorted";
            case "duplicates" -> "Many Duplicates";
            default           -> type; // Pass through unknown types unchanged
        };
    }
}
