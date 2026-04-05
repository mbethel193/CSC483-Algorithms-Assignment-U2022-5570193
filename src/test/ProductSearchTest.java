package test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import search.Product;
import search.ProductSearch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for {@link ProductSearch} and {@link Product}.
 *
 * <p>Covers:
 * <ul>
 *   <li>Product constructor validation (invalid arguments)</li>
 *   <li>Sequential search — best, average, worst, null/empty inputs</li>
 *   <li>Binary search   — best, average, worst, null/empty inputs, parameterised</li>
 *   <li>Sequential name search — exact match, case-insensitivity, not-found</li>
 *   <li>Hybrid HashMap name search — post-index build, case-insensitivity, not-found</li>
 *   <li>{@code addProduct} — sorted order maintained, boundary positions, null guard,
 *       name index updated</li>
 * </ul>
 *
 * @author  Bethel Messiah
 * @version 1.0
 */
@DisplayName("ProductSearch — Full Test Suite")
class ProductSearchTest {

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    /** Shared search engine re-created before every test. */
    private ProductSearch searcher;

    /**
     * Ten products sorted ascending by productId.
     * IDs are spaced by 10 so there is plenty of room to insert mid-range values.
     */
    private Product[] catalog;

    /** Logical size of the catalog (number of valid entries). */
    private static final int SIZE = 10;

    /**
     * Initialises a fresh {@link ProductSearch} instance and a ten-element
     * sorted catalog before each test method runs.
     */
    @BeforeEach
    void setUp() {
        searcher = new ProductSearch();

        // Build catalog sorted by productId (required for binary search)
        catalog = new Product[]{
                new Product(10,  "Alpha Laptop",     "Laptop",   999.99, 5),
                new Product(20,  "Beta Phone",       "Phone",    499.99, 20),
                new Product(30,  "Gamma Tablet",     "Tablet",   349.99, 15),
                new Product(40,  "Delta Monitor",    "Monitor",  299.99, 8),
                new Product(50,  "Epsilon Keyboard", "Keyboard",  79.99, 50),
                new Product(60,  "Zeta Mouse",       "Mouse",     39.99, 100),
                new Product(70,  "Eta Headset",      "Headset",  129.99, 25),
                new Product(80,  "Theta Camera",     "Camera",   249.99, 10),
                new Product(90,  "Iota Printer",     "Printer",  189.99, 7),
                new Product(100, "Kappa Speaker",    "Speaker",   89.99, 30),
        };
    }

    // =========================================================================
    // Product — constructor validation
    // =========================================================================

    /**
     * Verifies that a zero or negative productId is rejected with
     * {@link IllegalArgumentException}.
     */
    @Test
    @DisplayName("Product: rejects non-positive productId")
    void product_rejectsNonPositiveId() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(0,  "X", "Cat", 1.0, 1),
                "productId = 0 should throw");
        assertThrows(IllegalArgumentException.class,
                () -> new Product(-1, "X", "Cat", 1.0, 1),
                "negative productId should throw");
    }

    /**
     * Verifies that a blank or empty productName is rejected.
     */
    @Test
    @DisplayName("Product: rejects blank productName")
    void product_rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "",   "Cat", 1.0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "  ", "Cat", 1.0, 1));
    }

    /**
     * Verifies that a negative price is rejected.
     */
    @Test
    @DisplayName("Product: rejects negative price")
    void product_rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "X", "Cat", -0.01, 1));
    }

    /**
     * Verifies that a negative stockQuantity is rejected.
     */
    @Test
    @DisplayName("Product: rejects negative stockQuantity")
    void product_rejectsNegativeStock() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(1, "X", "Cat", 1.0, -1));
    }

    /**
     * Verifies that a valid product is constructed without errors and its
     * fields are stored correctly.
     */
    @Test
    @DisplayName("Product: valid construction stores all fields correctly")
    void product_validConstruction() {
        Product p = new Product(42, "TestProduct", "Electronics", 19.99, 7);
        assertEquals(42,             p.getProductId());
        assertEquals("TestProduct",  p.getProductName());
        assertEquals("Electronics",  p.getCategory());
        assertEquals(19.99,          p.getPrice(), 0.001);
        assertEquals(7,              p.getStockQuantity());
    }

    // =========================================================================
    // Sequential Search — sequentialSearchById
    // =========================================================================

    /**
     * Best case: the target is at index 0 — only one comparison is needed.
     */
    @Test
    @DisplayName("Sequential search: finds product at index 0 (best case)")
    void seqSearch_bestCase_firstElement() {
        Product result = searcher.sequentialSearchById(catalog, SIZE, 10);
        assertNotNull(result, "Should find productId=10");
        assertEquals("Alpha Laptop", result.getProductName());
    }

    /**
     * Average case: the target is somewhere in the middle of the array.
     */
    @Test
    @DisplayName("Sequential search: finds product in the middle (average case)")
    void seqSearch_averageCase_middleElement() {
        Product result = searcher.sequentialSearchById(catalog, SIZE, 50);
        assertNotNull(result);
        assertEquals("Epsilon Keyboard", result.getProductName());
    }

    /**
     * Worst case: the target is the last element — the full array must be scanned.
     */
    @Test
    @DisplayName("Sequential search: finds product at last index (worst case)")
    void seqSearch_worstCase_lastElement() {
        Product result = searcher.sequentialSearchById(catalog, SIZE, 100);
        assertNotNull(result);
        assertEquals("Kappa Speaker", result.getProductName());
    }

    /**
     * Verifies that a non-existent ID returns {@code null} (full scan, worst case).
     */
    @Test
    @DisplayName("Sequential search: returns null for non-existent ID")
    void seqSearch_notFound_returnsNull() {
        assertNull(searcher.sequentialSearchById(catalog, SIZE, 999));
    }

    /**
     * Verifies graceful handling of a {@code null} array.
     */
    @Test
    @DisplayName("Sequential search: returns null on null array")
    void seqSearch_nullArray_returnsNull() {
        assertNull(searcher.sequentialSearchById(null, 5, 10));
    }

    /**
     * Verifies graceful handling when logical size is zero (empty catalog).
     */
    @Test
    @DisplayName("Sequential search: returns null when size = 0")
    void seqSearch_emptyArray_returnsNull() {
        assertNull(searcher.sequentialSearchById(catalog, 0, 10));
    }

    // =========================================================================
    // Binary Search — binarySearchById
    // =========================================================================

    /**
     * Best case: the target is exactly at the first computed mid-point.
     * For SIZE=10 that is index 5 (productId=60).
     */
    @Test
    @DisplayName("Binary search: finds product at first mid-point (best case)")
    void binSearch_bestCase_midPoint() {
        // mid = (0 + 9) / 2 = 4 → productId = 50 for a 0-indexed array of 10
        Product result = searcher.binarySearchById(catalog, SIZE, 50);
        assertNotNull(result, "Should find productId=50 at mid-point");
    }

    /**
     * Verifies that the first element in the array is findable.
     */
    @Test
    @DisplayName("Binary search: finds first element")
    void binSearch_findsFirstElement() {
        Product result = searcher.binarySearchById(catalog, SIZE, 10);
        assertNotNull(result);
        assertEquals("Alpha Laptop", result.getProductName());
    }

    /**
     * Verifies that the last element in the array is findable.
     */
    @Test
    @DisplayName("Binary search: finds last element")
    void binSearch_findsLastElement() {
        Product result = searcher.binarySearchById(catalog, SIZE, 100);
        assertNotNull(result);
        assertEquals("Kappa Speaker", result.getProductName());
    }

    /**
     * Worst case: the target does not exist and all log₂(n) partitions are
     * exhausted before returning {@code null}.
     */
    @Test
    @DisplayName("Binary search: returns null for non-existent ID (worst case)")
    void binSearch_notFound_returnsNull() {
        assertNull(searcher.binarySearchById(catalog, SIZE, 999));
    }

    /**
     * Verifies graceful handling of a {@code null} array.
     */
    @Test
    @DisplayName("Binary search: returns null on null array")
    void binSearch_nullArray_returnsNull() {
        assertNull(searcher.binarySearchById(null, 5, 10));
    }

    /**
     * Parameterised test: verifies that binary search successfully finds every
     * valid product ID in the catalog.
     *
     * @param id a valid product ID in the catalog
     */
    @ParameterizedTest(name = "Binary search finds productId = {0}")
    @ValueSource(ints = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 })
    @DisplayName("Binary search: finds every valid productId")
    void binSearch_findsAllValidIds(int id) {
        Product result = searcher.binarySearchById(catalog, SIZE, id);
        assertNotNull(result, "Expected to find productId=" + id);
        assertEquals(id, result.getProductId());
    }

    // =========================================================================
    // Sequential Name Search — searchByName
    // =========================================================================

    /**
     * Verifies that an exact name match returns the correct product.
     */
    @Test
    @DisplayName("searchByName: finds product with exact name match")
    void nameSearch_exactMatch() {
        Product result = searcher.searchByName(catalog, SIZE, "Gamma Tablet");
        assertNotNull(result);
        assertEquals(30, result.getProductId());
    }

    /**
     * Verifies that matching is case-insensitive (all lower-case query).
     */
    @Test
    @DisplayName("searchByName: case-insensitive match succeeds")
    void nameSearch_caseInsensitive() {
        Product result = searcher.searchByName(catalog, SIZE, "alpha laptop");
        assertNotNull(result, "Lower-case query should still match 'Alpha Laptop'");
        assertEquals(10, result.getProductId());
    }

    /**
     * Verifies that an unrecognised name returns {@code null}.
     */
    @Test
    @DisplayName("searchByName: returns null when name not found")
    void nameSearch_notFound() {
        assertNull(searcher.searchByName(catalog, SIZE, "Unknown Product"));
    }

    /**
     * Verifies graceful handling of a {@code null} target name.
     */
    @Test
    @DisplayName("searchByName: returns null on null targetName")
    void nameSearch_nullName() {
        assertNull(searcher.searchByName(catalog, SIZE, null));
    }

    // =========================================================================
    // Hybrid HashMap Name Search — hybridSearchByName
    // =========================================================================

    /**
     * Verifies that {@link ProductSearch#hybridSearchByName} returns the correct
     * product after the name index has been built.
     */
    @Test
    @DisplayName("Hybrid search: finds product by name after buildNameIndex")
    void hybridSearch_found() {
        searcher.buildNameIndex(catalog, SIZE);
        Product result = searcher.hybridSearchByName("Beta Phone");
        assertNotNull(result, "Index should contain 'Beta Phone'");
        assertEquals(20, result.getProductId());
    }

    /**
     * Verifies that the index lookup is case-insensitive.
     */
    @Test
    @DisplayName("Hybrid search: case-insensitive lookup succeeds")
    void hybridSearch_caseInsensitive() {
        searcher.buildNameIndex(catalog, SIZE);
        assertNotNull(searcher.hybridSearchByName("BETA PHONE"),
                "Upper-case query should still find 'Beta Phone'");
    }

    /**
     * Verifies that an unknown name returns {@code null} from the index.
     */
    @Test
    @DisplayName("Hybrid search: returns null when name not in index")
    void hybridSearch_notFound() {
        searcher.buildNameIndex(catalog, SIZE);
        assertNull(searcher.hybridSearchByName("Nonexistent Item"));
    }

    /**
     * Verifies graceful handling of a {@code null} query.
     */
    @Test
    @DisplayName("Hybrid search: returns null on null targetName")
    void hybridSearch_nullName() {
        searcher.buildNameIndex(catalog, SIZE);
        assertNull(searcher.hybridSearchByName(null));
    }

    // =========================================================================
    // addProduct — sorted insertion with index update
    // =========================================================================

    /**
     * Verifies that inserting a mid-range product maintains strict ascending
     * order and returns the correct new logical size.
     */
    @Test
    @DisplayName("addProduct: maintains sorted order and returns size + 1")
    void addProduct_maintainsSortedOrder() {

        // Provide spare capacity so addProduct can shift elements
        Product[] arr = new Product[SIZE + 5];
        System.arraycopy(catalog, 0, arr, 0, SIZE);

        // productId=55 should slot between productId=50 and productId=60
        Product newProduct = new Product(55, "New Item", "Accessory", 29.99, 5);
        int newSize = searcher.addProduct(arr, SIZE, newProduct);

        // New size must be SIZE + 1
        assertEquals(SIZE + 1, newSize, "Logical size should increase by 1");

        // Every adjacent pair must satisfy arr[i].id < arr[i+1].id
        for (int i = 0; i < newSize - 1; i++) {
            assertTrue(arr[i].getProductId() < arr[i + 1].getProductId(),
                    "Array not sorted at index " + i + ": "
                            + arr[i].getProductId() + " vs " + arr[i + 1].getProductId());
        }

        // The newly inserted product must be locatable via binary search
        assertNotNull(searcher.binarySearchById(arr, newSize, 55),
                "Binary search should find the newly inserted product");
    }

    /**
     * Verifies that a product with the smallest ID is inserted at index 0.
     */
    @Test
    @DisplayName("addProduct: inserts at the beginning when ID is smallest")
    void addProduct_insertAtBeginning() {
        Product[] arr = new Product[SIZE + 5];
        System.arraycopy(catalog, 0, arr, 0, SIZE);

        Product first = new Product(1, "First Product", "Misc", 5.0, 1);
        int newSize = searcher.addProduct(arr, SIZE, first);

        assertEquals(1,       arr[0].getProductId(), "Smallest ID should be at index 0");
        assertEquals(SIZE + 1, newSize);
    }

    /**
     * Verifies that a product with the largest ID is appended at the end.
     */
    @Test
    @DisplayName("addProduct: appends at the end when ID is largest")
    void addProduct_appendAtEnd() {
        Product[] arr = new Product[SIZE + 5];
        System.arraycopy(catalog, 0, arr, 0, SIZE);

        Product last = new Product(999, "Last Product", "Misc", 5.0, 1);
        int newSize = searcher.addProduct(arr, SIZE, last);

        assertEquals(999, arr[newSize - 1].getProductId(),
                "Largest ID should be at the last index");
    }

    /**
     * Verifies that passing {@code null} as the new product throws
     * {@link IllegalArgumentException}.
     */
    @Test
    @DisplayName("addProduct: throws IllegalArgumentException on null product")
    void addProduct_nullProduct_throws() {
        Product[] arr = new Product[SIZE + 1];
        System.arraycopy(catalog, 0, arr, 0, SIZE);
        assertThrows(IllegalArgumentException.class,
                () -> searcher.addProduct(arr, SIZE, null));
    }

    /**
     * Verifies that after insertion the hybrid name index is updated so the
     * new product is immediately findable by name.
     */
    @Test
    @DisplayName("addProduct: updates the hybrid name index for the new product")
    void addProduct_updatesNameIndex() {
        Product[] arr = new Product[SIZE + 5];
        System.arraycopy(catalog, 0, arr, 0, SIZE);

        // Build index on existing products
        searcher.buildNameIndex(arr, SIZE);

        // Insert a product whose name is not yet in the index
        Product newProduct = new Product(55, "Lambda Drone", "Electronics", 299.99, 3);
        searcher.addProduct(arr, SIZE, newProduct);

        // The name index should now contain the new product
        assertNotNull(searcher.hybridSearchByName("Lambda Drone"),
                "Newly inserted product name should be in the index");
    }
}
