package search;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides three distinct search strategies for the TechMart product catalog,
 * plus a hybrid insertion operation that keeps the catalog sorted.
 *
 * <h2>Search strategies</h2>
 * <ol>
 *   <li><b>Sequential search by ID</b> – O(n); no precondition on order.</li>
 *   <li><b>Binary search by ID</b> – O(log n); array <em>must</em> be sorted
 *       ascending by {@code productId}.</li>
 *   <li><b>Hybrid name search</b> – O(1) average via an internal
 *       {@link HashMap}; call {@link #buildNameIndex} once before use.</li>
 * </ol>
 *
 * <h2>Hybrid insertion</h2>
 * {@link #addProduct} locates the correct position in O(log n) using binary
 * search, shifts elements in O(n), and simultaneously updates the name index.
 *
 * @author  Bethel Messiah
 * @version 1.0
 * @see     Product
 */
public class ProductSearch {

    // -------------------------------------------------------------------------
    // Internal state
    // -------------------------------------------------------------------------

    /**
     * Supplementary name-to-product index that powers the hybrid name search.
     * Keys are stored in lower-case so lookups are case-insensitive.
     * Must be populated by calling {@link #buildNameIndex} before use.
     */
    private final Map<String, Product> nameIndex = new HashMap<>();

    // -------------------------------------------------------------------------
    // 1. Sequential Search
    // -------------------------------------------------------------------------

    /**
     * Searches for a product by ID using sequential (linear) search.
     *
     * <p>Iterates through every occupied slot in the array from index {@code 0}
     * up to {@code size - 1}, returning the first element whose
     * {@code productId} matches {@code targetId}.
     *
     * <p><b>Complexity:</b>
     * <ul>
     *   <li>Best case  – O(1): target is at index 0.</li>
     *   <li>Average    – O(n/2): target is equally likely anywhere.</li>
     *   <li>Worst case – O(n): target is last or absent.</li>
     * </ul>
     *
     * @param products array of products to scan (need not be sorted)
     * @param size     number of valid (non-null) elements in the array
     * @param targetId the product ID being searched for
     * @return the first matching {@link Product}, or {@code null} if not found
     */
    public Product sequentialSearchById(Product[] products, int size, int targetId) {

        // Guard against null array or empty logical array
        if (products == null || size <= 0) {
            return null;
        }

        // Walk element by element — no assumption about order
        for (int i = 0; i < size; i++) {
            if (products[i] != null && products[i].getProductId() == targetId) {
                return products[i]; // Found — return immediately (best case if i == 0)
            }
        }

        return null; // Target not present in the array
    }

    // -------------------------------------------------------------------------
    // 2. Binary Search
    // -------------------------------------------------------------------------

    /**
     * Searches for a product by ID using iterative binary search.
     *
     * <p><b>Precondition:</b> {@code products[0..size-1]} must be sorted in
     * ascending order by {@code productId}. Violating this precondition produces
     * undefined results (the method may return {@code null} even when the product
     * exists, or may return the wrong product).
     *
     * <p><b>Complexity:</b>
     * <ul>
     *   <li>Best case  – O(1): target happens to be the first mid-point.</li>
     *   <li>Average    – O(log n): roughly log₂(n) comparisons.</li>
     *   <li>Worst case – O(log n): target is absent (all partitions exhausted).</li>
     * </ul>
     *
     * <p>The mid-point is computed as {@code left + (right - left) / 2} rather than
     * {@code (left + right) / 2} to avoid integer overflow when indices are large.
     *
     * @param products sorted array of products (ascending by productId)
     * @param size     number of valid elements in the array
     * @param targetId the product ID being searched for
     * @return the matching {@link Product}, or {@code null} if not found
     */
    public Product binarySearchById(Product[] products, int size, int targetId) {

        // Guard against null / empty input
        if (products == null || size <= 0) {
            return null;
        }

        int left  = 0;
        int right = size - 1;

        // Standard iterative binary search loop
        while (left <= right) {

            // Safe midpoint calculation — avoids potential overflow
            int mid   = left + (right - left) / 2;
            int midId = products[mid].getProductId();

            if (midId == targetId) {
                return products[mid];       // Found at mid-point
            } else if (midId < targetId) {
                left = mid + 1;             // Target is in the right half
            } else {
                right = mid - 1;            // Target is in the left half
            }
        }

        return null; // Search space exhausted — target not in array
    }

    // -------------------------------------------------------------------------
    // 3. Sequential Name Search
    // -------------------------------------------------------------------------

    /**
     * Searches for a product by name using a sequential scan.
     *
     * <p>Because product names are not kept in sorted order, only a linear scan
     * is possible here. This is an O(n) operation. For faster name lookups, use
     * {@link #buildNameIndex} and {@link #hybridSearchByName} instead.
     *
     * <p>Matching is <em>case-insensitive</em>.
     *
     * @param products  array of products to scan
     * @param size      number of valid elements in the array
     * @param targetName the exact product name to match (case-insensitive)
     * @return the first matching {@link Product}, or {@code null} if not found
     */
    public Product searchByName(Product[] products, int size, String targetName) {

        // Reject degenerate inputs
        if (products == null || size <= 0 || targetName == null) {
            return null;
        }

        // Linear scan — compare ignoring case so "LAPTOP" matches "Laptop"
        for (int i = 0; i < size; i++) {
            if (products[i] != null
                    && products[i].getProductName().equalsIgnoreCase(targetName)) {
                return products[i];
            }
        }

        return null; // No match found
    }

    // -------------------------------------------------------------------------
    // 4. Hybrid – HashMap name index
    // -------------------------------------------------------------------------

    /**
     * Builds (or rebuilds) the internal HashMap name index from a product array.
     *
     * <p>After this method returns, {@link #hybridSearchByName} delivers O(1)
     * average-time name lookups. Keys are stored in lower-case so the index is
     * case-insensitive.
     *
     * <p>Call this once after the initial array population. When products are
     * subsequently added via {@link #addProduct}, the index is kept in sync
     * automatically.
     *
     * @param products array of products to index
     * @param size     number of valid elements in the array
     */
    public void buildNameIndex(Product[] products, int size) {

        nameIndex.clear(); // Discard any previously built index

        for (int i = 0; i < size; i++) {
            if (products[i] != null) {
                // Store key in lower-case to enable case-insensitive lookups
                nameIndex.put(products[i].getProductName().toLowerCase(), products[i]);
            }
        }
    }

    /**
     * Looks up a product by name in the internal HashMap index.
     *
     * <p><b>Precondition:</b> {@link #buildNameIndex} must have been called at
     * least once before this method is used.
     *
     * <p><b>Complexity:</b> O(1) average, O(n) worst (hash collision chain).
     *
     * @param targetName product name to find (case-insensitive)
     * @return the matching {@link Product}, or {@code null} if not found
     */
    public Product hybridSearchByName(String targetName) {

        if (targetName == null) {
            return null;
        }

        // HashMap.getOrDefault avoids a NullPointerException on a miss
        return nameIndex.getOrDefault(targetName.toLowerCase(), null);
    }

    // -------------------------------------------------------------------------
    // 5. Hybrid – sorted insertion
    // -------------------------------------------------------------------------

    /**
     * Inserts a new product into the sorted array while preserving ascending
     * order by {@code productId}, and simultaneously updates the name index.
     *
     * <h3>Algorithm</h3>
     * <ol>
     *   <li>Use binary search to locate the correct insertion position in O(log n).</li>
     *   <li>Shift all elements from that position rightward by one slot in O(n).</li>
     *   <li>Place the new product at the vacated position.</li>
     *   <li>Insert the product into the name index in O(1) average.</li>
     * </ol>
     *
     * <p><b>Overall complexity:</b> O(n) time (dominated by the shift), O(1) extra space.
     *
     * @param products   sorted product array; must have at least one spare slot
     *                   ({@code products.length > size})
     * @param size       current number of valid elements
     * @param newProduct the product to insert; must not be {@code null}
     * @return the new logical size of the array after insertion ({@code size + 1})
     * @throws IllegalArgumentException if {@code newProduct} is {@code null}
     *         or the array has no remaining capacity
     */
    public int addProduct(Product[] products, int size, Product newProduct) {

        // Input validation
        if (newProduct == null) {
            throw new IllegalArgumentException("newProduct must not be null.");
        }
        if (size >= products.length) {
            throw new IllegalArgumentException(
                    "Array is at full capacity (" + products.length + ").");
        }

        // ── Step 1: Binary search for the correct insertion position ──────────
        int left  = 0;
        int right = size - 1;
        int pos   = size; // default: append at end if new ID is the largest

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (products[mid].getProductId() < newProduct.getProductId()) {
                left = mid + 1;         // New ID belongs further right
            } else {
                pos   = mid;            // Potential insertion point — keep searching left
                right = mid - 1;
            }
        }

        // ── Step 2: Shift elements right to open a slot at 'pos' ─────────────
        for (int i = size; i > pos; i--) {
            products[i] = products[i - 1];
        }

        // ── Step 3: Place the new product in the vacated slot ─────────────────
        products[pos] = newProduct;

        // ── Step 4: Keep the name index in sync ───────────────────────────────
        nameIndex.put(newProduct.getProductName().toLowerCase(), newProduct);

        // Return the updated logical size
        return size + 1;
    }
}
