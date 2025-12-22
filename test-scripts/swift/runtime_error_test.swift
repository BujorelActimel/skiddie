// This script compiles but crashes at runtime
// Tests Skiddie's handling of runtime exceptions

print("Starting runtime error test...")
print("This should print successfully")

// Force a runtime crash
let array = [1, 2, 3]
print("About to crash...")
print(array[10]) // This will crash with index out of bounds

print("This line should never be reached")
