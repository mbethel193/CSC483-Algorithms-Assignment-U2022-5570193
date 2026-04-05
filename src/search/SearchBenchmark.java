package search;

import java.util.Arrays;
import java.util.Random;

/**
 * Benchmark driver for <b>Question 1</b> — TechMart Search Performance Analysis.
 *
 * <p>This program:
 * <ol>
 *   <li>Generates a catalog of {@value #DATASET_SIZE} products with random IDs
 *       drawn from [1, {@value #MAX_ID}], sorted ascending.</li>
 *   <li>Warms up the JVM to allow Just-In-Time compilation to stabilise before
 *       any timing measurements are taken.</li>
 *   <li>Measures sequential search and binary search times for best, average,
 *       and worst cases, each averaged over {@value #MEASURED_RUNS} runs.</li>
 *   <li>Measures hybrid HashMap name-search time and sorted-insert time.</li>
 *   <li>Prints a formatted performance comparison table to standard output.</li>
 * </ol>
 *
 * <h2>How to run</h2>
 * <pre>
 *   javac -d out src/search/*.java
 *   java  -cp out search.SearchBenchmark
 * </pre>
 *
 * @author  Bethel Messiah
 * @version 1.0
 */
public class SearchBenchmark {

    // -------------------------------------------------------------------------
    // Configuration constants
    // -------------------------------------------------------------------------

    /** Number of products in the generated catalog. */
    private static final int DATASET_SIZE  = 100_000;

    /** Upper bound (inclusive) for randomly generated product IDs. */
    private static final int MAX_ID        = 200_000;

    /** Number of warm-up iterations before timing begins. */
    private static final int WARMUP_RUNS   = 5;

    /** Number of measured iterations whose times are averaged. */
    private static final int MEASURED_RUNS = 5;

    /** Fixed random seed for reproducible dataset generation. */
    private static final long SEED         = 42L;

    /** Product categories used during random generation. */
    private static final String[] CATEGORIES = {
            "Laptop", "Phone", "Tablet", "Monitor", "Keyboard",
            "Mouse",  "Headset", "Camera", "Printer", "Speaker"
    };

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Application entry point. Orchestrates dataset generation, JVM warm-up,
     * all benchmark measurements, and formatted output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        System.out.println("================================================================");
        System.out.printf ("  TECHMART SEARCH PERFORMANCE ANALYSIS (n = %,d products)%n",
                DATASET_SIZE);
        System.out.println("================================================================");

        // ── Step 1: Build the sorted product catalog ──────────────────────────
        Product[]     products = generateSortedCatalog(DATASET_SIZE, SEED);
        ProductSearch searcher = new ProductSearch();

        // ── Step 2: Warm up the JVM so JIT compilation does not skew results ──
        warmUp(products, searcher);

        // ── Step 3: Sequential Search benchmarks ──────────────────────────────
        System.out.println("\nSEQUENTIAL SEARCH:");

        // Best case  — target is at index 0 (only 1 comparison)
        double seqBest  = measureSeq(searcher, products, products[0].getProductId());

        // Average case — target is at the midpoint of the array
        double seqAvg   = measureSeq(searcher, products, products[DATASET_SIZE / 2].getProductId());

        // Worst case  — target does not exist, so the full array is scanned
        double seqWorst = measureSeq(searcher, products, MAX_ID + 1);

        System.out.printf("  Best Case   (target at index 0)     : %8.3f ms%n", seqBest);
        System.out.printf("  Average Case (target at midpoint)   : %8.3f ms%n", seqAvg);
        System.out.printf("  Worst Case   (target not in array)  : %8.3f ms%n", seqWorst);

        // ── Step 4: Binary Search benchmarks ──────────────────────────────────
        System.out.println("\nBINARY SEARCH:");

        // Best case  — target is exactly the first mid-point (index DATASET_SIZE/2)
        double binBest  = measureBin(searcher, products, products[DATASET_SIZE / 2].getProductId());

        // Average case — same target as sequential average for a fair comparison
        double binAvg   = measureBin(searcher, products, products[DATASET_SIZE / 3].getProductId());

        // Worst case  — target is absent, forcing all log₂(n) partitions
        double binWorst = measureBin(searcher, products, MAX_ID + 1);

        System.out.printf("  Best Case   (target at mid-point)   : %8.3f ms%n", binBest);
        System.out.printf("  Average Case (random existing ID)   : %8.3f ms%n", binAvg);
        System.out.printf("  Worst Case   (target not in array)  : %8.3f ms%n", binWorst);

        // ── Step 5: Compute speedup ratio ─────────────────────────────────────
        // Guard against division by zero for extremely fast binary search runs
        double speedup = (binAvg > 0) ? (seqAvg / binAvg) : Double.MAX_VALUE;
        System.out.printf("%nPERFORMANCE IMPROVEMENT: Binary search is ~%.0fx faster on average%n",
                speedup);

        // ── Step 6: Hybrid search benchmarks ──────────────────────────────────
        // Build the name index once, then time repeated lookups and insertions
        searcher.buildNameIndex(products, DATASET_SIZE);
        String sampleName = products[DATASET_SIZE / 4].getProductName();

        double hybridSearch = measureHybridSearch(searcher, sampleName);
        double hybridInsert = measureHybridInsert(searcher, products, DATASET_SIZE);

        System.out.println("\nHYBRID NAME SEARCH:");
        System.out.printf("  Average search time (HashMap) : %8.3f ms%n", hybridSearch);
        System.out.printf("  Average insert time (sorted)  : %8.3f ms%n", hybridInsert);
        System.out.println("\n================================================================");

        // ── Step 7: Print formatted summary table ─────────────────────────────
        printSummaryTable(seqBest, seqAvg, seqWorst,
                binBest, binAvg, binWorst,
                hybridSearch, hybridInsert);
    }

    // -------------------------------------------------------------------------
    // Dataset generation
    // -------------------------------------------------------------------------

    /**
     * Generates a catalog of {@code size} unique-ID products and returns them
     * sorted ascending by {@code productId}.
     *
     * <p>Product IDs are sampled without replacement from [1, {@value #MAX_ID}]
     * using a boolean "used" array to avoid duplicates efficiently. The result is
     * sorted with {@link Arrays#sort} (which uses Tim Sort under the hood) so the
     * array satisfies the precondition for binary search.
     *
     * @param size number of products to generate
     * @param seed random seed for reproducibility
     * @return sorted {@code Product[]} with {@code size + 100} allocated slots
     *         (the extra capacity is needed for the insert benchmark)
     */
    private static Product[] generateSortedCatalog(int size, long seed) {

        Random  rng  = new Random(seed);
        boolean[] used = new boolean[MAX_ID + 1]; // tracks which IDs have been used

        // Allocate with extra slots so addProduct() benchmarks have capacity
        Product[] arr = new Product[size + 100];

        int count = 0;
        while (count < size) {
            int id = rng.nextInt(MAX_ID) + 1; // ID in [1, MAX_ID]

            if (!used[id]) {
                used[id] = true;

                // Build a descriptive name and pick a random category
                String name     = "Product_" + id;
                String category = CATEGORIES[rng.nextInt(CATEGORIES.length)];

                // Random price in [10.00, 3000.00], rounded to 2 decimal places
                double price = Math.round((10.0 + rng.nextDouble() * 2990.0) * 100.0) / 100.0;

                int stock = rng.nextInt(501); // [0, 500]

                arr[count++] = new Product(id, name, category, price, stock);
            }
        }

        // Sort by productId so binary search works correctly
        Arrays.sort(arr, 0, size);

        return arr;
    }

    // -------------------------------------------------------------------------
    // Warm-up
    // -------------------------------------------------------------------------

    /**
     * Runs both search algorithms several times without recording any times.
     *
     * <p>This allows the JVM's Just-In-Time compiler to compile and optimise
     * the hot code paths before the real measurements begin, preventing the
     * first measured run from being artificially slow.
     *
     * @param products the product catalog
     * @param searcher the search instance to warm up
     */
    private static void warmUp(Product[] products, ProductSearch searcher) {
        int id = products[0].getProductId();

        for (int i = 0; i < WARMUP_RUNS; i++) {
            searcher.sequentialSearchById(products, DATASET_SIZE, id);
            searcher.binarySearchById(products, DATASET_SIZE, id);
        }
    }

    // -------------------------------------------------------------------------
    // Measurement helpers
    // -------------------------------------------------------------------------

    /**
     * Measures the average sequential search time for {@code targetId} over
     * {@value #MEASURED_RUNS} independent runs.
     *
     * @param s        the {@link ProductSearch} instance
     * @param products the sorted catalog
     * @param targetId the ID to search for
     * @return mean execution time in milliseconds
     */
    private static double measureSeq(ProductSearch s, Product[] products, int targetId) {
        long totalNs = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {
            long t0 = System.nanoTime();
            s.sequentialSearchById(products, DATASET_SIZE, targetId);
            totalNs += System.nanoTime() - t0;
        }

        // Convert nanoseconds to milliseconds and return the mean
        return (totalNs / (double) MEASURED_RUNS) / 1_000_000.0;
    }

    /**
     * Measures the average binary search time for {@code targetId} over
     * {@value #MEASURED_RUNS} independent runs.
     *
     * @param s        the {@link ProductSearch} instance
     * @param products the sorted catalog
     * @param targetId the ID to search for
     * @return mean execution time in milliseconds
     */
    private static double measureBin(ProductSearch s, Product[] products, int targetId) {
        long totalNs = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {
            long t0 = System.nanoTime();
            s.binarySearchById(products, DATASET_SIZE, targetId);
            totalNs += System.nanoTime() - t0;
        }

        return (totalNs / (double) MEASURED_RUNS) / 1_000_000.0;
    }

    /**
     * Measures the average HashMap name-search time over {@value #MEASURED_RUNS} runs.
     *
     * @param s          the {@link ProductSearch} instance (index must be built)
     * @param targetName the name to look up
     * @return mean execution time in milliseconds
     */
    private static double measureHybridSearch(ProductSearch s, String targetName) {
        long totalNs = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {
            long t0 = System.nanoTime();
            s.hybridSearchByName(targetName);
            totalNs += System.nanoTime() - t0;
        }

        return (totalNs / (double) MEASURED_RUNS) / 1_000_000.0;
    }

    /**
     * Measures the average sorted-insert time over {@value #MEASURED_RUNS} runs.
     *
     * <p>Each iteration clones the catalog into a fresh temporary array so that
     * repeated measurements are independent and do not compound (i.e. the array
     * does not grow across iterations).
     *
     * @param s        the {@link ProductSearch} instance
     * @param products the sorted catalog (used as the source for cloning)
     * @param size     current logical size of the catalog
     * @return mean execution time in milliseconds
     */
    private static double measureHybridInsert(ProductSearch s, Product[] products, int size) {
        long totalNs = 0;

        for (int i = 0; i < MEASURED_RUNS; i++) {

            // Clone the catalog so each insert starts from the same baseline
            Product[] tmp = Arrays.copyOf(products, size + 10);

            // Use a unique ID above MAX_ID so there is no collision with existing IDs
            int newId = MAX_ID + 10 + i;
            Product newProduct = new Product(newId, "NewProduct_" + i, "Accessory", 49.99, 10);

            long t0 = System.nanoTime();
            s.addProduct(tmp, size, newProduct);
            totalNs += System.nanoTime() - t0;
        }

        return (totalNs / (double) MEASURED_RUNS) / 1_000_000.0;
    }

    // -------------------------------------------------------------------------
    // Output formatting
    // -------------------------------------------------------------------------

    /**
     * Prints a formatted summary table comparing both search algorithms across
     * all three cases, plus the hybrid operations.
     *
     * @param sBest     sequential best-case time (ms)
     * @param sAvg      sequential average-case time (ms)
     * @param sWorst    sequential worst-case time (ms)
     * @param bBest     binary best-case time (ms)
     * @param bAvg      binary average-case time (ms)
     * @param bWorst    binary worst-case time (ms)
     * @param hSearch   hybrid name-search time (ms)
     * @param hInsert   hybrid sorted-insert time (ms)
     */
    private static void printSummaryTable(double sBest,  double sAvg,  double sWorst,
                                          double bBest,  double bAvg,  double bWorst,
                                          double hSearch, double hInsert) {

        System.out.println("\nSUMMARY TABLE");
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.printf("%-28s %10s %10s %10s%n",
                "Algorithm", "Best (ms)", "Avg (ms)", "Worst (ms)");
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.printf("%-28s %10.3f %10.3f %10.3f%n",
                "Sequential Search", sBest, sAvg, sWorst);
        System.out.printf("%-28s %10.3f %10.3f %10.3f%n",
                "Binary Search",     bBest, bAvg, bWorst);
        System.out.printf("%-28s %10.3f %10s %10s%n",
                "Hybrid Name Search", hSearch, "N/A", "N/A");
        System.out.printf("%-28s %10s %10.3f %10s%n",
                "Hybrid Sorted Insert", "N/A", hInsert, "N/A");
        System.out.println("─────────────────────────────────────────────────────────────");
    }
}
