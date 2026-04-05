# CSC483-Algorithms-Assignment-U2022-5570193
## Assignment: Algorithm Design, Analysis, and Optimization for Real-World Systems

| Field | Details |
|---|---|
| **Student** | Bethel Messiah |
| **Matric No.** | U2022/5570193 |
| **Course** | CSC 483.1 |
| **Session** | 2025/2026 — First Semester |
| **Due Date** | April 5, 2026 |

---

## Compilation

### Manual (javac) — no build tool

```bash
# 1. Create output directory
mkdir -p out

# 2. Compile all main source files
javac -d out \
  src/search/*.java \
  src/sorting/*.java

# 3. Compile test files (requires JUnit 5 JARs in a lib/ folder)
javac -cp "out:lib/junit-jupiter-api-5.10.0.jar" \
  -d out \
  src/search/*.java \
  src/sorting/*.java
```

## Running the Benchmarks

### Question 1 — TechMart Search Performance

```bash
java -cp out search.SearchBenchmark
```

### Question 2 — Sorting Algorithms Benchmark

```bash
java -cp out sorting.SortBenchmark
```

---

## Running JUnit Tests

In **IntelliJ IDEA** or **Eclipse**: right-click any `*Test.java` → **Run Tests**.

---

## Known Limitations

- `SearchBenchmark` skips Insertion Sort at n = 100,000 (too slow for random data).
- Timing results vary by machine; figures in the PDF report reflect a single consistent run.
- Quick Sort uses median-of-three pivot selection — adversarial worst-case O(n²) is mitigated but theoretically possible.
- `addProduct` shifts in O(n); a balanced BST would be preferable for very high insert rates.

---

## References

1. Cormen, T.H. et al. *Introduction to Algorithms* — 4th Edition (CLRS)
2. Sedgewick, R. & Wayne, K. *Algorithms* — 4th Edition
3. Java Documentation: https://docs.oracle.com/en/java/
4. JUnit 5 User Guide: https://junit.org/junit5/docs/current/user-guide/
