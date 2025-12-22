// This script allocates large amounts of memory
// Tests Skiddie's handling of memory-intensive operations

println("Starting memory stress test...")
println("This will allocate progressively larger arrays")
println()

for (i in 1..20) {
    val size = i * 1000000 // 1 million elements per iteration
    println("Allocating array of size: ${size / 1000000}M elements")

    try {
        val largeArray = IntArray(size) { it }
        val sum = largeArray.sum()
        println("  ✓ Success! Sum: $sum")
    } catch (e: OutOfMemoryError) {
        System.err.println("  ✗ Out of memory at ${size / 1000000}M elements")
        break
    }

    Thread.sleep(100)
}

println()
println("Memory stress test complete!")
