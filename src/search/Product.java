package search;

/**
 * Represents a single product in the TechMart online electronics store catalog.
 *
 * <p>Each product is uniquely identified by its {@code productId}. The class
 * implements {@link Comparable} so that arrays of products can be sorted by ID,
 * which is a precondition for binary search.
 *
 * <p><b>Invariants:</b>
 * <ul>
 *   <li>{@code productId} must be a positive integer.</li>
 *   <li>{@code productName} must be non-null and non-blank.</li>
 *   <li>{@code price} and {@code stockQuantity} must be non-negative.</li>
 * </ul>
 *
 * @author  Bethel Messiah
 * @version 1.0
 * @see     ProductSearch
 */
public class Product implements Comparable<Product> {

    /** Unique identifier for this product. Must be a positive integer. */
    private final int productId;

    /** Human-readable name of the product (e.g. "UltraBook Pro 14"). */
    private final String productName;

    /** Category the product belongs to (e.g. "Laptop", "Phone"). */
    private final String category;

    /** Retail price in USD. Must be non-negative. */
    private double price;

    /** Number of units currently available in the warehouse. Must be non-negative. */
    private int stockQuantity;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a fully-initialised Product.
     *
     * @param productId     unique positive integer identifier
     * @param productName   non-null, non-blank product name
     * @param category      product category string (may be null or empty)
     * @param price         retail price; must be &ge; 0
     * @param stockQuantity units in stock; must be &ge; 0
     * @throws IllegalArgumentException if {@code productId} &le; 0,
     *         {@code productName} is blank, {@code price} &lt; 0,
     *         or {@code stockQuantity} &lt; 0
     */
    public Product(int productId, String productName, String category,
                   double price, int stockQuantity) {

        // Validate every field before assigning anything
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "productId must be a positive integer, got: " + productId);
        }
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("productName must not be null or blank.");
        }
        if (price < 0) {
            throw new IllegalArgumentException(
                    "price must be non-negative, got: " + price);
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "stockQuantity must be non-negative, got: " + stockQuantity);
        }

        this.productId     = productId;
        this.productName   = productName;
        this.category      = category;
        this.price         = price;
        this.stockQuantity = stockQuantity;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the unique product identifier used as the primary sort/search key.
     *
     * @return positive integer product ID
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Returns the display name of the product.
     *
     * @return non-null, non-blank product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Returns the category this product belongs to.
     *
     * @return category string, or {@code null} if not set
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the current retail price of the product.
     *
     * @return price in USD; always &ge; 0
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the number of units currently available in stock.
     *
     * @return stock quantity; always &ge; 0
     */
    public int getStockQuantity() {
        return stockQuantity;
    }

    // -------------------------------------------------------------------------
    // Setters (mutable fields only)
    // -------------------------------------------------------------------------

    /**
     * Updates the retail price of this product.
     *
     * @param price new price in USD; must be &ge; 0
     * @throws IllegalArgumentException if {@code price} is negative
     */
    public void setPrice(double price) {
        if (price < 0) {
            throw new IllegalArgumentException(
                    "price must be non-negative, got: " + price);
        }
        this.price = price;
    }

    /**
     * Updates the stock quantity for this product.
     *
     * @param stockQuantity new quantity; must be &ge; 0
     * @throws IllegalArgumentException if {@code stockQuantity} is negative
     */
    public void setStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new IllegalArgumentException(
                    "stockQuantity must be non-negative, got: " + stockQuantity);
        }
        this.stockQuantity = stockQuantity;
    }

    // -------------------------------------------------------------------------
    // Comparable / Object overrides
    // -------------------------------------------------------------------------

    /**
     * Compares this product to another by {@code productId} in ascending order.
     *
     * <p>This ordering is the natural sort order used by {@link java.util.Arrays#sort}
     * and is a precondition for {@link ProductSearch#binarySearchById}.
     *
     * @param other the product to compare against; must not be {@code null}
     * @return negative if this ID is smaller, zero if equal, positive if larger
     */
    @Override
    public int compareTo(Product other) {
        return Integer.compare(this.productId, other.productId);
    }

    /**
     * Returns a human-readable representation of this product.
     *
     * @return formatted string showing all five fields
     */
    @Override
    public String toString() {
        return String.format(
                "Product{id=%d, name='%s', category='%s', price=%.2f, stock=%d}",
                productId, productName, category, price, stockQuantity);
    }

    /**
     * Two products are equal if and only if they share the same {@code productId}.
     *
     * @param obj object to compare with
     * @return {@code true} if {@code obj} is a {@code Product} with the same ID
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Product)) return false;
        return this.productId == ((Product) obj).productId;
    }

    /**
     * Returns a hash code consistent with {@link #equals}: based solely on {@code productId}.
     *
     * @return hash code derived from productId
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(productId);
    }
}
