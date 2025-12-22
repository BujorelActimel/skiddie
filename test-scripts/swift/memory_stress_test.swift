import Foundation

// This script allocates large amounts of memory
// Tests Skiddie's handling of memory-intensive operations

print("Starting memory stress test...")
print("This will allocate progressively larger arrays")
print()

for i in 1...20 {
    let size = i * 1000000 // 1 million elements per iteration
    print("Allocating array of size: \(size / 1000000)M elements")

    autoreleasepool {
        let largeArray = Array(0..<size)
        let sum = largeArray.reduce(0, +)
        print("  ✓ Success! Sum: \(sum)")
    }

    Thread.sleep(forTimeInterval: 0.1)
}

print()
print("Memory stress test complete!")
