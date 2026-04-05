package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sorting.DataGenerator;
import sorting.SortStats;
import sorting.SortingAlgorithms;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for {@link SortingAlgorithms}, {@link SortStats},
 * and {@link DataGenerator}.
 *
 * <p>Each sorting algorithm is verified against eight representative input types:
 * <ul>
 *   <li>Empty array</li>
 *   <li>Single-element array</li>
 *   <li>Already-sorted array</li>
 *   <li>Reverse-sorted array</li>
 *   <li>Random array with duplicates</li>
 *   <li>Nearly-sorted array</li>
 *   <li>All-same-value array</li>
 *   <li>Boundary-value array ({@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE})</li>
 * </ul>
 *
 * <p>Additional tests cover algorithm-specific properties (stable comparison counts,
 * handling of large arrays, no degradation on sorted input for Quick Sort), and
 * the correctness of {@link SortStats} and {@link DataGenerator}.
 *
 * @author  Bethel Messiah
 * @version 1.0
 */
@DisplayName("Sorting Algorithms — Full Test Suite")
class SortingAlgorithmsTest {

    // -------------------------------------------------------------------------
    // Shared test infrastructure
    // -------------------------------------------------------------------------

    /**
     * Provides a stream of representative input arrays used in parameterised tests.
     * Each array exercises a different structural property.
     *
     * @return stream of {@code int[]} test cases
     */
    static Stream<int[]> testArrays() {
        return Stream.of(
                new int[]{},                                        // empty
                new int[]{42},                                      // single element
                new int[]{1, 2, 3, 4, 5},                          // already sorted
                new int[]{5, 4, 3, 2, 1},                          // reverse sorted
                new int[]{3, 1, 4, 1, 5, 9, 2, 6, 5, 3, 5},       // random with duplicates
                new int[]{1, 2, 3, 5, 4, 6, 7, 8, 9, 10},         // nearly sorted
                new int[]{7, 7, 7, 7, 7},                          // all identical values
                new int[]{Integer.MIN_VALUE, 0, Integer.MAX_VALUE}  // boundary values
        );
    }

    /**
     * Returns a reference-sorted copy of {@code arr} using Java's built-in sort
     * (Tim Sort), which is known correct and serves as the oracle for all assertions.
     *
     * @param arr input array (not modified)
     * @return new array containing the same elements in ascending order
     */
    private static int[] expectedSorted(int[] arr) {
        int[] copy = Arrays.copyOf(arr, arr.length);
        Arrays.sort(copy);
        return copy;
    }

    /** Fresh stats object initialised before each test. */
    private SortStats stats;

    /** Reinitialise stats so counter values do not carry over between tests. */
    @BeforeEach
    void setUp() {
        stats = new SortStats();
    }

    // =========================================================================
    // 1. Insertion Sort
    // =========================================================================

    /**
     * Parameterised correctness test: Insertion Sort must produce the same
     * result as {@link Arrays#sort} for every input in {@link #testArrays()}.
     *
     * @param input a test array (a defensive copy is sorted so the original is not modified)
     */
    @ParameterizedTest(name = "InsertionSort correctness: {0}")
    @MethodSource("testArrays")
    @DisplayName("Insertion Sort: produces correctly sorted output for all input types")
    void insertionSort_correctness(int[] input) {
        int[] expected = expectedSorted(input);
        int[] actual   = Arrays.copyOf(input, input.length); // sort the copy, not the fixture

        SortingAlgorithms.insertionSort(actual, stats);

        assertArrayEquals(expected, actual,
                "Insertion Sort output does not match expected sorted order");
    }

    /**
     * Verifies that Insertion Sort records at least one comparison for a
     * non-trivial (n &gt; 1, unsorted) input.
     */
    @Test
    @DisplayName("Insertion Sort: records comparisons for non-trivial input")
    void insertionSort_recordsComparisons() {
        int[] arr = { 5, 3, 1, 4, 2 };
        SortingAlgorithms.insertionSort(arr, stats);
        assertTrue(stats.getComparisons() > 0,
                "Comparison counter should be > 0 after sorting an unsorted array");
    }

    /**
     * Verifies that a sorted array triggers fewer comparisons than a reverse-sorted
     * array of the same size (best case vs worst case).
     */
    @Test
    @DisplayName("Insertion Sort: sorted input uses fewer comparisons than reverse-sorted")
    void insertionSort_sortedInputFewerComparisons() {
        int[] sorted   = { 1, 2, 3, 4, 5 };
        int[] reversed = { 5, 4, 3, 2, 1 };

        SortStats s1 = new SortStats();
        SortStats s2 = new SortStats();

        SortingAlgorithms.insertionSort(Arrays.copyOf(sorted,   5), s1);
        SortingAlgorithms.insertionSort(Arrays.copyOf(reversed, 5), s2);

        assertTrue(s1.getComparisons() <= s2.getComparisons(),
                "Sorted input should require no more comparisons than reverse-sorted input");
    }

    // =========================================================================
    // 2. Merge Sort
    // =========================================================================

    /**
     * Parameterised correctness test for Merge Sort.
     *
     * @param input a test array
     */
    @ParameterizedTest(name = "MergeSort correctness: {0}")
    @MethodSource("testArrays")
    @DisplayName("Merge Sort: produces correctly sorted output for all input types")
    void mergeSort_correctness(int[] input) {
        int[] expected = expectedSorted(input);
        int[] actual   = Arrays.copyOf(input, input.length);

        SortingAlgorithms.mergeSort(actual, stats);

        assertArrayEquals(expected, actual);
    }

    /**
     * Verifies that the comparison count grows as input size increases,
     * consistent with O(n log n) complexity.
     */
    @Test
    @DisplayName("Merge Sort: comparison count scales with input size")
    void mergeSort_comparisonCountScalesWithSize() {
        SortStats s1 = new SortStats();
        SortStats s2 = new SortStats();

        SortingAlgorithms.mergeSort(DataGenerator.randomArray(100),   s1);
        SortingAlgorithms.mergeSort(DataGenerator.randomArray(1_000), s2);

        assertTrue(s2.getComparisons() > s1.getComparisons(),
                "n=1,000 should require more comparisons than n=100");
    }

    /**
     * Stress test: Merge Sort must correctly sort a large array (n = 50,000).
     */
    @Test
    @DisplayName("Merge Sort: handles large array (n = 50,000) correctly")
    void mergeSort_largeArray() {
        int[] arr      = DataGenerator.randomArray(50_000);
        int[] expected = expectedSorted(arr);

        SortingAlgorithms.mergeSort(arr, stats);

        assertArrayEquals(expected, arr,
                "Merge Sort failed on a large random array of 50,000 elements");
    }

    // =========================================================================
    // 3. Quick Sort
    // =========================================================================

    /**
     * Parameterised correctness test for Quick Sort.
     *
     * @param input a test array
     */
    @ParameterizedTest(name = "QuickSort correctness: {0}")
    @MethodSource("testArrays")
    @DisplayName("Quick Sort: produces correctly sorted output for all input types")
    void quickSort_correctness(int[] input) {
        int[] expected = expectedSorted(input);
        int[] actual   = Arrays.copyOf(input, input.length);

        SortingAlgorithms.quickSort(actual, stats);

        assertArrayEquals(expected, actual);
    }

    /**
     * Verifies that median-of-three pivot selection prevents Quick Sort from
     * degrading to O(n²) on already-sorted input.
     * The sort must complete in under 2 seconds for n = 10,000.
     */
    @Test
    @DisplayName("Quick Sort: no O(n²) degradation on sorted input (median-of-three pivot)")
    void quickSort_sortedInputNoDegradation() {
        int[] arr = DataGenerator.sortedArray(10_000);
        long  t0  = System.currentTimeMillis();

        SortingAlgorithms.quickSort(arr, stats);

        long elapsed = System.currentTimeMillis() - t0;
        assertTrue(elapsed < 2_000,
                "Quick Sort on sorted input took " + elapsed + " ms — expected < 2,000 ms");
    }

    /**
     * Verifies that Quick Sort handles an all-duplicate array without producing
     * incorrect output or infinite recursion.
     */
    @Test
    @DisplayName("Quick Sort: handles all-duplicate array correctly")
    void quickSort_allDuplicates() {
        int[] arr      = new int[1_000];
        Arrays.fill(arr, 7);       // all elements are 7
        int[] expected = Arrays.copyOf(arr, arr.length);

        SortingAlgorithms.quickSort(arr, stats);

        assertArrayEquals(expected, arr,
                "All-duplicate array should remain unchanged after sorting");
    }

    /**
     * Stress test: Quick Sort must correctly sort a large array (n = 50,000).
     */
    @Test
    @DisplayName("Quick Sort: handles large array (n = 50,000) correctly")
    void quickSort_largeArray() {
        int[] arr      = DataGenerator.randomArray(50_000);
        int[] expected = expectedSorted(arr);

        SortingAlgorithms.quickSort(arr, stats);

        assertArrayEquals(expected, arr);
    }

    // =========================================================================
    // 4. Heap Sort
    // =========================================================================

    /**
     * Parameterised correctness test for Heap Sort.
     *
     * @param input a test array
     */
    @ParameterizedTest(name = "HeapSort correctness: {0}")
    @MethodSource("testArrays")
    @DisplayName("Heap Sort: produces correctly sorted output for all input types")
    void heapSort_correctness(int[] input) {
        int[] expected = expectedSorted(input);
        int[] actual   = Arrays.copyOf(input, input.length);

        SortingAlgorithms.heapSort(actual, stats);

        assertArrayEquals(expected, actual);
    }

    /**
     * Stress test: Heap Sort must correctly sort a large array (n = 50,000).
     */
    @Test
    @DisplayName("Heap Sort: handles large array (n = 50,000) correctly")
    void heapSort_largeArray() {
        int[] arr      = DataGenerator.randomArray(50_000);
        int[] expected = expectedSorted(arr);

        SortingAlgorithms.heapSort(arr, stats);

        assertArrayEquals(expected, arr);
    }

    /**
     * Verifies that Heap Sort records a positive swap count for unsorted input.
     */
    @Test
    @DisplayName("Heap Sort: records positive swap count for unsorted input")
    void heapSort_recordsSwaps() {
        int[] arr = { 5, 3, 8, 1, 9, 2 };
        SortingAlgorithms.heapSort(arr, stats);
        assertTrue(stats.getSwaps() > 0,
                "Swap counter should be > 0 after sorting an unsorted array");
    }

    // =========================================================================
    // SortStats
    // =========================================================================

    /**
     * Verifies that {@link SortStats#reset()} sets both counters to zero.
     */
    @Test
    @DisplayName("SortStats: reset() clears comparisons and swaps to zero")
    void sortStats_resetClearsCounters() {
        stats.incrementComparisons();
        stats.incrementComparisons();
        stats.incrementSwaps();

        stats.reset();

        assertEquals(0, stats.getComparisons(), "comparisons should be 0 after reset");
        assertEquals(0, stats.getSwaps(),       "swaps should be 0 after reset");
    }

    /**
     * Verifies that each increment call increases the respective counter by exactly 1.
     */
    @Test
    @DisplayName("SortStats: increment methods each increase their counter by 1")
    void sortStats_incrementsByOne() {
        stats.incrementComparisons();
        stats.incrementComparisons();
        stats.incrementSwaps();

        assertEquals(2, stats.getComparisons());
        assertEquals(1, stats.getSwaps());
    }

    // =========================================================================
    // DataGenerator
    // =========================================================================

    /**
     * Verifies that {@link DataGenerator#randomArray} returns an array of the
     * requested length.
     */
    @Test
    @DisplayName("DataGenerator: randomArray has correct length")
    void dataGenerator_randomArrayLength() {
        assertEquals(500, DataGenerator.randomArray(500).length);
    }

    /**
     * Verifies that {@link DataGenerator#sortedArray} is in strict ascending order.
     */
    @Test
    @DisplayName("DataGenerator: sortedArray is strictly ascending")
    void dataGenerator_sortedArrayIsAscending() {
        int[] arr = DataGenerator.sortedArray(100);

        for (int i = 0; i < arr.length - 1; i++) {
            assertTrue(arr[i] < arr[i + 1],
                    "sortedArray is not ascending at index " + i);
        }
    }

    /**
     * Verifies that {@link DataGenerator#reverseSortedArray} is in strict descending order.
     */
    @Test
    @DisplayName("DataGenerator: reverseSortedArray is strictly descending")
    void dataGenerator_reverseSortedArrayIsDescending() {
        int[] arr = DataGenerator.reverseSortedArray(100);

        for (int i = 0; i < arr.length - 1; i++) {
            assertTrue(arr[i] > arr[i + 1],
                    "reverseSortedArray is not descending at index " + i);
        }
    }

    /**
     * Verifies that {@link DataGenerator#manyDuplicatesArray} contains at most
     * 10 distinct values.
     */
    @Test
    @DisplayName("DataGenerator: manyDuplicatesArray has at most 10 distinct values")
    void dataGenerator_manyDuplicatesHasFewDistinctValues() {
        int[] arr    = DataGenerator.manyDuplicatesArray(10_000);
        long  distinct = Arrays.stream(arr).distinct().count();

        assertTrue(distinct <= 10,
                "Expected at most 10 distinct values but found " + distinct);
    }

    /**
     * Verifies that {@link DataGenerator#nearlySortedArray} has the correct length
     * and that at least 85 % of adjacent pairs remain in order (confirming it is
     * "nearly" sorted, not completely shuffled).
     */
    @Test
    @DisplayName("DataGenerator: nearlySortedArray has correct length and is mostly sorted")
    void dataGenerator_nearlySortedMostlyOrdered() {
        int size = 1_000;
        int[] arr = DataGenerator.nearlySortedArray(size);

        assertEquals(size, arr.length);

        // Count adjacent pairs that are in ascending order
        long orderedPairs = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] <= arr[i + 1]) orderedPairs++;
        }

        double orderedFraction = (double) orderedPairs / (arr.length - 1);
        assertTrue(orderedFraction >= 0.85,
                String.format("Expected ≥ 85%% ordered pairs but got %.1f%%",
                        orderedFraction * 100));
    }
}
