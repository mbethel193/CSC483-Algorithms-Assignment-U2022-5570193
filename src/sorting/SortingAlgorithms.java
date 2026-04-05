package sorting;

/**
 * Implementations of four classic sorting algorithms studied in CSC 483.
 *
 * <p>All methods sort a primitive {@code int[]} in <b>ascending order in-place</b>
 * (Merge Sort requires O(n) auxiliary space, but the original array is modified).
 * Every method accepts a {@link SortStats} object into which it records the exact
 * number of element comparisons and swaps / assignments so that empirical results
 * can be compared against theoretical complexity curves.
 *
 * <h2>Algorithms</h2>
 * <table border="1">
 *   <tr><th>Algorithm</th><th>Best</th><th>Average</th><th>Worst</th><th>Space</th><th>Stable</th></tr>
 *   <tr><td>Insertion Sort</td><td>O(n)</td><td>O(n²)</td><td>O(n²)</td><td>O(1)</td><td>Yes</td></tr>
 *   <tr><td>Merge Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n)</td><td>Yes</td></tr>
 *   <tr><td>Quick Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n²)</td><td>O(log n)</td><td>No</td></tr>
 *   <tr><td>Heap Sort</td><td>O(n log n)</td><td>O(n log n)</td><td>O(n log n)</td><td>O(1)</td><td>No</td></tr>
 * </table>
 *
 * @author  Bethel Messiah
 * @version 1.0
 * @see     SortStats
 */
public class SortingAlgorithms {

    // -------------------------------------------------------------------------
    // 1. Insertion Sort
    // -------------------------------------------------------------------------

    /**
     * Sorts {@code arr} using the Insertion Sort algorithm.
     *
     * <p>Works by maintaining a sorted prefix {@code arr[0..i-1]}. On each
     * iteration, the element at position {@code i} (the "key") is shifted
     * leftward until it is in the correct position, growing the sorted prefix
     * to {@code arr[0..i]}.
     *
     * <p><b>When to use:</b> small arrays (n &lt; 50) or nearly-sorted data,
     * where it approaches O(n) performance. It is also a stable, in-place sort.
     *
     * @param arr   the array to sort; modified in place
     * @param stats counter for comparisons and swaps; must not be {@code null}
     */
    public static void insertionSort(int[] arr, SortStats stats) {

        if (arr == null || arr.length <= 1) {
            return; // Nothing to sort
        }

        int n = arr.length;

        // Grow the sorted prefix one element at a time
        for (int i = 1; i < n; i++) {

            int key = arr[i]; // The element being inserted into the sorted prefix
            int j   = i - 1;

            // Shift elements that are greater than 'key' one position to the right
            while (j >= 0 && arr[j] > key) {
                stats.incrementComparisons(); // We compared arr[j] with key
                stats.incrementSwaps();       // Shifting counts as a write/assignment
                arr[j + 1] = arr[j];
                j--;
            }

            // Count the comparison that broke out of the while loop (either arr[j] <= key
            // or j < 0, meaning we did one more comparison at the boundary)
            if (j >= 0) {
                stats.incrementComparisons();
            }

            // Insert the key into its correct sorted position
            arr[j + 1] = key;
        }
    }

    // -------------------------------------------------------------------------
    // 2. Merge Sort
    // -------------------------------------------------------------------------

    /**
     * Sorts {@code arr} using the top-down recursive Merge Sort algorithm.
     *
     * <p>Divides the array in half repeatedly until sub-arrays have size &le; 1,
     * then merges adjacent sorted sub-arrays back together. The merge step uses
     * temporary arrays, giving O(n) auxiliary space.
     *
     * <p><b>When to use:</b> large datasets where guaranteed O(n log n) performance
     * is required regardless of input order, or when a stable sort is needed
     * (e.g., sorting linked lists, external sorting).
     *
     * @param arr   the array to sort; modified in place
     * @param stats counter for comparisons; must not be {@code null}
     */
    public static void mergeSort(int[] arr, SortStats stats) {

        if (arr == null || arr.length <= 1) {
            return; // Base case — already sorted
        }

        mergeSortRecursive(arr, 0, arr.length - 1, stats);
    }

    /**
     * Recursive helper that divides {@code arr[left..right]} in half, recursively
     * sorts each half, then merges the two sorted halves.
     *
     * @param arr   the array being sorted
     * @param left  inclusive left boundary of the sub-array
     * @param right inclusive right boundary of the sub-array
     * @param stats comparison counter
     */
    private static void mergeSortRecursive(int[] arr, int left, int right, SortStats stats) {

        if (left >= right) {
            return; // Sub-array of size 0 or 1 — nothing to do
        }

        // Find the mid-point, avoiding overflow
        int mid = left + (right - left) / 2;

        // Recursively sort both halves
        mergeSortRecursive(arr, left,    mid,   stats); // left half
        mergeSortRecursive(arr, mid + 1, right, stats); // right half

        // Merge the two sorted halves in place
        merge(arr, left, mid, right, stats);
    }

    /**
     * Merges the two sorted sub-arrays {@code arr[left..mid]} and
     * {@code arr[mid+1..right]} into a single sorted sequence.
     *
     * <p>Copies both halves into temporary arrays, then interleaves them back
     * into the original array in sorted order.
     *
     * @param arr   the array containing the two halves
     * @param left  start of the left half
     * @param mid   end of the left half
     * @param right end of the right half
     * @param stats comparison counter
     */
    private static void merge(int[] arr, int left, int mid, int right, SortStats stats) {

        // Calculate sizes of the two halves
        int n1 = mid - left + 1;
        int n2 = right - mid;

        // Allocate temporary storage for both halves
        int[] L = new int[n1];
        int[] R = new int[n2];

        // Copy data from arr into the temporary arrays
        System.arraycopy(arr, left,    L, 0, n1);
        System.arraycopy(arr, mid + 1, R, 0, n2);

        // Merge: repeatedly pick the smaller front element from L or R
        int i = 0;      // index into L
        int j = 0;      // index into R
        int k = left;   // index into arr (destination)

        while (i < n1 && j < n2) {
            stats.incrementComparisons(); // comparing L[i] with R[j]

            if (L[i] <= R[j]) {
                arr[k++] = L[i++]; // Left element is smaller (or equal) — stable
            } else {
                arr[k++] = R[j++]; // Right element is smaller
            }
        }

        // Copy any remaining elements from L (R is already exhausted or vice versa)
        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }

    // -------------------------------------------------------------------------
    // 3. Quick Sort (median-of-three pivot)
    // -------------------------------------------------------------------------

    /**
     * Sorts {@code arr} using Quick Sort with <em>median-of-three</em> pivot selection.
     *
     * <p>Median-of-three examines the first, middle, and last elements of each
     * partition and uses their median as the pivot, dramatically reducing the
     * probability of the O(n²) worst case that arises on sorted/reverse-sorted
     * input when using a fixed-position pivot.
     *
     * <p>Sub-arrays of size &lt; 10 are handed off to Insertion Sort, which is
     * faster in practice for very small partitions due to lower overhead.
     *
     * <p><b>When to use:</b> general-purpose sorting of large, randomly distributed
     * datasets. Fastest in practice on average due to excellent cache locality.
     *
     * @param arr   the array to sort; modified in place
     * @param stats counter for comparisons and swaps; must not be {@code null}
     */
    public static void quickSort(int[] arr, SortStats stats) {

        if (arr == null || arr.length <= 1) {
            return; // Nothing to sort
        }

        quickSortRecursive(arr, 0, arr.length - 1, stats);
    }

    /**
     * Recursive Quick Sort helper. Partitions {@code arr[low..high]} around a
     * pivot, then recursively sorts both sub-arrays.
     *
     * @param arr   the array being sorted
     * @param low   inclusive left boundary of the current sub-array
     * @param high  inclusive right boundary of the current sub-array
     * @param stats comparison and swap counter
     */
    private static void quickSortRecursive(int[] arr, int low, int high, SortStats stats) {

        if (low >= high) {
            return; // Base case — sub-array has 0 or 1 elements
        }

        // For tiny sub-arrays, insertion sort is faster (lower overhead)
        if (high - low < 10) {
            insertionSortRange(arr, low, high, stats);
            return;
        }

        // Partition and get the pivot's final sorted index
        int pivotIndex = partition(arr, low, high, stats);

        // Recursively sort the sub-arrays on either side of the pivot
        quickSortRecursive(arr, low,           pivotIndex - 1, stats); // left partition
        quickSortRecursive(arr, pivotIndex + 1, high,          stats); // right partition
    }

    /**
     * Partitions {@code arr[low..high]} so that all elements less than or equal
     * to the pivot are to its left, and all greater elements are to its right.
     *
     * <p>Uses <em>median-of-three</em> to select the pivot: the median of
     * {@code arr[low]}, {@code arr[mid]}, and {@code arr[high]} is moved to
     * {@code arr[high]} before partitioning begins.
     *
     * @param arr   the array to partition
     * @param low   left boundary
     * @param high  right boundary (pivot ends up here after median selection)
     * @param stats comparison and swap counter
     * @return final index of the pivot element in the sorted array
     */
    private static int partition(int[] arr, int low, int high, SortStats stats) {

        // Compute mid-point, then rearrange arr[low], arr[mid], arr[high]
        // so that arr[mid] is the median; swap it to arr[high] to use as pivot
        int mid = low + (high - low) / 2;
        placeMedianAtHigh(arr, low, mid, high, stats);

        int pivot = arr[high]; // Pivot is now at arr[high]
        int i     = low - 1;  // i will end up pointing to the last element <= pivot

        for (int j = low; j < high; j++) {
            stats.incrementComparisons(); // compare arr[j] with pivot

            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j, stats); // Move element <= pivot to the left partition
            }
        }

        // Place the pivot in its final sorted position
        swap(arr, i + 1, high, stats);

        return i + 1; // Return pivot's final index
    }

    /**
     * Rearranges {@code arr[a]}, {@code arr[b]}, and {@code arr[c]} so that
     * {@code arr[b]} holds the median value, then swaps the median to {@code arr[c]}.
     *
     * <p>This is the "median-of-three" step: after the call, {@code arr[c]}
     * (i.e. {@code arr[high]}) holds the best pivot estimate for the partition.
     *
     * @param arr   the array
     * @param a     index of the first candidate (low)
     * @param b     index of the second candidate (mid)
     * @param c     index of the third candidate (high)
     * @param stats comparison and swap counter
     */
    private static void placeMedianAtHigh(int[] arr, int a, int b, int c, SortStats stats) {

        // Sort arr[a], arr[b], arr[c] such that arr[b] is the median
        stats.incrementComparisons();
        if (arr[a] > arr[b]) swap(arr, a, b, stats); // ensure arr[a] <= arr[b]

        stats.incrementComparisons();
        if (arr[b] > arr[c]) swap(arr, b, c, stats); // ensure arr[b] <= arr[c]

        stats.incrementComparisons();
        if (arr[a] > arr[b]) swap(arr, a, b, stats); // ensure arr[a] <= arr[b] again

        // Now arr[b] is the median; swap it to arr[c] so it becomes the pivot
        swap(arr, b, c, stats);
    }

    /**
     * Applies Insertion Sort to the sub-range {@code arr[low..high]} (inclusive).
     *
     * <p>Used by Quick Sort as a fall-back for partitions smaller than 10 elements,
     * where Insertion Sort's low overhead makes it faster than continuing to recurse.
     *
     * @param arr   the array containing the sub-range
     * @param low   start of the sub-range (inclusive)
     * @param high  end of the sub-range (inclusive)
     * @param stats comparison and swap counter
     */
    private static void insertionSortRange(int[] arr, int low, int high, SortStats stats) {

        for (int i = low + 1; i <= high; i++) {
            int key = arr[i];
            int j   = i - 1;

            while (j >= low && arr[j] > key) {
                stats.incrementComparisons();
                stats.incrementSwaps();
                arr[j + 1] = arr[j];
                j--;
            }
            if (j >= low) stats.incrementComparisons();
            arr[j + 1] = key;
        }
    }

    // -------------------------------------------------------------------------
    // 4. Heap Sort
    // -------------------------------------------------------------------------

    /**
     * Sorts {@code arr} using Heap Sort.
     *
     * <p>Works in two phases:
     * <ol>
     *   <li><b>Build max-heap:</b> rearrange {@code arr} so it satisfies the
     *       max-heap property (parent &ge; children) in O(n).</li>
     *   <li><b>Extract max repeatedly:</b> swap the root (maximum element) with
     *       the last element, reduce the heap size by one, and re-heapify in
     *       O(log n). Repeat n−1 times for O(n log n) total.</li>
     * </ol>
     *
     * <p><b>When to use:</b> when O(1) auxiliary space and O(n log n) worst-case
     * guarantee are both required (e.g. embedded systems with strict memory limits).
     *
     * @param arr   the array to sort; modified in place
     * @param stats counter for comparisons and swaps; must not be {@code null}
     */
    public static void heapSort(int[] arr, SortStats stats) {

        if (arr == null || arr.length <= 1) {
            return; // Nothing to sort
        }

        int n = arr.length;

        // ── Phase 1: Build a max-heap (bottom-up) ────────────────────────────
        // Start from the last non-leaf node and heapify each sub-tree upward.
        // This is O(n) overall (not O(n log n) despite the loop).
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i, stats);
        }

        // ── Phase 2: Extract elements from the heap one by one ────────────────
        for (int i = n - 1; i > 0; i--) {
            // The current root arr[0] is the max — move it to the sorted portion
            swap(arr, 0, i, stats);

            // Re-heapify the reduced heap (heap size is now i, not n)
            heapify(arr, i, 0, stats);
        }
    }

    /**
     * Ensures the sub-tree rooted at index {@code rootIdx} within a heap of
     * size {@code heapSize} satisfies the max-heap property.
     *
     * <p>If either child of the root is larger than the root, they are swapped
     * and the process repeats recursively down the affected branch until the
     * property is restored or a leaf is reached.
     *
     * @param arr     the array representing the heap
     * @param heapSize the number of elements currently in the heap
     * @param rootIdx  the index of the sub-tree root to heapify
     * @param stats    comparison and swap counter
     */
    private static void heapify(int[] arr, int heapSize, int rootIdx, SortStats stats) {

        int largest = rootIdx;        // Assume the root is the largest
        int left    = 2 * rootIdx + 1; // Left child index
        int right   = 2 * rootIdx + 2; // Right child index

        // Check if the left child exists and is larger than the current largest
        if (left < heapSize) {
            stats.incrementComparisons();
            if (arr[left] > arr[largest]) {
                largest = left;
            }
        }

        // Check if the right child exists and is larger than the current largest
        if (right < heapSize) {
            stats.incrementComparisons();
            if (arr[right] > arr[largest]) {
                largest = right;
            }
        }

        // If the largest is not the root, swap and continue heapifying downward
        if (largest != rootIdx) {
            swap(arr, rootIdx, largest, stats);
            heapify(arr, heapSize, largest, stats); // Recurse on the affected sub-tree
        }
    }

    // -------------------------------------------------------------------------
    // Shared utility
    // -------------------------------------------------------------------------

    /**
     * Swaps the elements at indices {@code i} and {@code j} in {@code arr},
     * and records one swap in {@code stats}.
     *
     * <p>This is a no-op when {@code i == j} (no swap needed, no count recorded).
     *
     * @param arr   the array whose elements are being swapped
     * @param i     index of the first element
     * @param j     index of the second element
     * @param stats swap counter
     */
    private static void swap(int[] arr, int i, int j, SortStats stats) {
        if (i == j) return; // Avoid unnecessary work and inflating the swap count

        stats.incrementSwaps();
        int temp = arr[i];
        arr[i]   = arr[j];
        arr[j]   = temp;
    }

    /**
     * Returns an independent copy of {@code original}.
     *
     * <p>Convenience method used by benchmark and test code to create a fresh
     * copy of an array before each sort, so that the same input can be fed to
     * multiple algorithms without the first sort affecting the others.
     *
     * @param original the source array
     * @return a new array with the same length and element values
     */
    public static int[] copyArray(int[] original) {
        return java.util.Arrays.copyOf(original, original.length);
    }
}
