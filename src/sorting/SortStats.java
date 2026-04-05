package sorting;

/**
 * A lightweight counter that tracks the number of element <em>comparisons</em>
 * and element <em>swaps / assignments</em> performed during a sort.
 *
 * <p>An instance of this class is passed into every sorting method in
 * {@link SortingAlgorithms}. The sorting method calls
 * {@link #incrementComparisons()} each time it compares two elements and
 * {@link #incrementSwaps()} each time it moves an element. After the sort
 * completes, the caller can read the accumulated counts.
 *
 * <p>Call {@link #reset()} between runs so that counters from one invocation do
 * not bleed into the next.
 *
 * <p><b>Thread safety:</b> This class is <em>not</em> thread-safe. Each thread
 * (or each benchmark run) should use its own {@code SortStats} instance.
 *
 * @author  Bethel Messiah
 * @version 1.0
 * @see     SortingAlgorithms
 */
public class SortStats {

    /** Total number of element comparisons performed since the last {@link #reset()}. */
    private long comparisons = 0;

    /** Total number of element swaps or array-write assignments since the last {@link #reset()}. */
    private long swaps       = 0;

    // -------------------------------------------------------------------------
    // Mutation methods (called by sorting algorithms)
    // -------------------------------------------------------------------------

    /**
     * Increments the comparison counter by one.
     *
     * <p>Sorting algorithms call this every time two array elements are compared
     * with a relational operator ({@code <}, {@code >}, {@code <=}, etc.).
     */
    public void incrementComparisons() {
        comparisons++;
    }

    /**
     * Increments the swap / assignment counter by one.
     *
     * <p>Sorting algorithms call this every time an element is written to a new
     * position — whether as a full swap, a shift (insertion sort), or a merge
     * write. Merge Sort, which has no swaps in the traditional sense, does not
     * call this method; its "N/A" swap count is handled at the benchmark level.
     */
    public void incrementSwaps() {
        swaps++;
    }

    /**
     * Resets both counters to zero, ready for the next benchmark run.
     */
    public void reset() {
        comparisons = 0;
        swaps       = 0;
    }

    // -------------------------------------------------------------------------
    // Query methods (called by the benchmark)
    // -------------------------------------------------------------------------

    /**
     * Returns the total number of element comparisons recorded since the last
     * {@link #reset()}.
     *
     * @return non-negative comparison count
     */
    public long getComparisons() {
        return comparisons;
    }

    /**
     * Returns the total number of element swaps / write-assignments recorded
     * since the last {@link #reset()}.
     *
     * @return non-negative swap count
     */
    public long getSwaps() {
        return swaps;
    }

    /**
     * Returns a human-readable representation of the current counter values.
     *
     * @return formatted string with both counters
     */
    @Override
    public String toString() {
        return String.format("SortStats{comparisons=%,d, swaps=%,d}",
                comparisons, swaps);
    }
}
