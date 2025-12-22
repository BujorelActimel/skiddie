import Foundation

// This script runs an infinite loop
// Tests Skiddie's ability to stop/kill running processes

print("Starting infinite loop test...")
print("Press Cmd+Enter (or click Stop) to terminate this script")
print()

var counter = 0
while true {
    counter += 1
    print("Loop iteration: \(counter)")
    fflush(stdout)
    Thread.sleep(forTimeInterval: 0.5)
}

print("This should never print")
